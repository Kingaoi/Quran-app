package com.example.ui.screens.wird

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.wird.WirdTimelineSession
import com.example.domain.wird.WirdTimelineState
import com.example.ui.theme.quranColors
import kotlinx.coroutines.launch

private const val DEFAULT_VISIBLE_UPCOMING = 5
private const val DEFAULT_VISIBLE_COMPLETED_SESSIONS = 6

@Composable
internal fun WirdTimelineScreen(
    timeline: WirdTimelineState,
    bottomClearance: Dp,
    onBack: () -> Unit,
    onOpenEdit: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleSession: (Int) -> Unit,
    onOpenSessionReader: (WirdTimelineSession) -> Unit,
    onRescheduleOverdue: () -> Unit,
    onDeferToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var expandUpcoming by remember(timeline.planId) { mutableStateOf(false) }
    var expandCompleted by remember(timeline.planId) { mutableStateOf(false) }

    val showStickyBar by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 220
        }
    }
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex >= 2 }
    }

    val visibleUpcoming = remember(timeline.upcomingSessions, expandUpcoming) {
        if (expandUpcoming || timeline.upcomingSessions.size <= DEFAULT_VISIBLE_UPCOMING) {
            timeline.upcomingSessions
        } else {
            timeline.upcomingSessions.take(DEFAULT_VISIBLE_UPCOMING)
        }
    }
    val hiddenUpcomingCount = (timeline.upcomingSessions.size - visibleUpcoming.size).coerceAtLeast(0)

    val visibleCompletedGroups = remember(timeline.completedDayGroups, expandCompleted) {
        if (expandCompleted) {
            timeline.completedDayGroups
        } else {
            var collected = 0
            buildList {
                for (group in timeline.completedDayGroups) {
                    if (collected >= DEFAULT_VISIBLE_COMPLETED_SESSIONS && isNotEmpty()) break
                    add(group)
                    collected += group.sessions.size
                }
            }
        }
    }
    val visibleCompletedCount = visibleCompletedGroups.sumOf { it.sessions.size }
    val hiddenCompletedCount = (timeline.completedSessions - visibleCompletedCount).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(qc.bg)
            .statusBarsPadding()
            .testTag("wird_timeline_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimelineTopBar(
            title = timeline.planTitle,
            onBack = onBack,
            onOpenEdit = onOpenEdit,
            onOpenSettings = onOpenSettings,
            modifier = Modifier.widthIn(max = 760.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 760.dp)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 6.dp,
                    bottom = 84.dp + bottomClearance
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(key = "timeline_hero") {
                    WirdTimelineHeroCard(timeline = timeline)
                }

                if (timeline.overdueSessions.isNotEmpty()) {
                    item(key = "overdue_header") {
                        WirdTimelineSectionHeader(
                            title = "الجلسات المتأخرة",
                            count = timeline.overdueSessions.size
                        )
                    }
                    item(key = "overdue_action") {
                        WirdTimelineActionBanner(
                            label = "إعادة جدولة المتأخر",
                            icon = Icons.Rounded.EventRepeat,
                            testTag = "timeline_reschedule_overdue_button",
                            onClick = onRescheduleOverdue
                        )
                    }
                    item(key = "overdue_group") {
                        WirdTimelineSessionGroupCard(
                            sessions = timeline.overdueSessions,
                            onToggleSession = onToggleSession,
                            onOpenSession = onOpenSessionReader
                        )
                    }
                }

                item(key = "today_header") {
                    WirdTimelineSectionHeader(
                        title = "جلسات اليوم",
                        count = timeline.todaySessions.size
                    )
                }
                if (timeline.todaySessions.isNotEmpty()) {
                    item(key = "today_group") {
                        WirdTimelineSessionGroupCard(
                            sessions = timeline.todaySessions,
                            onToggleSession = onToggleSession,
                            onOpenSession = onOpenSessionReader
                        )
                    }
                    item(key = "defer_today_action") {
                        WirdTimelineActionBanner(
                            label = "رحّل جلسات اليوم",
                            icon = Icons.AutoMirrored.Rounded.ArrowForward,
                            testTag = "timeline_defer_today_button",
                            onClick = onDeferToday
                        )
                    }
                } else {
                    item(key = "today_empty") {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = qc.surface,
                            border = BorderStroke(1.dp, qc.border),
                            modifier = Modifier.fillMaxWidth().testTag("timeline_today_empty")
                        ) {
                            Text(
                                text = if (timeline.isCompleted) "بحمد الله أتممت جميع جلسات الخطة"
                                else "لا توجد جلسات متبقية لليوم",
                                style = MaterialTheme.typography.bodyMedium,
                                color = qc.textMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
                            )
                        }
                    }
                }

                if (timeline.upcomingSessions.isNotEmpty()) {
                    item(key = "upcoming_header") {
                        WirdTimelineSectionHeader(
                            title = "الجلسات القادمة",
                            count = timeline.upcomingSessions.size
                        )
                    }
                    item(key = "upcoming_group") {
                        WirdTimelineSessionGroupCard(
                            sessions = visibleUpcoming,
                            onToggleSession = onToggleSession,
                            onOpenSession = onOpenSessionReader
                        )
                    }
                    if (timeline.upcomingSessions.size > DEFAULT_VISIBLE_UPCOMING) {
                        item(key = "upcoming_expand") {
                            TimelineExpandButton(
                                expanded = expandUpcoming,
                                hiddenCount = hiddenUpcomingCount,
                                useWestern = useWestern,
                                testTag = "timeline_upcoming_expand_button",
                                onClick = { expandUpcoming = !expandUpcoming }
                            )
                        }
                    }
                }

                if (timeline.completedDayGroups.isNotEmpty()) {
                    item(key = "completed_header") {
                        WirdTimelineSectionHeader(
                            title = "المنجزة",
                            count = timeline.completedSessions
                        )
                    }
                    items(items = visibleCompletedGroups, key = { "done_${it.epochDay}" }) { group ->
                        WirdCompletedDayCard(
                            group = group,
                            onToggleSession = onToggleSession,
                            onOpenSession = onOpenSessionReader
                        )
                    }
                    if (hiddenCompletedCount > 0 || expandCompleted) {
                        item(key = "completed_expand") {
                            TimelineExpandButton(
                                expanded = expandCompleted,
                                hiddenCount = hiddenCompletedCount,
                                useWestern = useWestern,
                                testTag = "timeline_completed_expand_button",
                                onClick = { expandCompleted = !expandCompleted }
                            )
                        }
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showStickyBar,
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                WirdTimelineStickyBar(timeline = timeline)
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showScrollToTop,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp + bottomClearance)
            ) {
                SmallFloatingActionButton(
                    onClick = { scope.launch { listState.animateScrollToItem(0) } },
                    shape = CircleShape,
                    containerColor = qc.surface2,
                    contentColor = qc.text,
                    modifier = Modifier.size(48.dp).testTag("timeline_scroll_top_button")
                ) {
                    Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "العودة للأعلى")
                }
            }
        }
    }
}
