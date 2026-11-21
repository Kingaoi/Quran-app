package com.example.ui.screens.index

import androidx.compose.runtime.Composable
import com.example.data.audio.AudioPlaybackState
import com.example.data.audio.AudioPlaylist
import com.example.data.local.RiwayahEdition
import com.example.domain.model.SurahInfo
import com.example.ui.components.CreateEditPlaylistSheet
import com.example.ui.components.PlaylistDetailSheet
import com.example.ui.components.PlaylistQuickPickerSheet
import com.example.ui.viewmodel.AudioPlaylistActions

@Composable
internal fun IndexPlaylistsModalsHost(
    playlists: List<AudioPlaylist>,
    surahsByNumber: Map<Int, SurahInfo>,
    playbackState: AudioPlaybackState,
    activeRiwayah: RiwayahEdition,
    actions: AudioPlaylistActions,
    inspectedPlaylistId: String?,
    showCreatePlaylistSheet: Boolean,
    quickAddSurah: SurahInfo?,
    getPageForAyah: (Int, Int) -> Int? = { _, _ -> null },
    onOpenAyahInMushaf: (page: Int, surah: Int, ayah: Int) -> Unit,
    onAddedSurahToast: (String) -> Unit,
    onDismissDetail: () -> Unit,
    onDismissCreate: () -> Unit,
    onDismissQuickAdd: () -> Unit
) {
    val activePlaylist = inspectedPlaylistId?.let { id -> playlists.firstOrNull { it.id == id } }
    if (activePlaylist != null) {
        PlaylistDetailSheet(
            playlist = activePlaylist,
            surahsByNumber = surahsByNumber,
            playbackState = playbackState,
            activeRiwayah = activeRiwayah,
            actions = actions,
            getPageForAyah = getPageForAyah,
            onOpenAyahInMushaf = onOpenAyahInMushaf,
            onDismiss = onDismissDetail
        )
    }

    if (showCreatePlaylistSheet) {
        CreateEditPlaylistSheet(
            onSave = { title, sub, icon, accent ->
                actions.onCreatePlaylist(title, sub, icon, accent, emptyList())
            },
            onDismiss = onDismissCreate
        )
    }

    if (quickAddSurah != null) {
        PlaylistQuickPickerSheet(
            playlists = playlists,
            surahsByNumber = surahsByNumber,
            playbackState = playbackState,
            actions = actions,
            quickAddSurah = quickAddSurah,
            onAddedSurahToast = onAddedSurahToast,
            onDismiss = onDismissQuickAdd
        )
    }
}
