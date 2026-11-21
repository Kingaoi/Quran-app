package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.domain.wird.NamedWirdPlanItem
import com.example.domain.wird.WirdPlanAudience
import com.example.domain.wird.WirdPlanCalculator
import com.example.ui.theme.quranColors

@Composable
internal fun CustomWirdPlansSectionHeader(
    plansCount: Int,
    onAddPlan: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (plansCount > 0) "خطط إضافية مستقلة (${plansCount.toAppDigits(useWestern)})" else "خطط إضافية مستقلة",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = qc.text
            )
            Text(
                text = "أوراد بأسماء مخصصة منفصلة تماماً عن وردك الأساسي",
                style = MaterialTheme.typography.bodySmall,
                color = qc.textMuted
            )
        }
        Button(
            onClick = onAddPlan,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = qc.primary, contentColor = qc.onPrimary),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            modifier = Modifier.testTag("wird_add_plan_button")
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.size(5.dp))
            Text("إضافة خطة", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
internal fun CustomWirdPlanCard(
    item: NamedWirdPlanItem,
    onOpenReader: () -> Unit,
    onCompleteWird: () -> Unit,
    onToggleReminder: () -> Unit = {},
    onOpenSchedule: () -> Unit,
    onRequestDelete: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    val config = item.config
    val summary = item.summary
    val current = summary.currentWird
    val iconType = CustomPlanIconType.fromKey(config.iconKey)
    val percent = (summary.progressFraction * 100f).toInt().coerceIn(0, 100)

    Surface(
        onClick = onOpenSchedule,
        shape = RoundedCornerShape(22.dp),
        color = qc.surface,
        border = BorderStroke(1.dp, qc.border),
        modifier = Modifier.fillMaxWidth().testTag("custom_plan_card_${config.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(qc.surface2)
                ) {
                    Icon(
                        imageVector = iconType.icon,
                        contentDescription = null,
                        tint = qc.categoryPastelAt(iconType.pastelIndex),
                        modifier = Modifier.size(21.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = config.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = qc.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val subtitleInfo = if (config.isV2Plan) {
                        val sessionsTxt = if (config.sessionsPerDay > 1) " • ${config.sessionsPerDay.toAppDigits(useWestern)} جلسات" else ""
                        val rabtTxt = if (config.rabtAmount > 0.01f) " • ربط" else ""
                        val periodicTxt = if (config.isPeriodicWird) " • وِرد دوري" else ""
                        val audienceTxt = if (config.audience == WirdPlanAudience.FOR_OTHER) " • لغيري" else ""
                        "${config.planType.labelAr}$periodicTxt$audienceTxt$sessionsTxt$rabtTxt • ${summary.completedCount.toAppDigits(useWestern)} / ${summary.totalWirds.toAppDigits(useWestern)} ورد"
                    } else {
                        "${WirdPlanCalculator.formatDailyPortionSummaryAr(config.quartersPerDay)} يومياً • ${summary.completedCount.toAppDigits(useWestern)} / ${summary.totalWirds.toAppDigits(useWestern)} ورد"
                    }
                    Text(text = subtitleInfo, style = MaterialTheme.typography.bodySmall, color = qc.textMuted)
                }

                CustomPlanStatusBadge(isCompleted = summary.isCompleted, status = summary.scheduleStatus)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LinearProgressIndicator(
                    progress = { summary.progressFraction },
                    color = if (summary.isCompleted) qc.success else qc.primary,
                    trackColor = qc.surface2,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(50))
                )
                Text(
                    text = "${percent.toAppDigits(useWestern)}٪",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (summary.isCompleted) qc.success else qc.primary
                )
            }

            if (current != null && !summary.isCompleted) {
                Surface(shape = RoundedCornerShape(14.dp), color = qc.bg, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "الورد الحالي (${(current.index + 1).toAppDigits(useWestern)}): ${current.juzNameAr}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = qc.primary
                        )
                        Text(
                            text = "من ${current.startSurahNameAr} (${current.startAyahNumber.toAppDigits(useWestern)}) إلى ${current.endSurahNameAr} (${current.endAyahNumber.toAppDigits(useWestern)}) • ص ${current.startPage.toAppDigits(useWestern)}-${current.endPage.toAppDigits(useWestern)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = qc.text
                        )
                    }
                }
            }

            CustomPlanReminderStatusChip(
                config = config,
                isCompleted = summary.isCompleted,
                useWestern = useWestern,
                onToggleReminder = onToggleReminder
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!summary.isCompleted) {
                    Button(
                        onClick = onOpenReader,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = qc.primary, contentColor = qc.onPrimary),
                        modifier = Modifier.weight(1.2f).height(48.dp).testTag("custom_plan_read_${config.id}")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = if (summary.hasStartedCurrentWird) "تابع" else "اقرأ الورد",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    FilledTonalButton(
                        onClick = onCompleteWird,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = qc.selected, contentColor = qc.onSelected),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("custom_plan_complete_${config.id}")
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.size(5.dp))
                        Text("أتممت", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                } else {
                    Surface(shape = RoundedCornerShape(14.dp), color = qc.successContainer, modifier = Modifier.weight(1f).height(48.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "تم إتمام الخطة بحمد الله",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = qc.success
                            )
                        }
                    }
                }

                if (!summary.isCompleted) {
                    IconButton(
                        onClick = onToggleReminder,
                        modifier = Modifier.size(48.dp).testTag("custom_plan_reminder_btn_${config.id}")
                    ) {
                        Icon(
                            imageVector = if (config.reminderEnabled) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
                            contentDescription = if (config.reminderEnabled) "إيقاف تذكير ${config.title}" else "تفعيل تذكير ${config.title}",
                            tint = if (config.reminderEnabled) qc.primary else qc.textMuted
                        )
                    }
                }

                IconButton(
                    onClick = onOpenSchedule,
                    modifier = Modifier.size(48.dp).testTag("custom_plan_schedule_${config.id}")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.FormatListBulleted, contentDescription = "جدول خطة ${config.title}", tint = qc.primary)
                }

                IconButton(
                    onClick = onRequestDelete,
                    modifier = Modifier.size(48.dp).testTag("custom_plan_delete_${config.id}")
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف خطة ${config.title}", tint = qc.danger)
                }
            }
        }
    }
}
