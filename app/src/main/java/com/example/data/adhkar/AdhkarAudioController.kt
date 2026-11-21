package com.example.data.adhkar

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.audio.AppAudioCoordinator
import com.example.data.audio.AppAudioOwner
import com.example.domain.model.DhikrItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class DhikrAudioState(
    val activeItemId: String? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val errorMessage: String? = null
)

class AdhkarAudioController(context: Context) {
    private val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .build(),
            true
        )
        .build()

    private val _state = MutableStateFlow(DhikrAudioState())
    val state: StateFlow<DhikrAudioState> = _state.asStateFlow()

    init {
        AppAudioCoordinator.register(AppAudioOwner.ADHKAR) { stop() }
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> _state.update { it.copy(isBuffering = true, errorMessage = null) }
                    Player.STATE_READY -> _state.update { it.copy(isBuffering = false, errorMessage = null) }
                    Player.STATE_ENDED, Player.STATE_IDLE -> {
                        _state.update { it.copy(isPlaying = false, isBuffering = false, activeItemId = null) }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _state.update {
                    it.copy(
                        activeItemId = null,
                        isPlaying = false,
                        isBuffering = false,
                        errorMessage = "تعذّر تشغيل المقطع الصوتي للذكر"
                    )
                }
            }
        })
    }

    fun togglePlay(item: DhikrItem) {
        val url = item.audioUrl.trim()
        if (url.isBlank()) return
        val cur = _state.value
        if (cur.activeItemId == item.id && (cur.isPlaying || cur.isBuffering)) {
            stop()
            return
        }
        _state.update {
            DhikrAudioState(activeItemId = item.id, isPlaying = false, isBuffering = true, errorMessage = null)
        }
        AppAudioCoordinator.claimFocus(AppAudioOwner.ADHKAR)
        player.setMediaItem(MediaItem.fromUri(HisnMuslimParser.normalizeHttpsUrl(url)))
        player.prepare()
        player.play()
    }

    fun stop() {
        player.stop()
        _state.value = DhikrAudioState()
    }

    fun release() {
        AppAudioCoordinator.unregister(AppAudioOwner.ADHKAR)
        player.release()
    }
}
