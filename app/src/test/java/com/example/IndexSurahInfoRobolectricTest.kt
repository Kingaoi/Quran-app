package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.QuranRepository
import com.example.domain.model.JuzInfo
import com.example.domain.model.JuzSurahSlice
import com.example.domain.model.SurahInfo
import com.example.domain.quran.SurahDetailedCatalog
import com.example.ui.screens.IndexScreen
import com.example.ui.theme.QuranWsTheme
import com.example.ui.viewmodel.QuranUiState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = QuranApplication::class)
class IndexSurahInfoRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `surahDetailedCatalog covers all 114 surahs with unique revelation order`() {
        val profiles = SurahDetailedCatalog.allProfiles()
        assertEquals(114, profiles.size)
        for (i in 1..114) {
            assertTrue("Missing profile for surah $i", profiles.containsKey(i))
        }
        val orders = profiles.values.map { it.revelationOrder }.toSet()
        assertEquals((1..114).toSet(), orders)
    }

    @Test
    fun `quranRepository enriches surahs with word letter and opening ayah`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = QuranRepository(context)
        repo.ensureLoaded()
        val surahs = repo.getSurahs()
        assertEquals(114, surahs.size)
        val fatiha = surahs.first()
        assertEquals(1, fatiha.number)
        assertTrue(fatiha.wordCount > 20)
        assertTrue(fatiha.letterCount > 100)
        assertTrue(fatiha.openingAyahText.isNotBlank())
    }

    @Test
    fun `indexScreen displays surahs directly in tab 0 and opens details sheet on info click`() {
        val surah1 = SurahInfo(
            number = 1,
            nameAr = "الفاتحة",
            nameEn = "Al-Fatiha",
            ayahCount = 7,
            revelationType = "مكية",
            isMakki = true,
            hasBasmalah = true,
            firstPage = 1,
            lastPage = 1,
            wordCount = 29,
            letterCount = 139,
            openingAyahText = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
        )
        val surah2 = SurahInfo(
            number = 2,
            nameAr = "البقرة",
            nameEn = "Al-Baqarah",
            ayahCount = 286,
            revelationType = "مدنية",
            isMakki = false,
            hasBasmalah = true,
            firstPage = 2,
            lastPage = 49,
            wordCount = 6144,
            letterCount = 25613,
            openingAyahText = "الٓمٓ"
        )
        val juz1 = JuzInfo(1, "الجزء الأول", 1, "الفاتحة", 1, 1)
        val uiState = QuranUiState(
            isLoading = false,
            surahs = listOf(surah1, surah2),
            ajza = listOf(juz1),
            juzSurahSlices = mapOf(
                1 to listOf(
                    JuzSurahSlice(1, surah1, 1, 1, false),
                    JuzSurahSlice(1, surah2, 1, 2, false)
                )
            )
        )
        var openedPage = -1

        composeRule.setContent {
            QuranWsTheme {
                IndexScreen(
                    uiState = uiState,
                    onOpenPage = { openedPage = it },
                    onOpenSurah = { openedPage = it },
                    onSearchQuran = { emptyList() }
                )
            }
        }

        // Tab 0 shows Surahs directly, not Juz accordions
        composeRule.onNodeWithTag("surah_card_1").assertIsDisplayed()
        composeRule.onNodeWithTag("surah_card_2").assertIsDisplayed()
        assertEquals(0, composeRule.onAllNodesWithTag("juz_accordion_1").fetchSemanticsNodes().size)

        // Clicking info button opens detailed sheet with rich Surah info
        composeRule.onNodeWithTag("surah_info_button_1").performClick()
        composeRule.onNodeWithTag("surah_details_sheet_1").assertIsDisplayed()
        composeRule.onNodeWithText("معنى اسم السورة وأسماؤها").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("المقصد العام للسورة").performScrollTo().assertIsDisplayed()

        // Clicking read inside details sheet navigates to Surah start page
        composeRule.onNodeWithTag("surah_details_read_button").performScrollTo().performClick()
        assertEquals(1, openedPage)

        // Switching to Tab 1 (Ajza) displays Juz accordion
        composeRule.onNodeWithTag("index_tab_1").performClick()
        composeRule.onNodeWithTag("juz_accordion_1").assertIsDisplayed()
    }
}
