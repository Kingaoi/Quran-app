package com.example.ui.screens.reader

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.example.data.local.AyahHighlightStyle
import com.example.domain.model.AyahItem
import com.example.ui.components.MushafCropCoordinateMap
import com.example.ui.components.SvgPageAyahMap

private sealed interface CachedHighlightShape {
    data class PolygonPath(val path: Path) : CachedHighlightShape
    data class RectBox(val left: Float, val top: Float, val width: Float, val height: Float) : CachedHighlightShape
}

@Composable
internal fun ReaderSvgAyahHighlightOverlay(
    svgMap: SvgPageAyahMap?,
    pageKey: String,
    highlightedAyah: Pair<Int, Int>?,
    accentColor: Color,
    highlightStyle: AyahHighlightStyle = AyahHighlightStyle.FILL_AND_BORDER,
    highlightAlpha: Float = 0.22f,
    userZoom: Float,
    panOffset: Offset,
    metricsRevision: Int = 0,
    modifier: Modifier = Modifier
) {
    if (svgMap == null || highlightedAyah == null) return
    val svgRects = remember(svgMap, highlightedAyah) {
        svgMap.rectsForAyah(highlightedAyah.first, highlightedAyah.second)
    }
    val svgPolys = remember(svgMap, highlightedAyah) {
        svgMap.polygonsForAyah(highlightedAyah.first, highlightedAyah.second)
    }
    if (svgRects.isEmpty() && svgPolys.isEmpty()) return

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val metrics = remember(pageKey, metricsRevision) { MushafCropCoordinateMap.getMetrics(pageKey) }

    val cachedShapes = remember(svgPolys, svgRects, svgMap.viewBox, metrics, canvasSize) {
        if (canvasSize.width <= 0 || canvasSize.height <= 0) {
            emptyList()
        } else {
            val w = canvasSize.width.toFloat()
            val h = canvasSize.height.toFloat()
            val ptBuf = FloatArray(2)
            val count = maxOf(svgRects.size, svgPolys.size)
            buildList(count) {
                for (idx in 0 until count) {
                    val poly = svgPolys.getOrNull(idx)
                    if (poly != null && poly.size >= 6) {
                        val vCount = poly.size / 2
                        val path = Path()
                        for (i in 0 until vCount) {
                            MushafCropCoordinateMap.writeSvgPointToViewport(
                                poly[i * 2], poly[i * 2 + 1], svgMap.viewBox, metrics, w, h, ptBuf, 0
                            )
                            if (i == 0) path.moveTo(ptBuf[0], ptBuf[1]) else path.lineTo(ptBuf[0], ptBuf[1])
                        }
                        path.close()
                        add(CachedHighlightShape.PolygonPath(path))
                    } else {
                        val raw: RectF = svgRects.getOrNull(idx) ?: continue
                        val mapped = MushafCropCoordinateMap.svgRectToViewport(raw, svgMap.viewBox, metrics, w, h)
                        if (mapped.width() > 1f && mapped.height() > 1f) {
                            add(
                                CachedHighlightShape.RectBox(
                                    left = mapped.left,
                                    top = mapped.top,
                                    width = mapped.width(),
                                    height = mapped.height()
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    val fillCol = when (highlightStyle) {
        AyahHighlightStyle.BORDER_ONLY -> Color.Transparent
        AyahHighlightStyle.FILL_AND_BORDER,
        AyahHighlightStyle.SOFT_FILL -> accentColor.copy(alpha = highlightAlpha)
    }
    val strokeCol = when (highlightStyle) {
        AyahHighlightStyle.SOFT_FILL -> Color.Transparent
        AyahHighlightStyle.FILL_AND_BORDER -> accentColor.copy(alpha = (highlightAlpha + 0.48f).coerceIn(0.55f, 0.92f))
        AyahHighlightStyle.BORDER_ONLY -> accentColor.copy(alpha = (highlightAlpha + 0.55f).coerceIn(0.65f, 0.96f))
    }
    val baseStrokeW = if (highlightStyle == AyahHighlightStyle.BORDER_ONLY) 2.6f else 2f

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
    ) {
        if (cachedShapes.isEmpty()) return@Canvas
        val cx = size.width / 2f
        val cy = size.height / 2f
        val effectiveZoom = userZoom.coerceAtLeast(1f)
        val strokeStyle = if (strokeCol.alpha > 0f) {
            Stroke(
                width = baseStrokeW / effectiveZoom,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        } else {
            null
        }
        val cornerRad = CornerRadius(6f / effectiveZoom, 6f / effectiveZoom)

        withTransform({
            if (panOffset != Offset.Zero) translate(panOffset.x, panOffset.y)
            if (effectiveZoom != 1f) scale(effectiveZoom, effectiveZoom, Offset(cx, cy))
        }) {
            cachedShapes.forEach { shape ->
                when (shape) {
                    is CachedHighlightShape.PolygonPath -> {
                        if (fillCol.alpha > 0f) drawPath(path = shape.path, color = fillCol)
                        if (strokeStyle != null) drawPath(path = shape.path, color = strokeCol, style = strokeStyle)
                    }
                    is CachedHighlightShape.RectBox -> {
                        val tl = Offset(shape.left, shape.top)
                        val sz = Size(shape.width, shape.height)
                        if (fillCol.alpha > 0f) drawRoundRect(color = fillCol, topLeft = tl, size = sz, cornerRadius = cornerRad)
                        if (strokeStyle != null) drawRoundRect(color = strokeCol, topLeft = tl, size = sz, cornerRadius = cornerRad, style = strokeStyle)
                    }
                }
            }
        }
    }
}

internal fun resolveAyahAtTap(
    tapOffset: Offset,
    boxSize: IntSize,
    userZoom: Float,
    panOffset: Offset,
    svgMap: SvgPageAyahMap?,
    pageKey: String
): Pair<Int, Int>? {
    if (svgMap == null || boxSize.width <= 0 || boxSize.height <= 0) return null
    val cx = boxSize.width / 2f
    val cy = boxSize.height / 2f
    val safeZoom = userZoom.coerceAtLeast(0.5f)
    val unzoomedX = ((tapOffset.x - panOffset.x) - cx) / safeZoom + cx
    val unzoomedY = ((tapOffset.y - panOffset.y) - cy) / safeZoom + cy

    val svgPt = MushafCropCoordinateMap.viewportPointToSvg(
        vx = unzoomedX,
        vy = unzoomedY,
        viewBox = svgMap.viewBox,
        metrics = MushafCropCoordinateMap.getMetrics(pageKey),
        boxWidthPx = boxSize.width.toFloat(),
        boxHeightPx = boxSize.height.toFloat()
    ) ?: return null

    val hit = svgMap.findAyahAt(svgPt.first, svgPt.second) ?: return null
    return hit.surahNumber to hit.ayahNumber
}

internal fun resolveLongPressedAyah(
    tapOffset: Offset,
    boxSize: IntSize,
    userZoom: Float,
    panOffset: Offset,
    svgMap: SvgPageAyahMap?,
    pageKey: String,
    pageAyahs: List<AyahItem>,
    haptic: HapticFeedback,
    onLongPressAyah: (AyahItem) -> Unit
) {
    if (pageAyahs.isEmpty()) return
    val hit = resolveAyahAtTap(tapOffset, boxSize, userZoom, panOffset, svgMap, pageKey) ?: return
    val matched = pageAyahs.firstOrNull {
        it.surahNumber == hit.first && it.ayahNumber == hit.second
    } ?: return

    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    onLongPressAyah(matched)
}
