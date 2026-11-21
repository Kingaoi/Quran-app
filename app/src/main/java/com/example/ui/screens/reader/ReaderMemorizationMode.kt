package com.example.ui.screens.reader

import android.graphics.RectF
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AyahItem
import com.example.domain.model.toAppDigits
import com.example.ui.components.MushafCropCoordinateMap
import com.example.ui.components.SvgPageAyahMap
import com.example.ui.theme.LocalQuranFontFamily
import kotlin.math.roundToInt

internal enum class MemorizationMaskStyle {
    FULL_BLANK,
    FIRST_WORD_HINT
}

@Immutable
internal data class MemorizationMaskState(
    val hideMode: Boolean = false,
    val maskStyle: MemorizationMaskStyle = MemorizationMaskStyle.FULL_BLANK,
    val revealedAyahs: Set<Pair<Int, Int>> = emptySet()
)

private sealed interface CachedVeilShape {
    data class Poly(val path: Path) : CachedVeilShape
    data class BoxRect(val left: Float, val top: Float, val width: Float, val height: Float) : CachedVeilShape
}

@Composable
internal fun ReaderMemorizationVeilOverlay(
    svgMap: SvgPageAyahMap?,
    pageKey: String,
    pageAyahs: List<AyahItem>,
    maskState: MemorizationMaskState,
    paperBg: Color,
    accentColor: Color,
    userZoom: Float,
    panOffset: Offset,
    metricsRevision: Int = 0,
    modifier: Modifier = Modifier
) {
    if (!maskState.hideMode || svgMap == null) return

    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val metrics = remember(pageKey, metricsRevision) { MushafCropCoordinateMap.getMetrics(pageKey) }
    val veilFill = lerp(paperBg, accentColor, 0.14f).copy(alpha = 0.97f)
    val veilBorder = accentColor.copy(alpha = 0.44f)
    val ayahMap = remember(pageAyahs) {
        pageAyahs.associateBy { it.surahNumber to it.ayahNumber }
    }

    val cachedVeils = remember(svgMap, maskState.revealedAyahs, metrics, boxSize) {
        if (boxSize.width <= 0 || boxSize.height <= 0) {
            emptyList()
        } else {
            val w = boxSize.width.toFloat()
            val h = boxSize.height.toFloat()
            val ptBuf = FloatArray(2)
            buildList {
                for (entry in svgMap.ayahs) {
                    val key = entry.surahNumber to entry.ayahNumber
                    if (key in maskState.revealedAyahs) continue
                    val count = maxOf(entry.rects.size, entry.subpathPolygons.size)
                    for (idx in 0 until count) {
                        val poly = entry.subpathPolygons.getOrNull(idx)
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
                            add(CachedVeilShape.Poly(path))
                        } else {
                            val raw: RectF = entry.rects.getOrNull(idx) ?: continue
                            val mapped = MushafCropCoordinateMap.svgRectToViewport(raw, svgMap.viewBox, metrics, w, h)
                            if (mapped.width() > 1f && mapped.height() > 1f) {
                                add(CachedVeilShape.BoxRect(mapped.left, mapped.top, mapped.width(), mapped.height()))
                            }
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { boxSize = it }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (cachedVeils.isEmpty()) return@Canvas
            val cx = size.width / 2f
            val cy = size.height / 2f
            val effectiveZoom = userZoom.coerceAtLeast(1f)
            val borderStroke = Stroke(width = 1.5f / effectiveZoom, cap = StrokeCap.Round, join = StrokeJoin.Round)
            val corner = CornerRadius(7f / effectiveZoom, 7f / effectiveZoom)

            withTransform({
                if (panOffset != Offset.Zero) translate(panOffset.x, panOffset.y)
                if (effectiveZoom != 1f) scale(effectiveZoom, effectiveZoom, Offset(cx, cy))
            }) {
                cachedVeils.forEach { shape ->
                    when (shape) {
                        is CachedVeilShape.Poly -> {
                            drawPath(path = shape.path, color = veilFill)
                            drawPath(path = shape.path, color = veilBorder, style = borderStroke)
                        }
                        is CachedVeilShape.BoxRect -> {
                            val tl = Offset(shape.left, shape.top)
                            val sz = Size(shape.width, shape.height)
                            drawRoundRect(color = veilFill, topLeft = tl, size = sz, cornerRadius = corner)
                            drawRoundRect(color = veilBorder, topLeft = tl, size = sz, cornerRadius = corner, style = borderStroke)
                        }
                    }
                }
            }
        }

        if (maskState.maskStyle == MemorizationMaskStyle.FIRST_WORD_HINT && boxSize.width > 0 && boxSize.height > 0) {
            val w = boxSize.width.toFloat()
            val h = boxSize.height.toFloat()
            val cx = w / 2f
            val cy = h / 2f
            val quranFont = LocalQuranFontFamily.current

            svgMap.ayahs.forEach { entry ->
                val key = entry.surahNumber to entry.ayahNumber
                if (key in maskState.revealedAyahs) return@forEach
                val anchorRect = entry.rects.firstOrNull() ?: return@forEach
                val ayah = ayahMap[key] ?: return@forEach
                val hintWords = ayah.text.trim().split(' ').filter { it.isNotBlank() }.take(2).joinToString(" ")
                if (hintWords.isBlank()) return@forEach

                val mapped = MushafCropCoordinateMap.svgRectToViewport(
                    svgRect = anchorRect,
                    viewBox = svgMap.viewBox,
                    metrics = metrics,
                    boxWidthPx = w,
                    boxHeightPx = h
                )
                val anchorRight = ((mapped.right - cx) * userZoom + cx + panOffset.x).roundToInt()
                val anchorTop = ((mapped.top - cy) * userZoom + cy + panOffset.y).roundToInt()
                val xPos = (anchorRight - 135).coerceIn(12, (boxSize.width - 150).coerceAtLeast(12))
                val yPos = anchorTop.coerceIn(8, (boxSize.height - 44).coerceAtLeast(8))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = lerp(paperBg, accentColor, 0.22f).copy(alpha = 0.96f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.55f)),
                    modifier = Modifier.offset { IntOffset(xPos, yPos) }
                ) {
                    Text(
                        text = "${ayah.ayahNumber.toAppDigits()} • $hintWords...",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = quranFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
