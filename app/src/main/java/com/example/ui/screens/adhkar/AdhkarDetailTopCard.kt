package com.example.ui.screens.adhkar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.TextDecrease
import androidx.compose.material.icons.rounded.TextIncrease
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranColors

@Composable
internal fun AdhkarDetailTopCard(
    title: String,
    currentIndex: Int,
    totalCount: Int,
    fontSizeSp: Float,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onAdjustFontSize: (Float) -> Unit,
    onResetCategory: () -> Unit,
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    val targetProgress = if (totalCount > 0) (currentIndex + 1f) / totalCount.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = targetProgress, label = "adhkar_top_progress")
    val cardShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 28.dp, bottomEnd = 28.dp)
    val edgeBorderColor = qc.border.copy(alpha = 0.9f)

    Surface(
        shape = cardShape,
        color = qc.surface,
        shadowElevation = 10.dp,
        tonalElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(qc.surface2, qc.surface)))
                .drawWithContent {
                    drawContent()
                    val cornerPx = 28.dp.toPx()
                    val strokePx = 1.dp.toPx()
                    val half = strokePx / 2f
                    val path = Path().apply {
                        moveTo(half, size.height - cornerPx)
                        arcTo(
                            rect = Rect(half, size.height - cornerPx * 2f + half, cornerPx * 2f - half, size.height - half),
                            startAngleDegrees = 180f,
                            sweepAngleDegrees = -90f,
                            forceMoveTo = false
                        )
                        lineTo(size.width - cornerPx, size.height - half)
                        arcTo(
                            rect = Rect(size.width - cornerPx * 2f + half, size.height - cornerPx * 2f + half, size.width - half, size.height - half),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = -90f,
                            forceMoveTo = false
                        )
                    }
                    drawPath(path = path, color = edgeBorderColor, style = Stroke(width = strokePx))
                }
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = qc.surface2,
                        border = BorderStroke(1.dp, qc.border),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(40.dp)
                            .testTag("adhkar_detail_back_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.adhkar_back_cd),
                                tint = qc.text,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = qc.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = titleModifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = qc.primary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, qc.primary.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = "${(currentIndex + 1).toAppDigits(useWestern)} / ${totalCount.toAppDigits(useWestern)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = qc.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Surface(
                        onClick = onResetCategory,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(40.dp)
                            .testTag("adhkar_detail_reset_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.RestartAlt,
                                contentDescription = stringResource(R.string.adhkar_reset_section_cd),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            color = qc.primary,
                            trackColor = qc.border.copy(alpha = 0.45f),
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = qc.surface2,
                            border = BorderStroke(1.dp, qc.border)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .clickable(enabled = fontSizeSp > 17f) { onAdjustFontSize(-1.5f) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.TextDecrease,
                                        contentDescription = stringResource(R.string.adhkar_font_decrease_cd),
                                        tint = if (fontSizeSp > 17f) qc.primary else qc.textDisabled,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                Text(
                                    text = fontSizeSp.toInt().toAppDigits(useWestern),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = qc.textMuted
                                )
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .clickable(enabled = fontSizeSp < 29f) { onAdjustFontSize(1.5f) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.TextIncrease,
                                        contentDescription = stringResource(R.string.adhkar_font_increase_cd),
                                        tint = if (fontSizeSp < 29f) qc.primary else qc.textDisabled,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                        val toggleDesc = stringResource(
                            if (expanded) R.string.adhkar_top_bar_collapse_cd else R.string.adhkar_top_bar_expand_cd
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .pointerInput(expanded) {
                                    detectVerticalDragGestures { _, dragAmount ->
                                        if (dragAmount < -12f && expanded) onExpandedChange(false)
                                        else if (dragAmount > 12f && !expanded) onExpandedChange(true)
                                    }
                                }
                                .clickable { onExpandedChange(!expanded) }
                                .semantics { contentDescription = toggleDesc }
                        .padding(vertical = 2.dp),
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
                            imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = qc.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
