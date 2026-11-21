package com.example.ui.screens.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ReaderSettings
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.QuranReciters
import com.example.domain.model.TafsirEdition
import com.example.domain.model.TafsirOfflineState
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MoreAudioAndTafsirSection(
    settings: ReaderSettings,
    tafsirState: TafsirOfflineState,
    actions: MoreSettingsActions,
    onOpenReciterPicker: () -> Unit
) {
    val useWestern = LocalUseWesternDigits.current
    val qc = MaterialTheme.quranColors
    val reciter = remember(settings.selectedReciterId) { QuranReciters.byId(settings.selectedReciterId) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MoreCategoryHeader(title = "التلاوة الصوتية والتفسير", icon = Icons.Rounded.RecordVoiceOver)

        MoreSubGroupHeader(title = "القارئ وإشعار التلاوة في الخلفية")
        MoreSettingsGroupCard {
            MoreSettingActionRow(
                title = "القارئ الافتراضي للتلاوة",
                subtitle = "${reciter.nameAr} • ${reciter.styleAr}",
                icon = Icons.Rounded.RecordVoiceOver,
                iconTint = qc.categoryPastelAt(4),
                onClick = onOpenReciterPicker,
                testTagKey = "more_reciter_picker_row"
            )
            PlaybackNotificationSettingsRows(
                config = settings.playbackNotificationConfig,
                onSetEnabled = actions.onSetPlaybackNotifEnabled,
                onToggleButton = actions.onTogglePlaybackNotifButton,
                onSetShowWhenPaused = actions.onSetPlaybackNotifShowWhenPaused
            )
        }

        MoreCategoryHeader(title = "التفسير بدون إنترنت", icon = Icons.Rounded.CollectionsBookmark)
        MoreSubGroupHeader(title = "تنزيل كتب التفسير المعتمدة")
        MoreSettingsGroupCard {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MoreSettingIconBadge(icon = Icons.AutoMirrored.Rounded.MenuBook, tint = qc.categoryPastelAt(2))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("التفسير بدون إنترنت", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = "${tafsirState.edition.titleAr} • ${tafsirState.downloadedSurahs.toAppDigits(useWestern)} / ${tafsirState.totalSurahs.toAppDigits(useWestern)} سورة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (tafsirState.isComplete) {
                        Icon(Icons.Rounded.CloudDone, contentDescription = "مكتمل", tint = qc.success)
                    }
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TafsirEdition.entries.forEach { edition ->
                        val sel = tafsirState.edition == edition
                        FilterChip(
                            selected = sel,
                            onClick = { actions.onSelectTafsirEdition(edition) },
                            enabled = !tafsirState.isDownloading,
                            colors = quranFilterChipColors(),
                            border = quranFilterChipBorder(sel),
                            label = { Text(edition.titleAr) },
                            modifier = Modifier.testTag("more_tafsir_edition_${edition.slug}")
                        )
                    }
                }

                if (tafsirState.isDownloading) {
                    LinearProgressIndicator(
                        progress = { (tafsirState.downloadedSurahs.toFloat() / tafsirState.totalSurahs.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (tafsirState.isDownloading) {
                        OutlinedButton(onClick = actions.onCancelTafsir, modifier = Modifier.weight(1f)) {
                            Text("إيقاف التنزيل")
                        }
                    } else {
                        Button(
                            onClick = actions.onDownloadTafsir,
                            enabled = !tafsirState.isComplete,
                            modifier = Modifier.weight(1f).testTag("more_tafsir_download_button")
                        ) {
                            Icon(Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (tafsirState.isComplete) "محفوظ بالكامل" else "تنزيل التفسير")
                        }
                        if (tafsirState.downloadedSurahs > 0) {
                            TextButton(onClick = actions.onDeleteTafsir) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف التفسير", modifier = Modifier.size(18.dp))
                                Text("حذف")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun MoreBackupAndAboutSection(
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onOpenSources: (() -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MoreCategoryHeader(title = "النسخ الاحتياطي والاستعادة", icon = Icons.Rounded.Backup)
        MoreSubGroupHeader(title = "حفظ الإعدادات والسجلات في ملف JSON")
        MoreSettingsGroupCard {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "يمكنك تصدير جميع تفضيلات القراءة، خطة الورد، الأذكار، والعلامات المرجعية في ملف واحد واستعادتها في أي وقت.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onExportClick,
                        modifier = Modifier.weight(1f).testTag("more_export_backup_button")
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تصدير نسخة")
                    }
                    OutlinedButton(
                        onClick = onImportClick,
                        modifier = Modifier.weight(1f).testTag("more_import_backup_button")
                    ) {
                        Icon(Icons.Rounded.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("استعادة")
                    }
                }
            }
        }
        MoreSubGroupHeader(title = "معلومات التطبيق")
        MoreAboutAppCard(onOpenSources = onOpenSources)
    }
}
