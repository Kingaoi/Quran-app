package com.example

import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.domain.model.DhikrCategory
import com.example.domain.model.SurahInfo
import com.example.ui.navigation.LocalAnimatedVisibilityScope
import com.example.ui.navigation.QuranMotionSpecs
import com.example.ui.navigation.QuranSharedTransitionHost
import com.example.ui.screens.IndexScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.WirdScreen
import com.example.ui.screens.adhkar.AdhkarCategoryBrowseScreen
import com.example.ui.screens.adhkar.AdhkarDetailScreen
import com.example.ui.screens.more.MoreSettingsActions
import com.example.ui.theme.QuranWsTheme
import com.example.ui.viewmodel.AdhkarUiState
import com.example.ui.viewmodel.OfflineDownloadState
import com.example.ui.viewmodel.PrayerUiState
import com.example.ui.viewmodel.QuranUiState
import com.example.ui.viewmodel.WirdUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = QuranApplication::class)
class PredictiveBackAndMotionTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `moreScreen supports predictive back gesture cancel rebound and full back commit`() {
        composeRule.setContent {
            QuranWsTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    QuranSharedTransitionHost {
                        MoreScreen(
                            uiState = QuranUiState(),
                            offlineState = OfflineDownloadState(),
                            adhkarState = AdhkarUiState(),
                            prayerState = PrayerUiState(),
                            wirdState = WirdUiState(),
                            actions = noopMoreActions()
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithTag("more_lazy_list").performScrollToNode(hasTestTag("more_category_card_appearance"))
        composeRule.onNodeWithTag("more_category_card_appearance").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_category_back_button").assertIsDisplayed()

        composeRule.runOnUiThread {
            val dispatcher = composeRule.activity.onBackPressedDispatcher
            dispatcher.dispatchOnBackStarted(BackEventCompat(0f, 0f, 0f, BackEventCompat.EDGE_RIGHT))
            dispatcher.dispatchOnBackProgressed(BackEventCompat(0.5f, 0f, 0.55f, BackEventCompat.EDGE_RIGHT))
            dispatcher.dispatchOnBackCancelled()
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_category_back_button").assertIsDisplayed()

        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_screen_title").assertIsDisplayed()
    }

    @Test
    fun `wirdScreen transitions to AllCustomPlansScreen inside SharedTransitionLayout and returns on back`() {
        composeRule.setContent {
            QuranWsTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    QuranSharedTransitionHost {
                        WirdScreen(
                            uiState = WirdUiState(),
                            onStartNewKhatmah = { _, _ -> },
                            onCompleteCurrentWird = {},
                            onUndoCompleteWird = {},
                            onSetCompletedCount = {},
                            onOpenPageInReader = { _, _ -> },
                            onClearStatusMessage = {}
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithTag("wird_all_plans_topbar_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("all_plans_screen").assertIsDisplayed()

        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wird_all_plans_topbar_button").assertIsDisplayed()
    }

    @Test
    fun `adhkarSharedTransitionNavHost navigates from browse to detail and pops back cleanly`() {
        composeRule.setContent {
            QuranWsTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val navController = rememberNavController()
                    QuranSharedTransitionHost {
                        NavHost(
                            navController = navController,
                            startDestination = "browse",
                            enterTransition = QuranMotionSpecs.navEnterTransition(),
                            exitTransition = QuranMotionSpecs.navExitTransition(),
                            popEnterTransition = QuranMotionSpecs.navPopEnterTransition(),
                            popExitTransition = QuranMotionSpecs.navPopExitTransition()
                        ) {
                            composable("browse") {
                                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
                                    AdhkarCategoryBrowseScreen(
                                        category = DhikrCategory.MOSQUE,
                                        uiState = AdhkarUiState(),
                                        onIncrementDhikr = { true },
                                        onResetCategory = {},
                                        onOpenReader = { _, _ -> navController.navigate("detail") },
                                        onBack = {}
                                    )
                                }
                            }
                            composable("detail") {
                                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
                                    AdhkarDetailScreen(
                                        category = DhikrCategory.MOSQUE,
                                        uiState = AdhkarUiState(),
                                        chapterTitle = "دعاء دخول المسجد",
                                        onIncrementDhikr = { true },
                                        onResetDhikr = {},
                                        onResetCategory = {},
                                        onResetItems = {},
                                        onAdjustFontSize = {},
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithTag("adhkar_browse_list").performScrollToNode(hasTestTag("adhkar_open_chapter_1"))
        composeRule.onNodeWithTag("adhkar_open_chapter_1").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("adhkar_detail_reset_button").assertIsDisplayed()

        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("adhkar_browse_screen").assertIsDisplayed()
    }

    @Test
    fun `indexScreen clears active search on predictive back press`() {
        val sampleSurahs = listOf(
            SurahInfo(1, "الفاتحة", "Al-Fatiha", 7, "مكية", true, true, 1, 1),
            SurahInfo(2, "البقرة", "Al-Baqarah", 286, "مدنية", false, true, 2, 49)
        )
        composeRule.setContent {
            QuranWsTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    IndexScreen(
                        uiState = QuranUiState(surahs = sampleSurahs),
                        onOpenPage = {},
                        onOpenSurah = {},
                        onSearchQuran = { emptyList() }
                    )
                }
            }
        }

        composeRule.onNodeWithTag("index_search_toggle_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("surah_filter_input").performTextInput("الفاتحة")
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("الفاتحة", substring = true)[0].assertIsDisplayed()

        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("surah_filter_input").assertDoesNotExist()
    }

    private fun noopMoreActions(): MoreSettingsActions = MoreSettingsActions(
        onSelectAppTheme = {}, onSetUseWesternDigits = {}, onSetKeepScreenOn = {},
        onSetReaderDimmerAlpha = {}, onSetTwoPageSpreadOnTablet = {}, onSetAutoAdvanceDhikr = {},
        onSetHapticFeedbackEnabled = {}, onSelectAyahMarkerColor = {}, onSelectAyahMarkerColorTarget = {},
        onSelectAyahHighlightColor = {}, onSelectAyahHighlightStyle = {}, onSetAyahHighlightAlpha = {},
        onToggleSurahBanner = {}, onSelectRiwayah = {}, onSelectQuranFont = {},
        onQuranFontSizeChange = {}, onResetQuranFontSettings = {}, onAdhkarFontSizeChange = {},
        onResetAdhkarFontSize = {}, onSelectReciter = {}, onSetPlaybackNotifEnabled = {},
        onTogglePlaybackNotifButton = {}, onSetPlaybackNotifShowWhenPaused = {},
        onStartFullDownload = {}, onCancelFullDownload = {}, onDeleteFullDownload = {},
        onSelectTafsirEdition = {}, onDownloadTafsir = {}, onCancelTafsir = {}, onDeleteTafsir = {},
        onUpdateMorningAdhkarReminder = { _, _, _ -> }, onUpdateEveningAdhkarReminder = { _, _, _ -> },
        onSelectCity = {}, onDetectGpsLocation = {}, onSelectCalculationMethod = {},
        onSelectAsrMethod = {}, onSetUse24Hour = {}, onSetPreAdhanMinutes = {},
        onSetAdhanSoundMode = {}, onSelectAdhanVoice = {}, onDownloadAdhanVoice = {},
        onDeleteAdhanVoice = {}, onTogglePreviewAdhanVoice = {}, onStopAdhanPreview = {},
        onTestPrayerAlert = {}, onTogglePrayerAlert = {}, onSetHijriOffsetDays = {},
        onUpdateHijriSunnah = {}, onClearPrayerStatus = {},
        onUpdateWirdReminder = { _, _, _ -> }, onSetWirdPureReadingMode = {},
        onSetHideWirdBannerOutsideWird = {}, onStartNewKhatmah = { _, _ -> }, onClearWirdStatus = {}
    )

    @Test
    fun `homeTabsContainer inside SharedTransitionLayout warms and switches tabs without lookahead crash`() {
        val selectedTab = androidx.compose.runtime.mutableStateOf(com.example.ui.navigation.HomeTab.PRAYER_TIMES)
        composeRule.setContent {
            QuranWsTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    QuranSharedTransitionHost {
                        val navController = rememberNavController()
                        androidx.navigation.compose.NavHost(
                            navController = navController,
                            startDestination = "home"
                        ) {
                            composable("home") {
                                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
                                    com.example.ui.navigation.HomeTabsContainer(
                                        selectedTab = selectedTab.value,
                                        uiState = QuranUiState(),
                                        offlineState = OfflineDownloadState(),
                                        wirdState = WirdUiState(),
                                        adhkarState = AdhkarUiState(),
                                        prayerState = PrayerUiState(),
                                        statsState = com.example.data.local.ReadingStats(),
                                        bookmarksState = emptyList(),
                                        tafsirOfflineState = com.example.domain.model.TafsirOfflineState(),
                                        offlineHubState = com.example.domain.model.OfflineInventoryUiState(),
                                        downloadHubActions = com.example.domain.model.DownloadHubActions(
                                            onRefresh = {}, onToggleSelection = {}, onSelectRecommended = {},
                                            onSelectAllIncomplete = {}, onClearSelection = {},
                                            onStartConcurrentDownload = {}, onCancelAllDownloads = {},
                                            onCancelSingleDownload = {}, onDeleteItem = {}, onClearStatusMessage = {}
                                        ),
                                        moreActions = noopMoreActions(),
                                        getAyahsForPage = { emptyList() },
                                        onOpenPage = {},
                                        onOpenWirdPage = { _, _ -> },
                                        onOpenAyah = { _, _, _ -> },
                                        onOpenSurah = {},
                                        onSearchQuran = { emptyList() },
                                        onDeleteAyahBookmark = {},
                                        onTogglePageBookmark = {},
                                        onStartNewKhatmah = { _, _ -> },
                                        onCompleteCurrentWird = {},
                                        onUndoCompleteWird = {},
                                        onSetWirdCompletedCount = {},
                                        onClearWirdStatus = {},
                                        onOpenAdhkarCategory = {},
                                        onRefreshAdhkarSource = {},
                                        onClearAdhkarSyncStatus = {},
                                        onAdhkarSearchQueryChange = {},
                                        onToggleAdhkarFavoritesOnly = {},
                                        onToggleAdhkarFavorite = {},
                                        onAddCustomDua = { _, _, _, _ -> },
                                        onDeleteCustomDua = {},
                                        onTogglePlayDhikrAudio = {},
                                        onIncrementTasbih = {},
                                        onAddCustomTasbihPhrase = {},
                                        onRemoveCustomTasbihPhrase = {},
                                        onShiftPrayerDay = {},
                                        onSelectCity = {},
                                        onDetectGpsLocation = {},
                                        onTogglePrayerAlert = {},
                                        onClearPrayerStatus = {}
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        composeRule.mainClock.advanceTimeBy(1500)
        composeRule.waitForIdle()
        for (tab in listOf(
            com.example.ui.navigation.HomeTab.INDEX,
            com.example.ui.navigation.HomeTab.ADHKAR,
            com.example.ui.navigation.HomeTab.WIRD,
            com.example.ui.navigation.HomeTab.MORE,
            com.example.ui.navigation.HomeTab.PRAYER_TIMES
        )) {
            composeRule.runOnUiThread { selectedTab.value = tab }
            composeRule.mainClock.advanceTimeBy(300)
            composeRule.waitForIdle()
        }
    }
}
