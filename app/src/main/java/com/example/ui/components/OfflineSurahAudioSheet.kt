package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.OfflineInventoryUiState
import com.example.domain.model.toAppDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OfflineSurahAudioSheet(
    hubState: OfflineInventoryUiState,
    activeReciterName: String,
    onDownloadSingleSurah: (Int) -> Unit,
    onDeleteSingleSurah: (Int) -> Unit,
    onDownloadJuzAmma: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val useWestern = LocalUseWesternDigits.current
    var query by remember { mutableStateOf("") }
    val filteredSurahs = remember(hubState.surahsCatalog, query) {
        val q = query.trim()
        if (q.isEmpty()) hubState.surahsCatalog
        else hubState.surahsCatalog.filter { it.nameAr.contains(q) || it.number.toString() == q }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.86f)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تنزيل السور صوتياً",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "القارئ النشط: $activeReciterName (${hubState.activeReciterDownloadedSurahs.toAppDigits(useWestern)}/١١٤)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp).testTag("offline_surah_sheet_close")) {
                    Icon(Icons.Rounded.Close, contentDescription = "إغلاق")
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("ابحث باسم السورة أو رقمها...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("offline_surah_search_input")
            )

            FilledTonalButton(
                onClick = onDownloadJuzAmma,
                enabled = hubState.downloadingSingleSurahNum == null,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("offline_download_juz_amma_button")
            ) {
                Icon(Icons.Rounded.DownloadForOffline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("تنزيل جزء عمّ كاملاً (٣٧ سورة)", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredSurahs, key = { it.number }) { surah ->
                    val isDownloaded = surah.number in hubState.activeReciterDownloadedSurahNums
                    val isDownloading = hubState.downloadingSingleSurahNum == surah.number
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isDownloaded) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("offline_surah_row_${surah.number}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = surah.number.toAppDigits(useWestern),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(30.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "سورة ${surah.nameAr}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${surah.ayahCount.toAppDigits(useWestern)} آية • ص ${surah.firstPage.toAppDigits(useWestern)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            when {
                                isDownloading -> CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                                isDownloaded -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = "محمّلة", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        IconButton(
                                            onClick = { onDeleteSingleSurah(surah.number) },
                                            modifier = Modifier.size(48.dp).testTag("offline_surah_action_${surah.number}")
                                        ) {
                                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف سورة ${surah.nameAr}", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                                else -> {
                                    IconButton(
                                        onClick = { onDownloadSingleSurah(surah.number) },
                                        enabled = hubState.downloadingSingleSurahNum == null,
                                        modifier = Modifier.size(48.dp).testTag("offline_surah_action_${surah.number}")
                                    ) {
                                        Icon(Icons.Rounded.DownloadForOffline, contentDescription = "تنزيل سورة ${surah.nameAr}", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
