package com.example.domain.quran

import com.example.data.repository.QuranRepository
import com.example.domain.model.AyahItem
import kotlin.math.roundToInt

class QuranMutashabihatEngine {

    private var indexedAyahs: List<AyahItem> = emptyList()
    private var normalizedWordsByIndex: List<List<String>> = emptyList()
    private var keyToIndex: Map<Pair<Int, Int>, Int> = emptyMap()
    private var exactNormalizedMap: Map<String, List<Int>> = emptyMap()
    private var trigramIndex: Map<String, List<Int>> = emptyMap()

    @Volatile
    private var isIndexed = false

    fun buildIndex(allAyahs: List<AyahItem>) {
        if (allAyahs.isEmpty() || isIndexed) return
        val wordsList = ArrayList<List<String>>(allAyahs.size)
        val keys = HashMap<Pair<Int, Int>, Int>(allAyahs.size)
        val exactMap = HashMap<String, MutableList<Int>>()
        val triMap = HashMap<String, MutableList<Int>>()

        allAyahs.forEachIndexed { idx, ayah ->
            keys[ayah.surahNumber to ayah.ayahNumber] = idx
            val words = tokenize(ayah)
            wordsList.add(words)
            val fullNorm = words.joinToString(" ")
            if (fullNorm.isNotEmpty()) {
                exactMap.getOrPut(fullNorm) { mutableListOf() }.add(idx)
            }
            if (words.size >= 3) {
                val seenTri = HashSet<String>()
                for (i in 0..(words.size - 3)) {
                    val tri = "${words[i]} ${words[i + 1]} ${words[i + 2]}"
                    if (seenTri.add(tri)) {
                        triMap.getOrPut(tri) { mutableListOf() }.add(idx)
                    }
                }
            }
        }

        indexedAyahs = allAyahs
        normalizedWordsByIndex = wordsList
        keyToIndex = keys
        exactNormalizedMap = exactMap
        trigramIndex = triMap
        isIndexed = true
    }

    fun countForAyah(surah: Int, ayah: Int): Int {
        if (!isIndexed) return 0
        val idx = keyToIndex[surah to ayah] ?: return 0
        val src = indexedAyahs.getOrNull(idx) ?: return 0
        return findCandidateScores(idx, src).size
    }

    fun findSimilarAyahs(source: AyahItem, limit: Int = 12): List<MutashabihMatch> {
        if (!isIndexed) return emptyList()
        val srcIdx = keyToIndex[source.surahNumber to source.ayahNumber] ?: return emptyList()
        val srcWords = normalizedWordsByIndex.getOrNull(srcIdx) ?: return emptyList()
        val srcFull = srcWords.joinToString(" ")
        val candidates = findCandidateScores(srcIdx, source)

        return candidates.entries
            .mapNotNull { (candIdx, scoreInfo) ->
                val target = indexedAyahs[candIdx]
                val candWords = normalizedWordsByIndex[candIdx]
                val candFull = candWords.joinToString(" ")
                val isExact = srcFull == candFull
                val percent = if (isExact) {
                    100
                } else {
                    val unionSize = (srcWords.toSet() + candWords.toSet()).size.coerceAtLeast(1)
                    val sharedCount = srcWords.toSet().intersect(candWords.toSet()).size
                    val jaccard = sharedCount.toFloat() / unionSize.toFloat()
                    val containment = sharedCount.toFloat() / minOf(srcWords.size, candWords.size).coerceAtLeast(1)
                    ((jaccard * 0.45f + containment * 0.55f) * 100f).roundToInt().coerceIn(38, 97)
                }
                val badge = when {
                    isExact -> "تطابق تام"
                    percent >= 75 -> "تشابه قوي"
                    scoreInfo.maxSharedWords >= 4 -> "تشابه لفظي متقارب"
                    else -> "تشابه في السياق"
                }
                val phrases = extractSharedPhrases(srcWords, candWords, target.text)
                MutashabihMatch(
                    target = target,
                    similarityPercent = percent,
                    sharedPhrases = phrases,
                    matchBadge = badge
                )
            }
            .sortedWith(compareByDescending<MutashabihMatch> { it.similarityPercent }.thenBy { it.target.surahNumber })
            .take(limit)
    }

    fun computeWordComparison(source: AyahItem, target: AyahItem): List<AyahWordDiffSegment> {
        val sourceNormSet = tokenize(source).toSet()
        val rawWords = target.text.trim().split(WHITESPACE_REGEX).filter { it.isNotBlank() }
        return rawWords.map { raw ->
            val norm = QuranRepository.normalizeArabic(raw).replace(PUNCT_REGEX, "")
            AyahWordDiffSegment(word = raw, isShared = norm.isNotEmpty() && norm in sourceNormSet)
        }
    }

    private data class CandidateScore(var trigramHits: Int = 0, var maxSharedWords: Int = 0)

    private fun findCandidateScores(srcIdx: Int, src: AyahItem): Map<Int, CandidateScore> {
        val srcWords = normalizedWordsByIndex[srcIdx]
        val srcFull = srcWords.joinToString(" ")
        val scores = LinkedHashMap<Int, CandidateScore>()

        exactNormalizedMap[srcFull]?.forEach { candIdx ->
            if (candIdx != srcIdx) {
                scores[candIdx] = CandidateScore(trigramHits = 10, maxSharedWords = srcWords.size)
            }
        }

        if (srcWords.size < 3) return scores

        val seenTri = HashSet<String>()
        for (i in 0..(srcWords.size - 3)) {
            val tri = "${srcWords[i]} ${srcWords[i + 1]} ${srcWords[i + 2]}"
            if (!seenTri.add(tri)) continue
            val hits = trigramIndex[tri] ?: continue
            if (hits.size > 48) continue
            for (candIdx in hits) {
                if (candIdx == srcIdx) continue
                val entry = scores.getOrPut(candIdx) { CandidateScore() }
                entry.trigramHits++
            }
        }

        val srcWordSet = srcWords.toSet()
        val iterator = scores.entries.iterator()
        while (iterator.hasNext()) {
            val (candIdx, info) = iterator.next()
            val candWords = normalizedWordsByIndex[candIdx]
            val candFull = candWords.joinToString(" ")
            if (candFull == srcFull) {
                info.maxSharedWords = srcWords.size
                continue
            }
            val sharedUnique = srcWordSet.intersect(candWords.toSet()).size
            info.maxSharedWords = sharedUnique
            val minWords = minOf(srcWords.size, candWords.size).coerceAtLeast(1)
            val ratio = sharedUnique.toFloat() / minWords.toFloat()
            val isSignificant = (info.trigramHits >= 2 && sharedUnique >= 4) ||
                (info.trigramHits >= 1 && ratio >= 0.48f && sharedUnique >= 4) ||
                (sharedUnique >= 6 && ratio >= 0.42f)
            if (!isSignificant || indexedAyahs[candIdx].surahNumber == src.surahNumber &&
                indexedAyahs[candIdx].ayahNumber == src.ayahNumber
            ) {
                iterator.remove()
            }
        }
        return scores
    }

    private fun extractSharedPhrases(
        srcWords: List<String>,
        candWords: List<String>,
        rawTargetText: String
    ): List<String> {
        val rawWords = rawTargetText.trim().split(WHITESPACE_REGEX).filter { it.isNotBlank() }
        val srcSet = HashSet<String>()
        if (srcWords.size >= 3) {
            for (i in 0..(srcWords.size - 3)) {
                srcSet.add("${srcWords[i]} ${srcWords[i + 1]} ${srcWords[i + 2]}")
            }
        }
        val phrases = mutableListOf<String>()
        var i = 0
        while (i <= candWords.size - 3) {
            val tri = "${candWords[i]} ${candWords[i + 1]} ${candWords[i + 2]}"
            if (tri in srcSet) {
                var end = i + 2
                while (end + 1 < candWords.size && end + 1 < rawWords.size) {
                    val nextTri = "${candWords[end - 1]} ${candWords[end]} ${candWords[end + 1]}"
                    if (nextTri in srcSet) end++ else break
                }
                val slice = rawWords.subList(i, (end + 1).coerceAtMost(rawWords.size)).joinToString(" ")
                if (slice.isNotBlank()) phrases.add(slice)
                i = end + 1
            } else {
                i++
            }
        }
        return phrases.distinct().take(3)
    }

    private fun tokenize(ayah: AyahItem): List<String> {
        val base = ayah.imlaiText.ifBlank { ayah.text }
        return QuranRepository.normalizeArabic(base)
            .replace(PUNCT_REGEX, "")
            .split(WHITESPACE_REGEX)
            .filter { it.isNotBlank() }
    }

    companion object {
        private val WHITESPACE_REGEX = Regex("\\s+")
        private val PUNCT_REGEX = Regex("[\\u06D6-\\u06ED۞۩۝،؛؟.,:;()\\[\\]{}]")
    }
}
