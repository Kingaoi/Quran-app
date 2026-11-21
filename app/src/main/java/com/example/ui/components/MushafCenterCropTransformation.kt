package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import coil.size.Size
import coil.transform.Transformation
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Trims outer SVG whitespace and re-centers the actual Mushaf ink bounding box
 * so odd and even pages share symmetrical margins. Detected relative bounds are
 * cached per pageKey so revisiting or re-decoding a page skips pixel scanning.
 */
class MushafCenterCropTransformation(
    private val pageKey: String? = null,
    private val horizontalPadRatio: Float = 0.024f,
    private val verticalPadRatio: Float = 0.018f
) : Transformation {

    override val cacheKey: String =
        "mushaf_sym_crop_${pageKey.orEmpty()}_${horizontalPadRatio}_${verticalPadRatio}_v2"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val w = input.width
        val h = input.height
        if (w < 16 || h < 16) return input

        val src = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            input.config == Bitmap.Config.HARDWARE
        ) {
            input.copy(Bitmap.Config.ARGB_8888, false) ?: return input
        } else {
            input
        }

        val bgPixel = src.getPixel(0, 0)
        val bgIsOpaque = ((bgPixel ushr 24) and 0xFF) > 200

        val bounds = pageKey?.let { boundsCache[it] } ?: detectInkBounds(src, w, h, bgPixel, bgIsOpaque)?.also {
            if (pageKey != null) boundsCache[pageKey] = it
        }

        if (bounds == null) {
            if (src !== input) src.recycle()
            return input
        }

        val minX = (bounds.leftRatio * w).roundToInt().coerceIn(0, w - 1)
        val minY = (bounds.topRatio * h).roundToInt().coerceIn(0, h - 1)
        val maxX = (bounds.rightRatio * w).roundToInt().coerceIn(minX, w - 1)
        val maxY = (bounds.bottomRatio * h).roundToInt().coerceIn(minY, h - 1)

        val contentW = (maxX - minX + 1).coerceAtLeast(1)
        val contentH = (maxY - minY + 1).coerceAtLeast(1)

        val padX = (contentW * horizontalPadRatio).roundToInt().coerceAtLeast(6)
        val padY = (contentH * verticalPadRatio).roundToInt().coerceAtLeast(6)

        val minTargetW = if (contentW < w * 0.70f) (w * 0.88f).roundToInt() else 0
        val minTargetH = if (contentH < h * 0.70f) (h * 0.88f).roundToInt() else 0

        val targetW = maxOf(contentW + padX * 2, minTargetW)
        val targetH = maxOf(contentH + padY * 2, minTargetH)

        val output = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        if (bgIsOpaque) {
            canvas.drawColor(bgPixel)
        }

        val srcRect = Rect(minX, minY, maxX + 1, maxY + 1)
        val dstLeft = ((targetW - contentW) / 2f).roundToInt()
        val dstTop = ((targetH - contentH) / 2f).roundToInt()
        val dstRect = Rect(dstLeft, dstTop, dstLeft + contentW, dstTop + contentH)

        canvas.drawBitmap(src, srcRect, dstRect, Paint(Paint.FILTER_BITMAP_FLAG))

        if (pageKey != null) {
            MushafCropCoordinateMap.recordMetrics(
                pageKey = pageKey,
                metrics = CropTransformMetrics(
                    inputW = w,
                    inputH = h,
                    srcLeft = minX,
                    srcTop = minY,
                    srcW = contentW,
                    srcH = contentH,
                    dstLeft = dstLeft,
                    dstTop = dstTop,
                    dstW = contentW,
                    dstH = contentH,
                    targetW = targetW,
                    targetH = targetH
                )
            )
        }

        if (src !== input) src.recycle()
        return output
    }

    private fun detectInkBounds(
        src: Bitmap,
        w: Int,
        h: Int,
        bgPixel: Int,
        bgIsOpaque: Boolean
    ): InkBoundsRatio? {
        val bgR = (bgPixel ushr 16) and 0xFF
        val bgG = (bgPixel ushr 8) and 0xFF
        val bgB = bgPixel and 0xFF

        fun isInk(pixel: Int): Boolean {
            val a = (pixel ushr 24) and 0xFF
            if (a <= 14) return false
            if (!bgIsOpaque) return true
            val r = (pixel ushr 16) and 0xFF
            val g = (pixel ushr 8) and 0xFF
            val b = pixel and 0xFF
            return (abs(r - bgR) + abs(g - bgG) + abs(b - bgB)) > 28
        }

        val row = IntArray(w)
        var minX = w
        var maxX = -1
        var minY = h
        var maxY = -1

        var y = 0
        while (y < h) {
            src.getPixels(row, 0, w, 0, y, w, 1)
            val yInside = y in minY..maxY
            if (!yInside || minX > maxX) {
                var x = 0
                while (x < w) {
                    if (isInk(row[x])) {
                        if (x < minX) minX = x
                        if (x > maxX) maxX = x
                        if (y < minY) minY = y
                        if (y > maxY) maxY = y
                    }
                    x += 6
                }
            } else {
                var x = 0
                while (x < minX) {
                    if (isInk(row[x])) {
                        if (x < minX) minX = x
                        break
                    }
                    x += 6
                }
                var rx = w - 1
                while (rx > maxX) {
                    if (isInk(row[rx])) {
                        if (rx > maxX) maxX = rx
                        break
                    }
                    rx -= 6
                }
            }
            y += 6
        }

        if (maxX < minX || maxY < minY) return null

        val refineTop = (minY - 6).coerceAtLeast(0)
        val refineBottom = (maxY + 6).coerceAtMost(h - 1)
        val refineLeft = (minX - 6).coerceAtLeast(0)
        val refineRight = (maxX + 6).coerceAtMost(w - 1)

        for (ry in refineTop..refineBottom) {
            val isEdgeY = ry < minY || ry > maxY
            if (!isEdgeY && minX <= refineLeft && maxX >= refineRight) continue
            src.getPixels(row, 0, w, 0, ry, w, 1)
            for (rx in refineLeft..minX) {
                if (isInk(row[rx])) {
                    if (rx < minX) minX = rx
                    if (ry < minY) minY = ry
                    if (ry > maxY) maxY = ry
                }
            }
            for (rx in maxX..refineRight) {
                if (isInk(row[rx])) {
                    if (rx > maxX) maxX = rx
                    if (ry < minY) minY = ry
                    if (ry > maxY) maxY = ry
                }
            }
            if (isEdgeY) {
                for (rx in minX..maxX) {
                    if (isInk(row[rx])) {
                        if (ry < minY) minY = ry
                        if (ry > maxY) maxY = ry
                    }
                }
            }
        }

        return InkBoundsRatio(
            leftRatio = minX.toFloat() / w.toFloat(),
            topRatio = minY.toFloat() / h.toFloat(),
            rightRatio = maxX.toFloat() / w.toFloat(),
            bottomRatio = maxY.toFloat() / h.toFloat()
        )
    }

    private data class InkBoundsRatio(
        val leftRatio: Float,
        val topRatio: Float,
        val rightRatio: Float,
        val bottomRatio: Float
    )

    companion object {
        private val boundsCache = ConcurrentHashMap<String, InkBoundsRatio>()
    }
}
