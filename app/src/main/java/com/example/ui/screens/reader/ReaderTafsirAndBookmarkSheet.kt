package com.example.ui.screens.reader

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AyahBookmark
import com.example.domain.model.AyahItem
import com.example.domain.model.TafsirEdition
import com.example.ui.components.OrnamentalDivider
import com.example.ui.util.copyTextToClipboard
import com.example.ui.viewmodel.AyahTafsirUiState

@Composable
internal fun AyahTafsirAndBookmarkSection(
    ayah: AyahItem,
    existingBookmark: AyahBookmark?,
    tafsirState: AyahTafsirUiState,
    onLoadTafsir: (Int, Int, TafsirEdition) -> Unit,
    onSaveBookmark: (AyahBookmark) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onTafsirCopied: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedEdition by remember(ayah) { mutableStateOf(tafsirState.edition) }
    var largeTafsirText by remember { mutableStateOf(false) }

    LaunchedEffect(ayah.surahNumber, ayah.ayahNumber, selectedEdition) {
        if (tafsirState.surahNumber != ayah.surahNumber ||
            tafsirState.ayahNumber != ayah.ayahNumber ||
            tafsirState.edition != selectedEdition ||
            (tafsirState.text == null && !tafsirState.isLoading)
        ) {
            onLoadTafsir(ayah.surahNumber, ayah.ayahNumber, selectedEdition)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
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
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "التفسير الميسّر والمختار",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = selectedEdition.titleAr,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { largeTafsirText = !largeTafsirText },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FormatSize,
                                contentDescription = if (largeTafsirText) "تصغير خط التفسير" else "تكبير خط التفسير",
                                tint = if (largeTafsirText) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TafsirEdition.entries.forEach { edition ->
                        val isSel = selectedEdition == edition
                        val chipLabel = when (edition) {
                            TafsirEdition.MUYASSAR -> "الميسر"
                            TafsirEdition.SAADI -> "السعدي"
                            TafsirEdition.IBN_KATHIR -> "ابن كثير"
                            TafsirEdition.BAGHAWI -> "البغوي"
                        }
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedEdition = edition },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSel,
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                            ),
                            label = { Text(text = chipLabel, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("tafsir_chip_${edition.name.lowercase()}")
                        )
                    }
                }

                OrnamentalDivider()

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = Triple(tafsirState.isLoading, tafsirState.text, tafsirState.errorMessage),
                        label = "tafsir_content_transition"
                    ) { (isLoading, text, err) ->
                        when {
                            isLoading -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Text("جاري جلب التفسير...", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            text != null -> {
                                val fontSize = if (largeTafsirText) 17.sp else 14.5.sp
                                val lineHeight = if (largeTafsirText) 30.sp else 26.sp
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = fontSize,
                                        lineHeight = lineHeight
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 76.dp, max = 250.dp)
                                        .verticalScroll(rememberScrollState())
                                        .padding(12.dp)
                                )
                            }
                            else -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = err ?: "تعذّر تحميل التفسير",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(
                                        onClick = { onLoadTafsir(ayah.surahNumber, ayah.ayahNumber, selectedEdition) }
                                    ) { Text("إعادة المحاولة") }
                                }
                            }
                        }
                    }
                }
            }
        }

        AyahBookmarkReflectionCard(
            ayah = ayah,
            existingBookmark = existingBookmark,
            onSaveBookmark = onSaveBookmark,
            onDeleteBookmark = onDeleteBookmark
        )
    }
}
