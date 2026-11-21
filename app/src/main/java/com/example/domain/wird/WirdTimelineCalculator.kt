package com.example.domain.wird

import com.example.domain.model.toArabicIndicDigits
import kotlin.math.max
import kotlin.math.roundToInt

object WirdTimelineCalculator {

    fun buildForMainKhatmah(
        config: KhatmahPlanConfig,
        summary: WirdPlanSummary,
        todayEpochDay: Long
    ): WirdTimelineState {
        val safeStart = config.startEpochDay.takeIf { it > 0L } ?: todayEpochDay
        val portionLabel = WirdPlanCalculator.formatDailyPortionSummaryAr(config.quartersPerDay)
        return buildTimeline(
            planId = null,
            planTitle = "الختمة الحالية",
            cadenceBadgeAr = "$portionLabel • كل يوم",
            summary = summary,
            startEpochDay = safeStart,
            explicitEndEpochDay = 0L,
            activeDaysMask = ALL_WEEK_DAYS_MASK,
            completedPortionDays = config.completedPortionDays,
            portionDayOverrides = config.portionDayOverrides,
            todayEpochDay = todayEpochDay
        )
    }

    fun buildForCustomPlan(
        item: NamedWirdPlanItem,
        todayEpochDay: Long
    ): WirdTimelineState {
        val cfg = item.config
        val safeStart = cfg.startEpochDay.takeIf { it > 0L } ?: todayEpochDay
        val mask = if (cfg.activeDaysMask == 0) ALL_WEEK_DAYS_MASK else cfg.activeDaysMask
        val typeLabel = if (cfg.isV2Plan) cfg.planType.labelAr else "تلاوة"
        val cadenceLabel = CustomWirdFormatters.formatCadenceAr(mask)
        return buildTimeline(
            planId = cfg.id,
            planTitle = cfg.title,
            cadenceBadgeAr = "$typeLabel • $cadenceLabel",
            summary = item.summary,
            startEpochDay = safeStart,
            explicitEndEpochDay = cfg.endEpochDay,
            activeDaysMask = mask,
            completedPortionDays = cfg.completedPortionDays,
            portionDayOverrides = cfg.portionDayOverrides,
            todayEpochDay = todayEpochDay
        )
    }

    fun buildTimeline(
        planId: String?,
        planTitle: String,
        cadenceBadgeAr: String,
        summary: WirdPlanSummary,
        startEpochDay: Long,
        explicitEndEpochDay: Long,
        activeDaysMask: Int,
        completedPortionDays: Map<Int, Long>,
        portionDayOverrides: Map<Int, Long>,
        todayEpochDay: Long
    ): WirdTimelineState {
        val portions = summary.allWirds
        val safeMask = if (activeDaysMask == 0) ALL_WEEK_DAYS_MASK else activeDaysMask
        val safeStart = startEpochDay.takeIf { it > 0L } ?: todayEpochDay
        val defaultDays = computeDefaultScheduledDays(portions.size, safeStart, safeMask)

        val effectiveCompleted = resolveEffectiveCompletedMap(
            totalPortions = portions.size,
            completedPortionDays = completedPortionDays,
            legacyCompletedCount = summary.completedCount,
            defaultDays = defaultDays,
            todayEpochDay = todayEpochDay
        )

        val sessions = portions.mapIndexed { idx, portion ->
            val defaultDay = defaultDays.getOrElse(idx) { safeStart + idx }
            val scheduledDay = portionDayOverrides[portion.index] ?: defaultDay
            val completedDay = effectiveCompleted[portion.index]
            val isDone = completedDay != null
            val bucket = when {
                isDone -> TimelineSessionBucket.COMPLETED
                scheduledDay < todayEpochDay -> TimelineSessionBucket.OVERDUE
                scheduledDay == todayEpochDay -> TimelineSessionBucket.TODAY
                else -> TimelineSessionBucket.UPCOMING
            }
            val scheduledDateAr = if (scheduledDay == todayEpochDay) {
                "اليوم"
            } else {
                CustomWirdFormatters.formatCompactEpochDayAr(scheduledDay, todayEpochDay)
            }
            WirdTimelineSession(
                portion = portion,
                sessionNumber = portion.index + 1,
                scheduledEpochDay = scheduledDay,
                scheduledDateCompactAr = scheduledDateAr,
                completedEpochDay = completedDay,
                completedRelativeAr = completedDay?.let {
                    CustomWirdFormatters.formatRelativeDayAgeAr(it, todayEpochDay)
                },
                isCompleted = isDone,
                bucket = bucket,
                rangeLabelAr = formatPortionRangeAr(portion),
                amountLabelAr = CustomWirdFormatters.formatAmountAr(
                    portion.pageSpanCount.toDouble(),
                    WirdAmountUnit.PAGE
                )
            )
        }

        val overdue = sessions.filter { it.bucket == TimelineSessionBucket.OVERDUE }
        val today = sessions.filter { it.bucket == TimelineSessionBucket.TODAY }
        val upcoming = sessions.filter { it.bucket == TimelineSessionBucket.UPCOMING }
        val completedGroups = sessions
            .filter { it.bucket == TimelineSessionBucket.COMPLETED }
            .groupBy { it.completedEpochDay ?: todayEpochDay }
            .entries
            .sortedByDescending { it.key }
            .map { (day, daySessions) ->
                CompletedDayGroup(
                    epochDay = day,
                    headerDateAr = CustomWirdFormatters.formatCompactWeekdayDateAr(day, todayEpochDay),
                    relativeLabelAr = CustomWirdFormatters.formatRelativeDayAgeAr(day, todayEpochDay),
                    sessions = daySessions.sortedByDescending { it.sessionNumber }
                )
            }

        val firstPortion = portions.firstOrNull()
        val lastPortion = portions.lastOrNull()
        val startEndpoint = firstPortion?.let {
            "${it.startSurahNameAr} ${it.startAyahNumber.toArabicIndicDigits()}"
        } ?: "الفاتحة ١"
        val endEndpoint = lastPortion?.let {
            "${it.endSurahNameAr} ${it.endAyahNumber.toArabicIndicDigits()}"
        } ?: "الناس ٦"

        val maxScheduled = sessions.maxOfOrNull { it.scheduledEpochDay } ?: safeStart
        val resolvedEndDay = max(maxScheduled, max(explicitEndEpochDay, safeStart))
        val spanDays = (resolvedEndDay - safeStart).coerceAtLeast(1L)
        val todayFraction = ((todayEpochDay - safeStart).toFloat() / spanDays.toFloat()).coerceIn(0f, 1f)
        val remainingDays = (resolvedEndDay - todayEpochDay).toInt().coerceAtLeast(0)

        val totalCount = portions.size
        val completedCount = effectiveCompleted.size
        val progressFraction = if (totalCount == 0) 0f else (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
        val progressPercent = if (totalCount == 0) 0 else ((completedCount * 100f) / totalCount.toFloat()).roundToInt().coerceIn(0, 100)
        val overduePercent = if (totalCount == 0) 0 else ((overdue.size * 100f) / totalCount.toFloat()).roundToInt().coerceIn(0, 100)

        return WirdTimelineState(
            planId = planId,
            planTitle = planTitle,
            cadenceBadgeAr = cadenceBadgeAr,
            totalSessions = totalCount,
            completedSessions = completedCount,
            progressFraction = progressFraction,
            progressPercent = progressPercent,
            startEndpointAr = startEndpoint,
            startDateAr = CustomWirdFormatters.formatCompactEpochDayAr(safeStart, todayEpochDay),
            endEndpointAr = endEndpoint,
            endDateAr = CustomWirdFormatters.formatCompactEpochDayAr(resolvedEndDay, todayEpochDay),
            todayDateAr = CustomWirdFormatters.formatCompactEpochDayAr(todayEpochDay, todayEpochDay),
            todayFraction = todayFraction,
            remainingDays = remainingDays,
            overdueCount = overdue.size,
            overduePercent = overduePercent,
            isCompleted = totalCount > 0 && completedCount >= totalCount,
            hasOverrides = portionDayOverrides.isNotEmpty(),
            overdueSessions = overdue,
            todaySessions = today,
            upcomingSessions = upcoming,
            completedDayGroups = completedGroups
        )
    }

    fun computeDefaultScheduledDays(count: Int, startEpochDay: Long, activeDaysMask: Int): LongArray {
        val result = LongArray(count)
        if (count <= 0) return result
        val safeMask = if (activeDaysMask == 0) ALL_WEEK_DAYS_MASK else activeDaysMask
        var cursor = startEpochDay
        var guard = 0
        var idx = 0
        while (idx < count && guard < 10_000) {
            if (isDayActiveInMask(safeMask, CustomWirdFormatters.dayOfWeekForEpochDay(cursor))) {
                result[idx] = cursor
                idx++
            }
            cursor++
            guard++
        }
        while (idx < count) {
            result[idx] = startEpochDay + idx
            idx++
        }
        return result
    }

    fun computeRescheduleOverdueOverrides(
        totalPortions: Int,
        startEpochDay: Long,
        activeDaysMask: Int,
        completedPortionDays: Map<Int, Long>,
        legacyCompletedCount: Int,
        currentOverrides: Map<Int, Long>,
        todayEpochDay: Long
    ): Map<Int, Long> {
        if (totalPortions <= 0) return currentOverrides
        val safeMask = if (activeDaysMask == 0) ALL_WEEK_DAYS_MASK else activeDaysMask
        val safeStart = startEpochDay.takeIf { it > 0L } ?: todayEpochDay
        val defaultDays = computeDefaultScheduledDays(totalPortions, safeStart, safeMask)
        val completedMap = resolveEffectiveCompletedMap(
            totalPortions, completedPortionDays, legacyCompletedCount, defaultDays, todayEpochDay
        )
        val incompleteIndices = (0 until totalPortions).filter { it !in completedMap }
        if (incompleteIndices.isEmpty()) return currentOverrides

        val hasAnyOverdue = incompleteIndices.any { idx ->
            (currentOverrides[idx] ?: defaultDays[idx]) < todayEpochDay
        }
        if (!hasAnyOverdue) return currentOverrides

        val anchorStart = CustomWirdFormatters.advanceByActiveStudyDays(todayEpochDay, 1, safeMask)
        val newDays = computeDefaultScheduledDays(incompleteIndices.size, anchorStart, safeMask)
        val updated = currentOverrides.toMutableMap()
        incompleteIndices.forEachIndexed { offset, portionIdx ->
            val targetDay = newDays[offset]
            if (targetDay == defaultDays[portionIdx]) {
                updated.remove(portionIdx)
            } else {
                updated[portionIdx] = targetDay
            }
        }
        return updated
    }

    fun computeDeferTodayOverrides(
        totalPortions: Int,
        startEpochDay: Long,
        activeDaysMask: Int,
        completedPortionDays: Map<Int, Long>,
        legacyCompletedCount: Int,
        currentOverrides: Map<Int, Long>,
        todayEpochDay: Long
    ): Map<Int, Long> {
        if (totalPortions <= 0) return currentOverrides
        val safeMask = if (activeDaysMask == 0) ALL_WEEK_DAYS_MASK else activeDaysMask
        val safeStart = startEpochDay.takeIf { it > 0L } ?: todayEpochDay
        val defaultDays = computeDefaultScheduledDays(totalPortions, safeStart, safeMask)
        val completedMap = resolveEffectiveCompletedMap(
            totalPortions, completedPortionDays, legacyCompletedCount, defaultDays, todayEpochDay
        )
        val targetIndices = (0 until totalPortions).filter { idx ->
            idx !in completedMap && (currentOverrides[idx] ?: defaultDays[idx]) >= todayEpochDay
        }
        val hasToday = targetIndices.any { (currentOverrides[it] ?: defaultDays[it]) == todayEpochDay }
        if (!hasToday) return currentOverrides

        val nextActiveDay = CustomWirdFormatters.advanceByActiveStudyDays(todayEpochDay + 1L, 1, safeMask)
        val newDays = computeDefaultScheduledDays(targetIndices.size, nextActiveDay, safeMask)
        val updated = currentOverrides.toMutableMap()
        targetIndices.forEachIndexed { offset, portionIdx ->
            val targetDay = newDays[offset]
            if (targetDay == defaultDays[portionIdx]) {
                updated.remove(portionIdx)
            } else {
                updated[portionIdx] = targetDay
            }
        }
        return updated
    }

    fun materializeCompletionMap(
        existingMap: Map<Int, Long>,
        legacyCompletedCount: Int,
        totalPortions: Int,
        startEpochDay: Long,
        activeDaysMask: Int,
        todayEpochDay: Long
    ): MutableMap<Int, Long> {
        val safeStart = startEpochDay.takeIf { it > 0L } ?: todayEpochDay
        val safeMask = if (activeDaysMask == 0) ALL_WEEK_DAYS_MASK else activeDaysMask
        val defaultDays = computeDefaultScheduledDays(totalPortions, safeStart, safeMask)
        return resolveEffectiveCompletedMap(
            totalPortions = totalPortions,
            completedPortionDays = existingMap,
            legacyCompletedCount = legacyCompletedCount,
            defaultDays = defaultDays,
            todayEpochDay = todayEpochDay
        ).toMutableMap()
    }

    private fun resolveEffectiveCompletedMap(
        totalPortions: Int,
        completedPortionDays: Map<Int, Long>,
        legacyCompletedCount: Int,
        defaultDays: LongArray,
        todayEpochDay: Long
    ): Map<Int, Long> {
        if (totalPortions <= 0) return emptyMap()
        if (completedPortionDays.isNotEmpty()) {
            return completedPortionDays.filterKeys { it in 0 until totalPortions }
        }
        val count = legacyCompletedCount.coerceIn(0, totalPortions)
        if (count == 0) return emptyMap()
        val synthesized = LinkedHashMap<Int, Long>(count)
        for (i in 0 until count) {
            val defaultDay = defaultDays.getOrElse(i) { todayEpochDay }
            synthesized[i] = minOf(defaultDay, todayEpochDay)
        }
        return synthesized
    }

    private fun formatPortionRangeAr(portion: WirdPortion): String {
        val sNum = portion.startAyahNumber.toArabicIndicDigits()
        val eNum = portion.endAyahNumber.toArabicIndicDigits()
        return if (portion.startSurahNumber == portion.endSurahNumber) {
            "${portion.startSurahNameAr} $sNum إلى $eNum"
        } else {
            "${portion.startSurahNameAr} $sNum إلى ${portion.endSurahNameAr} $eNum"
        }
    }
}
