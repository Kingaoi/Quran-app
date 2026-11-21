package com.example.ui.screens.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ChromeReaderMode
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.adhkar.AdhkarReminderConfig
import com.example.data.local.ReaderSettings
import com.example.domain.model.JuzInfo
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.domain.wird.KhatmahPlanConfig
import com.example.domain.wird.WirdPlanCalculator
import com.example.ui.theme.quranColors

@Composable
internal fun MoreAdhkarAndWirdSection(
    settings: ReaderSettings,
    reminderConfig: AdhkarReminderConfig,
    wirdConfig: KhatmahPlanConfig,
    ajza: List<JuzInfo>,
    actions: MoreSettingsActions,
    onToggleMorningReminder: (Boolean, Int, Int) -> Unit,
    onToggleEveningReminder: (Boolean, Int, Int) -> Unit,
    onToggleWirdReminder: (enabled: Boolean, hour: Int, minute: Int) -> Unit,
    onOpenNewKhatmahWizard: () -> Unit
) {
    val useWestern = LocalUseWesternDigits.current
    val qc = MaterialTheme.quranColors

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MoreCategoryHeader(title = "الأذكار وتذكيرها", icon = Icons.Rounded.VolunteerActivism)
        MoreSubGroupHeader(title = "تجربة قراءة الأذكار والمواعيد")
        MoreSettingsGroupCard {
            MoreSettingSwitchRow(
                title = "الانتقال التلقائي في الأذكار",
                subtitle = "الانتقال للذكر التالي فور إتمام عدد التكرار المطلوب",
                icon = Icons.Rounded.FastForward,
                checked = settings.autoAdvanceDhikr,
                onCheckedChange = actions.onSetAutoAdvanceDhikr,
                testTagKey = "more_auto_advance_dhikr_switch"
            )
            MoreGroupDivider()
            MoreSettingSwitchRow(
                title = "الاهتزاز اللمسي في الأذكار",
                subtitle = "نبضة خفيفة عند الضغط على عداد الذكر أو إتمامه",
                icon = Icons.Rounded.Vibration,
                checked = settings.hapticFeedbackEnabled,
                onCheckedChange = actions.onSetHapticFeedbackEnabled,
                testTagKey = "more_haptic_feedback_switch"
            )
            MoreGroupDivider()
            AdhkarReminderStepperRow(
                title = "تذكير أذكار الصباح",
                enabled = reminderConfig.morningEnabled,
                hour = reminderConfig.morningHour,
                minute = reminderConfig.morningMinute,
                useWestern = useWestern,
                testTagPrefix = "more_morning_adhkar",
                onChange = onToggleMorningReminder
            )
            MoreGroupDivider()
            AdhkarReminderStepperRow(
                title = "تذكير أذكار المساء",
                enabled = reminderConfig.eveningEnabled,
                hour = reminderConfig.eveningHour,
                minute = reminderConfig.eveningMinute,
                useWestern = useWestern,
                testTagPrefix = "more_evening_adhkar",
                onChange = onToggleEveningReminder
            )
        }

        MoreCategoryHeader(title = "الورد اليومي والختمة", icon = Icons.Rounded.BookmarkAdded)
        MoreSubGroupHeader(title = "وضع القراءة وخطة الختمة")
        MoreSettingsGroupCard {
            MoreSettingSwitchRow(
                title = "وضع القراءة الصافية في الورد",
                subtitle = "إخفاء أزرار الاستماع والحفظ والتنقل غير الضرورية عند الدخول من الورد",
                icon = Icons.AutoMirrored.Rounded.ChromeReaderMode,
                iconTint = qc.categoryPastelAt(1),
                checked = settings.wirdPureReadingMode,
                onCheckedChange = actions.onSetWirdPureReadingMode,
                testTagKey = "wird_pure_reading_switch"
            )
            MoreGroupDivider()
            MoreSettingSwitchRow(
                title = "إخفاء شريط الورد عند التصفح الحر",
                subtitle = "إخفاء شريط تقدم الورد داخل المصحف عند الدخول من الفهرس أو المتابعة الحرة",
                icon = Icons.Rounded.VisibilityOff,
                iconTint = qc.categoryPastelAt(0),
                checked = settings.hideWirdBannerOutsideWird,
                onCheckedChange = actions.onSetHideWirdBannerOutsideWird,
                testTagKey = "hide_wird_banner_outside_wird_switch"
            )
            MoreGroupDivider()
            MoreSettingSwitchRow(
                title = "التذكير اليومي بالورد",
                subtitle = "إشعار يومي لتذكيرك بقراءة وردك القرآني",
                icon = Icons.Rounded.NotificationsActive,
                iconTint = qc.categoryPastelAt(3),
                checked = wirdConfig.reminderEnabled,
                onCheckedChange = { checked -> onToggleWirdReminder(checked, wirdConfig.reminderHour, wirdConfig.reminderMinute) },
                testTagKey = "wird_reminder_switch"
            )
            MoreGroupDivider()
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MoreSettingIconBadge(icon = Icons.Rounded.Alarm)
                Column(modifier = Modifier.weight(1f)) {
                    Text("وقت التذكير اليومي", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text(
                        text = formatArabicReminderTime(wirdConfig.reminderHour, wirdConfig.reminderMinute, useWestern),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalIconButton(
                        onClick = {
                            val total = (wirdConfig.reminderHour * 60 + wirdConfig.reminderMinute - 30 + 1440) % 1440
                            onToggleWirdReminder(wirdConfig.reminderEnabled, total / 60, total % 60)
                        },
                        modifier = Modifier.size(48.dp).testTag("wird_reminder_time_minus")
                    ) { Icon(Icons.Rounded.Remove, contentDescription = "تقديم نصف ساعة") }
                    FilledTonalIconButton(
                        onClick = {
                            val total = (wirdConfig.reminderHour * 60 + wirdConfig.reminderMinute + 30) % 1440
                            onToggleWirdReminder(wirdConfig.reminderEnabled, total / 60, total % 60)
                        },
                        modifier = Modifier.size(48.dp).testTag("wird_reminder_time_plus")
                    ) { Icon(Icons.Rounded.Add, contentDescription = "تأخير نصف ساعة") }
                }
            }
            MoreGroupDivider()
            val planSummary = if (wirdConfig.isActive) {
                val startLabel = WirdPlanCalculator.formatStartPointAr(wirdConfig.startJuz, ajza)
                val portionLabel = WirdPlanCalculator.formatDailyPortionSummaryAr(wirdConfig.quartersPerDay)
                "تبدأ من $startLabel • $portionLabel يومياً"
            } else {
                "لا توجد ختمة نشطة حالياً — اضغط للبدء"
            }
            MoreSettingActionRow(
                title = if (wirdConfig.isActive) "تعديل خطة الختمة أو بدء ختمة جديدة" else "بدء ختمة جديدة",
                subtitle = planSummary,
                icon = Icons.Rounded.AutoStories,
                onClick = onOpenNewKhatmahWizard,
                testTagKey = "wird_settings_new_khatmah"
            )
        }
    }
}

@Composable
private fun AdhkarReminderStepperRow(
    title: String,
    enabled: Boolean,
    hour: Int,
    minute: Int,
    useWestern: Boolean,
    testTagPrefix: String,
    onChange: (Boolean, Int, Int) -> Unit
) {
    MoreSettingSwitchRow(
        title = title,
        subtitle = "موعد التذكير: ${formatArabicReminderTime(hour, minute, useWestern)}",
        icon = Icons.Rounded.NotificationsActive,
        iconTint = MaterialTheme.quranColors.categoryPastelAt(5),
        checked = enabled,
        onCheckedChange = { onChange(it, hour, minute) },
        testTagKey = "${testTagPrefix}_switch"
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 68.dp, end = 16.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("تعديل الوقت:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FilledTonalIconButton(
            onClick = {
                val total = (hour * 60 + minute - 30 + 1440) % 1440
                onChange(enabled, total / 60, total % 60)
            },
            modifier = Modifier.size(48.dp)
        ) { Icon(Icons.Rounded.Remove, contentDescription = "تقديم نصف ساعة") }
        FilledTonalIconButton(
            onClick = {
                val total = (hour * 60 + minute + 30) % 1440
                onChange(enabled, total / 60, total % 60)
            },
            modifier = Modifier.size(48.dp)
        ) { Icon(Icons.Rounded.Add, contentDescription = "تأخير نصف ساعة") }
    }
}
