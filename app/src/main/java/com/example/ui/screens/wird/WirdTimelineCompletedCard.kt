package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.localizeDigits
import com.example.domain.model.toAppDigits
import com.example.domain.wird.CompletedDayGroup
import com.example.domain.wird.WirdTimelineSession
import com.example.ui.theme.quranColors

@Composable
internal fun WirdCompletedDayCard(
    group: CompletedDayGroup,
    onToggleSession: (Int) -> Unit,
    onOpenSession: (WirdTimelineSession) -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = group.headerDateAr.localizeDigits(useWestern),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = qc.textMuted,
            modifier = Modifier.padding(top = 4.dp)
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            group.sessions.forEachIndexed { idx, session ->
                val shape = sliceShape(idx, group.sessions.lastIndex)
                val showTopLine = idx > 0
                val showBottomLine = idx < group.sessions.lastIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    Surface(
                        shape = shape,
                        color = qc.surface,
                        border = BorderStroke(1.dp, qc.border),
                        modifier = Modifier
                            .width(56.dp)
                            .fillMaxHeight()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = session.sessionNumber.toAppDigits(useWestern),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = qc.text
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = shape,
                        color = qc.surface,
                        border = BorderStroke(1.dp, qc.border),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(shape)
                            .clickable { onOpenSession(session) }
                            .testTag("timeline_completed_row_${session.portion.index}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 10.dp, start = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TimelineCheckNode(
                                isCompleted = true,
                                showTopLine = showTopLine,
                                showBottomLine = showBottomLine,
                                lineColor = qc.success.copy(alpha = 0.5f),
                                onClick = { onToggleSession(session.portion.index) },
                                testTag = "timeline_session_toggle_${session.portion.index}"
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = session.rangeLabelAr.localizeDigits(useWestern),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = TextDecoration.LineThrough
                                    ),
                                    color = qc.textMuted,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "أتممت • ${(session.completedRelativeAr ?: group.relativeLabelAr).localizeDigits(useWestern)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = qc.success
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = session.amountLabelAr.localizeDigits(useWestern),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = qc.textMuted
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                                    contentDescription = "فتح الجلسة في المصحف",
                                    tint = qc.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun TimelineCheckNode(
    isCompleted: Boolean,
    showTopLine: Boolean,
    showBottomLine: Boolean,
    lineColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val qc = MaterialTheme.quranColors
    Box(
        modifier = Modifier
            .width(48.dp)
            .fillMaxHeight()
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .weight(1f)
                    .background(if (showTopLine) lineColor else Color.Transparent)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .weight(1f)
                    .background(if (showBottomLine) lineColor else Color.Transparent)
            )
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isCompleted) qc.successContainer else qc.surface)
                .border(
                    width = 1.6.dp,
                    color = if (isCompleted) qc.success else qc.textMuted.copy(alpha = 0.65f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "جلسة منجزة",
                    tint = qc.success,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

internal fun sliceShape(index: Int, lastIndex: Int): RoundedCornerShape = when {
    lastIndex == 0 -> RoundedCornerShape(18.dp)
    index == 0 -> RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
    index == lastIndex -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    else -> RoundedCornerShape(4.dp)
}
