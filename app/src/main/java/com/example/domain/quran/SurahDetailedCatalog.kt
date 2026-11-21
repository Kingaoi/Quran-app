package com.example.domain.quran

import androidx.compose.runtime.Immutable
import com.example.domain.model.AyahItem
import com.example.domain.model.HizbQuarterInfo
import com.example.domain.model.JuzSurahSlice
import com.example.domain.model.SurahInfo

@Immutable
data class SurahDetailedProfile(
    val number: Int,
    val revelationOrder: Int,
    val nameMeaning: String,
    val otherNames: String?,
    val mainObjective: String,
    val keyThemes: List<String>,
    val revelationNotes: String
)

@Immutable
data class SurahFullDetails(
    val surah: SurahInfo,
    val profile: SurahDetailedProfile,
    val juzNumbers: List<Int>,
    val quartersCount: Int,
    val sajdahPositions: List<SajdahPositionInfo>,
    val surahContext: AsbabEntry?,
    val relatedAsbab: List<AsbabEntry>
)

internal fun surahSpec(
    number: Int,
    revelationOrder: Int,
    nameMeaning: String,
    otherNames: String?,
    mainObjective: String,
    keyThemes: List<String>,
    revelationNotes: String
): Pair<Int, SurahDetailedProfile> = number to SurahDetailedProfile(
    number = number,
    revelationOrder = revelationOrder,
    nameMeaning = nameMeaning,
    otherNames = otherNames,
    mainObjective = mainObjective,
    keyThemes = keyThemes,
    revelationNotes = revelationNotes
)

object SurahDetailedCatalog {

    private val profiles: Map<Int, SurahDetailedProfile> by lazy {
        buildMap {
            putAll(SurahCatalogPart1.entries)
            putAll(SurahCatalogPart2.entries)
            putAll(SurahCatalogPart3.entries)
            putAll(SurahCatalogPart4.entries)
        }
    }

    fun getProfile(surahNumber: Int): SurahDetailedProfile =
        profiles[surahNumber] ?: SurahDetailedProfile(
            number = surahNumber,
            revelationOrder = surahNumber,
            nameMeaning = "اسم توقيفي مأثور عن النبي ﷺ",
            otherNames = null,
            mainObjective = "هداية الخلق إلى توحيد الله وتزكية النفوس وإقامة العدل.",
            keyThemes = listOf("توحيد الله", "الوعظ والتذكير", "بناء الإيمان والأخلاق"),
            revelationNotes = "سورة من سور المصحف الشريف."
        )

    fun allProfiles(): Map<Int, SurahDetailedProfile> = profiles

    fun resolveDetails(
        surah: SurahInfo,
        juzSurahSlices: Map<Int, List<JuzSurahSlice>>,
        hizbQuartersByJuz: Map<Int, List<HizbQuarterInfo>>,
        sajdahAyahs: List<SajdahPositionInfo>
    ): SurahFullDetails {
        val juzNumbers = juzSurahSlices
            .filterValues { slices -> slices.any { it.surah.number == surah.number } }
            .keys
            .sorted()
        val quartersCount = hizbQuartersByJuz.values.flatten().count { it.surahNumber == surah.number }
        val sajdahs = sajdahAyahs.filter { it.surahNumber == surah.number }
        val surahPrefix = "${surah.number}:"
        val relatedAsbab = QuranAsbabCatalog.ayahEntries
            .filterKeys { it.startsWith(surahPrefix) }
            .values
            .flatten() +
            QuranAsbabEntriesPart2.extraAyahEntries
                .filterKeys { it.startsWith(surahPrefix) }
                .values
                .flatten()

        return SurahFullDetails(
            surah = surah,
            profile = getProfile(surah.number),
            juzNumbers = juzNumbers,
            quartersCount = quartersCount,
            sajdahPositions = sajdahs,
            surahContext = QuranAsbabEntriesPart2.surahContexts[surah.number],
            relatedAsbab = relatedAsbab.distinctBy { it.title }
        )
    }

    internal fun computeSurahTextMetrics(ayahs: List<AyahItem>): Pair<Int, Int> {
        var words = 0
        var letters = 0
        for (ayah in ayahs) {
            val source = ayah.imlaiText.ifBlank { ayah.text }
            val parts = source.trim().split(WHITESPACE_REGEX)
            for (token in parts) {
                var tokenLetters = 0
                for (ch in token) {
                    if (ch in '\u0621'..'\u064A' || ch == '\u0671') {
                        tokenLetters++
                    }
                }
                if (tokenLetters > 0) {
                    words++
                    letters += tokenLetters
                }
            }
        }
        return words to letters
    }

    private val WHITESPACE_REGEX = Regex("\\s+")
}
