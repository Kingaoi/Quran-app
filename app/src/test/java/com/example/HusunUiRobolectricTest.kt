package com.example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.example.domain.model.AyahItem
import com.example.domain.model.JuzInfo
import com.example.domain.wird.HusunActions
import com.example.domain.wird.HusunCalculator
import com.example.domain.wird.HusunConfig
import com.example.domain.wird.HusunNewRate
import com.example.domain.wird.KhatmahPlanConfig
import com.example.domain.wird.WirdDirection
import com.example.ui.navigation.HomeTab
import com.example.ui.navigation.QuranBottomNavBar
import com.example.ui.screens.CustomWirdPlanActions
import com.example.ui.screens.WirdScreen
import com.example.ui.theme.QuranWsTheme
import com.example.ui.viewmodel.WirdUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = QuranApplication::class)
class HusunUiRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sampleAyahs = (1..45).map { p ->
        AyahItem(
            surahNumber = if (p == 1) 1 else 2,
            surahNameAr = if (p == 1) "الفاتحة" else "البقرة",
            ayahNumber = 1,
            text = "الحمد لله",
            imlaiText = "الحمد لله",
            marker = "١",
            pageNumber = p,
            juzNumber = ((p - 1) / 20) + 1,
            hasSajdah = false
        )
    }

    private val sampleAjza = (1..30).map { j ->
        JuzInfo(j, "الجزء $j", 1, "الفاتحة", 1, ((j - 1) * 20) + 1)
    }

    @Test
    fun `all plans screen supports configuring opening interacting and completing husun khamsah`() {
        var config by mutableStateOf(HusunConfig(isActive = false))
        var openedPage = -1

        composeRule.setContent {
            val husunState = HusunCalculator.buildDailyPlan(config, sampleAyahs, 20000L)
            QuranWsTheme {
                WirdScreen(
                    uiState = WirdUiState(
                        isLoading = false,
                        config = KhatmahPlanConfig(isActive = false),
                        husunPlan = husunState,
                        ajza = sampleAjza
                    ),
                    onStartNewKhatmah = { _, _ -> },
                    onCompleteCurrentWird = {},
                    onUndoCompleteWird = {},
                    onSetCompletedCount = {},
                    onOpenPageInReader = { _, _ -> },
                    onClearStatusMessage = {},
                    customPlanActions = CustomWirdPlanActions(
                        husun = HusunActions(
                            onConfigure = { dir, startPages, rate, cont, far ->
                                config = HusunConfig(
                                    isActive = true,
                                    direction = dir,
                                    startingMemorizedPages = startPages,
                                    dailyNewRate = rate,
                                    continuousPagesPerDay = cont,
                                    farReviewDivisions = far,
                                    lastActiveEpochDay = 20000L
                                )
                            },
                            onToggleFortress = { type ->
                                config = config.copy(
                                    lastActiveEpochDay = 20000L,
                                    todayFortressMask = config.effectiveMask(20000L) xor type.bitMask
                                )
                            },
                            onIncrementRepeat = {
                                config = config.copy(
                                    lastActiveEpochDay = 20000L,
                                    todayNewRepeatCount = config.effectiveRepeatCount(20000L) + 1
                                )
                            },
                            onCompleteDay = {
                                config = config.copy(
                                    completedNewUnits = config.completedNewUnits + config.dailyNewRate.halfPageUnits,
                                    continuousCursorDay = config.continuousCursorDay + 1,
                                    todayFortressMask = 0,
                                    todayNewRepeatCount = 0
                                )
                            },
                            onReset = { config = HusunConfig(isActive = false) },
                            onOpenReaderPage = { openedPage = it }
                        )
                    )
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("wird_all_plans_topbar_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("husun_feature_card").assertIsDisplayed()
        composeRule.onNodeWithTag("husun_start_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("husun_setup_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("husun_dir_${WirdDirection.FATIHA_TO_NAS.key}").performClick()
        composeRule.onNodeWithTag("husun_rate_${HusunNewRate.ONE_PAGE.key}").performClick()
        composeRule.onNodeWithTag("husun_save_setup_button").performClick()
        composeRule.waitForIdle()

        assertTrue(config.isActive)
        composeRule.onNodeWithTag("husun_open_daily_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("husun_daily_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("husun_fortress_card_0").assertIsDisplayed()
        composeRule.onNodeWithTag("husun_daily_list")
            .performScrollToNode(hasTestTag("husun_fortress_card_2"))
        composeRule.onNodeWithTag("husun_fortress_card_2").assertIsDisplayed()
        composeRule.onNodeWithTag("husun_fortress_check_2").performClick()
        composeRule.onNodeWithTag("husun_repeat_increment_button").performClick()
        composeRule.waitForIdle()

        assertEquals(1, config.todayNewRepeatCount)
        assertTrue(config.todayFortressMask != 0)

        composeRule.onNodeWithTag("husun_advance_day_button").performClick()
        composeRule.waitForIdle()
        assertEquals(2, config.completedNewUnits)
        assertEquals(0, config.todayFortressMask)

        composeRule.onNodeWithTag("husun_daily_list")
            .performScrollToNode(hasTestTag("husun_fortress_card_2"))
        composeRule.onNodeWithTag("husun_fortress_open_2").performClick()
        composeRule.waitForIdle()
        assertEquals(2, openedPage)
    }

    @Test
    fun `bottom navigation bar switches selected tab cleanly`() {
        var selected by mutableStateOf(HomeTab.PRAYER_TIMES)
        composeRule.setContent {
            QuranWsTheme {
                QuranBottomNavBar(selectedTab = selected, onSelectTab = { selected = it })
            }
        }
        composeRule.waitForIdle()
        HomeTab.entries.forEach { tab ->
            composeRule.onNodeWithTag("nav_tab_${tab.route}").performClick()
            composeRule.waitForIdle()
            assertEquals(tab, selected)
        }
        composeRule.onNodeWithText("نظام الحصون الخمسة").assertDoesNotExist()
    }
}
