package com.example

import androidx.compose.ui.graphics.Color
import com.example.data.audio.RiwayahRecitersRepository
import com.example.data.local.AyahHighlightColorPreset
import com.example.data.local.AyahHighlightStyle
import com.example.data.local.ReaderSettings
import com.example.data.local.RiwayahEdition
import com.example.domain.model.QuranReciters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MultiRiwayahAndHighlightTest {

    @Test
    fun highlightCustomization_resolvesMarkerFollowAndCustomPresets() {
        val marker = Color(0xFF1B6B46L)
        val defaults = ReaderSettings()
        assertEquals(AyahHighlightColorPreset.FOLLOW_MARKER, defaults.ayahHighlightColor)
        assertEquals(AyahHighlightStyle.FILL_AND_BORDER, defaults.ayahHighlightStyle)
        assertEquals(0.22f, defaults.ayahHighlightAlpha, 0.001f)
        assertEquals(marker, defaults.resolveAyahHighlightColor(isNightPaper = false, markerAccentColor = marker))

        val custom = defaults.copy(
            ayahHighlightColor = AyahHighlightColorPreset.GOLD,
            ayahHighlightStyle = AyahHighlightStyle.BORDER_ONLY,
            ayahHighlightAlpha = 0.35f
        )
        assertEquals(Color(AyahHighlightColorPreset.GOLD.lightColorInt), custom.resolveAyahHighlightColor(false, marker))
        assertEquals(Color(AyahHighlightColorPreset.GOLD.nightColorInt), custom.resolveAyahHighlightColor(true, marker))
    }

    @Test
    fun curatedReciters_coverHafsAndWarshWithBiosAndCategories() {
        val hafs = QuranReciters.all.filter { it.riwayah == RiwayahEdition.HAFS }
        val warsh = QuranReciters.all.filter { it.riwayah == RiwayahEdition.WARSH }
        assertTrue(hafs.size >= 8)
        assertTrue(warsh.size >= 3)
        assertTrue(QuranReciters.all.all { it.categoryAr.isNotBlank() && it.bioAr.isNotBlank() })
        assertTrue(
            warsh.first().ayahAudioUrl(2, 255).contains("everyayah.com/data/warsh/")
        )
    }

    @Test
    fun riwayahRecitersRepository_parsesQaloonDouriShubahAndWarsh() {
        val sampleJson = """
            {
              "reciters": [
                {
                  "id": 10,
                  "name": "محمود خليل الحصري",
                  "moshaf": [
                    {
                      "id": 1001,
                      "name": "قالون عن نافع - مرتل",
                      "server": "https://server8.mp3quran.net/qalon_husr/",
                      "surah_total": 114
                    }
                  ]
                },
                {
                  "id": 20,
                  "name": "نورين محمد صديق",
                  "moshaf": [
                    {
                      "id": 1002,
                      "name": "الدوري عن أبي عمرو",
                      "server": "https://server16.mp3quran.net/nourin_dori",
                      "surah_total": 114
                    }
                  ]
                },
                {
                  "id": 30,
                  "name": "عبد الرشيد صوفي",
                  "moshaf": [
                    {
                      "id": 1003,
                      "name": "شعبة عن عاصم",
                      "server": "https://server16.mp3quran.net/soufi_shobah/",
                      "surah_total": 114
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val ctx = RuntimeEnvironment.getApplication()
        val parsed = RiwayahRecitersRepository(ctx).parseRecitersJson(sampleJson)
        assertEquals(3, parsed.size)

        val qalon = parsed.first { it.riwayah == RiwayahEdition.QALON }
        val douri = parsed.first { it.riwayah == RiwayahEdition.DOURI }
        val shubah = parsed.first { it.riwayah == RiwayahEdition.SHUBAH }

        assertTrue(qalon.isSurahLevelAudio)
        assertEquals("https://server8.mp3quran.net/qalon_husr/002.mp3", qalon.ayahAudioUrl(2, 255))
        assertEquals("https://server16.mp3quran.net/nourin_dori/001.mp3", douri.ayahAudioUrl(1, 1))
        assertFalse(shubah.bioAr.isBlank())
    }

    @Test
    fun pureWirdReadingMode_defaultsEnabledAndBuildsCorrectRoutes() {
        val defaults = ReaderSettings()
        assertTrue(defaults.wirdPureReadingMode)
        assertFalse(defaults.copy(wirdPureReadingMode = false).wirdPureReadingMode)
        assertEquals("reader?fromWird=true&wirdIndex=3&planId=", com.example.ui.navigation.readerRoute(fromWird = true, wirdIndex = 3))
        assertEquals("reader?fromWird=false&wirdIndex=-1&planId=", com.example.ui.navigation.readerRoute(fromWird = false))
    }

    @Test
    fun wirdPlanCalculator_juzAmmaSingleWirdScopesExactPages() {
        val quarters = (0 until 240).map { idx ->
            val juz = (idx / 8) + 1
            val page = if (juz == 30 && idx == 232) 582 else 1 + idx * 2
            com.example.domain.model.HizbQuarterInfo(
                rubNumber = idx + 1,
                juzNumber = juz,
                hizbNumber = (idx / 4) + 1,
                quarterInHizb = idx % 4,
                surahNumber = if (juz == 30) 78 else 1,
                surahNameAr = if (juz == 30) "النبأ" else "الفاتحة",
                ayahNumber = 1,
                pageNumber = page,
                openingText = "عم يتساءلون"
            )
        }
        val ayahs = listOf(
            com.example.domain.model.AyahItem(
                surahNumber = 78, surahNameAr = "النبأ", ayahNumber = 1,
                text = "عم يتساءلون", imlaiText = "عم يتساءلون", marker = "١",
                pageNumber = 582, juzNumber = 30, hasSajdah = false
            ),
            com.example.domain.model.AyahItem(
                surahNumber = 114, surahNameAr = "الناس", ayahNumber = 6,
                text = "من الجنة والناس", imlaiText = "من الجنة والناس", marker = "٦",
                pageNumber = 604, juzNumber = 30, hasSajdah = false
            )
        )
        val config = com.example.domain.wird.KhatmahPlanConfig(
            isActive = true,
            startJuz = 30,
            quartersPerDay = 8,
            completedWirdsCount = 0
        )
        val plan = com.example.domain.wird.WirdPlanCalculator.buildPlan(
            config = config,
            quarters = quarters,
            allAyahs = ayahs,
            ajza = emptyList(),
            todayEpochDay = 1L
        )
        val portion = plan?.currentWird
        assertEquals(1, plan?.totalWirds)
        assertEquals(582, portion?.startPage)
        assertEquals(604, portion?.endPage)
        assertEquals(23, portion?.pageSpanCount)
        assertEquals(78, portion?.startSurahNumber)
        assertEquals(114, portion?.endSurahNumber)

        val scope = com.example.ui.screens.reader.resolveWirdReaderScope(
            openedFromWird = true,
            plan = plan,
            wirdIndex = 0
        )
        assertEquals(582, scope.startPage)
        assertEquals(604, scope.endPage)
        assertEquals(23, scope.pageCount(isTwoPageSpread = false))
        assertEquals(0, scope.pageToIndex(582, isTwoPageSpread = false))
        assertEquals(22, scope.pageToIndex(604, isTwoPageSpread = false))
        assertEquals(582, scope.indexToPage(0, isTwoPageSpread = false))
        assertEquals(604, scope.indexToPage(22, isTwoPageSpread = false))
    }

    @Test
    fun mushafSvgAyahMap_aggregatesSplitAyahsAndDeduplicatesVertices() {
        val sampleSvg = """
            <svg viewBox="0 0 300 500">
              <path class="ayahPolygon" surah="2" ayah="255" d="M 10 10 L 10.01 10.01 L 100 10 L 100 40 L 10 40 Z" />
              <path class="ayahPolygon" surah="2" ayah="255" d="M 20 50 L 120 50 L 120 80 L 20 80 Z" />
              <polygon class="ayahPolygon" surah="2" ayah="256" points="10,100 90,100 90,130 10,130" />
            </svg>
        """.trimIndent()

        val parsed = com.example.ui.components.MushafSvgAyahMap.parseSvg("hafs_42", sampleSvg)
        assertTrue(parsed != null)
        val rects255 = parsed!!.rectsForAyah(2, 255)
        val polys255 = parsed.polygonsForAyah(2, 255)
        assertEquals(2, rects255.size)
        assertEquals(2, polys255.size)
        // Near-duplicate point (10.01, 10.01) must be filtered out -> 4 vertices (8 floats)
        assertEquals(8, polys255.first().size)

        // 4-vertex quadrilateral (8 floats >= 6) must hit-test accurately
        val hitFirst = parsed.findAyahAt(50f, 25f)
        val hitSecond = parsed.findAyahAt(70f, 65f)
        val hitNextAyah = parsed.findAyahAt(50f, 115f)
        assertEquals(255, hitFirst?.ayahNumber)
        assertEquals(255, hitSecond?.ayahNumber)
        assertEquals(256, hitNextAyah?.ayahNumber)
    }
}
