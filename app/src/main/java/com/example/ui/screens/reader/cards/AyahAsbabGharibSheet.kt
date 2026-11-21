package com.example.ui.screens.reader.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.AudioPlaybackState
import com.example.domain.model.AyahItem
import com.example.domain.model.toAppDigits
import com.example.domain.quran.AsbabEntry
import com.example.domain.quran.QuranSciencesResolver
import com.example.ui.components.OrnamentalDivider
import com.example.ui.theme.LocalQuranFontFamily
import com.example.ui.util.copyTextToClipboard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AyahAsbabGharibSheet(
    ayah: AyahItem,
    accentColor: Color,
    playbackState: AudioPlaybackState? = null,
    onPlayAyah: ((Int, Int) -> Unit)? = null,
    onTogglePlayPause: (() -> Unit)? = null,
    onCopied: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val info = remember(ayah.surahNumber, ayah.ayahNumber) {
        QuranSciencesResolver.resolveForAyah(ayah)
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
                .testTag("dedicated_asbab_gharib_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AyahCardSheetHeader(
                title = "أسباب النزول وغريب الكلمات",
                subtitle = "سورة ${ayah.surahNameAr} • الآية ${ayah.ayahNumber.toAppDigits()}",
                icon = Icons.Rounded.AutoStories,
                accentColor = accentColor,
                ayah = ayah,
                playbackState = playbackState,
                onPlayAyah = onPlayAyah,
                onTogglePlayPause = onTogglePlayPause,
                onDismiss = onDismiss
            )

            OrnamentalDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.HistoryEdu,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "أسباب النزول وسياق السورة",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (info.asbab.isNotEmpty()) {
                info.asbab.forEach { entry ->
                    AsbabNarrationCard(entry = entry)
                }
            }
            info.surahContext?.let { ctx ->
                AsbabNarrationCard(entry = ctx, isContext = true)
            }
            if (info.asbab.isEmpty() && info.surahContext == null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "نزلت هذه الآية ابتداءً لتقرير الهداية والتشريع دون رواية خاصة منفردة في سبب نزولها.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Translate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "معاني الكلمات الغريبة",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                if (info.gharibWords.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val summary = info.gharibWords.joinToString("\n") { "• ${it.word}: ${it.meaning}" }
                            copyTextToClipboard(context, "غريب الآية", summary)
                            onCopied()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "نسخ معاني الكلمات",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (info.gharibWords.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ألفاظ هذه الآية واضحة الدلالة، ويمكنك مراجعة «التفسير» للاطلاع على الشرح التفصيلي لمعانيها.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    info.gharibWords.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = item.word,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = LocalQuranFontFamily.current,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                                Text(
                                    text = item.meaning,
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 21.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AsbabNarrationCard(
    entry: AsbabEntry,
    isContext: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isContext) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isContext) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                ) {
                    Text(
                        text = if (isContext) "مقصد السورة" else "سبب النزول",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = entry.narration,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 25.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "المصدر: ${entry.source}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
