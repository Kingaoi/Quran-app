package com.example.data.audio

import androidx.compose.runtime.Immutable
import com.example.domain.model.toAppDigits
import java.util.UUID

enum class PlaylistIconKey(
    val id: String,
    val labelAr: String
) {
    BEDTIME("bedtime", "سكينة ونوم"),
    SHIELD("shield", "تحصين ورقية"),
    MOSQUE("mosque", "جمعة وقيام"),
    BOOK("book", "تدبّر"),
    STAR("star", "مختارات"),
    QUEUE("queue", "تلاوة");

    companion object {
        fun fromId(id: String?): PlaylistIconKey =
            entries.firstOrNull { it.id == id } ?: QUEUE
    }
}

@Immutable
data class AudioPlaylistTrack(
    val id: String = UUID.randomUUID().toString(),
    val surahNumber: Int,
    val startAyah: Int = 1,
    val endAyah: Int = 7,
    val labelAr: String? = null
) {
    fun isFullSurah(surahAyahCount: Int): Boolean =
        startAyah <= 1 && endAyah >= surahAyahCount.coerceAtLeast(1)

    fun displayRangeAr(surahAyahCount: Int): String = when {
        !labelAr.isNullOrBlank() -> labelAr
        isFullSurah(surahAyahCount) -> "السورة كاملة (${surahAyahCount.toAppDigits()} آية)"
        startAyah == endAyah -> "الآية ${startAyah.toAppDigits()}"
        else -> "الآيات ${startAyah.toAppDigits()}–${endAyah.toAppDigits()}"
    }
}

@Immutable
data class AudioPlaylist(
    val id: String = UUID.randomUUID().toString(),
    val titleAr: String,
    val subtitleAr: String = "",
    val iconKey: PlaylistIconKey = PlaylistIconKey.QUEUE,
    val accentIndex: Int = 0,
    val isPreset: Boolean = false,
    val repeatPlaylist: Boolean = false,
    val shuffleEnabled: Boolean = false,
    val preferredReciterId: String? = null,
    val tracks: List<AudioPlaylistTrack> = emptyList(),
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Immutable
data class PlaylistPlaybackSession(
    val playlistId: String,
    val playlistTitleAr: String,
    val tracks: List<AudioPlaylistTrack>,
    val currentTrackIndex: Int = 0,
    val repeatPlaylist: Boolean = false,
    val shuffleEnabled: Boolean = false
) {
    val totalTracks: Int get() = tracks.size
    val currentTrack: AudioPlaylistTrack? get() = tracks.getOrNull(currentTrackIndex)
    val hasNext: Boolean get() = tracks.isNotEmpty() && (currentTrackIndex + 1 < tracks.size || repeatPlaylist)
    val hasPrevious: Boolean get() = tracks.isNotEmpty() && (currentTrackIndex > 0 || repeatPlaylist)
}
