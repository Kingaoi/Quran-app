package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.QuranFontOption

const val QURAN_LINE_HEIGHT_RATIO = 1.85f

val AlyamamaFontFamily = FontFamily(
    Font(R.font.alyamama_light, FontWeight.Light),
    Font(R.font.alyamama_regular, FontWeight.Normal),
    Font(R.font.alyamama_medium, FontWeight.Medium),
    Font(R.font.alyamama_semibold, FontWeight.SemiBold),
    Font(R.font.alyamama_bold, FontWeight.Bold)
)

val ScheherazadeNewFontFamily = FontFamily(
    Font(R.font.scheherazade_new, FontWeight.Normal)
)

val AmiriQuranFontFamily = FontFamily(
    Font(R.font.amiri_quran, FontWeight.Normal)
)

val AmiriFontFamily = FontFamily(
    Font(R.font.amiri, FontWeight.Normal)
)

val LocalQuranFontFamily = compositionLocalOf { AlyamamaFontFamily }

fun resolveQuranFontFamily(
    option: QuranFontOption,
    uthmanicFontFamily: FontFamily? = null
): FontFamily = when (option) {
    QuranFontOption.SCHEHERAZADE_NEW -> ScheherazadeNewFontFamily
    QuranFontOption.ALYAMAMA -> AlyamamaFontFamily
    QuranFontOption.AMIRI_QURAN -> AmiriQuranFontFamily
    QuranFontOption.AMIRI -> AmiriFontFamily
    QuranFontOption.UTHMANI_HAFS -> uthmanicFontFamily ?: AlyamamaFontFamily
}

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = AlyamamaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = AlyamamaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AlyamamaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AlyamamaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 26.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AlyamamaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AlyamamaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AlyamamaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AlyamamaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AlyamamaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AlyamamaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AlyamamaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AlyamamaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AlyamamaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp
    )
)
