package com.example

import com.example.data.audio.PlaybackNotificationButton
import com.example.data.audio.PlaybackNotificationConfig
import com.example.data.local.AdhanSoundMode
import com.example.data.local.PrayerSettings
import com.example.data.local.ReaderSettings
import com.example.data.prayer.PrayerAlarmScheduler
import com.example.domain.model.AyahItem
import com.example.domain.model.HizbQuarterInfo
import com.example.domain.model.JuzInfo
import com.example.domain.prayer.AdhanVoices
import com.example.domain.wird.KhatmahPlanConfig
import com.example.domain.wird.NamedWirdPlanConfig
import com.example.domain.wird.WirdPlanCalculator
import com.example.ui.screens.reader.shouldShowWirdBannerInReader
import com.example.ui.theme.MushafPaperSepia
import com.example.ui.theme.OnSepiaPrimary
import com.example.ui.theme.OnSepiaSecondary
import com.example.ui.theme.OnSepiaTertiary
import com.example.ui.theme.SepiaBackground
import com.example.ui.theme.SepiaInk
import com.example.ui.theme.SepiaPrimary
import com.example.ui.theme.SepiaSecondary
import com.example.ui.theme.SepiaSurface
import com.example.ui.theme.SepiaSurfaceHigh
import com.example.ui.theme.SepiaSurfaceHighest
import com.example.ui.theme.SepiaSurfaceLow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReliabilityAndMediaNotifTest {

    @Test
    fun adhanVoicesCatalog_isCompleteWithValidHttpsUrls() {
        val voices = AdhanVoices.all
        assertTrue("Expected at least 5 real Adhan voices", voices.size >= 5)
        val uniqueIds = voices.map { it.id }.toSet()
        assertEquals(voices.size, uniqueIds.size)
        voices.forEach { voice ->
            assertTrue(voice.nameAr.isNotBlank())
            assertTrue(voice.muezzinAr.isNotBlank())
            assertTrue(voice.audioUrl.startsWith("https://"))
            assertTrue(voice.audioUrl.endsWith(".mp3"))
        }
        assertEquals(AdhanVoices.defaultVoice, AdhanVoices.byId("unknown_id"))
        assertEquals("madinah_nabawi", AdhanVoices.byId("madinah_nabawi").id)
        val short13s = AdhanVoices.byId(AdhanVoices.SHORT_ADHAN_13S_ID)
        assertEquals(AdhanVoices.SHORT_ADHAN_13S_ID, short13s.id)
        assertTrue(short13s.isShort)
        assertEquals(13_000L, short13s.maxDurationMs)
    }

    @Test
    fun playbackNotificationConfig_togglesAndPreservesEmptyVsDefault() {
        val defaultCfg = PlaybackNotificationConfig()
        assertTrue(defaultCfg.enabled)
        assertTrue(defaultCfg.showWhenPaused)
        assertTrue(defaultCfg.hasButton(PlaybackNotificationButton.PREV_NEXT))
        assertTrue(defaultCfg.hasButton(PlaybackNotificationButton.REPEAT_AYAH))
        assertTrue(defaultCfg.hasButton(PlaybackNotificationButton.STOP_CLOSE))
        assertFalse(defaultCfg.hasButton(PlaybackNotificationButton.SEEK_10S))

        val withSeek = defaultCfg.toggledButton(PlaybackNotificationButton.SEEK_10S)
        assertTrue(withSeek.hasButton(PlaybackNotificationButton.SEEK_10S))
        val withoutSeek = withSeek.toggledButton(PlaybackNotificationButton.SEEK_10S)
        assertFalse(withoutSeek.hasButton(PlaybackNotificationButton.SEEK_10S))

        assertEquals(PlaybackNotificationButton.defaultButtons, PlaybackNotificationButton.fromIds(null))
        assertTrue(PlaybackNotificationButton.fromIds(emptySet()).isEmpty())
        assertEquals(
            setOf(PlaybackNotificationButton.CYCLE_SPEED, PlaybackNotificationButton.SEEK_10S),
            PlaybackNotificationButton.fromIds(setOf("speed", "seek_10s", "unknown"))
        )
    }

    @Test
    fun prayerSettingsAndNextTriggerSelection_handleTodayTomorrowAndPreAdhan() {
        val settings = PrayerSettings()
        assertEquals(AdhanSoundMode.FULL_ADHAN, settings.adhanSoundMode)
        assertEquals(AdhanVoices.defaultVoice.id, settings.selectedAdhanVoiceId)
        val readerSettings = ReaderSettings()
        assertTrue(readerSettings.playbackNotificationConfig.enabled)
        assertFalse(readerSettings.hideWirdBannerOutsideWird)
        assertTrue(readerSettings.copy(hideWirdBannerOutsideWird = true).hideWirdBannerOutsideWird)

        val now = 1_700_000_000_000L
        val todayFuture = now + 600_000L
        val tomorrowFuture = now + 86_400_000L

        assertEquals(
            todayFuture,
            PrayerAlarmScheduler.nextTriggerMillis(todayFuture, tomorrowFuture, now, 0L)
        )
        assertEquals(
            todayFuture - 300_000L,
            PrayerAlarmScheduler.nextTriggerMillis(todayFuture, tomorrowFuture, now, 300_000L)
        )
        // If pre-adhan offset pushes today's alert into the past, roll forward to tomorrow's pre-adhan
        assertEquals(
            tomorrowFuture - 900_000L,
            PrayerAlarmScheduler.nextTriggerMillis(todayFuture, tomorrowFuture, now, 900_000L)
        )
        assertNull(PrayerAlarmScheduler.nextTriggerMillis(now - 60_000L, now - 10_000L, now, 0L))
    }

    @Test
    fun wirdBannerVisibility_respectsHideOutsideWirdSetting() {
        assertFalse(shouldShowWirdBannerInReader(openedFromWird = false, hideWirdBannerOutsideWird = true))
        assertTrue(shouldShowWirdBannerInReader(openedFromWird = false, hideWirdBannerOutsideWird = false))
        assertTrue(shouldShowWirdBannerInReader(openedFromWird = true, hideWirdBannerOutsideWird = true))
        assertTrue(shouldShowWirdBannerInReader(openedFromWird = true, hideWirdBannerOutsideWird = false))
    }

    @Test
    fun sepiaPalette_containsZeroWhiteOrNearWhiteColors() {
        val sepiaColors = listOf(
            SepiaPrimary, OnSepiaPrimary, SepiaSecondary, OnSepiaSecondary,
            OnSepiaTertiary, SepiaBackground, SepiaInk, SepiaSurfaceLow,
            SepiaSurface, SepiaSurfaceHigh, SepiaSurfaceHighest, MushafPaperSepia
        )
        sepiaColors.forEach { color ->
            val isNearWhite = color.red > 0.93f && color.green > 0.90f && color.blue > 0.85f
            assertFalse("Sepia color $color should not be white or near-white", isNearWhite)
            assertNotEquals(1f, color.red * color.green * color.blue)
        }
    }

    @Test
    fun namedWirdPlans_calculateIndependentlyFromPrimaryWird() {
        val primaryConfig = KhatmahPlanConfig(
            isActive = true,
            startJuz = 1,
            quartersPerDay = 8,
            completedWirdsCount = 2,
            startEpochDay = 19_000L
        )
        val namedPlanA = NamedWirdPlanConfig(
            id = "plan_a",
            title = "مراجعة الحفظ",
            startJuz = 28,
            quartersPerDay = 4,
            completedWirdsCount = 1,
            startEpochDay = 19_000L,
            iconKey = "hifz"
        )
        val namedPlanB = NamedWirdPlanConfig(
            id = "plan_b",
            title = "ختمة تدبّر",
            startJuz = 1,
            quartersPerDay = 2,
            completedWirdsCount = 0,
            startEpochDay = 19_000L,
            iconKey = "tadabbur"
        )

        val ajza = (1..30).map { j ->
            JuzInfo(
                number = j,
                nameAr = "الجزء $j",
                startSurahNumber = 1,
                startSurahNameAr = "الفاتحة",
                startAyahNumber = 1,
                startPage = ((j - 1) * 20) + 1
            )
        }
        val quarters = (0 until 240).map { idx ->
            val juz = (idx / 8) + 1
            val hizb = (idx / 4) + 1
            val ayahNum = (idx % 240) + 1
            HizbQuarterInfo(
                rubNumber = idx + 1,
                juzNumber = juz,
                hizbNumber = hizb,
                quarterInHizb = idx % 4,
                surahNumber = 2,
                surahNameAr = "البقرة",
                ayahNumber = ayahNum,
                pageNumber = ((idx * 604) / 240) + 1,
                openingText = "آية رقم $ayahNum"
            )
        }
        val ayahs = (1..240).map { n ->
            AyahItem(
                surahNumber = 2,
                surahNameAr = "البقرة",
                ayahNumber = n,
                text = "آية $n",
                imlaiText = "اية $n",
                marker = "$n",
                pageNumber = ((n * 604) / 240) + 1,
                juzNumber = ((n - 1) / 8) + 1,
                hasSajdah = false
            )
        }

        val summaryPrimary = requireNotNull(WirdPlanCalculator.buildPlan(primaryConfig, quarters, ayahs, ajza, 19_002L))
        val summaryA = requireNotNull(WirdPlanCalculator.buildPlan(namedPlanA.toPlanConfig(), quarters, ayahs, ajza, 19_001L))
        val summaryB = requireNotNull(WirdPlanCalculator.buildPlan(namedPlanB.toPlanConfig(), quarters, ayahs, ajza, 19_000L))

        assertEquals(2, summaryPrimary.completedCount)
        assertEquals(1, summaryA.completedCount)
        assertEquals(0, summaryB.completedCount)
        assertEquals(30, summaryPrimary.totalWirds)
        assertEquals(6, summaryA.totalWirds)
        assertEquals(120, summaryB.totalWirds)
        assertNotEquals(summaryPrimary.totalWirds, summaryA.totalWirds)
        assertNotEquals(summaryA.totalWirds, summaryB.totalWirds)
    }
}
