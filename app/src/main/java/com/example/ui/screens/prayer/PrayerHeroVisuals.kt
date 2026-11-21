package com.example.ui.screens.prayer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.localizeDigits
import com.example.domain.prayer.PrayerName
import com.example.domain.prayer.PrayerTimeEntry
import com.example.domain.prayer.PrayerTickerState
import com.example.domain.prayer.PrayerTimesCalculator
import com.example.ui.theme.quranColors
import kotlinx.coroutines.flow.StateFlow
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal fun DrawScope.drawPrayerMihrabWatermark(accent: Color, borderTint: Color) {
    val cx = size.width * 0.14f
    val cy = size.height * 0.34f
    val ringRadius = size.minDimension * 0.42f
    drawCircle(
        color = accent.copy(alpha = 0.07f),
        radius = ringRadius,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = borderTint.copy(alpha = 0.14f),
        radius = ringRadius * 0.78f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.dp.toPx())
    )
    val sqSide = ringRadius * 0.96f
    val sqPath = Path().apply {
        moveTo(cx - sqSide / 2f, cy - sqSide / 2f)
        lineTo(cx + sqSide / 2f, cy - sqSide / 2f)
        lineTo(cx + sqSide / 2f, cy + sqSide / 2f)
        lineTo(cx - sqSide / 2f, cy + sqSide / 2f)
        close()
    }
    val stroke = Stroke(width = 1.dp.toPx())
    drawPath(sqPath, color = accent.copy(alpha = 0.12f), style = stroke)
    rotate(degrees = 45f, pivot = Offset(cx, cy)) {
        drawPath(sqPath, color = accent.copy(alpha = 0.12f), style = stroke)
    }
}

@Composable
internal fun PrayerHeroLiveCountdownSection(
    fallbackNextPrayer: PrayerTimeEntry?,
    fallbackCountdown: String,
    fallbackIqamahStatus: String?,
    fallbackProgress: Float,
    timeZone: TimeZone,
    use24Hour: Boolean,
    useWestern: Boolean,
    tickerStateFlow: StateFlow<PrayerTickerState>?
) {
    val live = tickerStateFlow?.collectAsStateWithLifecycle()?.value
    val nextPrayer = live?.nextPrayer ?: fallbackNextPrayer
    val countdownText = live?.countdownText ?: fallbackCountdown
    val iqamahStatusText = live?.iqamahStatusText ?: fallbackIqamahStatus
    val targetProgress = (live?.intervalProgress ?: fallbackProgress).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(stiffness = 220f, dampingRatio = 0.85f),
        label = "prayer_interval_progress"
    )

    val qc = MaterialTheme.quranColors
    val arcColor = qc.primary
    val accentDot = qc.accent
    val trackColor = qc.border.copy(alpha = 0.45f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = qc.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (nextPrayer != null) {
                        "الصلاة القادمة: ${nextPrayer.prayer.titleAr}"
                    } else {
                        "المواقيت اليومية"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = qc.primary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = countdownText.localizeDigits(useWestern),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    letterSpacing = 0.5.sp,
                    fontFeatureSettings = "tnum"
                ),
                color = qc.text
            )
            if (nextPrayer != null) {
                Spacer(modifier = Modifier.height(2.dp))
                val adhanFormatted = PrayerTimesCalculator.formatTime(
                    timestampMillis = nextPrayer.timestampMillis,
                    timeZone = timeZone,
                    use24Hour = use24Hour
                ).localizeDigits(useWestern)
                Text(
                    text = "يرفع الأذان عند $adhanFormatted",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                    color = qc.textMuted
                )
            }
            if (!iqamahStatusText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = qc.primary.copy(alpha = 0.14f),
                    modifier = Modifier
                        .border(1.dp, qc.primary.copy(alpha = 0.28f), RoundedCornerShape(50))
                        .testTag("prayer_iqamah_pill")
                ) {
                    Text(
                        text = iqamahStatusText.localizeDigits(useWestern),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = qc.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }

        PrayerAstronomicalDial(
            prayer = nextPrayer?.prayer,
            progress = animatedProgress,
            arcColor = arcColor,
            accentDotColor = accentDot,
            trackColor = trackColor
        )
    }
}

@Composable
private fun PrayerAstronomicalDial(
    prayer: PrayerName?,
    progress: Float,
    arcColor: Color,
    accentDotColor: Color,
    trackColor: Color
) {
    val qc = MaterialTheme.quranColors
    Box(
        modifier = Modifier.size(92.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokePx = 6.dp.toPx()
            val inset = strokePx / 2f + 4.dp.toPx()
            val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            val sweep = (progress * 360f).coerceIn(10f, 360f)
            drawArc(
                color = arcColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            val angleRad = (-90f + sweep) * PI / 180.0
            val r = arcSize.width / 2f
            val cx = size.width / 2f + (r * cos(angleRad)).toFloat()
            val cy = size.height / 2f + (r * sin(angleRad)).toFloat()
            drawCircle(color = accentDotColor, radius = 4.5.dp.toPx(), center = Offset(cx, cy))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = prayerPhaseIcon(prayer),
                contentDescription = prayer?.titleAr,
                tint = qc.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = prayer?.titleAr ?: "الصلاة",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = qc.text
            )
        }
    }
}

internal fun prayerPhaseIcon(prayer: PrayerName?): ImageVector = when (prayer) {
    PrayerName.FAJR -> Icons.Rounded.DarkMode
    PrayerName.SUNRISE -> Icons.Rounded.WbSunny
    PrayerName.DHUHR -> Icons.Rounded.LightMode
    PrayerName.ASR -> Icons.Rounded.Brightness6
    PrayerName.MAGHRIB -> Icons.Rounded.NightsStay
    PrayerName.ISHA, null -> Icons.Rounded.Bedtime
}
