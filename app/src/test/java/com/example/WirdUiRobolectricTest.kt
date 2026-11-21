package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.example.domain.model.JuzInfo
import com.example.domain.model.SurahInfo
import com.example.domain.wird.KhatmahPlanConfig
import com.example.domain.wird.NamedWirdPlanConfig
import com.example.domain.wird.NamedWirdPlanItem
import com.example.domain.wird.WirdDirection
import com.example.domain.wird.WirdGoalType
import com.example.domain.wird.WirdPlanAudience
import com.example.domain.wird.WirdPlanSummary
import com.example.domain.wird.WirdPlanType
import com.example.domain.wird.WirdPortion
import com.example.domain.wird.WirdScheduleStatus
import com.example.domain.wird.WirdSessionTimingMode
import com.example.ui.screens.WirdScreen
import com.example.ui.screens.wird.AddCustomWirdPlanSheet
import com.example.ui.theme.QuranWsTheme
import com.example.ui.viewmodel.WirdUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = QuranApplication::class)
class WirdUiRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sampleAjza = (1..30).map { j ->
        JuzInfo(
            number = j,
            nameAr = "الجزء $j",
            startSurahNumber = 1,
            startSurahNameAr = "الفاتحة",
            startAyahNumber = 1,
            startPage = ((j - 1) * 20) + 1
        )
    }

    private val sampleSurahs = listOf(
        SurahInfo(1, "الفاتحة", "Al-Fatiha", 7, "مكية", true, true, 1, 1),
        SurahInfo(2, "البقرة", "Al-Baqarah", 286, "مدنية", false, true, 2, 49),
        SurahInfo(114, "الناس", "An-Nas", 6, "مكية", true, true, 604, 604)
    )

    private fun sampleWird(index: Int = 0) = WirdPortion(
        index = index,
        juzNumber = 1,
        juzNameAr = "الجزء الأول",
        startSurahNumber = 1,
        startSurahNameAr = "الفاتحة",
        startAyahNumber = 1,
        startPage = 1,
        endSurahNumber = 2,
        endSurahNameAr = "البقرة",
        endAyahNumber = 141,
        endPage = 21,
        openingAyahText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        quartersCount = 8
    )

    @Test
    fun `active khatmah hides new khatmah button and completed khatmah shows it`() {
        val activeSummary = WirdPlanSummary(
            allWirds = listOf(sampleWird(0), sampleWird(1)),
            totalWirds = 2,
            completedCount = 1,
            upcomingCount = 1,
            currentWird = sampleWird(1),
            previousWirds = listOf(sampleWird(0)),
            upcomingWirds = emptyList(),
            isCompleted = false,
            progressFraction = 0.5f,
            scheduleStatus = WirdScheduleStatus.OnTrack,
            resumePage = 1,
            hasStartedCurrentWird = false
        )

        composeRule.setContent {
            QuranWsTheme {
                WirdScreen(
                    uiState = WirdUiState(
                        isLoading = false,
                        config = KhatmahPlanConfig(isActive = true, startJuz = 1, quartersPerDay = 8),
                        plan = activeSummary,
                        ajza = sampleAjza
                    ),
                    onStartNewKhatmah = { _, _ -> },
                    onCompleteCurrentWird = {},
                    onUndoCompleteWird = {},
                    onSetCompletedCount = {},
                    onOpenPageInReader = { _, _ -> },
                    onClearStatusMessage = {}
                )
            }
        }

        composeRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("wird_progress_card"))
        composeRule.onNodeWithTag("wird_progress_card").assertIsDisplayed()
        composeRule.onNodeWithTag("wird_new_khatmah_text_button").assertDoesNotExist()
        composeRule.onNodeWithTag("wird_completed_new_khatmah_button").assertDoesNotExist()
    }

    @Test
    fun `completed khatmah displays start new khatmah button`() {
        val completedSummary = WirdPlanSummary(
            allWirds = listOf(sampleWird(0)),
            totalWirds = 1,
            completedCount = 1,
            upcomingCount = 0,
            currentWird = null,
            previousWirds = listOf(sampleWird(0)),
            upcomingWirds = emptyList(),
            isCompleted = true,
            progressFraction = 1f,
            scheduleStatus = WirdScheduleStatus.OnTrack,
            resumePage = 1,
            hasStartedCurrentWird = false
        )

        composeRule.setContent {
            QuranWsTheme {
                WirdScreen(
                    uiState = WirdUiState(
                        isLoading = false,
                        config = KhatmahPlanConfig(isActive = true, startJuz = 1, quartersPerDay = 8, completedWirdsCount = 1),
                        plan = completedSummary,
                        ajza = sampleAjza
                    ),
                    onStartNewKhatmah = { _, _ -> },
                    onCompleteCurrentWird = {},
                    onUndoCompleteWird = {},
                    onSetCompletedCount = {},
                    onOpenPageInReader = { _, _ -> },
                    onClearStatusMessage = {}
                )
            }
        }

        composeRule.onNodeWithTag("wird_completed_card").assertIsDisplayed()
        composeRule.onNodeWithTag("wird_completed_new_khatmah_button").assertIsDisplayed()
        composeRule.onNodeWithTag("wird_new_khatmah_text_button").assertDoesNotExist()
    }

    @Test
    fun `addCustomWirdPlanSheet honors all four step updates validation and state preservation`() {
        var createdPlan: NamedWirdPlanConfig? = null

        composeRule.setContent {
            QuranWsTheme {
                AddCustomWirdPlanSheet(
                    surahs = sampleSurahs,
                    allAyahs = emptyList(),
                    onConfirmAdd = { createdPlan = it },
                    onDismiss = {}
                )
            }
        }
        composeRule.waitForIdle()

        // Title "خطة جديدة" + Step 1 dropdown, audience, follow-up
        composeRule.onNodeWithText("خطة جديدة").assertIsDisplayed()
        composeRule.onNodeWithTag("wizard_close_button").assertIsDisplayed()
        composeRule.onNodeWithTag("wizard_next_button").assertIsNotEnabled()
        composeRule.onNodeWithTag("wizard_title_input").performScrollTo().performTextInput("حفظ سورة البقرة")
        composeRule.onNodeWithTag("wizard_plan_type_dropdown").performScrollTo().performClick()
        composeRule.onNodeWithTag("wizard_type_hifz_review").performClick()
        composeRule.onNodeWithTag("wizard_audience_for_other").performScrollTo().performClick()
        composeRule.onNodeWithTag("wizard_followup_yes").performScrollTo().performClick()
        composeRule.onNodeWithTag("wizard_next_button").assertIsEnabled().performClick()
        composeRule.waitForIdle()

        // Step 2: Section header is "نطاق الخطة" for hifz_review; no periodic switch; go back to test MEMORIZATION & RECITATION
        composeRule.onNodeWithText("نطاق الخطة").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("wizard_periodic_wird_switch").assertDoesNotExist()
        composeRule.onNodeWithTag("wizard_bottom_back_button").performClick()
        composeRule.waitForIdle()

        // Pick RECITATION to verify "وِرد دوري" appears, then return to MEMORIZATION_AND_REVIEW
        composeRule.onNodeWithTag("wizard_plan_type_dropdown").performScrollTo().performClick()
        composeRule.onNodeWithTag("wizard_type_recitation").performClick()
        composeRule.onNodeWithTag("wizard_next_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wizard_periodic_wird_switch").performScrollTo().assertIsDisplayed()

        // Switch back to Step 1 and pick MEMORIZATION_AND_REVIEW; verify audience stayed FOR_OTHER
        composeRule.onNodeWithTag("wizard_bottom_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wizard_plan_type_dropdown").performScrollTo().performClick()
        composeRule.onNodeWithTag("wizard_type_hifz_review").performClick()
        composeRule.onNodeWithTag("wizard_next_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("wizard_dir_nas_to_fatiha").performScrollTo().performClick()
        composeRule.onNodeWithTag("wizard_from_surah").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("wizard_to_surah").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("wizard_next_button").performClick()
        composeRule.waitForIdle()

        // Step 3: Check dynamic fields for MEMORIZATION_AND_REVIEW, days dropdown, duration/date_range disabled Next + reasons
        composeRule.onNodeWithTag("wizard_days_dropdown").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("مقدار الحفظ اليومي").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("مقدار المراجعة اليومي").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("wizard_unit_dropdown").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("لم يُحدَّد").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("wizard_goal_duration").performScrollTo().performClick()
        composeRule.onNodeWithTag("wizard_duration_value_text").performScrollTo().assertTextEquals("٣٠ يوماً").assertIsDisplayed()
        composeRule.onNodeWithText("يُحسَب من التاريخ").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("wizard_next_button").assertIsNotEnabled()
        composeRule.onNodeWithTag("wizard_disabled_reason").assertIsDisplayed()

        composeRule.onNodeWithTag("wizard_goal_date_range").performScrollTo().performClick()
        composeRule.onNodeWithTag("wizard_next_button").assertIsNotEnabled()
        composeRule.onNodeWithTag("wizard_disabled_reason").assertIsDisplayed()

        composeRule.onNodeWithTag("wizard_goal_amount").performScrollTo().performClick()
        composeRule.onNodeWithTag("wizard_next_button").assertIsEnabled().performClick()
        composeRule.waitForIdle()

        // Step 4: Single session unspecified -> 7:00 AM copy; change sessions via dropdown to 2 -> scheduled -> lead -> summary
        composeRule.onNodeWithText("سنُذكّرك في هذا الوقت يوم الجلسة (٧:٠٠ ص)").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("wizard_sessions_dropdown").performScrollTo().performClick()
        composeRule.onNodeWithTag("wizard_sessions_2").performClick()
        composeRule.onNodeWithTag("wizard_timing_scheduled").performScrollTo().performClick()
        composeRule.onNodeWithTag("wizard_session_card_0").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("wizard_session_card_1").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("wizard_lead_15").performScrollTo().performClick()
        composeRule.onNodeWithTag("wizard_reminder_switch").performScrollTo().performClick()
        composeRule.onNodeWithTag("wizard_bottom_summary").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("wizard_create_plan_button").performClick()
        composeRule.waitForIdle()

        val plan = createdPlan
        assertNotNull(plan)
        assertEquals("حفظ سورة البقرة", plan!!.title)
        assertEquals(WirdPlanType.MEMORIZATION_AND_REVIEW, plan.planType)
        assertEquals(WirdPlanAudience.FOR_OTHER, plan.audience)
        assertTrue(plan.allowFollowUp)
        assertFalse(plan.isPeriodicWird)
        assertEquals(WirdDirection.NAS_TO_FATIHA, plan.direction)
        assertEquals(WirdGoalType.AMOUNT, plan.goalType)
        assertEquals(2, plan.sessionsPerDay)
        assertEquals(WirdSessionTimingMode.SCHEDULED, plan.sessionTimingMode)
        assertEquals(15, plan.reminderLeadMinutes)
        assertTrue(plan.reminderEnabled)
    }

    @Test
    fun `custom plans are hidden from wird feed and shown when tapping top bar all plans button`() {
        val mainPlan = WirdPlanSummary(
            allWirds = listOf(sampleWird(0)), totalWirds = 1, completedCount = 0, upcomingCount = 0,
            currentWird = sampleWird(0), previousWirds = emptyList(), upcomingWirds = emptyList(),
            isCompleted = false, progressFraction = 0f, scheduleStatus = WirdScheduleStatus.OnTrack,
            resumePage = 1, hasStartedCurrentWird = false
        )
        val customItem = NamedWirdPlanItem(
            config = NamedWirdPlanConfig(id = "p1", title = "حفظ البقرة", isV2Plan = true, planType = WirdPlanType.MEMORIZATION),
            summary = mainPlan
        )
        composeRule.setContent {
            QuranWsTheme {
                WirdScreen(
                    uiState = WirdUiState(
                        isLoading = false,
                        config = KhatmahPlanConfig(isActive = true, startJuz = 1, quartersPerDay = 8),
                        plan = mainPlan,
                        customPlans = listOf(customItem),
                        ajza = sampleAjza
                    ),
                    onStartNewKhatmah = { _, _ -> }, onCompleteCurrentWird = {}, onUndoCompleteWird = {},
                    onSetCompletedCount = {}, onOpenPageInReader = { _, _ -> }, onClearStatusMessage = {}
                )
            }
        }
        composeRule.onNodeWithTag("wird_all_plans_topbar_button").assertIsDisplayed()
        composeRule.onNodeWithTag("custom_plan_card_p1").assertDoesNotExist()
        composeRule.onNodeWithTag("wird_all_plans_topbar_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("all_plans_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("custom_plan_card_p1").assertIsDisplayed()
        composeRule.onNodeWithTag("wird_add_plan_button").assertIsDisplayed()
        composeRule.onNodeWithTag("all_plans_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("all_plans_screen").assertDoesNotExist()
    }
}
