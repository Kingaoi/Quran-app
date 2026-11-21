package com.example.ui.screens.prayer

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.localizeDigits
import com.example.domain.prayer.PrayerName
import com.example.domain.prayer.PrayerTimeEntry
import com.example.domain.prayer.PrayerTimesCalculator
import com.example.ui.theme.quranColors

@Composable
internal fun PrayerListItemCard(
    entry: PrayerTimeEntry,
    isNextPrayer: Boolean,
    isPassed: Boolean,
    isAlertEnabled: Boolean,
    use24Hour: Boolean,
    onToggleAlert: (PrayerName) -> Unit
) {
    val useWestern = LocalUseWesternDigits.current
    val qc = MaterialTheme.quranColors
    val cardShape = RoundedCornerShape(22.dp)
    val isSunrise = !entry.prayer.isMandatoryPrayer

    val containerColor by animateColorAsState(
        targetValue = when {
            isNextPrayer -> qc.surface2
            isSunrise -> qc.pastelAmber.copy(alpha = 0.08f)
            isPassed -> qc.surface.copy(alpha = 0.72f)
            else -> qc.surface
        },
        label = "prayer_row_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isNextPrayer -> qc.primary
            isSunrise -> qc.pastelAmber.copy(alpha = 0.45f)
            else -> qc.border
        },
        label = "prayer_row_border"
    )

    val formattedTime = PrayerTimesCalculator.formatHourMinute(
        hour24 = entry.hour24,
        minute = entry.minute,
        use24Hour = use24Hour
    ).localizeDigits(useWestern)

    val rowBackground = if (isNextPrayer) {
        Brush.horizontalGradient(
            listOf(qc.primary.copy(alpha = 0.14f), qc.surface2)
        )
    } else {
        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
    }

    Surface(
        shape = cardShape,
        color = containerColor,
        border = BorderStroke(
            width = if (isNextPrayer) 1.6.dp else 1.dp,
            color = borderColor
        ),
        tonalElevation = if (isNextPrayer) 3.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isPassed && !isNextPrayer) 0.78f else 1f)
            .clip(cardShape)
            .testTag("prayer_item_${entry.prayer.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(rowBackground)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isNextPrayer -> qc.primary
                            isSunrise -> qc.pastelAmber.copy(alpha = 0.18f)
                            else -> qc.surface2
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = prayerPhaseIcon(entry.prayer),
                    contentDescription = entry.prayer.titleAr,
                    tint = when {
                        isNextPrayer -> qc.onPrimary
                        isSunrise -> qc.accent
                        else -> qc.primary
                    },
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = entry.prayer.titleAr,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isPassed && !isNextPrayer) qc.textMuted else qc.text
                    )

                    if (isNextPrayer) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = qc.primary
                        ) {
                            Text(
                                text = "القادمة",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = qc.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    } else if (isPassed) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "انتهى وقتها",
                            tint = qc.success.copy(alpha = 0.75f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Text(
                    text = prayerPhaseSubtitleAr(entry.prayer),
                    style = MaterialTheme.typography.bodySmall,
                    color = qc.textMuted
                )
            }

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                ),
                color = when {
                    isNextPrayer -> qc.primary
                    isPassed -> qc.textMuted
                    else -> qc.text
                }
            )

            Spacer(modifier = Modifier.width(6.dp))

            if (entry.prayer.isMandatoryPrayer) {
                IconButton(
                    onClick = { onToggleAlert(entry.prayer) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("prayer_alert_toggle_${entry.prayer.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isAlertEnabled) qc.primary.copy(alpha = 0.14f)
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAlertEnabled) {
                                Icons.Rounded.NotificationsActive
                            } else {
                                Icons.Rounded.NotificationsNone
                            },
                            contentDescription = if (isAlertEnabled) {
                                "إيقاف تنبيه ${entry.prayer.titleAr}"
                            } else {
                                "تفعيل تنبيه ${entry.prayer.titleAr}"
                            },
                            tint = if (isAlertEnabled) qc.primary else qc.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    }
}

private fun prayerPhaseSubtitleAr(prayer: PrayerName): String = when (prayer) {
    PrayerName.FAJR -> "طلوع الفجر الصادق"
    PrayerName.SUNRISE -> "شروق الشمس • نهاية وقت الفجر"
    PrayerName.DHUHR -> "زوال الشمس"
    PrayerName.ASR -> "وقت العصر"
    PrayerName.MAGHRIB -> "غروب الشمس"
    PrayerName.ISHA -> "غياب الشفق"
}
