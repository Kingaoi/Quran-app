package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.AudioPlaybackState
import com.example.data.audio.AudioPlaylist
import com.example.data.audio.PlaybackNotifActions
import com.example.data.audio.PlaybackNotificationConfig
import com.example.data.audio.SurahAudioDownloadState
import com.example.data.local.RiwayahEdition
import com.example.domain.model.QuranReciters
import com.example.domain.model.ReciterOption
import com.example.domain.model.SurahInfo
import com.example.domain.model.toAppDigits
import com.example.ui.screens.more.PlaybackNotifCustomizeSheet
import com.example.ui.viewmodel.AudioPlaylistActions

@Composable
fun MushafExpandedAudioSheet(
    visible: Boolean,
    playbackState: AudioPlaybackState,
    currentSurahName: String,
    currentPage: Int,
    activeRiwayah: RiwayahEdition = RiwayahEdition.HAFS,
    reciterCatalog: List<ReciterOption> = QuranReciters.combinedCatalog(),
    surahAudioState: SurahAudioDownloadState = SurahAudioDownloadState(),
    onTogglePlayPause: () -> Unit,
    onPlayPageFirstAyah: () -> Unit,
    onNextAyah: () -> Unit,
    onPreviousAyah: () -> Unit,
    onToggleRepeat: () -> Unit,
    onCycleSpeed: () -> Unit = {},
    onSetSleepTimerMinutes: (Int?) -> Unit = {},
    onToggleStopAfterSurah: () -> Unit = {},
    onOpenMemorizationLoop: () -> Unit = {},
    onDownloadSurahAudio: () -> Unit = {},
    onCancelSurahAudio: () -> Unit = {},
    onDeleteSurahAudio: () -> Unit = {},
    onSeek: (Long) -> Unit,
    onStopAudio: () -> Unit,
    onSelectReciter: (String) -> Unit,
    playlists: List<AudioPlaylist> = emptyList(),
    surahsByNumber: Map<Int, SurahInfo> = emptyMap(),
    playlistActions: AudioPlaylistActions = AudioPlaylistActions(),
    playbackNotifConfig: PlaybackNotificationConfig = PlaybackNotificationConfig(),
    playbackNotifActions: PlaybackNotifActions = PlaybackNotifActions(),
    modifier: Modifier = Modifier
) {
    var showNotifCustomize by remember { mutableStateOf(false) }
    var showPlaylistsPicker by remember { mutableStateOf(false) }
    AnimatedVisibility(visible = visible, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut(), modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = 8.dp,
            shadowElevation = 12.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(top = 12.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.align(Alignment.CenterHorizontally).width(40.dp).height(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant))
                AudioSheetStatusRow(playbackState = playbackState, currentSurahName = currentSurahName, currentPage = currentPage, onOpenPlaylists = if (playlists.isNotEmpty()) ({ showPlaylistsPicker = true }) else null, onOpenNotifCustomize = { showNotifCustomize = true }, onStopAudio = onStopAudio)

                AudioSheetScrubber(playbackState = playbackState, onSeek = onSeek)

                AudioSheetTransportControls(
                    playbackState = playbackState, onTogglePlayPause = onTogglePlayPause, onPlayPageFirstAyah = onPlayPageFirstAyah,
                    onNextAyah = onNextAyah, onPreviousAyah = onPreviousAyah, onToggleRepeat = onToggleRepeat,
                    onCycleSpeed = onCycleSpeed, onOpenMemorizationLoop = onOpenMemorizationLoop
                )
                SurahOfflineAudioStrip(
                    surahName = if (playbackState.isActive) playbackState.surahNameAr else currentSurahName,
                    state = surahAudioState, onDownload = onDownloadSurahAudio, onCancel = onCancelSurahAudio, onDelete = onDeleteSurahAudio
                )
                AudioSleepTimerBar(playbackState = playbackState, onSetSleepTimerMinutes = onSetSleepTimerMinutes, onToggleStopAfterSurah = onToggleStopAfterSurah)
                ReciterCarouselSection(
                    selectedReciterId = playbackState.reciter.id, activeRiwayah = activeRiwayah,
                    reciters = reciterCatalog, onSelectReciter = onSelectReciter
                )
            }
        }
    }
    if (showNotifCustomize) {
        PlaybackNotifCustomizeSheet(
            config = playbackNotifConfig, onSetEnabled = playbackNotifActions.onSetEnabled,
            onToggleButton = playbackNotifActions.onToggleButton, onSetShowWhenPaused = playbackNotifActions.onSetShowWhenPaused,
            onDismiss = { showNotifCustomize = false }
        )
    }
    if (showPlaylistsPicker) {
        PlaylistQuickPickerSheet(
            playlists = playlists, surahsByNumber = surahsByNumber, playbackState = playbackState,
            actions = playlistActions, onDismiss = { showPlaylistsPicker = false }
        )
    }
}

@Composable
private fun SurahOfflineAudioStrip(
    surahName: String, state: SurahAudioDownloadState, onDownload: () -> Unit, onCancel: () -> Unit, onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = if (state.isComplete) Icons.Rounded.CloudDone else Icons.Rounded.CloudDownload,
                contentDescription = null,
                tint = if (state.isComplete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = when {
                    state.isDownloading -> "جاري تنزيل سورة $surahName (${state.downloadedAyahs.toAppDigits()}/${state.totalAyahs.toAppDigits()})"
                    state.isComplete -> "سورة $surahName محفوظة بدون نت"
                    else -> "تنزيل صوت سورة $surahName (${state.downloadedAyahs.toAppDigits()}/${state.totalAyahs.toAppDigits()})"
                },
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        when {
            state.isDownloading -> TextButton(onClick = onCancel, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) { Text("إلغاء", style = MaterialTheme.typography.labelSmall) }
            state.isComplete -> TextButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف الصوت", modifier = Modifier.size(16.dp))
                Text("حذف", style = MaterialTheme.typography.labelSmall)
            }
            else -> TextButton(onClick = onDownload, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp), modifier = Modifier.testTag("download_surah_audio_button")) { Text("تنزيل", style = MaterialTheme.typography.labelSmall) }
        }
    }
    if (state.isDownloading && state.totalAyahs > 0) {
        LinearProgressIndicator(
            progress = { (state.downloadedAyahs.toFloat() / state.totalAyahs.toFloat()).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(3.dp)
        )
    }
}

@Composable
private fun AudioSheetStatusRow(
    playbackState: AudioPlaybackState,
    currentSurahName: String,
    currentPage: Int,
    onOpenPlaylists: (() -> Unit)?,
    onOpenNotifCustomize: () -> Unit,
    onStopAudio: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val loopInfo = if (playbackState.memorizationLoop.enabled) {
                val l = playbackState.memorizationLoop
                " • تحفيظ (${l.startAyah.toAppDigits()}-${l.endAyah.toAppDigits()})"
            } else ""
            Text(
                text = if (playbackState.isActive) "سورة ${playbackState.surahNameAr} • الآية ${playbackState.currentAyah.toAppDigits()}$loopInfo"
                else "سورة $currentSurahName • صفحة ${currentPage.toAppDigits()}",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val plSession = playbackState.playlistSession
            val subtitleText = when {
                playbackState.errorMessage != null -> playbackState.errorMessage
                plSession != null -> "قائمة: ${plSession.playlistTitleAr} (${(plSession.currentTrackIndex + 1).toAppDigits()}/${plSession.totalTracks.toAppDigits()}) • ${playbackState.reciter.nameAr}"
                else -> playbackState.reciter.nameAr
            }
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    playbackState.errorMessage != null -> MaterialTheme.colorScheme.error
                    plSession != null -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (onOpenPlaylists != null) {
                IconButton(onClick = onOpenPlaylists, modifier = Modifier.size(44.dp).testTag("player_playlists_button")) {
                    Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = "قوائم التلاوة الصوتية", tint = MaterialTheme.colorScheme.primary)
                }
            }
            IconButton(onClick = onOpenNotifCustomize, modifier = Modifier.size(44.dp).testTag("player_notif_customize_button")) {
                Icon(Icons.Rounded.Tune, contentDescription = "تخصيص إشعار التلاوة", tint = MaterialTheme.colorScheme.primary)
            }
            if (playbackState.isActive) {
                Surface(
                    onClick = onStopAudio,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.testTag("player_close_button")
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Rounded.Stop, contentDescription = "إيقاف", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Text("إيقاف", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioSheetScrubber(playbackState: AudioPlaybackState, onSeek: (Long) -> Unit) {
    val durMs = playbackState.durationMs.coerceAtLeast(0L)
    val posMs = playbackState.positionMs.coerceIn(0L, durMs.coerceAtLeast(1L))
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    val liveFraction = if (durMs > 0L) (posMs.toFloat() / durMs.toFloat()).coerceIn(0f, 1f) else 0f

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
        Slider(
            value = if (isDragging) dragFraction else liveFraction,
            onValueChange = { frac -> isDragging = true; dragFraction = frac },
            onValueChangeFinished = { if (durMs > 0L) onSeek((dragFraction * durMs).toLong()); isDragging = false },
            enabled = playbackState.isActive && durMs > 0L,
            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary, inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier.fillMaxWidth().height(26.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val shownPosMs = if (isDragging && durMs > 0L) (dragFraction * durMs).toLong() else posMs
            Text(formatDurationMs(shownPosMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatDurationMs(durMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
