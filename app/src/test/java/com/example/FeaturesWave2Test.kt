package com.example

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MushafOfflineStorage
import com.example.data.local.RiwayahEdition
import com.example.data.wird.WirdReminderScheduler
import com.example.domain.model.AdhkarDownloadStatus
import com.example.domain.model.OfflineInventoryUiState
import com.example.domain.model.RiwayahDownloadStatus
import com.example.domain.model.TafsirDownloadStatus
import com.example.domain.model.TafsirEdition
import com.example.domain.prayer.HijriDayInsight
import com.example.domain.prayer.HijriSunnahCalculator
import com.example.domain.prayer.HijriSunnahCategory
import com.example.domain.prayer.HijriSunnahConfig
import com.example.domain.prayer.PrayerName
import com.example.domain.prayer.PrayerTimeEntry
import com.example.domain.prayer.PresetCities
import com.example.domain.wird.NamedWirdPlanConfig
import com.example.domain.wird.NamedWirdPlanItem
import com.example.domain.wird.WirdPlanSummary
import com.example.domain.wird.WirdPortion
import com.example.domain.wird.WirdScheduleStatus
import com.example.ui.components.OfflineReadinessQuickCard
import com.example.ui.components.OfflineStorageBreakdownCard
import com.example.ui.navigation.AppLaunchDestination
import com.example.ui.screens.prayer.HijriSunnahInsightBanner
import com.example.ui.screens.prayer.NightWatchCard
import com.example.ui.screens.prayer.PrayerHeroCard
import com.example.ui.screens.prayer.PrayerListItemCard
import com.example.ui.screens.prayer.PrayerScheduleSectionHeader
import com.example.ui.screens.prayer.PrayerTodaySunnahRibbon
import com.example.ui.screens.prayer.PrayerTopBar
import com.example.ui.screens.wird.CustomPlanReminderStatusChip
import com.example.ui.theme.QuranWsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = QuranApplication::class)
class FeaturesWave2Test {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `hijriSunnahCalculator finds Friday Kahf MonThu and White Days`() {
        val mondayMorning = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 13, 7, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val config = HijriSunnahConfig(enabled = true)
        val notices = HijriSunnahCalculator.computeNextNotices(config = config, nowMillis = mondayMorning)
        val kahf = notices.firstOrNull { it.category == HijriSunnahCategory.FRIDAY_KAHF }
        assertNotNull(kahf)
        assertTrue(kahf!!.openKahfOnTap)
        assertTrue(notices.any { it.category == HijriSunnahCategory.MONDAY_THURSDAY })
        assertTrue(HijriSunnahCalculator.computeNextNotices(config.copy(enabled = false), mondayMorning).isEmpty())
    }

    @Test
    fun `appLaunchDestination parses all shortcut and notification intents`() {
        assertEquals(AppLaunchDestination.LastReadPage, AppLaunchDestination.fromIntent(Intent(AppLaunchDestination.ACTION_QUICK_LAST_PAGE)))
        assertEquals(AppLaunchDestination.OpenWirdTab(), AppLaunchDestination.fromIntent(Intent(AppLaunchDestination.ACTION_QUICK_WIRD)))
        assertEquals(AppLaunchDestination.AdhkarTab, AppLaunchDestination.fromIntent(Intent(AppLaunchDestination.ACTION_QUICK_ADHKAR)))
        assertEquals(AppLaunchDestination.PrayerTab, AppLaunchDestination.fromIntent(Intent(AppLaunchDestination.ACTION_QUICK_PRAYER)))
        assertEquals(AppLaunchDestination.HijriCalendar, AppLaunchDestination.fromIntent(Intent(AppLaunchDestination.ACTION_OPEN_HIJRI_CALENDAR)))

        val kahfIntent = Intent(AppLaunchDestination.ACTION_OPEN_SURAH).putExtra(AppLaunchDestination.EXTRA_TARGET_SURAH, 18)
        assertEquals(AppLaunchDestination.OpenSurah(18), AppLaunchDestination.fromIntent(kahfIntent))

        val planReaderIntent = Intent(AppLaunchDestination.ACTION_OPEN_CUSTOM_PLAN)
            .putExtra(AppLaunchDestination.EXTRA_CUSTOM_PLAN_ID, "plan_42")
            .putExtra(AppLaunchDestination.EXTRA_OPEN_PLAN_READER, true)
        assertEquals(
            AppLaunchDestination.OpenWirdTab(openAllPlans = false, customPlanId = "plan_42", openReaderForPlan = true),
            AppLaunchDestination.fromIntent(planReaderIntent)
        )
        assertNull(AppLaunchDestination.fromIntent(null))
    }

    @Test
    fun `offlineInventoryUiState computes managed storage totals and readiness`() {
        val incomplete = OfflineInventoryUiState(
            rasterCacheBytes = 4_000L,
            adhanAudioBytes = 8_000L
        )
        assertFalse(incomplete.isOfflineCoreReady(RiwayahEdition.HAFS))
        assertEquals(12_000L, incomplete.totalManagedStorageBytes)

        val ready = OfflineInventoryUiState(
            riwayahStatuses = listOf(RiwayahDownloadStatus(RiwayahEdition.HAFS, downloadedPages = MushafOfflineStorage.TOTAL_PAGES, sizeBytes = 10_000_000L)),
            tafsirStatuses = listOf(TafsirDownloadStatus(TafsirEdition.MUYASSAR, downloadedSurahs = 114, sizeBytes = 2_000_000L)),
            adhkarStatus = AdhkarDownloadStatus(isSynced = true, sizeBytes = 500_000L),
            rasterCacheBytes = 100_000L,
            adhanAudioBytes = 250_000L
        )
        assertTrue(ready.isOfflineCoreReady(RiwayahEdition.HAFS))
        assertEquals(12_850_000L, ready.totalManagedStorageBytes)
        assertTrue(ready.offlineReadinessLabelAr(RiwayahEdition.HAFS).contains("جاهز للعمل الكامل"))
    }

    @Test
    fun `wirdReminderScheduler handles custom plan items cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scheduler = WirdReminderScheduler(context)
        val cfg = NamedWirdPlanConfig(id = "test_p1", title = "حفظ الفاتحة", reminderEnabled = true)
        val portion = WirdPortion(0, 1, "الجزء الأول", 1, "الفاتحة", 1, 1, 2, "البقرة", 141, 21, "بِسْمِ اللَّهِ", 8)
        val summary = WirdPlanSummary(
            allWirds = listOf(portion),
            totalWirds = 1,
            completedCount = 0,
            upcomingCount = 0,
            currentWird = portion,
            previousWirds = emptyList(),
            upcomingWirds = emptyList(),
            isCompleted = false,
            progressFraction = 0f,
            scheduleStatus = WirdScheduleStatus.OnTrack,
            resumePage = 1,
            hasStartedCurrentWird = false
        )
        val item = NamedWirdPlanItem(config = cfg, summary = summary)
        scheduler.syncCustomPlanItems(listOf(item))
        scheduler.syncCustomPlanItems(listOf(item.copy(config = cfg.copy(reminderEnabled = false))))
    }

    @Test
    fun `compose cards for custom plan reminder, offline manager, and hijri sunnah work`() {
        var reminderToggled = false
        var quickPrepareClicked = false
        var clearCacheClicked = false
        var surahPickerClicked = false
        var sunnahToggled = false

        val readyState = OfflineInventoryUiState(
            riwayahStatuses = listOf(RiwayahDownloadStatus(RiwayahEdition.HAFS, downloadedPages = 100, sizeBytes = 1_000_000L)),
            rasterCacheBytes = 512_000L,
            adhanAudioBytes = 256_000L,
            cachedAdhanVoicesCount = 2,
            activeReciterDownloadedSurahs = 5
        )

        composeRule.setContent {
            QuranWsTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    CustomPlanReminderStatusChip(
                        config = NamedWirdPlanConfig(id = "c1", title = "ورد", reminderEnabled = true),
                        isCompleted = false,
                        useWestern = false,
                        onToggleReminder = { reminderToggled = true }
                    )
                    OfflineReadinessQuickCard(
                        hubState = readyState,
                        activeRiwayah = RiwayahEdition.HAFS,
                        onPrepareEssentialOffline = { quickPrepareClicked = true },
                        onClearRasterCache = { clearCacheClicked = true },
                        onOpenSurahAudioPicker = { surahPickerClicked = true }
                    )
                    OfflineStorageBreakdownCard(hubState = readyState)
                    HijriSunnahInsightBanner(
                        hijriOffsetDays = 0,
                        useWestern = false,
                        sunnahConfig = HijriSunnahConfig(enabled = true),
                        onToggleSunnahMaster = { sunnahToggled = it }
                    )
                }
            }
        }

        composeRule.onNodeWithTag("custom_plan_reminder_chip_c1").performScrollTo().performClick()
        assertTrue(reminderToggled)

        composeRule.onNodeWithTag("offline_quick_prepare_button").performScrollTo().performClick()
        composeRule.onNodeWithTag("offline_clear_raster_cache_button").performScrollTo().performClick()
        composeRule.onNodeWithTag("offline_open_surah_audio_button").performScrollTo().performClick()
        assertTrue(quickPrepareClicked && clearCacheClicked && surahPickerClicked)

        composeRule.onNodeWithTag("offline_storage_breakdown_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("تفصيل المساحة المدارة").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("hijri_sheet_sunnah_switch").performScrollTo().performClick()
        assertFalse(sunnahToggled)
    }

    @Test
    fun `redesigned prayer times components render key elements and handle clicks`() {
        var hijriOpened = false
        var qiblaToggled = false
        var cityPickerOpened = false
        var sunnahClicked = false
        var alertToggledPrayer: PrayerName? = null
        val now = System.currentTimeMillis()
        val fajr = PrayerTimeEntry(PrayerName.FAJR, 4, 45, now + 3_600_000L)
        val sunrise = PrayerTimeEntry(PrayerName.SUNRISE, 6, 10, now + 7_200_000L)

        composeRule.setContent {
            QuranWsTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    PrayerTopBar(
                        showQibla = false,
                        onToggleQibla = { qiblaToggled = true },
                        onOpenHijriCalendar = { hijriOpened = true }
                    )
                    PrayerHeroCard(
                        city = PresetCities.defaultCity,
                        hijriDateAr = "١٥ رمضان ١٤٤٧",
                        gregorianDateAr = "٥ مارس ٢٠٢٦",
                        dayOffset = 0,
                        nextPrayer = fajr,
                        countdownText = "٠١:٠٠:٠٠",
                        iqamahStatusText = "الإقامة بعد ١٥ د",
                        intervalProgress = 0.65f,
                        qiblaBearingDegrees = 136.0,
                        use24Hour = false,
                        methodTitleAr = "أم القرى",
                        onOpenCityPicker = { cityPickerOpened = true },
                        onPrevDay = {},
                        onToday = {},
                        onNextDay = {},
                        onToggleQibla = { qiblaToggled = true }
                    )
                    PrayerTodaySunnahRibbon(
                        insight = HijriDayInsight("سنّة اليوم", "الجمعة: يستحب قراءة سورة الكهف", false),
                        onClick = { sunnahClicked = true }
                    )
                    PrayerScheduleSectionHeader()
                    PrayerListItemCard(
                        entry = fajr,
                        isNextPrayer = true,
                        isPassed = false,
                        isAlertEnabled = true,
                        use24Hour = false,
                        onToggleAlert = { alertToggledPrayer = it }
                    )
                    PrayerListItemCard(
                        entry = sunrise,
                        isNextPrayer = false,
                        isPassed = false,
                        isAlertEnabled = false,
                        use24Hour = false,
                        onToggleAlert = {}
                    )
                    NightWatchCard(
                        midnightMillis = now + 18_000_000L,
                        lastThirdMillis = now + 24_000_000L,
                        use24Hour = false
                    )
                }
            }
        }

        composeRule.onNodeWithTag("prayer_hijri_calendar_button").performScrollTo().performClick()
        assertTrue(hijriOpened)
        composeRule.onNodeWithTag("prayer_qibla_toggle_button").performScrollTo().performClick()
        assertTrue(qiblaToggled)
        composeRule.onNodeWithTag("prayer_city_picker_chip").performScrollTo().performClick()
        assertTrue(cityPickerOpened)
        composeRule.onNodeWithTag("prayer_iqamah_pill").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("prayer_today_sunnah_card").performScrollTo().performClick()
        assertTrue(sunnahClicked)
        composeRule.onNodeWithTag("prayer_item_fajr").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("prayer_alert_toggle_fajr").performScrollTo().performClick()
        assertEquals(PrayerName.FAJR, alertToggledPrayer)
        composeRule.onNodeWithTag("prayer_item_sunrise").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("بداية الثلث الأخير").performScrollTo().assertIsDisplayed()
    }
}
