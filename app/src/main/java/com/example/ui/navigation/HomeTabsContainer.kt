package com.example.ui.navigation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.data.audio.AudioPlaybackState
import com.example.data.audio.AudioPlaylist
import com.example.data.local.AyahBookmark
import com.example.data.local.ReadingStats
import com.example.domain.model.AyahItem
import com.example.domain.model.DhikrCategory
import com.example.domain.model.DhikrItem
import com.example.domain.model.DownloadHubActions
import com.example.domain.model.OfflineInventoryUiState
import com.example.domain.model.SearchHit
import com.example.domain.model.TafsirOfflineState
import com.example.domain.prayer.CityLocation
import com.example.domain.prayer.HijriSunnahConfig
import com.example.domain.prayer.PrayerName
import com.example.domain.prayer.PrayerTickerState
import com.example.domain.quran.QuranConcordanceReport
import com.example.domain.quran.QuranSearchMode
import com.example.domain.wird.WirdPortion
import com.example.ui.screens.AdhkarScreen
import com.example.ui.screens.CustomWirdPlanActions
import com.example.ui.screens.IndexScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.PrayerTimesScreen
import com.example.ui.screens.WirdScreen
import com.example.ui.screens.more.MoreSettingsActions
import com.example.ui.viewmodel.AdhkarUiState
import com.example.ui.viewmodel.AudioPlaylistActions
import com.example.ui.viewmodel.OfflineDownloadState
import com.example.ui.viewmodel.PrayerUiState
import com.example.ui.viewmodel.QuranUiState
import com.example.ui.viewmodel.WirdUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun HomeTabsContainer(
    selectedTab: HomeTab,
    isHomeVisible: Boolean = true,
    openAllWirdPlansRequestCount: Int = 0,
    openHijriSheetRequestCount: Int = 0,
    uiState: QuranUiState,
    offlineState: OfflineDownloadState,
    wirdState: WirdUiState,
    adhkarState: AdhkarUiState,
    prayerState: PrayerUiState,
    prayerTickerFlow: StateFlow<PrayerTickerState>? = null,
    statsState: ReadingStats,
    bookmarksState: List<AyahBookmark>,
    recentSearchesState: List<String> = emptyList(),
    playbackState: AudioPlaybackState = AudioPlaybackState(),
    playlists: List<AudioPlaylist> = emptyList(),
    hiddenPresetsCount: Int = 0,
    playlistActions: AudioPlaylistActions = AudioPlaylistActions(),
    tafsirOfflineState: TafsirOfflineState,
    offlineHubState: OfflineInventoryUiState,
    downloadHubActions: DownloadHubActions,
    moreActions: MoreSettingsActions,
    getAyahsForPage: (Int) -> List<AyahItem>, getPageForAyah: (Int, Int) -> Int? = { _, _ -> null },
    onOpenPage: (Int) -> Unit,
    onOpenWirdPage: (Int, Int) -> Unit,
    onOpenAyah: (Int, Int, Int) -> Unit,
    onOpenSurah: (Int) -> Unit,
    onTogglePlaySurah: (Int) -> Unit = {},
    onTogglePlayWird: (WirdPortion) -> Unit = {},
    onSearchQuran: (String) -> List<SearchHit>,
    onSearchConcordance: (String, QuranSearchMode) -> QuranConcordanceReport = { q, m -> QuranConcordanceReport.empty(q, m) },
    onRecordSearch: (String) -> Unit = {},
    onRemoveRecentSearch: (String) -> Unit = {},
    onClearRecentSearches: () -> Unit = {},
    onDeleteAyahBookmark: (String) -> Unit,
    onTogglePageBookmark: (Int) -> Unit,
    onStartNewKhatmah: (Int, Int) -> Unit,
    onCompleteCurrentWird: () -> Unit,
    onUndoCompleteWird: () -> Unit,
    onSetWirdCompletedCount: (Int) -> Unit,
    onSetDailyGoalPages: (Int) -> Unit = {},
    onClearWirdStatus: () -> Unit,
    customWirdActions: CustomWirdPlanActions = CustomWirdPlanActions(),
    onOpenAdhkarCategory: (DhikrCategory) -> Unit,
    onOpenAdhkarItem: (DhikrItem) -> Unit = { onOpenAdhkarCategory(it.category) },
    onRefreshAdhkarSource: () -> Unit,
    onClearAdhkarSyncStatus: () -> Unit,
    onAdhkarSearchQueryChange: (String) -> Unit,
    onToggleAdhkarFavoritesOnly: () -> Unit,
    onToggleAdhkarFavorite: (String) -> Unit,
    onAddCustomDua: (String, String, Int, String) -> Unit,
    onDeleteCustomDua: (String) -> Unit,
    onTogglePlayDhikrAudio: (DhikrItem) -> Unit,
    onIncrementTasbih: () -> Unit,
    onAddCustomTasbihPhrase: (String) -> Unit,
    onRemoveCustomTasbihPhrase: (String) -> Unit,
    onShiftPrayerDay: (Int) -> Unit,
    onSelectCity: (CityLocation) -> Unit,
    onDetectGpsLocation: () -> Unit,
    onTogglePrayerAlert: (PrayerName) -> Unit,
    onUpdateHijriSunnah: ((HijriSunnahConfig) -> HijriSunnahConfig) -> Unit = {},
    onToggleRawatibItem: (String) -> Unit = {},
    onClearPrayerStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val saveableStateHolder = rememberSaveableStateHolder()
    val transition = updateTransition(targetState = selectedTab, label = "home_tab_transition")
    val isTransitioning = transition.currentState != transition.targetState
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val logicalDirection = if (transition.targetState.ordinal >= transition.currentState.ordinal) 1 else -1
    val visualDirection = if (isRtl) -logicalDirection else logicalDirection

    Box(modifier = modifier.fillMaxSize()) {
        HomeTab.entries.forEach { tab ->
            key(tab) {
                val isTarget = tab == transition.targetState
                val isOutgoing = tab == transition.currentState && isTransitioning
                val shouldRender = isTarget || isOutgoing
                val enterProgress = transition.animateFloat(
                    transitionSpec = { tween(durationMillis = 190, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)) },
                    label = "tab_enter_progress"
                ) { state ->
                    if (state == tab) 1f else 0f
                }

                if (shouldRender) {
                    saveableStateHolder.SaveableStateProvider(key = tab) {
                        TabLayerSlot(
                            isActive = isTarget,
                            isTransitioning = isTransitioning,
                            directionSign = visualDirection,
                            progressState = enterProgress
                        ) {
                            when (tab) {
                            HomeTab.WIRD -> WirdScreen(
                                uiState = wirdState,
                                stats = statsState,
                                playbackState = playbackState,
                                isTabActive = isHomeVisible && isTarget,
                                openAllPlansRequestCount = openAllWirdPlansRequestCount,
                                onStartNewKhatmah = onStartNewKhatmah,
                                onCompleteCurrentWird = onCompleteCurrentWird,
                                onUndoCompleteWird = onUndoCompleteWird,
                                onSetCompletedCount = onSetWirdCompletedCount,
                                onOpenPageInReader = onOpenWirdPage,
                                onTogglePlayWird = onTogglePlayWird,
                                onSetDailyGoalPages = onSetDailyGoalPages,
                                onClearStatusMessage = onClearWirdStatus,
                                customPlanActions = customWirdActions
                            )

                            HomeTab.ADHKAR -> AdhkarScreen(
                                uiState = adhkarState,
                                stats = statsState,
                                isTabActive = isHomeVisible && isTarget,
                                onOpenCategory = onOpenAdhkarCategory,
                                onOpenDhikrItem = onOpenAdhkarItem,
                                onRefreshSource = onRefreshAdhkarSource,
                                onClearSyncStatus = onClearAdhkarSyncStatus,
                                onSearchQueryChange = onAdhkarSearchQueryChange,
                                onToggleFavoritesOnly = onToggleAdhkarFavoritesOnly,
                                onToggleFavorite = onToggleAdhkarFavorite,
                                onAddCustomDua = onAddCustomDua,
                                onDeleteCustomDua = onDeleteCustomDua,
                                onTogglePlayAudio = onTogglePlayDhikrAudio,
                                onIncrementTasbih = onIncrementTasbih,
                                onAddCustomTasbihPhrase = onAddCustomTasbihPhrase,
                                onRemoveCustomTasbihPhrase = onRemoveCustomTasbihPhrase
                            )

                            HomeTab.PRAYER_TIMES -> PrayerTimesScreen(
                                uiState = prayerState,
                                stats = statsState,
                                isTabActive = isHomeVisible && isTarget,
                                openHijriSheetRequestCount = openHijriSheetRequestCount,
                                onShiftDay = onShiftPrayerDay,
                                onSelectCity = onSelectCity,
                                onDetectGpsLocation = onDetectGpsLocation,
                                onTogglePrayerAlert = onTogglePrayerAlert,
                                onUpdateHijriSunnah = onUpdateHijriSunnah,
                                onToggleRawatibItem = onToggleRawatibItem,
                                onClearStatusMessage = onClearPrayerStatus,
                                tickerStateFlow = prayerTickerFlow
                            )

                            HomeTab.INDEX -> IndexScreen(
                                uiState = uiState,
                                bookmarks = bookmarksState,
                                recentSearches = recentSearchesState,
                                playbackState = playbackState,
                                playlists = playlists,
                                hiddenPresetsCount = hiddenPresetsCount,
                                playlistActions = playlistActions,
                                getAyahsForPage = getAyahsForPage, getPageForAyah = getPageForAyah,
                                onOpenPage = onOpenPage,
                                onOpenAyah = onOpenAyah,
                                onOpenSurah = onOpenSurah,
                                onTogglePlaySurah = onTogglePlaySurah,
                                onSearchQuran = onSearchQuran,
                                onSearchConcordance = onSearchConcordance,
                                onRecordSearch = onRecordSearch,
                                onRemoveRecentSearch = onRemoveRecentSearch,
                                onClearRecentSearches = onClearRecentSearches,
                                onDeleteAyahBookmark = onDeleteAyahBookmark,
                                onTogglePageBookmark = onTogglePageBookmark
                            )

                            HomeTab.MORE -> MoreScreen(
                                uiState = uiState,
                                offlineState = offlineState,
                                adhkarState = adhkarState,
                                prayerState = prayerState,
                                wirdState = wirdState,
                                actions = moreActions,
                                isTabActive = isHomeVisible && isTarget,
                                tafsirOfflineState = tafsirOfflineState,
                                offlineHubState = offlineHubState,
                                downloadHubActions = downloadHubActions
                            )
                        }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TabLayerSlot(
    isActive: Boolean,
    isTransitioning: Boolean,
    directionSign: Int,
    progressState: State<Float>,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(if (isActive) 1f else 0f)
            .focusProperties { canFocus = isActive }
            .semantics {
                if (!isActive) invisibleToUser()
            }
            .graphicsLayer {
                compositingStrategy = if (isTransitioning) {
                    CompositingStrategy.ModulateAlpha
                } else {
                    CompositingStrategy.Auto
                }
                if (isActive && isTransitioning) {
                    val p = progressState.value.coerceIn(0f, 1f)
                    val s = 0.985f + 0.015f * p
                    scaleX = s
                    scaleY = s
                    alpha = p
                    translationX = directionSign * 14.dp.toPx() * (1f - p)
                } else {
                    scaleX = 1f
                    scaleY = 1f
                    alpha = 1f
                    translationX = 0f
                }
            }
    ) {
        content()
    }
}
