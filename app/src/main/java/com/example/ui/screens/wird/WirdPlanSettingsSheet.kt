package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.localizeDigits
import com.example.domain.wird.CustomWirdFormatters
import com.example.domain.wird.KhatmahPlanConfig
import com.example.domain.wird.NamedWirdPlanConfig
import com.example.domain.wird.WirdTimelineState
import com.example.ui.theme.quranColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WirdPlanSettingsSheet(
    timeline: WirdTimelineState,
    mainConfig: KhatmahPlanConfig?,
    customConfig: NamedWirdPlanConfig?,
    onToggleMainReminder: (Boolean, Int, Int) -> Unit,
    onToggleCustomReminder: (String) -> Unit,
    onRescheduleOverdue: () -> Unit,
    onResetScheduleOverrides: () -> Unit,
    onOpenEditPlan: () -> Unit,
    onRequestDeleteCustomPlan: () -> Unit,
    onDismiss: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = qc.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 22.dp)
                .testTag("wird_plan_settings_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "إعدادات الخطة",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = qc.text
                    )
                    Text(
                        text = timeline.planTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = qc.textMuted
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = "إغلاق", tint = qc.text)
                }
            }

            if (customConfig != null) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = qc.bg,
                    border = BorderStroke(1.dp, qc.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (customConfig.reminderEnabled) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
                                contentDescription = null,
                                tint = if (customConfig.reminderEnabled) qc.primary else qc.textMuted
                            )
                            Column {
                                Text(
                                    text = "تذكير جلسات الخطة",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = qc.text
                                )
                                Text(
                                    text = if (customConfig.reminderEnabled) "مفعّل حسب جدول الجلسات" else "التذكير متوقف",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = qc.textMuted
                                )
                            }
                        }
                        Switch(
                            checked = customConfig.reminderEnabled,
                            onCheckedChange = { onToggleCustomReminder(customConfig.id) },
                            modifier = Modifier.testTag("settings_custom_reminder_switch")
                        )
                    }
                }
            } else if (mainConfig != null) {
                MainKhatmahReminderCard(
                    config = mainConfig,
                    useWestern = useWestern,
                    onUpdateReminder = onToggleMainReminder
                )
            }

            if (timeline.overdueCount > 0) {
                FilledTonalButton(
                    onClick = { onRescheduleOverdue(); onDismiss() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = qc.surface2, contentColor = qc.text),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Rounded.EventRepeat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("إعادة جدولة المتأخر بدءاً من اليوم", fontWeight = FontWeight.Bold)
                }
            }

            if (timeline.hasOverrides) {
                OutlinedButton(
                    onClick = { onResetScheduleOverrides(); onDismiss() },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, qc.border),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("settings_reset_schedule_button")
                ) {
                    Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("استعادة التواريخ الأصلية للجدول", fontWeight = FontWeight.SemiBold)
                }
            }

            OutlinedButton(
                onClick = { onDismiss(); onOpenEditPlan() },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, qc.border),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text("تعديل الخطة ونطاقها", fontWeight = FontWeight.SemiBold)
            }

            if (customConfig != null) {
                OutlinedButton(
                    onClick = { onDismiss(); onRequestDeleteCustomPlan() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = qc.danger),
                    border = BorderStroke(1.dp, qc.danger.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("حذف الخطة", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MainKhatmahReminderCard(
    config: KhatmahPlanConfig,
    useWestern: Boolean,
    onUpdateReminder: (Boolean, Int, Int) -> Unit
) {
    val qc = MaterialTheme.quranColors
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = qc.bg,
        border = BorderStroke(1.dp, qc.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "التذكير اليومي للختمة",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = qc.text
                    )
                    Text(
                        text = CustomWirdFormatters.formatTime12hAr(config.reminderHour, config.reminderMinute).localizeDigits(useWestern),
                        style = MaterialTheme.typography.bodySmall,
                        color = qc.primary
                    )
                }
                Switch(
                    checked = config.reminderEnabled,
                    onCheckedChange = { checked -> onUpdateReminder(checked, config.reminderHour, config.reminderMinute) },
                    modifier = Modifier.testTag("settings_main_reminder_switch")
                )
            }
            if (config.reminderEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تعديل الوقت", style = MaterialTheme.typography.labelLarge, color = qc.textMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalIconButton(
                            onClick = {
                                val total = ((config.reminderHour * 60 + config.reminderMinute - 30) + 1440) % 1440
                                onUpdateReminder(true, total / 60, total % 60)
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = qc.surface2, contentColor = qc.primary),
                            modifier = Modifier.size(48.dp)
                        ) { Icon(Icons.Rounded.Remove, contentDescription = "تقديم ٣٠ دقيقة") }
                        FilledTonalIconButton(
                            onClick = {
                                val total = (config.reminderHour * 60 + config.reminderMinute + 30) % 1440
                                onUpdateReminder(true, total / 60, total % 60)
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = qc.surface2, contentColor = qc.primary),
                            modifier = Modifier.size(48.dp)
                        ) { Icon(Icons.Rounded.Add, contentDescription = "تأخير ٣٠ دقيقة") }
                    }
                }
            }
        }
    }
}
