package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.audio.AudioPlaylistTrack
import com.example.data.audio.PlaylistIconKey
import com.example.domain.model.SurahInfo
import com.example.ui.theme.quranColors

fun PlaylistIconKey.toImageVector(): ImageVector = when (this) {
    PlaylistIconKey.BEDTIME -> Icons.Rounded.Bedtime
    PlaylistIconKey.SHIELD -> Icons.Rounded.Shield
    PlaylistIconKey.MOSQUE -> Icons.Rounded.Mosque
    PlaylistIconKey.BOOK -> Icons.Rounded.AutoStories
    PlaylistIconKey.STAR -> Icons.Rounded.Star
    PlaylistIconKey.QUEUE -> Icons.AutoMirrored.Rounded.QueueMusic
}

@Composable
fun PlaylistMedallionIcon(
    iconKey: PlaylistIconKey,
    accentIndex: Int,
    size: Dp = 46.dp,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.quranColors.categoryPastelAt(accentIndex)
    Surface(
        shape = RoundedCornerShape(size * 0.30f),
        color = Color.Transparent,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.42f)),
        modifier = modifier.size(size)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.background(
                Brush.linearGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.24f),
                        accent.copy(alpha = 0.10f)
                    )
                )
            )
        ) {
            Icon(
                imageVector = iconKey.toImageVector(),
                contentDescription = iconKey.labelAr,
                tint = accent,
                modifier = Modifier.size(size * 0.50f)
            )
        }
    }
}

fun formatPlaylistTracksPreview(
    tracks: List<AudioPlaylistTrack>,
    surahsByNumber: Map<Int, SurahInfo>
): String {
    if (tracks.isEmpty()) return "قائمة فارغة • أضف سوراً أو آيات"
    val previewNames = tracks.take(3).map { track ->
        track.labelAr?.takeIf { it.isNotBlank() }
            ?: surahsByNumber[track.surahNumber]?.nameAr
            ?: "سورة ${track.surahNumber}"
    }
    val base = previewNames.joinToString(" • ")
    return if (tracks.size > 3) "$base..." else base
}
