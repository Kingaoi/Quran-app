package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ReadingStats
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranColors

@Composable
internal fun WirdStatsCard(
    stats: ReadingStats,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("wird_stats_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "ملخص القراءة والأذكار",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMiniTile(
                    icon = Icons.Rounded.LocalFireDepartment,
                    value = "${stats.currentStreakDays.toAppDigits(useWestern)} ي",
                    label = "السلسلة الحالية",
                    modifier = Modifier.weight(1f)
                )
                StatMiniTile(
                    icon = Icons.Rounded.WorkspacePremium,
                    value = "${stats.longestStreakDays.toAppDigits(useWestern)} ي",
                    label = "أطول سلسلة",
                    modifier = Modifier.weight(1f)
                )
                StatMiniTile(
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    value = stats.totalPagesRead.toAppDigits(useWestern),
                    label = "الصفحات",
                    modifier = Modifier.weight(1f)
                )
                StatMiniTile(
                    icon = Icons.Rounded.Verified,
                    value = stats.completedKhatmahsCount.toAppDigits(useWestern),
                    label = "الختمات",
                    modifier = Modifier.weight(1f)
                )
            }

            val stripBrush = Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.42f)
                )
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = qc.surface,
                border = BorderStroke(1.dp, qc.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wird_stats_adhkar_strip")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(stripBrush)
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(qc.accent.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.VolunteerActivism,
                                contentDescription = null,
                                tint = qc.accent,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = "الأذكار اليوم: ${stats.adhkarTodayCount.toAppDigits(useWestern)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = qc.text
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = qc.surface.copy(alpha = 0.88f),
                        border = BorderStroke(1.dp, qc.border)
                    ) {
                        Text(
                            text = "الإجمالي: ${stats.adhkarTotalCount.toAppDigits(useWestern)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = qc.primary,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMiniTile(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = qc.surface,
        border = BorderStroke(1.dp, qc.border),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = qc.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = qc.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = qc.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
