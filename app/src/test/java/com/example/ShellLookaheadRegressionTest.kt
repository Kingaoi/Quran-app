package com.example

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = QuranApplication::class)
class ShellLookaheadRegressionTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun mainActivity_switchesAllTabsAndOpensReaderAndAdhkarWithoutLookaheadCrash() {
        composeRule.waitUntilAtLeastOneExists(hasTestTag("nav_tab_prayer_times"), timeoutMillis = 15_000L)
        composeRule.waitForIdle()

        val tabTags = listOf(
            "nav_tab_wird",
            "nav_tab_adhkar",
            "nav_tab_index",
            "nav_tab_more",
            "nav_tab_prayer_times",
            "nav_tab_adhkar",
            "nav_tab_wird"
        )
        for (tag in tabTags) {
            composeRule.onNodeWithTag(tag).performClick()
            composeRule.mainClock.advanceTimeBy(300)
            composeRule.waitForIdle()
        }

        // Open More category and return
        composeRule.onNodeWithTag("nav_tab_more").performClick()
        composeRule.mainClock.advanceTimeBy(300)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_lazy_list").performScrollToNode(hasTestTag("more_category_card_appearance"))
        composeRule.onNodeWithTag("more_category_card_appearance").performClick()
        composeRule.mainClock.advanceTimeBy(300)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("more_category_back_button").performClick()
        composeRule.mainClock.advanceTimeBy(300)
        composeRule.waitForIdle()

        // Open Index and jump into MushafReaderScreen, then pop back
        composeRule.onNodeWithTag("nav_tab_index").performClick()
        composeRule.mainClock.advanceTimeBy(300)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("hero_continue_reading_card").performClick()
        composeRule.mainClock.advanceTimeBy(400)
        composeRule.waitForIdle()
        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.mainClock.advanceTimeBy(400)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_tab_prayer_times").performClick()
        composeRule.mainClock.advanceTimeBy(300)
        composeRule.waitForIdle()
    }
}
