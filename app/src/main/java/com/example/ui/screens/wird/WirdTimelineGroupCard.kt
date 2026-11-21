package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.localizeDigits
import com.example.domain.model.toAppDigits
import com.example.domain.wird.TimelineSessionBucket
import com.example.domain.wird.WirdTimelineSession
import com.example.ui.theme.quranColors

@Composable
internal fun WirdTimelineSectionHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = qc.accent
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = qc.border.copy(alpha = 0.7f)
        )
        Surface(
            shape = CircleShape,
            color = qc.surface2,
            border = BorderStroke(1.dp, qc.border.copy(alpha = 0.6f))
        ) {
            Text(
                text = count.toAppDigits(useWestern),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = qc.accent,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
internal fun WirdTimelineActionBanner(
    label: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = qc.surface,
        border = BorderStroke(1.dp, qc.border),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = qc.accent,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = qc.text
            )
        }
    }
}

@Composable
internal fun WirdTimelineSessionGroupCard(
    sessions: List<WirdTimelineSession>,
    onToggleSession: (Int) -> Unit,
    onOpenSession: (WirdTimelineSession) -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    Column(modifier = modifier.fillMaxWidth()) {
        sessions.forEachIndexed { idx, session ->
            val shape = sliceShape(idx, sessions.lastIndex)
            val showTopLine = idx > 0
            val showBottomLine = idx < sessions.lastIndex
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
                        .width(96.dp)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "الجلسة ${session.sessionNumber.toAppDigits(useWestern)}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (session.bucket == TimelineSessionBucket.TODAY) qc.accent else qc.text,
                            maxLines = 1
                        )
                        Text(
                            text = session.scheduledDateCompactAr.localizeDigits(useWestern),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (session.bucket == TimelineSessionBucket.TODAY) qc.primary else qc.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                        .testTag("timeline_session_row_${session.portion.index}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 10.dp, start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimelineCheckNode(
                            isCompleted = false,
                            showTopLine = showTopLine,
                            showBottomLine = showBottomLine,
                            lineColor = qc.border,
                            onClick = { onToggleSession(session.portion.index) },
                            testTag = "timeline_session_toggle_${session.portion.index}"
                        )

                        Text(
                            text = session.rangeLabelAr.localizeDigits(useWestern),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = qc.text,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 14.dp, horizontal = 4.dp)
                        )

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
