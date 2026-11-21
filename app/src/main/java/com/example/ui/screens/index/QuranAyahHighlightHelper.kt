package com.example.ui.screens.index

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.domain.quran.QuranRootLexicon

internal fun buildHighlightedAyahText(
    ayahText: String,
    matchedTokens: List<String>,
    highlightColor: Color,
    highlightBackground: Color
): AnnotatedString {
    if (ayahText.isBlank() || matchedTokens.isEmpty()) return AnnotatedString(ayahText)
    val normTargets = matchedTokens
        .map { QuranRootLexicon.normalizeToken(it) }
        .filter { it.isNotBlank() }
        .toSet()
    val deAlefedTargets = normTargets.map { stripMedialAlef(it) }.toSet()

    val rawTokens = ayahText.split(" ")
    return buildAnnotatedString {
        rawTokens.forEachIndexed { idx, rawToken ->
            if (idx > 0) append(" ")
            val normToken = QuranRootLexicon.normalizeToken(rawToken)
            val deAlefToken = stripMedialAlef(normToken)
            val isMatch = normToken.isNotBlank() && (
                normToken in normTargets ||
                    deAlefToken in deAlefedTargets ||
                    normTargets.any { t ->
                        t.length >= 3 && (normToken.contains(t) || t == deAlefToken)
                    }
                )
            if (isMatch) {
                withStyle(
                    SpanStyle(
                        color = highlightColor,
                        background = highlightBackground,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(rawToken)
                }
            } else {
                append(rawToken)
            }
        }
    }
}

private fun stripMedialAlef(word: String): String {
    if (word.length <= 2) return word
    return buildString(word.length) {
        word.forEachIndexed { idx, c ->
            if (idx == 0 || c != 'ا') append(c)
        }
    }
}
