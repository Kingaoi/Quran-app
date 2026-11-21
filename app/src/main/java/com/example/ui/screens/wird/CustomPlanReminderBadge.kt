package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
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
import androidx.compose.ui.unit.dp
import com.example.domain.model.localizeDigits
import com.example.domain.model.toAppDigits
import com.example.domain.wird.NamedWirdPlanConfig
import com.example.domain.wird.WirdPlanCalculator
import com.example.domain.wird.WirdScheduleStatus
import com.example.domain.wird.WirdSessionTimingMode
import com.example.ui.theme.quranColors

@Composable
internal fun CustomWirdPlansEmptyCard(onAddPlan: () -> Unit) {
    val qc = MaterialTheme.quranColors
    Surface(
        onClick = onAddPlan,
        shape = RoundedCornerShape(20.dp),
        color = qc.surface,
        border = BorderStroke(1.dp, qc.border),
        modifier = Modifier.fillMaxWidth().testTag("custom_wird_empty_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp).clip(CircleShape).background(qc.surface2)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = qc.pastelRose,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "أضف ورداً إضافياً خاصاً بك",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = qc.text
                )
                Text(
                    text = "مثل: مراجعة الحفظ أو ختمة تدبّر أو ورد العائلة، ولكل خطة تقدمها وجدولها المستقل.",
                    style = MaterialTheme.typography.bodySmall,
                    color = qc.textMuted
                )
            }
        }
    }
}

@Composable
internal fun CustomPlanStatusBadge(isCompleted: Boolean, status: WirdScheduleStatus) {
    val qc = MaterialTheme.quranColors
    val (bg, fg, label) = when {
        isCompleted -> Triple(qc.successContainer, qc.success, "مكتملة")
        status is WirdScheduleStatus.Behind -> Triple(qc.dangerContainer, qc.danger, WirdPlanCalculator.formatScheduleStatusAr(status))
        else -> Triple(qc.successContainer, qc.success, WirdPlanCalculator.formatScheduleStatusAr(status))
    }
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = fg,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

@Composable
internal fun CustomPlanReminderStatusChip(
    config: NamedWirdPlanConfig,
    isCompleted: Boolean,
    useWestern: Boolean,
    onToggleReminder: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val active = config.reminderEnabled && !isCompleted
    val label = when {
        isCompleted -> "اكتملت الخطة"
        !config.reminderEnabled -> "تذكير الجلسات متوقف • اضغط للتفعيل"
        config.sessionTimingMode == WirdSessionTimingMode.UNSPECIFIED ->
            "تذكير يومي مجدول عند ٧:٠٠ ص".localizeDigits(useWestern)
        else -> {
            val count = config.sessionsPerDay.coerceIn(1, 5)
            val lead = if (config.reminderLeadMinutes <= 0) "في وقتها" else "قبل ${config.reminderLeadMinutes.toAppDigits(useWestern)} د"
            "تذكير ${count.toAppDigits(useWestern)} جلسات مجدول ($lead)"
        }
    }
    Surface(
        onClick = { if (!isCompleted) onToggleReminder() },
        shape = RoundedCornerShape(12.dp),
        color = if (active) qc.selected.copy(alpha = 0.14f) else qc.bg,
        border = BorderStroke(1.dp, if (active) qc.primary.copy(alpha = 0.35f) else qc.border),
        modifier = Modifier.fillMaxWidth().testTag("custom_plan_reminder_chip_${config.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (active) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
                contentDescription = null,
                tint = if (active) qc.primary else qc.textMuted,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (active) qc.primary else qc.textMuted,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
