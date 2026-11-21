package com.example.data.audio

import androidx.compose.runtime.Immutable

@Immutable
data class FamousPassagePreset(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val surahNumber: Int,
    val startAyah: Int,
    val endAyah: Int
) {
    fun toTrack(): AudioPlaylistTrack = AudioPlaylistTrack(
        id = "famous_$id",
        surahNumber = surahNumber,
        startAyah = startAyah,
        endAyah = endAyah,
        labelAr = titleAr
    )
}

object PresetAudioPlaylists {

    val famousPassages: List<FamousPassagePreset> = listOf(
        FamousPassagePreset("kursi", "آية الكرسي", "سورة البقرة • ٢٥٥", 2, 255, 255),
        FamousPassagePreset("baqarah_end", "أواخر سورة البقرة", "سورة البقرة • ٢٨٤–٢٨٦", 2, 284, 286),
        FamousPassagePreset("imran_end", "أواخر آل عمران", "سورة آل عمران • ١٩٠–٢٠٠", 3, 190, 200),
        FamousPassagePreset("noor_ayah", "آية النور", "سورة النور • ٣٥", 24, 35, 35),
        FamousPassagePreset("hashr_end", "أواخر سورة الحشر", "سورة الحشر • ٢١–٢٤", 59, 21, 24),
        FamousPassagePreset("kahf_open", "فواتح سورة الكهف", "سورة الكهف • ١–١٠", 18, 1, 10)
    )

    val all: List<AudioPlaylist> = listOf(
        AudioPlaylist(
            id = "preset_sleep",
            titleAr = "تلاوات النوم والسكينة",
            subtitleAr = "السور والآيات المأثورة قبل النوم",
            iconKey = PlaylistIconKey.BEDTIME,
            accentIndex = 4,
            isPreset = true,
            tracks = listOf(
                AudioPlaylistTrack("ps_mulk", 67, 1, 30),
                AudioPlaylistTrack("ps_sajdah", 32, 1, 30),
                AudioPlaylistTrack("ps_kursi", 2, 255, 255, "آية الكرسي"),
                AudioPlaylistTrack("ps_baqarah_end", 2, 284, 286, "أواخر سورة البقرة"),
                AudioPlaylistTrack("ps_ikhlas", 112, 1, 4),
                AudioPlaylistTrack("ps_falaq", 113, 1, 5),
                AudioPlaylistTrack("ps_nas", 114, 1, 6)
            )
        ),
        AudioPlaylist(
            id = "preset_ruqyah",
            titleAr = "الرقية والتحصين اليومي",
            subtitleAr = "الفاتحة وآيات الحفظ والمعوذات",
            iconKey = PlaylistIconKey.SHIELD,
            accentIndex = 1,
            isPreset = true,
            tracks = listOf(
                AudioPlaylistTrack("pr_fatihah", 1, 1, 7),
                AudioPlaylistTrack("pr_kursi", 2, 255, 255, "آية الكرسي"),
                AudioPlaylistTrack("pr_baqarah_end", 2, 284, 286, "أواخر سورة البقرة"),
                AudioPlaylistTrack("pr_kafirun", 109, 1, 6),
                AudioPlaylistTrack("pr_ikhlas", 112, 1, 4),
                AudioPlaylistTrack("pr_falaq", 113, 1, 5),
                AudioPlaylistTrack("pr_nas", 114, 1, 6)
            )
        ),
        AudioPlaylist(
            id = "preset_friday",
            titleAr = "سنن الجمعة والكهف",
            subtitleAr = "نور بين الجمعتين وسور فجر الجمعة",
            iconKey = PlaylistIconKey.MOSQUE,
            accentIndex = 3,
            isPreset = true,
            tracks = listOf(
                AudioPlaylistTrack("pf_kahf", 18, 1, 110),
                AudioPlaylistTrack("pf_sajdah", 32, 1, 30),
                AudioPlaylistTrack("pf_insan", 76, 1, 31),
                AudioPlaylistTrack("pf_jumuah", 62, 1, 11)
            )
        ),
        AudioPlaylist(
            id = "preset_qiyam",
            titleAr = "تلاوات القيام والتدبّر",
            subtitleAr = "مختارات خاشعة للقيام والطمأنينة",
            iconKey = PlaylistIconKey.BOOK,
            accentIndex = 2,
            isPreset = true,
            tracks = listOf(
                AudioPlaylistTrack("pq_yasin", 36, 1, 83),
                AudioPlaylistTrack("pq_rahman", 55, 1, 78),
                AudioPlaylistTrack("pq_waqiah", 56, 1, 96),
                AudioPlaylistTrack("pq_qaf", 50, 1, 45)
            )
        ),
        AudioPlaylist(
            id = "preset_short",
            titleAr = "قصار السور (جزء عمّ)",
            subtitleAr = "مجموعة مختارة للحفظ والمراجعة السريعة",
            iconKey = PlaylistIconKey.STAR,
            accentIndex = 0,
            isPreset = true,
            tracks = listOf(
                AudioPlaylistTrack("psh_ala", 87, 1, 19),
                AudioPlaylistTrack("psh_ghashiyah", 88, 1, 26),
                AudioPlaylistTrack("psh_duha", 93, 1, 11),
                AudioPlaylistTrack("psh_sharh", 94, 1, 8),
                AudioPlaylistTrack("psh_tin", 95, 1, 8),
                AudioPlaylistTrack("psh_qadr", 97, 1, 5),
                AudioPlaylistTrack("psh_zalzalah", 99, 1, 8),
                AudioPlaylistTrack("psh_adiyat", 100, 1, 11),
                AudioPlaylistTrack("psh_qariah", 101, 1, 11),
                AudioPlaylistTrack("psh_ikhlas", 112, 1, 4),
                AudioPlaylistTrack("psh_falaq", 113, 1, 5),
                AudioPlaylistTrack("psh_nas", 114, 1, 6)
            )
        )
    )
}
