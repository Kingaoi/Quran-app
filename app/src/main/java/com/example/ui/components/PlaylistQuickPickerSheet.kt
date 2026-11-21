package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.audio.AudioPlaybackState
import com.example.data.audio.AudioPlaylist
import com.example.data.audio.AudioPlaylistTrack
import com.example.domain.model.SurahInfo
import com.example.domain.model.toAppDigits
import com.example.ui.viewmodel.AudioPlaylistActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistQuickPickerSheet(
    playlists: List<AudioPlaylist>,
    surahsByNumber: Map<Int, SurahInfo>,
    playbackState: AudioPlaybackState,
    actions: AudioPlaylistActions,
    quickAddSurah: SurahInfo? = null,
    quickAddAyah: com.example.domain.model.AyahItem? = null,
    onAddedSurahToast: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCreateSheet by remember { mutableStateOf(false) }
    val activeSession = playbackState.playlistSession
    val targetSurahNum = quickAddAyah?.surahNumber ?: quickAddSurah?.number
    val maxSurahAyah = targetSurahNum?.let { surahsByNumber[it]?.ayahCount } ?: quickAddAyah?.ayahNumber ?: 1
    var endAyah by remember(quickAddAyah) {
        mutableStateOf((quickAddAyah?.ayahNumber ?: 1).coerceIn(1, maxSurahAyah.coerceAtLeast(1)))
    }
    val isAddMode = quickAddSurah != null || quickAddAyah != null

    fun buildTrackToAdd(): AudioPlaylistTrack? = when {
        quickAddAyah != null -> AudioPlaylistTrack(surahNumber = quickAddAyah.surahNumber, startAyah = quickAddAyah.ayahNumber, endAyah = endAyah.coerceIn(quickAddAyah.ayahNumber, maxSurahAyah))
        quickAddSurah != null -> AudioPlaylistTrack(surahNumber = quickAddSurah.number, startAyah = 1, endAyah = quickAddSurah.ayahCount)
        else -> null
    }

    fun addedLabel(): String = when {
        quickAddAyah != null && endAyah > quickAddAyah.ayahNumber ->
            "الآيات ${quickAddAyah.ayahNumber.toAppDigits()}–${endAyah.toAppDigits()} من سورة ${quickAddAyah.surahNameAr}"
        quickAddAyah != null -> "آية ${quickAddAyah.ayahNumber.toAppDigits()} من سورة ${quickAddAyah.surahNameAr}"
        quickAddSurah != null -> "سورة ${quickAddSurah.nameAr}"
        else -> ""
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp).padding(bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            quickAddAyah != null -> "إضافة من سورة ${quickAddAyah.surahNameAr} للقائمة"
                            quickAddSurah != null -> "إضافة سورة ${quickAddSurah.nameAr} إلى قائمة"
                            else -> "قوائم التلاوة الصوتية"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when {
                            quickAddAyah != null -> "حدد نهاية المقطع ثم اختر القائمة"
                            quickAddSurah != null -> "اختر القائمة لإضافة السورة كاملة"
                            else -> "اختر قائمة للاستماع المتتابع"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = "إغلاق")
                }
            }

            if (quickAddAyah != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (endAyah == quickAddAyah.ayahNumber) "الآية ${quickAddAyah.ayahNumber.toAppDigits()} فقط"
                            else "من الآية ${quickAddAyah.ayahNumber.toAppDigits()} إلى ${endAyah.toAppDigits()}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { if (endAyah > quickAddAyah.ayahNumber) endAyah-- },
                                enabled = endAyah > quickAddAyah.ayahNumber,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) { Text("−", fontWeight = FontWeight.Bold) }
                            OutlinedButton(
                                onClick = { if (endAyah < maxSurahAyah) endAyah++ },
                                enabled = endAyah < maxSurahAyah,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) { Text("+", fontWeight = FontWeight.Bold) }
                            if (maxSurahAyah > quickAddAyah.ayahNumber) {
                                OutlinedButton(
                                    onClick = { endAyah = if (endAyah == maxSurahAyah) quickAddAyah.ayahNumber else maxSurahAyah },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) { Text(if (endAyah == maxSurahAyah) "آية فقط" else "لنهاية السورة") }
                            }
                        }
                    }
                }
            }

            if (!isAddMode && activeSession != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("قيد التشغيل: ${activeSession.playlistTitleAr}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("المقطع ${(activeSession.currentTrackIndex + 1).toAppDigits()} من ${activeSession.totalTracks.toAppDigits()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = actions.onPreviousTrack, enabled = activeSession.hasPrevious) {
                                Icon(Icons.Rounded.SkipPrevious, contentDescription = "المقطع السابق")
                            }
                            IconButton(onClick = actions.onNextTrack, enabled = activeSession.hasNext) {
                                Icon(Icons.Rounded.SkipNext, contentDescription = "المقطع التالي")
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                contentPadding = PaddingValues(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(playlists, key = { it.id }) { playlist ->
                    val isActive = playbackState.isActive && activeSession?.playlistId == playlist.id
                    val isPlaying = isActive && playbackState.isPlaying
                    Surface(
                        onClick = {
                            val trackToAdd = buildTrackToAdd()
                            if (trackToAdd != null) {
                                actions.onAddTrack(playlist.id, trackToAdd)
                                onAddedSurahToast("تمت إضافة ${addedLabel()} إلى ${playlist.titleAr}")
                                onDismiss()
                            } else {
                                actions.onTogglePlayPlaylist(playlist)
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PlaylistMedallionIcon(playlist.iconKey, playlist.accentIndex, size = 40.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(playlist.titleAr, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${playlist.tracks.size.toAppDigits()} مقطع • ${formatPlaylistTracksPreview(playlist.tracks, surahsByNumber)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            if (isAddMode) {
                                Icon(Icons.Rounded.Add, contentDescription = "إضافة", tint = MaterialTheme.colorScheme.primary)
                            } else {
                                Surface(shape = CircleShape, color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(36.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                            contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                                            tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { showCreateSheet = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (isAddMode) "إنشاء قائمة جديدة بهذا المقطع" else "إنشاء قائمة جديدة", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showCreateSheet) {
        CreateEditPlaylistSheet(
            onSave = { title, sub, icon, accent ->
                val initial = listOfNotNull(buildTrackToAdd())
                actions.onCreatePlaylist(title, sub, icon, accent, initial)
                if (isAddMode) {
                    onAddedSurahToast("تم إنشاء قائمة «$title» وإضافة ${addedLabel()}")
                    onDismiss()
                }
            },
            onDismiss = { showCreateSheet = false }
        )
    }
}
