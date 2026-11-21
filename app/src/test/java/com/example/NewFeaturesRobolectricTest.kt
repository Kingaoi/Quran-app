package com.example

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.data.audio.AudioPlaybackState
import com.example.data.audio.WirdAudioRange
import com.example.data.local.IndexSearchHistoryRepository
import com.example.data.local.MushafOfflineStorage
import com.example.data.local.ReadingStats
import com.example.domain.model.SurahInfo
import com.example.domain.quran.SurahGroupsCatalog
import com.example.domain.wird.KhatmahPlanConfig
import com.example.domain.wird.WirdPlanSummary
import com.example.domain.wird.WirdPortion
import com.example.domain.wird.WirdScheduleStatus
import com.example.ui.screens.IndexScreen
import com.example.ui.screens.MushafReaderScreen
import com.example.ui.screens.WirdScreen
import com.example.ui.theme.QuranWsTheme
import com.example.ui.viewmodel.QuranUiState
import com.example.ui.viewmodel.WirdUiState
import kotlinx.coroutines.flow.first
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
class NewFeaturesRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sampleSurahs = listOf(
        SurahInfo(1, "الفاتحة", "Al-Fatiha", 7, "مكية", true, true, 1, 1, 29, 139, "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"),
        SurahInfo(2, "البقرة", "Al-Baqarah", 286, "مدنية", false, true, 2, 49, 6144, 25613, "الٓمٓ"),
        SurahInfo(3, "آل عمران", "Aal-i-Imran", 200, "مدنية", false, true, 50, 76, 3503, 14605, "الٓمٓ"),
        SurahInfo(108, "الكوثر", "Al-Kawthar", 3, "مكية", true, true, 602, 602, 10, 42, "إِنَّا أَعْطَيْنَاكَ ٱلْكَوْثَرَ")
    )

    @Test
    fun `surahGroupsCatalog links all 114 surahs and pairs zahrawan`() {
        for (i in 1..114) {
            val groups = SurahGroupsCatalog.groupsForSurah(i)
            assertTrue("Expected at least 1 group for surah $i", groups.isNotEmpty())
        }
        val baqarahGroups = SurahGroupsCatalog.groupsForSurah(2)
        assertTrue(baqarahGroups.any { it.id == "zahrawan" && 3 in it.surahNumbers })
    }

    @Test
    fun `indexSearchHistoryRepository persists deduplicates and clears queries`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = IndexSearchHistoryRepository(context)
        repo.clearAll()
        repo.recordQuery("البقرة")
        repo.recordQuery("الرحمن")
        repo.recordQuery("البقرة")
        assertEquals(listOf("البقرة", "الرحمن"), repo.recentSearchesFlow.first())

        repo.removeQuery("الرحمن")
        assertEquals(listOf("البقرة"), repo.recentSearchesFlow.first())

        repo.clearAll()
        assertTrue(repo.recentSearchesFlow.first().isEmpty())
    }

    @Test
    fun `indexScreen supports sort options, direct play, sister surahs, and recent searches`() {
        var playedSurah = -1
        var clearedRecent = false
        val recorded = mutableListOf<String>()
        val removed = mutableListOf<String>()

        composeRule.setContent {
            QuranWsTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    IndexScreen(
                        uiState = QuranUiState(isLoading = false, surahs = sampleSurahs),
                        recentSearches = listOf("الكوثر", "البقرة"),
                        playbackState = AudioPlaybackState(isActive = false),
                        onOpenPage = {},
                        onOpenSurah = {},
                        onTogglePlaySurah = { playedSurah = it },
                        onSearchQuran = { emptyList() },
                        onRecordSearch = { recorded += it },
                        onRemoveRecentSearch = { removed += it },
                        onClearRecentSearches = { clearedRecent = true }
                    )
                }
            }
        }

        // Verify sort button sits at the top, to the left of search button in RTL, and above tabs
        val sortBounds = composeRule.onNodeWithTag("surah_sort_button").getUnclippedBoundsInRoot()
        val searchBounds = composeRule.onNodeWithTag("index_search_toggle_button").getUnclippedBoundsInRoot()
        val tabsBounds = composeRule.onNodeWithTag("index_tab_0").getUnclippedBoundsInRoot()
        assertTrue(sortBounds.right <= searchBounds.left)
        assertTrue(sortBounds.bottom <= tabsBounds.top)

        // 1. Direct Play button on Surah card triggers playback
        composeRule.onNodeWithTag("surah_play_button_2").performClick()
        assertEquals(2, playedSurah)

        // 2. Sort dropdown works: Fewest Ayahs brings Al-Kawthar (3 ayahs) to top, Most Ayahs brings Al-Baqarah (286) to top
        composeRule.onNodeWithTag("surah_sort_button").performClick()
        composeRule.onNodeWithTag("surah_sort_option_fewest_ayahs").performClick()
        composeRule.onNodeWithTag("surah_card_108").assertIsDisplayed()

        composeRule.onNodeWithTag("surah_sort_button").performClick()
        composeRule.onNodeWithTag("surah_sort_option_most_ayahs").performClick()
        composeRule.onNodeWithText("الأكثر آيات").assertIsDisplayed()

        // 3. Details sheet play button + sister Surah navigation (Zahrawan: Baqarah -> Aal-i-Imran)
        composeRule.onNodeWithTag("surah_info_button_2").performClick()
        composeRule.onNodeWithTag("surah_details_play_button").performClick()
        assertEquals(2, playedSurah)

        composeRule.onNodeWithTag("sister_surah_pill_zahrawan_3").performScrollTo().performClick()
        composeRule.onNodeWithTag("surah_details_sheet_3").assertIsDisplayed()
        composeRule.onNodeWithTag("close_surah_details_button").performClick()
        composeRule.waitForIdle()

        // 4. Search toggle reveals recent searches chips & clear action
        composeRule.onNodeWithTag("index_search_toggle_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("recent_searches_strip").assertIsDisplayed()
        composeRule.onNodeWithTag("recent_search_chip_0").performClick()
        composeRule.waitForIdle()
        assertTrue("Expected الكوثر recorded, got recorded=$recorded removed=$removed", recorded == listOf("الكوثر"))

        composeRule.onNodeWithTag("surah_filter_input").performTextReplacement("")
        composeRule.onNodeWithTag("clear_recent_searches_button").performClick()
        assertTrue(clearedRecent)
    }

    @Test
    fun `mushafReaderScreen toggles auto scroll bar and preset speeds`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val offlineStorage = MushafOfflineStorage(context)

        composeRule.setContent {
            QuranWsTheme {
                MushafReaderScreen(
                    uiState = QuranUiState(isLoading = false, surahs = sampleSurahs),
                    currentPage = 1,
                    playbackState = AudioPlaybackState(),
                    offlineStorage = offlineStorage,
                    getAyahsForPage = { emptyList() },
                    getSvgModel = { "" },
                    getRawGithubSvgUrl = { "" },
                    onPageScrolling = {},
                    onPageSettled = {},
                    onSelectSurah = {},
                    onToggleBookmark = {},
                    onPlayAyah = { _, _ -> },
                    onPlayPageFirstAyah = {},
                    onTogglePlayPause = {},
                    onNextAyah = {},
                    onPreviousAyah = {},
                    onToggleRepeat = {},
                    onSeekAudio = {},
                    onStopAudio = {},
                    onSelectReciter = {}
                )
            }
        }

        // Toggle Auto-Scroll on from bottom scrubber pill
        composeRule.onNodeWithTag("reader_autoscroll_button").performClick()
        composeRule.onNodeWithTag("reader_autoscroll_bar").assertIsDisplayed()

        // Expand speed presets & select 40s preset
        composeRule.onNodeWithTag("autoscroll_speed_expand_button").performClick()
        composeRule.onNodeWithTag("autoscroll_preset_40").assertIsDisplayed().performClick()

        // Toggle pause & close auto-scroll bar
        composeRule.onNodeWithTag("autoscroll_play_pause_button").performClick()
        composeRule.onNodeWithTag("autoscroll_close_button").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun `wirdScreen places stats button next to plans button and opens stats modal sheet`() {
        composeRule.setContent {
            QuranWsTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    WirdScreen(
                        uiState = WirdUiState(isLoading = false),
                        stats = ReadingStats(
                            currentStreakDays = 4,
                            longestStreakDays = 9,
                            totalPagesRead = 120,
                            completedKhatmahsCount = 1,
                            adhkarTodayCount = 3,
                            adhkarTotalCount = 42,
                            tasbihTodayCount = 33,
                            tasbihTotalCount = 300
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
        }

        val plansBounds = composeRule.onNodeWithTag("wird_all_plans_topbar_button").getUnclippedBoundsInRoot()
        val statsBounds = composeRule.onNodeWithTag("wird_stats_topbar_button").getUnclippedBoundsInRoot()
        assertTrue(statsBounds.right <= plansBounds.left)
        composeRule.onNodeWithTag("wird_stats_card").assertDoesNotExist()

        composeRule.onNodeWithTag("wird_stats_topbar_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wird_stats_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("wird_stats_card").assertIsDisplayed()
        composeRule.onNodeWithTag("wird_stats_tasbih_strip").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("wird_stats_close_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wird_stats_sheet").assertDoesNotExist()
    }

    @Test
    fun `wirdHeroCard direct listen button triggers wird playback and displays active status`() {
        val currentWird = WirdPortion(
            index = 0,
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
            openingAyahText = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
            quartersCount = 8
        )
        val plan = WirdPlanSummary(
            allWirds = listOf(currentWird),
            totalWirds = 30,
            completedCount = 0,
            upcomingCount = 29,
            currentWird = currentWird,
            previousWirds = emptyList(),
            upcomingWirds = emptyList(),
            isCompleted = false,
            progressFraction = 0.03f,
            scheduleStatus = WirdScheduleStatus.OnTrack,
            resumePage = 1,
            hasStartedCurrentWird = false
        )
        var toggledWird: WirdPortion? = null
        val range = WirdAudioRange(0, 1, 1, 2, 141)
        assertTrue(range.isEnd(2, 141))
        assertTrue(!range.isEnd(2, 140))

        composeRule.setContent {
            QuranWsTheme {
                WirdScreen(
                    uiState = WirdUiState(
                        isLoading = false,
                        config = KhatmahPlanConfig(isActive = true, startJuz = 1, quartersPerDay = 8),
                        plan = plan
                    ),
                    playbackState = AudioPlaybackState(
                        isActive = true,
                        isPlaying = true,
                        currentSurah = 2,
                        currentAyah = 25,
                        surahNameAr = "البقرة",
                        wirdAudioRange = range
                    ),
                    onStartNewKhatmah = { _, _ -> },
                    onCompleteCurrentWird = {},
                    onUndoCompleteWird = {},
                    onSetCompletedCount = {},
                    onOpenPageInReader = { _, _ -> },
                    onTogglePlayWird = { toggledWird = it },
                    onClearStatusMessage = {}
                )
            }
        }

        composeRule.onNodeWithTag("wird_live_audio_strip").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("wird_listen_button").performScrollTo().assertIsDisplayed().performClick()
        composeRule.onNodeWithText("إيقاف التلاوة").assertIsDisplayed()
        assertEquals(0, toggledWird?.index)
    }
}
