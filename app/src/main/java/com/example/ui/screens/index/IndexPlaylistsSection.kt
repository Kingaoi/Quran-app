package com.example.ui.screens.index

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.audio.AudioPlaybackState
import com.example.data.audio.AudioPlaylist
import com.example.domain.model.QuranReciters
import com.example.domain.model.SurahInfo
import com.example.domain.model.toAppDigits
import com.example.ui.components.PlaylistMedallionIcon
import com.example.ui.components.formatPlaylistTracksPreview
import com.example.ui.theme.quranColors

internal fun LazyListScope.indexPlaylistsSection(
    playlists: List<AudioPlaylist>,
    surahsByNumber: Map<Int, SurahInfo>,
    playbackState: AudioPlaybackState,
    hiddenPresetsCount: Int,
    onCreatePlaylist: () -> Unit,
    onOpenPlaylistDetail: (AudioPlaylist) -> Unit,
    onTogglePlayPlaylist: (AudioPlaylist) -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onRestorePresets: () -> Unit
) {
    val activeSession = playbackState.playlistSession
    val customPlaylists = playlists.filterNot { it.isPreset }
    val presetPlaylists = playlists.filter { it.isPreset }

    item(key = "playlists_hero_banner") {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("قوائم التلاوة الصوتية", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Text("استماع متتابع للسور أو مقاطع الآيات المختارة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = onCreatePlaylist,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("create_playlist_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("جديدة", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (playbackState.isActive && activeSession != null) {
        val activePlaylist = playlists.firstOrNull { it.id == activeSession.playlistId }
        item(key = "active_playlist_banner") {
            Surface(
                onClick = { activePlaylist?.let(onOpenPlaylistDetail) },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "يُتلى الآن: ${activeSession.playlistTitleAr}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "سورة ${playbackState.surahNameAr} • المقطع ${(activeSession.currentTrackIndex + 1).toAppDigits()} من ${activeSession.totalTracks.toAppDigits()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onPreviousTrack, enabled = activeSession.hasPrevious, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Rounded.SkipPrevious, contentDescription = "المقطع السابق")
                        }
                        IconButton(onClick = onNextTrack, enabled = activeSession.hasNext, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Rounded.SkipNext, contentDescription = "المقطع التالي")
                        }
                    }
                }
            }
        }
    }

    if (customPlaylists.isNotEmpty()) {
        item(key = "custom_playlists_header") {
            PlaylistSectionLabel("قوائمي الخاصة (${customPlaylists.size.toAppDigits()})")
        }
        items(customPlaylists, key = { "pl_${it.id}" }) { playlist ->
            PlaylistCardItem(
                playlist = playlist,
                surahsByNumber = surahsByNumber,
                isActive = playbackState.isActive && activeSession?.playlistId == playlist.id,
                isPlaying = playbackState.isActive && activeSession?.playlistId == playlist.id && playbackState.isPlaying,
                onOpen = { onOpenPlaylistDetail(playlist) },
                onTogglePlay = { onTogglePlayPlaylist(playlist) }
            )
        }
    }

    if (presetPlaylists.isNotEmpty()) {
        item(key = "preset_playlists_header") {
            PlaylistSectionLabel("قوائم مختارة (${presetPlaylists.size.toAppDigits()})")
        }
        items(presetPlaylists, key = { "pl_${it.id}" }) { playlist ->
            PlaylistCardItem(
                playlist = playlist,
                surahsByNumber = surahsByNumber,
                isActive = playbackState.isActive && activeSession?.playlistId == playlist.id,
                isPlaying = playbackState.isActive && activeSession?.playlistId == playlist.id && playbackState.isPlaying,
                onOpen = { onOpenPlaylistDetail(playlist) },
                onTogglePlay = { onTogglePlayPlaylist(playlist) }
            )
        }
    }

    if (hiddenPresetsCount > 0) {
        item(key = "restore_presets_row") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = onRestorePresets) {
                    Icon(Icons.Rounded.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("استعادة القوائم المختارة المحذوفة")
                }
            }
        }
    }
}

@Composable
private fun PlaylistSectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
    )
}

@Composable
private fun PlaylistCardItem(
    playlist: AudioPlaylist,
    surahsByNumber: Map<Int, SurahInfo>,
    isActive: Boolean,
    isPlaying: Boolean,
    onOpen: () -> Unit,
    onTogglePlay: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val accent = qc.categoryPastelAt(playlist.accentIndex)
    val reciterName = playlist.preferredReciterId?.let { QuranReciters.byId(it).nameAr }
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(18.dp),
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().testTag("playlist_card_${playlist.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PlaylistMedallionIcon(playlist.iconKey, playlist.accentIndex, size = 46.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = playlist.titleAr,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(shape = RoundedCornerShape(50), color = accent.copy(alpha = 0.16f)) {
                        Text(
                            text = "${playlist.tracks.size.toAppDigits()} مقطع",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = accent,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = formatPlaylistTracksPreview(playlist.tracks, surahsByNumber),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (playlist.repeatPlaylist || playlist.shuffleEnabled || reciterName != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (playlist.repeatPlaylist) Icon(Icons.Rounded.Repeat, contentDescription = "تكرار", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        if (playlist.shuffleEnabled) Icon(Icons.Rounded.Shuffle, contentDescription = "عشوائي", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                        if (reciterName != null) Text(reciterName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Surface(
                onClick = onTogglePlay,
                enabled = playlist.tracks.isNotEmpty(),
                shape = CircleShape,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f),
                border = if (isActive) null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                modifier = Modifier.size(44.dp).minimumInteractiveComponentSize().testTag("playlist_play_${playlist.id}")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "إيقاف ${playlist.titleAr}" else "تشغيل ${playlist.titleAr}",
                        tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
