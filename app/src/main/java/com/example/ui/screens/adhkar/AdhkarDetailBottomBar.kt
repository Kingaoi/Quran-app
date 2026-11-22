package com.example.ui.screens.adhkar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.adhkar.DhikrAudioState
import com.example.domain.model.DhikrItem
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranColors

@Composable
internal fun AdhkarDetailBottomBar(
    item: DhikrItem,
    currentCount: Int,
    hasPrevious: Boolean,
    isLastPage: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    isFavorite: Boolean = false,
    audioState: DhikrAudioState = DhikrAudioState(),
    hapticEnabled: Boolean = true,
    onToggleFavorite: () -> Unit = {},
    onTogglePlayAudio: () -> Unit = {},
    onIncrement: () -> Boolean,
    onReset: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onShareImage: () -> Unit,
    onPrevious: () -> Unit,
    onNextOrFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val haptics = LocalHapticFeedback.current
    val useWestern = LocalUseWesternDigits.current

    val remaining = (item.targetCount - currentCount).coerceAtLeast(0)
    val isCompleted = remaining == 0
    val isPlayingThis = audioState.activeItemId == item.id && audioState.isPlaying
    val isBufferingThis = audioState.activeItemId == item.id && audioState.isBuffering
    val cardShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
    val edgeBorderColor = qc.border.copy(alpha = 0.9f)

    val onTapRemaining = {
        val justDone = onIncrement()
        if (hapticEnabled) {
            haptics.performHapticFeedback(
                if (justDone) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
            )
        }
    }

    val compactRemainingLabel = when {
        isCompleted -> stringResource(R.string.adhkar_done_label)
        item.targetCount == 1 -> stringResource(R.string.adhkar_once_label)
        else -> stringResource(R.string.adhkar_remaining_format, remaining.toAppDigits(useWestern))
    }

    Surface(
        shape = cardShape,
        color = qc.surface,
        shadowElevation = 12.dp,
        tonalElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(qc.surface, qc.surface2)))
                .drawWithContent {
                    drawContent()
                    val cornerPx = 28.dp.toPx()
                    val strokePx = 1.dp.toPx()
                    val half = strokePx / 2f
                    val path = Path().apply {
                        moveTo(half, cornerPx)
                        arcTo(
                            rect = Rect(half, half, cornerPx * 2f - half, cornerPx * 2f - half),
                            startAngleDegrees = 180f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        lineTo(size.width - cornerPx, half)
                        arcTo(
                            rect = Rect(size.width - cornerPx * 2f + half, half, size.width - half, cornerPx * 2f - half),
                            startAngleDegrees = 270f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                    }
                    drawPath(path = path, color = edgeBorderColor, style = Stroke(width = strokePx))
                }
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val bottomToggleDesc = stringResource(
                if (expanded) R.string.adhkar_bottom_bar_collapse_cd else R.string.adhkar_bottom_bar_expand_cd
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .pointerInput(expanded) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount > 12f && expanded) onExpandedChange(false)
                            else if (dragAmount < -12f && !expanded) onExpandedChange(true)
                        }
                    }
                    .clickable { onExpandedChange(!expanded) }
                    .semantics { contentDescription = bottomToggleDesc }
                    .padding(vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(qc.border)
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                        contentDescription = null,
                        tint = qc.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (item.audioUrl.isNotBlank()) {
                        Surface(
                            onClick = onTogglePlayAudio,
                            shape = CircleShape,
                            color = if (isPlayingThis) qc.primary.copy(alpha = 0.14f) else qc.surface2,
                            border = BorderStroke(1.dp, if (isPlayingThis) qc.primary.copy(alpha = 0.55f) else qc.border),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(38.dp)
                                .testTag("adhkar_detail_audio_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isBufferingThis) {
                                    CircularProgressIndicator(modifier = Modifier.size(17.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(
                                        imageVector = if (isPlayingThis) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                        contentDescription = stringResource(
                                            if (isPlayingThis) R.string.adhkar_audio_stop_cd else R.string.adhkar_audio_play_cd
                                        ),
                                        tint = qc.primary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        onClick = onToggleFavorite,
                        shape = CircleShape,
                        color = if (isFavorite) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f) else qc.surface2,
                        border = BorderStroke(
                            1.dp,
                            if (isFavorite) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f) else qc.border
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(38.dp)
                            .testTag("adhkar_detail_favorite_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = stringResource(
                                    if (isFavorite) R.string.adhkar_favorite_remove_cd else R.string.adhkar_favorite_add_cd
                                ),
                                tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else qc.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.targetCount > 1) {
                            Text(
                                text = "${currentCount.toAppDigits(useWestern)} / ${item.targetCount.toAppDigits(useWestern)}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isCompleted) qc.success else qc.textMuted,
                                maxLines = 1
                            )
                        }
                    }

                    AdhkarBottomUtilityActions(
                        showReset = currentCount > 0,
                        onReset = onReset,
                        onCopy = onCopy,
                        onShare = onShare,
                        onShareImage = onShareImage
                    )
                }
            }

            AdhkarChoreographedActionRow(
                itemId = item.id,
                compactRemainingLabel = compactRemainingLabel,
                isCompleted = isCompleted,
                hasPrevious = hasPrevious,
                isLastPage = isLastPage,
                compactPrevButton = !expanded,
                onTapRemaining = onTapRemaining,
                onPrevious = onPrevious,
                onNextOrFinish = onNextOrFinish
            )
        }
    }
}

