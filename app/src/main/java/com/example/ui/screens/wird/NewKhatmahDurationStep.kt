package com.example.ui.screens.wird

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalIconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.model.toArabicIndicDigits
import com.example.domain.wird.WirdPlanCalculator

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DurationAndPortionStepContent(
    totalRemainingQuarters: Int,
    quartersPerDay: Int,
    onChangeQuartersPerDay: (Int) -> Unit,
    confirmLabel: String = "الاستمرار وبدء الختمة",
    confirmTestTag: String = "khatmah_confirm_start_button",
    planSummarySubtitle: String? = null,
    onConfirm: () -> Unit
) {
    val durationDays = WirdPlanCalculator.daysFromQuarters(totalRemainingQuarters, quartersPerDay)
    val juzPart = (quartersPerDay / 8).coerceIn(0, WirdPlanCalculator.MAX_JUZ_PER_DAY)
    val rubPart = (quartersPerDay % 8).coerceIn(0, 7)

    var showJuzMenu by remember { mutableStateOf(false) }
    var showRubMenu by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "الرجاء تحديد المدة التي تريد أن تختم فيها أو كمية الورد اليومي الذي تود قراءته",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(7, 15, 30, 60).filter { it <= totalRemainingQuarters }.forEach { presetDays ->
                FilterChip(
                    selected = durationDays == presetDays,
                    onClick = {
                        onChangeQuartersPerDay(
                            WirdPlanCalculator.quartersFromDays(totalRemainingQuarters, presetDays)
                        )
                    },
                    label = { Text(WirdPlanCalculator.formatDurationDaysAr(presetDays)) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "مدة الختمة:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.34f)
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .weight(0.36f)
                    .height(48.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = WirdPlanCalculator.formatDurationDaysAr(durationDays),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalIconButton(
                    onClick = {
                        val nextDays = (durationDays - 1).coerceAtLeast(1)
                        onChangeQuartersPerDay(WirdPlanCalculator.quartersFromDays(totalRemainingQuarters, nextDays))
                    },
                    enabled = durationDays > 1,
                    modifier = Modifier.size(48.dp).testTag("khatmah_days_minus")
                ) {
                    Icon(Icons.Rounded.Remove, contentDescription = "إنقاص المدة")
                }

                FilledTonalIconButton(
                    onClick = {
                        val nextDays = (durationDays + 1).coerceAtMost(totalRemainingQuarters)
                        onChangeQuartersPerDay(WirdPlanCalculator.quartersFromDays(totalRemainingQuarters, nextDays))
                    },
                    enabled = durationDays < totalRemainingQuarters,
                    modifier = Modifier.size(48.dp).testTag("khatmah_days_plus")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "زيادة المدة")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "كمية الورد:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.34f)
            )

            Box(modifier = Modifier.weight(0.33f)) {
                PortionPickerPill(
                    text = WirdPlanCalculator.formatJuzCountAr(juzPart),
                    tag = "khatmah_juz_picker",
                    onClick = { showJuzMenu = true }
                )
                DropdownMenu(
                    expanded = showJuzMenu,
                    onDismissRequest = { showJuzMenu = false },
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    (0..WirdPlanCalculator.MAX_JUZ_PER_DAY).forEach { j ->
                        val candidateQuarters = (j * 8 + rubPart).coerceAtLeast(1)
                        if (candidateQuarters <= totalRemainingQuarters) {
                            DropdownMenuItem(
                                text = { Text(WirdPlanCalculator.formatJuzCountAr(j)) },
                                onClick = {
                                    onChangeQuartersPerDay(candidateQuarters)
                                    showJuzMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(0.33f)) {
                PortionPickerPill(
                    text = WirdPlanCalculator.formatRubCountAr(rubPart),
                    tag = "khatmah_rub_picker",
                    onClick = { showRubMenu = true }
                )
                DropdownMenu(
                    expanded = showRubMenu,
                    onDismissRequest = { showRubMenu = false },
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    (0..7).forEach { r ->
                        if (juzPart == 0 && r == 0) return@forEach
                        val candidateQuarters = (juzPart * 8 + r).coerceAtLeast(1)
                        if (candidateQuarters <= totalRemainingQuarters) {
                            DropdownMenuItem(
                                text = { Text(WirdPlanCalculator.formatRubCountAr(r)) },
                                onClick = {
                                    onChangeQuartersPerDay(candidateQuarters)
                                    showRubMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!planSummarySubtitle.isNullOrBlank()) {
                    Text(
                        text = planSummarySubtitle,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = "ملخص الخطة: ${durationDays.toArabicIndicDigits()} ورداً • بمقدار ${WirdPlanCalculator.formatDailyPortionSummaryAr(quartersPerDay)} يومياً",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Button(
            onClick = onConfirm,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag(confirmTestTag)
        ) {
            Text(confirmLabel, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun PortionPickerPill(text: String, tag: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .clip(shape)
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
