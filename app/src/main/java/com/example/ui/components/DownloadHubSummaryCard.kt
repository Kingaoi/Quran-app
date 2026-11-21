package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.OfflineInventoryUiState
import com.example.domain.model.formatStorageSizeAr
import com.example.domain.model.toAppDigits

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DownloadHubSummaryCard(
    hubState: OfflineInventoryUiState,
    onSelectRecommended: () -> Unit,
    onSelectAllIncomplete: () -> Unit,
    onClearSelection: () -> Unit
) {
    val useWestern = LocalUseWesternDigits.current
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
    val surfaceLow = MaterialTheme.colorScheme.surfaceContainerLow
    val cardBrush = remember(primaryContainer, secondaryContainer, surfaceLow) {
        Brush.linearGradient(
            listOf(
                primaryContainer.copy(alpha = 0.62f),
                secondaryContainer.copy(alpha = 0.36f),
                surfaceLow
            )
        )
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = surfaceLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("download_hub_summary_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "حالة التنزيلات على جهازك",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "المساحة المستخدمة: ${formatStorageSizeAr(hubState.totalStorageBytes, useWestern)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "المصاحف المحمّلة بالروايات:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val started = hubState.startedRiwayahs
                    if (started.isEmpty()) {
                        InventoryStatusPill(text = "لم يُنزّل أي مصحف بعد", highlighted = false)
                    } else {
                        started.forEach { r ->
                            val label = if (r.isComplete) {
                                "${r.edition.titleAr} ✓"
                            } else {
                                "${r.edition.titleAr} (${r.downloadedPages.toAppDigits(useWestern)}/${r.totalPages.toAppDigits(useWestern)})"
                            }
                            InventoryStatusPill(text = label, highlighted = r.isComplete)
                        }
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tafsirDone = hubState.completedTafsirs
                InventoryStatusPill(
                    text = if (tafsirDone.isEmpty()) {
                        "التفاسير: غير محمّلة"
                    } else {
                        "التفسير: ${tafsirDone.joinToString("، ") { it.edition.titleAr }}"
                    },
                    highlighted = tafsirDone.isNotEmpty()
                )
                val audioCount = hubState.startedReciters.size
                InventoryStatusPill(
                    text = if (audioCount == 0) {
                        "التلاوات: بدون تنزيل"
                    } else {
                        "تلاوات محفوظة: ${audioCount.toAppDigits(useWestern)} قرّاء"
                    },
                    highlighted = audioCount > 0
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = false,
                    onClick = onSelectRecommended,
                    enabled = !hubState.isBatchDownloading,
                    label = { Text("المقترح", style = MaterialTheme.typography.labelMedium) }
                )
                FilterChip(
                    selected = false,
                    onClick = onSelectAllIncomplete,
                    enabled = !hubState.isBatchDownloading,
                    label = { Text("تحديد الكل", style = MaterialTheme.typography.labelMedium) }
                )
                Spacer(modifier = Modifier.weight(1f))
                if (hubState.selectedKeys.isNotEmpty() && !hubState.isBatchDownloading) {
                    TextButton(onClick = onClearSelection) {
                        Text("مسح التحديد", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryStatusPill(text: String, highlighted: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (highlighted) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (highlighted) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
