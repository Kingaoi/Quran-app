package com.example

import com.example.domain.model.AyahItem
import com.example.domain.quran.QuranExplorerCatalog
import com.example.domain.quran.QuranMutashabihatEngine
import com.example.domain.quran.QuranSciencesResolver
import com.example.domain.quran.QuranWaqfCatalog
import com.example.domain.quran.WaqfMarkType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranSciencesTest {

    private fun makeAyah(
        surah: Int,
        surahName: String,
        ayah: Int,
        text: String,
        imlai: String,
        page: Int,
        juz: Int,
        hasSajdah: Boolean = false
    ) = AyahItem(
        surahNumber = surah,
        surahNameAr = surahName,
        ayahNumber = ayah,
        text = text,
        imlaiText = imlai,
        marker = "۝$ayah",
        pageNumber = page,
        juzNumber = juz,
        hasSajdah = hasSajdah
    )

    @Test
    fun waqfCatalog_detectsMultipleMarksAndSajdah() {
        val sample = makeAyah(
            surah = 2,
            surahName = "البقرة",
            ayah = 2,
            text = "ذَٰلِكَ ٱلْكِتَٰبُ لَا رَيْبَ\u06DB فِيهِ\u06DB هُدًى لِّلْمُتَّقِينَ\u06D7",
            imlai = "ذلك الكتاب لا ريب فيه هدى للمتقين",
            page = 2,
            juz = 1,
            hasSajdah = true
        )

        val detected = QuranWaqfCatalog.analyzeAyah(sample)
        val types = detected.map { it.type }

        assertTrue(WaqfMarkType.MUANAQAH in types)
        assertTrue(WaqfMarkType.AL_WAQF_AWLA in types)
        assertTrue(WaqfMarkType.SAJDAH in types)
        assertEquals(3, QuranWaqfCatalog.countMarksInAyah(sample))
    }

    @Test
    fun mutashabihatEngine_findsSimilarAyahsAndComputesWordDiff() {
        val ayahA = makeAyah(
            surah = 55,
            surahName = "الرحمن",
            ayah = 13,
            text = "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ",
            imlai = "فبأي آلاء ربكما تكذبان",
            page = 531,
            juz = 27
        )
        val ayahB = makeAyah(
            surah = 55,
            surahName = "الرحمن",
            ayah = 16,
            text = "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ",
            imlai = "فبأي آلاء ربكما تكذبان",
            page = 531,
            juz = 27
        )
        val ayahC = makeAyah(
            surah = 2,
            surahName = "البقرة",
            ayah = 5,
            text = "أُولَٰئِكَ عَلَىٰ هُدًى مِّن رَّبِّهِمْ وَأُولَٰئِكَ هُمُ الْمُفْلِحُونَ",
            imlai = "أولئك على هدى من ربهم وأولئك هم المفلحون",
            page = 2,
            juz = 1
        )
        val ayahD = makeAyah(
            surah = 31,
            surahName = "لقمان",
            ayah = 5,
            text = "أُولَٰئِكَ عَلَىٰ هُدًى مِّن رَّبِّهِمْ وَأُولَٰئِكَ هُمُ الْمُفْلِحُونَ",
            imlai = "أولئك على هدى من ربهم وأولئك هم المفلحون",
            page = 411,
            juz = 21
        )

        val engine = QuranMutashabihatEngine()
        engine.buildIndex(listOf(ayahA, ayahB, ayahC, ayahD))

        val matches = engine.findSimilarAyahs(ayahA)
        assertEquals(1, matches.size)
        assertEquals(16, matches.first().target.ayahNumber)
        assertEquals(100, matches.first().similarityPercent)

        val diff = engine.computeWordComparison(ayahC, ayahD)
        assertTrue(diff.isNotEmpty())
        assertTrue(diff.all { it.isShared })
    }

    @Test
    fun quranSciencesResolver_returnsAsbabAndGharibForKnownAyah() {
        val sampleAsbab = makeAyah(
            surah = 2,
            surahName = "البقرة",
            ayah = 186,
            text = "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ",
            imlai = "وإذا سألك عبادي عني فإني قريب أجيب دعوة الداع إذا دعان",
            page = 28,
            juz = 2
        )
        val infoAsbab = QuranSciencesResolver.resolveForAyah(sampleAsbab)
        assertTrue(infoAsbab.asbab.isNotEmpty())
        assertTrue(infoAsbab.surahContext != null)

        val sampleGharib = makeAyah(
            surah = 112,
            surahName = "الإخلاص",
            ayah = 2,
            text = "اللَّهُ الصَّمَدُ",
            imlai = "الله الصمد",
            page = 604,
            juz = 30
        )
        val infoGharib = QuranSciencesResolver.resolveForAyah(sampleGharib)
        assertTrue(infoGharib.gharibWords.isNotEmpty())
    }

    @Test
    fun quranExplorerCatalog_buildsSajdahPositionsAndTopicGroups() {
        val sampleAyahs = listOf(
            makeAyah(7, "الأعراف", 206, "إِنَّ الَّذِينَ عِندَ رَبِّكَ...", "إن الذين عند ربك...", 176, 9, hasSajdah = true),
            makeAyah(22, "الحج", 77, "يَا أَيُّهَا الَّذِينَ آمَنُوا ارْكَعُوا وَاسْجُدُوا...", "يا أيها الذين آمنوا اركعوا واسجدوا...", 341, 17, hasSajdah = true),
            makeAyah(38, "ص", 24, "وَظَنَّ دَاوُودُ أَنَّمَا فَتَنَّاهُ...", "وظن داوود أنما فتناه...", 454, 23, hasSajdah = true),
            makeAyah(2, "البقرة", 255, "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ", "الله لا إله إلا هو الحي القيوم", 42, 3),
            makeAyah(13, "الرعد", 28, "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ", "ألا بذكر الله تطمئن القلوب", 252, 13)
        )

        val sajdahs = QuranExplorerCatalog.buildSajdahPositions(sampleAyahs)
        assertEquals(3, sajdahs.size)
        assertEquals(1, sajdahs[0].index)
        assertTrue(sajdahs[0].classificationAr.contains("مؤكّدة"))
        assertTrue(sajdahs[1].classificationAr.contains("الشافعية"))
        assertTrue(sajdahs[2].classificationAr.contains("شكر"))

        val topics = QuranExplorerCatalog.buildTopicGroups(sampleAyahs)
        assertEquals(2, topics.size)
        assertEquals("aqeedah", topics[0].id)
        assertEquals(42, topics[0].items.first().pageNumber)
        assertEquals("sabr_sakina", topics[1].id)
        assertEquals(252, topics[1].items.first().pageNumber)
    }
}
