package com.example.data.local

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.data.audio.PlaybackNotificationConfig
import com.example.ui.components.MushafSvgMarkerRecolorer

enum class AppTheme(val labelAr: String, val subtitleAr: String) {
    SYSTEM("تلقائي", "العتيق أو الليلي"),
    SEPIA("عتيق", "طابع تراثي"),
    DARK("ليلي", "قراءة ليلية");

    companion object {
        fun fromStoredName(name: String?): AppTheme = when (name) {
            "DARK" -> DARK
            "SEPIA", "CREAM" -> SEPIA
            "SYSTEM" -> SYSTEM
            else -> SYSTEM
        }
    }
}

enum class QuranFontOption(val labelAr: String) {
    ALYAMAMA("خط اليمامة"),
    SCHEHERAZADE_NEW("شهرزاد الجديد"),
    AMIRI_QURAN("الأميري القرآني"),
    AMIRI("الأميري"),
    UTHMANI_HAFS("عثماني حفص")
}

enum class AyahMarkerColorPreset(
    val code: String,
    val labelAr: String,
    val lightHex: String,
    val nightHex: String,
    val lightPreviewColor: Color,
    val nightPreviewColor: Color
) {
    EMERALD("em", "أخضر", "#1B6B46", "#6CCB9B", Color(0xFF1B6B46), Color(0xFF6CCB9B)),
    OLIVE("ol", "زيتوني", "#4F6E4E", "#8FAE8B", Color(0xFF4F6E4E), Color(0xFF8FAE8B)),
    GOLD("gd", "ذهبي", "#9E6E1B", "#E2B55A", Color(0xFF9E6E1B), Color(0xFFE2B55A)),
    BLUE("bl", "أزرق", "#1B5694", "#7BB4EC", Color(0xFF1B5694), Color(0xFF7BB4EC)),
    CRIMSON("cr", "عنابي", "#8C2535", "#E48391", Color(0xFF8C2535), Color(0xFFE48391)),
    DEFAULT("def", "أسود", "#231F20", "#D0CCBA", Color(0xFF231F20), Color(0xFFD0CCBA))
}

enum class AyahMarkerColorTarget(val code: String, val labelAr: String) {
    NUMBER_AND_ORNAMENT("b", "الرقم والزخرفة"),
    NUMBER_ONLY("n", "الرقم فقط"),
    ORNAMENT_ONLY("o", "الزخرفة فقط")
}

enum class AyahHighlightColorPreset(
    val code: String,
    val labelAr: String,
    val lightColorInt: Long,
    val nightColorInt: Long
) {
    FOLLOW_MARKER("auto", "يتبع لون الآيات", 0xFF4F6E4EL, 0xFF8FAE8BL),
    GOLD("gold", "ذهبي دافئ", 0xFF9E6E1BL, 0xFFE2B55AL),
    OLIVE("olive", "زيتوني", 0xFF4F6E4EL, 0xFF8FAE8BL),
    EMERALD("emerald", "أخضر زمردي", 0xFF1B6B46L, 0xFF6CCB9BL),
    BLUE("blue", "سماوي هادئ", 0xFF1B5694L, 0xFF7BB4ECL),
    CRIMSON("crimson", "عنابي تراثي", 0xFF8C2535L, 0xFFE48391L);

    fun resolveColor(isNightPaper: Boolean, markerAccentColor: Color): Color =
        if (this == FOLLOW_MARKER) markerAccentColor
        else Color(if (isNightPaper) nightColorInt else lightColorInt)
}

enum class AyahHighlightStyle(val labelAr: String) {
    FILL_AND_BORDER("ممتلئ بإطار"),
    SOFT_FILL("تظليل ناعم"),
    BORDER_ONLY("إطار فقط")
}

enum class RiwayahEdition(
    val slug: String,
    val titleAr: String
) {
    HAFS("hafs-kfqc", "حفص عن عاصم"),
    WARSH("warsh-kfqc", "ورش عن نافع"),
    QALON("qalon-kfqc", "قالون عن نافع"),
    DOURI("douri-kfqc", "الدوري عن أبي عمرو"),
    SHUBAH("shubah-kfqc", "شعبة عن عاصم");

    companion object {
        fun fromSlug(slug: String): RiwayahEdition =
            entries.firstOrNull { it.slug == slug } ?: HAFS
    }
}

@Immutable
data class ReaderSettings(
    val lastPage: Int = 1,
    val lastSurah: Int = 1,
    val lastAyah: Int = 1,
    val riwayah: RiwayahEdition = RiwayahEdition.HAFS,
    val fontSizeSp: Float = 26f,
    val quranFont: QuranFontOption = QuranFontOption.ALYAMAMA,
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val ayahMarkerColor: AyahMarkerColorPreset = AyahMarkerColorPreset.EMERALD,
    val ayahMarkerColorTarget: AyahMarkerColorTarget = AyahMarkerColorTarget.NUMBER_AND_ORNAMENT,
    val ayahHighlightColor: AyahHighlightColorPreset = AyahHighlightColorPreset.FOLLOW_MARKER,
    val ayahHighlightStyle: AyahHighlightStyle = AyahHighlightStyle.FILL_AND_BORDER,
    val ayahHighlightAlpha: Float = 0.22f,
    val surahBannerEnabled: Boolean = true,
    val selectedReciterId: String = "Abdul_Basit_Murattal_192kbps",
    val bookmarkedPages: Set<Int> = emptySet(),
    val useWesternDigits: Boolean = false,
    val keepScreenOn: Boolean = true,
    val readerDimmerAlpha: Float = 0f,
    val twoPageSpreadOnTablet: Boolean = true,
    val autoAdvanceDhikr: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val wirdPureReadingMode: Boolean = true,
    val hideWirdBannerOutsideWird: Boolean = false,
    val playbackNotificationConfig: PlaybackNotificationConfig = PlaybackNotificationConfig()
) {
    fun mushafMarkerColorKey(isNightPaper: Boolean): String {
        val mode = if (isNightPaper) "n" else "d"
        if (ayahMarkerColor == AyahMarkerColorPreset.DEFAULT) {
            return if (surahBannerEnabled) "def_${mode}_s2" else "def"
        }
        val bannerSuffix = if (surahBannerEnabled) "_s2" else ""
        return "${ayahMarkerColor.code}_${ayahMarkerColorTarget.code}_$mode$bannerSuffix"
    }

    fun resolveSvgOrnamentHex(isNightPaper: Boolean): String? {
        if (ayahMarkerColor == AyahMarkerColorPreset.DEFAULT) return null
        if (ayahMarkerColorTarget == AyahMarkerColorTarget.NUMBER_ONLY) return null
        return resolveTargetSvgHex(isNightPaper)
    }

    fun resolveSvgNumberHex(isNightPaper: Boolean): String? {
        if (ayahMarkerColor == AyahMarkerColorPreset.DEFAULT) return null
        if (ayahMarkerColorTarget == AyahMarkerColorTarget.ORNAMENT_ONLY) return null
        return resolveTargetSvgHex(isNightPaper)
    }

    fun resolveSvgSurahBannerHex(isNightPaper: Boolean): String? {
        if (!surahBannerEnabled) return null
        return resolveTargetSvgHex(isNightPaper)
    }

    fun resolveAyahHighlightColor(isNightPaper: Boolean, markerAccentColor: Color): Color =
        ayahHighlightColor.resolveColor(isNightPaper, markerAccentColor)

    private fun resolveTargetSvgHex(isNightPaper: Boolean): String = if (isNightPaper) {
        MushafSvgMarkerRecolorer.invertHexForNightFilter(ayahMarkerColor.nightHex)
    } else {
        ayahMarkerColor.lightHex
    }
}
