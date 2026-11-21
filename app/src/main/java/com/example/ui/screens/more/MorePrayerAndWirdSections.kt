package com.example.ui.screens.more

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AdhanSoundMode
import com.example.data.local.PrayerSettings
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.domain.prayer.AsrJuristicMethod
import com.example.domain.prayer.PrayerName
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

private val PRE_ADHAN_OPTIONS = listOf(0, 5, 10, 15)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MorePrayerSection(
    prayerSettings: PrayerSettings,
    cachedAdhanVoiceIds: Set<String>,
    actions: MoreSettingsActions,
    onOpenCityPicker: () -> Unit,
    onOpenCalcMethodPicker: () -> Unit,
    onOpenAdhanVoicePicker: () -> Unit,
    onRequestPrayerAlertToggle: (PrayerName) -> Unit,
    onRequestHijriSunnahToggle: (Boolean) -> Unit = {}
) {
    val useWestern = LocalUseWesternDigits.current
    var expandAlerts by rememberSaveable { mutableStateOf(false) }
    val mandatoryPrayers = PrayerName.entries.filter { it.isMandatoryPrayer }
    val enabledAlertCount = mandatoryPrayers.count { prayerSettings.isAlertEnabled(it) }
    val qc = MaterialTheme.quranColors

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MoreCategoryHeader(title = "أوقات الصلاة والتنبيهات", icon = Icons.Rounded.Mosque)

        MoreSubGroupHeader(title = "الموقع والحساب الفلكي والتقويم")
        MoreSettingsGroupCard {
            MoreSettingActionRow(
                title = "المدينة والموقع الجغرافي",
                subtitle = "${prayerSettings.city.nameAr}، ${prayerSettings.city.countryAr}",
                icon = if (prayerSettings.city.isFromGps) Icons.Rounded.MyLocation else Icons.Rounded.LocationOn,
                iconTint = qc.categoryPastelAt(1),
                onClick = onOpenCityPicker,
                testTagKey = "more_prayer_city_row",
                trailingAccessory = if (prayerSettings.city.isFromGps) {
                    {
                        Surface(shape = RoundedCornerShape(50), color = qc.successContainer) {
                            Text(
                                text = "GPS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = qc.success,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                } else null
            )

            MoreGroupDivider()

            MoreSettingActionRow(
                title = "طريقة الحساب الفلكي",
                subtitle = prayerSettings.calculationMethod.titleAr,
                icon = Icons.Rounded.Calculate,
                iconTint = qc.categoryPastelAt(2),
                onClick = onOpenCalcMethodPicker,
                testTagKey = "more_prayer_method_row"
            )

            MoreGroupDivider()

            HijriOffsetStepperRow(
                offsetDays = prayerSettings.hijriOffsetDays,
                onSetOffsetDays = actions.onSetHijriOffsetDays
            )

            MoreGroupDivider()

            ChipChoiceRow(
                title = "مذهب صلاة العصر",
                subtitle = prayerSettings.asrMethod.titleAr,
                icon = Icons.Rounded.WbSunny
            ) {
                AsrJuristicMethod.entries.forEach { method ->
                    val sel = prayerSettings.asrMethod == method
                    FilterChip(
                        selected = sel,
                        onClick = { actions.onSelectAsrMethod(method) },
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(sel),
                        label = { Text(method.titleAr) },
                        modifier = Modifier.testTag("more_asr_method_${method.name}")
                    )
                }
            }

            MoreGroupDivider()

            MoreSettingSwitchRow(
                title = "نظام ${24.toAppDigits(useWestern)} ساعة",
                subtitle = "عرض أوقات الصلاة بصيغة ${24.toAppDigits(useWestern)} ساعة بدل ص/م",
                icon = Icons.Rounded.Schedule,
                iconTint = qc.categoryPastelAt(5),
                checked = prayerSettings.use24Hour,
                onCheckedChange = actions.onSetUse24Hour,
                testTagKey = "prayer_24h_switch"
            )
        }

        MoreSubGroupHeader(title = "صوت الأذان والمؤذن")
        MoreSettingsGroupCard {
            ChipChoiceRow(
                title = "التنبيه المسبق قبل الأذان",
                subtitle = if (prayerSettings.preAdhanMinutes == 0) "عند دخول الوقت فقط" else "قبل الأذان بـ ${prayerSettings.preAdhanMinutes.toAppDigits(useWestern)} دقائق",
                icon = Icons.Rounded.Timer
            ) {
                PRE_ADHAN_OPTIONS.forEach { mins ->
                    val sel = prayerSettings.preAdhanMinutes == mins
                    FilterChip(
                        selected = sel,
                        onClick = { actions.onSetPreAdhanMinutes(mins) },
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(sel),
                        label = { Text(if (mins == 0) "بدون" else "${mins.toAppDigits(useWestern)} د") },
                        modifier = Modifier.testTag("more_pre_adhan_$mins")
                    )
                }
            }

            MoreGroupDivider()

            ChipChoiceRow(
                title = "نمط صوت الأذان",
                subtitle = prayerSettings.adhanSoundMode.labelAr,
                icon = Icons.AutoMirrored.Rounded.VolumeUp
            ) {
                AdhanSoundMode.entries.forEach { mode ->
                    val sel = prayerSettings.adhanSoundMode == mode
                    FilterChip(
                        selected = sel,
                        onClick = { actions.onSetAdhanSoundMode(mode) },
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(sel),
                        label = { Text(mode.labelAr) },
                        modifier = Modifier.testTag("more_adhan_sound_${mode.name}")
                    )
                }
            }

            MorePrayerAlarmExtrasGroup(
                prayerSettings = prayerSettings,
                cachedAdhanVoiceIds = cachedAdhanVoiceIds,
                onOpenAdhanVoicePicker = onOpenAdhanVoicePicker,
                onTestPrayerAlert = actions.onTestPrayerAlert
            )
        }

        MoreSubGroupHeader(title = "تنبيهات الصلوات والسنن")
        MoreSettingsGroupCard {
            MoreExpandableSectionRow(
                title = "تنبيهات الصلوات الخمس",
                subtitle = if (enabledAlertCount == 0) {
                    "جميع التنبيهات متوقفة"
                } else {
                    "${enabledAlertCount.toAppDigits(useWestern)} من ${5.toAppDigits(useWestern)} صلوات مفعّلة"
                },
                icon = if (enabledAlertCount > 0) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsNone,
                expanded = expandAlerts,
                onToggleExpand = { expandAlerts = !expandAlerts },
                testTagKey = "more_expand_prayer_alerts"
            ) {
                mandatoryPrayers.forEach { prayer ->
                    val enabled = prayerSettings.isAlertEnabled(prayer)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth().clickable { onRequestPrayerAlertToggle(prayer) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "تنبيه صلاة ${prayer.titleAr}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Switch(
                                checked = enabled,
                                onCheckedChange = { onRequestPrayerAlertToggle(prayer) },
                                modifier = Modifier.testTag("more_prayer_alert_${prayer.id}")
                            )
                        }
                    }
                }
            }

            MoreGroupDivider()

            MoreHijriSunnahSectionRow(
                config = prayerSettings.hijriSunnahConfig,
                hijriOffsetDays = prayerSettings.hijriOffsetDays,
                onRequestToggleMaster = onRequestHijriSunnahToggle,
                onUpdateConfig = actions.onUpdateHijriSunnah
            )
        }
    }
}
