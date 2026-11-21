package com.example.ui.screens.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PauseCircleOutline
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.AudioPlaybackState
import com.example.domain.model.AyahItem
import com.example.domain.model.toAppDigits
import com.example.domain.quran.QuranWaqfCatalog
import com.example.ui.theme.LocalQuranFontFamily
import com.example.ui.theme.quranColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PageAyahsBottomSheet(
    currentPage: Int,
    pageAyahs: List<AyahItem>,
    markerAccentColor: Color,
    playbackState: AudioPlaybackState = AudioPlaybackState(),
    getMutashabihatCount: (Int, Int) -> Int = { _, _ -> 0 },
    onSelectAyah: (AyahItem) -> Unit,
    onOpenPageWaqfGuide: () -> Unit = {},
    onPlayAyah: (Int, Int) -> Unit,
    onTogglePlayPause: () -> Unit = {},
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آيات الصفحة ${currentPage.toAppDigits()}",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                FilledTonalButton(
                    onClick = onOpenPageWaqfGuide,
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("page_ayahs_waqf_guide_button")
                ) {
                    Icon(Icons.Rounded.PauseCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("الوقوفات", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(pageAyahs, key = { "${it.surahNumber}:${it.ayahNumber}" }) { ayah ->
                    val waqfCount = remember(ayah.surahNumber, ayah.ayahNumber) {
                        QuranWaqfCatalog.countMarksInAyah(ayah)
                    }
                    val mutashabihatCount = remember(ayah.surahNumber, ayah.ayahNumber) {
                        getMutashabihatCount(ayah.surahNumber, ayah.ayahNumber)
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectAyah(ayah) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${ayah.surahNameAr} • الآية ${ayah.ayahNumber.toAppDigits()}",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (mutashabihatCount > 0) {
                                        MiniInfoBadge("متشابهات ${mutashabihatCount.toAppDigits()}")
                                    }
                                    if (waqfCount > 0) {
                                        MiniInfoBadge("وقف ${waqfCount.toAppDigits()}")
                                    }
                                }
                                val qc = MaterialTheme.quranColors
                                val isThisActive = playbackState.isActive &&
                                    playbackState.currentSurah == ayah.surahNumber &&
                                    playbackState.currentAyah == ayah.ayahNumber
                                val isPlaying = isThisActive && playbackState.isPlaying
                                val isBuffering = isThisActive && playbackState.isBuffering
                                val btnContainer = when {
                                    isPlaying -> qc.selected
                                    isThisActive -> qc.surface2
                                    else -> Color.Transparent
                                }
                                val btnTint = if (isPlaying) qc.onSelected else qc.primary
                                IconButton(
                                    onClick = {
                                        if (isThisActive) onTogglePlayPause()
                                        else onPlayAyah(ayah.surahNumber, ayah.ayahNumber)
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = btnContainer,
                                        contentColor = btnTint
                                    ),
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("sheet_play_ayah_${ayah.surahNumber}_${ayah.ayahNumber}")
                                ) {
                                    when {
                                        isBuffering -> CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = btnTint
                                        )
                                        isPlaying -> Icon(
                                            imageVector = Icons.Rounded.PauseCircle,
                                            contentDescription = "إيقاف التلاوة مؤقتاً"
                                        )
                                        isThisActive -> Icon(
                                            imageVector = Icons.Rounded.PlayCircle,
                                            contentDescription = "استكمال التلاوة"
                                        )
                                        else -> Icon(
                                            imageVector = Icons.Rounded.PlayCircle,
                                            contentDescription = "تشغيل الآية"
                                        )
                                    }
                                }
                            }
                            Text(
                                text = buildAnnotatedString {
                                    append(ayah.text)
                                    append(' ')
                                    withStyle(SpanStyle(color = markerAccentColor)) { append(ayah.marker) }
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = LocalQuranFontFamily.current,
                                    fontSize = 23.sp,
                                    lineHeight = 40.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniInfoBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
