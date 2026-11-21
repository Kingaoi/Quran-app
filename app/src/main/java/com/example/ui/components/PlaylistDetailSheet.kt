package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.audio.AudioPlaybackState
import com.example.data.audio.AudioPlaylist
import com.example.data.audio.AudioPlaylistTrack
import com.example.data.local.RiwayahEdition
import com.example.domain.model.QuranReciters
import com.example.domain.model.SurahInfo
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors
import com.example.ui.viewmodel.AudioPlaylistActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailSheet(
    playlist: AudioPlaylist,
    surahsByNumber: Map<Int, SurahInfo>,
    playbackState: AudioPlaybackState,
    activeRiwayah: RiwayahEdition,
    actions: AudioPlaylistActions,
    getPageForAyah: (Int, Int) -> Int? = { _, _ -> null },
    onOpenAyahInMushaf: (page: Int, surah: Int, ayah: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showEditMeta by remember { mutableStateOf(false) }
    var showAddTrack by remember { mutableStateOf(false) }
    var showReciterPicker by remember { mutableStateOf(false) }

    val session = playbackState.playlistSession
    val isThisPlaylistActive = playbackState.isActive && session?.playlistId == playlist.id
    val isThisPlaylistPlaying = isThisPlaylistActive && playbackState.isPlaying
    val qc = MaterialTheme.quranColors
    val preferredReciter = playlist.preferredReciterId?.let(QuranReciters::byId)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp).padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PlaylistMedallionIcon(playlist.iconKey, playlist.accentIndex, size = 48.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(playlist.titleAr, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val meta = buildString {
                        append("${playlist.tracks.size.toAppDigits()} مقطع")
                        if (playlist.subtitleAr.isNotBlank()) append(" • ${playlist.subtitleAr}")
                    }
                    Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = { showEditMeta = true }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.Edit, contentDescription = "تعديل القائمة", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { actions.onDeletePlaylist(playlist.id); onDismiss() }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف القائمة", tint = qc.danger)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = "إغلاق")
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { actions.onTogglePlayPlaylist(playlist) },
                    enabled = playlist.tracks.isNotEmpty(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("playlist_detail_play_all")
                ) {
                    Icon(if (isThisPlaylistPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isThisPlaylistPlaying) "إيقاف مؤقت" else if (isThisPlaylistActive) "متابعة القائمة" else "تشغيل القائمة", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { showAddTrack = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.heightIn(min = 48.dp).testTag("playlist_detail_add_track")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("إضافة مقطع", fontWeight = FontWeight.Bold)
                }
            }

            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = playlist.repeatPlaylist,
                    onClick = { actions.onToggleRepeat(playlist.id) },
                    colors = quranFilterChipColors(),
                    border = quranFilterChipBorder(playlist.repeatPlaylist),
                    leadingIcon = { Icon(Icons.Rounded.Repeat, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("تكرار القائمة") }
                )
                FilterChip(
                    selected = playlist.shuffleEnabled,
                    onClick = { actions.onToggleShuffle(playlist.id) },
                    colors = quranFilterChipColors(),
                    border = quranFilterChipBorder(playlist.shuffleEnabled),
                    leadingIcon = { Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("ترتيب عشوائي") }
                )
                FilterChip(
                    selected = preferredReciter != null,
                    onClick = { showReciterPicker = true },
                    colors = quranFilterChipColors(),
                    border = quranFilterChipBorder(preferredReciter != null),
                    leadingIcon = { Icon(Icons.Rounded.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text(preferredReciter?.nameAr ?: "القارئ الافتراضي") }
                )
                if (preferredReciter != null) {
                    FilterChip(
                        selected = false,
                        onClick = { actions.onSetPreferredReciter(playlist.id, null) },
                        label = { Text("إلغاء تخصيص القارئ") }
                    )
                }
            }

            if (playlist.tracks.isEmpty()) {
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "القائمة فارغة حالياً. اضغط «إضافة مقطع» لاختيار سورة كاملة أو نطاق آيات محدد.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(playlist.tracks, key = { _, t -> t.id }) { index, track ->
                        val surah = surahsByNumber[track.surahNumber]
                        val isTrackPlaying = isThisPlaylistActive && session?.currentTrack?.id == track.id
                        PlaylistTrackRowCard(
                            index = index,
                            total = playlist.tracks.size,
                            track = track,
                            surah = surah,
                            isCurrent = isTrackPlaying,
                            onPlayTrack = { actions.onPlayPlaylist(playlist, index) },
                            onOpenInMushaf = {
                                surah?.let {
                                    val page = getPageForAyah(it.number, track.startAyah) ?: it.firstPage
                                    onOpenAyahInMushaf(page, it.number, track.startAyah)
                                    onDismiss()
                                }
                            },
                            onMoveUp = { actions.onMoveTrack(playlist.id, index, index - 1) },
                            onMoveDown = { actions.onMoveTrack(playlist.id, index, index + 1) },
                            onRemove = { actions.onRemoveTrack(playlist.id, track.id) }
                        )
                    }
                }
            }
        }
    }

    if (showEditMeta) {
        CreateEditPlaylistSheet(
            existingPlaylist = playlist,
            onSave = { title, sub, icon, accent -> actions.onUpdatePlaylistMeta(playlist.id, title, sub, icon, accent) },
            onDismiss = { showEditMeta = false }
        )
    }
    if (showAddTrack) {
        AddPlaylistTrackSheet(
            surahs = surahsByNumber.values.sortedBy { it.number },
            onAddTrack = { track -> actions.onAddTrack(playlist.id, track) },
            onDismiss = { showAddTrack = false }
        )
    }
    if (showReciterPicker) {
        ReciterPickerSheet(
            selectedReciterId = playlist.preferredReciterId ?: playbackState.reciter.id,
            activeRiwayah = activeRiwayah,
            onSelectReciter = { recId -> actions.onSetPreferredReciter(playlist.id, recId); showReciterPicker = false },
            onDismiss = { showReciterPicker = false }
        )
    }
}

@Composable
private fun PlaylistTrackRowCard(
    index: Int,
    total: Int,
    track: AudioPlaylistTrack,
    surah: SurahInfo?,
    isCurrent: Boolean,
    onPlayTrack: () -> Unit,
    onOpenInMushaf: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    Surface(
        onClick = onPlayTrack,
        shape = RoundedCornerShape(14.dp),
        color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = CircleShape, color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.size(30.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    if (isCurrent) Icon(Icons.Rounded.GraphicEq, contentDescription = "يُتلى الآن", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                    else Text((index + 1).toAppDigits(), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("سورة ${surah?.nameAr ?: track.surahNumber}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(track.displayRangeAr(surah?.ayahCount ?: track.endAyah), style = MaterialTheme.typography.labelSmall, color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            IconButton(onClick = onOpenInMushaf, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = "فتح في المصحف", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
            }
            if (index > 0) {
                IconButton(onClick = onMoveUp, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Rounded.ArrowUpward, contentDescription = "تحريك لأعلى", modifier = Modifier.size(16.dp))
                }
            }
            if (index < total - 1) {
                IconButton(onClick = onMoveDown, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Rounded.ArrowDownward, contentDescription = "تحريك لأسفل", modifier = Modifier.size(16.dp))
                }
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = "إزالة المقطع", tint = qc.danger, modifier = Modifier.size(17.dp))
            }
        }
    }
}
