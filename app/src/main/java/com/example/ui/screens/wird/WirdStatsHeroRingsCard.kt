package com.example.ui.screens.wird

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AdvancedStatsSnapshot
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.domain.wird.WirdPlanSummary
import com.example.ui.theme.quranColors
import kotlin.math.abs

@Composable
internal fun WirdStatsHeroRingsCard(
    snapshot: AdvancedStatsSnapshot,
    activeWirdPlan: WirdPlanSummary? = null,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current

    val animQuran by animateFloatAsState(
        targetValue = snapshot.quranGoalFraction,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ring_quran"
    )
    val animSpiritual by animateFloatAsState(
        targetValue = snapshot.spiritualTodayRatio,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ring_spiritual"
    )
    val animRawatib by animateFloatAsState(
        targetValue = snapshot.rawatibRatio,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ring_rawatib"
    )

    val heroBg = Brush.linearGradient(
        colors = listOf(
            qc.primary.copy(alpha = 0.10f),
            qc.surface,
            qc.accent.copy(alpha = 0.09f)
        )
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = qc.surface,
        border = BorderStroke(1.dp, qc.border),
        modifier = modifier.fillMaxWidth().testTag("stats_hero_rings_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().background(heroBg).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(142.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(142.dp)) {
                        val strokePx = 8.5.dp.toPx()
                        val gapPx = 4.dp.toPx()
                        val ringColors = listOf(qc.primary, qc.accent, qc.success)
                        val progressValues = listOf(animQuran, animSpiritual, animRawatib)
                        ringColors.forEachIndexed { idx, color ->
                            val inset = strokePx / 2f + idx * (strokePx + gapPx)
                            val diameter = size.minDimension - inset * 2f
                            if (diameter > 0f) {
                                val topLeft = Offset(inset, inset)
                                val arcSize = Size(diameter, diameter)
                                val style = Stroke(width = strokePx, cap = StrokeCap.Round)
                                drawArc(color.copy(alpha = 0.16f), 0f, 360f, false, topLeft, arcSize, style = style)
                                val sweep = (progressValues[idx].coerceIn(0f, 1f) * 360f)
                                if (sweep > 0f) {
                                    drawArc(color, -90f, sweep, false, topLeft, arcSize, style = style)
                                }
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${snapshot.overallDailyScorePercent.toAppDigits(useWestern)}٪",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = qc.text,
                            maxLines = 1
                        )
                        Text(
                            text = "التوازن",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = qc.textMuted,
                            maxLines = 1
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RingLegendRow(
                        color = qc.primary,
                        label = "تلاوة المصحف",
                        detail = "${snapshot.todayPages.toAppDigits(useWestern)}/${snapshot.dailyGoalPages.toAppDigits(useWestern)} ص"
                    )
                    RingLegendRow(
                        color = qc.accent,
                        label = "الأذكار والتسبيح",
                        detail = "${(snapshot.spiritualTodayRatio * 100).toInt().toAppDigits(useWestern)}٪"
                    )
                    RingLegendRow(
                        color = qc.success,
                        label = "السنن الرواتب",
                        detail = "${snapshot.rawatibRakatToday.toAppDigits(useWestern)}/${12.toAppDigits(useWestern)} ركعة"
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val trend = snapshot.weeklyTrendPercent
                val trendLabel = when {
                    trend == null -> "ابدأ قراءة اليوم"
                    trend > 0 -> "+${trend.toAppDigits(useWestern)}٪ عن الأسبوع السابق"
                    trend < 0 -> "−${abs(trend).toAppDigits(useWestern)}٪ عن الأسبوع السابق"
                    else -> "ثبات مقارنة بالأسبوع السابق"
                }
                InsightPill(
                    icon = Icons.AutoMirrored.Rounded.TrendingUp,
                    text = trendLabel,
                    accentColor = if ((trend ?: 0) >= 0) qc.success else qc.accent,
                    modifier = Modifier.weight(1f)
                )

                val paceLabel = when {
                    activeWirdPlan != null && !activeWirdPlan.isCompleted ->
                        "ختمتك: ${(activeWirdPlan.progressFraction * 100).toInt().toAppDigits(useWestern)}٪"
                    snapshot.projectedDaysPerKhatmah != null ->
                        "ختمة كل ~${snapshot.projectedDaysPerKhatmah.toAppDigits(useWestern)} يوماً"
                    else -> "وتيرة ٣٠ يوماً: ${snapshot.monthlyPages.toAppDigits(useWestern)} ص"
                }
                InsightPill(
                    icon = if (activeWirdPlan != null) Icons.AutoMirrored.Rounded.MenuBook else Icons.Rounded.Schedule,
                    text = paceLabel,
                    accentColor = qc.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RingLegendRow(color: Color, label: String, detail: String) {
    val qc = MaterialTheme.quranColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(color))
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = qc.text)
        }
        Text(text = detail, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = color)
    }
}

@Composable
private fun InsightPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = accentColor.copy(alpha = 0.11f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(15.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = qc.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
