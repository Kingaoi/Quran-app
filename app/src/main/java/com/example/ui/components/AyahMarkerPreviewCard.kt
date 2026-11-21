package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalQuranFontFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun MushafOrnamentPreviewCard(
    previewBg: Color,
    defaultInk: Color,
    ornamentColor: Color,
    numberColor: Color,
    bannerColor: Color,
    showSurahBanner: Boolean
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = previewBg,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(visible = showSurahBanner) {
                SurahBannerMiniPreview(
                    bannerColor = bannerColor,
                    titleInk = defaultInk
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = LocalQuranFontFamily.current,
                        fontSize = 21.sp
                    ),
                    color = defaultInk
                )
                Spacer(modifier = Modifier.width(8.dp))
                AyahRosettePreview(
                    ornamentColor = ornamentColor,
                    numberColor = numberColor,
                    digitText = "١"
                )
            }
        }
    }
}

@Composable
private fun SurahBannerMiniPreview(
    bannerColor: Color,
    titleInk: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val pad = 2.dp.toPx()
            val innerW = w - pad * 2f
            val innerH = h - pad * 2f
            val slotW = innerW * 0.48f
            val slotLeft = (w - slotW) / 2f

            drawRoundRect(
                color = bannerColor.copy(alpha = 0.11f),
                topLeft = Offset(slotLeft, pad + 3.dp.toPx()),
                size = Size(slotW, innerH - 6.dp.toPx()),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )
            drawRoundRect(
                color = bannerColor,
                topLeft = Offset(pad, pad),
                size = Size(innerW, innerH),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = 1.4.dp.toPx())
            )
            drawRoundRect(
                color = bannerColor.copy(alpha = 0.7f),
                topLeft = Offset(pad + 3.dp.toPx(), pad + 3.dp.toPx()),
                size = Size(innerW - 6.dp.toPx(), innerH - 6.dp.toPx()),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                style = Stroke(width = 0.8.dp.toPx())
            )

            // Left & right ornamental wing brackets flanking the Surah name cartouche
            val midY = h / 2f
            listOf(slotLeft - 6.dp.toPx() to -1f, slotLeft + slotW + 6.dp.toPx() to 1f).forEach { (x, dir) ->
                val cusp = Path().apply {
                    moveTo(x, midY - 8.dp.toPx())
                    quadraticTo(x + dir * 9.dp.toPx(), midY, x, midY + 8.dp.toPx())
                }
                drawPath(cusp, color = bannerColor, style = Stroke(width = 1.2.dp.toPx()))
                drawCircle(color = bannerColor, radius = 2.dp.toPx(), center = Offset(x + dir * 10.dp.toPx(), midY))
            }
        }
        Text(
            text = "سُورَةُ الفَاتِحَةِ",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = LocalQuranFontFamily.current,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            color = titleInk
        )
    }
}

@Composable
private fun AyahRosettePreview(
    ornamentColor: Color,
    numberColor: Color,
    digitText: String
) {
    Box(
        modifier = Modifier.size(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val outerR = size.minDimension * 0.43f
            val innerR = size.minDimension * 0.34f
            drawCircle(
                color = ornamentColor.copy(alpha = 0.10f),
                radius = innerR,
                center = c
            )
            drawCircle(
                color = ornamentColor,
                radius = outerR,
                center = c,
                style = Stroke(width = 1.6.dp.toPx())
            )
            drawCircle(
                color = ornamentColor,
                radius = innerR,
                center = c,
                style = Stroke(width = 1.1.dp.toPx())
            )
            val lobes = 8
            for (i in 0 until lobes) {
                val angle = (i * 2.0 * Math.PI) / lobes
                val pt = Offset(
                    x = c.x + (outerR * cos(angle)).toFloat(),
                    y = c.y + (outerR * sin(angle)).toFloat()
                )
                drawCircle(
                    color = ornamentColor,
                    radius = 1.6.dp.toPx(),
                    center = pt
                )
            }
        }
        Text(
            text = digitText,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            color = numberColor
        )
    }
}
