package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.audio.AppAudioCoordinator
import com.example.data.audio.AppAudioOwner
import com.example.data.audio.AudioPlaylist
import com.example.data.audio.AudioPlaylistTrack
import com.example.data.audio.MemorizationLoopConfig
import com.example.data.audio.QuranAudioPlayer
import com.example.data.local.BackupManager
import com.example.data.local.ReadingStatsRepository
import com.example.data.local.WirdPreferencesRepository
import com.example.domain.model.AsmaAllahCatalog
import com.example.domain.model.QuranReciters
import com.example.domain.prayer.SunnahRawatibCatalog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import ws.quran.qurantext.Mushaf

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = QuranApplication::class)
class AuditAndComplementaryFeaturesTest {

    private lateinit var app: QuranApplication

    @Before
    fun setUp() {
        AppAudioCoordinator.clearForTest()
        app = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        AppAudioCoordinator.clearForTest()
    }

    @Test
    fun appAudioCoordinator_stopsOnlyOtherRegisteredOwners() {
        var quranStops = 0
        var adhkarStops = 0
        var adhanStops = 0

        AppAudioCoordinator.register(AppAudioOwner.QURAN) { quranStops++ }
        AppAudioCoordinator.register(AppAudioOwner.ADHKAR) { adhkarStops++ }
        AppAudioCoordinator.register(AppAudioOwner.ADHAN_PREVIEW) { adhanStops++ }

        AppAudioCoordinator.claimFocus(AppAudioOwner.ADHKAR)
        assertEquals(1, quranStops)
        assertEquals(0, adhkarStops)
        assertEquals(1, adhanStops)

        AppAudioCoordinator.unregister(AppAudioOwner.ADHAN_PREVIEW)
        AppAudioCoordinator.claimFocus(AppAudioOwner.QURAN)
        assertEquals(1, quranStops)
        assertEquals(1, adhkarStops)
        assertEquals(1, adhanStops)
    }

    @Test
    fun quranAudioPlayer_restoresDefaultReciterAndClampsMemorizationBounds() {
        val surahs = Mushaf.hafs().surahs.associate { it.number to (it.nameAr to it.ayahCount) }
        val player = QuranAudioPlayer(app) { surah -> surahs[surah] ?: ("الفاتحة" to 7) }
        try {
            val defaultId = QuranReciters.all[0].id
            val overrideId = QuranReciters.all[1].id
            player.setDefaultReciter(defaultId)
            assertEquals(defaultId, player.state.value.reciter.id)

            val playlistWithOverride = AudioPlaylist(
                id = "p_override",
                titleAr = "مخصصة",
                preferredReciterId = overrideId,
                tracks = listOf(AudioPlaylistTrack("t1", surahNumber = 1, startAyah = 1, endAyah = 7))
            )
            player.playPlaylist(playlistWithOverride)
            assertEquals(overrideId, player.state.value.reciter.id)

            val playlistNoOverride = AudioPlaylist(
                id = "p_default",
                titleAr = "افتراضية",
                preferredReciterId = null,
                tracks = listOf(AudioPlaylistTrack("t2", surahNumber = 112, startAyah = 1, endAyah = 4))
            )
            player.playPlaylist(playlistNoOverride)
            assertEquals(defaultId, player.state.value.reciter.id)

            player.playPlaylist(playlistWithOverride)
            assertEquals(overrideId, player.state.value.reciter.id)
            player.playAyah(1, 2)
            assertEquals(defaultId, player.state.value.reciter.id)
            assertEquals(null, player.state.value.playlistSession)

            // Memorization loop clamps start/end bounds to surah ayah count (Al-Ikhlas has 4 ayahs)
            player.startMemorizationLoop(
                MemorizationLoopConfig(
                    enabled = true,
                    surahNumber = 112,
                    startAyah = 4,
                    endAyah = 99,
                    ayahRepeatCount = 25,
                    rangeRepeatCount = -2,
                    pauseSeconds = 50
                )
            )
            val loop = player.state.value.memorizationLoop
            assertTrue(loop.enabled)
            assertEquals(4, loop.startAyah)
            assertEquals(4, loop.endAyah)
            assertEquals(10, loop.ayahRepeatCount)
            assertEquals(0, loop.rangeRepeatCount)
            assertEquals(10, loop.pauseSeconds)

            player.togglePlayPause()
            assertFalse(player.state.value.isWaitingPause)
        } finally {
            player.release()
        }
    }

    @Test
    fun readingStatsAndBackup_roundTripDailyGoalAndRawatibWithDayRollover() = runBlocking {
        val repo = ReadingStatsRepository(app)
        val backup = BackupManager(app)
        val today = WirdPreferencesRepository.currentLocalEpochDay()

        // Yesterday's entries are automatically hidden on today and cleared on next toggle
        repo.toggleRawatibItem("old_item", epochDay = today - 1L)
        assertTrue(repo.statsFlow.first().rawatibCompletedIdsToday.isEmpty())

        repo.setDailyGoalPages(10)
        repo.toggleRawatibItem("fajr_before", epochDay = today)
        repo.toggleRawatibItem("dhuhr_before", epochDay = today)
        repo.toggleRawatibItem("duha", epochDay = today)

        val beforeExport = repo.statsFlow.first()
        assertEquals(10, beforeExport.dailyGoalPages)
        assertEquals(setOf("fajr_before", "dhuhr_before", "duha"), beforeExport.rawatibCompletedIdsToday)

        val exportedJson = backup.exportAllJson()
        assertTrue(exportedJson.contains("\"dailyGoalPages\": 10"))
        assertTrue(exportedJson.contains("fajr_before"))
        assertFalse(exportedJson.contains("old_item"))

        // Change values then restore from backup JSON
        repo.setDailyGoalPages(2)
        repo.toggleRawatibItem("fajr_before", epochDay = today)
        val restored = backup.importAllJson(exportedJson)
        assertTrue(restored.isSuccess)

        val restoredStats = repo.statsFlow.first()
        assertEquals(10, restoredStats.dailyGoalPages)
        assertEquals(setOf("fajr_before", "dhuhr_before", "duha"), restoredStats.rawatibCompletedIdsToday)
    }

    @Test
    fun sunnahRawatibCatalog_sumsConfirmedTwelveRakatAccurately() {
        val allIds = SunnahRawatibCatalog.dailyItems.map { it.id }.toSet()
        assertEquals(12, SunnahRawatibCatalog.completedConfirmedRakatCount(allIds))
        assertEquals(
            6,
            SunnahRawatibCatalog.completedConfirmedRakatCount(setOf("fajr_before", "dhuhr_before", "duha", "witr"))
        )
        assertEquals(0, SunnahRawatibCatalog.completedConfirmedRakatCount(setOf("duha", "witr")))
    }

    @Test
    fun asmaAllahCatalog_hasAll99DistinctNumberedNamesAndMeanings() {
        val entries = AsmaAllahCatalog.all
        assertEquals(99, entries.size)
        assertEquals((1..99).toList(), entries.map { it.number })
        assertEquals(99, entries.map { it.nameAr }.toSet().size)
        entries.forEach { entry ->
            assertTrue("Blank name at #${entry.number}", entry.nameAr.isNotBlank())
            assertTrue("Blank meaning at #${entry.number}", entry.meaningAr.isNotBlank())
        }
    }
}
