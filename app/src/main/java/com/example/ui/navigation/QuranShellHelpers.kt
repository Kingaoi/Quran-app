package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.data.audio.WirdAudioRange
import com.example.domain.model.DhikrCategory
import com.example.domain.model.DhikrItem
import com.example.domain.wird.HusunActions
import com.example.domain.wird.WirdPortion
import com.example.ui.components.MiniAudioPlayerBar
import com.example.ui.screens.CustomWirdPlanActions
import com.example.ui.viewmodel.AdhkarViewModel
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.WirdViewModel

@Immutable
internal data class ShellNavigationCallbacks(
    val openPage: (Int) -> Unit,
    val openWirdPage: (Int, Int) -> Unit,
    val openAyah: (Int, Int, Int) -> Unit,
    val openSurah: (Int) -> Unit,
    val togglePlaySurah: (Int) -> Unit,
    val togglePlayWird: (WirdPortion) -> Unit,
    val openAdhkarCategory: (DhikrCategory) -> Unit,
    val openAdhkarItem: (DhikrItem) -> Unit,
    val customWirdActions: CustomWirdPlanActions
)

@Composable
internal fun rememberShellNavigationCallbacks(
    viewModel: QuranViewModel,
    wirdViewModel: WirdViewModel,
    adhkarViewModel: AdhkarViewModel,
    navController: NavHostController
): ShellNavigationCallbacks = remember(viewModel, wirdViewModel, adhkarViewModel, navController) {
    ShellNavigationCallbacks(
        openPage = { page ->
            viewModel.openPage(page)
            navController.navigate(readerRoute(fromWird = false)) { launchSingleTop = true }
        },
        openWirdPage = { page, wirdIdx ->
            viewModel.openPage(page)
            navController.navigate(readerRoute(fromWird = true, wirdIndex = wirdIdx)) { launchSingleTop = true }
        },
        openAyah = { page, surah, ayah ->
            viewModel.openAyah(page, surah, ayah)
            navController.navigate(readerRoute(fromWird = false)) { launchSingleTop = true }
        },
        openSurah = { surahNum ->
            viewModel.openSurah(surahNum)
            navController.navigate(readerRoute(fromWird = false)) { launchSingleTop = true }
        },
        togglePlaySurah = { surahNum ->
            val currentAudio = viewModel.playbackState.value
            if (currentAudio.isActive && currentAudio.currentSurah == surahNum &&
                currentAudio.wirdAudioRange == null && currentAudio.playlistSession == null && !currentAudio.memorizationLoop.enabled
            ) {
                viewModel.togglePlayPause()
            } else {
                viewModel.playAyah(surahNum, 1)
            }
        },
        togglePlayWird = { wird ->
            viewModel.togglePlayWirdRange(
                WirdAudioRange(
                    wirdIndex = wird.index,
                    startSurah = wird.startSurahNumber,
                    startAyah = wird.startAyahNumber,
                    endSurah = wird.endSurahNumber,
                    endAyah = wird.endAyahNumber
                )
            )
        },
        openAdhkarCategory = { category ->
            adhkarViewModel.selectCategory(category)
            val state = adhkarViewModel.uiState.value
            if (state.shouldOpenDirectlyInReader(category)) {
                navController.navigate(
                    adhkarDetailRoute(
                        categoryId = category.id,
                        chapterTitle = "",
                        initialIndex = state.firstIncompleteIndex(category)
                    )
                ) { launchSingleTop = true }
            } else {
                navController.navigate(adhkarBrowseRoute(category.id)) { launchSingleTop = true }
            }
        },
        openAdhkarItem = { item ->
            adhkarViewModel.selectCategory(item.category)
            val chapter = if (item.category.isSequentialLitany) "" else item.resolvedChapterTitle()
            val scopedIdx = adhkarViewModel.uiState.value
                .itemsForScope(item.category, chapter)
                .indexOfFirst { it.id == item.id }
                .coerceAtLeast(0)
            navController.navigate(adhkarDetailRoute(item.category.id, chapter, scopedIdx)) { launchSingleTop = true }
        },
        customWirdActions = CustomWirdPlanActions(
            onAddPlan = wirdViewModel::addCustomPlan,
            onUpdatePlan = wirdViewModel::updateCustomPlan,
            onCompletePlanWird = { id -> wirdViewModel.completeCustomPlanWird(id) },
            onSetPlanCompletedCount = { id, count -> wirdViewModel.setCustomPlanCompletedCount(id, count) },
            onDeletePlan = wirdViewModel::deleteCustomPlan,
            onTogglePlanReminder = wirdViewModel::toggleCustomPlanReminder,
            onOpenPlanPage = { page, idx, id ->
                viewModel.openPage(page)
                navController.navigate(readerRoute(fromWird = true, wirdIndex = idx, planId = id)) { launchSingleTop = true }
            },
            onToggleTimelinePortion = wirdViewModel::toggleTimelinePortion,
            onRescheduleOverdue = wirdViewModel::rescheduleOverdueSessions,
            onDeferToday = wirdViewModel::deferTodaySessions,
            onResetTimelineOverrides = wirdViewModel::resetTimelineOverrides,
            onUpdateMainReminder = wirdViewModel::updateReminder,
            husun = HusunActions(
                onConfigure = wirdViewModel::configureHusun,
                onToggleFortress = wirdViewModel::toggleHusunFortress,
                onIncrementRepeat = wirdViewModel::incrementHusunRepeat,
                onCompleteDay = wirdViewModel::completeHusunDay,
                onReset = wirdViewModel::resetHusun,
                onOpenReaderPage = { page ->
                    viewModel.openPage(page)
                    navController.navigate(readerRoute(fromWird = false)) { launchSingleTop = true }
                }
            )
        )
    )
}

@Composable
internal fun rememberAudioPlaylistActions(viewModel: QuranViewModel): com.example.ui.viewmodel.AudioPlaylistActions =
    remember(viewModel) {
        com.example.ui.viewmodel.AudioPlaylistActions(
            onPlayPlaylist = viewModel::playPlaylist,
            onTogglePlayPlaylist = viewModel::togglePlayPlaylist,
            onNextTrack = viewModel::nextPlaylistTrack,
            onPreviousTrack = viewModel::previousPlaylistTrack,
            onCreatePlaylist = viewModel.extras.playlists::createPlaylist,
            onUpdatePlaylistMeta = viewModel.extras.playlists::updatePlaylistMeta,
            onDeletePlaylist = viewModel.extras.playlists::deletePlaylist,
            onAddTrack = viewModel.extras.playlists::addTrack,
            onRemoveTrack = viewModel.extras.playlists::removeTrack,
            onMoveTrack = viewModel.extras.playlists::moveTrack,
            onToggleRepeat = viewModel.extras.playlists::toggleRepeat,
            onToggleShuffle = viewModel.extras.playlists::toggleShuffle,
            onSetPreferredReciter = viewModel.extras.playlists::setPreferredReciter,
            onRestorePresets = viewModel.extras.playlists::restorePresets
        )
    }

@Composable
internal fun ShellMiniAudioBarHost(
    viewModel: QuranViewModel,
    onOpenReciterPicker: () -> Unit,
    onOpenPlaylists: () -> Unit = {},
    onJumpToMushaf: (surah: Int, ayah: Int) -> Unit = { _, _ -> }
) {
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    MiniAudioPlayerBar(
        playbackState = playbackState,
        onTogglePlayPause = viewModel::togglePlayPause,
        onNextAyah = viewModel::nextAyah,
        onPreviousAyah = viewModel::previousAyah,
        onToggleRepeat = viewModel::toggleRepeatAyah,
        onOpenReciterPicker = onOpenReciterPicker,
        onOpenPlaylists = onOpenPlaylists,
        onJumpToMushaf = onJumpToMushaf,
        onClose = viewModel::stopAudio
    )
}
