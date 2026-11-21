package com.example.ui.screens.prayer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.prayer.AdhanVoice
import com.example.domain.prayer.AdhanVoices
import com.example.ui.theme.quranColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhanVoicePickerSheet(
    selectedVoiceId: String,
    cachedVoiceIds: Set<String>,
    downloadingVoiceId: String?,
    previewingVoiceId: String?,
    onSelectVoice: (String) -> Unit,
    onTogglePreview: (AdhanVoice) -> Unit,
    onDownloadVoice: (AdhanVoice) -> Unit,
    onDeleteVoice: (AdhanVoice) -> Unit,
    onStopPreview: () -> Unit,
    onDismiss: () -> Unit
) {
    DisposableEffect(Unit) {
        onDispose { onStopPreview() }
    }

    ModalBottomSheet(
        onDismissRequest = {
            onStopPreview()
            onDismiss()
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "اختيار صوت المؤذن",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "يمكنك المعاينة وحفظ الصوت للعمل بدون إنترنت عند دخول الوقت",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(AdhanVoices.all, key = { it.id }) { voice ->
                    AdhanVoiceCardItem(
                        voice = voice,
                        isSelected = voice.id == selectedVoiceId,
                        isCached = voice.id in cachedVoiceIds,
                        isDownloading = downloadingVoiceId == voice.id,
                        isPreviewing = previewingVoiceId == voice.id,
                        onSelect = { onSelectVoice(voice.id) },
                        onTogglePreview = { onTogglePreview(voice) },
                        onDownload = { onDownloadVoice(voice) },
                        onDelete = { onDeleteVoice(voice) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdhanVoiceCardItem(
    voice: AdhanVoice,
    isSelected: Boolean,
    isCached: Boolean,
    isDownloading: Boolean,
    isPreviewing: Boolean,
    onSelect: () -> Unit,
    onTogglePreview: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val shape = RoundedCornerShape(18.dp)
    Surface(
        shape = shape,
        color = if (isSelected) qc.selected else qc.surface,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) qc.selected else qc.border
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onSelect)
            .testTag("adhan_voice_item_${voice.id}")
    ) {
        val titleColor = if (isSelected) qc.onSelected else qc.text
        val subtitleColor = if (isSelected) qc.onSelected.copy(alpha = 0.82f) else qc.textMuted
        val iconTint = if (isSelected) qc.onSelected else qc.primary

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalIconButton(
                onClick = onTogglePreview,
                colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isSelected) qc.onSelected.copy(alpha = 0.16f) else qc.surface2,
                    contentColor = if (isSelected) qc.onSelected else qc.primary
                ),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("adhan_voice_preview_${voice.id}")
            ) {
                Icon(
                    imageVector = if (isPreviewing) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPreviewing) "إيقاف المعاينة" else "معاينة صوت ${voice.nameAr}"
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = voice.nameAr,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    voice.durationBadgeAr?.let { badge ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) qc.onSelected.copy(alpha = 0.2f) else qc.surface2
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) qc.onSelected else qc.selected,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (isCached) {
                        Icon(
                            imageVector = Icons.Rounded.CloudDone,
                            contentDescription = "محفوظ أوفلاين",
                            tint = if (isSelected) qc.onSelected else qc.success,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Text(
                    text = voice.muezzinAr,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isDownloading) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.2.dp,
                        color = iconTint
                    )
                }
            } else if (isCached) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("adhan_voice_delete_${voice.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "حذف الملف المحفوظ",
                        tint = subtitleColor
                    )
                }
            } else {
                IconButton(
                    onClick = onDownload,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("adhan_voice_download_${voice.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudDownload,
                        contentDescription = "حفظ للعمل بدون إنترنت",
                        tint = iconTint
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "الصوت المختار",
                    tint = qc.onSelected,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
