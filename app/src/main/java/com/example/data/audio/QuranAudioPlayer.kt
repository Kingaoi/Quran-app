package com.example.data.audio

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.domain.model.QuranReciters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class QuranAudioPlayer(
    context: Context,
    private val audioStorage: SurahAudioStorage = SurahAudioStorage(context),
    private val surahMetaLookup: (Int) -> Pair<String, Int>
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var progressJob: Job? = null
    private var pauseGapJob: Job? = null
    private var defaultReciterId: String = QuranReciters.all.first().id
    private lateinit var sleepTimer: AudioSleepTimerHelper

    private val player: ExoPlayer = ExoPlayer.Builder(appContext)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .build(),
            true
        )
        .build()

    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()
    private val _notificationConfig = MutableStateFlow(PlaybackNotificationConfig())
    val notificationConfig: StateFlow<PlaybackNotificationConfig> = _notificationConfig.asStateFlow()

    init {
        AppAudioCoordinator.register(AppAudioOwner.QURAN) { pauseFromCoordinator() }
        sleepTimer = AudioSleepTimerHelper(
            scope = scope,
            onTickRemainingSec = { sec -> _state.update { it.copy(sleepTimerRemainingSec = sec) } },
            onTimerExpired = { stop() }
        )
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying, durationMs = player.duration.coerceAtLeast(0L)) }
                if (isPlaying) startProgressTracking() else stopProgressTracking()
                syncNotificationService()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> _state.update { it.copy(isBuffering = true, errorMessage = null) }
                    Player.STATE_READY -> _state.update { it.copy(isBuffering = false, errorMessage = null, durationMs = player.duration.coerceAtLeast(0L), positionMs = player.currentPosition.coerceAtLeast(0L)) }
                    Player.STATE_ENDED -> { _state.update { it.copy(isBuffering = false, positionMs = it.durationMs) }; handleAyahCompleted() }
                    Player.STATE_IDLE -> _state.update { it.copy(isBuffering = false) }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                stopProgressTracking()
                _state.update { it.copy(isPlaying = false, isBuffering = false, errorMessage = "تعذّر تشغيل التلاوة، تأكد من اتصال الإنترنت") }
            }
        })
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && player.isPlaying) {
                _state.update { it.copy(positionMs = player.currentPosition.coerceAtLeast(0L), durationMs = player.duration.coerceAtLeast(0L)) }
                delay(250)
            }
        }
    }

    private fun stopProgressTracking() { progressJob?.cancel(); progressJob = null }

    fun setDefaultReciter(reciterId: String) {
        defaultReciterId = reciterId
        setReciter(reciterId)
    }

    fun setReciter(reciterId: String) {
        val reciter = QuranReciters.byId(reciterId)
        val wasPlaying = _state.value.isPlaying
        val surah = _state.value.currentSurah
        val ayah = _state.value.currentAyah
        _state.update { it.copy(reciter = reciter) }
        if (_state.value.isActive && wasPlaying) playAyahInternal(surah, ayah, clearScopes = false)
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.75f, 2.0f)
        player.setPlaybackSpeed(clamped)
        _state.update { it.copy(playbackSpeed = clamped) }
    }

    fun cyclePlaybackSpeed() {
        val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        val cur = _state.value.playbackSpeed
        val idx = speeds.indexOfFirst { kotlin.math.abs(it - cur) < 0.05f }
        setPlaybackSpeed(speeds[(idx + 1) % speeds.size])
    }

    fun updateNotificationConfig(config: PlaybackNotificationConfig) {
        if (_notificationConfig.value == config) return
        _notificationConfig.value = config
        syncNotificationService()
    }

    private fun syncNotificationService() {
        QuranPlaybackService.sync(appContext, _state.value, _notificationConfig.value)
    }

    fun startMemorizationLoop(config: MemorizationLoopConfig) {
        val (_, ayahCount) = surahMetaLookup(config.surahNumber)
        val s = config.startAyah.coerceIn(1, ayahCount.coerceAtLeast(1))
        val e = config.endAyah.coerceIn(s, ayahCount.coerceAtLeast(1))
        val clean = config.copy(
            enabled = true, startAyah = s, endAyah = e,
            ayahRepeatCount = config.ayahRepeatCount.coerceIn(1, 10),
            rangeRepeatCount = config.rangeRepeatCount.coerceIn(0, 20),
            pauseSeconds = config.pauseSeconds.coerceIn(0, 10),
            currentAyahRepeatIndex = 1, currentRangeRepeatIndex = 1
        )
        _state.update { it.copy(memorizationLoop = clean, repeatAyah = false, wirdAudioRange = null, playlistSession = null) }
        playAyahInternal(clean.surahNumber, clean.startAyah, clearScopes = false)
    }

    fun clearMemorizationLoop() { pauseGapJob?.cancel(); _state.update { it.copy(isWaitingPause = false, memorizationLoop = it.memorizationLoop.copy(enabled = false)) } }

    fun togglePlayWirdRange(range: WirdAudioRange) {
        val cur = _state.value
        if (cur.isActive && cur.wirdAudioRange?.wirdIndex == range.wirdIndex) { togglePlayPause(); return }
        pauseGapJob?.cancel()
        _state.update { it.copy(wirdAudioRange = range, playlistSession = null, repeatAyah = false, stopAfterCurrentSurah = false, memorizationLoop = it.memorizationLoop.copy(enabled = false)) }
        playAyahInternal(range.startSurah, range.startAyah, clearScopes = false)
    }

    fun playPlaylist(playlist: AudioPlaylist, startTrackIndex: Int = 0) {
        val session = AudioPlaylistPlayerHelper.buildSession(playlist, startTrackIndex, surahMetaLookup) ?: return
        val firstTrack = session.currentTrack ?: return
        pauseGapJob?.cancel(); pauseGapJob = null
        val reciter = playlist.preferredReciterId?.let(QuranReciters::byId) ?: QuranReciters.byId(defaultReciterId)
        _state.update {
            it.copy(
                reciter = reciter, playlistSession = session, wirdAudioRange = null,
                repeatAyah = false, stopAfterCurrentSurah = false, isWaitingPause = false,
                memorizationLoop = it.memorizationLoop.copy(enabled = false)
            )
        }
        playAyahInternal(firstTrack.surahNumber, firstTrack.startAyah, clearScopes = false)
    }

    fun nextPlaylistTrack() {
        val session = _state.value.playlistSession ?: return
        applyPlaylistStep(AudioPlaylistPlayerHelper.advanceToNextTrack(session))
    }

    fun previousPlaylistTrack() {
        val session = _state.value.playlistSession ?: return
        AudioPlaylistPlayerHelper.jumpToPreviousTrack(session)?.let(::applyPlaylistStep)
    }

    fun togglePlaylistSessionRepeat() {
        _state.update { st -> st.playlistSession?.let { st.copy(playlistSession = it.copy(repeatPlaylist = !it.repeatPlaylist)) } ?: st }
    }

    private fun applyPlaylistStep(step: AudioPlaylistPlayerHelper.StepOutcome) {
        when (step) {
            AudioPlaylistPlayerHelper.StepOutcome.StopPlayback -> stop()
            is AudioPlaylistPlayerHelper.StepOutcome.PlayAyah -> {
                _state.update { it.copy(playlistSession = step.updatedSession) }
                playAyahInternal(step.surah, step.ayah, clearScopes = false)
            }
        }
    }

    fun playAyah(surah: Int, ayah: Int) = playAyahInternal(surah, ayah, clearScopes = true)

    private fun playAyahInternal(surah: Int, ayah: Int, clearScopes: Boolean) {
        pauseGapJob?.cancel(); pauseGapJob = null
        AppAudioCoordinator.claimFocus(AppAudioOwner.QURAN)
        val (nameAr, ayahCount) = surahMetaLookup(surah)
        val clampedAyah = ayah.coerceIn(1, ayahCount.coerceAtLeast(1))
        val activeReciter = if (clearScopes && _state.value.playlistSession != null) QuranReciters.byId(defaultReciterId) else _state.value.reciter
        val localFile = if (activeReciter.isSurahLevelAudio) audioStorage.getLocalSurahFile(activeReciter.id, surah)
        else audioStorage.getLocalAyahFile(activeReciter.id, surah, clampedAyah)
        val mediaUri = localFile?.let { Uri.fromFile(it).toString() } ?: activeReciter.ayahAudioUrl(surah, clampedAyah)
        _state.update {
            it.copy(
                isActive = true, isBuffering = localFile == null, isWaitingPause = false, reciter = activeReciter,
                currentSurah = surah, currentAyah = clampedAyah, totalAyahsInSurah = ayahCount,
                surahNameAr = nameAr, positionMs = 0L, durationMs = 0L, errorMessage = null,
                wirdAudioRange = if (clearScopes) null else it.wirdAudioRange,
                playlistSession = if (clearScopes) null else it.playlistSession
            )
        }
        player.setPlaybackSpeed(_state.value.playbackSpeed)
        player.setMediaItem(MediaItem.fromUri(mediaUri))
        player.prepare(); player.play(); syncNotificationService()
    }

    fun seekRelative(deltaMs: Long) = seekTo(player.currentPosition.coerceAtLeast(0L) + deltaMs)

    fun seekTo(positionMs: Long) {
        val dur = player.duration.coerceAtLeast(0L)
        if (dur > 0L) { val clamped = positionMs.coerceIn(0L, dur); player.seekTo(clamped); _state.update { it.copy(positionMs = clamped) } }
    }

    fun togglePlayPause() {
        val cur = _state.value
        if (!cur.isActive) { playAyahInternal(cur.currentSurah, cur.currentAyah, clearScopes = false); return }
        if (player.isPlaying) { pauseGapJob?.cancel(); pauseGapJob = null; _state.update { it.copy(isWaitingPause = false) }; player.pause() }
        else if (cur.isWaitingPause || player.playbackState == Player.STATE_ENDED || player.playbackState == Player.STATE_IDLE) {
            pauseGapJob?.cancel(); pauseGapJob = null; _state.update { it.copy(isWaitingPause = false) }
            playAyahInternal(cur.currentSurah, cur.currentAyah, clearScopes = false)
        } else { AppAudioCoordinator.claimFocus(AppAudioOwner.QURAN); player.play() }
    }

    private fun pauseFromCoordinator() {
        pauseGapJob?.cancel(); pauseGapJob = null
        if (player.isPlaying) player.pause()
        if (_state.value.isWaitingPause || _state.value.isBuffering) _state.update { it.copy(isWaitingPause = false, isBuffering = false) }
    }

    fun nextAyah() {
        val cur = _state.value
        cur.playlistSession?.let { s -> AudioPlaylistPlayerHelper.onManualNextAyah(cur, s)?.let(::applyPlaylistStep); return }
        if (cur.reciter.isSurahLevelAudio) { if (cur.currentSurah < 114) playAyahInternal(cur.currentSurah + 1, 1, false); return }
        if (cur.currentAyah < cur.totalAyahsInSurah) playAyahInternal(cur.currentSurah, cur.currentAyah + 1, false)
        else if (cur.currentSurah < 114) playAyahInternal(cur.currentSurah + 1, 1, false)
    }

    fun previousAyah() {
        val cur = _state.value
        cur.playlistSession?.let { s -> AudioPlaylistPlayerHelper.onManualPreviousAyah(cur, s)?.let(::applyPlaylistStep); return }
        if (cur.reciter.isSurahLevelAudio) { if (cur.currentSurah > 1) playAyahInternal(cur.currentSurah - 1, 1, false); return }
        if (cur.currentAyah > 1) playAyahInternal(cur.currentSurah, cur.currentAyah - 1, false)
        else if (cur.currentSurah > 1) playAyahInternal(cur.currentSurah - 1, surahMetaLookup(cur.currentSurah - 1).second, false)
    }

    fun toggleRepeatAyah() = _state.update { it.copy(repeatAyah = !it.repeatAyah) }

    fun setSleepTimerMinutes(minutes: Int) {
        if (minutes <= 0) sleepTimer.cancel() else { _state.update { it.copy(stopAfterCurrentSurah = false) }; sleepTimer.startMinutes(minutes) }
    }

    fun toggleStopAfterSurah() { sleepTimer.cancel(); _state.update { it.copy(stopAfterCurrentSurah = !it.stopAfterCurrentSurah) } }

    fun stop() {
        sleepTimer.cancel(); pauseGapJob?.cancel(); stopProgressTracking(); player.stop()
        _state.update {
            it.copy(
                isActive = false, isPlaying = false, isBuffering = false, isWaitingPause = false,
                stopAfterCurrentSurah = false, wirdAudioRange = null, playlistSession = null,
                memorizationLoop = it.memorizationLoop.copy(enabled = false), positionMs = 0L, durationMs = 0L
            )
        }
        syncNotificationService()
    }

    private fun handleAyahCompleted() {
        val cur = _state.value
        val loop = cur.memorizationLoop
        if (loop.enabled) { handleMemorizationLoopStep(cur, loop); return }
        cur.playlistSession?.let { session ->
            val track = session.currentTrack
            val atTrackEnd = cur.reciter.isSurahLevelAudio || (track != null && cur.currentSurah == track.surahNumber && cur.currentAyah >= track.endAyah)
            when {
                cur.repeatAyah -> playAyahInternal(cur.currentSurah, cur.currentAyah, false)
                cur.stopAfterCurrentSurah && atTrackEnd -> stop()
                else -> applyPlaylistStep(AudioPlaylistPlayerHelper.onAyahCompleted(cur, session))
            }
            return
        }
        val wirdRange = cur.wirdAudioRange
        if (cur.reciter.isSurahLevelAudio) {
            when {
                cur.repeatAyah -> playAyahInternal(cur.currentSurah, 1, false)
                wirdRange != null && cur.currentSurah >= wirdRange.endSurah -> stop()
                cur.stopAfterCurrentSurah -> stop()
                cur.autoAdvance && cur.currentSurah < 114 -> playAyahInternal(cur.currentSurah + 1, 1, false)
                else -> _state.update { it.copy(isPlaying = false) }
            }
            return
        }
        when {
            cur.repeatAyah -> playAyahInternal(cur.currentSurah, cur.currentAyah, false)
            wirdRange != null && wirdRange.isEnd(cur.currentSurah, cur.currentAyah) -> stop()
            cur.autoAdvance && cur.currentAyah < cur.totalAyahsInSurah -> playAyahInternal(cur.currentSurah, cur.currentAyah + 1, false)
            cur.stopAfterCurrentSurah -> stop()
            cur.autoAdvance && cur.currentSurah < 114 -> playAyahInternal(cur.currentSurah + 1, 1, false)
            else -> _state.update { it.copy(isPlaying = false) }
        }
    }

    private fun handleMemorizationLoopStep(cur: AudioPlaybackState, loop: MemorizationLoopConfig) {
        val delayMs = loop.pauseSeconds * 1000L
        fun schedulePlay(surah: Int, ayah: Int, updatedLoop: MemorizationLoopConfig) {
            _state.update { it.copy(memorizationLoop = updatedLoop, isWaitingPause = delayMs > 0L) }
            if (delayMs > 0L) {
                pauseGapJob?.cancel()
                pauseGapJob = scope.launch { delay(delayMs); if (isActive && _state.value.memorizationLoop.enabled) playAyahInternal(surah, ayah, false) }
            } else playAyahInternal(surah, ayah, false)
        }
        if (loop.currentAyahRepeatIndex < loop.ayahRepeatCount) schedulePlay(cur.currentSurah, cur.currentAyah, loop.copy(currentAyahRepeatIndex = loop.currentAyahRepeatIndex + 1))
        else if (cur.currentAyah < loop.endAyah && cur.currentSurah == loop.surahNumber) schedulePlay(loop.surahNumber, cur.currentAyah + 1, loop.copy(currentAyahRepeatIndex = 1))
        else if (loop.rangeRepeatCount == 0 || loop.currentRangeRepeatIndex < loop.rangeRepeatCount) schedulePlay(loop.surahNumber, loop.startAyah, loop.copy(currentAyahRepeatIndex = 1, currentRangeRepeatIndex = loop.currentRangeRepeatIndex + 1))
        else _state.update { it.copy(isPlaying = false, isWaitingPause = false, memorizationLoop = loop.copy(enabled = false)) }
    }

    fun release() { AppAudioCoordinator.unregister(AppAudioOwner.QURAN); sleepTimer.cancel(); pauseGapJob?.cancel(); stopProgressTracking(); scope.cancel(); player.release() }
}
