package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.localizeDigits
import com.example.domain.model.toAppDigits
import com.example.domain.wird.CustomPlanMetrics
import com.example.domain.wird.CustomWirdPlanCalculator
import com.example.domain.wird.WirdAmountUnit
import com.example.domain.wird.WirdPlanType
import com.example.domain.wird.WirdSessionSlot
import com.example.domain.wird.WirdSessionTimingMode
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

private data class EditingSessionTime(val sessionIndex: Int, val isStart: Boolean)

private val REMINDER_LEAD_OPTIONS = listOf(
    0 to "في وقتها",
    5 to "قبل بـ ٥ دقيقة",
    10 to "قبل بـ ١٠ دقيقة",
    15 to "قبل بـ ١٥ دقيقة",
    30 to "قبل بـ ٣٠ دقيقة",
    60 to "قبل بساعة"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CreatePlanStep4SessionsReminder(
    planTitle: String,
    planType: WirdPlanType,
    amountUnit: WirdAmountUnit,
    rabtAmount: Float,
    reviewAmountValue: Float,
    reviewAmountUnit: WirdAmountUnit,
    sessionsPerDay: Int,
    timingMode: WirdSessionTimingMode,
    sessions: List<WirdSessionSlot>,
    reminderLeadMinutes: Int,
    reminderEnabled: Boolean,
    metrics: CustomPlanMetrics,
    onChangeSessionsPerDay: (Int) -> Unit,
    onChangeTimingMode: (WirdSessionTimingMode) -> Unit,
    onUpdateSessionSlot: (index: Int, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit,
    onChangeReminderLeadMinutes: (Int) -> Unit,
    onChangeReminderEnabled: (Boolean) -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    var sessionsMenuExpanded by remember { mutableStateOf(false) }
    var editingTime by remember { mutableStateOf<EditingSessionTime?>(null) }

    val dailyLabel = CustomWirdPlanCalculator.formatAmountAr(metrics.dailyInUnit, amountUnit)
    val perSessionLabel = CustomWirdPlanCalculator.formatAmountAr(metrics.perSessionInUnit, amountUnit)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "عدد الجلسات في اليوم",
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
                        .clickable { sessionsMenuExpanded = true }
                        .testTag("wizard_sessions_dropdown")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = CustomWirdPlanCalculator.formatSessionsCountAr(sessionsPerDay).localizeDigits(useWestern),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = qc.text,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Rounded.ArrowDropDown, contentDescription = "اختر عدد الجلسات", tint = qc.primary)
                    }
                }

                DropdownMenu(expanded = sessionsMenuExpanded, onDismissRequest = { sessionsMenuExpanded = false }) {
                    (1..5).forEach { count ->
                        DropdownMenuItem(
                            text = { Text(CustomWirdPlanCalculator.formatSessionsCountAr(count).localizeDigits(useWestern)) },
                            onClick = {
                                onChangeSessionsPerDay(count)
                                sessionsMenuExpanded = false
                            },
                            modifier = Modifier.testTag("wizard_sessions_$count")
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "وقت الجلسة",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = qc.text
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WirdSessionTimingMode.entries.forEach { mode ->
                    val selected = timingMode == mode
                    Surface(
                        onClick = { onChangeTimingMode(mode) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) qc.selected.copy(alpha = 0.18f) else qc.surface2,
                        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) qc.primary else qc.border),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("wizard_timing_${mode.key}")
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
                            Text(
                                text = mode.labelAr,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (selected) qc.primary else qc.text,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        if (timingMode == WirdSessionTimingMode.SCHEDULED) {
            if (sessionsPerDay > 1) {
                Text(
                    text = "يُقسَّم مقدار اليوم بالتساوي على الجلسات ($perSessionLabel لكل جلسة)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = qc.primary
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                sessions.take(sessionsPerDay).forEachIndexed { idx, slot ->
                    SessionTimeSlotCard(
                        slot = slot,
                        perSessionLabel = perSessionLabel,
                        onPickStart = { editingTime = EditingSessionTime(idx, isStart = true) },
                        onPickEnd = { editingTime = EditingSessionTime(idx, isStart = false) }
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "تذكير الجلسات",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = qc.text
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    REMINDER_LEAD_OPTIONS.forEach { (mins, label) ->
                        val selected = reminderLeadMinutes == mins
                        FilterChip(
                            selected = selected,
                            onClick = { onChangeReminderLeadMinutes(mins) },
                            colors = quranFilterChipColors(),
                            border = quranFilterChipBorder(selected),
                            label = { Text(label.localizeDigits(useWestern), style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("wizard_lead_$mins")
                        )
                    }
                }
            }
        }

        val switchTitle = if (timingMode == WirdSessionTimingMode.UNSPECIFIED) {
            "تفعيل التذكير عند ٧:٠٠ ص".localizeDigits(useWestern)
        } else {
            "تفعيل تذكير الجلسات"
        }
        val switchSubtitle = if (timingMode == WirdSessionTimingMode.UNSPECIFIED) {
            "سنُذكّرك في هذا الوقت يوم الجلسة (٧:٠٠ ص)".localizeDigits(useWestern)
        } else {
            "سنُذكّرك يوم الجلسة (${CustomWirdPlanCalculator.formatReminderLeadAr(reminderLeadMinutes)})"
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = qc.surface2,
            border = BorderStroke(1.dp, qc.border),
            modifier = Modifier.fillMaxWidth().clickable { onChangeReminderEnabled(!reminderEnabled) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = qc.primary)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(switchTitle, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = qc.text)
                    Text(switchSubtitle, style = MaterialTheme.typography.bodySmall, color = qc.textMuted)
                }
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = onChangeReminderEnabled,
                    modifier = Modifier.testTag("wizard_reminder_switch")
                )
            }
        }

        PlanSummaryBottomCard(
            planTitle = planTitle,
            planType = planType,
            rabtAmount = rabtAmount,
            reviewAmountValue = reviewAmountValue,
            reviewAmountUnit = reviewAmountUnit,
            sessionsPerDay = sessionsPerDay,
            metrics = metrics,
            dailyLabel = dailyLabel,
            perSessionLabel = perSessionLabel
        )
    }

    editingTime?.let { target ->
        val slot = sessions.getOrNull(target.sessionIndex) ?: return@let
        PlanTimePickerDialog(
            title = if (target.isStart) {
                "وقت بداية الجلسة ${(target.sessionIndex + 1).toAppDigits(useWestern)}"
            } else {
                "وقت نهاية الجلسة ${(target.sessionIndex + 1).toAppDigits(useWestern)}"
            },
            initialHour = if (target.isStart) slot.startHour else slot.endHour,
            initialMinute = if (target.isStart) slot.startMinute else slot.endMinute,
            onConfirmTime = { h, m ->
                if (target.isStart) {
                    onUpdateSessionSlot(target.sessionIndex, h, m, slot.endHour, slot.endMinute)
                } else {
                    onUpdateSessionSlot(target.sessionIndex, slot.startHour, slot.startMinute, h, m)
                }
            },
            onDismiss = { editingTime = null }
        )
    }
}
