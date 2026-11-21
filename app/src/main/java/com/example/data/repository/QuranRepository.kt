package com.example.data.repository

import android.content.Context
import com.example.data.local.RiwayahEdition
import com.example.data.remote.QuranWsCdnClient
import com.example.domain.model.AyahItem
import com.example.domain.model.HizbQuarterInfo
import com.example.domain.model.JuzInfo
import com.example.domain.model.JuzSurahSlice
import com.example.domain.model.SearchHit
import com.example.domain.model.SurahInfo
import com.example.domain.quran.AyahWordDiffSegment
import com.example.domain.quran.MutashabihMatch
import com.example.domain.quran.QuranConcordanceEngine
import com.example.domain.quran.QuranConcordanceReport
import com.example.domain.quran.QuranExplorerCatalog
import com.example.domain.quran.QuranMutashabihatEngine
import com.example.domain.quran.QuranSearchMode
import com.example.domain.quran.QuranTopicGroup
import com.example.domain.quran.SajdahPositionInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ws.quran.qurantext.Mushaf
import java.io.File

class QuranRepository(
    private val context: Context
) {
    private val initMutex = Mutex()
    private val mutashabihatEngine = QuranMutashabihatEngine()
    private val concordanceEngine = QuranConcordanceEngine()

    @Volatile
    private var initialized = false

    private var surahsCache: List<SurahInfo> = emptyList()
    private var allAyahsCache: List<AyahItem> = emptyList()
    private var ayahsByPageCache: Map<Int, List<AyahItem>> = emptyMap()
    private var ajzaCache: List<JuzInfo> = emptyList()
    private var juzSurahSlicesCache: Map<Int, List<JuzSurahSlice>> = emptyMap()
    private var hizbQuartersByJuzCache: Map<Int, List<HizbQuarterInfo>> = emptyMap()
    private var allQuartersCache: List<HizbQuarterInfo> = emptyList()
    private var sajdahPositionsCache: List<SajdahPositionInfo> = emptyList()
    private var topicGroupsCache: List<QuranTopicGroup> = emptyList()
    private var uthmanicFontFile: File? = null

    suspend fun ensureLoaded() {
        if (initialized) return
        initMutex.withLock {
            if (initialized) return
            withContext(Dispatchers.IO) {
                val mushaf = Mushaf.hafs()

                runCatching {
                    val outFile = File(context.cacheDir, "UthmanicHafs-v-3.0.ttf")
                    if (!outFile.exists() || outFile.length() == 0L) {
                        mushaf.font.open()?.use { input ->
                            outFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    uthmanicFontFile = outFile
                }

                val surahs = mushaf.surahs.map { s ->
                    val rev = s.revelation.lowercase()
                    val isMakki = rev.contains("makk") || rev.contains("mecc") || rev.contains("مك")
                    SurahInfo(
                        number = s.number,
                        nameAr = s.nameAr,
                        nameEn = s.nameEn,
                        ayahCount = s.ayahCount,
                        revelationType = if (isMakki) "مكية" else "مدنية",
                        isMakki = isMakki,
                        hasBasmalah = s.hasBasmalah,
                        firstPage = s.firstPage.number,
                        lastPage = s.lastPage.number
                    )
                }

                val ayahs = mushaf.ayahs.map { a ->
                    val sNum = a.surah.number
                    AyahItem(
                        surahNumber = sNum,
                        surahNameAr = a.surah.nameAr,
                        ayahNumber = a.number,
                        text = a.text,
                        imlaiText = a.rasm_imlai?.filterNotNull()?.joinToString(" ").orEmpty(),
                        marker = a.marker,
                        pageNumber = a.page.number,
                        juzNumber = a.juz?.number ?: 1,
                        hasSajdah = a.hasSajdah
                    )
                }

                val ayahsBySurah = ayahs.groupBy { it.surahNumber }
                val enrichedSurahs = surahs.map { s ->
                    val surahAyahs = ayahsBySurah[s.number].orEmpty()
                    val (words, letters) = com.example.domain.quran.SurahDetailedCatalog.computeSurahTextMetrics(surahAyahs)
                    s.copy(
                        wordCount = words,
                        letterCount = letters,
                        openingAyahText = surahAyahs.firstOrNull()?.text.orEmpty()
                    )
                }
                val surahByNumber = enrichedSurahs.associateBy { it.number }
                val byPage = ayahs.groupBy { it.pageNumber }

                val juzNames = listOf(
                    "الجزء الأول", "الجزء الثاني", "الجزء الثالث", "الجزء الرابع", "الجزء الخامس",
                    "الجزء السادس", "الجزء السابع", "الجزء الثامن", "الجزء التاسع", "الجزء العاشر",
                    "الجزء الحادي عشر", "الجزء الثاني عشر", "الجزء الثالث عشر", "الجزء الرابع عشر", "الجزء الخامس عشر",
                    "الجزء السادس عشر", "الجزء السابع عشر", "الجزء الثامن عشر", "الجزء التاسع عشر", "الجزء العشرون",
                    "الجزء الحادي والعشرون", "الجزء الثاني والعشرون", "الجزء الثالث والعشرون", "الجزء الرابع والعشرون", "الجزء الخامس والعشرون",
                    "الجزء السادس والعشرون", "الجزء السابع والعشرون", "الجزء الثامن والعشرون", "الجزء التاسع والعشرون", "الجزء الثلاثون"
                )

                val ajza = (1..30).mapNotNull { juzNum ->
                    val firstAyah = ayahs.firstOrNull { it.juzNumber == juzNum }
                    firstAyah?.let {
                        JuzInfo(
                            number = juzNum,
                            nameAr = juzNames.getOrElse(juzNum - 1) { "الجزء $juzNum" },
                            startSurahNumber = it.surahNumber,
                            startSurahNameAr = it.surahNameAr,
                            startAyahNumber = it.ayahNumber,
                            startPage = it.pageNumber
                        )
                    }
                }

                // Group Surah slices by Juz so the Index shows which Surahs/parts sit in each Juz
                val slicesByJuz = (1..30).associateWith { juzNum ->
                    val juzAyahs = ayahs.filter { it.juzNumber == juzNum }
                    juzAyahs.groupBy { it.surahNumber }.mapNotNull { (surahNum, list) ->
                        val firstInJuz = list.firstOrNull() ?: return@mapNotNull null
                        val surahInfo = surahByNumber[surahNum] ?: return@mapNotNull null
                        JuzSurahSlice(
                            juzNumber = juzNum,
                            surah = surahInfo,
                            startAyah = firstInJuz.ayahNumber,
                            startPage = firstInJuz.pageNumber,
                            isContinuationFromPreviousJuz = firstInJuz.ayahNumber > 1
                        )
                    }
                }

                // Build exact 240 Rub' al-Hizb quarters (60 Ahzab x 4) from divisionMarks() + Juz/Surah-1 starts
                val quarterKeys = linkedSetOf<Pair<Int, Int>>()
                ajza.forEach { quarterKeys.add(it.startSurahNumber to it.startAyahNumber) }
                mushaf.divisionMarks().forEach { w ->
                    val aNum = w.ayah?.number ?: return@forEach
                    quarterKeys.add(w.surah.number to aNum)
                }
                SURAH_START_RUB_KEYS.forEach { quarterKeys.add(it) }

                val orderedQuarterAyahs = ayahs.filter { (it.surahNumber to it.ayahNumber) in quarterKeys }
                val quarters = orderedQuarterAyahs.mapIndexed { idx, ayah ->
                    val rubNumber = idx + 1
                    val hizbNumber = (idx / 4) + 1
                    val quarterInHizb = idx % 4
                    HizbQuarterInfo(
                        rubNumber = rubNumber,
                        juzNumber = ayah.juzNumber,
                        hizbNumber = hizbNumber,
                        quarterInHizb = quarterInHizb,
                        surahNumber = ayah.surahNumber,
                        surahNameAr = ayah.surahNameAr,
                        ayahNumber = ayah.ayahNumber,
                        pageNumber = ayah.pageNumber,
                        openingText = ayah.text
                    )
                }

                surahsCache = enrichedSurahs
                allAyahsCache = ayahs
                ayahsByPageCache = byPage
                ajzaCache = ajza
                juzSurahSlicesCache = slicesByJuz
                allQuartersCache = quarters
                hizbQuartersByJuzCache = quarters.groupBy { it.juzNumber }
                sajdahPositionsCache = QuranExplorerCatalog.buildSajdahPositions(ayahs)
                topicGroupsCache = QuranExplorerCatalog.buildTopicGroups(ayahs)
                mutashabihatEngine.buildIndex(ayahs)
                concordanceEngine.buildIndex(ayahs, surahByNumber)
                initialized = true
            }
        }
    }

    fun getUthmanicFontFile(): File? = uthmanicFontFile

    fun getSurahs(): List<SurahInfo> = surahsCache

    fun getSurah(number: Int): SurahInfo? = surahsCache.getOrNull(number - 1)

    fun getSurahMetaOrDefault(number: Int): Pair<String, Int> {
        val cached = getSurah(number)
        if (cached != null) return cached.nameAr to cached.ayahCount
        return runCatching {
            val mushafSurah = Mushaf.hafs().surahs.getOrNull(number - 1)
            (mushafSurah?.nameAr ?: "الفاتحة") to (mushafSurah?.ayahCount ?: 7)
        }.getOrDefault("الفاتحة" to 7)
    }

    fun getAyahsForPage(pageNumber: Int): List<AyahItem> =
        ayahsByPageCache[pageNumber].orEmpty()

    fun getAjza(): List<JuzInfo> = ajzaCache

    fun getJuzSurahSlices(): Map<Int, List<JuzSurahSlice>> = juzSurahSlicesCache

    fun getHizbQuartersByJuz(): Map<Int, List<HizbQuarterInfo>> = hizbQuartersByJuzCache

    fun getAllHizbQuarters(): List<HizbQuarterInfo> = allQuartersCache

    fun getSajdahPositions(): List<SajdahPositionInfo> = sajdahPositionsCache

    fun getTopicGroups(): List<QuranTopicGroup> = topicGroupsCache

    fun getAllAyahs(): List<AyahItem> = allAyahsCache

    fun getMutashabihatCount(surah: Int, ayah: Int): Int =
        mutashabihatEngine.countForAyah(surah, ayah)

    fun findMutashabihat(ayah: AyahItem): List<MutashabihMatch> =
        mutashabihatEngine.findSimilarAyahs(ayah)

    fun compareAyahWords(source: AyahItem, target: AyahItem): List<AyahWordDiffSegment> =
        mutashabihatEngine.computeWordComparison(source, target)

    fun getPagePrimarySurah(pageNumber: Int): SurahInfo? {
        val firstAyah = ayahsByPageCache[pageNumber]?.firstOrNull() ?: return null
        return getSurah(firstAyah.surahNumber)
    }

    fun getPageJuz(pageNumber: Int): Int =
        ayahsByPageCache[pageNumber]?.firstOrNull()?.juzNumber ?: 1

    fun getPageForAyah(surahNumber: Int, ayahNumber: Int): Int? =
        allAyahsCache.firstOrNull { it.surahNumber == surahNumber && it.ayahNumber == ayahNumber }?.pageNumber

    fun searchAyahs(query: String, limit: Int = 35): List<SearchHit> {
        val normalizedQuery = normalizeArabic(query)
        if (normalizedQuery.length < 2) return emptyList()
        val results = mutableListOf<SearchHit>()
        for (ayah in allAyahsCache) {
            val normalizedImlai = normalizeArabic(ayah.imlaiText)
            if (normalizedImlai.contains(normalizedQuery)) {
                results.add(
                    SearchHit(
                        surahNumber = ayah.surahNumber,
                        surahNameAr = ayah.surahNameAr,
                        ayahNumber = ayah.ayahNumber,
                        pageNumber = ayah.pageNumber,
                        ayahText = ayah.text,
                        matchedWord = query.trim()
                    )
                )
                if (results.size >= limit) break
            }
        }
        return results
    }

    fun searchConcordance(query: String, mode: QuranSearchMode): QuranConcordanceReport =
        concordanceEngine.search(query, mode)

    suspend fun resolveSvgVersion(): String = QuranWsCdnClient.getSvgVersion()

    fun getSvgPageUrl(version: String, edition: RiwayahEdition, page: Int): String =
        QuranWsCdnClient.buildSvgPageUrl(version, edition.slug, page)

    fun getRawGithubSvgUrl(edition: RiwayahEdition, page: Int): String =
        QuranWsCdnClient.buildRawGithubSvgUrl(edition.slug, page)

    companion object {
        private val DIACRITICS_REGEX = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640]")

        // Surah-opening Rub' al-Hizb boundaries omitted by divisionMarks() when starting at Ayah 1
        private val SURAH_START_RUB_KEYS = setOf(
            5 to 1,
            7 to 1,
            8 to 1,
            16 to 1,
            20 to 1,
            22 to 1,
            24 to 1,
            25 to 1,
            26 to 1,
            27 to 1,
            29 to 1,
            30 to 1,
            33 to 1,
            40 to 1,
            51 to 1,
            55 to 1,
            56 to 1,
            62 to 1,
            64 to 1,
            65 to 1,
            68 to 1,
            69 to 1,
            72 to 1,
            75 to 1,
            80 to 1,
            82 to 1,
            84 to 1,
            87 to 1,
            89 to 1,
            92 to 1
        )

        fun normalizeArabic(input: String): String {
            return input
                .replace(DIACRITICS_REGEX, "")
                .replace('أ', 'ا')
                .replace('إ', 'ا')
                .replace('آ', 'ا')
                .replace('ٱ', 'ا')
                .replace('ى', 'ي')
                .replace('ة', 'ه')
                .replace('ؤ', 'و')
                .replace('ئ', 'ي')
                .trim()
        }
    }
}
