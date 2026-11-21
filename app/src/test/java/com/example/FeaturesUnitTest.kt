package com.example

import com.example.data.local.AyahBookmark
import com.example.data.local.BookmarkCategory
import com.example.data.local.BookmarksRepository
import com.example.domain.model.AppDigitConfig
import com.example.domain.model.localizeDigits
import com.example.domain.model.toAppDigits
import com.example.domain.prayer.PrayerTimesCalculator
import com.example.ui.components.CropTransformMetrics
import com.example.ui.components.MushafCropCoordinateMap
import com.example.ui.components.SvgViewBoxRect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeaturesUnitTest {

    @Test
    fun digitFormatter_switchesBetweenArabicIndicAndWesternDigits() {
        val prev = AppDigitConfig.useWesternDigits
        try {
            assertEquals("صفحة ١٢٣ من ٦٠٤", "صفحة 123 من 604".localizeDigits(useWestern = false))
            assertEquals("صفحة 123 من 604", "صفحة ١٢٣ من ٦٠٤".localizeDigits(useWestern = true))
            assertEquals("٢٥٥", 255.toAppDigits(useWestern = false))
            assertEquals("255", 255.toAppDigits(useWestern = true))
            assertEquals("1000", 1000L.toAppDigits(useWestern = true))

            AppDigitConfig.useWesternDigits = true
            assertEquals("01:02:05", PrayerTimesCalculator.formatCountdown((3600 + 120 + 5) * 1000L))
            AppDigitConfig.useWesternDigits = false
            assertEquals("٠١:٠٢:٠٥", PrayerTimesCalculator.formatCountdown((3600 + 120 + 5) * 1000L))
        } finally {
            AppDigitConfig.useWesternDigits = prev
        }
    }

    @Test
    fun bookmarksRepository_encodesAndDecodesCategoriesAndNotes() {
        val items = listOf(
            AyahBookmark(
                id = BookmarksRepository.buildId(255, 2, 255),
                page = 42,
                surahNumber = 2,
                ayahNumber = 255,
                surahNameAr = "البقرة",
                category = BookmarkCategory.MEMORIZATION,
                note = "آية الكرسي للمراجعة اليومية",
                createdAtMillis = 1700000000000L
            ),
            AyahBookmark(
                id = BookmarksRepository.buildId(1, 1, 1),
                page = 1,
                surahNumber = 1,
                ayahNumber = 1,
                surahNameAr = "الفاتحة",
                category = BookmarkCategory.REFLECTION,
                note = "",
                createdAtMillis = 1700000001000L
            )
        )
        val json = BookmarksRepository.encodeBookmarks(items)
        val decoded = BookmarksRepository.decodeBookmarks(json)
        assertEquals(2, decoded.size)
        assertEquals(BookmarkCategory.MEMORIZATION, decoded[0].category)
        assertEquals("آية الكرسي للمراجعة اليومية", decoded[0].note)
        assertEquals(42, decoded[0].page)
        assertEquals(255, decoded[0].ayahNumber)
        assertEquals(BookmarkCategory.REFLECTION, decoded[1].category)
        assertTrue(BookmarksRepository.decodeBookmarks("not-json").isEmpty())
    }

    @Test
    fun mushafCropCoordinateMap_roundTripsViewportAndSvgCoordinates() {
        val viewBox = SvgViewBoxRect(minX = -6f, minY = 0f, width = 345f, height = 550f)
        val metrics = CropTransformMetrics(
            inputW = 690,
            inputH = 1100,
            srcLeft = 20,
            srcTop = 30,
            srcW = 650,
            srcH = 1040,
            dstLeft = 36,
            dstTop = 24,
            dstW = 1008,
            dstH = 1752,
            targetW = 1080,
            targetH = 1800
        )
        val boxW = 1080f
        val boxH = 1800f
        val expectedSvgX = 166.5f
        val expectedSvgY = 275.0f

        val (vx, vy) = MushafCropCoordinateMap.svgPointToViewport(
            svgX = expectedSvgX,
            svgY = expectedSvgY,
            viewBox = viewBox,
            metrics = metrics,
            boxWidthPx = boxW,
            boxHeightPx = boxH
        )
        val back = MushafCropCoordinateMap.viewportPointToSvg(
            vx = vx,
            vy = vy,
            viewBox = viewBox,
            metrics = metrics,
            boxWidthPx = boxW,
            boxHeightPx = boxH
        )
        assertNotNull(back)
        assertEquals(expectedSvgX, back!!.first, 0.1f)
        assertEquals(expectedSvgY, back.second, 0.1f)
    }

    @Test
    fun reciters_haveHttpsPortraitPhotoUrls_andTasbihAdhkarAreSeparated() {
        assertTrue(com.example.domain.model.QuranReciters.all.all { it.photoUrl.startsWith("https://") })

        val sleepItems = com.example.data.adhkar.AdhkarCatalog.byCategory(com.example.domain.model.DhikrCategory.SLEEP)
        assertEquals(33, sleepItems.first { it.id == "sleep_subhanallah" }.targetCount)
        assertEquals(33, sleepItems.first { it.id == "sleep_tasbih_alhamdulillah" }.targetCount)
        assertEquals(34, sleepItems.first { it.id == "sleep_allahuakbar" }.targetCount)

        val tasbihItems = com.example.data.adhkar.AdhkarCatalog.byCategory(com.example.domain.model.DhikrCategory.TASBIH)
        assertEquals(33, tasbihItems.first { it.id == "tasbih_subhanallah" }.targetCount)
        assertEquals(33, tasbihItems.first { it.id == "tasbih_alhamdulillah" }.targetCount)
        assertEquals(33, tasbihItems.first { it.id == "tasbih_allahuakbar" }.targetCount)
    }

    @Test
    fun offlineDownloadHub_formatsSizesAggregatesRiwayahsAndComputesProgress() {
        assertEquals("512 ك.ب", com.example.domain.model.formatStorageSizeAr(512 * 1024L, useWestern = true))
        assertEquals("٥١٢ ك.ب", com.example.domain.model.formatStorageSizeAr(512 * 1024L, useWestern = false))
        assertEquals("12.5 م.ب", com.example.domain.model.formatStorageSizeAr((12.5 * 1024 * 1024).toLong(), useWestern = true))

        val hafsKey = com.example.domain.model.DownloadItemKeys.riwayah(com.example.data.local.RiwayahEdition.HAFS)
        val warshKey = com.example.domain.model.DownloadItemKeys.riwayah(com.example.data.local.RiwayahEdition.WARSH)
        val muyassarKey = com.example.domain.model.DownloadItemKeys.tafsir(com.example.domain.model.TafsirEdition.MUYASSAR)

        val state = com.example.domain.model.OfflineInventoryUiState(
            riwayahStatuses = listOf(
                com.example.domain.model.RiwayahDownloadStatus(
                    edition = com.example.data.local.RiwayahEdition.HAFS,
                    downloadedPages = 604,
                    sizeBytes = 10_000_000L
                ),
                com.example.domain.model.RiwayahDownloadStatus(
                    edition = com.example.data.local.RiwayahEdition.WARSH,
                    downloadedPages = 302,
                    sizeBytes = 5_000_000L,
                    isDownloading = true
                )
            ),
            tafsirStatuses = listOf(
                com.example.domain.model.TafsirDownloadStatus(
                    edition = com.example.domain.model.TafsirEdition.MUYASSAR,
                    downloadedSurahs = 114,
                    sizeBytes = 2_000_000L
                )
            ),
            selectedKeys = setOf(hafsKey, warshKey, muyassarKey),
            activeKeys = setOf(warshKey, muyassarKey),
            isBatchDownloading = true
        )

        assertEquals(1, state.completedRiwayahs.size)
        assertEquals(com.example.data.local.RiwayahEdition.HAFS, state.completedRiwayahs.first().edition)
        assertEquals(1, state.selectableSelectedCount) // only incomplete warsh counts
        assertEquals(17_000_000L, state.totalStorageBytes)
        assertEquals(0.75f, state.overallBatchProgress, 0.01f)
        val summary = state.compactSummaryAr(useWestern = true)
        assertTrue(summary.contains("حفص"))
        assertTrue(summary.contains("التفسير: 1"))
    }

    @Test
    fun hisnMuslimParser_mapsOnlyExistingCategoriesAndSplitsCombinedTasbih() {
        val indexJson = "\uFEFF" + """
            {
              "العربية": [
                {
                  "ID": 27,
                  "TITLE": "أذكار الصباح والمساء",
                  "AUDIO_URL": "http://www.hisnmuslim.com/audio/ar/27.mp3",
                  "TEXT": "http://www.hisnmuslim.com/api/ar/27.json"
                },
                {
                  "ID": 28,
                  "TITLE": "أذكار النوم",
                  "TEXT": "https://www.hisnmuslim.com/api/ar/28.json"
                },
                {
                  "ID": 6,
                  "TITLE": "دعاء لبس الثوب",
                  "TEXT": "http://www.hisnmuslim.com/api/ar/6.json"
                }
              ]
            }
        """.trimIndent()

        val chapters = com.example.data.adhkar.HisnMuslimParser.parseIndex(indexJson)
        assertEquals(3, chapters.size)
        assertEquals("https://www.hisnmuslim.com/api/ar/27.json", chapters[0].textUrl)

        assertEquals(
            listOf(com.example.domain.model.DhikrCategory.MORNING, com.example.domain.model.DhikrCategory.EVENING),
            com.example.data.adhkar.HisnCategoryMapper.mapChapterToCategories(chapters[0])
        )
        assertEquals(
            listOf(com.example.domain.model.DhikrCategory.SLEEP),
            com.example.data.adhkar.HisnCategoryMapper.mapChapterToCategories(chapters[1])
        )
        assertEquals(
            listOf(com.example.domain.model.DhikrCategory.DAILY_LIFE),
            com.example.data.adhkar.HisnCategoryMapper.mapChapterToCategories(chapters[2])
        )

        val chapterJson = """
            {
              "أذكار النوم": [
                {
                  "ID": 99,
                  "ARABIC_TEXT": "سُبْحَانَ اللَّهِ، وَالْحَمْدُ لِلَّهِ، وَاللَّهُ أَكْبَرُ",
                  "REPEAT": 33,
                  "AUDIO": "http://www.hisnmuslim.com/audio/ar/99.mp3",
                  "LANGUAGE_ARABIC_TRANSLATED_TEXT": "تقال قبل النوم"
                },
                {
                  "ID": 100,
                  "ARABIC_TEXT": "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي",
                  "REPEAT": 1
                }
              ]
            }
        """.trimIndent()

        val parsedItems = com.example.data.adhkar.HisnMuslimParser.parseChapterItems(
            rawJson = chapterJson,
            chapter = chapters[1],
            category = com.example.domain.model.DhikrCategory.SLEEP
        )
        assertEquals(4, parsedItems.size)
        assertEquals(33, parsedItems[0].targetCount)
        assertEquals(33, parsedItems[1].targetCount)
        assertEquals(34, parsedItems[2].targetCount)
        assertEquals(1, parsedItems[3].targetCount)
        assertEquals("https://www.hisnmuslim.com/audio/ar/99.mp3", parsedItems[0].audioUrl)

        val encoded = com.example.data.adhkar.HisnMuslimParser.encodeCache(
            cachedAtMillis = 123456L,
            map = mapOf(com.example.domain.model.DhikrCategory.SLEEP to parsedItems)
        )
        val decoded = com.example.data.adhkar.HisnMuslimParser.decodeCache(encoded)
        assertNotNull(decoded)
        assertEquals(123456L, decoded!!.cachedAtMillis)
        assertEquals(4, decoded.itemsByCategory[com.example.domain.model.DhikrCategory.SLEEP]?.size)
        assertEquals(
            "https://www.hisnmuslim.com/audio/ar/99.mp3",
            decoded.itemsByCategory[com.example.domain.model.DhikrCategory.SLEEP]?.firstOrNull()?.audioUrl
        )
    }

    @Test
    fun newEnhancements_tafsirEditions_hijriOffset_adhkarHub_sleepTimerAndStatsWorkAsExpected() {
        val slugs = com.example.domain.model.TafsirEdition.entries.map { it.slug }
        assertEquals(
            listOf("ar-tafsir-muyassar", "ar-tafseer-al-saddi", "ar-tafsir-ibn-kathir", "ar-tafsir-al-baghawi"),
            slugs
        )

        val prayerDefaults = com.example.data.local.PrayerSettings()
        assertEquals(0, prayerDefaults.hijriOffsetDays)
        assertEquals(2, prayerDefaults.copy(hijriOffsetDays = 2).hijriOffsetDays)

        val hub = com.example.domain.model.OfflineInventoryUiState(
            adhkarStatus = com.example.domain.model.AdhkarDownloadStatus(
                isSynced = true,
                downloadedChapters = 132,
                sizeBytes = 640_000L
            ),
            selectedKeys = setOf(com.example.domain.model.DownloadItemKeys.ADHKAR)
        )
        assertTrue(hub.adhkarStatus.isComplete)
        assertEquals(0, hub.selectableSelectedCount)
        assertEquals(640_000L, hub.totalStorageBytes)
        assertTrue(hub.compactSummaryAr(useWestern = true).contains("حصن المسلم"))

        val audioState = com.example.data.audio.AudioPlaybackState()
        assertEquals(null, audioState.sleepTimerRemainingSec)
        assertEquals(false, audioState.stopAfterCurrentSurah)
        val stats = com.example.data.local.ReadingStats()
        assertEquals(0, stats.adhkarTodayCount)
        assertEquals(0, stats.adhkarTotalCount)
    }
}
