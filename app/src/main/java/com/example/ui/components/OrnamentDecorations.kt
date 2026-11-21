package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.toArabicIndicDigits
import com.example.ui.theme.LocalQuranFontFamily

/**
 * Eight-pointed Rub el Hizb geometric badge displaying a Surah or Ayah number in Arabic-Indic digits.
 */
@Composable
fun RubElHizbBadge(
    number: Int,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    strokeColor: Color = MaterialTheme.colorScheme.secondary,
    fillColor: Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    RubElHizbBadge(
        label = number.toArabicIndicDigits(),
        modifier = modifier,
        size = size,
        strokeColor = strokeColor,
        fillColor = fillColor,
        textColor = textColor
    )
}

@Composable
fun RubElHizbBadge(
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    strokeColor: Color = MaterialTheme.colorScheme.secondary,
    fillColor: Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithCache {
                    val side = this.size.minDimension * 0.64f
                    val left = (this.size.width - side) / 2f
                    val top = (this.size.height - side) / 2f
                    val rectPath = Path().apply {
                        moveTo(left, top)
                        lineTo(left + side, top)
                        lineTo(left + side, top + side)
                        lineTo(left, top + side)
                        close()
                    }
                    val stroke = Stroke(width = 1.6.dp.toPx())
                    val pivot = Offset(this.size.width / 2f, this.size.height / 2f)
                    onDrawBehind {
                        drawPath(rectPath, color = fillColor)
                        drawPath(rectPath, color = strokeColor, style = stroke)
                        rotate(degrees = 45f, pivot = pivot) {
                            drawPath(rectPath, color = fillColor)
                            drawPath(rectPath, color = strokeColor, style = stroke)
                        }
                    }
                }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (label.length >= 3) 9.sp else if (size >= 40.dp) 12.sp else 10.sp
            ),
            color = textColor,
            maxLines = 1
        )
    }
}

/**
 * Ornamental Surah title banner modeled after Madani Mushaf illuminated chapter headers.
 */
@Composable
fun SurahHeaderOrnament(
    surahNameAr: String,
    surahNumber: Int,
    ayahCount: Int,
    revelationLabel: String,
    showBasmalah: Boolean,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val containerStart = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
    val containerEnd = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(containerStart, containerEnd, containerStart)
                    )
                )
                .border(
                    width = 1.2.dp,
                    color = secondary.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = revelationLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = primary
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "سُورَةُ $surahNameAr",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = LocalQuranFontFamily.current,
                            fontSize = 27.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = "${ayahCount.toArabicIndicDigits()} آية",
                    style = MaterialTheme.typography.labelMedium,
                    color = primary
                )
            }
        }

        if (showBasmalah) {
            Text(
                text = "بِسۡمِ ٱللَّهِ ٱلرَّحۡمَٰنِ ٱلرَّحِيمِ",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = LocalQuranFontFamily.current,
                    fontSize = 25.sp,
                    lineHeight = 42.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp)
            )
        }
    }
}

/**
 * Subtle Islamic geometric divider line between sections.
 */
@Composable
fun OrnamentalDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
            .drawWithCache {
                val midY = size.height / 2f
                val midX = size.width / 2f
                val r = 5.dp.toPx()
                val gap = 16.dp.toPx()
                val lineStroke = 1.dp.toPx()
                val diamondStroke = Stroke(width = 1.3.dp.toPx())
                val diamond = Path().apply {
                    moveTo(midX, midY - r)
                    lineTo(midX + r, midY)
                    lineTo(midX, midY + r)
                    lineTo(midX - r, midY)
                    close()
                }
                onDrawBehind {
                    drawLine(
                        color = color,
                        start = Offset(0f, midY),
                        end = Offset(midX - gap, midY),
                        strokeWidth = lineStroke
                    )
                    drawLine(
                        color = color,
                        start = Offset(midX + gap, midY),
                        end = Offset(size.width, midY),
                        strokeWidth = lineStroke
                    )
                    drawPath(diamond, color = color, style = diamondStroke)
                }
            }
    )
}
