package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PauseCircleOutline
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.RiwayahEdition
import com.example.domain.model.DownloadHubActions
import com.example.domain.model.DownloadHubCategory
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.OfflineInventoryUiState
import com.example.domain.model.QuranReciters
import com.example.domain.model.toAppDigits
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprehensiveDownloadSheet(
    hubState: OfflineInventoryUiState,
    activeRiwayah: RiwayahEdition,
    activeReciterId: String,
    actions: DownloadHubActions,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val useWestern = LocalUseWesternDigits.current
    var selectedCategory by remember { mutableStateOf(DownloadHubCategory.ALL) }
    var showSurahPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        actions.onRefresh()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .navigationBarsPadding()
        ) {
            DownloadHubHeaderAndFilters(
                selectedCategory = selectedCategory,
                onSelectCategory = { selectedCategory = it },
                onDismiss = onDismiss
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(key = "offline_readiness_quick") {
                    OfflineReadinessQuickCard(
                        hubState = hubState,
                        activeRiwayah = activeRiwayah,
                        onPrepareEssentialOffline = actions.onPrepareEssentialOffline,
                        onClearRasterCache = actions.onClearRasterCache,
                        onOpenSurahAudioPicker = { showSurahPicker = true }
                    )
                }

                item(key = "offline_storage_breakdown") {
                    OfflineStorageBreakdownCard(hubState = hubState)
                }

                item(key = "inventory_summary") {
                    DownloadHubSummaryCard(
                        hubState = hubState,
                        onSelectRecommended = actions.onSelectRecommended,
                        onSelectAllIncomplete = actions.onSelectAllIncomplete,
                        onClearSelection = actions.onClearSelection
                    )
                }

                if (selectedCategory == DownloadHubCategory.ALL || selectedCategory == DownloadHubCategory.MUSHAF) {
                    item(key = "header_mushaf") {
                        DownloadHubSectionHeader(
                            title = "المصحف الشريف (${hubState.riwayahStatuses.size.toAppDigits(useWestern)} روايات)",
                            subtitle = "صفحات متجهية فائقة الدقة (٦٠٤ صفحة)",
                            icon = Icons.AutoMirrored.Rounded.MenuBook
                        )
                    }
                    items(hubState.riwayahStatuses, key = { it.key }) { item ->
                        DownloadHubSelectableRowCard(
                            itemKey = item.key,
                            title = item.edition.titleAr,
                            subtitle = "${item.downloadedPages.toAppDigits(useWestern)} / ${item.totalPages.toAppDigits(useWestern)} صفحة",
                            progress = item.progress,
                            isComplete = item.isComplete,
                            isPartial = item.isPartial,
                            isDownloading = item.isDownloading,
                            isSelected = item.key in hubState.selectedKeys,
                            isActiveBadge = item.edition == activeRiwayah,
                            sizeBytes = item.sizeBytes,
                            errorMessage = item.errorMessage,
                            leadingIcon = Icons.AutoMirrored.Rounded.MenuBook,
                            onToggleSelect = { actions.onToggleSelection(item.key) },
                            onCancelSingle = { actions.onCancelSingleDownload(item.key) },
                            onDelete = { actions.onDeleteItem(item.key) }
                        )
                    }
                }

                if (selectedCategory == DownloadHubCategory.ALL || selectedCategory == DownloadHubCategory.TAFSIR) {
                    item(key = "header_tafsir") {
                        DownloadHubSectionHeader(
                            title = "كتب التفسير (${hubState.tafsirStatuses.size.toAppDigits(useWestern)})",
                            subtitle = "كامل القرآن (١١٤ سورة)",
                            icon = Icons.Rounded.AutoStories
                        )
                    }
                    items(hubState.tafsirStatuses, key = { it.key }) { item ->
                        DownloadHubSelectableRowCard(
                            itemKey = item.key,
                            title = item.edition.titleAr,
                            subtitle = "${item.downloadedSurahs.toAppDigits(useWestern)} / ${item.totalSurahs.toAppDigits(useWestern)} سورة • ${item.edition.subtitleAr}",
                            progress = item.progress,
                            isComplete = item.isComplete,
                            isPartial = item.isPartial,
                            isDownloading = item.isDownloading,
                            isSelected = item.key in hubState.selectedKeys,
                            sizeBytes = item.sizeBytes,
                            errorMessage = item.errorMessage,
                            leadingIcon = Icons.Rounded.AutoStories,
                            onToggleSelect = { actions.onToggleSelection(item.key) },
                            onCancelSingle = { actions.onCancelSingleDownload(item.key) },
                            onDelete = { actions.onDeleteItem(item.key) }
                        )
                    }
                }

                if (selectedCategory == DownloadHubCategory.ALL || selectedCategory == DownloadHubCategory.AUDIO) {
                    item(key = "header_audio") {
                        DownloadHubSectionHeader(
                            title = "التلاوات الصوتية الكاملة (${hubState.reciterStatuses.size.toAppDigits(useWestern)} قرّاء)",
                            subtitle = "مصحف مرتّل آية بآية (٦٢٣٦ آية)",
                            icon = Icons.Rounded.GraphicEq
                        )
                    }
                    items(hubState.reciterStatuses, key = { it.key }) { item ->
                        DownloadHubSelectableRowCard(
                            itemKey = item.key,
                            title = item.reciter.nameAr,
                            subtitle = "${item.downloadedAyahs.toAppDigits(useWestern)} / ${item.totalAyahs.toAppDigits(useWestern)} آية • ${item.reciter.styleAr}",
                            progress = item.progress,
                            isComplete = item.isComplete,
                            isPartial = item.isPartial,
                            isDownloading = item.isDownloading,
                            isSelected = item.key in hubState.selectedKeys,
                            isActiveBadge = item.reciter.id == activeReciterId,
                            sizeBytes = item.sizeBytes,
                            errorMessage = item.errorMessage,
                            leadingIcon = null,
                            leadingContent = {
                                ReciterPortraitAvatar(
                                    reciter = item.reciter,
                                    isSelected = item.key in hubState.selectedKeys || item.isComplete,
                                    size = 44.dp
                                )
                            },
                            onToggleSelect = { actions.onToggleSelection(item.key) },
                            onCancelSingle = { actions.onCancelSingleDownload(item.key) },
                            onDelete = { actions.onDeleteItem(item.key) }
                        )
                    }
                }

                if (selectedCategory == DownloadHubCategory.ALL || selectedCategory == DownloadHubCategory.ADHKAR) {
                    item(key = "header_adhkar") { DownloadHubSectionHeader(title = "حصن المسلم والأذكار", subtitle = "النصوص والصوتيات الكاملة", icon = Icons.Rounded.VolunteerActivism) }
                    item(key = hubState.adhkarStatus.key) { DownloadHubAdhkarCard(hubState.adhkarStatus, hubState.adhkarStatus.key in hubState.selectedKeys, actions) }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    hubState.statusMessage?.takeIf { it.isNotBlank() }?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (hubState.isBatchDownloading) {
                        val overallPct = (hubState.overallBatchProgress * 100f).roundToInt().coerceIn(0, 100)
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text(
                                "تنزيل متزامن (${hubState.activeKeys.size.toAppDigits(useWestern)} عناصر نشطة)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("${overallPct.toAppDigits(useWestern)}٪", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        }
                        LinearProgressIndicator(progress = { hubState.overallBatchProgress }, strokeCap = StrokeCap.Round, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)))
                        OutlinedButton(onClick = actions.onCancelAllDownloads, modifier = Modifier.fillMaxWidth().testTag("download_hub_cancel_all_button")) {
                            Icon(Icons.Rounded.PauseCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إيقاف جميع التنزيلات مؤقتاً")
                        }
                    } else {
                        val count = hubState.selectableSelectedCount
                        Button(onClick = actions.onStartConcurrentDownload, enabled = count > 0, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(50.dp).testTag("download_hub_start_button")) {
                            Icon(Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (count > 0) "بدء التنزيل المتزامن (${count.toAppDigits(useWestern)})" else "حدّد من القائمة لبدء التنزيل", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }

    if (showSurahPicker) {
        val reciterName = remember(activeReciterId) { QuranReciters.byId(activeReciterId).nameAr }
        OfflineSurahAudioSheet(
            hubState = hubState,
            activeReciterName = reciterName,
            onDownloadSingleSurah = actions.onDownloadSingleSurah,
            onDeleteSingleSurah = actions.onDeleteSingleSurah,
            onDownloadJuzAmma = actions.onDownloadJuzAmmaAudio,
            onDismiss = { showSurahPicker = false }
        )
    }
}
