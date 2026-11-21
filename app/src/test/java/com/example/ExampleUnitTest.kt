package com.example

import com.example.data.adhkar.AdhkarCatalog
import com.example.data.local.AppTheme
import com.example.data.local.QuranFontOption
import com.example.data.local.ReaderSettings
import com.example.data.remote.QuranWsCdnClient
import com.example.data.repository.QuranRepository
import com.example.domain.model.DhikrCategory
import com.example.domain.model.QuranReciters
import com.example.domain.model.toArabicIndicDigits
import com.example.domain.prayer.AsrJuristicMethod
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.PrayerName
import com.example.domain.prayer.PrayerTimesCalculator
import com.example.ui.theme.AlyamamaFontFamily
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.AmiriQuranFontFamily
import com.example.ui.theme.QURAN_LINE_HEIGHT_RATIO
import com.example.ui.theme.ScheherazadeNewFontFamily
import com.example.ui.theme.Typography
import com.example.ui.theme.resolveQuranFontFamily
import com.example.ui.util.DisplayRefreshConfig
import com.example.ui.util.selectHighestRefreshMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ws.quran.qurantext.Mushaf
import java.io.File
import java.util.Calendar
import java.util.TimeZone

class ExampleUnitTest {
    @Test
    fun quranTextDataset_hasAll114SurahsAnd604Pages() {
        val mushaf = Mushaf.hafs()
        assertEquals(114, mushaf.surahs.size)
        assertEquals(604, mushaf.pageCount)
        assertEquals(6236, mushaf.ayahCount)
    }

    @Test
    fun arabicNormalization_stripsTashkeelAndNormalizesAlef() {
        val normalized = QuranRepository.normalizeArabic("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ")
        assertTrue(normalized.contains("الرحم"))
        assertEquals("الحمد لله", QuranRepository.normalizeArabic("الْحَمْدُ لِلَّهِ"))
    }

    @Test
    fun cdnUrls_andReciterAudioUrls_formatCorrectly() {
        val svgUrl = QuranWsCdnClient.buildSvgPageUrl("v1.1.1", "hafs-kfqc", 1)
        assertEquals("https://cdn.quran.ws/svg/pages/v1.1.1/hafs-kfqc/001.svg", svgUrl)

        val audioUrl = QuranReciters.all.first().ayahAudioUrl(2, 255)
        assertEquals("https://everyayah.com/data/Abdul_Basit_Murattal_192kbps/002255.mp3", audioUrl)

        val alafasyUrl = QuranReciters.byId("Alafasy_128kbps").ayahAudioUrl(2, 255)
        assertEquals("https://everyayah.com/data/Alafasy_128kbps/002255.mp3", alafasyUrl)
    }

    @Test
    fun arabicIndicDigits_convertsNumbersAccurately() {
        assertEquals("٦٠٤", 604.toArabicIndicDigits())
        assertEquals("١١٤", 114.toArabicIndicDigits())
    }

    @Test
    fun kotlinSourceFiles_mustNotExceed333Lines() {
        val maxLines = 333
        val srcRoot = listOf(File("src"), File("app/src")).first { it.exists() }
        val ktFiles = srcRoot.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        assertTrue("Expected Kotlin source files under ${srcRoot.path}", ktFiles.isNotEmpty())
        val violations = ktFiles.mapNotNull { file ->
            val count = file.useLines { it.count() }
            if (count > maxLines) "${file.relativeTo(srcRoot).path}: $count lines" else null
        }
        assertEquals("Files exceeding $maxLines lines: $violations", emptyList<String>(), violations)
    }

    @Test
    fun selectHighestRefreshMode_picksMaxRefreshMatchingActiveResolution() {
        val active = DisplayRefreshConfig(modeId = 1, width = 1440, height = 3120, refreshRate = 60f)
        val modes = listOf(
            active,
            DisplayRefreshConfig(modeId = 2, width = 1440, height = 3120, refreshRate = 90f),
            DisplayRefreshConfig(modeId = 3, width = 1440, height = 3120, refreshRate = 120f),
            DisplayRefreshConfig(modeId = 4, width = 1440, height = 3120, refreshRate = 144f),
            DisplayRefreshConfig(modeId = 5, width = 1080, height = 2340, refreshRate = 240f)
        )
        val best = selectHighestRefreshMode(active, modes)
        assertEquals(4, best?.modeId)
        assertEquals(144f, best?.refreshRate)
    }

    @Test
    fun selectHighestRefreshMode_picksGlobalMaxWhenActiveModeIsNull() {
        val modes = listOf(
            DisplayRefreshConfig(modeId = 1, width = 1080, height = 2400, refreshRate = 60f),
            DisplayRefreshConfig(modeId = 2, width = 1080, height = 2400, refreshRate = 165f)
        )
        val best = selectHighestRefreshMode(null, modes)
        assertEquals(2, best?.modeId)
        assertEquals(165f, best?.refreshRate)
    }

    @Test
    fun adhkarCatalog_hasAllCategoriesPopulatedAndValid() {
        DhikrCategory.entries.forEach { category ->
            val items = AdhkarCatalog.byCategory(category)
            assertTrue("Expected Adhkar items for ${category.name}", items.isNotEmpty())
            assertTrue(items.all { it.title.isNotBlank() && it.arabicText.isNotBlank() && it.targetCount > 0 })
        }
    }

    @Test
    fun themeAndFontSettings_useUnifiedAppThemeAndAlyamamaDefault() {
        val defaults = ReaderSettings()
        assertEquals(AppTheme.SYSTEM, defaults.appTheme)
        assertEquals(3, AppTheme.entries.size)
        assertEquals(AppTheme.SEPIA, AppTheme.fromStoredName("CREAM"))
        assertEquals(QuranFontOption.ALYAMAMA, defaults.quranFont)
        assertEquals(ScheherazadeNewFontFamily, resolveQuranFontFamily(QuranFontOption.SCHEHERAZADE_NEW))
        assertEquals(AlyamamaFontFamily, resolveQuranFontFamily(QuranFontOption.ALYAMAMA))
        assertEquals(AmiriQuranFontFamily, resolveQuranFontFamily(QuranFontOption.AMIRI_QURAN))
        assertEquals(AmiriFontFamily, resolveQuranFontFamily(QuranFontOption.AMIRI))
        assertEquals(AlyamamaFontFamily, Typography.bodyLarge.fontFamily)
        assertEquals(AlyamamaFontFamily, Typography.headlineLarge.fontFamily)
        assertTrue(QURAN_LINE_HEIGHT_RATIO >= 1.5f)
    }

    @Test
    fun prayerTimesCalculator_computesChronologicalTimesAndQiblaBearing() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Riyadh")).apply {
            set(2026, Calendar.MARCH, 20, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val shafii = PrayerTimesCalculator.calculateForDate(
            calendar = cal,
            latitude = 24.7136,
            longitude = 46.6753,
            method = CalculationMethod.UMM_AL_QURA,
            asrMethod = AsrJuristicMethod.SHAFII
        )
        val hanafi = PrayerTimesCalculator.calculateForDate(
            calendar = cal,
            latitude = 24.7136,
            longitude = 46.6753,
            method = CalculationMethod.UMM_AL_QURA,
            asrMethod = AsrJuristicMethod.HANAFI
        )

        assertEquals(6, shafii.entries.size)
        for (i in 0 until shafii.entries.lastIndex) {
            assertTrue(
                "Expected ${shafii.entries[i].prayer} before ${shafii.entries[i + 1].prayer}",
                shafii.entries[i].timestampMillis < shafii.entries[i + 1].timestampMillis
            )
        }
        val shafiiAsr = shafii.entries.first { it.prayer == PrayerName.ASR }.timestampMillis
        val hanafiAsr = hanafi.entries.first { it.prayer == PrayerName.ASR }.timestampMillis
        assertTrue("Hanafi Asr should be later than Shafi'i Asr", hanafiAsr > shafiiAsr)
        assertTrue("Midnight should be before last third of night", shafii.midnightMillis < shafii.lastThirdMillis)

        // Cairo -> Makkah bearing is roughly Southeast (~136 degrees)
        val cairoBearing = PrayerTimesCalculator.calculateQiblaBearing(30.0444, 31.2357)
        assertTrue("Expected Cairo Qibla bearing near 136°, got $cairoBearing", cairoBearing in 130.0..142.0)

        assertEquals("٠١:٠٢:٠٥", PrayerTimesCalculator.formatCountdown((3600 + 120 + 5) * 1000L))
    }

    @Test
    fun mushafSvgMarkerRecolorer_recolorsHafsAndDouriTargetsWithoutTouchingContent() {
        val sampleHafsSvg = """
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:ayah="https://quranpedia.net">
              <g id="ayah_markers" class="ayah_markers">
                <g class="ayah-mark"><g class="ayah-mark-ornament" transform="translate(1 2)"><path d="M0 0" fill="#231f20" fill-rule="evenodd" /></g><g transform="translate(3 4)" ayah:x="10" ayah:y="20"><path d="M1 1" fill="#231f20" /></g></g>
              </g>
              <g id="content"><path d="M5 5" fill="#231f20" /></g>
              <path class="ayahPolygon" fill-opacity="0" number="001001" />
            </svg>
        """.trimIndent()

        val numberOnly = com.example.ui.components.MushafSvgMarkerRecolorer.recolorAyahMarkers(
            svg = sampleHafsSvg,
            ornamentHex = null,
            numberHex = "#1B6B46"
        )
        assertTrue(numberOnly.contains("""<g class="ayah-mark-ornament" transform="translate(1 2)"><path d="M0 0" fill="#231f20""""))
        assertTrue(numberOnly.contains("""ayah:x="10" ayah:y="20"><path d="M1 1" fill="#1B6B46""""))
        assertTrue(numberOnly.contains("""<g id="content"><path d="M5 5" fill="#231f20" /></g>"""))

        val ornamentOnly = com.example.ui.components.MushafSvgMarkerRecolorer.recolorAyahMarkers(
            svg = sampleHafsSvg,
            ornamentHex = "#9E6E1B",
            numberHex = null
        )
        assertTrue(ornamentOnly.contains("""<g class="ayah-mark-ornament" transform="translate(1 2)"><path d="M0 0" fill="#9E6E1B""""))
        assertTrue(ornamentOnly.contains("""ayah:x="10" ayah:y="20"><path d="M1 1" fill="#231f20""""))

        val sampleDouriSvg = """
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:ayah="https://quranpedia.net">
              <g id="ayah_markers"><g class="ayah-mark"><g class="ayah-mark-ornament"><path d="M0" fill="#231f20"/></g><g ayah:x="1" ayah:y="2"><path class="st1" d="M2"/></g></g></g>
              <g id="content"><path class="st1" d="M9"/></g>
            </svg>
        """.trimIndent()
        val douriBoth = com.example.ui.components.MushafSvgMarkerRecolorer.recolorAyahMarkers(
            svg = sampleDouriSvg,
            ornamentHex = "#1B5694",
            numberHex = "#1B5694"
        )
        assertTrue(douriBoth.contains("""<g ayah:x="1" ayah:y="2"><path fill="#1B5694" d="M2"/></g>"""))
        assertTrue(douriBoth.contains("""<g id="content"><path class="st1" d="M9"/></g>"""))
    }

    @Test
    fun mushafSvgSurahBannerInjector_placesTopMidSurah9SliverAndSkipsIlluminatedPages() {
        val sampleTemplate = """<g id="q_sh_m"><g fill="{{SLOT_FILL}}"><path d="M0 0 H10 V10 H0 Z"/></g><g fill="{{INK_FILL}}"><path d="M0 0 H10"/></g></g>"""

        // 1) Illuminated pages (001 & 002) have negative viewBox minY and must remain untouched
        val illuminated = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="-53.31 -198.47 345 550">
              <g id="content"><path d="M0 0"/></g>
              <path class="ayahPolygon" surah="1" ayah="1" d="M 10 10 L 300 10 L 300 40 L 10 40 Z" />
            </svg>
        """.trimIndent()
        assertEquals(
            illuminated,
            com.example.ui.components.MushafSvgSurahBannerInjector.injectSurahBanners(illuminated, sampleTemplate, "#1B6B46")
        )

        // 2) Top-of-page Surah 10 + mid-page Surah 113 with Warsh-style left sliver on previous line
        val mixedPage = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="-6 0 345 550">
              <g id="content"><path d="M1 1"/></g>
              <path class="ayahPolygon" surah="112" ayah="4" d="M 65 115 L 337 115 L 337 154.25 L 65 154.25 Z" />
              <path class="ayahPolygon" surah="113" ayah="1" d="M -6 115 L 64.5 115 L 64.5 154.25 L -6 154.25 Z M 190.5 228.25 L 337 228.25 L 337 261.5 L 190.5 261.5 Z" />
            </svg>
        """.trimIndent()
        val placements = com.example.ui.components.MushafSvgSurahBannerInjector.computeBannerPlacements(mixedPage)
        assertEquals(1, placements.size)
        assertEquals(113, placements[0].surah)
        assertEquals(-6f, placements[0].leftX, 0.01f)
        assertEquals(343f, placements[0].width, 0.01f)
        assertEquals(174.75f, placements[0].centerY, 0.05f)

        val injected = com.example.ui.components.MushafSvgSurahBannerInjector.injectSurahBanners(
            mixedPage,
            sampleTemplate,
            "#1B6B46"
        )
        assertTrue(injected.contains("xmlns:xlink=\"http://www.w3.org/1999/xlink\""))
        assertTrue(injected.contains("<g id=\"surah_header_banners\">"))
        assertTrue(injected.indexOf("<g id=\"surah_header_banners\">") < injected.indexOf("<g id=\"content\">"))
        assertTrue(injected.contains("fill=\"#1B6B46\""))

        // 3) Real inverted-matrix Hafs structure: banners must be at <svg> root BEFORE matrix(),
        //    and top/mid Surah titles must be detected above Basmalah (never on Basmalah or between lines)
        val matrixHafsPage = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 345 550">
              <g transform="matrix(1.3333 0 0 -1.3333 -115 640)">
                <g id="ayah_markers" class="ayah_markers"></g>
                <g id="content">
                  <g transform="translate(200 462)">
                    <path d="M0 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z"/>
                  </g>
                  <g transform="translate(200 352)">
                    <path d="M0 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z m12 0 h10 v10 h-10 z"/>
                  </g>
                </g>
              </g>
              <path class="ayahPolygon" surah="112" ayah="1" d="M 0 74.25 L 341 74.25 L 341 112.75 L 0 112.75 Z" />
              <path class="ayahPolygon" surah="112" ayah="4" d="M 0 112.75 L 341 112.75 L 341 148.25 L 0 148.25 Z" />
              <path class="ayahPolygon" surah="113" ayah="1" d="M 0 218.5 L 341 218.5 L 341 257.25 L 0 257.25 Z" />
            </svg>
        """.trimIndent()
        val hafsPlacements = com.example.ui.components.MushafSvgSurahBannerInjector.computeBannerPlacements(matrixHafsPage)
        assertEquals(2, hafsPlacements.size)
        assertEquals(112, hafsPlacements[0].surah)
        assertTrue("Top surah 112 centerY=${hafsPlacements[0].centerY}", hafsPlacements[0].centerY in 15f..33f)
        assertEquals(113, hafsPlacements[1].surah)
        assertTrue("Mid surah 113 centerY=${hafsPlacements[1].centerY}", hafsPlacements[1].centerY in 162f..212f)

        val injectedHafs = com.example.ui.components.MushafSvgSurahBannerInjector.injectSurahBanners(
            matrixHafsPage,
            sampleTemplate,
            "#1B6B46"
        )
        assertTrue(
            injectedHafs.indexOf("<g id=\"surah_header_banners\">") <
                injectedHafs.indexOf("<g transform=\"matrix(1.3333 0 0 -1.3333 -115 640)\">")
        )

        // 4) Surah 9 (no Basmalah) at top of page
        val surah9Page = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 345 550">
              <g id="content"><path d="M1 1"/></g>
              <path class="ayahPolygon" surah="9" ayah="1" d="M 0 39.5 L 338 39.5 L 338 75 L 0 75 Z" />
            </svg>
        """.trimIndent()
        val s9Placements = com.example.ui.components.MushafSvgSurahBannerInjector.computeBannerPlacements(surah9Page)
        assertEquals(1, s9Placements.size)
        assertEquals(25f, s9Placements[0].centerY, 0.5f)
    }
}
