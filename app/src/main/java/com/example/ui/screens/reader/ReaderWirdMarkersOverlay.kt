package com.example.ui.screens.reader

import android.graphics.RectF
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CropTransformMetrics
import com.example.ui.components.MushafCropCoordinateMap
import com.example.ui.components.SvgPageAyahMap
import com.example.ui.components.SvgViewBoxRect
import com.example.ui.theme.quranColors
import kotlin.math.roundToInt

private data class MappedBoundaryBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

private sealed interface CachedWirdTintShape {
    data class Poly(val path: Path) : CachedWirdTintShape
    data class Box(val left: Float, val top: Float, val width: Float, val height: Float) : CachedWirdTintShape
}

@Composable
internal fun ReaderWirdMarkersOverlay(
    svgMap: SvgPageAyahMap?,
    pageKey: String,
    wirdStartAyah: Pair<Int, Int>?,
    wirdEndAyah: Pair<Int, Int>?,
    startColor: Color,
    endColor: Color,
    userZoom: Float,
    panOffset: Offset,
    metricsRevision: Int = 0,
    modifier: Modifier = Modifier
) {
    if (svgMap == null || (wirdStartAyah == null && wirdEndAyah == null)) return

    val metrics = remember(pageKey, metricsRevision) { MushafCropCoordinateMap.getMetrics(pageKey) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val startRects = remember(svgMap, wirdStartAyah) {
        wirdStartAyah?.let { svgMap.rectsForAyah(it.first, it.second) }.orEmpty()
    }
    val startPolys = remember(svgMap, wirdStartAyah) {
        wirdStartAyah?.let { svgMap.polygonsForAyah(it.first, it.second) }.orEmpty()
    }
    val endRects = remember(svgMap, wirdEndAyah) {
        wirdEndAyah?.let { svgMap.rectsForAyah(it.first, it.second) }.orEmpty()
    }
    val endPolys = remember(svgMap, wirdEndAyah) {
        wirdEndAyah?.let { svgMap.polygonsForAyah(it.first, it.second) }.orEmpty()
    }
    if (startRects.isEmpty() && endRects.isEmpty()) return

    val startTintShapes = remember(startRects, startPolys, svgMap.viewBox, metrics, canvasSize) {
        buildWirdTintShapes(startRects, startPolys, svgMap.viewBox, metrics, canvasSize)
    }
    val endTintShapes = remember(endRects, endPolys, svgMap.viewBox, metrics, canvasSize) {
        buildWirdTintShapes(endRects, endPolys, svgMap.viewBox, metrics, canvasSize)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val effectiveZoom = userZoom.coerceAtLeast(1f)
            val cx = size.width / 2f
            val cy = size.height / 2f

            withTransform({
                if (panOffset != Offset.Zero) translate(panOffset.x, panOffset.y)
                if (effectiveZoom != 1f) scale(effectiveZoom, effectiveZoom, Offset(cx, cy))
            }) {
                if (startTintShapes.isNotEmpty()) {
                    drawCachedWirdTint(startTintShapes, startColor.copy(alpha = 0.085f))
                }
                if (endTintShapes.isNotEmpty()) {
                    drawCachedWirdTint(endTintShapes, endColor.copy(alpha = 0.085f))
                }
            }

            if (startRects.isNotEmpty()) {
                val first = startRects.minByOrNull { it.top }
                if (first != null) {
                    val m = mapRect(first, svgMap.viewBox, metrics, size.width, size.height, effectiveZoom, panOffset)
                    val span = ((m.right - m.left) * 0.42f).coerceIn(28f, 110f)
                    val tick = 14f * effectiveZoom
                    drawLine(startColor.copy(alpha = 0.88f), Offset(m.right - span, m.top - 2f), Offset(m.right, m.top - 2f), strokeWidth = 3f, cap = StrokeCap.Round)
                    drawLine(startColor.copy(alpha = 0.88f), Offset(m.right, m.top - 2f), Offset(m.right, m.top + tick), strokeWidth = 3f, cap = StrokeCap.Round)
                }
            }
            if (endRects.isNotEmpty()) {
                val last = endRects.maxByOrNull { it.bottom }
                if (last != null) {
                    val m = mapRect(last, svgMap.viewBox, metrics, size.width, size.height, effectiveZoom, panOffset)
                    val span = ((m.right - m.left) * 0.42f).coerceIn(28f, 110f)
                    val tick = 14f * effectiveZoom
                    drawLine(endColor.copy(alpha = 0.90f), Offset(m.left, m.bottom + 2f), Offset(m.left + span, m.bottom + 2f), strokeWidth = 3f, cap = StrokeCap.Round)
                    drawLine(endColor.copy(alpha = 0.90f), Offset(m.left, m.bottom + 2f), Offset(m.left, m.bottom - tick), strokeWidth = 3f, cap = StrokeCap.Round)
                }
            }
        }

        if (canvasSize.width > 0 && canvasSize.height > 0) {
            val effectiveZoom = userZoom.coerceAtLeast(1f)
            val pillContentColor = MaterialTheme.quranColors.onPrimary
            startRects.minByOrNull { it.top }?.let { first ->
                val mapped = mapRect(first, svgMap.viewBox, metrics, canvasSize.width.toFloat(), canvasSize.height.toFloat(), effectiveZoom, panOffset)
                WirdBoundaryPill(
                    label = "بداية الورد",
                    isStart = true,
                    containerColor = startColor,
                    contentColor = pillContentColor,
                    canvasSize = canvasSize,
                    anchorX = mapped.right,
                    anchorY = mapped.top,
                    alignToRightEdge = true,
                    placeAbove = true,
                    tag = "wird_page_start_marker"
                )
            }
            endRects.maxByOrNull { it.bottom }?.let { last ->
                val mapped = mapRect(last, svgMap.viewBox, metrics, canvasSize.width.toFloat(), canvasSize.height.toFloat(), effectiveZoom, panOffset)
                WirdBoundaryPill(
                    label = "نهاية الورد",
                    isStart = false,
                    containerColor = endColor,
                    contentColor = pillContentColor,
                    canvasSize = canvasSize,
                    anchorX = mapped.left,
                    anchorY = mapped.bottom,
                    alignToRightEdge = false,
                    placeAbove = false,
                    tag = "wird_page_end_marker"
                )
            }
        }
    }
}

@Composable
private fun WirdBoundaryPill(
    label: String,
    isStart: Boolean,
    containerColor: Color,
    contentColor: Color,
    canvasSize: IntSize,
    anchorX: Float,
    anchorY: Float,
    alignToRightEdge: Boolean,
    placeAbove: Boolean,
    tag: String
) {
    var badgeSize by remember { mutableStateOf(IntSize.Zero) }
    val posX = if (alignToRightEdge) (anchorX - badgeSize.width) else anchorX
    val posY = if (placeAbove) (anchorY - badgeSize.height - 6f) else (anchorY + 6f)
    val maxX = (canvasSize.width - badgeSize.width - 8).coerceAtLeast(8)
    val maxY = (canvasSize.height - badgeSize.height - 4).coerceAtLeast(4)

    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.45f)),
        shadowElevation = 3.dp,
        modifier = Modifier
            .onSizeChanged { badgeSize = it }
            .offset {
                IntOffset(
                    x = posX.roundToInt().coerceIn(8, maxX),
                    y = posY.roundToInt().coerceIn(4, maxY)
                )
            }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isStart) Icons.Rounded.Flag else Icons.Rounded.CheckCircle,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = contentColor
            )
        }
    }
}

private fun mapRect(
    raw: RectF,
    viewBox: SvgViewBoxRect,
    metrics: CropTransformMetrics?,
    widthPx: Float,
    heightPx: Float,
    userZoom: Float,
    panOffset: Offset
): MappedBoundaryBox {
    val cx = widthPx / 2f
    val cy = heightPx / 2f
    val m = MushafCropCoordinateMap.svgRectToViewport(raw, viewBox, metrics, widthPx, heightPx)
    return MappedBoundaryBox(
        left = (m.left - cx) * userZoom + cx + panOffset.x,
        top = (m.top - cy) * userZoom + cy + panOffset.y,
        right = (m.right - cx) * userZoom + cx + panOffset.x,
        bottom = (m.bottom - cy) * userZoom + cy + panOffset.y
    )
}

private fun buildWirdTintShapes(
    rects: List<RectF>,
    polys: List<FloatArray>,
    viewBox: SvgViewBoxRect,
    metrics: CropTransformMetrics?,
    canvasSize: IntSize
): List<CachedWirdTintShape> {
    if (canvasSize.width <= 0 || canvasSize.height <= 0) return emptyList()
    val w = canvasSize.width.toFloat()
    val h = canvasSize.height.toFloat()
    val ptBuf = FloatArray(2)
    val count = maxOf(rects.size, polys.size)
    return buildList(count) {
        for (idx in 0 until count) {
            val poly = polys.getOrNull(idx)
            if (poly != null && poly.size >= 6) {
                val path = Path()
                val vCount = poly.size / 2
                for (i in 0 until vCount) {
                    MushafCropCoordinateMap.writeSvgPointToViewport(
                        poly[i * 2], poly[i * 2 + 1], viewBox, metrics, w, h, ptBuf, 0
                    )
                    if (i == 0) path.moveTo(ptBuf[0], ptBuf[1]) else path.lineTo(ptBuf[0], ptBuf[1])
                }
                path.close()
                add(CachedWirdTintShape.Poly(path))
            } else {
                val raw = rects.getOrNull(idx) ?: continue
                val m = MushafCropCoordinateMap.svgRectToViewport(raw, viewBox, metrics, w, h)
                if (m.right > m.left && m.bottom > m.top) {
                    add(CachedWirdTintShape.Box(m.left, m.top, m.width(), m.height()))
                }
            }
        }
    }
}

private fun DrawScope.drawCachedWirdTint(shapes: List<CachedWirdTintShape>, tint: Color) {
    shapes.forEach { shape ->
        when (shape) {
            is CachedWirdTintShape.Poly -> drawPath(path = shape.path, color = tint)
            is CachedWirdTintShape.Box -> drawRect(
                color = tint,
                topLeft = Offset(shape.left, shape.top),
                size = Size(shape.width, shape.height)
            )
        }
    }
}
