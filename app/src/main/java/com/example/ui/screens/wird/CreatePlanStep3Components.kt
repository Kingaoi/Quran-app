package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.localizeDigits
import com.example.domain.wird.ALL_WEEK_DAYS_MASK
import com.example.domain.wird.CustomWirdPlanCalculator
import com.example.domain.wird.WirdAmountUnit
import com.example.domain.wird.isDayActiveInMask
import com.example.domain.wird.toggleDayInMask
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors
import java.util.Calendar
import kotlin.math.roundToInt

private val WEEK_DAYS_ORDERED = listOf(
    Calendar.SATURDAY to "السبت",
    Calendar.SUNDAY to "الأحد",
    Calendar.MONDAY to "الاثنين",
    Calendar.TUESDAY to "الثلاثاء",
    Calendar.WEDNESDAY to "الأربعاء",
    Calendar.THURSDAY to "الخميس",
    Calendar.FRIDAY to "الجمعة"
)

private val WORK_DAYS_MASK: Int = listOf(
    Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY
).fold(0) { acc, day -> acc or (1 shl (day - Calendar.SUNDAY)) }

private val FRIDAY_ONLY_MASK: Int = 1 shl (Calendar.FRIDAY - Calendar.SUNDAY)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PlanDaysSelectorSection(
    activeDaysMask: Int,
    onChangeMask: (Int) -> Unit
) {
    val qc = MaterialTheme.quranColors
    var expanded by remember { mutableStateOf(false) }
    var forceCustomVisible by remember { mutableStateOf(false) }
    val isCustomPreset = activeDaysMask != ALL_WEEK_DAYS_MASK &&
        activeDaysMask != WORK_DAYS_MASK &&
        activeDaysMask != FRIDAY_ONLY_MASK

    val currentLabel = when (activeDaysMask) {
        ALL_WEEK_DAYS_MASK -> "كل يوم"
        WORK_DAYS_MASK -> "الأحد - الخميس"
        FRIDAY_ONLY_MASK -> "يوم الجمعة"
        else -> WEEK_DAYS_ORDERED.filter { (d, _) -> isDayActiveInMask(activeDaysMask, d) }
            .joinToString("، ") { it.second }
            .ifBlank { "أيام محددة" }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "أيام الخطة",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = qc.text
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            val shape = RoundedCornerShape(14.dp)
            Surface(
                shape = shape,
                color = qc.surface2,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
                    .border(1.dp, qc.border, shape)
                    .clip(shape)
                    .clickable { expanded = true }
                    .testTag("wizard_days_dropdown")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = currentLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = qc.text,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = "اختر أيام الخطة", tint = qc.primary)
                }
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("كل يوم") },
                    onClick = {
                        forceCustomVisible = false
                        onChangeMask(ALL_WEEK_DAYS_MASK)
                        expanded = false
                    },
                    modifier = Modifier.testTag("wizard_days_preset_everyday")
                )
                DropdownMenuItem(
                    text = { Text("الأحد - الخميس") },
                    onClick = {
                        forceCustomVisible = false
                        onChangeMask(WORK_DAYS_MASK)
                        expanded = false
                    },
                    modifier = Modifier.testTag("wizard_days_preset_workdays")
                )
                DropdownMenuItem(
                    text = { Text("يوم الجمعة") },
                    onClick = {
                        forceCustomVisible = false
                        onChangeMask(FRIDAY_ONLY_MASK)
                        expanded = false
                    },
                    modifier = Modifier.testTag("wizard_days_preset_friday")
                )
                DropdownMenuItem(
                    text = { Text("أيام محددة") },
                    onClick = {
                        forceCustomVisible = true
                        expanded = false
                    },
                    modifier = Modifier.testTag("wizard_days_preset_custom")
                )
            }
        }

        if (forceCustomVisible || isCustomPreset) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WEEK_DAYS_ORDERED.forEach { (calDay, label) ->
                    val active = isDayActiveInMask(activeDaysMask, calDay)
                    FilterChip(
                        selected = active,
                        onClick = { onChangeMask(toggleDayInMask(activeDaysMask, calDay)) },
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(active),
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("wizard_day_$calDay")
                    )
                }
            }
        }
    }
}

@Composable
internal fun RabtAmountSelectorSection(
    rabtAmount: Float,
    label: String = "مقدار الربط (وجه)",
    onChangeRabt: (Float) -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    val presets = listOf(0f to "بدون ربط", 0.5f to "نصف وجه", 1f to "١ وجه", 2f to "وجهان", 5f to "٥ أوجه")
    val displayText = if (rabtAmount <= 0.01f) {
        "بدون ربط"
    } else {
        CustomWirdPlanCalculator.formatAmountAr(rabtAmount.toDouble(), WirdAmountUnit.PAGE)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = qc.text)
                Text(text = displayText, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = qc.primary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalIconButton(
                    onClick = {
                        val next = ((rabtAmount - 0.5f) * 2f).roundToInt() / 2f
                        onChangeRabt(next.coerceAtLeast(0f))
                    },
                    enabled = rabtAmount > 0f,
                    modifier = Modifier.size(48.dp).testTag("wizard_rabt_minus")
                ) { Icon(Icons.Rounded.Remove, contentDescription = "إنقاص الربط") }
                FilledTonalIconButton(
                    onClick = {
                        val next = ((rabtAmount + 0.5f) * 2f).roundToInt() / 2f
                        onChangeRabt(next.coerceAtMost(30f))
                    },
                    modifier = Modifier.size(48.dp).testTag("wizard_rabt_plus")
                ) { Icon(Icons.Rounded.Add, contentDescription = "زيادة الربط") }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presets.forEach { (valPages, chipLabel) ->
                val selected = kotlin.math.abs(rabtAmount - valPages) < 0.05f
                FilterChip(
                    selected = selected,
                    onClick = { onChangeRabt(valPages) },
                    colors = quranFilterChipColors(),
                    border = quranFilterChipBorder(selected),
                    label = { Text(chipLabel.localizeDigits(useWestern), style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }
}

@Composable
internal fun PlanDateSelectorCard(
    label: String,
    epochDay: Long?,
    tag: String,
    isRequired: Boolean = false,
    errorText: String? = null,
    onClear: (() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val hasError = !errorText.isNullOrBlank()
    val borderColor = if (hasError) qc.danger else qc.border
    val shape = RoundedCornerShape(14.dp)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier.fillMaxWidth()) {
        Surface(
            shape = shape,
            color = qc.surface2,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .border(if (hasError) 1.5.dp else 1.dp, borderColor, shape)
                .clip(shape)
                .clickable(onClick = onClick)
                .testTag(tag)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = label, style = MaterialTheme.typography.labelMedium, color = qc.textMuted)
                        if (isRequired && epochDay == null) {
                            Text(text = "مطلوب", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = qc.danger)
                        }
                    }
                    Text(
                        text = if (epochDay != null) CustomWirdPlanCalculator.formatEpochDayWithWeekdayAr(epochDay) else "لم يُحدَّد",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (epochDay != null) qc.text else if (hasError) qc.danger else qc.textMuted
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (epochDay != null && onClear != null) {
                        IconButton(onClick = onClear, modifier = Modifier.size(36.dp).testTag("${tag}_clear")) {
                            Icon(Icons.Rounded.Close, contentDescription = "مسح التاريخ", tint = qc.textMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = label, tint = if (hasError) qc.danger else qc.primary)
                }
            }
        }
        if (hasError) {
            Text(text = errorText!!, style = MaterialTheme.typography.bodySmall, color = qc.danger, modifier = Modifier.testTag("${tag}_error"))
        }
    }
}
