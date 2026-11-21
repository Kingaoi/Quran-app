package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.localizeDigits
import com.example.domain.model.toAppDigits
import com.example.domain.wird.WirdTimelineState
import com.example.ui.theme.quranColors
import kotlin.math.roundToInt

@Composable
internal fun WirdTimelineHeroCard(
    timeline: WirdTimelineState,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = qc.surface,
        border = BorderStroke(1.dp, qc.border),
        modifier = modifier
            .fillMaxWidth()
            .testTag("timeline_hero_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(if (timeline.completedSessions > 0) qc.success else qc.primary)
                    )
                    Text(
                        text = "${timeline.completedSessions.toAppDigits(useWestern)} من ${timeline.totalSessions.toAppDigits(useWestern)} جلسة",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = qc.text
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = qc.bg,
                    border = BorderStroke(1.dp, qc.border)
                ) {
                    Text(
                        text = timeline.cadenceBadgeAr.localizeDigits(useWestern),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = qc.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val density = LocalDensity.current
                    val maxWidthPx = with(density) { maxWidth.toPx() }
                    val pillApproxPx = with(density) { 126.dp.toPx() }
                    val startPadPx = with(density) { 26.dp.toPx() }
                    val endPadPx = with(density) { 54.dp.toPx() }
                    val usablePx = (maxWidthPx - startPadPx - endPadPx).coerceAtLeast(1f)
                    val anchorFromStartPx = startPadPx + timeline.todayFraction * usablePx
                    val pillOffsetPx = (anchorFromStartPx - pillApproxPx / 2f)
                        .coerceIn(0f, (maxWidthPx - pillApproxPx).coerceAtLeast(0f))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = qc.surface2,
                            border = BorderStroke(1.dp, qc.border),
                            modifier = Modifier.offset { IntOffset(pillOffsetPx.roundToInt(), 0) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(qc.danger)
                                )
                                Text(
                                    text = "اليوم • ${timeline.todayDateAr.localizeDigits(useWestern)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = qc.text,
                                    maxLines = 1
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(anchorFromStartPx.roundToInt(), 0) }
                                .width(1.5.dp)
                                .height(8.dp)
                                .background(qc.danger.copy(alpha = 0.7f))
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RocketLaunch,
                        contentDescription = null,
                        tint = qc.textMuted,
                        modifier = Modifier.size(18.dp)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val trackColor = qc.border.copy(alpha = 0.75f)
                        val progressColor = qc.success
                        val dotColor = qc.textMuted.copy(alpha = 0.45f)
                        Canvas(modifier = Modifier.fillMaxWidth().height(12.dp)) {
                            val barH = 7.dp.toPx()
                            val topY = (size.height - barH) / 2f
                            drawRoundRect(
                                color = trackColor,
                                topLeft = Offset(0f, topY),
                                size = Size(size.width, barH),
                                cornerRadius = CornerRadius(barH / 2f, barH / 2f)
                            )
                            val dotStep = 8.dp.toPx()
                            var x = dotStep / 2f
                            while (x < size.width - dotStep / 2f) {
                                drawCircle(
                                    color = dotColor,
                                    radius = 1.4.dp.toPx(),
                                    center = Offset(x, size.height / 2f)
                                )
                                x += dotStep
                            }
                            val fillW = (size.width * timeline.progressFraction).coerceIn(0f, size.width)
                            if (fillW > 0f) {
                                val startX = if (isRtl) size.width - fillW else 0f
                                drawRoundRect(
                                    color = progressColor,
                                    topLeft = Offset(startX, topY),
                                    size = Size(fillW, barH),
                                    cornerRadius = CornerRadius(barH / 2f, barH / 2f)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${timeline.progressPercent.toAppDigits(useWestern)}٪",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (timeline.completedSessions > 0) qc.success else qc.textMuted
                    )

                    Icon(
                        imageVector = Icons.Rounded.Flag,
                        contentDescription = null,
                        tint = qc.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = timeline.startEndpointAr.localizeDigits(useWestern),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = qc.text
                    )
                    Text(
                        text = timeline.startDateAr.localizeDigits(useWestern),
                        style = MaterialTheme.typography.labelSmall,
                        color = qc.textMuted
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = if (timeline.isCompleted) "أتممت الخطة بحمد الله"
                        else "متبقي ${timeline.remainingDays.toAppDigits(useWestern)} يوم",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (timeline.isCompleted) qc.success else qc.textMuted
                    )
                    if (timeline.overdueCount > 0 && !timeline.isCompleted) {
                        OverdueBadgePill(
                            overdueCount = timeline.overdueCount,
                            overduePercent = timeline.overduePercent,
                            useWestern = useWestern
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = timeline.endEndpointAr.localizeDigits(useWestern),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = qc.text
                    )
                    Text(
                        text = timeline.endDateAr.localizeDigits(useWestern),
                        style = MaterialTheme.typography.labelSmall,
                        color = qc.textMuted
                    )
                }
            }
        }
    }
}
