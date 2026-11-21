package com.example.ui.screens.adhkar

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

internal fun DrawScope.drawHomeDaily(tint: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    val roof = Path().apply {
        moveTo(w * 0.14f, h * 0.48f)
        lineTo(w * 0.50f, h * 0.16f)
        lineTo(w * 0.86f, h * 0.48f)
    }
    drawPath(roof, color = tint, style = stroke)
    drawRect(tint, topLeft = Offset(w * 0.24f, h * 0.48f), size = Size(w * 0.52f, h * 0.40f), style = stroke)
    drawRoundRect(tint, topLeft = Offset(w * 0.42f, h * 0.62f), size = Size(w * 0.16f, h * 0.26f), cornerRadius = CornerRadius(8f), style = stroke)
}

internal fun DrawScope.drawHeartRelief(tint: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    val heart = Path().apply {
        moveTo(w * 0.50f, h * 0.82f)
        cubicTo(w * 0.16f, h * 0.60f, w * 0.16f, h * 0.24f, w * 0.50f, h * 0.36f)
        cubicTo(w * 0.84f, h * 0.24f, w * 0.84f, h * 0.60f, w * 0.50f, h * 0.82f)
    }
    drawPath(heart, color = tint, style = stroke)
    drawCircle(tint, radius = size.minDimension * 0.12f, center = Offset(w * 0.50f, h * 0.52f), style = stroke)
}

internal fun DrawScope.drawRainClouds(tint: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    drawArc(tint, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(w * 0.22f, h * 0.24f), size = Size(w * 0.40f, h * 0.32f), style = stroke)
    drawArc(tint, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(w * 0.48f, h * 0.30f), size = Size(w * 0.32f, h * 0.26f), style = stroke)
    drawLine(tint, Offset(w * 0.20f, h * 0.56f), Offset(w * 0.80f, h * 0.56f), stroke.width, StrokeCap.Round)
    listOf(0.32f, 0.50f, 0.68f).forEach { x ->
        drawLine(tint, Offset(w * x, h * 0.66f), Offset(w * (x - 0.05f), h * 0.84f), stroke.width, StrokeCap.Round)
    }
}

internal fun DrawScope.drawSocialLantern(tint: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    drawLine(tint, Offset(w * 0.50f, h * 0.10f), Offset(w * 0.50f, h * 0.24f), stroke.width, StrokeCap.Round)
    val lantern = Path().apply {
        moveTo(w * 0.36f, h * 0.24f)
        lineTo(w * 0.64f, h * 0.24f)
        lineTo(w * 0.72f, h * 0.68f)
        lineTo(w * 0.50f, h * 0.86f)
        lineTo(w * 0.28f, h * 0.68f)
        close()
    }
    drawPath(lantern, color = tint, style = stroke)
    drawCircle(tint, radius = size.minDimension * 0.10f, center = Offset(w * 0.50f, h * 0.52f), style = stroke)
}

internal fun DrawScope.drawKaabaArch(tint: Color, stroke: Stroke) {
    val w = size.width
    val h = size.height
    drawRoundRect(tint, topLeft = Offset(w * 0.26f, h * 0.24f), size = Size(w * 0.48f, h * 0.62f), cornerRadius = CornerRadius(6f), style = stroke)
    drawLine(tint, Offset(w * 0.26f, h * 0.38f), Offset(w * 0.74f, h * 0.38f), stroke.width * 1.3f)
    drawRoundRect(tint, topLeft = Offset(w * 0.44f, h * 0.58f), size = Size(w * 0.12f, h * 0.28f), cornerRadius = CornerRadius(4f), style = stroke)
}
