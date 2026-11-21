package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.audio.AppAudioCoordinator
import com.example.data.audio.AppAudioOwner
import com.example.data.audio.RiwayahRecitersRepository
import com.example.data.local.RiwayahEdition
import com.example.domain.model.QuranReciters
import com.example.domain.model.ReciterOption
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReciterPickerSheet(
    selectedReciterId: String,
    activeRiwayah: RiwayahEdition = RiwayahEdition.HAFS,
    onSelectReciter: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var catalog by remember { mutableStateOf(QuranReciters.combinedCatalog()) }
    var filterRiwayah by remember(activeRiwayah) { mutableStateOf<RiwayahEdition?>(null) }
    var previewReciterId by remember { mutableStateOf<String?>(null) }
    var isPreviewBuffering by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val loaded = RiwayahRecitersRepository(context).loadMultiRiwayahReciters()
        if (loaded.isNotEmpty()) catalog = QuranReciters.combinedCatalog()
    }

    val previewPlayer = remember(context) {
        ExoPlayer.Builder(context.applicationContext)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                true
            )
            .build()
    }

    DisposableEffect(previewPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> isPreviewBuffering = true
                    Player.STATE_READY -> isPreviewBuffering = false
                    Player.STATE_ENDED, Player.STATE_IDLE -> {
                        isPreviewBuffering = false
                        previewReciterId = null
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isPreviewBuffering = false
                previewReciterId = null
            }
        }
        previewPlayer.addListener(listener)
        AppAudioCoordinator.register(AppAudioOwner.RECITER_PREVIEW) {
            previewPlayer.stop()
            previewReciterId = null
            isPreviewBuffering = false
        }
        onDispose {
            AppAudioCoordinator.unregister(AppAudioOwner.RECITER_PREVIEW)
            previewPlayer.removeListener(listener)
            previewPlayer.release()
        }
    }

    val displayedReciters = remember(catalog, filterRiwayah, activeRiwayah) {
        val base = filterRiwayah?.let { r -> catalog.filter { it.riwayah == r } } ?: catalog
        if (filterRiwayah == null) {
            base.filter { it.riwayah == activeRiwayah } + base.filter { it.riwayah != activeRiwayah }
        } else {
            base
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(bottom = 22.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RecordVoiceOver,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(9.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "اختر القارئ والتصنيف",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "مصحفك الحالي: ${activeRiwayah.titleAr} • اضغط ▶ لمعاينة الصوت",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                item(key = "all") {
                    FilterChip(
                        selected = filterRiwayah == null,
                        onClick = { filterRiwayah = null },
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(filterRiwayah == null),
                        label = { Text("الكل") },
                        modifier = Modifier.testTag("reciter_filter_all")
                    )
                }
                items(RiwayahEdition.entries, key = { it.slug }) { edition ->
                    val isSelected = filterRiwayah == edition
                    FilterChip(
                        selected = isSelected,
                        onClick = { filterRiwayah = if (isSelected) null else edition },
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(isSelected),
                        label = { Text(edition.titleAr.substringBefore(" عن")) },
                        modifier = Modifier.testTag("reciter_filter_${edition.slug}")
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(displayedReciters, key = { it.id }) { reciter ->
                    ReciterPickerRowCard(
                        reciter = reciter,
                        isSelected = reciter.id == selectedReciterId,
                        isPreviewing = reciter.id == previewReciterId,
                        isPreviewBuffering = isPreviewBuffering,
                        matchesActiveRiwayah = reciter.riwayah == activeRiwayah,
                        onSelect = {
                            onSelectReciter(reciter.id)
                            onDismiss()
                        },
                        onTogglePreview = {
                            if (reciter.id == previewReciterId) {
                                previewPlayer.stop()
                                previewReciterId = null
                                isPreviewBuffering = false
                            } else {
                                AppAudioCoordinator.claimFocus(AppAudioOwner.RECITER_PREVIEW)
                                previewReciterId = reciter.id
                                isPreviewBuffering = true
                                previewPlayer.setMediaItem(MediaItem.fromUri(reciter.ayahAudioUrl(1, 1)))
                                previewPlayer.prepare()
                                previewPlayer.play()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReciterPickerRowCard(
    reciter: ReciterOption,
    isSelected: Boolean,
    isPreviewing: Boolean,
    isPreviewBuffering: Boolean,
    matchesActiveRiwayah: Boolean,
    onSelect: () -> Unit,
    onTogglePreview: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) qc.selected else qc.surface,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) qc.selected else qc.border),
        modifier = Modifier.fillMaxWidth().testTag("reciter_option_${reciter.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReciterPortraitAvatar(reciter = reciter, isSelected = isSelected, size = 56.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = reciter.nameAr,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) qc.onSelected else qc.text
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) qc.onSelected.copy(alpha = 0.16f) else qc.surface2
                    ) {
                        Text(
                            text = reciter.categoryAr,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) qc.onSelected else qc.textMuted,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = reciter.riwayah.titleAr,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) qc.onSelected.copy(alpha = 0.9f) else qc.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (reciter.bioAr.isNotBlank()) {
                    Text(
                        text = reciter.bioAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) qc.onSelected.copy(alpha = 0.85f) else qc.textMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            FilledTonalIconButton(
                onClick = onTogglePreview,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isPreviewing) qc.selected else qc.surface2,
                    contentColor = if (isPreviewing) qc.onSelected else qc.primary
                ),
                modifier = Modifier.size(48.dp).testTag("reciter_preview_${reciter.id}")
            ) {
                when {
                    isPreviewing && isPreviewBuffering -> {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    }
                    isPreviewing -> {
                        Icon(Icons.Rounded.Stop, contentDescription = "إيقاف المعاينة")
                    }
                    else -> {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = "معاينة صوت ${reciter.nameAr}")
                    }
                }
            }
            if (isSelected) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = "محدد", tint = qc.onSelected)
            }
        }
    }
}
