package com.example.ui.screens.prayer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.localizeDigits
import com.example.domain.prayer.HijriDayInsight
import com.example.domain.prayer.PrayerTimesCalculator
import com.example.ui.theme.quranColors
import java.util.TimeZone

@Composable
internal fun PrayerTodaySunnahRibbon(
    insight: HijriDayInsight,
    onClick: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val shape = RoundedCornerShape(18.dp)
    Surface(
        shape = shape,
        color = qc.pastelAmber.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, qc.accent.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
            .testTag("prayer_today_sunnah_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = qc.accent.copy(alpha = 0.18f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.EventAvailable,
                        contentDescription = null,
                        tint = qc.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.titleAr,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = qc.accent
                )
                Text(
                    text = insight.subtitleAr,
                    style = MaterialTheme.typography.bodySmall,
                    color = qc.text
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = "عرض التقويم الهجري",
                tint = qc.accent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
internal fun PrayerScheduleSectionHeader() {
    val qc = MaterialTheme.quranColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "جدول الصلوات اليومي",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = qc.text
        )
        Surface(
            shape = RoundedCornerShape(50),
            color = qc.surface,
            border = BorderStroke(1.dp, qc.border)
        ) {
            Text(
                text = "تنبيه / أذان",
                style = MaterialTheme.typography.labelSmall,
                color = qc.textMuted,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
internal fun NightWatchCard(
    midnightMillis: Long,
    lastThirdMillis: Long,
    use24Hour: Boolean,
    timeZone: TimeZone = TimeZone.getDefault()
) {
    val useWestern = LocalUseWesternDigits.current
    val qc = MaterialTheme.quranColors
    val cardShape = RoundedCornerShape(24.dp)
    val glowBrush = remember(qc.surface, qc.surface2, qc.pastelLavender) {
        Brush.linearGradient(
            listOf(
                qc.surface,
                qc.pastelLavender.copy(alpha = 0.08f),
                qc.surface2.copy(alpha = 0.65f)
            )
        )
    }

    Surface(
        shape = cardShape,
        color = qc.surface,
        border = BorderStroke(1.dp, qc.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(glowBrush)
                .drawBehind {
                    val c = qc.accent.copy(alpha = 0.14f)
                    drawCircle(color = c, radius = 2.5.dp.toPx(), center = Offset(size.width * 0.12f, size.height * 0.22f))
                    drawCircle(color = c, radius = 1.6.dp.toPx(), center = Offset(size.width * 0.18f, size.height * 0.14f))
                    drawCircle(color = c, radius = 2.dp.toPx(), center = Offset(size.width * 0.08f, size.height * 0.35f))
                }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = qc.pastelLavender.copy(alpha = 0.18f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.NightsStay,
                            contentDescription = null,
                            tint = qc.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "أوقات الليل وقيامه",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = qc.text
                    )
                    Text(
                        text = "ينزل ربنا كل ليلة إلى السماء الدنيا حين يبقى ثلث الليل الآخر",
                        style = MaterialTheme.typography.labelSmall,
                        color = qc.textMuted
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NightMarkerTile(
                    icon = Icons.Rounded.Bedtime,
                    title = "منتصف الليل الشرعي",
                    time = PrayerTimesCalculator.formatTime(midnightMillis, timeZone, use24Hour).localizeDigits(useWestern),
                    modifier = Modifier.weight(1f)
                )
                NightMarkerTile(
                    icon = Icons.Rounded.DarkMode,
                    title = "بداية الثلث الأخير",
                    time = PrayerTimesCalculator.formatTime(lastThirdMillis, timeZone, use24Hour).localizeDigits(useWestern),
                    highlighted = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NightMarkerTile(
    icon: ImageVector,
    title: String,
    time: String,
    highlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (highlighted) qc.primary.copy(alpha = 0.10f) else qc.bg.copy(alpha = 0.72f),
        border = BorderStroke(
            width = 1.dp,
            color = if (highlighted) qc.primary.copy(alpha = 0.35f) else qc.border
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (highlighted) qc.primary else qc.textMuted,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (highlighted) qc.primary else qc.textMuted
                )
            }
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = time,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                ),
                color = qc.text
            )
        }
    }
}
