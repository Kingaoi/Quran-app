package com.example.ui.screens.adhkar

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.quranColors
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
internal fun AdhkarRemainingCountPill(
    remainingLabel: String,
    isCompleted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isCompleted) {
            qc.successContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
        },
        border = BorderStroke(
            1.dp,
            if (isCompleted) qc.success.copy(alpha = 0.55f) else qc.border
        ),
        modifier = modifier
            .width(120.dp)
            .height(48.dp)
            .testTag("adhkar_detail_count_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.TouchApp,
                contentDescription = null,
                tint = if (isCompleted) qc.success else MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = remainingLabel,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isCompleted) qc.success else MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun AdhkarBottomUtilityActions(
    showReset: Boolean,
    onReset: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onShareImage: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    var showShareMenu by remember { mutableStateOf(false) }

    if (showReset) {
        Surface(
            onClick = onReset,
            shape = CircleShape,
            color = qc.surface2,
            border = BorderStroke(1.dp, qc.border),
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(38.dp)
                .testTag("adhkar_detail_item_reset_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.adhkar_reset_item_cd),
                    tint = qc.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    AdhkarUtilityChip(
        label = stringResource(R.string.adhkar_copy_label),
        icon = Icons.Rounded.ContentCopy,
        contentDescription = stringResource(R.string.adhkar_copy_cd),
        onClick = onCopy,
        modifier = Modifier.testTag("adhkar_detail_copy_button")
    )

    Box {
        AdhkarUtilityChip(
            label = stringResource(R.string.adhkar_share_label),
            icon = Icons.Rounded.Share,
            contentDescription = stringResource(R.string.adhkar_share_cd),
            onClick = { showShareMenu = true },
            modifier = Modifier.testTag("adhkar_detail_share_button")
        )
        DropdownMenu(expanded = showShareMenu, onDismissRequest = { showShareMenu = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.adhkar_share_as_image)) }, onClick = { showShareMenu = false; onShareImage() })
            DropdownMenuItem(text = { Text(stringResource(R.string.adhkar_share_as_text)) }, onClick = { showShareMenu = false; onShare() })
        }
    }
}

@Composable
private fun AdhkarUtilityChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = qc.surface2,
        border = BorderStroke(1.dp, qc.border),
        modifier = modifier.minimumInteractiveComponentSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription, tint = qc.primary, modifier = Modifier.size(16.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = qc.text, maxLines = 1)
        }
    }
}

@Composable
internal fun AdhkarChoreographedActionRow(
    itemId: String,
    compactRemainingLabel: String,
    isCompleted: Boolean,
    hasPrevious: Boolean,
    isLastPage: Boolean,
    compactPrevButton: Boolean = false,
    onTapRemaining: () -> Unit,
    onPrevious: () -> Unit,
    onNextOrFinish: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    var collapsedAfterDone by remember(itemId) { mutableStateOf(isCompleted) }

    LaunchedEffect(itemId, isCompleted) {
        if (isCompleted) {
            if (!collapsedAfterDone) {
                delay(320L)
                collapsedAfterDone = true
            }
        } else {
            collapsedAfterDone = false
        }
    }

    val visibleFraction by animateFloatAsState(
        targetValue = if (collapsedAfterDone) 0f else 1f,
        animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
        label = "adhkar_buttons_choreography"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (visibleFraction > 0.005f) {
            Box(
                modifier = Modifier
                    .clipToBounds()
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints.copy(minWidth = 0))
                        val visibleWidth = (placeable.width * visibleFraction).roundToInt().coerceAtLeast(0)
                        layout(visibleWidth, placeable.height) {
                            if (visibleWidth > 0) {
                                placeable.placeRelative(0, 0)
                            }
                        }
                    }
                    .graphicsLayer { alpha = (visibleFraction * 1.25f).coerceIn(0f, 1f) }
            ) {
                AdhkarRemainingCountPill(
                    remainingLabel = if (isCompleted) stringResource(R.string.adhkar_done_label) else compactRemainingLabel,
                    isCompleted = isCompleted,
                    onClick = onTapRemaining
                )
            }
            Spacer(modifier = Modifier.width((8f * visibleFraction).dp))
        }

        Button(
            onClick = onNextOrFinish,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = qc.primary,
                contentColor = qc.onPrimary
            ),
            contentPadding = PaddingValues(horizontal = 14.dp),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("adhkar_detail_next_button")
        ) {
            Text(
                text = stringResource(if (isLastPage) R.string.adhkar_finish_section else R.string.adhkar_next_label),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
            if (!isLastPage) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (hasPrevious && visibleFraction > 0.005f) {
            Spacer(modifier = Modifier.width((8f * visibleFraction).dp))
            Box(
                modifier = Modifier
                    .clipToBounds()
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints.copy(minWidth = 0))
                        val visibleWidth = (placeable.width * visibleFraction).roundToInt().coerceAtLeast(0)
                        layout(visibleWidth, placeable.height) {
                            if (visibleWidth > 0) {
                                placeable.place(0, 0)
                            }
                        }
                    }
                    .graphicsLayer { alpha = (visibleFraction * 1.25f).coerceIn(0f, 1f) }
            ) {
                OutlinedButton(
                    onClick = onPrevious,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, qc.border),
                    contentPadding = PaddingValues(horizontal = if (compactPrevButton) 10.dp else 12.dp),
                    modifier = Modifier
                        .width(if (compactPrevButton) 52.dp else 94.dp)
                        .height(48.dp)
                        .testTag("adhkar_detail_prev_button")
                ) {
                    val prevLabel = stringResource(R.string.adhkar_prev_label)
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = if (compactPrevButton) prevLabel else null,
                        modifier = Modifier.size(18.dp)
                    )
                    if (!compactPrevButton) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = prevLabel,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
