package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.Badge
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.ui.navigation.QuranSharedKey
import com.example.ui.navigation.quranSharedElement
import com.example.ui.theme.quranColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WirdTopBar(
    hasActivePlan: Boolean,
    customPlansCount: Int,
    subtitleText: String,
    onOpenAllPlans: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenDua: () -> Unit,
    onOpenSchedule: () -> Unit,
    onShareProgress: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "الورد اليومي",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = qc.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall,
                    color = qc.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box {
                    FilledTonalIconButton(
                        onClick = onOpenAllPlans,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = qc.primary,
                            contentColor = qc.onPrimary
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("wird_all_plans_topbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.LibraryBooks,
                            contentDescription = "كل الخطط",
                            modifier = Modifier.quranSharedElement(
                                QuranSharedKey.WirdPlansAnchor,
                                zIndexInOverlay = 1f
                            )
                        )
                    }
                    if (customPlansCount > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text(customPlansCount.toAppDigits(useWestern))
                        }
                    }
                }

                FilledTonalIconButton(
                    onClick = onOpenStats,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("wird_stats_topbar_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BarChart,
                        contentDescription = "إحصائيات القراءة"
                    )
                }
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WirdTopActionPill(
                label = "دعاء الختم",
                icon = Icons.Rounded.VolunteerActivism,
                containerColor = qc.surface2,
                contentColor = qc.primary,
                onClick = onOpenDua,
                modifier = Modifier.testTag("wird_dua_topbar_button")
            )

            if (hasActivePlan) {
                WirdTopActionPill(
                    label = "جدول الأوراد",
                    icon = Icons.AutoMirrored.Rounded.FormatListBulleted,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.primary,
                    onClick = onOpenSchedule,
                    modifier = Modifier.testTag("wird_schedule_topbar_button")
                )

                WirdTopActionPill(
                    label = "مشاركة الورد",
                    icon = Icons.Rounded.Share,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    onClick = onShareProgress,
                    modifier = Modifier.testTag("wird_share_topbar_button")
                )
            }
        }
    }
}

@Composable
private fun WirdTopActionPill(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        modifier = modifier.minimumInteractiveComponentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = contentColor,
                maxLines = 1
            )
        }
    }
}
