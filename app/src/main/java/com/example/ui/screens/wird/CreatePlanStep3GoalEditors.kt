package com.example.ui.screens.wird

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import com.example.domain.wird.CustomWirdPlanCalculator
import com.example.domain.wird.WirdAmountUnit
import com.example.domain.wird.WirdDurationUnit
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

@Composable
internal fun DailyAmountGoalEditor(
    label: String,
    amountValue: Float,
    amountUnit: WirdAmountUnit,
    unitDropdownTag: String = "wizard_unit_dropdown",
    unitItemTagPrefix: String = "wizard_unit",
    minusTag: String = "wizard_amount_minus",
    plusTag: String = "wizard_amount_plus",
    onChangeAmountValue: (Float) -> Unit,
    onChangeAmountUnit: (WirdAmountUnit) -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    var unitMenuExpanded by remember { mutableStateOf(false) }

    val step = if (amountUnit == WirdAmountUnit.AYAH) 1f else 0.5f
    val minVal = if (amountUnit == WirdAmountUnit.AYAH) 1f else 0.5f
    val presets = when (amountUnit) {
        WirdAmountUnit.PAGE -> listOf(0.5f, 1f, 1.5f, 2f, 3f, 5f)
        WirdAmountUnit.AYAH -> listOf(3f, 5f, 10f, 15f, 20f, 30f)
        WirdAmountUnit.QUARTER -> listOf(1f, 2f, 4f, 8f)
        WirdAmountUnit.HIZB -> listOf(0.5f, 1f, 2f, 4f)
        WirdAmountUnit.JUZ -> listOf(0.5f, 1f, 2f, 3f)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = qc.text
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = CustomWirdPlanCalculator.formatAmountAr(amountValue.toDouble(), amountUnit),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = qc.primary,
                modifier = Modifier.weight(1f)
            )

            Box {
                val shape = RoundedCornerShape(12.dp)
                Surface(
                    shape = shape,
                    color = qc.surface2,
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .border(1.dp, qc.border, shape)
                        .clip(shape)
                        .clickable { unitMenuExpanded = true }
                        .testTag(unitDropdownTag)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = amountUnit.singularAr,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = qc.text
                        )
                        Icon(Icons.Rounded.ArrowDropDown, contentDescription = "اختر الوحدة", tint = qc.primary)
                    }
                }

                DropdownMenu(expanded = unitMenuExpanded, onDismissRequest = { unitMenuExpanded = false }) {
                    WirdAmountUnit.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.singularAr) },
                            onClick = {
                                onChangeAmountUnit(unit)
                                unitMenuExpanded = false
                            },
                            modifier = Modifier.testTag("${unitItemTagPrefix}_${unit.key}")
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalIconButton(
                    onClick = { onChangeAmountValue((amountValue - step).coerceAtLeast(minVal)) },
                    enabled = amountValue > minVal,
                    modifier = Modifier.size(48.dp).testTag(minusTag)
                ) { Icon(Icons.Rounded.Remove, contentDescription = "إنقاص المقدار") }
                FilledTonalIconButton(
                    onClick = { onChangeAmountValue((amountValue + step).coerceAtMost(200f)) },
                    modifier = Modifier.size(48.dp).testTag(plusTag)
                ) { Icon(Icons.Rounded.Add, contentDescription = "زيادة المقدار") }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presets.forEach { preset ->
                val sel = kotlin.math.abs(amountValue - preset) < 0.05f
                FilterChip(
                    selected = sel,
                    onClick = { onChangeAmountValue(preset) },
                    colors = quranFilterChipColors(),
                    border = quranFilterChipBorder(sel),
                    label = {
                        Text(
                            CustomWirdPlanCalculator.formatAmountAr(preset.toDouble(), amountUnit).localizeDigits(useWestern),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
        }
    }
}

@Composable
internal fun TargetDurationEditor(
    targetDurationCount: Int,
    durationUnit: WirdDurationUnit,
    onChangeTargetDurationCount: (Int) -> Unit,
    onChangeDurationUnit: (WirdDurationUnit) -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    var unitMenuExpanded by remember { mutableStateOf(false) }

    val presets = when (durationUnit) {
        WirdDurationUnit.DAY -> listOf(7, 14, 30, 60, 90, 180)
        WirdDurationUnit.WEEK -> listOf(1, 2, 4, 8, 12)
        WirdDurationUnit.MONTH -> listOf(1, 2, 3, 6, 12)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "المدّة المستهدفة",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = qc.text
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = CustomWirdPlanCalculator.formatDurationUnitAr(targetDurationCount, durationUnit).localizeDigits(useWestern),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = qc.primary,
                modifier = Modifier.weight(1f).testTag("wizard_duration_value_text")
            )

            Box {
                val shape = RoundedCornerShape(12.dp)
                Surface(
                    shape = shape,
                    color = qc.surface2,
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .border(1.dp, qc.border, shape)
                        .clip(shape)
                        .clickable { unitMenuExpanded = true }
                        .testTag("wizard_duration_unit_dropdown")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = durationUnit.labelAr,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = qc.text
                        )
                        Icon(Icons.Rounded.ArrowDropDown, contentDescription = "اختر وحدة المدة", tint = qc.primary)
                    }
                }

                DropdownMenu(expanded = unitMenuExpanded, onDismissRequest = { unitMenuExpanded = false }) {
                    WirdDurationUnit.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.labelAr) },
                            onClick = {
                                onChangeDurationUnit(unit)
                                unitMenuExpanded = false
                            },
                            modifier = Modifier.testTag("wizard_duration_unit_${unit.key}")
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalIconButton(
                    onClick = { onChangeTargetDurationCount((targetDurationCount - 1).coerceAtLeast(1)) },
                    enabled = targetDurationCount > 1,
                    modifier = Modifier.size(48.dp).testTag("wizard_duration_minus")
                ) { Icon(Icons.Rounded.Remove, contentDescription = "إنقاص المدة") }
                FilledTonalIconButton(
                    onClick = { onChangeTargetDurationCount((targetDurationCount + 1).coerceAtMost(365)) },
                    modifier = Modifier.size(48.dp).testTag("wizard_duration_plus")
                ) { Icon(Icons.Rounded.Add, contentDescription = "زيادة المدة") }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presets.forEach { count ->
                val sel = targetDurationCount == count
                FilterChip(
                    selected = sel,
                    onClick = { onChangeTargetDurationCount(count) },
                    colors = quranFilterChipColors(),
                    border = quranFilterChipBorder(sel),
                    label = {
                        Text(
                            CustomWirdPlanCalculator.formatDurationUnitAr(count, durationUnit).localizeDigits(useWestern),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
        }
    }
}
