package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.domain.wird.WirdTimelineState
import com.example.ui.theme.quranColors

@Composable
internal fun WirdTimelineStickyBar(
    timeline: WirdTimelineState,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = qc.surface.copy(alpha = 0.96f),
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, qc.border),
        modifier = modifier
            .fillMaxWidth()
            .testTag("timeline_sticky_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (timeline.completedSessions > 0) qc.success else qc.primary)
            )
            Text(
                text = "${timeline.completedSessions.toAppDigits(useWestern)} من ${timeline.totalSessions.toAppDigits(useWestern)} جلسة",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = qc.text,
                maxLines = 1
            )
            LinearProgressIndicator(
                progress = { timeline.progressFraction },
                color = qc.success,
                trackColor = qc.surface2,
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .weight(1f)
                    .height(7.dp)
                    .clip(RoundedCornerShape(50))
            )
            Text(
                text = "${timeline.progressPercent.toAppDigits(useWestern)}٪",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (timeline.completedSessions > 0) qc.success else qc.textMuted
            )
            if (timeline.overdueCount > 0 && !timeline.isCompleted) {
                OverdueBadgePill(
                    overdueCount = timeline.overdueCount,
                    overduePercent = timeline.overduePercent,
                    useWestern = useWestern
                )
            }
        }
    }
}

@Composable
internal fun OverdueBadgePill(
    overdueCount: Int,
    overduePercent: Int,
    useWestern: Boolean
) {
    val qc = MaterialTheme.quranColors
    Surface(
        shape = RoundedCornerShape(50),
        color = qc.dangerContainer
    ) {
        Text(
            text = "${overdueCount.toAppDigits(useWestern)} متأخرة • ${overduePercent.toAppDigits(useWestern)}٪",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = qc.danger,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
        )
    }
}
