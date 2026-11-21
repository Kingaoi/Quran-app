package com.example

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.adhkar.HisnChapterEntry
import com.example.data.adhkar.HisnMuslimParser
import com.example.domain.model.DhikrCategory
import com.example.ui.screens.adhkar.AdhkarCategoryBrowseScreen
import com.example.ui.screens.adhkar.AdhkarDetailScreen
import com.example.ui.theme.QuranWsTheme
import com.example.ui.viewmodel.AdhkarUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = QuranApplication::class)
class AdhkarSubChapterBrowseTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `subChaptersForCategory groups distinct topics and itemsForScope isolates single sub-chapter`() {
        val state = AdhkarUiState()

        val mosqueChapters = state.subChaptersForCategory(DhikrCategory.MOSQUE)
        assertEquals(3, mosqueChapters.size)
        assertEquals(
            listOf("الذهاب إلى المسجد", "دعاء دخول المسجد", "دعاء الخروج من المسجد"),
            mosqueChapters.map { it.chapterTitle }
        )

        val scoped = state.itemsForScope(DhikrCategory.MOSQUE, "دعاء دخول المسجد")
        assertEquals(1, scoped.size)
        assertEquals("mosque_entering", scoped.first().id)

        val travelChapters = state.subChaptersForCategory(DhikrCategory.TRAVEL)
        assertEquals(3, travelChapters.size)

        val morningChapters = state.subChaptersForCategory(DhikrCategory.MORNING)
        assertEquals(1, morningChapters.size)
        assertEquals(DhikrCategory.MORNING.titleAr, morningChapters.first().chapterTitle)
    }

    @Test
    fun `hisnMuslimParser preserves chapterTitle through parse and json cache roundtrip`() {
        val chapter = HisnChapterEntry(id = 19, title = "دعاء دخول المسجد", textUrl = "https://www.hisnmuslim.com/api/ar/19.json")
        val json = """
            {
              "دعاء دخول المسجد": [
                { "ID": 55, "ARABIC_TEXT": "اللهم افتح لي أبواب رحمتك", "REPEAT": 1 }
              ]
            }
        """.trimIndent()
        val parsed = HisnMuslimParser.parseChapterItems(json, chapter, DhikrCategory.MOSQUE)
        assertEquals(1, parsed.size)
        assertEquals("دعاء دخول المسجد", parsed.first().chapterTitle)

        val encoded = HisnMuslimParser.encodeCache(999L, mapOf(DhikrCategory.MOSQUE to parsed))
        val decoded = HisnMuslimParser.decodeCache(encoded)
        assertEquals("دعاء دخول المسجد", decoded?.itemsByCategory?.get(DhikrCategory.MOSQUE)?.first()?.chapterTitle)
    }

    @Test
    fun `adhkarCategoryBrowseScreen lists sub-chapters, allows inline expand & count, and opens scoped reader`() {
        var openedChapter = ""
        var openedIndex = -1
        var incrementedId = ""

        composeRule.setContent {
            QuranWsTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AdhkarCategoryBrowseScreen(
                        category = DhikrCategory.MOSQUE,
                        uiState = AdhkarUiState(),
                        onIncrementDhikr = { item -> incrementedId = item.id; true },
                        onResetCategory = {},
                        onOpenReader = { chapter, idx -> openedChapter = chapter; openedIndex = idx },
                        onBack = {}
                    )
                }
            }
        }

        composeRule.onNodeWithTag("adhkar_browse_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("adhkar_browse_mode_chapters").assertIsDisplayed()
        val listNode = composeRule.onNodeWithTag("adhkar_browse_list")
        listNode.performScrollToNode(hasTestTag("adhkar_subchapter_0"))
        composeRule.onNodeWithTag("adhkar_subchapter_0").assertIsDisplayed()
        listNode.performScrollToNode(hasTestTag("adhkar_subchapter_1"))
        composeRule.onNodeWithTag("adhkar_subchapter_1").assertIsDisplayed()
        listNode.performScrollToNode(hasTestTag("adhkar_subchapter_2"))
        composeRule.onNodeWithTag("adhkar_subchapter_2").assertIsDisplayed()

        listNode.performScrollToNode(hasTestTag("adhkar_expand_chapter_1"))
        composeRule.onNodeWithTag("adhkar_expand_chapter_1").performClick()
        composeRule.waitForIdle()
        listNode.performScrollToNode(hasTestTag("adhkar_browse_item_mosque_entering"))
        composeRule.onNodeWithTag("adhkar_browse_item_mosque_entering").assertIsDisplayed()
        composeRule.onNodeWithTag("adhkar_browse_count_mosque_entering").performClick()
        assertEquals("mosque_entering", incrementedId)

        listNode.performScrollToNode(hasTestTag("adhkar_open_chapter_1"))
        composeRule.onNodeWithTag("adhkar_open_chapter_1").performClick()
        assertEquals("دعاء دخول المسجد", openedChapter)
        assertEquals(0, openedIndex)

        listNode.performScrollToNode(hasTestTag("adhkar_browse_search"))
        composeRule.onNodeWithTag("adhkar_browse_search").performTextReplacement("الخروج")
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("دعاء الخروج من المسجد", substring = true)[0].assertIsDisplayed()
    }

    @Test
    fun `adhkarDetailScreen scoped to chapterTitle displays only that chapter title and items`() {
        val resetIds = mutableListOf<String>()

        composeRule.setContent {
            QuranWsTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AdhkarDetailScreen(
                        category = DhikrCategory.MOSQUE,
                        uiState = AdhkarUiState(),
                        chapterTitle = "دعاء دخول المسجد",
                        onIncrementDhikr = { true },
                        onResetDhikr = {},
                        onResetCategory = {},
                        onResetItems = { resetIds.addAll(it) },
                        onAdjustFontSize = {},
                        onBack = {}
                    )
                }
            }
        }

        composeRule.onAllNodesWithText("دعاء دخول المسجد").assertCountEquals(2)
        composeRule.onNodeWithText("دعاء الذهاب إلى المسجد").assertDoesNotExist()

        composeRule.onNodeWithTag("adhkar_detail_reset_button").performClick()
        assertTrue(resetIds.contains("mosque_entering"))
        assertEquals(1, resetIds.size)
    }

    @Test
    fun `shouldOpenDirectlyInReader is true for morning, evening, after prayer, sleep, and single-type categories, and false for multi-type`() {
        val state = AdhkarUiState()

        assertTrue(state.shouldOpenDirectlyInReader(DhikrCategory.MORNING))
        assertTrue(state.shouldOpenDirectlyInReader(DhikrCategory.EVENING))
        assertTrue(state.shouldOpenDirectlyInReader(DhikrCategory.AFTER_PRAYER))
        assertTrue(state.shouldOpenDirectlyInReader(DhikrCategory.SLEEP))

        assertTrue(!state.shouldOpenDirectlyInReader(DhikrCategory.MOSQUE))
        assertTrue(!state.shouldOpenDirectlyInReader(DhikrCategory.TRAVEL))
        assertTrue(!state.shouldOpenDirectlyInReader(DhikrCategory.RUQYAH))
        assertTrue(!state.shouldOpenDirectlyInReader(DhikrCategory.TASBIH))
        assertTrue(!state.shouldOpenDirectlyInReader(DhikrCategory.QURANIC_DUAS))

        val singleChapterWakeState = state.copy(
            itemsByCategory = state.itemsByCategory + (
                DhikrCategory.WAKE_UP to listOf(
                    state.itemsForCategory(DhikrCategory.WAKE_UP).first()
                )
            )
        )
        assertTrue(singleChapterWakeState.shouldOpenDirectlyInReader(DhikrCategory.WAKE_UP))
    }

    @Test
    fun `firstIncompleteIndex skips completed morning items and resets to 0 when all are done`() {
        val morningItems = AdhkarUiState().itemsForCategory(DhikrCategory.MORNING)
        val freshState = AdhkarUiState()
        assertEquals(0, freshState.firstIncompleteIndex(DhikrCategory.MORNING))

        val partialState = AdhkarUiState(
            countsById = mapOf(
                morningItems[0].id to morningItems[0].targetCount,
                morningItems[1].id to morningItems[1].targetCount
            )
        )
        assertEquals(2, partialState.firstIncompleteIndex(DhikrCategory.MORNING))

        val completedState = AdhkarUiState(
            countsById = morningItems.associate { it.id to it.targetCount }
        )
        assertEquals(0, completedState.firstIncompleteIndex(DhikrCategory.MORNING))
    }
}
