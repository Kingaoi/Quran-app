package com.example.data.audio

import androidx.compose.runtime.Immutable
import com.example.domain.model.QuranReciters
import com.example.domain.model.ReciterOption

enum class PlaybackNotificationButton(
    val id: String,
    val labelAr: String
) {
    PREV_NEXT("prev_next", "السابق والتالي"),
    REPEAT_AYAH("repeat", "تكرار الآية"),
    CYCLE_SPEED("speed", "تغيير السرعة"),
    SEEK_10S("seek_10s", "تقديم/إرجاع ١٠ ث"),
    STOP_CLOSE("stop", "زر إيقاف وإغلاق");

    companion object {
        val defaultButtons: Set<PlaybackNotificationButton> = setOf(
            PREV_NEXT,
            REPEAT_AYAH,
            STOP_CLOSE
        )

        fun fromIds(ids: Set<String>?): Set<PlaybackNotificationButton> {
            if (ids == null) return defaultButtons
            return entries.filterTo(linkedSetOf()) { it.id in ids }
        }
    }
}

@Immutable
data class PlaybackNotificationConfig(
    val enabled: Boolean = true,
    val buttons: Set<PlaybackNotificationButton> = PlaybackNotificationButton.defaultButtons,
    val showWhenPaused: Boolean = true
) {
    fun hasButton(button: PlaybackNotificationButton): Boolean = button in buttons

    fun toggledButton(button: PlaybackNotificationButton): PlaybackNotificationConfig {
        val updated = if (button in buttons) buttons - button else buttons + button
        return copy(buttons = updated)
    }

    fun buttonIds(): Set<String> = buttons.mapTo(linkedSetOf()) { it.id }
}

@Immutable
data class PlaybackNotifActions(
    val onSetEnabled: (Boolean) -> Unit = {},
    val onToggleButton: (PlaybackNotificationButton) -> Unit = {},
    val onSetShowWhenPaused: (Boolean) -> Unit = {}
)

@Immutable
data class MemorizationLoopConfig(
    val enabled: Boolean = false,
    val surahNumber: Int = 1,
    val startAyah: Int = 1,
    val endAyah: Int = 7,
    val ayahRepeatCount: Int = 1,
    val rangeRepeatCount: Int = 3,
    val pauseSeconds: Int = 0,
    val currentAyahRepeatIndex: Int = 1,
    val currentRangeRepeatIndex: Int = 1
)

@Immutable
data class WirdAudioRange(
    val wirdIndex: Int,
    val startSurah: Int,
    val startAyah: Int,
    val endSurah: Int,
    val endAyah: Int
) {
    fun isEnd(surah: Int, ayah: Int): Boolean = surah == endSurah && ayah == endAyah
}

@Immutable
data class AudioPlaybackState(
    val isActive: Boolean = false,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isWaitingPause: Boolean = false,
    val currentSurah: Int = 1,
    val currentAyah: Int = 1,
    val totalAyahsInSurah: Int = 7,
    val surahNameAr: String = "الفاتحة",
    val reciter: ReciterOption = QuranReciters.all.first(),
    val repeatAyah: Boolean = false,
    val autoAdvance: Boolean = true,
    val playbackSpeed: Float = 1.0f,
    val memorizationLoop: MemorizationLoopConfig = MemorizationLoopConfig(),
    val wirdAudioRange: WirdAudioRange? = null,
    val playlistSession: PlaylistPlaybackSession? = null,
    val sleepTimerRemainingSec: Int? = null,
    val stopAfterCurrentSurah: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val errorMessage: String? = null
)

@Immutable
data class SurahAudioDownloadState(
    val reciterId: String = QuranReciters.all.first().id,
    val surahNumber: Int = 1,
    val downloadedAyahs: Int = 0,
    val totalAyahs: Int = 7,
    val isDownloading: Boolean = false,
    val errorMessage: String? = null
) {
    val isComplete: Boolean get() = totalAyahs > 0 && downloadedAyahs >= totalAyahs
}
