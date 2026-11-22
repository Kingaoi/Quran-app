package com.example.ui.viewmodel

import com.example.data.audio.AudioPlaybackState
import com.example.data.audio.AudioPlaylist
import com.example.data.audio.MemorizationLoopConfig
import com.example.data.audio.PlaybackNotificationConfig
import com.example.data.audio.QuranAudioPlayer
import com.example.data.audio.RiwayahRecitersRepository
import com.example.data.audio.WirdAudioRange
import com.example.domain.model.QuranReciters
import com.example.domain.model.ReciterOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuranPlaybackDelegate(
    private val scope: CoroutineScope,
    private val audioPlayer: QuranAudioPlayer,
    private val riwayahRecitersRepo: RiwayahRecitersRepository
) {
    val playbackState: StateFlow<AudioPlaybackState> = audioPlayer.state

    private val _reciterCatalog = MutableStateFlow(QuranReciters.all)
    val reciterCatalog: StateFlow<List<ReciterOption>> = _reciterCatalog.asStateFlow()

    fun loadMultiRiwayahReciters(activeReciterIdProvider: () -> String) {
        scope.launch {
            riwayahRecitersRepo.loadMultiRiwayahReciters()
            _reciterCatalog.value = QuranReciters.combinedCatalog()
            audioPlayer.setDefaultReciter(activeReciterIdProvider())
        }
    }

    fun setDefaultReciter(reciterId: String) = audioPlayer.setDefaultReciter(reciterId)

    fun updateNotificationConfig(config: PlaybackNotificationConfig) =
        audioPlayer.updateNotificationConfig(config)

    fun playAyah(surah: Int, ayah: Int) = audioPlayer.playAyah(surah, ayah)

    fun togglePlayWirdRange(range: WirdAudioRange) = audioPlayer.togglePlayWirdRange(range)

    fun playPlaylist(playlist: AudioPlaylist, startTrackIndex: Int = 0) =
        audioPlayer.playPlaylist(playlist, startTrackIndex)

    fun togglePlayPlaylist(playlist: AudioPlaylist) {
        val cur = playbackState.value
        if (cur.isActive && cur.playlistSession?.playlistId == playlist.id) {
            audioPlayer.togglePlayPause()
        } else {
            audioPlayer.playPlaylist(playlist, 0)
        }
    }

    fun nextPlaylistTrack() = audioPlayer.nextPlaylistTrack()
    fun previousPlaylistTrack() = audioPlayer.previousPlaylistTrack()
    fun togglePlaylistSessionRepeat() = audioPlayer.togglePlaylistSessionRepeat()
    fun seekTo(positionMs: Long) = audioPlayer.seekTo(positionMs)
    fun togglePlayPause() = audioPlayer.togglePlayPause()

    fun nextAyah(fallbackPlayCurrentPage: () -> Unit) {
        if (!playbackState.value.isActive) fallbackPlayCurrentPage() else audioPlayer.nextAyah()
    }

    fun previousAyah(fallbackPlayCurrentPage: () -> Unit) {
        if (!playbackState.value.isActive) fallbackPlayCurrentPage() else audioPlayer.previousAyah()
    }

    fun toggleRepeatAyah() = audioPlayer.toggleRepeatAyah()
    fun setPlaybackSpeed(speed: Float) = audioPlayer.setPlaybackSpeed(speed)
    fun cyclePlaybackSpeed() = audioPlayer.cyclePlaybackSpeed()
    fun startMemorizationLoop(config: MemorizationLoopConfig) = audioPlayer.startMemorizationLoop(config)
    fun clearMemorizationLoop() = audioPlayer.clearMemorizationLoop()
    fun setSleepTimerMinutes(minutes: Int?) = audioPlayer.setSleepTimerMinutes(minutes ?: 0)
    fun toggleStopAfterSurah() = audioPlayer.toggleStopAfterSurah()
    fun stop() = audioPlayer.stop()
    fun release() = audioPlayer.release()
}
