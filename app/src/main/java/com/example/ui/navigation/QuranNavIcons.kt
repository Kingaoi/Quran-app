package com.example.ui.navigation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

internal object QuranNavIcons {

    val MushafSelected: ImageVector by lazy { buildMushafIcon(filled = true) }
    val MushafOutlined: ImageVector by lazy { buildMushafIcon(filled = false) }

    val WirdSelected: ImageVector by lazy { buildWirdIcon(filled = true) }
    val WirdOutlined: ImageVector by lazy { buildWirdIcon(filled = false) }

    val AdhkarSelected: ImageVector by lazy { buildTasbihIcon(filled = true) }
    val AdhkarOutlined: ImageVector by lazy { buildTasbihIcon(filled = false) }

    val PrayerSelected: ImageVector by lazy { buildMosqueIcon(filled = true) }
    val PrayerOutlined: ImageVector by lazy { buildMosqueIcon(filled = false) }

    val MoreSelected: ImageVector by lazy { buildMoreIcon(filled = true) }
    val MoreOutlined: ImageVector by lazy { buildMoreIcon(filled = false) }

    private fun buildMushafIcon(filled: Boolean): ImageVector {
        val name = if (filled) "MushafSelected" else "MushafOutlined"
        return ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            if (filled) {
                path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
                    // Left & Right arched open Mushaf pages
                    moveTo(2.4f, 6.2f)
                    curveTo(5.2f, 4.7f, 8.6f, 4.9f, 11.3f, 6.8f)
                    lineTo(11.3f, 19.2f)
                    curveTo(8.7f, 17.5f, 5.3f, 17.3f, 2.4f, 18.6f)
                    close()

                    moveTo(21.6f, 6.2f)
                    curveTo(18.8f, 4.7f, 15.4f, 4.9f, 12.7f, 6.8f)
                    lineTo(12.7f, 19.2f)
                    curveTo(15.3f, 17.5f, 18.7f, 17.3f, 21.6f, 18.6f)
                    close()

                    // Negative-space script lines inside filled pages
                    addLineBar(4.3f, 9.2f, 9.4f, 9.6f, 1.1f)
                    addLineBar(4.3f, 12.3f, 9.4f, 12.7f, 1.1f)
                    addLineBar(14.6f, 9.6f, 19.7f, 9.2f, 1.1f)
                    addLineBar(14.6f, 12.7f, 19.7f, 12.3f, 1.1f)
                }
                path(
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.6f,
                    strokeLineCap = StrokeCap.Round
                ) {
                    moveTo(7.5f, 20.8f)
                    curveTo(10.2f, 19.6f, 13.8f, 19.6f, 16.5f, 20.8f)
                }
            } else {
                path(
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.75f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(2.5f, 6.2f)
                    curveTo(5.3f, 4.8f, 8.8f, 5.0f, 12f, 7.0f)
                    curveTo(15.2f, 5.0f, 18.7f, 4.8f, 21.5f, 6.2f)
                    lineTo(21.5f, 18.5f)
                    curveTo(18.7f, 17.2f, 15.2f, 17.4f, 12f, 19.3f)
                    curveTo(8.8f, 17.4f, 5.3f, 17.2f, 2.5f, 18.5f)
                    close()
                    moveTo(12f, 7.0f)
                    lineTo(12f, 19.3f)
                    moveTo(5.0f, 10.0f)
                    lineTo(9.2f, 10.4f)
                    moveTo(5.0f, 13.3f)
                    lineTo(9.2f, 13.7f)
                    moveTo(14.8f, 10.4f)
                    lineTo(19.0f, 10.0f)
                    moveTo(14.8f, 13.7f)
                    lineTo(19.0f, 13.3f)
                }
            }
        }.build()
    }

    private fun buildWirdIcon(filled: Boolean): ImageVector {
        val name = if (filled) "WirdSelected" else "WirdOutlined"
        return ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            if (filled) {
                path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
                    moveTo(6.2f, 3.5f)
                    lineTo(17.8f, 3.5f)
                    curveTo(18.8f, 3.5f, 19.5f, 4.2f, 19.5f, 5.2f)
                    lineTo(19.5f, 20.5f)
                    lineTo(12f, 16.4f)
                    lineTo(4.5f, 20.5f)
                    lineTo(4.5f, 5.2f)
                    curveTo(4.5f, 4.2f, 5.2f, 3.5f, 6.2f, 3.5f)
                    close()
                    // Clean circular emblem cutout inside bookmark
                    addCircle(12f, 9.6f, 2.2f)
                }
            } else {
                path(
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.75f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(6.2f, 3.5f)
                    lineTo(17.8f, 3.5f)
                    curveTo(18.8f, 3.5f, 19.5f, 4.2f, 19.5f, 5.2f)
                    lineTo(19.5f, 20.5f)
                    lineTo(12f, 16.4f)
                    lineTo(4.5f, 20.5f)
                    lineTo(4.5f, 5.2f)
                    curveTo(4.5f, 4.2f, 5.2f, 3.5f, 6.2f, 3.5f)
                    close()
                    addCircle(12f, 9.6f, 2.0f)
                }
            }
        }.build()
    }

    private fun buildTasbihIcon(filled: Boolean): ImageVector {
        val name = if (filled) "AdhkarSelected" else "AdhkarOutlined"
        return ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            val centerX = 12f
            val centerY = 14.2f
            val ringRadius = 6.3f

            // Subtle connecting thread ring
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.3f
            ) {
                addCircle(centerX, centerY, ringRadius)
                moveTo(12f, 2.2f)
                lineTo(12f, 8.2f)
            }

            // Elongated Imam finial bead at top
            path(
                fill = if (filled) SolidColor(Color.Black) else null,
                stroke = if (filled) null else SolidColor(Color.Black),
                strokeLineWidth = if (filled) 0f else 1.55f
            ) {
                addCircle(12f, 5.8f, 1.55f)
            }

            // 9 symmetrical prayer beads around the ring
            val anglesDeg = listOf(-54f, -18f, 18f, 54f, 90f, 126f, 162f, 198f, 234f)
            path(
                fill = if (filled) SolidColor(Color.Black) else null,
                stroke = if (filled) null else SolidColor(Color.Black),
                strokeLineWidth = if (filled) 0f else 1.55f
            ) {
                for (deg in anglesDeg) {
                    val rad = Math.toRadians(deg.toDouble())
                    val bx = centerX + (ringRadius * cos(rad)).toFloat()
                    val by = centerY + (ringRadius * sin(rad)).toFloat()
                    addCircle(bx, by, 1.55f)
                }
            }
        }.build()
    }

    private fun buildMosqueIcon(filled: Boolean): ImageVector {
        val name = if (filled) "PrayerSelected" else "PrayerOutlined"
        return ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            if (filled) {
                path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
                    // Mosque pointed dome + base hall
                    moveTo(5.6f, 12.6f)
                    curveTo(5.6f, 9.2f, 8.4f, 6.7f, 12f, 5.0f)
                    curveTo(15.6f, 6.7f, 18.4f, 9.2f, 18.4f, 12.6f)
                    lineTo(20.2f, 12.6f)
                    lineTo(20.2f, 20.5f)
                    lineTo(3.8f, 20.5f)
                    lineTo(3.8f, 12.6f)
                    close()

                    // Arched mihrab doorway cutout
                    moveTo(10.0f, 20.5f)
                    lineTo(10.0f, 16.5f)
                    curveTo(10.0f, 15.1f, 11.0f, 14.3f, 12f, 13.8f)
                    curveTo(13.0f, 14.3f, 14.0f, 15.1f, 14.0f, 16.5f)
                    lineTo(14.0f, 20.5f)
                    close()
                }
                path(
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.6f,
                    strokeLineCap = StrokeCap.Round
                ) {
                    moveTo(12f, 2.4f)
                    lineTo(12f, 5.2f)
                    moveTo(4.2f, 8.5f)
                    lineTo(4.2f, 12.6f)
                    moveTo(19.8f, 8.5f)
                    lineTo(19.8f, 12.6f)
                }
            } else {
                path(
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.75f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                ) {
                    moveTo(12f, 2.5f)
                    lineTo(12f, 5.0f)
                    moveTo(5.6f, 12.6f)
                    curveTo(5.6f, 9.2f, 8.4f, 6.7f, 12f, 5.0f)
                    curveTo(15.6f, 6.7f, 18.4f, 9.2f, 18.4f, 12.6f)
                    moveTo(3.8f, 12.6f)
                    lineTo(20.2f, 12.6f)
                    lineTo(20.2f, 20.5f)
                    lineTo(3.8f, 20.5f)
                    close()
                    moveTo(10.0f, 20.5f)
                    lineTo(10.0f, 16.5f)
                    curveTo(10.0f, 15.1f, 11.0f, 14.3f, 12f, 13.8f)
                    curveTo(13.0f, 14.3f, 14.0f, 15.1f, 14.0f, 16.5f)
                    lineTo(14.0f, 20.5f)
                    moveTo(4.2f, 8.8f)
                    lineTo(4.2f, 12.6f)
                    moveTo(19.8f, 8.8f)
                    lineTo(19.8f, 12.6f)
                }
            }
        }.build()
    }

    private fun buildMoreIcon(filled: Boolean): ImageVector {
        val name = if (filled) "MoreSelected" else "MoreOutlined"
        return ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            path(
                fill = if (filled) SolidColor(Color.Black) else null,
                stroke = if (filled) null else SolidColor(Color.Black),
                strokeLineWidth = if (filled) 0f else 1.75f,
                strokeLineJoin = StrokeJoin.Round
            ) {
                addRoundedSquare(4.0f, 4.0f, 6.5f)
                addRoundedSquare(4.0f, 13.5f, 6.5f)
                addRoundedSquare(13.5f, 13.5f, 6.5f)
                // Rotated Rub' el-Hizb diamond accent in top-right quadrant
                moveTo(16.75f, 3.3f)
                lineTo(20.4f, 7.25f)
                lineTo(16.75f, 11.2f)
                lineTo(13.1f, 7.25f)
                close()
            }
        }.build()
    }

    private fun PathBuilder.addCircle(cx: Float, cy: Float, r: Float) {
        moveTo(cx - r, cy)
        arcTo(r, r, 0f, isMoreThanHalf = false, isPositiveArc = true, cx + r, cy)
        arcTo(r, r, 0f, isMoreThanHalf = false, isPositiveArc = true, cx - r, cy)
        close()
    }

    private fun PathBuilder.addRoundedSquare(left: Float, top: Float, size: Float) {
        val r = 1.6f
        val right = left + size
        val bottom = top + size
        moveTo(left + r, top)
        lineTo(right - r, top)
        curveTo(right, top, right, top, right, top + r)
        lineTo(right, bottom - r)
        curveTo(right, bottom, right, bottom, right - r, bottom)
        lineTo(left + r, bottom)
        curveTo(left, bottom, left, bottom, left, bottom - r)
        lineTo(left, top + r)
        curveTo(left, top, left, top, left + r, top)
        close()
    }

    private fun PathBuilder.addLineBar(x1: Float, y1: Float, x2: Float, y2: Float, thickness: Float) {
        val half = thickness / 2f
        moveTo(x1, y1 - half)
        lineTo(x2, y2 - half)
        lineTo(x2, y2 + half)
        lineTo(x1, y1 + half)
        close()
    }
}
