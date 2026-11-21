package com.example.ui.screens.more

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.localizeDigits
import com.example.domain.model.toAppDigits
import com.example.domain.prayer.HijriSunnahCalculator
import com.example.domain.prayer.HijriSunnahConfig
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MoreHijriSunnahSectionRow(
    config: HijriSunnahConfig,
    hijriOffsetDays: Int,
    onRequestToggleMaster: (Boolean) -> Unit,
    onUpdateConfig: ((HijriSunnahConfig) -> HijriSunnahConfig) -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    var expanded by rememberSaveable { mutableStateOf(false) }

    val nextCount = remember(config, hijriOffsetDays) {
        if (!config.enabled) 0 else listOf(
            config.fridayKahfEnabled,
            config.mondayThursdayEnabled,
            config.whiteDaysEnabled,
            config.occasionsEnabled
        ).count { it }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .testTag("more_hijri_sunnah_header"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MoreSettingIconBadge(
                icon = Icons.Rounded.EventAvailable,
                tint = qc.categoryPastelAt(4)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "تنبيهات السنن والمناسبات الهجرية",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = qc.text
                )
                Text(
                    text = if (config.enabled) {
                        "مفعّل (${nextCount.toAppDigits(useWestern)} أصناف: الكهف، الصيام، المناسبات)"
                    } else {
                        "الكهف يوم الجمعة، الاثنين والخميس، الأيام البيض، وعاشوراء وعرفة"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (config.enabled) qc.primary else qc.textMuted
                )
            }
            Switch(
                checked = config.enabled,
                onCheckedChange = onRequestToggleMaster,
                modifier = Modifier.testTag("more_hijri_sunnah_switch")
            )
            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = if (expanded) "طي التفاصيل" else "تخصيص التنبيهات",
                tint = qc.textMuted
            )
        }

        if (expanded || config.enabled) {
            HijriSunnahCategoryChipsBlock(
                config = config,
                hijriOffsetDays = hijriOffsetDays,
                useWestern = useWestern,
                onUpdateConfig = onUpdateConfig
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun HijriSunnahCategoryChipsBlock(
    config: HijriSunnahConfig,
    hijriOffsetDays: Int,
    useWestern: Boolean,
    onUpdateConfig: ((HijriSunnahConfig) -> HijriSunnahConfig) -> Unit
) {
    val qc = MaterialTheme.quranColors
    val nextNotice = remember(config, hijriOffsetDays) {
        HijriSunnahCalculator.computeNextNotices(
            config = config.copy(enabled = true),
            hijriOffsetDays = hijriOffsetDays
        ).minByOrNull { it.triggerAtMillis }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = qc.bg,
        border = BorderStroke(1.dp, qc.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = config.fridayKahfEnabled,
                    onClick = { onUpdateConfig { it.copy(fridayKahfEnabled = !it.fridayKahfEnabled) } },
                    colors = quranFilterChipColors(),
                    border = quranFilterChipBorder(config.fridayKahfEnabled),
                    label = { Text("سورة الكهف (الجمعة)", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("sunnah_chip_kahf")
                )
                FilterChip(
                    selected = config.mondayThursdayEnabled,
                    onClick = { onUpdateConfig { it.copy(mondayThursdayEnabled = !it.mondayThursdayEnabled) } },
                    colors = quranFilterChipColors(),
                    border = quranFilterChipBorder(config.mondayThursdayEnabled),
                    label = { Text("الاثنين والخميس", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("sunnah_chip_mon_thu")
                )
                FilterChip(
                    selected = config.whiteDaysEnabled,
                    onClick = { onUpdateConfig { it.copy(whiteDaysEnabled = !it.whiteDaysEnabled) } },
                    colors = quranFilterChipColors(),
                    border = quranFilterChipBorder(config.whiteDaysEnabled),
                    label = { Text("الأيام البيض", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("sunnah_chip_white_days")
                )
                FilterChip(
                    selected = config.occasionsEnabled,
                    onClick = { onUpdateConfig { it.copy(occasionsEnabled = !it.occasionsEnabled) } },
                    colors = quranFilterChipColors(),
                    border = quranFilterChipBorder(config.occasionsEnabled),
                    label = { Text("عاشوراء وعرفة ورمضان", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("sunnah_chip_occasions")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تنبيه الصيام مساءً:",
                    style = MaterialTheme.typography.labelSmall,
                    color = qc.textMuted
                )
                listOf(19 to "٧:٣٠ م", 20 to "٨:٣٠ م", 21 to "٩:٣٠ م").forEach { (h, label) ->
                    val sel = config.fastingReminderHour == h
                    FilterChip(
                        selected = sel,
                        onClick = { onUpdateConfig { it.copy(fastingReminderHour = h, fastingReminderMinute = 30) } },
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(sel),
                        label = { Text(label.localizeDigits(useWestern), style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            if (nextNotice != null) {
                Text(
                    text = "أقرب تذكير قادم: ${nextNotice.titleAr}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = qc.primary
                )
            }
        }
    }
}
