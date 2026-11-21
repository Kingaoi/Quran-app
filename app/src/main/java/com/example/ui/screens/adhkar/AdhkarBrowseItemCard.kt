package com.example.ui.screens.adhkar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.adhkar.DhikrAudioState
import com.example.domain.model.DhikrItem
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.ui.navigation.QuranSharedKey
import com.example.ui.navigation.quranSharedBounds
import com.example.ui.theme.LocalQuranFontFamily
import com.example.ui.theme.quranColors

@Composable
internal fun AdhkarBrowseItemCard(
    item: DhikrItem,
    currentCount: Int,
    isFavorite: Boolean,
    audioState: DhikrAudioState,
    hapticEnabled: Boolean,
    showChapterTag: Boolean,
    onIncrement: () -> Boolean,
    onToggleFavorite: () -> Unit,
    onTogglePlayAudio: () -> Unit,
    onOpenReader: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val haptics = LocalHapticFeedback.current
    val useWestern = LocalUseWesternDigits.current
    val isDone = currentCount >= item.targetCount
    val isPlaying = audioState.activeItemId == item.id && audioState.isPlaying
    val isBuffering = audioState.activeItemId == item.id && audioState.isBuffering

    Surface(
        onClick = onOpenReader,
        shape = RoundedCornerShape(18.dp),
        color = qc.surface2.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, if (isDone) qc.success.copy(alpha = 0.65f) else qc.border),
        modifier = Modifier
            .quranSharedBounds(QuranSharedKey.AdhkarItemCard(item.id))
            .fillMaxWidth()
            .testTag("adhkar_browse_item_${item.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = qc.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (showChapterTag) {
                        Text(
                            text = item.resolvedChapterTitle(),
                            style = MaterialTheme.typography.labelSmall,
                            color = qc.accent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.audioUrl.isNotBlank()) {
                        IconButton(onClick = onTogglePlayAudio, modifier = Modifier.size(48.dp)) {
                            if (isBuffering) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                    contentDescription = if (isPlaying) "إيقاف الصوت" else "تشغيل الذكر",
                                    tint = qc.primary
                                )
                            }
                        }
                    }
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "تبديل المفضلة",
                            tint = if (isFavorite) qc.accent else qc.textMuted
                        )
                    }
                }
            }

            Text(
                text = item.arabicText,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = LocalQuranFontFamily.current),
                color = qc.text
            )

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = item.reference,
                    style = MaterialTheme.typography.labelSmall,
                    color = qc.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = {
                            val doneNow = onIncrement()
                            if (hapticEnabled) {
                                haptics.performHapticFeedback(if (doneNow) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove)
                            }
                        },
                        shape = RoundedCornerShape(50),
                        color = if (isDone) qc.successContainer else qc.primary.copy(alpha = 0.14f),
                        modifier = Modifier.testTag("adhkar_browse_count_${item.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.TouchApp,
                                contentDescription = "عدّ الذكر",
                                tint = if (isDone) qc.success else qc.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${currentCount.toAppDigits(useWestern)}/${item.targetCount.toAppDigits(useWestern)}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isDone) qc.success else qc.primary
                            )
                        }
                    }

                    FilledTonalIconButton(
                        onClick = onOpenReader,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = qc.surface, contentColor = qc.primary),
                        modifier = Modifier.size(48.dp).testTag("adhkar_browse_open_${item.id}")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = "فتح في وضع القراءة", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
