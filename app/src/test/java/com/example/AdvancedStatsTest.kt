package com.example

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ReadingStats
import com.example.data.local.ReadingStatsRepository
import com.example.data.local.WirdPreferencesRepository
import com.example.domain.model.StatsAnalyticsCalculator
import com.example.domain.prayer.SunnahRawatibCatalog
import com.example.ui.screens.wird.WirdStatsBottomSheet
import com.example.ui.theme.QuranWsTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
class AdvancedStatsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `statsAnalyticsCalculator computes weekly momentum, heatmap, score, and badges`() {
        val today = 20_500L
        val dailyPages = buildMap {
            for (d in 0L..6L) put(today - d, 6)
            for (d in 7L..13L) put(today - d, 3)
        }
        val dailyAdhkar = mapOf(today to 10, today - 1L to 4)
        val dailyTasbih = mapOf(today to 100, today - 1L to 33)
        val rawatib12 = SunnahRawatibCatalog.dailyItems
            .filter { it.isRawatibConfirmed }
            .map { it.id }
            .toSet()

        val stats = ReadingStats(
            currentStreakDays = 14,
            longestStreakDays = 14,
            lastReadEpochDay = today,
            totalPagesRead = 150,
            completedKhatmahsCount = 1,
            recentDailyPages = dailyPages,
            tasbihTotalCount = 620L,
            tasbihTodayCount = 100,
            tasbihTodayEpochDay = today,
            adhkarTotalCount = 60,
            adhkarTodayCount = 10,
            recentDailyAdhkar = dailyAdhkar,
            recentDailyTasbih = dailyTasbih,
            dailyGoalPages = 4,
            rawatibCompletedIdsToday = rawatib12,
            rawatibTodayEpochDay = today
        )

        val snap = StatsAnalyticsCalculator.compute(stats, today)

        assertEquals(6, snap.todayPages)
        assertEquals(42, snap.weeklyPages)
        assertEquals(21, snap.previousWeeklyPages)
        assertEquals(100, snap.weeklyTrendPercent)
        assertEquals(63, snap.monthlyPages)
        assertEquals(100, snap.overallDailyScorePercent)
        assertEquals((604f / 6f).toInt(), snap.projectedDaysPerKhatmah)
        assertEquals(84, snap.heatmap84.size)
        assertEquals(14, snap.activeDaysLast84)
        assertEquals(4, snap.heatmap84.last().intensityLevel)
        assertEquals(0, snap.heatmap84.first().intensityLevel)
        assertNotNull(snap.bestDay30d)
        assertEquals(6, snap.bestDay30d?.pages)

        val badgeMap = snap.badges.associateBy { it.id }
        assertTrue(badgeMap.getValue("streak_7").isUnlocked)
        assertTrue(badgeMap.getValue("pages_100").isUnlocked)
        assertTrue(badgeMap.getValue("khatmah_1").isUnlocked)
        assertTrue(badgeMap.getValue("tasbih_500").isUnlocked)
        assertTrue(badgeMap.getValue("rawatib_12").isUnlocked)
        assertFalse(badgeMap.getValue("streak_30").isUnlocked)
    }

    @Test
    fun `readingStatsRepository persists daily tasbih map and roundtrips json backup`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<QuranApplication>()
        val repo = ReadingStatsRepository(app)
        val today = WirdPreferencesRepository.currentLocalEpochDay()

        repo.setDailyGoalPages(10)
        repo.incrementTasbih(today)
        repo.incrementTasbih(today)
        repo.recordPageSettled(today)
        repo.recordAdhkarCompleted(today)

        val beforeBackup = repo.statsFlow.first()
        assertEquals(10, beforeBackup.dailyGoalPages)
        assertTrue((beforeBackup.recentDailyTasbih[today] ?: 0) >= 2)
        assertTrue((beforeBackup.recentDailyPages[today] ?: 0) >= 1)
        assertTrue((beforeBackup.recentDailyAdhkar[today] ?: 0) >= 1)

        val exported = repo.exportJson()
        assertTrue(exported.has("dailyTasbih"))
        assertEquals(10, exported.optInt("dailyGoalPages"))

        repo.setDailyGoalPages(2)
        repo.importJson(exported)
        val restored = repo.statsFlow.first()
        assertEquals(10, restored.dailyGoalPages)
        assertEquals(beforeBackup.recentDailyTasbih[today], restored.recentDailyTasbih[today])
    }

    @Test
    fun `wirdStatsBottomSheet renders advanced charts, heatmap, badges, scope tabs, and goal chips`() {
        val today = WirdPreferencesRepository.currentLocalEpochDay()
        var selectedGoal = -1
        var dismissed = false
        val stats = ReadingStats(
            currentStreakDays = 9,
            longestStreakDays = 12,
            lastReadEpochDay = today,
            totalPagesRead = 210,
            completedKhatmahsCount = 1,
            recentDailyPages = mapOf(today to 6, today - 1L to 4, today - 2L to 8),
            tasbihTotalCount = 540L,
            tasbihTodayCount = 66,
            tasbihTodayEpochDay = today,
            adhkarTotalCount = 35,
            adhkarTodayCount = 5,
            recentDailyAdhkar = mapOf(today to 5, today - 1L to 3),
            recentDailyTasbih = mapOf(today to 66, today - 1L to 33),
            dailyGoalPages = 4
        )

        composeRule.setContent {
            QuranWsTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    WirdStatsBottomSheet(
                        stats = stats,
                        onSetDailyGoalPages = { selectedGoal = it },
                        onDismiss = { dismissed = true }
                    )
                }
            }
        }

        composeRule.onNodeWithTag("wird_stats_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("wird_stats_share_button").assertIsDisplayed()
        composeRule.onNodeWithTag("wird_stats_card").assertIsDisplayed()

        composeRule.onNodeWithTag("stats_hero_rings_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("stats_trend_chart_card").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("stats_range_30d_chip").performScrollTo().performClick()
        composeRule.onNodeWithTag("stats_metric_tasbih").performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("daily_reading_goal_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("daily_goal_chip_10").performScrollTo().performClick()
        assertEquals(10, selectedGoal)
        composeRule.onNodeWithTag("daily_goal_plus").performScrollTo().performClick()
        assertEquals(5, selectedGoal)

        composeRule.onNodeWithTag("wird_pace_insights_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("stats_heatmap_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("stats_milestones_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("stats_badge_streak_7").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("stats_scope_tab_habits").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("stats_heatmap_card").assertDoesNotExist()
        composeRule.onNodeWithTag("stats_habits_summary_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("stats_milestones_card").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("stats_scope_tab_quran").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("stats_milestones_card").assertDoesNotExist()
        composeRule.onNodeWithTag("wird_pace_insights_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("stats_heatmap_card").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("wird_stats_close_button").performClick()
        assertTrue(dismissed)
    }
}
