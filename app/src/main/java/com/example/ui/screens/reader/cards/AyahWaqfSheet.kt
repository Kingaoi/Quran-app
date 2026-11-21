package com.example.ui.screens.reader.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.PauseCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.AudioPlaybackState
import com.example.domain.model.AyahItem
import com.example.domain.model.toAppDigits
import com.example.domain.quran.DetectedWaqfMark
import com.example.domain.quran.QuranWaqfCatalog
import com.example.domain.quran.WaqfMarkType
import com.example.ui.components.OrnamentalDivider
import com.example.ui.theme.LocalQuranFontFamily
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

private enum class WaqfSheetTab(val labelAr: String) {
    THIS_AYAH("في هذه الآية"),
    THIS_PAGE("علامات الصفحة"),
    ALL_GUIDE("دليل الوقوفات")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AyahWaqfSheet(
    ayah: AyahItem,
    pageAyahs: List<AyahItem>,
    accentColor: Color,
    playbackState: AudioPlaybackState? = null,
    onPlayAyah: ((Int, Int) -> Unit)? = null,
    onTogglePlayPause: (() -> Unit)? = null,
    initialTabPage: Boolean = false,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val ayahMarks = remember(ayah) { QuranWaqfCatalog.analyzeAyah(ayah) }
    val pageMarks = remember(pageAyahs) { QuranWaqfCatalog.analyzePage(pageAyahs) }
    var activeTab by remember(ayah, initialTabPage) {
        mutableStateOf(
            when {
                initialTabPage -> WaqfSheetTab.THIS_PAGE
                ayahMarks.isNotEmpty() -> WaqfSheetTab.THIS_AYAH
                pageMarks.isNotEmpty() -> WaqfSheetTab.THIS_PAGE
                else -> WaqfSheetTab.ALL_GUIDE
            }
        )
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
                .testTag("dedicated_waqf_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AyahCardSheetHeader(
                title = "الوقوفات وعلامات المصحف",
                subtitle = "سورة ${ayah.surahNameAr} • الآية ${ayah.ayahNumber.toAppDigits()} • صفحة ${ayah.pageNumber.toAppDigits()}",
                icon = Icons.Rounded.PauseCircleOutline,
                accentColor = accentColor,
                ayah = ayah,
                showAyahQuote = activeTab == WaqfSheetTab.THIS_AYAH,
                playbackState = playbackState,
                onPlayAyah = onPlayAyah,
                onTogglePlayPause = onTogglePlayPause,
                onDismiss = onDismiss
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WaqfSheetTab.entries.forEach { tab ->
                    val countSuffix = when (tab) {
                        WaqfSheetTab.THIS_AYAH -> if (ayahMarks.isNotEmpty()) " (${ayahMarks.size.toAppDigits()})" else ""
                        WaqfSheetTab.THIS_PAGE -> if (pageMarks.isNotEmpty()) " (${pageMarks.size.toAppDigits()})" else ""
                        WaqfSheetTab.ALL_GUIDE -> ""
                    }
                    val selected = activeTab == tab
                    FilterChip(
                        selected = selected,
                        onClick = { activeTab = tab },
                        shape = RoundedCornerShape(50),
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(selected),
                        label = { Text("${tab.labelAr}$countSuffix", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            OrnamentalDivider()

            when (activeTab) {
                WaqfSheetTab.THIS_AYAH -> {
                    if (ayahMarks.isEmpty()) {
                        EmptyWaqfNote("لا توجد علامات وقف أو سكت ظاهرة داخل هذه الآية، ويمكنك استعراض علامات الصفحة أو الدليل الشامل.")
                    } else {
                        ayahMarks.forEach { DetectedWaqfCard(it, showAyahRef = false) }
                    }
                }
                WaqfSheetTab.THIS_PAGE -> {
                    if (pageMarks.isEmpty()) {
                        EmptyWaqfNote("لا توجد علامات وقف خاصة في آيات هذه الصفحة.")
                    } else {
                        pageMarks.forEach { DetectedWaqfCard(it, showAyahRef = true) }
                    }
                }
                WaqfSheetTab.ALL_GUIDE -> {
                    WaqfMarkType.entries.forEach { type ->
                        WaqfGuideItemCard(type = type)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetectedWaqfCard(mark: DetectedWaqfMark, showAyahRef: Boolean) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WaqfSymbolMedallion(mark.type.symbolGlyph)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mark.type.titleAr,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    val sub = if (showAyahRef) {
                        "${mark.surahNameAr} (${mark.ayahNumber.toAppDigits()}) • ${mark.type.rulingAr}"
                    } else {
                        mark.type.rulingAr
                    }
                    Text(
                        text = sub,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (mark.snippet.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "﴿... ${mark.snippet} ...﴾",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = LocalQuranFontFamily.current,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Text(
                text = mark.type.explanationAr,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 21.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "العمل: ${mark.type.recitationTipAr}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun WaqfGuideItemCard(type: WaqfMarkType) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            WaqfSymbolMedallion(type.symbolGlyph)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(type.titleAr, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text(
                            text = type.badgeShort,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = type.explanationAr,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WaqfSymbolMedallion(symbol: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = LocalQuranFontFamily.current,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EmptyWaqfNote(message: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 22.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(14.dp)
        )
    }
}
