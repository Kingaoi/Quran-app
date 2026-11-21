package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.AdhkarDownloadStatus
import com.example.domain.model.DownloadHubActions
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.formatStorageSizeAr
import com.example.domain.model.toAppDigits
import kotlin.math.roundToInt

@Composable
internal fun DownloadHubSelectableRowCard(
    itemKey: String,
    title: String,
    subtitle: String,
    progress: Float,
    isComplete: Boolean,
    isPartial: Boolean,
    isDownloading: Boolean,
    isSelected: Boolean,
    isActiveBadge: Boolean = false,
    sizeBytes: Long,
    errorMessage: String? = null,
    leadingIcon: ImageVector? = Icons.AutoMirrored.Rounded.MenuBook,
    leadingContent: (@Composable () -> Unit)? = null,
    onToggleSelect: () -> Unit,
    onCancelSingle: () -> Unit,
    onDelete: () -> Unit
) {
    val useWestern = LocalUseWesternDigits.current
    val pct = (progress * 100f).roundToInt().coerceIn(0, 100)
    val bgColor by animateColorAsState(
        targetValue = when {
            isDownloading -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            isComplete -> MaterialTheme.colorScheme.surfaceContainerLow
            else -> MaterialTheme.colorScheme.surface
        },
        label = "hub_row_bg"
    )
    val borderColor = when {
        isDownloading || isSelected -> MaterialTheme.colorScheme.primary
        isComplete -> MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        onClick = { if (!isComplete && !isDownloading) onToggleSelect() },
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        border = BorderStroke(if (isSelected || isDownloading) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("download_hub_item_$itemKey")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isComplete) {
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "مكتمل التنزيل",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                } else {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { if (!isDownloading) onToggleSelect() },
                        enabled = !isDownloading,
                        modifier = Modifier.size(48.dp).testTag("download_hub_check_$itemKey")
                    )
                }

                if (leadingContent != null) {
                    leadingContent()
                } else if (leadingIcon != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = leadingIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isActiveBadge) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "الحالية",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    val sizeText = if (sizeBytes > 0L) " • ${formatStorageSizeAr(sizeBytes, useWestern)}" else ""
                    val stateHint = when {
                        isDownloading -> "جاري التنزيل ${pct.toAppDigits(useWestern)}٪"
                        isComplete -> "جاهز بدون إنترنت$sizeText"
                        isPartial -> "$subtitle (${pct.toAppDigits(useWestern)}٪)$sizeText"
                        else -> subtitle
                    }
                    Text(
                        text = stateHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isComplete || isDownloading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                when {
                    isDownloading -> {
                        IconButton(
                            onClick = onCancelSingle,
                            modifier = Modifier.size(48.dp).testTag("download_hub_cancel_$itemKey")
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "إيقاف تنزيل هذا العنصر", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    isComplete || isPartial -> {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(48.dp).testTag("download_hub_delete_$itemKey")
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف الملفات المحمّلة", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            if (isDownloading || isPartial) {
                LinearProgressIndicator(
                    progress = { progress },
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                )
            }

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
internal fun DownloadHubSectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector = Icons.Rounded.AutoStories
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun DownloadHubAdhkarCard(
    status: AdhkarDownloadStatus,
    isSelected: Boolean,
    actions: DownloadHubActions
) {
    val useWestern = LocalUseWesternDigits.current
    DownloadHubSelectableRowCard(
        itemKey = status.key,
        title = "حصن المسلم والأذكار الكاملة",
        subtitle = "أكثر من ${status.totalChapters.toAppDigits(useWestern)} باباً مع الصوتيات",
        progress = status.progress,
        isComplete = status.isComplete,
        isPartial = !status.isComplete && status.downloadedChapters > 0,
        isDownloading = status.isDownloading,
        isSelected = isSelected,
        sizeBytes = status.sizeBytes,
        errorMessage = status.errorMessage,
        leadingIcon = Icons.Rounded.VolunteerActivism,
        onToggleSelect = { actions.onToggleSelection(status.key) },
        onCancelSingle = { actions.onCancelSingleDownload(status.key) },
        onDelete = { actions.onDeleteItem(status.key) }
    )
}
