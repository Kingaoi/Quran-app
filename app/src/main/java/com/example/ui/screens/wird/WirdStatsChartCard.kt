package com.example.ui.screens.wird

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.domain.model.AdvancedStatsSnapshot
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.StatsChartMetric
import com.example.domain.model.localizeDigits
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranColors
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WirdStatsChartCard(
    snapshot: AdvancedStatsSnapshot,
    suggestedMetric: StatsChartMetric? = null,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    var rangeDays by remember { mutableIntStateOf(7) }
    var metric by remember { mutableStateOf(suggestedMetric ?: StatsChartMetric.QURAN_PAGES) }
    LaunchedEffect(suggestedMetric) {
        if (suggestedMetric != null) metric = suggestedMetric
    }
    var selectedIdx by remember(rangeDays, metric) { mutableStateOf<Int?>(null) }

    val series = remember(snapshot, rangeDays) { if (rangeDays == 30) snapshot.days30 else snapshot.days7 }
    val values = remember(series, metric) { series.map { it.valueFor(metric) } }
    val goalRef = if (metric == StatsChartMetric.QURAN_PAGES) snapshot.dailyGoalPages else 0
    val maxVal = remember(values, goalRef) { maxOf(values.maxOrNull() ?: 1, goalRef, 1) }
    val periodTotal = remember(values) { values.sum() }
    val periodAvg = if (series.isNotEmpty()) periodTotal.toFloat() / series.size else 0f
    val periodBest = remember(series, metric) { series.maxByOrNull { it.valueFor(metric) } }
    val accent = when (metric) {
        StatsChartMetric.QURAN_PAGES -> qc.primary
        StatsChartMetric.ADHKAR -> qc.accent
        StatsChartMetric.TASBIH -> qc.success
    }

    fun pickIndexFromX(x: Float, widthPx: Int): Int {
        if (series.size <= 1 || widthPx <= 0) return 0
        val step = widthPx.toFloat() / (series.size - 1).toFloat()
        return (x / step).roundToInt().coerceIn(0, series.lastIndex)
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = qc.surface,
        border = BorderStroke(1.dp, qc.border),
        modifier = modifier.fillMaxWidth().testTag("stats_trend_chart_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.Timeline, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
                    Text(
                        text = "المنحنى البياني للأداء",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = qc.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(7 to "٧ أيام", 30 to "٣٠ يوماً").forEach { (days, title) ->
                        val active = rangeDays == days
                        Surface(
                            onClick = { rangeDays = days },
                            shape = RoundedCornerShape(50),
                            color = if (active) accent else qc.surface2,
                            modifier = Modifier.heightIn(min = 36.dp).testTag("stats_range_${days}d_chip")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (active) qc.onPrimary else qc.textMuted
                                )
                            }
                        }
                    }
                }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StatsChartMetric.entries.forEach { m ->
                    val selected = metric == m
                    FilterChip(
                        selected = selected,
                        onClick = { metric = m },
                        label = { Text(m.labelAr, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = accent.copy(alpha = 0.16f), selectedLabelColor = accent),
                        modifier = Modifier.testTag("stats_metric_${m.name.lowercase()}")
                    )
                }
            }

            AnimatedContent(targetState = selectedIdx?.let { series.getOrNull(it) }, label = "chart_inspector") { picked ->
                val text = if (picked != null) {
                    "${picked.dateLabelAr}: ${picked.valueFor(metric).toAppDigits(useWestern)} ${metric.unitAr}"
                } else {
                    val goalNote = if (goalRef > 0) " • الخط المتقطع: الهدف اليومي (${goalRef.toAppDigits(useWestern)} ص)" else ""
                    "المس أو اسحب على المنحنى للتفاصيل$goalNote"
                }
                Surface(shape = RoundedCornerShape(10.dp), color = qc.surface2.copy(alpha = 0.65f)) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (picked != null) accent else qc.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Force consistent LTR coordinate space between Canvas X-points, pointer input, and axis labels
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(148.dp)
                            .pointerInput(series.size) {
                                detectTapGestures { offset ->
                                    val idx = pickIndexFromX(offset.x, size.width)
                                    selectedIdx = if (selectedIdx == idx) null else idx
                                }
                            }
                            .pointerInput(series.size) {
                                detectHorizontalDragGestures { change, _ ->
                                    change.consume()
                                    selectedIdx = pickIndexFromX(change.position.x, size.width)
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxWidth().height(148.dp)) {
                            val padX = 8.dp.toPx()
                            val w = (size.width - padX * 2f).coerceAtLeast(10f)
                            val h = size.height
                            val topPad = 14.dp.toPx()
                            val bottomPad = 10.dp.toPx()
                            val plotH = (h - topPad - bottomPad).coerceAtLeast(10f)
                            val count = series.size.coerceAtLeast(1)
                            val stepX = if (count > 1) w / (count - 1).toFloat() else w

                            if (goalRef > 0) {
                                val goalY = topPad + plotH * (1f - (goalRef.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f))
                                drawLine(
                                    color = qc.accent.copy(alpha = 0.6f),
                                    start = Offset(padX, goalY),
                                    end = Offset(padX + w, goalY),
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                                )
                            }

                            val pts = series.mapIndexed { idx, pt ->
                                val x = if (count > 1) padX + idx * stepX else padX + w / 2f
                                val ratio = (pt.valueFor(metric).toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
                                Offset(x, topPad + plotH * (1f - ratio))
                            }

                            if (rangeDays <= 7) {
                                val barW = (w / (count * 2.5f)).coerceAtLeast(10.dp.toPx())
                                pts.forEachIndexed { idx, p ->
                                    val barH = (topPad + plotH - p.y).coerceAtLeast(4.dp.toPx())
                                    drawRoundRect(
                                        color = accent.copy(alpha = if (selectedIdx == idx) 0.42f else 0.16f),
                                        topLeft = Offset(p.x - barW / 2f, topPad + plotH - barH),
                                        size = Size(barW, barH),
                                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                    )
                                }
                            }

                            if (pts.size >= 2) {
                                val linePath = Path().apply {
                                    moveTo(pts.first().x, pts.first().y)
                                    for (i in 0 until pts.lastIndex) {
                                        val midX = (pts[i].x + pts[i + 1].x) / 2f
                                        cubicTo(midX, pts[i].y, midX, pts[i + 1].y, pts[i + 1].x, pts[i + 1].y)
                                    }
                                }
                                val fillPath = Path().apply {
                                    addPath(linePath)
                                    lineTo(pts.last().x, topPad + plotH)
                                    lineTo(pts.first().x, topPad + plotH)
                                    close()
                                }
                                drawPath(fillPath, brush = Brush.verticalGradient(listOf(accent.copy(alpha = 0.28f), accent.copy(alpha = 0.02f))))
                                drawPath(linePath, color = accent, style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round))
                            }

                            pts.forEachIndexed { idx, p ->
                                val isPicked = selectedIdx == idx
                                if (isPicked) {
                                    drawLine(accent.copy(alpha = 0.4f), Offset(p.x, topPad), Offset(p.x, topPad + plotH), strokeWidth = 1.5.dp.toPx())
                                }
                                if (rangeDays <= 7 || isPicked || series[idx].valueFor(metric) > 0) {
                                    val r = if (isPicked) 6.dp.toPx() else if (rangeDays <= 7) 4.dp.toPx() else 2.5.dp.toPx()
                                    drawCircle(color = qc.surface, radius = r + 1.5.dp.toPx(), center = p)
                                    drawCircle(color = accent, radius = r, center = p)
                                }
                            }
                        }
                    }

                    if (rangeDays <= 7) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            series.forEach { pt ->
                                Text(pt.shortWeekdayAr, style = MaterialTheme.typography.labelSmall, color = qc.textMuted)
                            }
                        }
                    } else if (series.size >= 3) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(series.first().dateLabelAr.localizeDigits(useWestern), style = MaterialTheme.typography.labelSmall, color = qc.textMuted)
                            Text(series[series.size / 2].dateLabelAr.localizeDigits(useWestern), style = MaterialTheme.typography.labelSmall, color = qc.textMuted)
                            Text("اليوم", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = accent)
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricFooterChip("الإجمالي", "${periodTotal.toAppDigits(useWestern)} ${metric.unitAr}", Modifier.weight(1f))
                MetricFooterChip("المتوسط اليومي", ((periodAvg * 10f).roundToInt() / 10f).toString().localizeDigits(useWestern), Modifier.weight(1f))
                MetricFooterChip("أعلى يوم", "${(periodBest?.valueFor(metric) ?: 0).toAppDigits(useWestern)} ${metric.unitAr}", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricFooterChip(label: String, value: String, modifier: Modifier = Modifier) {
    val qc = MaterialTheme.quranColors
    Surface(shape = RoundedCornerShape(12.dp), color = qc.surface2.copy(alpha = 0.65f), modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = qc.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, style = MaterialTheme.typography.labelSmall, color = qc.textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
