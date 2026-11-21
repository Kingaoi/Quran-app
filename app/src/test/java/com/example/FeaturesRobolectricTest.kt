package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AyahBookmark
import com.example.data.local.BackupManager
import com.example.data.local.BookmarkCategory
import com.example.data.local.BookmarksRepository
import com.example.data.local.ReaderPreferencesRepository
import com.example.data.local.ReadingStatsRepository
import com.example.ui.components.MushafSvgAyahMap
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = QuranApplication::class)
class FeaturesRobolectricTest {

    @Test
    fun `backupManager exports and imports reader settings bookmarks and stats`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val readerPrefs = ReaderPreferencesRepository(context)
        val bookmarksRepo = BookmarksRepository(context)
        val statsRepo = ReadingStatsRepository(context)
        val backupManager = BackupManager(context)

        readerPrefs.setUseWesternDigits(true)
        readerPrefs.setKeepScreenOn(false)
        readerPrefs.setAutoAdvanceDhikr(true)
        bookmarksRepo.upsertBookmark(
            AyahBookmark(
                id = BookmarksRepository.buildId(42, 2, 255),
                page = 42,
                surahNumber = 2,
                ayahNumber = 255,
                surahNameAr = "البقرة",
                category = BookmarkCategory.MEMORIZATION,
                note = "ملاحظة اختبارية"
            )
        )
        statsRepo.recordPageSettled(epochDay = 20000L)

        val exportedJson = backupManager.exportAllJson()
        val parsed = JSONObject(exportedJson)
        assertEquals(1, parsed.getInt("version"))
        assertTrue(parsed.has("reader"))
        assertTrue(parsed.has("prayer"))
        assertTrue(parsed.has("wird"))
        assertTrue(parsed.has("bookmarks"))
        assertTrue(parsed.has("stats"))
        assertTrue(parsed.has("adhkar"))

        // Mutate state, then restore from backup JSON
        readerPrefs.setUseWesternDigits(false)
        bookmarksRepo.deleteBookmark(BookmarksRepository.buildId(42, 2, 255))

        val importResult = backupManager.importAllJson(exportedJson)
        assertTrue("Expected backup import to succeed", importResult.isSuccess)

        val restoredSettings = readerPrefs.settingsFlow.first()
        assertTrue(restoredSettings.useWesternDigits)
        assertEquals(false, restoredSettings.keepScreenOn)
        assertTrue(restoredSettings.autoAdvanceDhikr)

        val restoredBookmarks = bookmarksRepo.bookmarksFlow.first()
        assertTrue(restoredBookmarks.any { it.surahNumber == 2 && it.ayahNumber == 255 && it.note == "ملاحظة اختبارية" })

        val restoredStats = statsRepo.statsFlow.first()
        assertTrue(restoredStats.totalPagesRead >= 1)
    }

    @Test
    fun `mushafSvgAyahMap parses polygons and finds nearest ayah`() {
        val sampleSvg = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="-6 0 345 550">
              <path class="ayahPolygon" surah="1" ayah="1" d="M 10 40 L 320 40 L 320 75 L 10 75 Z" />
              <path class="ayahPolygon" surah="1" ayah="2" d="M 10 80 L 320 80 L 320 120 L 10 120 Z M 150 122 L 320 122 L 320 150 L 150 150 Z" />
            </svg>
        """.trimIndent()

        val pageMap = MushafSvgAyahMap.parseSvg("hafs_1", sampleSvg)
        assertNotNull(pageMap)
        pageMap!!
        assertEquals(-6f, pageMap.viewBox.minX, 0.01f)
        assertEquals(345f, pageMap.viewBox.width, 0.01f)
        assertEquals(2, pageMap.ayahs.size)
        assertEquals(2, pageMap.rectsForAyah(1, 2).size)

        val hit1 = pageMap.findAyahAt(100f, 55f)
        assertNotNull(hit1)
        assertEquals(1, hit1!!.ayahNumber)

        val hit2 = pageMap.findAyahAt(200f, 135f)
        assertNotNull(hit2)
        assertEquals(2, hit2!!.ayahNumber)

        val miss = pageMap.findAyahAt(200f, 400f)
        assertNull(miss)

        val illuminatedSvg = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="-53.3109 -198.4777 345 550">
              <g transform="translate(10, -20) scale(1, 1)">
                <path class="ayahPolygon" surah="1" ayah="1" d="M 100 0 H 200 V 30 H 0 V 60 H 100 Z" />
                <path class="ayahPolygon" surah="1" ayah="2" d="M 0 0 H 95 V 28 H 0 Z" />
              </g>
            </svg>
        """.trimIndent()
        val illuminatedMap = MushafSvgAyahMap.parseSvg("hafs_1_illum", illuminatedSvg)
        assertNotNull(illuminatedMap)
        illuminatedMap!!
        assertEquals(-53.3109f, illuminatedMap.viewBox.minX, 0.01f)
        assertEquals(-198.4777f, illuminatedMap.viewBox.minY, 0.01f)
        // Translated by (+10, -20): Ayah 2 sits in x=[10..105], y=[-20..8] inside the concave notch of Ayah 1
        assertEquals(2, illuminatedMap.findAyahAt(50f, -5f)?.ayahNumber)
        // Inside Ayah 1 right arm (x=150, y=-5) and lower bar (x=50, y=25)
        assertEquals(1, illuminatedMap.findAyahAt(150f, -5f)?.ayahNumber)
        assertEquals(1, illuminatedMap.findAyahAt(50f, 25f)?.ayahNumber)
    }

    @Test
    fun `offline storages report accurate counts byte sizes and clean deletions`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val mushaf = com.example.data.local.MushafOfflineStorage(context)
        val tafsir = com.example.data.tafsir.TafsirRepository(context)
        val audio = com.example.data.audio.SurahAudioStorage(context)

        mushaf.deleteEdition(com.example.data.local.RiwayahEdition.WARSH)
        tafsir.deleteEdition(com.example.domain.model.TafsirEdition.MUYASSAR)
        audio.deleteReciter("Alafasy_128kbps")

        val svgFile = mushaf.pageFile(com.example.data.local.RiwayahEdition.WARSH, 1)
        svgFile.parentFile?.mkdirs()
        svgFile.writeText("<svg>" + "a".repeat(250) + "</svg>")

        val tafsirFile = tafsir.surahFile(com.example.domain.model.TafsirEdition.MUYASSAR, 1)
        tafsirFile.parentFile?.mkdirs()
        tafsirFile.writeText("[{\"ayah\":1,\"text\":\"الحمد لله رب العالمين\"}]")

        val mp3File = audio.ayahAudioFile("Alafasy_128kbps", 1, 1)
        mp3File.parentFile?.mkdirs()
        mp3File.writeBytes(ByteArray(1024) { 7 })

        assertEquals(1, mushaf.countDownloadedPages(com.example.data.local.RiwayahEdition.WARSH))
        assertTrue(mushaf.getEditionStorageBytes(com.example.data.local.RiwayahEdition.WARSH) >= 250L)

        assertEquals(1, tafsir.countDownloadedSurahs(com.example.domain.model.TafsirEdition.MUYASSAR))
        assertTrue(tafsir.getEditionStorageBytes(com.example.domain.model.TafsirEdition.MUYASSAR) > 30L)

        assertEquals(1, audio.countDownloadedAyahsForReciter("Alafasy_128kbps"))
        assertEquals(1024L, audio.getReciterStorageBytes("Alafasy_128kbps"))

        audio.deleteReciter("Alafasy_128kbps")
        mushaf.deleteEdition(com.example.data.local.RiwayahEdition.WARSH)
        tafsir.deleteEdition(com.example.domain.model.TafsirEdition.MUYASSAR)

        assertEquals(0, audio.countDownloadedAyahsForReciter("Alafasy_128kbps"))
        assertEquals(0L, audio.getReciterStorageBytes("Alafasy_128kbps"))
    }

    @Test
    fun `hijri date offset shifts formatted date and stats tracks adhkar completions`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val statsRepo = ReadingStatsRepository(context)
        val cal = java.util.GregorianCalendar(2026, 2, 1)
        val base = com.example.domain.prayer.PrayerTimesCalculator.formatHijriDateAr(cal, hijriOffsetDays = 0)
        val plusOne = com.example.domain.prayer.PrayerTimesCalculator.formatHijriDateAr(cal, hijriOffsetDays = 1)
        assertTrue(base.isNotBlank() && plusOne.isNotBlank() && base != plusOne)

        statsRepo.recordAdhkarCompleted(epochDay = 20500L)
        val stats = statsRepo.statsFlow.first()
        assertTrue(stats.adhkarTotalCount >= 1)
        assertTrue((stats.recentDailyAdhkar[20500L] ?: 0) >= 1)
    }

    @Test
    fun `test triggerTestPrayerAlert and notification receiver`() = runTest {
        val app = ApplicationProvider.getApplicationContext<QuranApplication>()
        val prayerViewModel = com.example.ui.viewmodel.PrayerViewModel(app)
        prayerViewModel.triggerTestPrayerAlert()
        assertTrue(prayerViewModel.uiState.value.statusMessage != null)
    }
}
