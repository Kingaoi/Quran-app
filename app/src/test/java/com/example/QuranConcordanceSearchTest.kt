package com.example

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.QuranRepository
import com.example.domain.model.SurahInfo
import com.example.domain.quran.QuranSearchMode
import com.example.ui.screens.IndexScreen
import com.example.ui.theme.QuranWsTheme
import com.example.ui.viewmodel.QuranUiState
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
@Config(sdk = [36])
class QuranConcordanceSearchTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `root concordance search computes exact statistical invariants over full Quran`() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repo = QuranRepository(context)
            repo.ensureLoaded()

            val report = repo.searchConcordance("رحم", QuranSearchMode.ROOT)

            assertEquals("رحم", report.resolvedRoot)
            assertTrue("Expected > 200 occurrences for root رحم, got ${report.totalOccurrences}", report.totalOccurrences > 200)
            assertTrue("Expected > 150 matching ayahs, got ${report.totalMatchingAyahs}", report.totalMatchingAyahs > 150)
            assertTrue("Expected > 40 surahs, got ${report.totalMatchingSurahs}", report.totalMatchingSurahs > 40)

            assertEquals(report.totalOccurrences, report.makkiOccurrences + report.madaniOccurrences)
            assertEquals(report.totalMatchingAyahs, report.makkiAyahs + report.madaniAyahs)
            assertEquals(30, report.juzCounts.size)
            assertEquals(report.totalOccurrences, report.juzCounts.sum())
            assertEquals(report.totalOccurrences, report.topSurahs.sumOf { it.occurrenceCount })
            assertEquals(report.totalMatchingAyahs, report.topSurahs.sumOf { it.ayahCount })
            assertNotNull(report.firstOccurrence)
            assertNotNull(report.lastOccurrence)

            val derivedReport = repo.searchConcordance("الرحمن", QuranSearchMode.ROOT)
            assertEquals("رحم", derivedReport.resolvedRoot)
            assertEquals(report.totalOccurrences, derivedReport.totalOccurrences)
        }
    }

    @Test
    fun `meaning search matches Quranic concepts and key ayahs with coherent stats`() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repo = QuranRepository(context)
            repo.ensureLoaded()

            val report = repo.searchConcordance("السكينة والقلق", QuranSearchMode.MEANING)
            assertTrue(report.matchedConcepts.isNotEmpty())
            assertEquals("sakinah", report.matchedConcepts.first().id)
            assertTrue(report.totalMatchingAyahs > 5)
            assertEquals(report.totalOccurrences, report.makkiOccurrences + report.madaniOccurrences)
            assertEquals(report.totalOccurrences, report.juzCounts.sum())

            val parentsReport = repo.searchConcordance("بر الوالدين", QuranSearchMode.MEANING)
            assertTrue(parentsReport.matchedConcepts.any { it.id == "parents" })
            assertTrue(parentsReport.hits.any { it.surahNumber == 17 && it.ayahNumber == 23 })
        }
    }

    @Test
    fun `word search counts multi-word phrase accurately across whole Quran`() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repo = QuranRepository(context)
            repo.ensureLoaded()

            val phraseReport = repo.searchConcordance("الحمد لله", QuranSearchMode.WORD)
            assertTrue(phraseReport.totalOccurrences >= 20)
            assertEquals(phraseReport.totalOccurrences, phraseReport.juzCounts.sum())
            assertFalse(phraseReport.hits.isEmpty())
        }
    }

    @Test
    fun `index screen displays 3-mode selector, stats card, and interactive filters`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = QuranRepository(context)
        runBlocking { repo.ensureLoaded() }

        var openedAyah: Triple<Int, Int, Int>? = null
        val surahs: List<SurahInfo> = repo.getSurahs()

        composeRule.setContent {
            QuranWsTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    IndexScreen(
                        uiState = QuranUiState(isLoading = false, surahs = surahs, topicGroups = repo.getTopicGroups()),
                        onOpenPage = {},
                        onOpenAyah = { p, s, a -> openedAyah = Triple(p, s, a) },
                        onOpenSurah = {},
                        onSearchQuran = { repo.searchAyahs(it) },
                        onSearchConcordance = { q, m -> repo.searchConcordance(q, m) }
                    )
                }
            }
        }

        // Open smart search from Topics tab
        composeRule.onNodeWithTag("index_tab_4").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("topics_smart_search_button").performClick()
        composeRule.waitForIdle()

        // Switch to Root mode and tap quick root suggestion "رحم"
        composeRule.onNodeWithTag("search_mode_root").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("search_discovery_card").assertIsDisplayed()
        composeRule.onNodeWithTag("search_suggestion_chip_root_0").performClick()
        composeRule.waitForIdle()

        // Verify statistical concordance card & KPIs & Juz chart are rendered
        composeRule.onNodeWithTag("concordance_stats_card").assertIsDisplayed()
        composeRule.onNodeWithTag("concordance_kpi_occurrences").assertIsDisplayed()
        composeRule.onNodeWithTag("concordance_juz_chart").performScrollTo().assertIsDisplayed()

        // Scroll to first matching Ayah (Al-Fatiha 1:1) and tap it
        composeRule.onNodeWithTag("index_search_results_list")
            .performScrollToNode(hasTestTag("concordance_hit_1_1"))
        composeRule.onNodeWithTag("concordance_hit_1_1").performClick()
        composeRule.waitForIdle()
        assertEquals(Triple(1, 1, 1), openedAyah)

        // Switch to Meaning mode & search "الرزق"
        composeRule.onNodeWithTag("search_mode_meaning").performClick()
        composeRule.onNodeWithTag("surah_filter_input").performTextReplacement("الرزق")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("concordance_stats_card").assertIsDisplayed()
    }
}
