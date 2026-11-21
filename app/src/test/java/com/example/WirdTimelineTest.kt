package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.domain.model.AyahItem
import com.example.domain.model.HizbQuarterInfo
import com.example.domain.model.JuzInfo
import com.example.domain.wird.ALL_WEEK_DAYS_MASK
import com.example.domain.wird.CustomWirdFormatters
import com.example.domain.wird.KhatmahPlanConfig
import com.example.domain.wird.WirdPlanCalculator
import com.example.domain.wird.WirdPlanSummary
import com.example.domain.wird.WirdPortion
import com.example.domain.wird.WirdScheduleStatus
import com.example.domain.wird.WirdTimelineCalculator
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
class WirdTimelineTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun samplePortion(index: Int): WirdPortion {
        val startPage = index * 4 + 1
        return WirdPortion(
            index = index,
            juzNumber = 1,
            juzNameAr = "الجزء الأول",
            startSurahNumber = 2,
            startSurahNameAr = "البقرة",
            startAyahNumber = index * 10 + 1,
            startPage = startPage,
            endSurahNumber = 2,
            endSurahNameAr = "البقرة",
            endAyahNumber = index * 10 + 10,
            endPage = startPage + 3,
            openingAyahText = "آية ${index + 1}",
            quartersCount = 2
        )
    }

    private fun sampleSummary(portions: List<WirdPortion>, completedIndices: Set<Int>): WirdPlanSummary {
        val incomplete = portions.filter { it.index !in completedIndices }
        return WirdPlanSummary(
            allWirds = portions,
            totalWirds = portions.size,
            completedCount = completedIndices.size,
            upcomingCount = (incomplete.size - 1).coerceAtLeast(0),
            currentWird = incomplete.firstOrNull(),
            previousWirds = portions.filter { it.index in completedIndices },
            upcomingWirds = incomplete.drop(1),
            isCompleted = incomplete.isEmpty(),
            progressFraction = completedIndices.size.toFloat() / portions.size.coerceAtLeast(1),
            scheduleStatus = WirdScheduleStatus.Behind(1),
            resumePage = incomplete.firstOrNull()?.startPage ?: 1,
            hasStartedCurrentWird = false
        )
    }

    @Test
    fun buildTimeline_handlesOutOfOrderCompletionAndBuckets() {
        val portions = (0 until 5).map(::samplePortion)
        val today = 20_400L
        val startDay = today - 3L // scheduled days: -3, -2, -1, 0 (today), +1 (upcoming)
        val completedMap = mapOf(
            0 to (today - 3L),
            2 to (today - 1L)
        )
        val summary = sampleSummary(portions, completedMap.keys)

        val timeline = WirdTimelineCalculator.buildTimeline(
            planId = null,
            planTitle = "ختمة التجربة",
            cadenceBadgeAr = "تلاوة • كل يوم",
            summary = summary,
            startEpochDay = startDay,
            explicitEndEpochDay = 0L,
            activeDaysMask = ALL_WEEK_DAYS_MASK,
            completedPortionDays = completedMap,
            portionDayOverrides = emptyMap(),
            todayEpochDay = today
        )

        assertEquals(5, timeline.totalSessions)
        assertEquals(2, timeline.completedSessions)
        assertEquals(40, timeline.progressPercent)
        // Portion 1 (day -2) is overdue
        assertEquals(listOf(1), timeline.overdueSessions.map { it.portion.index })
        // Portion 3 (day 0) is today
        assertEquals(listOf(3), timeline.todaySessions.map { it.portion.index })
        // Portion 4 (day +1) is upcoming
        assertEquals(listOf(4), timeline.upcomingSessions.map { it.portion.index })
        // Completed groups sorted newest first
        assertEquals(listOf(today - 1L, today - 3L), timeline.completedDayGroups.map { it.epochDay })
        assertEquals(listOf(2), timeline.completedDayGroups[0].sessions.map { it.portion.index })
        assertEquals(listOf(0), timeline.completedDayGroups[1].sessions.map { it.portion.index })
    }

    @Test
    fun rescheduleOverdue_and_deferToday_respectActiveWeekDays() {
        val today = 20_400L
        val startDay = today - 3L
        val mask = ALL_WEEK_DAYS_MASK

        val afterReschedule = WirdTimelineCalculator.computeRescheduleOverdueOverrides(
            totalPortions = 4,
            startEpochDay = startDay,
            activeDaysMask = mask,
            completedPortionDays = mapOf(0 to startDay),
            legacyCompletedCount = 1,
            currentOverrides = emptyMap(),
            todayEpochDay = today
        )
        // Incomplete: 1, 2, 3 -> scheduled starting from today (0, +1, +2)
        assertEquals(today, afterReschedule[1])
        assertEquals(today + 1L, afterReschedule[2])
        assertEquals(today + 2L, afterReschedule[3])

        val afterDefer = WirdTimelineCalculator.computeDeferTodayOverrides(
            totalPortions = 4,
            startEpochDay = startDay,
            activeDaysMask = mask,
            completedPortionDays = mapOf(0 to startDay),
            legacyCompletedCount = 1,
            currentOverrides = afterReschedule,
            todayEpochDay = today
        )
        val expectedNext = CustomWirdFormatters.advanceByActiveStudyDays(today + 1L, 1, mask)
        assertEquals(expectedNext, afterDefer[1])
        assertEquals(expectedNext + 1L, afterDefer[2])
    }

    @Test
    fun buildPlan_withCompletedPortionDays_selectsFirstIncomplete() {
        val sampleAyahs = (1..24).map { idx ->
            AyahItem(1, "الفاتحة", idx, "آية $idx", "اية $idx", "۝", idx, 1, false)
        }
        val sampleQuarters = (1..24).map { q ->
            HizbQuarterInfo(q, 1, (q - 1) / 4 + 1, (q - 1) % 4, 1, "الفاتحة", q, q, "ربع $q")
        }
        val sampleAjza = listOf(JuzInfo(1, "الجزء ١", 1, "الفاتحة", 1, 1))
        val cfg = KhatmahPlanConfig(
            isActive = true,
            startJuz = 1,
            quartersPerDay = 8,
            startEpochDay = 20_000L,
            completedPortionDays = mapOf(0 to 20_000L, 2 to 20_001L)
        )
        // Total quarters remaining = 240, but our sample only has 24 quarters; calculator slices up to quarters.size
        val summary = WirdPlanCalculator.buildPlan(cfg, sampleQuarters, sampleAyahs, sampleAjza, 20_002L)!!
        assertEquals(2, summary.completedCount)
        assertEquals(1, summary.currentWird?.index)
        assertEquals(setOf(0, 2), summary.previousWirds.map { it.index }.toSet())
    }

    @Test
    fun timelineScreen_displaysSectionsAndHandlesActions() {
        val today = 20_400L
        val startDay = today - 2L
        val portions = (0 until 4).map(::samplePortion)
        val completedMap = mapOf(0 to startDay)
        val summary = sampleSummary(portions, completedMap.keys)
        val config = KhatmahPlanConfig(
            isActive = true,
            startJuz = 1,
            quartersPerDay = 8,
            startEpochDay = startDay,
            completedPortionDays = completedMap
        )
        val timeline = WirdTimelineCalculator.buildForMainKhatmah(config, summary, today)

        var toggledIndex = -1
        var openedPage = -1
        var rescheduled = false
        var deferred = false

        composeRule.setContent {
            QuranWsTheme {
                WirdScreen(
                    uiState = WirdUiState(
                        isLoading = false,
                        config = config,
                        plan = summary,
                        mainTimeline = timeline,
                        ajza = listOf(JuzInfo(1, "الجزء الأول", 1, "الفاتحة", 1, 1))
                    ),
                    onStartNewKhatmah = { _, _ -> },
                    onCompleteCurrentWird = {},
                    onUndoCompleteWird = {},
                    onSetCompletedCount = {},
                    onOpenPageInReader = { page, _ -> openedPage = page },
                    onClearStatusMessage = {},
                    customPlanActions = CustomWirdPlanActions(
                        onToggleTimelinePortion = { _, idx -> toggledIndex = idx },
                        onRescheduleOverdue = { rescheduled = true },
                        onDeferToday = { deferred = true }
                    )
                )
            }
        }

        // Open timeline from visible top bar action pill
        composeRule.onNodeWithTag("wird_schedule_topbar_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wird_timeline_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("timeline_hero_card").assertIsDisplayed()

        // Reschedule overdue banner visible & clickable
        composeRule.onNodeWithTag("timeline_reschedule_overdue_button").performClick()
        assertTrue(rescheduled)

        // Toggle session 1 checkbox
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("timeline_session_toggle_1"))
        composeRule.onNodeWithTag("timeline_session_toggle_1").performClick()
        assertEquals(1, toggledIndex)

        // Click session 2 (today) row opens reader at session start page
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("timeline_session_row_2"))
        composeRule.onNodeWithTag("timeline_session_row_2").performClick()
        assertEquals(9, openedPage)

        // Defer today banner clickable
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("timeline_defer_today_button"))
        composeRule.onNodeWithTag("timeline_defer_today_button").performClick()
        assertTrue(deferred)

        // Completed group rendered
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("timeline_completed_row_0"))
        composeRule.onNodeWithTag("timeline_completed_row_0").assertIsDisplayed()

        // Settings sheet opens and dismisses
        composeRule.onNodeWithTag("timeline_settings_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wird_plan_settings_sheet").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("إغلاق").performClick()
        composeRule.waitForIdle()

        // Back button returns to main feed
        composeRule.onNodeWithTag("timeline_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wird_timeline_screen").assertDoesNotExist()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasTestTag("wird_view_schedule_text_button"))
        composeRule.onNodeWithTag("wird_view_schedule_text_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wird_timeline_screen").assertIsDisplayed()
    }
}
