package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import com.example.data.audio.AudioPlaylist
import com.example.data.audio.AudioPlaylistRepository
import com.example.data.audio.AudioPlaylistTrack
import com.example.data.audio.PlaylistIconKey
import com.example.data.audio.PresetAudioPlaylists
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

@Immutable
data class AudioPlaylistActions(
    val onPlayPlaylist: (AudioPlaylist, Int) -> Unit = { _, _ -> },
    val onTogglePlayPlaylist: (AudioPlaylist) -> Unit = {},
    val onNextTrack: () -> Unit = {},
    val onPreviousTrack: () -> Unit = {},
    val onCreatePlaylist: (title: String, subtitle: String, iconKey: PlaylistIconKey, accentIndex: Int, initialTracks: List<AudioPlaylistTrack>) -> Unit = { _, _, _, _, _ -> },
    val onUpdatePlaylistMeta: (id: String, title: String, subtitle: String, iconKey: PlaylistIconKey, accentIndex: Int) -> Unit = { _, _, _, _, _ -> },
    val onDeletePlaylist: (String) -> Unit = {},
    val onAddTrack: (playlistId: String, track: AudioPlaylistTrack) -> Unit = { _, _ -> },
    val onRemoveTrack: (playlistId: String, trackId: String) -> Unit = { _, _ -> },
    val onMoveTrack: (playlistId: String, fromIndex: Int, toIndex: Int) -> Unit = { _, _, _ -> },
    val onToggleRepeat: (String) -> Unit = {},
    val onToggleShuffle: (String) -> Unit = {},
    val onSetPreferredReciter: (String, String?) -> Unit = { _, _ -> },
    val onRestorePresets: () -> Unit = {}
)

class AudioPlaylistDelegate(
    private val repository: AudioPlaylistRepository,
    private val scope: CoroutineScope
) {
    constructor(application: Application, scope: CoroutineScope) : this(
        repository = AudioPlaylistRepository(application),
        scope = scope
    )

    val playlistsState: StateFlow<List<AudioPlaylist>> = repository.playlistsFlow
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), PresetAudioPlaylists.all)

    val hiddenPresetsCountState: StateFlow<Int> = repository.hiddenPresetsCountFlow
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), 0)

    fun createPlaylist(
        title: String,
        subtitle: String,
        iconKey: PlaylistIconKey,
        accentIndex: Int,
        initialTracks: List<AudioPlaylistTrack> = emptyList()
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        val playlist = AudioPlaylist(
            id = "custom_${UUID.randomUUID()}",
            titleAr = cleanTitle,
            subtitleAr = subtitle.trim(),
            iconKey = iconKey,
            accentIndex = accentIndex,
            isPreset = false,
            tracks = initialTracks
        )
        scope.launch { repository.upsertPlaylist(playlist) }
    }

    fun updatePlaylistMeta(
        id: String,
        title: String,
        subtitle: String,
        iconKey: PlaylistIconKey,
        accentIndex: Int
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        scope.launch {
            repository.updatePlaylistById(id) {
                it.copy(titleAr = cleanTitle, subtitleAr = subtitle.trim(), iconKey = iconKey, accentIndex = accentIndex)
            }
        }
    }

    fun deletePlaylist(playlistId: String) {
        scope.launch { repository.deletePlaylist(playlistId) }
    }

    fun addTrack(playlistId: String, track: AudioPlaylistTrack) {
        val uniqueTrack = track.copy(id = "trk_${UUID.randomUUID()}")
        scope.launch {
            repository.updatePlaylistById(playlistId) { current ->
                current.copy(tracks = current.tracks + uniqueTrack)
            }
        }
    }

    fun removeTrack(playlistId: String, trackId: String) {
        scope.launch {
            repository.updatePlaylistById(playlistId) { current ->
                current.copy(tracks = current.tracks.filterNot { it.id == trackId })
            }
        }
    }

    fun moveTrack(playlistId: String, fromIndex: Int, toIndex: Int) {
        scope.launch {
            repository.updatePlaylistById(playlistId) { current ->
                if (fromIndex !in current.tracks.indices || toIndex !in current.tracks.indices || fromIndex == toIndex) {
                    current
                } else {
                    val mutable = current.tracks.toMutableList()
                    val item = mutable.removeAt(fromIndex)
                    mutable.add(toIndex, item)
                    current.copy(tracks = mutable)
                }
            }
        }
    }

    fun toggleRepeat(playlistId: String) {
        scope.launch {
            repository.updatePlaylistById(playlistId) { it.copy(repeatPlaylist = !it.repeatPlaylist) }
        }
    }

    fun toggleShuffle(playlistId: String) {
        scope.launch {
            repository.updatePlaylistById(playlistId) { it.copy(shuffleEnabled = !it.shuffleEnabled) }
        }
    }

    fun setPreferredReciter(playlistId: String, reciterId: String?) {
        scope.launch {
            repository.updatePlaylistById(playlistId) { it.copy(preferredReciterId = reciterId) }
        }
    }

    fun restorePresets() {
        scope.launch { repository.restorePresetPlaylists() }
    }
}
