package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.OfflineInventoryUiState
import com.example.domain.model.localizeDigits
import com.example.domain.model.toAppDigits
import com.example.ui.viewmodel.OfflineDownloadState
import kotlin.math.roundToInt

@Composable
internal fun OfflineDownloadSection(
    offlineState: OfflineDownloadState,
    onStartDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onDeleteDownload: () -> Unit,
    offlineHubState: OfflineInventoryUiState = OfflineInventoryUiState(),
    onOpenComprehensiveHub: () -> Unit = {}
) {
    val useWestern = LocalUseWesternDigits.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (offlineState.isComplete) {
                            Icons.Rounded.CheckCircle
                        } else {
                            Icons.Rounded.DownloadForOffline
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "تنزيل المصحف كاملاً",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${offlineState.edition.titleAr} • ${offlineState.downloadedCount.toAppDigits(useWestern)} / ${offlineState.totalPages.toAppDigits(useWestern)} صفحة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (offlineState.downloadedCount > 0 && !offlineState.isDownloading) {
                    IconButton(
                        onClick = onDeleteDownload,
                        modifier = Modifier.testTag("offline_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "حذف الصفحات المحمّلة",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (offlineState.isDownloading || (offlineState.downloadedCount in 1 until offlineState.totalPages)) {
                val fraction = (offlineState.downloadedCount.toFloat() / offlineState.totalPages.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            offlineState.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            when {
                offlineState.isDownloading -> {
                    OutlinedButton(
                        onClick = onCancelDownload,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("offline_cancel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إيقاف التنزيل مؤقتاً")
                    }
                }

                offlineState.isComplete -> {
                    Text(
                        text = "جميع صفحات المصحف (٦٠٤) محفوظة وتعمل فوراً بدون إنترنت".localizeDigits(useWestern),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                else -> {
                    Button(
                        onClick = onStartDownload,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("offline_download_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DownloadForOffline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (offlineState.downloadedCount > 0) {
                                "استكمال تنزيل المصحف (${(offlineState.totalPages - offlineState.downloadedCount).toAppDigits(useWestern)} متبقية)"
                            } else {
                                "تنزيل المصحف كاملاً (٦٠٤ صفحة)".localizeDigits(useWestern)
                            }
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (offlineHubState.isBatchDownloading) {
                        val pct = (offlineHubState.overallBatchProgress * 100f).roundToInt().coerceIn(0, 100)
                        "جاري التنزيل المتزامن (${offlineHubState.activeKeys.size.toAppDigits(useWestern)} عناصر • ${pct.toAppDigits(useWestern)}٪)"
                    } else {
                        offlineHubState.compactSummaryAr(useWestern)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilledTonalButton(
                    onClick = onOpenComprehensiveHub,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_comprehensive_download_hub_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مركز التنزيل الشامل (الروايات والتفسير والصوت)")
                }
            }
        }
    }
}
