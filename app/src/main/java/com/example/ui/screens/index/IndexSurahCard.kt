package com.example.ui.screens.index

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SurahInfo
import com.example.domain.model.toAppDigits
import com.example.ui.components.RubElHizbBadge

@Composable
internal fun SurahDirectCard(
    surah: SurahInfo,
    revelationOrder: Int?,
    isActiveSurah: Boolean,
    isPlayingSurah: Boolean,
    onOpenSurah: () -> Unit,
    onTogglePlaySurah: () -> Unit,
    onShowInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pageRangeText = if (surah.firstPage == surah.lastPage) {
        "ص ${surah.firstPage.toAppDigits()}"
    } else {
        "ص ${surah.firstPage.toAppDigits()}–${surah.lastPage.toAppDigits()}"
    }
    val revBg = if (surah.isMakki) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.75f)
    }
    val revFg = if (surah.isMakki) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }
    val subtitle = buildString {
        append("${surah.ayahCount.toAppDigits()} آية • $pageRangeText")
        if (revelationOrder != null) {
            append(" • النزول ${revelationOrder.toAppDigits()}")
        }
    }

    Surface(
        onClick = onOpenSurah,
        shape = RoundedCornerShape(18.dp),
        color = if (isActiveSurah) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isActiveSurah) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("surah_card_${surah.number}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RubElHizbBadge(
                number = surah.number,
                size = 42.dp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "سورة ${surah.nameAr}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(revBg)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = surah.revelationType,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = revFg,
                            maxLines = 1
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SurahPlayActionButton(
                    surahNameAr = surah.nameAr,
                    surahNumber = surah.number,
                    isActiveSurah = isActiveSurah,
                    isPlaying = isPlayingSurah,
                    onClick = onTogglePlaySurah
                )
                SurahInfoActionButton(
                    surahNameAr = surah.nameAr,
                    surahNumber = surah.number,
                    onClick = onShowInfo
                )
            }
        }
    }
}

@Composable
internal fun SurahPlayActionButton(
    surahNameAr: String,
    surahNumber: Int,
    isActiveSurah: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isActiveSurah) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
        },
        border = if (isActiveSurah) null else BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = modifier
            .size(40.dp)
            .minimumInteractiveComponentSize()
            .testTag("surah_play_button_$surahNumber")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) {
                    "إيقاف تلاوة سورة $surahNameAr"
                } else {
                    "استماع لسورة $surahNameAr"
                },
                tint = if (isActiveSurah) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
internal fun SurahInfoActionButton(
    surahNameAr: String,
    surahNumber: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag("surah_info_button_$surahNumber")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "معلومات سورة $surahNameAr",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "معلومات",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1
            )
        }
    }
}
