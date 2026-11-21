package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.audio.AudioPlaybackState
import com.example.data.audio.AudioPlaylist
import com.example.data.local.AyahBookmark
import com.example.data.repository.QuranRepository
import com.example.domain.model.AyahItem
import com.example.domain.model.SearchHit
import com.example.domain.model.SurahInfo
import com.example.domain.quran.QuranConcordanceReport
import com.example.domain.quran.QuranSearchMode
import com.example.domain.quran.SurahDetailedCatalog
import com.example.ui.components.QuranSnackbarHost
import com.example.ui.components.showQuranToast
import com.example.ui.navigation.QuranMotionSpecs
import com.example.ui.navigation.predictiveBackTransform
import com.example.ui.navigation.rememberPredictiveBackState
import com.example.ui.screens.index.BookmarksManagementSheet
import com.example.ui.screens.index.HizbRangeFilter
import com.example.ui.screens.index.IndexPlaylistsModalsHost
import com.example.ui.screens.index.IndexRecentSearchesStrip
import com.example.ui.screens.index.IndexResumeStrip
import com.example.ui.screens.index.IndexSearchControlsPanel
import com.example.ui.screens.index.IndexSearchDiscoveryCard
import com.example.ui.screens.index.IndexSearchResultsList
import com.example.ui.screens.index.IndexSegmentedTabs
import com.example.ui.screens.index.IndexTabletPreviewPane
import com.example.ui.screens.index.IndexTopBar
import com.example.ui.screens.index.JuzAccordionCard
import com.example.ui.screens.index.SurahDetailsModalSheet
import com.example.ui.screens.index.SurahRevelationFilter
import com.example.ui.screens.index.SurahSortOption
import com.example.ui.screens.index.indexAhzabSection
import com.example.ui.screens.index.indexPlaylistsSection
import com.example.ui.screens.index.indexSajdahSection
import com.example.ui.screens.index.indexSurahsSection
import com.example.ui.screens.index.indexTopicsSection
import com.example.ui.theme.LocalFloatingNavBottomPadding
import com.example.ui.viewmodel.AudioPlaylistActions
import com.example.ui.viewmodel.QuranUiState
import kotlinx.coroutines.launch

@Composable
fun IndexScreen(
    uiState: QuranUiState,
    bookmarks: List<AyahBookmark> = emptyList(),
    recentSearches: List<String> = emptyList(),
    playbackState: AudioPlaybackState = AudioPlaybackState(),
    playlists: List<AudioPlaylist> = emptyList(),
    hiddenPresetsCount: Int = 0,
    playlistActions: AudioPlaylistActions = AudioPlaylistActions(),
    getAyahsForPage: (Int) -> List<AyahItem> = { emptyList() }, getPageForAyah: (Int, Int) -> Int? = { _, _ -> null },
    onOpenPage: (Int) -> Unit,
    onOpenAyah: (page: Int, surah: Int, ayah: Int) -> Unit = { p, _, _ -> onOpenPage(p) },
    onOpenSurah: (Int) -> Unit,
    onTogglePlaySurah: (Int) -> Unit = {},
    onSearchQuran: (String) -> List<SearchHit>,
    onSearchConcordance: (String, QuranSearchMode) -> QuranConcordanceReport = { q, m -> QuranConcordanceReport.empty(q, m) },
    onRecordSearch: (String) -> Unit = {},
    onRemoveRecentSearch: (String) -> Unit = {},
    onClearRecentSearches: () -> Unit = {},
    onDeleteAyahBookmark: (String) -> Unit = {},
    onTogglePageBookmark: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedSurahFilter by remember { mutableStateOf(SurahRevelationFilter.ALL) }
    var selectedSurahSort by remember { mutableStateOf(SurahSortOption.MUSHAF) }
    var selectedHizbRange by remember { mutableStateOf(HizbRangeFilter.ALL) }
    var selectedTopicId by remember { mutableStateOf<String?>(null) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf(QuranSearchMode.WORD) }
    var showBookmarksSheet by remember { mutableStateOf(false) }
    var inspectedSurah by remember { mutableStateOf<SurahInfo?>(null) }
    var inspectedPlaylistId by remember { mutableStateOf<String?>(null) }
    var showCreatePlaylistSheet by remember { mutableStateOf(false) }
    var quickAddSurahToPlaylist by remember { mutableStateOf<SurahInfo?>(null) }
    var previewPageNum by remember(uiState.currentPage) { mutableIntStateOf(uiState.currentPage) }
    val surahsByNumber = remember(uiState.surahs) { uiState.surahs.associateBy { it.number } }

    val expandedJuzMap = remember(uiState.currentPageJuz) { mutableStateMapOf(1 to true, 2 to true, uiState.currentPageJuz to true) }
    val expandedHizbMap = remember { mutableStateMapOf(1 to true) }
    val quartersByHizb = remember(uiState.hizbQuartersByJuz) { uiState.hizbQuartersByJuz.values.flatten().groupBy { it.hizbNumber } }

    val filteredSurahs = remember(uiState.surahs, searchQuery) {
        if (searchQuery.isBlank()) emptyList() else {
            val q = QuranRepository.normalizeArabic(searchQuery)
            uiState.surahs.filter {
                QuranRepository.normalizeArabic(it.nameAr).contains(q) ||
                    it.nameEn.contains(searchQuery, ignoreCase = true) ||
                    it.number.toString() == searchQuery.trim()
            }
        }
    }
    val ayahSearchHits = remember(searchQuery, uiState.isLoading) {
        if (searchQuery.trim().length < 2) emptyList() else onSearchQuran(searchQuery)
    }
    val concordanceReport = remember(searchQuery, searchMode, uiState.isLoading) {
        if (searchQuery.trim().length < 2) QuranConcordanceReport.empty(searchQuery, searchMode)
        else onSearchConcordance(searchQuery, searchMode)
    }

    fun commitActiveSearch() {
        val trimmed = searchQuery.trim()
        if (trimmed.isNotEmpty()) onRecordSearch(trimmed)
    }
    val searchPredictiveState = rememberPredictiveBackState(enabled = showSearch || searchQuery.isNotBlank()) {
        showSearch = false; searchQuery = ""
    }
    val bottomClearance = LocalFloatingNavBottomPadding.current
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { QuranSnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(bottom = bottomClearance)) }
    ) { innerPadding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(innerPadding).statusBarsPadding()) {
            val isWideTablet = maxWidth >= 720.dp

            val renderScrollableList: @Composable (Modifier, Dp, (Int) -> Unit, (Int, Int, Int) -> Unit) -> Unit =
                { listModifier, hPad, pageAction, ayahAction ->
                    AnimatedContent(
                        targetState = searchQuery.isNotBlank(),
                        transitionSpec = QuranMotionSpecs.subScreenTransition { _, target -> target },
                        label = "index_search_results_switch"
                    ) { showingResults ->
                        if (showingResults) {
                            Box(modifier = listModifier.predictiveBackTransform(searchPredictiveState)) {
                                IndexSearchResultsList(
                                    filteredSurahs = filteredSurahs, ayahSearchHits = ayahSearchHits,
                                    searchMode = searchMode, concordanceReport = concordanceReport, playbackState = playbackState,
                                    onOpenSurah = { s -> commitActiveSearch(); onOpenSurah(s) },
                                    onOpenHit = { hit -> commitActiveSearch(); onOpenAyah(hit.pageNumber, hit.surahNumber, hit.ayahNumber) },
                                    onOpenConcordanceAyah = { p, s, a -> commitActiveSearch(); ayahAction(p, s, a) },
                                    onTogglePlaySurah = { s -> commitActiveSearch(); onTogglePlaySurah(s) },
                                    onShowSurahInfo = { s -> commitActiveSearch(); inspectedSurah = s },
                                    horizontalPadding = hPad,
                                    topContent = {
                                        IndexTopBar(showSearch = showSearch, onToggleSearch = { showSearch = !showSearch; if (!showSearch) searchQuery = "" })
                                        IndexSearchControlsPanel(query = searchQuery, searchMode = searchMode, onQueryChange = { searchQuery = it }, onSelectMode = { searchMode = it }, onSearchSubmit = ::commitActiveSearch)
                                    }
                                )
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(start = hPad, end = hPad, top = 4.dp, bottom = 12.dp + bottomClearance),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = listModifier.predictiveBackTransform(searchPredictiveState)
                            ) {
                            item(key = "top_bar") {
                                IndexTopBar(
                                    showSearch = showSearch,
                                    onToggleSearch = { showSearch = !showSearch; if (!showSearch) searchQuery = "" },
                                    showSortButton = selectedTab == 0,
                                    selectedSort = selectedSurahSort,
                                    onSelectSort = { selectedSurahSort = it }
                                )
                            }
                            if (showSearch) {
                                item(key = "search_controls") {
                                    IndexSearchControlsPanel(query = searchQuery, searchMode = searchMode, onQueryChange = { searchQuery = it }, onSelectMode = { searchMode = it }, onSearchSubmit = ::commitActiveSearch)
                                }
                                if (recentSearches.isNotEmpty()) {
                                    item(key = "recent_searches") {
                                        IndexRecentSearchesStrip(
                                            recentSearches = recentSearches,
                                            onSelectQuery = { selected -> searchQuery = selected; onRecordSearch(selected) },
                                            onRemoveQuery = onRemoveRecentSearch,
                                            onClearAll = onClearRecentSearches
                                        )
                                    }
                                }
                                item(key = "search_discovery") {
                                    IndexSearchDiscoveryCard(mode = searchMode, onSelectSuggestion = { suggested -> searchQuery = suggested; onRecordSearch(suggested) })
                                }
                            }
                            item(key = "resume_strip") {
                                IndexResumeStrip(
                                    uiState = uiState,
                                    totalSavedBookmarks = bookmarks.size,
                                    onOpenPage = onOpenPage,
                                    onOpenBookmarksSheet = { showBookmarksSheet = true }
                                )
                            }
                            item(key = "segmented_tabs") {
                                IndexSegmentedTabs(
                                    selectedTab = selectedTab,
                                    sajdahCount = uiState.sajdahAyahs.size.takeIf { it > 0 } ?: 15,
                                    topicCount = uiState.topicGroups.size.takeIf { it > 0 } ?: 6,
                                    playlistCount = playlists.size,
                                    onSelectTab = { selectedTab = it }
                                )
                            }
                            when (selectedTab) {
                                0 -> indexSurahsSection(
                                    surahs = uiState.surahs, selectedFilter = selectedSurahFilter, selectedSort = selectedSurahSort,
                                    playbackState = playbackState, onSelectFilter = { selectedSurahFilter = it },
                                    onOpenSurahPage = pageAction, onTogglePlaySurah = onTogglePlaySurah, onShowSurahInfo = { inspectedSurah = it }
                                )
                                1 -> items(uiState.ajza, key = { it.number }) { juz ->
                                    val isExpanded = expandedJuzMap[juz.number] == true
                                    JuzAccordionCard(
                                        juz = juz, isExpanded = isExpanded, surahSlices = uiState.juzSurahSlices[juz.number].orEmpty(),
                                        onToggleExpand = { expandedJuzMap[juz.number] = !isExpanded }, onOpenPage = pageAction, onShowSurahInfo = { inspectedSurah = it }
                                    )
                                }
                                2 -> indexAhzabSection(
                                    quartersByHizb = quartersByHizb, selectedRange = selectedHizbRange, expandedHizbMap = expandedHizbMap,
                                    onSelectRange = { selectedHizbRange = it }, onOpenPage = pageAction
                                )
                                3 -> indexSajdahSection(sajdahs = uiState.sajdahAyahs, onOpenAyah = ayahAction)
                                4 -> indexTopicsSection(
                                    topicGroups = uiState.topicGroups, selectedTopicId = selectedTopicId,
                                    onSelectTopic = { selectedTopicId = it }, onOpenAyah = ayahAction,
                                    onOpenSmartSearch = { showSearch = true; searchMode = QuranSearchMode.MEANING }
                                )
                                else -> indexPlaylistsSection(
                                    playlists = playlists, surahsByNumber = surahsByNumber, playbackState = playbackState,
                                    hiddenPresetsCount = hiddenPresetsCount, onCreatePlaylist = { showCreatePlaylistSheet = true },
                                    onOpenPlaylistDetail = { inspectedPlaylistId = it.id }, onTogglePlayPlaylist = playlistActions.onTogglePlayPlaylist,
                                    onNextTrack = playlistActions.onNextTrack, onPreviousTrack = playlistActions.onPreviousTrack, onRestorePresets = playlistActions.onRestorePresets
                                )
                            }
                        }
                    }
                }
            }

            if (isWideTablet) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    renderScrollableList(
                        Modifier.weight(1.05f).fillMaxHeight(),
                        12.dp,
                        { previewPageNum = it },
                        { page, _, _ -> previewPageNum = page }
                    )
                    IndexTabletPreviewPane(
                        pageNumber = previewPageNum,
                        ayahs = remember(previewPageNum) { getAyahsForPage(previewPageNum) },
                        onOpenReader = { onOpenPage(previewPageNum) },
                        modifier = Modifier.weight(0.95f).fillMaxHeight().padding(top = 12.dp, bottom = 12.dp + bottomClearance)
                    )
                }
            } else {
                renderScrollableList(
                    Modifier.widthIn(max = 760.dp).fillMaxSize().align(Alignment.TopCenter),
                    16.dp,
                    onOpenPage,
                    onOpenAyah
                )
            }
        }
    }

    if (showBookmarksSheet) {
        BookmarksManagementSheet(
            ayahBookmarks = bookmarks, bookmarkedPages = uiState.settings.bookmarkedPages,
            onOpenAyah = onOpenAyah, onOpenPage = onOpenPage,
            onDeleteAyahBookmark = onDeleteAyahBookmark, onRemovePageBookmark = onTogglePageBookmark,
            onDismiss = { showBookmarksSheet = false }
        )
    }

    inspectedSurah?.let { surah ->
        val details = remember(surah.number, uiState.juzSurahSlices, uiState.hizbQuartersByJuz, uiState.sajdahAyahs) {
            SurahDetailedCatalog.resolveDetails(surah, uiState.juzSurahSlices, uiState.hizbQuartersByJuz, uiState.sajdahAyahs)
        }
        SurahDetailsModalSheet(
            details = details,
            allSurahs = uiState.surahs,
            playbackState = playbackState,
            onOpenPage = onOpenPage,
            onOpenAyah = onOpenAyah,
            onTogglePlaySurah = onTogglePlaySurah,
            onAddToPlaylist = { quickAddSurahToPlaylist = it },
            onSelectSurahNumber = { targetNum -> uiState.surahs.firstOrNull { it.number == targetNum }?.let { inspectedSurah = it } },
            onDismiss = { inspectedSurah = null }
        )
    }

    IndexPlaylistsModalsHost(
        playlists = playlists, surahsByNumber = surahsByNumber, playbackState = playbackState,
        activeRiwayah = uiState.settings.riwayah, actions = playlistActions,
        inspectedPlaylistId = inspectedPlaylistId, showCreatePlaylistSheet = showCreatePlaylistSheet,
        quickAddSurah = quickAddSurahToPlaylist, getPageForAyah = getPageForAyah, onOpenAyahInMushaf = onOpenAyah,
        onAddedSurahToast = { msg -> scope.launch { snackbarHostState.showQuranToast(msg) } },
        onDismissDetail = { inspectedPlaylistId = null }, onDismissCreate = { showCreatePlaylistSheet = false },
        onDismissQuickAdd = { quickAddSurahToPlaylist = null }
    )
}
