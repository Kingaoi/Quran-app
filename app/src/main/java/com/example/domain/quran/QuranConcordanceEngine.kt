package com.example.domain.quran

import com.example.domain.model.AyahItem
import com.example.domain.model.SurahInfo

class QuranConcordanceEngine {
    private data class Posting(val ayahIndex: Int, val countInAyah: Int)

    private var indexedAyahs: List<AyahItem> = emptyList()
    private var surahsByNumber: Map<Int, SurahInfo> = emptyMap()
    private var ayahNormWords: List<List<String>> = emptyList()
    private var ayahFullNormStrings: List<String> = emptyList()
    private var vocabPostings: Map<String, List<Posting>> = emptyMap()

    @Volatile
    private var isBuilt = false

    fun buildIndex(allAyahs: List<AyahItem>, surahs: Map<Int, SurahInfo>) {
        if (allAyahs.isEmpty() || isBuilt) return
        val wordsPerAyah = ArrayList<List<String>>(allAyahs.size)
        val joinedNorm = ArrayList<String>(allAyahs.size)
        val postingsBuilder = HashMap<String, MutableList<Posting>>(16_384)

        allAyahs.forEachIndexed { idx, ayah ->
            val tokens = tokenizeAyah(ayah)
            wordsPerAyah.add(tokens)
            joinedNorm.add(tokens.joinToString(" "))
            if (tokens.isNotEmpty()) {
                val freq = LinkedHashMap<String, Int>()
                tokens.forEach { w -> freq[w] = (freq[w] ?: 0) + 1 }
                freq.forEach { (word, count) ->
                    postingsBuilder.getOrPut(word) { mutableListOf() }.add(Posting(idx, count))
                }
            }
        }

        indexedAyahs = allAyahs
        surahsByNumber = surahs
        ayahNormWords = wordsPerAyah
        ayahFullNormStrings = joinedNorm
        vocabPostings = postingsBuilder
        isBuilt = true
    }

    fun search(rawQuery: String, mode: QuranSearchMode): QuranConcordanceReport {
        val trimmed = rawQuery.trim()
        if (!isBuilt || trimmed.length < 2) return QuranConcordanceReport.empty(trimmed, mode)
        return when (mode) {
            QuranSearchMode.WORD -> searchByWordOrPhrase(trimmed)
            QuranSearchMode.ROOT -> searchByRoot(trimmed)
            QuranSearchMode.MEANING -> searchByMeaning(trimmed)
        }
    }

    private fun searchByWordOrPhrase(rawQuery: String): QuranConcordanceReport {
        val normQuery = QuranRootLexicon.normalizeToken(rawQuery).replace(WHITESPACE_REGEX, " ").trim()
        if (normQuery.length < 2) return QuranConcordanceReport.empty(rawQuery, QuranSearchMode.WORD)
        val queryWords = normQuery.split(" ").filter { it.isNotBlank() }

        val ayahMatches = LinkedHashMap<Int, AyahMatchAccumulator>()
        val formStats = LinkedHashMap<String, MutableFormCounter>()

        if (queryWords.size == 1) {
            val target = queryWords.first()
            vocabPostings.forEach { (vocabWord, postings) ->
                val isExact = vocabWord == target
                val isContains = !isExact && target.length >= 2 && vocabWord.contains(target)
                if (isExact || isContains) {
                    val formCounter = formStats.getOrPut(vocabWord) { MutableFormCounter() }
                    postings.forEach { p ->
                        formCounter.occurrences += p.countInAyah
                        formCounter.ayahCount += 1
                        val acc = ayahMatches.getOrPut(p.ayahIndex) { AyahMatchAccumulator() }
                        acc.matchCount += p.countInAyah
                        acc.score += (if (isExact) 10 else 5) * p.countInAyah
                        acc.matchedTokens.add(vocabWord)
                    }
                }
            }
        } else {
            val formCounter = formStats.getOrPut(normQuery) { MutableFormCounter() }
            ayahFullNormStrings.forEachIndexed { idx, fullNorm ->
                val occ = countNonOverlappingOccurrences(fullNorm, normQuery)
                if (occ > 0) {
                    formCounter.occurrences += occ
                    formCounter.ayahCount += 1
                    val acc = ayahMatches.getOrPut(idx) { AyahMatchAccumulator() }
                    acc.matchCount += occ
                    acc.score += 15 * occ
                    acc.matchedTokens.addAll(queryWords)
                }
            }
        }

        return QuranConcordanceReportBuilder.build(
            query = rawQuery,
            normQuery = normQuery,
            mode = QuranSearchMode.WORD,
            resolvedRoot = null,
            matchedConcepts = emptyList(),
            indexedAyahs = indexedAyahs,
            surahsByNumber = surahsByNumber,
            ayahMatches = ayahMatches,
            formStats = formStats,
            sortByRelevance = false
        )
    }

    private fun searchByRoot(rawQuery: String): QuranConcordanceReport {
        val resolvedRoot = QuranRootLexicon.resolveRootQuery(rawQuery)
        val normRoot = QuranRootLexicon.normalizeToken(resolvedRoot)
        if (normRoot.length < 2) return QuranConcordanceReport.empty(rawQuery, QuranSearchMode.ROOT)
        val spec = QuranRootLexicon.specByRoot[normRoot]

        val ayahMatches = LinkedHashMap<Int, AyahMatchAccumulator>()
        val formStats = LinkedHashMap<String, MutableFormCounter>()

        vocabPostings.forEach { (vocabWord, postings) ->
            if (QuranRootLexicon.wordMatchesRoot(vocabWord, normRoot, spec)) {
                val counter = formStats.getOrPut(vocabWord) { MutableFormCounter() }
                postings.forEach { p ->
                    counter.occurrences += p.countInAyah
                    counter.ayahCount += 1
                    val acc = ayahMatches.getOrPut(p.ayahIndex) { AyahMatchAccumulator() }
                    acc.matchCount += p.countInAyah
                    acc.score += 8 * p.countInAyah
                    acc.matchedTokens.add(vocabWord)
                }
            }
        }

        return QuranConcordanceReportBuilder.build(
            query = rawQuery,
            normQuery = QuranRootLexicon.normalizeToken(rawQuery),
            mode = QuranSearchMode.ROOT,
            resolvedRoot = spec?.root ?: normRoot,
            matchedConcepts = emptyList(),
            indexedAyahs = indexedAyahs,
            surahsByNumber = surahsByNumber,
            ayahMatches = ayahMatches,
            formStats = formStats,
            sortByRelevance = false
        )
    }

    private fun searchByMeaning(rawQuery: String): QuranConcordanceReport {
        val normQuery = QuranRootLexicon.normalizeToken(rawQuery)
        val conceptMatches = QuranSemanticLexicon.findMatchingConcepts(rawQuery)
        val ayahMatches = LinkedHashMap<Int, AyahMatchAccumulator>()
        val formStats = LinkedHashMap<String, MutableFormCounter>()

        val conceptInfos = conceptMatches.take(4).map { (c, _) ->
            ConcordanceConceptInfo(c.id, c.titleAr, c.descriptionAr, c.triggers.take(5))
        }

        val targetVocabWeights = LinkedHashMap<String, Int>()
        val vocabConceptTitle = HashMap<String, String>()

        if (conceptMatches.isNotEmpty()) {
            conceptMatches.take(3).forEach { (concept, conceptWeight) ->
                concept.relatedRoots.forEach { rootStr ->
                    val rNorm = QuranRootLexicon.normalizeToken(rootStr)
                    val spec = QuranRootLexicon.specByRoot[rNorm]
                    vocabPostings.keys.forEach { vocabWord ->
                        if (QuranRootLexicon.wordMatchesRoot(vocabWord, rNorm, spec)) {
                            val w = (conceptWeight / 2).coerceAtLeast(4)
                            targetVocabWeights[vocabWord] = maxOf(targetVocabWeights[vocabWord] ?: 0, w)
                            vocabConceptTitle.putIfAbsent(vocabWord, concept.titleAr)
                        }
                    }
                }
            }
        }

        val freeTokens = normQuery.split(WHITESPACE_REGEX)
            .filter { it.length >= 2 && it !in QuranRootLexicon.stopwords }
        for (tok in freeTokens) {
            val rNorm = QuranRootLexicon.normalizeToken(QuranRootLexicon.resolveRootQuery(tok))
            val spec = QuranRootLexicon.specByRoot[rNorm]
            vocabPostings.keys.forEach { vocabWord ->
                val exactOrSub = vocabWord == tok || vocabWord.contains(tok)
                val rootMatch = rNorm.length >= 3 && QuranRootLexicon.wordMatchesRoot(vocabWord, rNorm, spec)
                if (exactOrSub || rootMatch) {
                    val w = if (vocabWord == tok) 12 else if (exactOrSub) 8 else 5
                    targetVocabWeights[vocabWord] = maxOf(targetVocabWeights[vocabWord] ?: 0, w)
                }
            }
        }

        // Apply matched vocabulary tokens cleanly once per posting
        targetVocabWeights.forEach { (vocabWord, weight) ->
            val postings = vocabPostings[vocabWord].orEmpty()
            val fc = formStats.getOrPut(vocabWord) { MutableFormCounter() }
            postings.forEach { p ->
                fc.occurrences += p.countInAyah
                fc.ayahCount += 1
                val acc = ayahMatches.getOrPut(p.ayahIndex) { AyahMatchAccumulator() }
                acc.matchCount += p.countInAyah
                acc.score += weight * p.countInAyah
                if (acc.conceptTitle == null) acc.conceptTitle = vocabConceptTitle[vocabWord]
                acc.matchedTokens.add(vocabWord)
            }
        }

        // Boost exact multi-word / stem phrase matches from matched concepts
        if (conceptMatches.isNotEmpty()) {
            conceptMatches.take(3).forEach { (concept, conceptWeight) ->
                val normStems = concept.matchStems.map { QuranRootLexicon.normalizeToken(it) }.filter { it.length >= 3 }
                ayahFullNormStrings.forEachIndexed { idx, fullNorm ->
                    normStems.forEach { stem ->
                        if (fullNorm.contains(stem)) {
                            val acc = ayahMatches.getOrPut(idx) { AyahMatchAccumulator() }
                            acc.score += conceptWeight + 16
                            if (acc.conceptTitle == null) acc.conceptTitle = concept.titleAr
                            val stemWords = stem.split(" ").filter { it.length >= 2 && it !in QuranRootLexicon.stopwords }
                            var addedNewWord = false
                            stemWords.forEach { sw ->
                                ayahNormWords[idx].forEach { aw ->
                                    if (aw == sw || aw.contains(sw)) {
                                        if (acc.matchedTokens.add(aw)) {
                                            addedNewWord = true
                                            val fc = formStats.getOrPut(aw) { MutableFormCounter() }
                                            fc.occurrences += 1
                                            fc.ayahCount += 1
                                            acc.matchCount += 1
                                        }
                                    }
                                }
                            }
                            if (!addedNewWord && acc.matchCount == 0) {
                                acc.matchCount = 1
                            }
                        }
                    }
                }
            }
        }

        return QuranConcordanceReportBuilder.build(
            query = rawQuery,
            normQuery = normQuery,
            mode = QuranSearchMode.MEANING,
            resolvedRoot = null,
            matchedConcepts = conceptInfos,
            indexedAyahs = indexedAyahs,
            surahsByNumber = surahsByNumber,
            ayahMatches = ayahMatches,
            formStats = formStats,
            sortByRelevance = true
        )
    }

    private fun tokenizeAyah(ayah: AyahItem): List<String> {
        val src = ayah.imlaiText.ifBlank { ayah.text }
        return src.trim().split(WHITESPACE_REGEX)
            .map { QuranRootLexicon.normalizeToken(it) }
            .filter { it.isNotBlank() }
    }

    private fun countNonOverlappingOccurrences(text: String, sub: String): Int {
        if (sub.isEmpty()) return 0
        var count = 0
        var idx = 0
        while (true) {
            val found = text.indexOf(sub, idx)
            if (found < 0) break
            count++
            idx = found + sub.length
        }
        return count
    }

    companion object {
        private val WHITESPACE_REGEX = Regex("\\s+")
    }
}
