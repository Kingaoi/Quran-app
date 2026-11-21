package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.AudioPlaybackState
import com.example.domain.model.toArabicIndicDigits
import com.example.domain.wird.WirdPortion
import com.example.ui.theme.LocalQuranFontFamily

@Composable
internal fun WirdHeroCard(
    wird: WirdPortion,
    totalWirds: Int,
    hasStartedCurrentWird: Boolean,
    resumePage: Int,
    playbackState: AudioPlaybackState = AudioPlaybackState(),
    onOpenReader: () -> Unit,
    onToggleListenWird: () -> Unit = {},
    onCompleteWird: () -> Unit
) {
    val quranFont = LocalQuranFontFamily.current
    val isThisWirdAudio = playbackState.isActive && playbackState.wirdAudioRange?.wirdIndex == wird.index
    val isPlayingThisWird = isThisWirdAudio && playbackState.isPlaying
    val isBufferingThisWird = isThisWirdAudio && playbackState.isBuffering

    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(26.dp)
                )
                .testTag("wird_hero_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "من قوله تعالى",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "الورد ${(wird.index + 1).toArabicIndicDigits()} من ${totalWirds.toArabicIndicDigits()}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = wird.juzNameAr,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceContainer
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = wird.openingAyahText,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = quranFont,
                            fontSize = 25.sp,
                            lineHeight = 40.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WirdBoundaryRow(
                        label = "سورة ${wird.startSurahNameAr} - آية ${wird.startAyahNumber.toArabicIndicDigits()}",
                        pageText = "صفحة ${wird.startPage.toArabicIndicDigits()}"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
                    WirdBoundaryRow(
                        label = "إلى سورة ${wird.endSurahNameAr} - آية ${wird.endAyahNumber.toArabicIndicDigits()}",
                        pageText = "صفحة ${wird.endPage.toArabicIndicDigits()}"
                    )
                }

                if (hasStartedCurrentWird) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "توقفت عند صفحة ${resumePage.toArabicIndicDigits()} من أصل ${wird.pageSpanCount.toArabicIndicDigits()} صفحات لهذا الورد",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
                if (isThisWirdAudio) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth().testTag("wird_live_audio_strip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "سورة ${playbackState.surahNameAr} • آية ${playbackState.currentAyah.toArabicIndicDigits()} (${playbackState.reciter.nameAr})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        WirdHeroActionsColumn(
            hasStartedCurrentWird = hasStartedCurrentWird,
            isThisWirdAudio = isThisWirdAudio,
            isPlayingThisWird = isPlayingThisWird,
            isBufferingThisWird = isBufferingThisWird,
            onOpenReader = onOpenReader,
            onToggleListenWird = onToggleListenWird,
            onCompleteWird = onCompleteWird
        )
    }
}
