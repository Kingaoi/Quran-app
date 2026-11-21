package com.example.domain.model

import androidx.compose.runtime.compositionLocalOf

/**
 * Controls numeral presentation across the entire app (Arabic-Indic ١٢٣ vs Western 123).
 * Updated reactively from ReaderPreferences and CompositionLocal so changing the setting
 * immediately updates every screen, formatter, and widget.
 */
object AppDigitConfig {
    @Volatile
    var useWesternDigits: Boolean = false

    fun formatInt(value: Int, useWestern: Boolean = useWesternDigits): String {
        return value.toString().localizeDigits(useWestern)
    }

    fun formatLong(value: Long, useWestern: Boolean = useWesternDigits): String {
        return value.toString().localizeDigits(useWestern)
    }
}

val LocalUseWesternDigits = compositionLocalOf { false }

private val ARABIC_INDIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

fun String.localizeDigits(useWestern: Boolean = AppDigitConfig.useWesternDigits): String {
    if (isEmpty()) return this
    return buildString(length) {
        for (ch in this@localizeDigits) {
            when {
                useWestern && ch in '٠'..'٩' -> append(('0'.code + (ch.code - '٠'.code)).toChar())
                !useWestern && ch in '0'..'9' -> append(ARABIC_INDIC_DIGITS[ch - '0'])
                else -> append(ch)
            }
        }
    }
}

fun Int.toAppDigits(useWestern: Boolean = AppDigitConfig.useWesternDigits): String =
    AppDigitConfig.formatInt(this, useWestern)

fun Long.toAppDigits(useWestern: Boolean = AppDigitConfig.useWesternDigits): String =
    AppDigitConfig.formatLong(this, useWestern)
