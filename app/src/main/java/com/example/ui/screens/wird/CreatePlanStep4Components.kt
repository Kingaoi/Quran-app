package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.domain.wird.CustomPlanMetrics
import com.example.domain.wird.CustomWirdPlanCalculator
import com.example.domain.wird.WirdAmountUnit
import com.example.domain.wird.WirdDurationUnit
import com.example.domain.wird.WirdPlanType
import com.example.domain.wird.WirdSessionSlot
import com.example.ui.theme.quranColors
import kotlin.math.roundToInt

@Composable
internal fun SessionTimeSlotCard(
    slot: WirdSessionSlot,
    perSessionLabel: String,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = qc.surface2,
        border = BorderStroke(1.dp, qc.border),
        modifier = Modifier.fillMaxWidth().testTag("wizard_session_card_${slot.index}")
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الجلسة ${(slot.index + 1).toAppDigits(useWestern)}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = qc.text
                )
                Text(
                    text = perSessionLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = qc.primary
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimePill(
                    prefix = "من",
                    timeText = CustomWirdPlanCalculator.formatTime12hAr(slot.startHour, slot.startMinute),
                    tag = "wizard_session_start_${slot.index}",
                    onClick = onPickStart,
                    modifier = Modifier.weight(1f)
                )
                TimePill(
                    prefix = "إلى",
                    timeText = CustomWirdPlanCalculator.formatTime12hAr(slot.endHour, slot.endMinute),
                    tag = "wizard_session_end_${slot.index}",
                    onClick = onPickEnd,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TimePill(
    prefix: String,
    timeText: String,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val shape = RoundedCornerShape(12.dp)
    Surface(
        shape = shape,
        color = qc.surface,
        modifier = modifier
            .heightIn(min = 48.dp)
            .border(1.dp, qc.border, shape)
            .clip(shape)
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$prefix: $timeText",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = qc.text
            )
            Icon(Icons.Rounded.Schedule, contentDescription = null, tint = qc.primary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
internal fun PlanSummaryBottomCard(
    planTitle: String,
    planType: WirdPlanType,
    rabtAmount: Float,
    reviewAmountValue: Float,
    reviewAmountUnit: WirdAmountUnit,
    sessionsPerDay: Int,
    metrics: CustomPlanMetrics,
    dailyLabel: String,
    perSessionLabel: String
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth().testTag("wizard_bottom_summary")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "ملخص الخطة • ${planTitle.trim()} (${planType.labelAr})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = qc.primary
            )
            HorizontalDivider(color = qc.border.copy(alpha = 0.5f))
            val durationText = CustomWirdPlanCalculator.formatDurationUnitAr(metrics.studyDaysCount, WirdDurationUnit.DAY)
            SummaryRow(
                label = "مدة الخطة",
                value = "$durationText (${CustomWirdPlanCalculator.formatEpochDayAr(metrics.startEpochDay)} - ${CustomWirdPlanCalculator.formatEpochDayAr(metrics.endEpochDay)})"
            )
            val approxAyahs = metrics.dailyAyahsExact.roundToInt().coerceAtLeast(1).toAppDigits(useWestern)
            SummaryRow(
                label = "المقدار اليومي",
                value = "$dailyLabel (~$approxAyahs آية)" + if (sessionsPerDay > 1) " • $perSessionLabel/جلسة" else ""
            )
            if (planType == WirdPlanType.MEMORIZATION_AND_REVIEW) {
                SummaryRow(
                    label = "مقدار المراجعة",
                    value = CustomWirdPlanCalculator.formatAmountAr(reviewAmountValue.toDouble(), reviewAmountUnit)
                )
            }
            if ((planType == WirdPlanType.MEMORIZATION || planType == WirdPlanType.MEMORIZATION_AND_REVIEW) && rabtAmount > 0.01f) {
                SummaryRow(
                    label = "مقدار الربط",
                    value = CustomWirdPlanCalculator.formatAmountAr(rabtAmount.toDouble(), WirdAmountUnit.PAGE)
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val qc = MaterialTheme.quranColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(text = "$label:", style = MaterialTheme.typography.labelMedium, color = qc.textMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = qc.text,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        )
    }
}
