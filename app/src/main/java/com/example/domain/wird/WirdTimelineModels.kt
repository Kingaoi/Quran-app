package com.example.domain.wird

import androidx.compose.runtime.Immutable

@Immutable
enum class TimelineSessionBucket {
    OVERDUE,
    TODAY,
    UPCOMING,
    COMPLETED
}

@Immutable
data class WirdTimelineSession(
    val portion: WirdPortion,
    val sessionNumber: Int,
    val scheduledEpochDay: Long,
    val scheduledDateCompactAr: String,
    val completedEpochDay: Long?,
    val completedRelativeAr: String?,
    val isCompleted: Boolean,
    val bucket: TimelineSessionBucket,
    val rangeLabelAr: String,
    val amountLabelAr: String
)

@Immutable
data class CompletedDayGroup(
    val epochDay: Long,
    val headerDateAr: String,
    val relativeLabelAr: String,
    val sessions: List<WirdTimelineSession>
)

@Immutable
data class WirdTimelineState(
    val planId: String?,
    val planTitle: String,
    val cadenceBadgeAr: String,
    val totalSessions: Int,
    val completedSessions: Int,
    val progressFraction: Float,
    val progressPercent: Int,
    val startEndpointAr: String,
    val startDateAr: String,
    val endEndpointAr: String,
    val endDateAr: String,
    val todayDateAr: String,
    val todayFraction: Float,
    val remainingDays: Int,
    val overdueCount: Int,
    val overduePercent: Int,
    val isCompleted: Boolean,
    val hasOverrides: Boolean,
    val overdueSessions: List<WirdTimelineSession>,
    val todaySessions: List<WirdTimelineSession>,
    val upcomingSessions: List<WirdTimelineSession>,
    val completedDayGroups: List<CompletedDayGroup>
)
