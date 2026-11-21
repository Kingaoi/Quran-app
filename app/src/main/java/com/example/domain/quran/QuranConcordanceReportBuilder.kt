package com.example.domain.quran

import com.example.domain.model.AyahItem
import com.example.domain.model.SurahInfo

internal class AyahMatchAccumulator {
    var matchCount: Int = 0
    var score: Int = 0
    var conceptTitle: String? = null
    val matchedTokens = linkedSetOf<String>()
}

internal class MutableFormCounter {
    var occurrences: Int = 0
    var ayahCount: Int = 0
}

private class MutableSurahCounter(val nameAr: String, val isMakki: Boolean) {
    var ayahCount: Int = 0
    var occurrenceCount: Int = 0
}

internal object QuranConcordanceReportBuilder {
    fun build(
        query: String,
        normQuery: String,
        mode: QuranSearchMode,
        resolvedRoot: String?,
        matchedConcepts: List<ConcordanceConceptInfo>,
        indexedAyahs: List<AyahItem>,
        surahsByNumber: Map<Int, SurahInfo>,
        ayahMatches: Map<Int, AyahMatchAccumulator>,
        formStats: Map<String, MutableFormCounter>,
        sortByRelevance: Boolean
    ): QuranConcordanceReport {
        if (ayahMatches.isEmpty()) {
            return QuranConcordanceReport(
                query = query,
                normalizedQuery = normQuery,
                mode = mode,
                resolvedRoot = resolvedRoot,
                matchedConcepts = matchedConcepts
            )
        }

        val juzCounts = IntArray(30)
        val surahAggregates = HashMap<Int, MutableSurahCounter>()
        var totalOccurrences = 0
        var makkiOcc = 0
        var madaniOcc = 0
        var makkiAyahs = 0
        var madaniAyahs = 0

        val orderedIndices = ayahMatches.keys.sorted()
        var firstHit: ConcordanceHit? = null
        var lastHit: ConcordanceHit? = null

        val hits = orderedIndices.mapIndexed { pos, ayahIdx ->
            val ayah = indexedAyahs[ayahIdx]
            val acc = ayahMatches.getValue(ayahIdx)
            val isMakki = surahsByNumber[ayah.surahNumber]?.isMakki ?: true
            totalOccurrences += acc.matchCount
            if (isMakki) {
                makkiOcc += acc.matchCount
                makkiAyahs += 1
            } else {
                madaniOcc += acc.matchCount
                madaniAyahs += 1
            }
            val juzSlot = (ayah.juzNumber - 1).coerceIn(0, 29)
            juzCounts[juzSlot] += acc.matchCount
            val sCounter = surahAggregates.getOrPut(ayah.surahNumber) {
                MutableSurahCounter(ayah.surahNameAr, isMakki)
            }
            sCounter.ayahCount += 1
            sCounter.occurrenceCount += acc.matchCount

            val hit = ConcordanceHit(
                surahNumber = ayah.surahNumber,
                surahNameAr = ayah.surahNameAr,
                isMakki = isMakki,
                ayahNumber = ayah.ayahNumber,
                pageNumber = ayah.pageNumber,
                juzNumber = ayah.juzNumber,
                ayahText = ayah.text,
                imlaiText = ayah.imlaiText,
                matchedTokens = acc.matchedTokens.toList(),
                matchCountInAyah = acc.matchCount,
                relevanceScore = acc.score,
                matchedConceptTitle = acc.conceptTitle
            )
            if (pos == 0) firstHit = hit
            lastHit = hit
            hit
        }

        val finalHits = if (sortByRelevance) {
            hits.sortedWith(
                compareByDescending<ConcordanceHit> { it.relevanceScore }
                    .thenBy { it.surahNumber }
                    .thenBy { it.ayahNumber }
            )
        } else {
            hits
        }

        val topSurahs = surahAggregates.entries
            .map { (surahNum, c) ->
                ConcordanceSurahStat(surahNum, c.nameAr, c.isMakki, c.ayahCount, c.occurrenceCount)
            }
            .sortedWith(compareByDescending<ConcordanceSurahStat> { it.occurrenceCount }.thenBy { it.surahNumber })

        val derivedForms = formStats.entries
            .map { (form, c) -> ConcordanceFormStat(form, c.occurrences, c.ayahCount) }
            .sortedByDescending { it.occurrenceCount }
            .take(24)

        return QuranConcordanceReport(
            query = query,
            normalizedQuery = normQuery,
            mode = mode,
            resolvedRoot = resolvedRoot,
            matchedConcepts = matchedConcepts,
            totalOccurrences = totalOccurrences,
            totalMatchingAyahs = hits.size,
            totalMatchingSurahs = surahAggregates.size,
            makkiOccurrences = makkiOcc,
            madaniOccurrences = madaniOcc,
            makkiAyahs = makkiAyahs,
            madaniAyahs = madaniAyahs,
            juzCounts = juzCounts.toList(),
            topSurahs = topSurahs,
            derivedForms = derivedForms,
            firstOccurrence = firstHit,
            lastOccurrence = lastHit,
            hits = finalHits
        )
    }
}
