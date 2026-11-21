package com.example.domain.quran

import androidx.compose.runtime.Immutable
import com.example.domain.model.AyahItem

@Immutable
data class AsbabEntry(
    val title: String,
    val narration: String,
    val source: String,
    val isSurahContext: Boolean = false
)

@Immutable
data class GharibWordEntry(
    val word: String,
    val meaning: String
)

@Immutable
data class AyahSciencesInfo(
    val asbab: List<AsbabEntry>,
    val surahContext: AsbabEntry?,
    val gharibWords: List<GharibWordEntry>
)

@Immutable
data class MutashabihMatch(
    val target: AyahItem,
    val similarityPercent: Int,
    val sharedPhrases: List<String>,
    val matchBadge: String
)

@Immutable
data class AyahWordDiffSegment(
    val word: String,
    val isShared: Boolean
)
