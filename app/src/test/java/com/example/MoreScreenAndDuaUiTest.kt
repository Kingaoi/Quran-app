package com.example

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.LayoutDirection
import com.example.domain.model.AppSourcesCatalog
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.more.MoreSettingsActions
import com.example.ui.screens.wird.KhatmahDuaBottomSheet
import com.example.ui.theme.QuranWsTheme
import com.example.ui.viewmodel.AdhkarUiState
import com.example.ui.viewmodel.OfflineDownloadState
import com.example.ui.viewmodel.PrayerUiState
import com.example.ui.viewmodel.QuranUiState
import com.example.ui.viewmodel.WirdUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = QuranApplication::class)
class MoreScreenAndDuaUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `khatmahDuaBottomSheet displays ornamental stanzas, font controls, and share buttons`() {
        var copiedMsg: String? = null
        var dismissed = false

        composeRule.setContent {
            QuranWsTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    KhatmahDuaBottomSheet(
                        onCopiedMessage = { copiedMsg = it },
                        onDismiss = { dismissed = true }
                    )
                }
            }
        }

        composeRule.onNodeWithTag("khatmah_dua_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("khatmah_dua_font_minus").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("khatmah_dua_font_plus").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("khatmah_dua_stanza_0").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("khatmah_dua_copy").assertIsDisplayed().performClick()
        assertTrue(copiedMsg?.isNotBlank() == true)
        composeRule.onNodeWithTag("khatmah_dua_share").assertIsDisplayed()
        composeRule.onNodeWithTag("khatmah_dua_share_card").assertIsDisplayed()
        composeRule.onNodeWithTag("khatmah_dua_close").performClick()
        assertTrue(dismissed)
    }

    @Test
    fun `moreScreen divides settings into hub categories, focused detail sections, and live search`() {
        val actions = noopMoreActions()

        composeRule.setContent {
            QuranWsTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MoreScreen(
                        uiState = QuranUiState(),
                        offlineState = OfflineDownloadState(),
                        adhkarState = AdhkarUiState(),
                        prayerState = PrayerUiState(),
                        wirdState = WirdUiState(),
                        actions = actions
                    )
                }
            }
        }

        composeRule.onNodeWithTag("more_screen_title").assertIsDisplayed()
        composeRule.onNodeWithTag("more_lazy_list").performScrollToNode(hasTestTag("more_category_card_appearance"))
        composeRule.onNodeWithTag("more_category_card_appearance").assertIsDisplayed()
        composeRule.onNodeWithTag("more_lazy_list").performScrollToNode(hasTestTag("more_category_card_mushaf_reader"))
        composeRule.onNodeWithTag("more_category_card_mushaf_reader").assertIsDisplayed()
        composeRule.onNodeWithTag("more_lazy_list").performScrollToNode(hasTestTag("more_category_card_prayer"))
        composeRule.onNodeWithTag("more_category_card_prayer").assertIsDisplayed()
        composeRule.onNodeWithTag("more_western_digits_switch").assertDoesNotExist()

        composeRule.onNodeWithTag("more_lazy_list").performScrollToNode(hasTestTag("more_category_card_appearance"))
        composeRule.onNodeWithTag("more_category_card_appearance").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_category_back_button").assertIsDisplayed()
        composeRule.onNodeWithTag("more_western_digits_switch").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("more_prayer_city_row").assertDoesNotExist()

        composeRule.onNodeWithTag("more_category_pill_prayer").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_prayer_city_row").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("more_category_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_settings_search_input").performTextInput("تصدير")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_export_backup_button").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `moreScreen opens detailed sources section, filters groups, and excludes rights or AI markings`() {
        composeRule.setContent {
            QuranWsTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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

        composeRule.onNodeWithTag("more_lazy_list")
            .performScrollToNode(hasTestTag("more_category_card_sources"))
        composeRule.onNodeWithTag("more_category_card_sources").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("more_sources_overview_card").assertIsDisplayed()
        composeRule.onNodeWithTag("more_lazy_list")
            .performScrollToNode(hasTestTag("source_item_ws_quran_text"))
        composeRule.onNodeWithTag("source_item_ws_quran_text").assertIsDisplayed()

        composeRule.onNodeWithTag("more_lazy_list")
            .performScrollToNode(hasTestTag("sources_filter_tafsir_sciences"))
        composeRule.onNodeWithTag("sources_filter_tafsir_sciences").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("source_group_tafsir_sciences").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("source_group_mushaf_text").assertDoesNotExist()

        composeRule.onNodeWithTag("sources_filter_all").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_lazy_list")
            .performScrollToNode(hasTestTag("source_item_android_open_libraries"))
        composeRule.onNodeWithTag("source_item_android_open_libraries").assertIsDisplayed()

        composeRule.onNodeWithText("حقوق", substring = true).assertDoesNotExist()
        composeRule.onNodeWithText("ذكاء اصطناعي", substring = true).assertDoesNotExist()
        composeRule.onNodeWithText("Remix", substring = true, ignoreCase = true).assertDoesNotExist()
        composeRule.onNodeWithText("Gemini", substring = true, ignoreCase = true).assertDoesNotExist()

        val allCatalogText = AppSourcesCatalog.groups.flatMap { g ->
            listOf(g.titleAr, g.subtitleAr) + g.items.flatMap { listOf(it.titleAr, it.roleAr, it.referenceText, it.detailsAr) }
        }.joinToString(" ")
        assertFalse(allCatalogText.contains("حقوق"))
        assertFalse(allCatalogText.contains("ذكاء"))
        assertFalse(allCatalogText.contains("Gemini", ignoreCase = true))
        assertTrue(AppSourcesCatalog.totalSourcesCount >= 12)
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
}
