package com.example.ui.screens.adhkar

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.domain.model.DhikrCategory
import kotlin.math.cos
import kotlin.math.sin

internal fun DrawScope.drawCategoryWatermark(category: DhikrCategory, tint: Color) {
    val stroke = Stroke(width = 2.1f * density, cap = StrokeCap.Round, join = StrokeJoin.Round)
    when (category) {
        DhikrCategory.MORNING -> drawMorningSun(tint, stroke)
        DhikrCategory.EVENING -> drawCrescentWaves(tint, stroke)
        DhikrCategory.AFTER_PRAYER -> drawPrayingSilhouette(tint, stroke)
        DhikrCategory.SLEEP -> drawMoonAndStars(tint, stroke)
        DhikrCategory.MOSQUE -> drawMosqueDome(tint, stroke)
        DhikrCategory.WAKE_UP -> drawSunriseHorizon(tint, stroke)
        DhikrCategory.QURANIC_DUAS -> drawOpenMushaf(tint, stroke)
        DhikrCategory.PROPHETIC_DUAS -> drawRubElHizbStar(tint, stroke)
        DhikrCategory.RUQYAH -> drawRaisedPalms(tint, stroke)
        DhikrCategory.TRAVEL -> drawTravelPlane(tint, stroke)
        DhikrCategory.TASBIH -> drawTasbihBeads(tint, stroke)
        DhikrCategory.DAILY_LIFE -> drawHomeDaily(tint, stroke)
        DhikrCategory.WORRY_RELIEF -> drawHeartRelief(tint, stroke)
        DhikrCategory.WEATHER_NATURE -> drawRainClouds(tint, stroke)
        DhikrCategory.SOCIAL_ETHICS -> drawSocialLantern(tint, stroke)
        DhikrCategory.HAJJ_WORSHIP -> drawKaabaArch(tint, stroke)
    }
}

private fun DrawScope.drawMorningSun(tint: Color, stroke: Stroke) {
    val cx = size.width * 0.45f
    val cy = size.height * 0.18f
    val r = size.minDimension * 0.28f
    drawCircle(tint, radius = r, center = Offset(cx, cy), style = stroke)
    drawCircle(tint, radius = r * 0.65f, center = Offset(cx, cy), style = stroke)
    for (deg in 15..165 step 30) {
        val rad = Math.toRadians(deg.toDouble())
        val inner = r * 1.28f
        val outer = r * 1.75f
        drawLine(
            color = tint,
            start = Offset(cx + (cos(rad) * inner).toFloat(), cy + (sin(rad) * inner).toFloat()),
            end = Offset(cx + (cos(rad) * outer).toFloat(), cy + (sin(rad) * outer).toFloat()),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawCrescentWaves(tint: Color, stroke: Stroke) {
    val cx = size.width * 0.48f
    val cy = size.height * 0.36f
    val r = size.minDimension * 0.24f
    val moon = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r))
    }
    drawPath(moon, color = tint, style = stroke)
    drawCircle(tint, radius = r * 0.58f, center = Offset(cx + r * 0.22f, cy - r * 0.16f), style = stroke)
    val y1 = size.height * 0.70f
    val y2 = size.height * 0.80f
    val y3 = size.height * 0.90f
    drawLine(tint, Offset(size.width * 0.12f, y1), Offset(size.width * 0.84f, y1), stroke.width, StrokeCap.Round)
    drawLine(tint, Offset(size.width * 0.22f, y2), Offset(size.width * 0.74f, y2), stroke.width, StrokeCap.Round)
    drawLine(tint, Offset(size.width * 0.34f, y3), Offset(size.width * 0.62f, y3), stroke.width, StrokeCap.Round)
}

private fun DrawScope.drawPrayingSilhouette(tint: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    drawCircle(tint, radius = w * 0.11f, center = Offset(w * 0.56f, h * 0.21f), style = stroke)
    val body = Path().apply {
        moveTo(w * 0.48f, h * 0.34f)
        quadraticTo(w * 0.36f, h * 0.50f, w * 0.40f, h * 0.94f)
        moveTo(w * 0.66f, h * 0.35f)
        lineTo(w * 0.72f, h * 0.54f)
        lineTo(w * 0.58f, h * 0.62f)
        lineTo(w * 0.64f, h * 0.94f)
    }
    drawPath(body, color = tint, style = stroke)
}

private fun DrawScope.drawMoonAndStars(tint: Color, stroke: Stroke) {
    val cx = size.width * 0.46f
    val cy = size.height * 0.52f
    val r = size.minDimension * 0.32f
    val path = Path().apply {
        moveTo(cx + r * 0.35f, cy - r * 0.9f)
        cubicTo(cx - r * 0.95f, cy - r * 0.6f, cx - r * 0.95f, cy + r * 0.7f, cx + r * 0.42f, cy + r * 0.85f)
        cubicTo(cx - r * 0.25f, cy + r * 0.55f, cx - r * 0.22f, cy - r * 0.48f, cx + r * 0.35f, cy - r * 0.9f)
    }
    drawPath(path, color = tint, style = stroke)
    drawStarDot(Offset(cx + r * 0.55f, cy - r * 0.45f), r * 0.11f, tint, stroke)
    drawStarDot(Offset(cx + r * 0.82f, cy + r * 0.08f), r * 0.08f, tint, stroke)
}

private fun DrawScope.drawStarDot(center: Offset, radius: Float, tint: Color, stroke: Stroke) {
    drawCircle(color = tint, radius = radius, center = center, style = stroke)
}

private fun DrawScope.drawMosqueDome(tint: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    drawRect(tint, topLeft = Offset(w * 0.14f, h * 0.38f), size = Size(w * 0.12f, h * 0.56f), style = stroke)
    val minaretRoof = Path().apply {
        moveTo(w * 0.11f, h * 0.38f)
        lineTo(w * 0.20f, h * 0.20f)
        lineTo(w * 0.29f, h * 0.38f)
        close()
    }
    drawPath(minaretRoof, color = tint, style = stroke)
    val dome = Path().apply {
        moveTo(w * 0.32f, h * 0.62f)
        cubicTo(w * 0.32f, h * 0.35f, w * 0.54f, h * 0.30f, w * 0.58f, h * 0.22f)
        cubicTo(w * 0.62f, h * 0.30f, w * 0.84f, h * 0.35f, w * 0.84f, h * 0.62f)
        close()
    }
    drawPath(dome, color = tint, style = stroke)
    drawRect(tint, topLeft = Offset(w * 0.30f, h * 0.62f), size = Size(w * 0.56f, h * 0.32f), style = stroke)
    drawArc(
        color = tint,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(w * 0.48f, h * 0.72f),
        size = Size(w * 0.20f, h * 0.36f),
        style = stroke
    )
}

private fun DrawScope.drawSunriseHorizon(tint: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    val cx = w * 0.48f
    val cy = h * 0.62f
    val r = size.minDimension * 0.24f
    drawArc(tint, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(cx - r, cy - r), size = Size(r * 2, r * 2), style = stroke)
    for (deg in 200..340 step 28) {
        val rad = Math.toRadians(deg.toDouble())
        drawLine(
            tint,
            Offset(cx + (cos(rad) * r * 1.28f).toFloat(), cy + (sin(rad) * r * 1.28f).toFloat()),
            Offset(cx + (cos(rad) * r * 1.72f).toFloat(), cy + (sin(rad) * r * 1.72f).toFloat()),
            stroke.width,
            StrokeCap.Round
        )
    }
    drawLine(tint, Offset(w * 0.10f, cy), Offset(w * 0.86f, cy), stroke.width, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.22f, h * 0.75f), Offset(w * 0.74f, h * 0.75f), stroke.width, StrokeCap.Round)
    drawLine(tint, Offset(w * 0.34f, h * 0.87f), Offset(w * 0.62f, h * 0.87f), stroke.width, StrokeCap.Round)
}

private fun DrawScope.drawOpenMushaf(tint: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    val pages = Path().apply {
        moveTo(w * 0.50f, h * 0.30f)
        lineTo(w * 0.18f, h * 0.22f)
        lineTo(w * 0.12f, h * 0.58f)
        lineTo(w * 0.50f, h * 0.66f)
        lineTo(w * 0.88f, h * 0.58f)
        lineTo(w * 0.82f, h * 0.22f)
        close()
        moveTo(w * 0.50f, h * 0.30f)
        lineTo(w * 0.50f, h * 0.66f)
        moveTo(w * 0.30f, h * 0.62f)
        lineTo(w * 0.70f, h * 0.88f)
        moveTo(w * 0.70f, h * 0.62f)
        lineTo(w * 0.30f, h * 0.88f)
    }
    drawPath(pages, color = tint, style = stroke)
}

private fun DrawScope.drawRubElHizbStar(tint: Color, stroke: Stroke) {
    val s = size.minDimension * 0.52f
    val topLeft = Offset((size.width - s) / 2f, (size.height - s) / 2f)
    drawRoundRect(tint, topLeft = topLeft, size = Size(s, s), cornerRadius = CornerRadius(6f), style = stroke)
    rotate(45f, pivot = center) {
        drawRoundRect(tint, topLeft = topLeft, size = Size(s, s), cornerRadius = CornerRadius(6f), style = stroke)
    }
    drawCircle(tint, radius = s * 0.22f, center = center, style = stroke)
}

private fun DrawScope.drawRaisedPalms(tint: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    val leftPalm = Path().apply {
        moveTo(w * 0.22f, h * 0.88f)
        lineTo(w * 0.22f, h * 0.32f)
        quadraticTo(w * 0.28f, h * 0.24f, w * 0.34f, h * 0.32f)
        lineTo(w * 0.34f, h * 0.56f)
        lineTo(w * 0.42f, h * 0.56f)
        lineTo(w * 0.42f, h * 0.30f)
        quadraticTo(w * 0.46f, h * 0.26f, w * 0.47f, h * 0.36f)
        lineTo(w * 0.47f, h * 0.88f)
    }
    val rightPalm = Path().apply {
        moveTo(w * 0.53f, h * 0.88f)
        lineTo(w * 0.53f, h * 0.36f)
        quadraticTo(w * 0.54f, h * 0.26f, w * 0.58f, h * 0.30f)
        lineTo(w * 0.58f, h * 0.56f)
        lineTo(w * 0.66f, h * 0.56f)
        lineTo(w * 0.66f, h * 0.32f)
        quadraticTo(w * 0.72f, h * 0.24f, w * 0.78f, h * 0.32f)
        lineTo(w * 0.78f, h * 0.88f)
    }
    drawPath(leftPalm, color = tint, style = stroke)
    drawPath(rightPalm, color = tint, style = stroke)
}

private fun DrawScope.drawTravelPlane(tint: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    val plane = Path().apply {
        moveTo(w * 0.82f, h * 0.22f)
        lineTo(w * 0.58f, h * 0.44f)
        lineTo(w * 0.72f, h * 0.76f)
        lineTo(w * 0.58f, h * 0.72f)
        lineTo(w * 0.44f, h * 0.56f)
        lineTo(w * 0.24f, h * 0.72f)
        lineTo(w * 0.24f, h * 0.84f)
        lineTo(w * 0.14f, h * 0.70f)
        lineTo(w * 0.18f, h * 0.60f)
        lineTo(w * 0.34f, h * 0.46f)
        lineTo(w * 0.18f, h * 0.34f)
        lineTo(w * 0.32f, h * 0.28f)
        lineTo(w * 0.50f, h * 0.36f)
        lineTo(w * 0.74f, h * 0.18f)
        close()
    }
    drawPath(plane, color = tint, style = stroke)
}

private fun DrawScope.drawTasbihBeads(tint: Color, stroke: Stroke) {
    val r = size.minDimension * 0.34f
    drawCircle(tint, radius = r, center = center, style = stroke)
    for (i in 0 until 8) {
        val rad = Math.toRadians((i * 45).toDouble())
        drawCircle(
            color = tint,
            radius = 3.5f * density,
            center = Offset(center.x + (cos(rad) * r).toFloat(), center.y + (sin(rad) * r).toFloat()),
            style = stroke
        )
    }
}
