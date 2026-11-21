package com.example.ui.screens.wird

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.JuzInfo
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.domain.wird.KhatmahPlanConfig
import com.example.domain.wird.WirdPlanCalculator
import com.example.domain.wird.WirdPlanSummary
import com.example.domain.wird.WirdScheduleStatus
import com.example.ui.theme.quranColors

@Composable
internal fun WirdProgressCard(
    plan: WirdPlanSummary,
    config: KhatmahPlanConfig,
    ajza: List<JuzInfo>,
    onOpenSchedule: () -> Unit
) {
    val useWestern = LocalUseWesternDigits.current
    val animatedProgress by animateFloatAsState(
        targetValue = plan.progressFraction,
        animationSpec = tween(durationMillis = 360),
        label = "khatmah_progress"
    )
    val startLabel = WirdPlanCalculator.formatStartPointAr(config.startJuz, ajza)
    val portionLabel = WirdPlanCalculator.formatDailyPortionSummaryAr(config.quartersPerDay)
    val percent = (plan.progressFraction * 100f).toInt().coerceIn(0, 100)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(22.dp))
            .testTag("wird_progress_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الختمة الحالية",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$startLabel • $portionLabel يومياً",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!plan.isCompleted) {
                    ScheduleStatusPill(status = plan.scheduleStatus)
                } else {
                    val qc = MaterialTheme.quranColors
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = qc.successContainer
                    ) {
                        Text(
                            text = "مكتملة ١٠٠٪",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = qc.success,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                )
                Text(
                    text = "${percent.toAppDigits(useWestern)}٪",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WirdStatChip(
                    label = "الأوراد السابقة",
                    count = plan.completedCount,
                    useWestern = useWestern,
                    onClick = onOpenSchedule,
                    tag = "wird_past_count_chip",
                    modifier = Modifier.weight(1f)
                )
                WirdStatChip(
                    label = "الأوراد القادمة",
                    count = plan.upcomingCount,
                    useWestern = useWestern,
                    onClick = onOpenSchedule,
                    tag = "wird_upcoming_count_chip",
                    modifier = Modifier.weight(1f)
                )
            }

            TextButton(
                onClick = onOpenSchedule,
                modifier = Modifier
                    .align(Alignment.Start)
                    .testTag("wird_view_schedule_text_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.FormatListBulleted,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text("عرض جدول الأوراد", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ScheduleStatusPill(status: WirdScheduleStatus) {
    val qc = MaterialTheme.quranColors
    val (bg, fg, icon) = when (status) {
        is WirdScheduleStatus.Behind -> Triple(
            qc.dangerContainer,
            qc.danger,
            Icons.Rounded.WarningAmber
        )
        is WirdScheduleStatus.Ahead -> Triple(
            qc.successContainer,
            qc.success,
            Icons.AutoMirrored.Rounded.TrendingUp
        )
        WirdScheduleStatus.OnTrack -> Triple(
            qc.successContainer,
            qc.success,
            Icons.Rounded.CheckCircleOutline
        )
    }

    Surface(shape = RoundedCornerShape(50), color = bg) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
            Text(
                text = WirdPlanCalculator.formatScheduleStatusAr(status),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = fg
            )
        }
    }
}

@Composable
private fun WirdStatChip(
    label: String,
    count: Int,
    useWestern: Boolean,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .clip(shape)
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = count.toAppDigits(useWestern),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
