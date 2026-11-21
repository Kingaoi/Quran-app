package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.example.domain.wird.WirdGoalType
import com.example.domain.wird.WirdPlanType
import com.example.ui.theme.quranColors
import kotlin.math.roundToInt

private enum class DateTarget { AMOUNT_START, AMOUNT_END, DURATION_START, RANGE_START, RANGE_END }

@Composable
internal fun CreatePlanStep3GoalDates(
    planType: WirdPlanType,
    activeDaysMask: Int,
    goalType: WirdGoalType,
    amountValue: Float,
    amountUnit: WirdAmountUnit,
    rabtAmount: Float,
    reviewAmountValue: Float,
    reviewAmountUnit: WirdAmountUnit,
    targetDurationCount: Int,
    durationUnit: WirdDurationUnit,
    startEpochDay: Long,
    optionalAmountEndEpochDay: Long?,
    durationStartEpochDay: Long?,
    rangeEndEpochDay: Long?,
    metrics: CustomPlanMetrics,
    onChangeActiveDaysMask: (Int) -> Unit,
    onChangeGoalType: (WirdGoalType) -> Unit,
    onChangeAmountValue: (Float) -> Unit,
    onChangeAmountUnit: (WirdAmountUnit) -> Unit,
    onChangeRabtAmount: (Float) -> Unit,
    onChangeReviewAmountValue: (Float) -> Unit,
    onChangeReviewAmountUnit: (WirdAmountUnit) -> Unit,
    onChangeTargetDurationCount: (Int) -> Unit,
    onChangeDurationUnit: (WirdDurationUnit) -> Unit,
    onChangeStartEpochDay: (Long) -> Unit,
    onChangeOptionalAmountEndEpochDay: (Long?) -> Unit,
    onChangeDurationStartEpochDay: (Long?) -> Unit,
    onChangeRangeEndEpochDay: (Long?) -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    var dateTarget by remember { mutableStateOf<DateTarget?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        PlanDaysSelectorSection(activeDaysMask = activeDaysMask, onChangeMask = onChangeActiveDaysMask)

        Text(
            text = "نوع الهدف",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = qc.text
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WirdGoalType.entries.forEach { option ->
                val selected = goalType == option
                Surface(
                    onClick = { onChangeGoalType(option) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) qc.selected.copy(alpha = 0.18f) else qc.surface2,
                    border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) qc.primary else qc.border),
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("wizard_goal_${option.key}")
                ) {
                    Text(
                        text = option.labelAr,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (selected) qc.primary else qc.text,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp)
                    )
                }
            }
        }

        when (goalType) {
            WirdGoalType.AMOUNT -> {
                val primaryLabel = when (planType) {
                    WirdPlanType.MEMORIZATION, WirdPlanType.MEMORIZATION_AND_REVIEW -> "مقدار الحفظ اليومي"
                    WirdPlanType.REVIEW -> "مقدار المراجعة اليومي"
                    WirdPlanType.RECITATION -> "مقدار التلاوة اليومي"
                }
                DailyAmountGoalEditor(
                    label = primaryLabel,
                    amountValue = amountValue,
                    amountUnit = amountUnit,
                    onChangeAmountValue = onChangeAmountValue,
                    onChangeAmountUnit = onChangeAmountUnit
                )
                if (planType == WirdPlanType.MEMORIZATION || planType == WirdPlanType.MEMORIZATION_AND_REVIEW) {
                    RabtAmountSelectorSection(
                        rabtAmount = rabtAmount,
                        label = "مقدار الربط (وجه)",
                        onChangeRabt = onChangeRabtAmount
                    )
                }
                if (planType == WirdPlanType.MEMORIZATION_AND_REVIEW) {
                    DailyAmountGoalEditor(
                        label = "مقدار المراجعة اليومي",
                        amountValue = reviewAmountValue,
                        amountUnit = reviewAmountUnit,
                        unitDropdownTag = "wizard_review_unit_dropdown",
                        unitItemTagPrefix = "wizard_review_unit",
                        minusTag = "wizard_review_minus",
                        plusTag = "wizard_review_plus",
                        onChangeAmountValue = onChangeReviewAmountValue,
                        onChangeAmountUnit = onChangeReviewAmountUnit
                    )
                }
                PlanDateSelectorCard(
                    label = "تاريخ البدء",
                    epochDay = startEpochDay,
                    tag = "wizard_start_date_picker",
                    onClick = { dateTarget = DateTarget.AMOUNT_START }
                )
                val optionalEndError = if (optionalAmountEndEpochDay != null && optionalAmountEndEpochDay < startEpochDay) {
                    "تاريخ النهاية يجب أن يكون بعد أو يساوي تاريخ البدء"
                } else null
                PlanDateSelectorCard(
                    label = "تاريخ النهاية (اختياري)",
                    epochDay = optionalAmountEndEpochDay,
                    tag = "wizard_optional_end_date_picker",
                    errorText = optionalEndError,
                    onClear = { onChangeOptionalAmountEndEpochDay(null) },
                    onClick = { dateTarget = DateTarget.AMOUNT_END }
                )
                Text(
                    text = "التاريخ المتوقع للإتمام: ${CustomWirdPlanCalculator.formatEpochDayWithWeekdayAr(metrics.endEpochDay)} (${metrics.studyDaysCount.toAppDigits(useWestern)} يوم)",
                    style = MaterialTheme.typography.bodySmall,
                    color = qc.textMuted
                )
            }

            WirdGoalType.DURATION -> {
                TargetDurationEditor(
                    targetDurationCount = targetDurationCount,
                    durationUnit = durationUnit,
                    onChangeTargetDurationCount = onChangeTargetDurationCount,
                    onChangeDurationUnit = onChangeDurationUnit
                )
                RabtAmountSelectorSection(rabtAmount = rabtAmount, label = "مقدار الربط (وجه)", onChangeRabt = onChangeRabtAmount)
                val approxAyahs = metrics.dailyAyahsExact.roundToInt().coerceAtLeast(1).toAppDigits(useWestern)
                AutoCalculatedHighlightCard(
                    label = "المقدار اليومي",
                    value = "يُحسَب من التاريخ",
                    subtitle = if (durationStartEpochDay != null) {
                        "التقدير الحالي: ${CustomWirdPlanCalculator.formatAmountAr(metrics.dailyInUnit, amountUnit)} (~$approxAyahs آية/يوم)"
                    } else {
                        "سيظهر التقدير فور اختيار تاريخ البدء"
                    },
                    tag = "wizard_auto_daily_amount"
                )
                PlanDateSelectorCard(
                    label = "تاريخ البدء",
                    epochDay = durationStartEpochDay,
                    isRequired = true,
                    errorText = if (durationStartEpochDay == null) "اختر تاريخ البدء لحساب تاريخ النهاية والمتابعة" else null,
                    tag = "wizard_duration_start_date_picker",
                    onClick = { dateTarget = DateTarget.DURATION_START }
                )
                if (durationStartEpochDay != null) {
                    AutoCalculatedHighlightCard(
                        label = "تاريخ النهاية المتوقع",
                        value = CustomWirdPlanCalculator.formatEpochDayWithWeekdayAr(metrics.endEpochDay),
                        subtitle = "مدة العمل: ${metrics.studyDaysCount.toAppDigits(useWestern)} يوم",
                        tag = "wizard_auto_end_date"
                    )
                }
            }

            WirdGoalType.DATE_RANGE -> {
                PlanDateSelectorCard(
                    label = "تاريخ البدء",
                    epochDay = startEpochDay,
                    tag = "wizard_start_date_picker",
                    onClick = { dateTarget = DateTarget.RANGE_START }
                )
                val rangeError = when {
                    rangeEndEpochDay == null -> "اختر تاريخ النهاية للمتابعة"
                    rangeEndEpochDay < startEpochDay -> "تاريخ النهاية يجب أن يكون بعد أو يساوي تاريخ البدء"
                    else -> null
                }
                PlanDateSelectorCard(
                    label = "تاريخ النهاية",
                    epochDay = rangeEndEpochDay,
                    isRequired = true,
                    errorText = rangeError,
                    tag = "wizard_end_date_picker",
                    onClick = { dateTarget = DateTarget.RANGE_END }
                )
                val validRange = rangeEndEpochDay != null && rangeEndEpochDay >= startEpochDay
                val approxAyahs = metrics.dailyAyahsExact.roundToInt().coerceAtLeast(1).toAppDigits(useWestern)
                AutoCalculatedHighlightCard(
                    label = "المقدار اليومي",
                    value = "يُحسَب من التاريخ",
                    subtitle = if (validRange) {
                        "${CustomWirdPlanCalculator.formatAmountAr(metrics.dailyInUnit, amountUnit)} • ~$approxAyahs آية يومياً"
                    } else {
                        "يُحسَب تلقائياً بعد تحديد تاريخ النهاية"
                    },
                    tag = "wizard_auto_daily_amount"
                )
                RabtAmountSelectorSection(rabtAmount = rabtAmount, label = "مقدار الربط (جزء/وجه)", onChangeRabt = onChangeRabtAmount)
            }
        }
    }

    val initialPickerDay = when (dateTarget) {
        DateTarget.AMOUNT_START, DateTarget.RANGE_START -> startEpochDay
        DateTarget.AMOUNT_END -> optionalAmountEndEpochDay ?: startEpochDay
        DateTarget.DURATION_START -> durationStartEpochDay ?: CustomWirdPlanCalculator.currentEpochDay()
        DateTarget.RANGE_END -> rangeEndEpochDay ?: startEpochDay
        null -> startEpochDay
    }
    if (dateTarget != null) {
        PlanDatePickerDialog(
            initialEpochDay = initialPickerDay,
            onConfirmEpochDay = { picked ->
                when (dateTarget) {
                    DateTarget.AMOUNT_START -> onChangeStartEpochDay(picked)
                    DateTarget.AMOUNT_END -> onChangeOptionalAmountEndEpochDay(picked)
                    DateTarget.DURATION_START -> onChangeDurationStartEpochDay(picked)
                    DateTarget.RANGE_START -> onChangeStartEpochDay(picked)
                    DateTarget.RANGE_END -> onChangeRangeEndEpochDay(picked)
                    null -> Unit
                }
            },
            onDismiss = { dateTarget = null }
        )
    }
}

@Composable
private fun AutoCalculatedHighlightCard(
    label: String,
    value: String,
    subtitle: String? = null,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth().testTag(tag)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                )
            }
        }
    }
}
