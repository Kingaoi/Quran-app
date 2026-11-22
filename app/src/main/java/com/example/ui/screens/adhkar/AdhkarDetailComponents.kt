package com.example.ui.screens.adhkar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.DhikrItem
import com.example.ui.components.OrnamentalDivider
import com.example.ui.theme.LocalQuranFontFamily
import com.example.ui.theme.QURAN_LINE_HEIGHT_RATIO

@Composable
internal fun AdhkarDetailPageContent(
    item: DhikrItem,
    currentCount: Int,
    fontSizeSp: Float,
    isCurrentPage: Boolean = true,
    isImmersiveCollapsed: Boolean = false,
    hapticEnabled: Boolean = true,
    onIncrement: () -> Boolean,
    onImmersiveChange: (Boolean) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    modifier: Modifier = Modifier,
    cardModifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val isCompleted = currentCount >= item.targetCount
    val latestCollapsed by rememberUpdatedState(isImmersiveCollapsed)
    val latestImmersiveCallback by rememberUpdatedState(onImmersiveChange)

    LaunchedEffect(scrollState, isCurrentPage, isCompleted) {
        if (!isCurrentPage) return@LaunchedEffect
        if (isCompleted) {
            latestImmersiveCallback(false)
            return@LaunchedEffect
        }
        var anchorScroll = scrollState.value
        snapshotFlow { scrollState.value to scrollState.isScrollInProgress }.collect { (current, inProgress) ->
            val collapsedNow = latestCollapsed
            if (current <= 16) {
                if (collapsedNow) latestImmersiveCallback(false)
                anchorScroll = current
                return@collect
            }
            if (!inProgress) {
                anchorScroll = current
                return@collect
            }
            val delta = current - anchorScroll
            if (!collapsedNow && delta > 36 && current > 48) {
                anchorScroll = current
                latestImmersiveCallback(true)
            } else if (collapsedNow && delta < -48) {
                anchorScroll = current
                latestImmersiveCallback(false)
            } else if ((collapsedNow && delta > 0) || (!collapsedNow && delta < 0)) {
                anchorScroll = current
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(enabled = !isCompleted) {
                val justDone = onIncrement()
                if (hapticEnabled) {
                    haptics.performHapticFeedback(if (justDone) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove)
                }
            }
            .verticalScroll(scrollState)
            .padding(contentPadding)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                width = if (isCompleted) 1.5.dp else 1.dp,
                color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = cardModifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    AnimatedVisibility(visible = isCompleted) {
                        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(stringResource(R.string.adhkar_done_label), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }

                OrnamentalDivider()

                Text(
                    text = item.arabicText,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = LocalQuranFontFamily.current,
                        fontSize = fontSizeSp.sp,
                        lineHeight = (fontSizeSp * QURAN_LINE_HEIGHT_RATIO).sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "(${item.reference})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                if (item.virtueOrNote.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            text = item.virtueOrNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}



