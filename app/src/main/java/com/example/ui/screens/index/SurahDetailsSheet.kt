package com.example.ui.screens.index

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.AudioPlaybackState
import com.example.domain.model.SurahInfo
import com.example.domain.model.toAppDigits
import com.example.domain.quran.SurahFullDetails
import com.example.ui.components.OrnamentalDivider
import com.example.ui.components.RubElHizbBadge
import com.example.ui.theme.LocalQuranFontFamily

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun SurahDetailsModalSheet(
    details: SurahFullDetails,
    allSurahs: List<SurahInfo> = emptyList(),
    playbackState: AudioPlaybackState = AudioPlaybackState(),
    onOpenPage: (Int) -> Unit,
    onOpenAyah: (page: Int, surah: Int, ayah: Int) -> Unit,
    onTogglePlaySurah: (Int) -> Unit = {},
    onAddToPlaylist: (SurahInfo) -> Unit = {},
    onSelectSurahNumber: (Int) -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    val surah = details.surah
    val profile = details.profile
    val pageSpan = (surah.lastPage - surah.firstPage + 1).coerceAtLeast(1)
    val isThisSurahActive = playbackState.isActive && playbackState.currentSurah == surah.number
    val isThisSurahPlaying = isThisSurahActive && playbackState.isPlaying
    val juzSummary = when {
        details.juzNumbers.isEmpty() -> "—"
        details.juzNumbers.size == 1 -> "الجزء ${details.juzNumbers.first().toAppDigits()}"
        else -> "ج ${details.juzNumbers.first().toAppDigits()}–${details.juzNumbers.last().toAppDigits()}"
    }
    LaunchedEffect(surah.number) { scrollState.scrollTo(0) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp)
                .padding(bottom = 26.dp)
                .testTag("surah_details_sheet_${surah.number}"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RubElHizbBadge(
                    number = surah.number,
                    size = 48.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "سورة ${surah.nameAr}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = LocalQuranFontFamily.current,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${surah.nameEn} • ترتيب المصحف: ${surah.number.toAppDigits()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp).testTag("close_surah_details_button")
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "إغلاق نافذة معلومات السورة", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SurahTagPill(
                    text = surah.revelationType,
                    containerColor = if (surah.isMakki) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = if (surah.isMakki) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onTertiaryContainer
                )
                SurahTagPill(
                    text = "ترتيب النزول: ${profile.revelationOrder.toAppDigits()}",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
                SurahTagPill(
                    text = if (surah.hasBasmalah) "تفتتح بالبسملة" else "بدون بسملة",
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (details.sajdahPositions.isNotEmpty()) {
                    SurahTagPill(
                        text = "${details.sajdahPositions.size.toAppDigits()} سجدة تلاوة",
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { onDismiss(); onOpenPage(surah.firstPage) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1.1f).heightIn(min = 48.dp).testTag("surah_details_read_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("قراءة ص ${surah.firstPage.toAppDigits()}", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                }
                FilledTonalButton(
                    onClick = { onTogglePlaySurah(surah.number) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(0.9f).heightIn(min = 48.dp).testTag("surah_details_play_button")
                ) {
                    Icon(
                        imageVector = when {
                            isThisSurahPlaying -> Icons.Rounded.Pause
                            isThisSurahActive -> Icons.Rounded.PlayArrow
                            else -> Icons.Rounded.Headphones
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when {
                            isThisSurahPlaying -> "إيقاف مؤقت"
                            isThisSurahActive -> "متابعة"
                            else -> "استماع"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                }
                OutlinedIconButton(
                    onClick = { onDismiss(); onAddToPlaylist(surah) },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.size(48.dp).testTag("surah_details_add_playlist_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, contentDescription = "إضافة سورة ${surah.nameAr} إلى قائمة تلاوة", tint = MaterialTheme.colorScheme.primary)
                }
            }

            OrnamentalDivider()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SurahStatTile(
                        label = "الآيات",
                        value = surah.ayahCount.toAppDigits(),
                        modifier = Modifier.weight(1f)
                    )
                    SurahStatTile(
                        label = "الكلمات",
                        value = if (surah.wordCount > 0) surah.wordCount.toAppDigits() else "—",
                        modifier = Modifier.weight(1f)
                    )
                    SurahStatTile(
                        label = "الحروف",
                        value = if (surah.letterCount > 0) surah.letterCount.toAppDigits() else "—",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SurahStatTile(
                        label = "الصفحات (${pageSpan.toAppDigits()})",
                        value = "${surah.firstPage.toAppDigits()}–${surah.lastPage.toAppDigits()}",
                        modifier = Modifier.weight(1f)
                    )
                    SurahStatTile(
                        label = "الأجزاء",
                        value = juzSummary,
                        modifier = Modifier.weight(1f)
                    )
                    SurahStatTile(
                        label = "الأرباع الحزبية",
                        value = "${details.quartersCount.toAppDigits()} ربع",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            SurahDetailsRichSections(
                details = details,
                onOpenAyah = { page, sNum, aNum ->
                    onDismiss()
                    onOpenAyah(page, sNum, aNum)
                }
            )

            SurahGroupsDetailsSection(
                currentSurahNumber = surah.number,
                allSurahs = allSurahs,
                onSelectSurahNumber = onSelectSurahNumber
            )
        }
    }
}

@Composable
private fun SurahTagPill(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun SurahStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
