package com.example.ui.screens.reader.cards

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.AudioPlaybackState
import com.example.domain.model.AyahItem
import com.example.domain.model.TafsirEdition
import com.example.domain.model.toAppDigits
import com.example.ui.components.OrnamentalDivider
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors
import com.example.ui.util.copyTextToClipboard
import com.example.ui.viewmodel.AyahTafsirUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AyahTafsirCardSheet(
    ayah: AyahItem,
    accentColor: Color,
    tafsirState: AyahTafsirUiState,
    playbackState: AudioPlaybackState? = null,
    onPlayAyah: ((Int, Int) -> Unit)? = null,
    onTogglePlayPause: (() -> Unit)? = null,
    onLoadTafsir: (Int, Int, TafsirEdition) -> Unit,
    onTafsirCopied: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedEdition by remember(ayah) { mutableStateOf(tafsirState.edition) }
    var largeText by remember { mutableStateOf(false) }

    LaunchedEffect(ayah.surahNumber, ayah.ayahNumber, selectedEdition) {
        if (tafsirState.surahNumber != ayah.surahNumber ||
            tafsirState.ayahNumber != ayah.ayahNumber ||
            tafsirState.edition != selectedEdition ||
            (tafsirState.text == null && !tafsirState.isLoading)
        ) {
            onLoadTafsir(ayah.surahNumber, ayah.ayahNumber, selectedEdition)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .testTag("dedicated_tafsir_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AyahCardSheetHeader(
                title = "تفسير الآية",
                subtitle = "سورة ${ayah.surahNameAr} • الآية ${ayah.ayahNumber.toAppDigits()} • ${selectedEdition.titleAr}",
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                accentColor = accentColor,
                ayah = ayah,
                playbackState = playbackState,
                onPlayAyah = onPlayAyah,
                onTogglePlayPause = onTogglePlayPause,
                onDismiss = onDismiss
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TafsirEdition.entries.forEach { edition ->
                        val isSel = selectedEdition == edition
                        val label = when (edition) {
                            TafsirEdition.MUYASSAR -> "الميسر"
                            TafsirEdition.SAADI -> "السعدي"
                            TafsirEdition.IBN_KATHIR -> "ابن كثير"
                            TafsirEdition.BAGHAWI -> "البغوي"
                        }
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedEdition = edition },
                            shape = RoundedCornerShape(50),
                            colors = quranFilterChipColors(),
                            border = quranFilterChipBorder(isSel),
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("tafsir_chip_${edition.name.lowercase()}")
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { largeText = !largeText },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FormatSize,
                            contentDescription = "تغيير حجم خط التفسير",
                            tint = if (largeText) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (!tafsirState.isLoading && !tafsirState.text.isNullOrBlank()) {
                        IconButton(
                            onClick = {
                                copyTextToClipboard(
                                    context = context,
                                    label = "تفسير الآية",
                                    text = "${selectedEdition.titleAr} (${ayah.surahNameAr}: ${ayah.ayahNumber}):\n${tafsirState.text}"
                                )
                                onTafsirCopied()
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "نسخ التفسير",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            OrnamentalDivider()

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = Triple(tafsirState.isLoading, tafsirState.text, tafsirState.errorMessage),
                    label = "dedicated_tafsir_body"
                ) { (isLoading, text, err) ->
                    when {
                        isLoading -> Row(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text("جاري جلب التفسير...", style = MaterialTheme.typography.bodySmall)
                        }
                        text != null -> Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = if (largeText) 17.5.sp else 15.sp,
                                lineHeight = if (largeText) 31.sp else 27.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 90.dp, max = 320.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(14.dp)
                        )
                        else -> Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = err ?: "تعذّر تحميل التفسير",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { onLoadTafsir(ayah.surahNumber, ayah.ayahNumber, selectedEdition) }) {
                                Text("إعادة المحاولة")
                            }
                        }
                    }
                }
            }
        }
    }
}
