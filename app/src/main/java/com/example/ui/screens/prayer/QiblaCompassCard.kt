package com.example.ui.screens.prayer

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranColors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
internal fun QiblaCompassCard(
    cityNameAr: String,
    qiblaBearingDegrees: Double
) {
    val haptic = LocalHapticFeedback.current
    val useWestern = LocalUseWesternDigits.current
    val qc = MaterialTheme.quranColors
    val sensor = rememberQiblaSensorState()
    var wasAligned by remember { mutableStateOf(false) }

    val relativeQibla = if (sensor.hasSensor) {
        ((qiblaBearingDegrees.toFloat() - sensor.azimuthDeg) + 360f) % 360f
    } else {
        qiblaBearingDegrees.toFloat()
    }
    val signedDiff = ((relativeQibla + 540f) % 360f) - 180f
    val isAligned = sensor.hasSensor && abs(signedDiff) <= 4f
    val isLevel = abs(sensor.pitchDeg) <= 12f && abs(sensor.rollDeg) <= 12f

    LaunchedEffect(isAligned) {
        if (isAligned && !wasAligned) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        wasAligned = isAligned
    }

    val needleRotation by animateFloatAsState(
        targetValue = signedDiff,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.8f),
        label = "qibla_needle_angle"
    )
    val dialRotation by animateFloatAsState(
        targetValue = if (sensor.hasSensor) -sensor.azimuthDeg else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.85f),
        label = "qibla_dial_angle"
    )

    val cardShape = RoundedCornerShape(24.dp)
    val primaryColor = qc.primary
    val goldColor = qc.accent
    val outlineColor = qc.border
    val onSurfaceColor = qc.text

    Surface(
        shape = cardShape,
        color = qc.surface,
        border = BorderStroke(
            width = if (isAligned) 2.dp else 1.dp,
            color = if (isAligned) primaryColor else outlineColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_qibla_compass_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Explore,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "بوصلة القبلة — $cityNameAr",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = onSurfaceColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isAligned) primaryColor else qc.surface2,
                    border = BorderStroke(1.dp, if (isAligned) primaryColor else outlineColor)
                ) {
                    Text(
                        text = "${qiblaBearingDegrees.roundToInt().toAppDigits(useWestern)}° من الشمال",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"
                        ),
                        color = if (isAligned) qc.onPrimary else qc.text,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .size(214.dp)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f - 10.dp.toPx()

                    drawCircle(
                        color = if (isAligned) primaryColor.copy(alpha = 0.14f) else qc.bg.copy(alpha = 0.55f),
                        radius = radius,
                        center = center
                    )
                    drawCircle(
                        color = if (isAligned) primaryColor else outlineColor,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = goldColor.copy(alpha = 0.25f),
                        radius = radius - 18.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    rotate(degrees = dialRotation, pivot = center) {
                        for (deg in 0 until 360 step 15) {
                            val rad = deg * PI / 180.0
                            val isMajor = deg % 90 == 0
                            val tickStart = radius - if (isMajor) 14.dp.toPx() else 7.dp.toPx()
                            val start = Offset(
                                x = center.x + (tickStart * sin(rad)).toFloat(),
                                y = center.y - (tickStart * cos(rad)).toFloat()
                            )
                            val end = Offset(
                                x = center.x + (radius * sin(rad)).toFloat(),
                                y = center.y - (radius * cos(rad)).toFloat()
                            )
                            drawLine(
                                color = if (deg == 0) goldColor else onSurfaceColor.copy(alpha = if (isMajor) 0.7f else 0.3f),
                                start = start,
                                end = end,
                                strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    rotate(degrees = needleRotation, pivot = center) {
                        val needleLength = radius - 22.dp.toPx()
                        val pointerPath = Path().apply {
                            moveTo(center.x, center.y - needleLength)
                            lineTo(center.x - 10.dp.toPx(), center.y)
                            lineTo(center.x, center.y - 12.dp.toPx())
                            lineTo(center.x + 10.dp.toPx(), center.y)
                            close()
                        }
                        drawPath(path = pointerPath, color = if (isAligned) primaryColor else goldColor)
                        drawCircle(
                            color = goldColor,
                            radius = 7.dp.toPx(),
                            center = Offset(center.x, center.y - needleLength - 8.dp.toPx())
                        )
                    }

                    val levelRingRadius = 22.dp.toPx()
                    drawCircle(
                        color = if (isLevel) primaryColor.copy(alpha = 0.55f) else outlineColor.copy(alpha = 0.65f),
                        radius = levelRingRadius,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    val maxOffset = levelRingRadius - 6.dp.toPx()
                    val dx = (sensor.rollDeg / 45f).coerceIn(-1f, 1f) * maxOffset
                    val dy = (-sensor.pitchDeg / 45f).coerceIn(-1f, 1f) * maxOffset
                    val dist = hypot(dx, dy)
                    val scale = if (dist > maxOffset && dist > 0f) maxOffset / dist else 1f
                    drawCircle(
                        color = if (isLevel) primaryColor else goldColor.copy(alpha = 0.85f),
                        radius = 5.dp.toPx(),
                        center = Offset(center.x + dx * scale, center.y + dy * scale)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = when {
                    !sensor.hasSensor -> "مستشعر البوصلة غير متوفر على هذا الجهاز — تُعرض زاوية القبلة الجغرافية."
                    isAligned -> "أنت الآن متجه نحو القبلة المشرفة تماماً"
                    !isLevel -> "أمسك الهاتف أفقياً لضبط ميزان الفقاعة الوسطى"
                    else -> "أدر الهاتف أفقياً حتى يتجه السهم الذهبي إلى الأعلى"
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isAligned) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isAligned) primaryColor else qc.textMuted,
                textAlign = TextAlign.Center
            )

            if (sensor.hasSensor && sensor.needsCalibration) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = qc.pastelAmber.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, qc.pastelAmber.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ScreenRotation,
                            contentDescription = null,
                            tint = qc.accent,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "حرّك الهاتف بشكل الرقم ${8.toAppDigits(useWestern)} لمعايرة البوصلة",
                            style = MaterialTheme.typography.labelSmall,
                            color = qc.text
                        )
                    }
                }
            }
        }
    }
}
