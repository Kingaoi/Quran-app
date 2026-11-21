package com.example.ui.screens.prayer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.prayer.HijriSunnahCalculator
import com.example.domain.prayer.HijriSunnahConfig

@Composable
internal fun HijriSunnahInsightBanner(
    hijriOffsetDays: Int,
    useWestern: Boolean,
    sunnahConfig: HijriSunnahConfig?,
    onToggleSunnahMaster: ((Boolean) -> Unit)?
) {
    val todayInsight = remember(hijriOffsetDays, useWestern) {
        HijriSunnahCalculator.inspectToday(
            hijriOffsetDays = hijriOffsetDays,
            useWesternDigits = useWestern
        )
    }
    val nextNotice = remember(sunnahConfig, hijriOffsetDays) {
        if (sunnahConfig?.enabled != true) {
            null
        } else {
            HijriSunnahCalculator.computeNextNotices(
                config = sunnahConfig,
                hijriOffsetDays = hijriOffsetDays
            ).minByOrNull { it.triggerAtMillis }
        }
    }

    if (todayInsight != null) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
            modifier = Modifier.fillMaxWidth().testTag("hijri_today_sunnah_insight")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.WbSunny,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = todayInsight.titleAr,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = todayInsight.subtitleAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if (sunnahConfig != null && onToggleSunnahMaster != null) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(19.dp)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "تنبيهات السنن التلقائية",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = nextNotice?.let { "القادم: ${it.titleAr}" }
                            ?: "تذكير تلقائي بسورة الكهف وصيام الاثنين والخميس والأيام البيض",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = sunnahConfig.enabled,
                    onCheckedChange = onToggleSunnahMaster,
                    modifier = Modifier.testTag("hijri_sheet_sunnah_switch")
                )
            }
        }
    }
}
