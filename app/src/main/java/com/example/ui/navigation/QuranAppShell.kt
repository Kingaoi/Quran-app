package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.audio.AudioPlaybackState
import com.example.data.audio.PlaybackNotifActions
import com.example.domain.model.DhikrCategory
import com.example.ui.components.MiniAudioPlayerBar
import com.example.ui.components.PlaylistQuickPickerSheet
import com.example.ui.components.ReciterPickerSheet
import com.example.ui.screens.CustomWirdPlanActions
import com.example.ui.screens.MushafReaderScreen
import com.example.ui.screens.more.rememberMoreSettingsActions
import com.example.ui.theme.LocalFloatingNavBottomPadding
import com.example.ui.viewmodel.AdhkarViewModel
import com.example.ui.viewmodel.OfflineDownloadState
import com.example.ui.viewmodel.PrayerViewModel
import com.example.ui.viewmodel.QuranUiState
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.WirdViewModel

@Composable
fun QuranAppShell(
    useNavigationRail: Boolean,
    openWirdOnLaunch: Boolean = false,
    launchDestinationEvent: Pair<Int, AppLaunchDestination>? = null,
    viewModel: QuranViewModel,
    uiState: QuranUiState,
    offlineState: OfflineDownloadState
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isReaderVisible = currentRoute?.startsWith("reader") == true ||
        currentRoute?.startsWith("adhkar_detail") == true ||
        currentRoute?.startsWith("adhkar_browse") == true

    var selectedTab by rememberSaveable {
        mutableStateOf(if (openWirdOnLaunch || launchDestinationEvent?.second is AppLaunchDestination.OpenWirdTab) HomeTab.WIRD else HomeTab.PRAYER_TIMES)
    }
    var openAllWirdPlansCount by remember { mutableIntStateOf(0) }
    var openHijriSheetCount by remember { mutableIntStateOf(0) }

    val wirdViewModel: WirdViewModel = viewModel()
    val wirdState by wirdViewModel.uiState.collectAsStateWithLifecycle()

    val adhkarViewModel: AdhkarViewModel = viewModel()
    val adhkarState by adhkarViewModel.uiState.collectAsStateWithLifecycle()

    val prayerViewModel: PrayerViewModel = viewModel()
    val prayerState by prayerViewModel.uiState.collectAsStateWithLifecycle()

    val statsState by viewModel.statsState.collectAsStateWithLifecycle()
    val bookmarksState by viewModel.bookmarksState.collectAsStateWithLifecycle()
    val recentSearchesState by viewModel.extras.recentSearchesState.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val playlistsState by viewModel.extras.playlists.playlistsState.collectAsStateWithLifecycle()
    val hiddenPresetsCount by viewModel.extras.playlists.hiddenPresetsCountState.collectAsStateWithLifecycle()
    val tafsirOfflineState by viewModel.tafsirOfflineState.collectAsStateWithLifecycle()
    val offlineHubState by viewModel.offlineHubState.collectAsStateWithLifecycle()
    val isAudioActive = playbackState.isActive
    val downloadHubActions = remember(viewModel) { viewModel.offlineHub.createActions() }

    var showReciterSheet by remember { mutableStateOf(false) }
    var showPlaylistsQuickPicker by remember { mutableStateOf(false) }
    val notifActions = remember(viewModel) { PlaybackNotifActions(viewModel::setPlaybackNotifEnabled, viewModel::togglePlaybackNotifButton, viewModel::setPlaybackNotifShowWhenPaused) }
    val playlistActions = rememberAudioPlaylistActions(viewModel)
    val moreActions = rememberMoreSettingsActions(viewModel, adhkarViewModel, prayerViewModel, wirdViewModel)
    val shellCallbacks = rememberShellNavigationCallbacks(viewModel, wirdViewModel, adhkarViewModel, navController)
    val jumpToPlayingAyah: (Int, Int) -> Unit = { surah, ayah ->
        viewModel.getPageForAyah(surah, ayah)?.let { shellCallbacks.openAyah(it, surah, ayah) } ?: shellCallbacks.openSurah(surah)
    }

    fun navigateToTab(tab: HomeTab) {
        selectedTab = tab
        if (currentRoute != null && currentRoute != ROUTE_HOME) {
            navController.popBackStack(ROUTE_HOME, inclusive = false)
        }
    }

    LaunchedEffect(launchDestinationEvent?.first) {
        when (val dest = launchDestinationEvent?.second) {
            AppLaunchDestination.PrayerTab -> navigateToTab(HomeTab.PRAYER_TIMES)
            AppLaunchDestination.HijriCalendar -> { navigateToTab(HomeTab.PRAYER_TIMES); openHijriSheetCount++ }
            AppLaunchDestination.AdhkarTab -> navigateToTab(HomeTab.ADHKAR)
            AppLaunchDestination.LastReadPage -> shellCallbacks.openPage(uiState.currentPage.coerceIn(1, 604))
            is AppLaunchDestination.OpenSurah -> shellCallbacks.openSurah(dest.surahNumber)
            is AppLaunchDestination.OpenWirdTab -> {
                navigateToTab(HomeTab.WIRD)
                val plan = dest.customPlanId?.let { id -> wirdState.customPlans.firstOrNull { it.config.id == id } }
                if (dest.openReaderForPlan && plan != null) {
                    val idx = plan.summary.currentWird?.index ?: (plan.summary.totalWirds - 1).coerceAtLeast(0)
                    shellCallbacks.customWirdActions.onOpenPlanPage(plan.summary.resumePage, idx, plan.config.id)
                } else if (dest.openAllPlans) {
                    openAllWirdPlansCount++
                }
            }
            null -> Unit
        }
    }

    val navInsetBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val floatingBottomClearance = navInsetBottom + when {
        isReaderVisible -> 0.dp
        useNavigationRail -> if (isAudioActive) 88.dp else 16.dp
        isAudioActive -> 160.dp
        else -> 90.dp
    }

    QuranSharedTransitionHost {
        CompositionLocalProvider(LocalFloatingNavBottomPadding provides floatingBottomClearance) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (useNavigationRail && !isReaderVisible) {
                        QuranShellNavigationRail(selectedTab = selectedTab, onSelectTab = ::navigateToTab, modifier = Modifier.quranFloatingChromeOverlay())
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        NavHost(
                            navController = navController, startDestination = ROUTE_HOME,
                            enterTransition = QuranMotionSpecs.navEnterTransition(),
                            exitTransition = QuranMotionSpecs.navExitTransition(),
                            popEnterTransition = QuranMotionSpecs.navPopEnterTransition(),
                            popExitTransition = QuranMotionSpecs.navPopExitTransition(),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable(ROUTE_HOME) {
                                val tabPredictiveState = rememberPredictiveBackState(selectedTab != HomeTab.PRAYER_TIMES) { selectedTab = HomeTab.PRAYER_TIMES }
                                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
                                    HomeTabsContainer(
                                        selectedTab = selectedTab, isHomeVisible = !isReaderVisible,
                                        openAllWirdPlansRequestCount = openAllWirdPlansCount,
                                        openHijriSheetRequestCount = openHijriSheetCount,
                                uiState = uiState,
                                offlineState = offlineState,
                                wirdState = wirdState,
                                adhkarState = adhkarState,
                                prayerState = prayerState,
                                prayerTickerFlow = prayerViewModel.tickerState,
                                statsState = statsState, bookmarksState = bookmarksState,
                                recentSearchesState = recentSearchesState, playbackState = playbackState,
                                playlists = playlistsState, hiddenPresetsCount = hiddenPresetsCount, playlistActions = playlistActions,
                                tafsirOfflineState = tafsirOfflineState, offlineHubState = offlineHubState,
                                downloadHubActions = downloadHubActions, moreActions = moreActions,
                                getAyahsForPage = viewModel::getAyahsForPage, getPageForAyah = viewModel::getPageForAyah,
                                onOpenPage = shellCallbacks.openPage, onOpenWirdPage = shellCallbacks.openWirdPage,
                                onOpenAyah = shellCallbacks.openAyah, onOpenSurah = shellCallbacks.openSurah,
                                onTogglePlaySurah = shellCallbacks.togglePlaySurah, onTogglePlayWird = shellCallbacks.togglePlayWird,
                                onSearchQuran = viewModel::searchQuran, onSearchConcordance = viewModel::searchConcordance, onRecordSearch = viewModel.extras::recordIndexSearch,
                                onRemoveRecentSearch = viewModel.extras::removeIndexSearch, onClearRecentSearches = viewModel.extras::clearIndexSearches,
                                onDeleteAyahBookmark = viewModel.extras::deleteBookmark, onTogglePageBookmark = viewModel::toggleBookmark,
                                onStartNewKhatmah = wirdViewModel::startNewKhatmah,
                                onCompleteCurrentWird = wirdViewModel::completeCurrentWird,
                                onUndoCompleteWird = wirdViewModel::undoLastWirdCompletion,
                                onSetWirdCompletedCount = wirdViewModel::setCompletedWirdsCount,
                                onSetDailyGoalPages = viewModel.extras::setDailyGoalPages,
                                onClearWirdStatus = wirdViewModel::clearStatusMessage,
                                customWirdActions = shellCallbacks.customWirdActions,
                                onOpenAdhkarCategory = shellCallbacks.openAdhkarCategory,
                                onOpenAdhkarItem = shellCallbacks.openAdhkarItem,
                                onRefreshAdhkarSource = adhkarViewModel::refreshFromHisnMuslim,
                                onClearAdhkarSyncStatus = adhkarViewModel::clearSyncStatusMessage,
                                onAdhkarSearchQueryChange = adhkarViewModel::updateSearchQuery,
                                onToggleAdhkarFavoritesOnly = adhkarViewModel::toggleFavoritesOnly,
                                onToggleAdhkarFavorite = adhkarViewModel::toggleFavorite,
                                onAddCustomDua = adhkarViewModel::addCustomDua,
                                onDeleteCustomDua = adhkarViewModel::deleteCustomDua,
                                onTogglePlayDhikrAudio = adhkarViewModel::togglePlayDhikrAudio,
                                onIncrementTasbih = viewModel.extras::incrementTasbih,
                                onAddCustomTasbihPhrase = viewModel.extras::addCustomTasbihPhrase,
                                onRemoveCustomTasbihPhrase = viewModel.extras::removeCustomTasbihPhrase,
                                onShiftPrayerDay = prayerViewModel::shiftDay,
                                onSelectCity = prayerViewModel::selectCity,
                                onDetectGpsLocation = prayerViewModel::detectCoarseLocation,
                                onTogglePrayerAlert = prayerViewModel::togglePrayerAlert,
                                onUpdateHijriSunnah = prayerViewModel::updateHijriSunnah,
                                        onToggleRawatibItem = viewModel.extras::toggleRawatibItem,
                                        onClearPrayerStatus = prayerViewModel::clearStatusMessage,
                                        modifier = Modifier.predictiveBackTransform(tabPredictiveState, maxCornerRadius = 24.dp, maxScaleDrop = 0.05f, maxShiftDp = 14.dp)
                                    )
                                }
                            }

                        addAdhkarNavigationRoutes(navController = navController, adhkarViewModel = adhkarViewModel, quranViewModel = viewModel)

                        composable(
                            route = ROUTE_READER,
                            arguments = listOf(
                                navArgument(ROUTE_READER_ARG_FROM_WIRD) { type = NavType.BoolType; defaultValue = false },
                                navArgument(ROUTE_READER_ARG_WIRD_INDEX) { type = NavType.IntType; defaultValue = -1 },
                                navArgument(ROUTE_READER_ARG_PLAN_ID) { type = NavType.StringType; defaultValue = "" }
                            )
                        ) { backStackEntry ->
                            val readerCurrentPage by viewModel.currentPage.collectAsStateWithLifecycle()
                            val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
                            val ayahTafsirState by viewModel.ayahTafsirState.collectAsStateWithLifecycle()
                            val surahAudioState by viewModel.surahAudioState.collectAsStateWithLifecycle()
                            val highlightedSearchAyah by viewModel.highlightedSearchAyah.collectAsStateWithLifecycle()
                            val reciterCatalog by viewModel.reciterCatalog.collectAsStateWithLifecycle()
                            val openedFromWird = backStackEntry.arguments?.getBoolean(ROUTE_READER_ARG_FROM_WIRD) ?: false
                            val targetWirdIndex = backStackEntry.arguments?.getInt(ROUTE_READER_ARG_WIRD_INDEX) ?: -1
                            val readerPlanId = backStackEntry.arguments?.getString(ROUTE_READER_ARG_PLAN_ID).orEmpty().ifBlank { null }
                            val scopedPlan = readerPlanId?.let { id -> wirdState.customPlans.firstOrNull { it.config.id == id }?.summary } ?: wirdState.plan
                            CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
                                MushafReaderScreen(
                                    uiState = uiState, currentPage = readerCurrentPage, playbackState = playbackState,
                                    offlineStorage = viewModel.offlineStorage, bookmarks = bookmarksState, tafsirState = ayahTafsirState,
                                    surahAudioState = surahAudioState, highlightedSearchAyah = highlightedSearchAyah,
                                    reciterCatalog = reciterCatalog, activeWirdPlan = scopedPlan,
                                    playlists = playlistsState, playlistActions = playlistActions,
                                    openedFromWird = openedFromWird, targetWirdIndex = targetWirdIndex,
                                    getAyahsForPage = viewModel::getAyahsForPage, getPageForAyah = viewModel::getPageForAyah,
                                    getMutashabihatCount = viewModel::getMutashabihatCount, findMutashabihat = viewModel::findMutashabihat,
                                    compareAyahWords = viewModel::compareAyahWords, getSvgModel = viewModel::getSvgModel, getRawGithubSvgUrl = viewModel::getRawGithubSvgUrl,
                                    onPageScrolling = viewModel::onPageScrolling,
                                    onPageSettled = { page -> viewModel.onPageSettled(page); wirdViewModel.onReaderPageSettled(page, readerPlanId) },
                                    onSelectSurah = viewModel::openSurah, onJumpToAyah = viewModel::openAyah, onToggleBookmark = viewModel::toggleBookmark,
                                    onSaveAyahBookmark = viewModel.extras::upsertBookmark, onDeleteAyahBookmark = viewModel.extras::deleteBookmark,
                                    onLoadTafsir = viewModel.extras::loadAyahTafsir,
                                    onCompleteActiveWird = { if (readerPlanId != null) wirdViewModel.completeCustomPlanWird(readerPlanId, false) else wirdViewModel.completeCurrentWird(false) },
                                    onCompleteWirdAtIndex = { idx -> if (readerPlanId != null) wirdViewModel.setCustomPlanCompletedCount(readerPlanId, idx + 1, false) else wirdViewModel.setCompletedWirdsCount(idx + 1, false) },
                                    onSetDimmerAlpha = viewModel::setReaderDimmerAlpha, onSetKeepScreenOn = viewModel::setKeepScreenOn,
                                    onPlayAyah = viewModel::playAyah, onPlayPageFirstAyah = viewModel::playCurrentPageFirstAyah,
                                    onTogglePlayPause = viewModel::togglePlayPause, onNextAyah = viewModel::nextAyah,
                                    onPreviousAyah = viewModel::previousAyah, onToggleRepeat = viewModel::toggleRepeatAyah,
                                    onCycleSpeed = viewModel::cyclePlaybackSpeed,
                                    onSetSleepTimerMinutes = viewModel::setSleepTimerMinutes,
                                    onToggleStopAfterSurah = viewModel::toggleStopAfterSurah,
                                    onStartMemorizationLoop = viewModel::startMemorizationLoop,
                                    onClearMemorizationLoop = viewModel::clearMemorizationLoop,
                                    onDownloadSurahAudio = { uiState.currentPageSurah?.let { viewModel.extras.downloadSurahAudio(uiState.settings.selectedReciterId, it.number, it.ayahCount) } },
                                    onCancelSurahAudio = { uiState.currentPageSurah?.let { viewModel.extras.cancelSurahAudioDownload(uiState.settings.selectedReciterId, it.number, it.ayahCount) } },
                                    onDeleteSurahAudio = { uiState.currentPageSurah?.let { viewModel.extras.deleteSurahAudio(uiState.settings.selectedReciterId, it.number, it.ayahCount) } },
                                    onSeekAudio = viewModel::seekAudio, onStopAudio = viewModel::stopAudio, onSelectReciter = viewModel::setReciter,
                                    playbackNotifActions = notifActions, onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }

                    if (useNavigationRail && !isReaderVisible) {
                        MiniAudioPlayerBar(
                            playbackState = playbackState, onTogglePlayPause = viewModel::togglePlayPause,
                            onNextAyah = viewModel::nextAyah, onPreviousAyah = viewModel::previousAyah,
                            onToggleRepeat = viewModel::toggleRepeatAyah, onOpenReciterPicker = { showReciterSheet = true },
                            onOpenPlaylists = { showPlaylistsQuickPicker = true }, onJumpToMushaf = jumpToPlayingAyah,
                            onClose = viewModel::stopAudio,
                            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 8.dp).quranFloatingChromeOverlay()
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !useNavigationRail && !isReaderVisible,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().quranFloatingChromeOverlay()
            ) {
                Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                    ShellMiniAudioBarHost(viewModel = viewModel, onOpenReciterPicker = { showReciterSheet = true }, onOpenPlaylists = { showPlaylistsQuickPicker = true }, onJumpToMushaf = jumpToPlayingAyah)
                    QuranBottomNavBar(selectedTab = selectedTab, onSelectTab = ::navigateToTab)
                }
            }
        }
    }
    }

    if (showReciterSheet) {
        ReciterPickerSheet(
            selectedReciterId = uiState.settings.selectedReciterId, activeRiwayah = uiState.settings.riwayah,
            onSelectReciter = viewModel::setReciter, onDismiss = { showReciterSheet = false }
        )
    }
    if (showPlaylistsQuickPicker) {
        PlaylistQuickPickerSheet(
            playlists = playlistsState, surahsByNumber = remember(uiState.surahs) { uiState.surahs.associateBy { it.number } },
            playbackState = playbackState, actions = playlistActions, onDismiss = { showPlaylistsQuickPicker = false }
        )
    }
}
