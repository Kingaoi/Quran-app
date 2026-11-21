package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.domain.model.ReciterOption

private val PORTRAIT_TOP_CENTER = BiasAlignment(0f, -0.45f)
private val PORTRAIT_CONTRAST_FILTER = ColorFilter.colorMatrix(
    ColorMatrix().apply { setToSaturation(1.06f) }
)

@Composable
internal fun ReciterPortraitAvatar(
    reciter: ReciterOption,
    isSelected: Boolean,
    size: Dp = 54.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var useFallback by remember(reciter.id) { mutableStateOf(false) }
    val activeUrl = remember(reciter.photoUrl, reciter.fallbackPhotoUrl, useFallback) {
        if (useFallback && reciter.fallbackPhotoUrl.isNotBlank()) reciter.fallbackPhotoUrl else reciter.photoUrl
    }
    val initialGlyph = remember(reciter.nameAr) {
        reciter.nameAr.trim().firstOrNull()?.toString() ?: "ق"
    }
    val request = remember(activeUrl, context) {
        ImageRequest.Builder(context)
            .data(activeUrl.ifBlank { null })
            .addHeader("User-Agent", "AlQuranMushafApp/1.0 (Android)")
            .addHeader("Accept", "image/*")
            .allowHardware(true)
            .crossfade(true)
            .build()
    }
    val haloBrush = Brush.linearGradient(
        colors = if (isSelected) {
            listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.primary
            )
        } else {
            listOf(
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f),
                MaterialTheme.colorScheme.outlineVariant
            )
        }
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(width = if (isSelected) 2.2.dp else 1.4.dp, brush = haloBrush, shape = CircleShape)
            .padding(2.5.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!reciter.preferMonogramAvatar && activeUrl.isNotBlank()) {
            SubcomposeAsyncImage(
                model = request,
                contentDescription = reciter.nameAr,
                contentScale = ContentScale.Crop,
                alignment = PORTRAIT_TOP_CENTER,
                colorFilter = PORTRAIT_CONTRAST_FILTER,
                modifier = Modifier.fillMaxSize(),
                loading = { ReciterMonogramFallback(initialGlyph = initialGlyph, isSelected = isSelected) },
                error = {
                    if (!useFallback && reciter.fallbackPhotoUrl.isNotBlank()) useFallback = true
                    ReciterMonogramFallback(initialGlyph = initialGlyph, isSelected = isSelected)
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.62f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.16f)
                        )
                    )
            )
        } else {
            ReciterMonogramFallback(initialGlyph = initialGlyph, isSelected = isSelected)
        }
    }
}

@Composable
private fun ReciterMonogramFallback(
    initialGlyph: String,
    isSelected: Boolean
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = if (isSelected) {
                        listOf(primary.copy(alpha = 0.28f), secondary.copy(alpha = 0.16f))
                    } else {
                        listOf(secondary.copy(alpha = 0.24f), primary.copy(alpha = 0.08f))
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initialGlyph,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) primary else MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
