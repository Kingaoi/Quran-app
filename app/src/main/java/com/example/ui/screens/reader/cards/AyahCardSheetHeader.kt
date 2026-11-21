package com.example.ui.screens.reader.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.AudioPlaybackState
import com.example.domain.model.AyahItem
import com.example.ui.theme.LocalQuranFontFamily
import com.example.ui.theme.quranColors

@Composable
internal fun AyahCardSheetHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    ayah: AyahItem? = null,
    showAyahQuote: Boolean = true,
    playbackState: AudioPlaybackState? = null,
    onPlayAyah: ((Int, Int) -> Unit)? = null,
    onTogglePlayPause: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val isThisAyahActive = ayah != null && playbackState != null &&
        playbackState.isActive &&
        playbackState.currentSurah == ayah.surahNumber &&
        playbackState.currentAyah == ayah.ayahNumber
    val isPlaying = isThisAyahActive && playbackState?.isPlaying == true
    val isBuffering = isThisAyahActive && playbackState?.isBuffering == true

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(qc.surface2)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = qc.primary,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = qc.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = qc.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (ayah != null && onPlayAyah != null) {
                    val container = when {
                        isPlaying -> qc.selected
                        isThisAyahActive -> qc.surface2
                        else -> Color.Transparent
                    }
                    val tint = if (isPlaying) qc.onSelected else qc.primary
                    IconButton(
                        onClick = {
                            if (isThisAyahActive && onTogglePlayPause != null) {
                                onTogglePlayPause()
                            } else {
                                onPlayAyah(ayah.surahNumber, ayah.ayahNumber)
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = container,
                            contentColor = tint
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("card_header_play_${ayah.surahNumber}_${ayah.ayahNumber}")
                    ) {
                        when {
                            isBuffering -> CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = tint
                            )
                            isPlaying -> Icon(Icons.Rounded.Pause, contentDescription = "إيقاف التلاوة مؤقتاً")
                            isThisAyahActive -> Icon(Icons.Rounded.PlayArrow, contentDescription = "استكمال التلاوة")
                            else -> Icon(Icons.Rounded.PlayArrow, contentDescription = "تشغيل الآية")
                        }
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "إغلاق البطاقة",
                        tint = qc.textMuted
                    )
                }
            }
        }

        if (showAyahQuote && ayah != null) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(ayah.text)
                        append(' ')
                        withStyle(SpanStyle(color = accentColor)) { append(ayah.marker) }
                    },
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = LocalQuranFontFamily.current,
                        fontSize = 21.sp,
                        lineHeight = 37.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }
        }
    }
}
