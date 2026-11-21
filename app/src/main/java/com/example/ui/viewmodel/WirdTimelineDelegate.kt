package com.example.ui.viewmodel

import com.example.data.local.ReadingStatsRepository
import com.example.data.local.WirdPreferencesRepository
import com.example.domain.model.toArabicIndicDigits
import com.example.domain.wird.ALL_WEEK_DAYS_MASK
import com.example.domain.wird.WirdTimelineCalculator

internal class WirdTimelineDelegate(
    private val prefsRepository: WirdPreferencesRepository,
    private val statsRepository: ReadingStatsRepository
) {
    suspend fun togglePortionCompleted(
        state: WirdUiState,
        planId: String?,
        portionIndex: Int
    ): String? {
        val today = WirdPreferencesRepository.currentLocalEpochDay()
        if (planId.isNullOrBlank()) {
            val plan = state.plan ?: return null
            if (portionIndex !in 0 until plan.totalWirds) return null
            val cfg = state.config
            val map = WirdTimelineCalculator.materializeCompletionMap(
                existingMap = cfg.completedPortionDays,
                legacyCompletedCount = cfg.completedWirdsCount,
                totalPortions = plan.totalWirds,
                startEpochDay = cfg.startEpochDay,
                activeDaysMask = ALL_WEEK_DAYS_MASK,
                todayEpochDay = today
            )
            val nowDone = if (portionIndex in map) {
                map.remove(portionIndex); false
            } else {
                map[portionIndex] = today; true
            }
            prefsRepository.setMainTimelineState(completedDays = map)
            if (nowDone && map.size >= plan.totalWirds) {
                statsRepository.recordKhatmahCompleted()
                return "ما شاء الله! أتممت الختمة المباركة بحمد الله."
            }
            return if (nowDone) {
                "تم إتمام الجلسة ${(portionIndex + 1).toArabicIndicDigits()}"
            } else {
                "تم التراجع عن إتمام الجلسة ${(portionIndex + 1).toArabicIndicDigits()}"
            }
        }

        val item = state.customPlans.firstOrNull { it.config.id == planId } ?: return null
        val total = item.summary.totalWirds
        if (portionIndex !in 0 until total) return null
        val cfg = item.config
        val map = WirdTimelineCalculator.materializeCompletionMap(
            existingMap = cfg.completedPortionDays,
            legacyCompletedCount = cfg.completedWirdsCount,
            totalPortions = total,
            startEpochDay = cfg.startEpochDay,
            activeDaysMask = cfg.activeDaysMask,
            todayEpochDay = today
        )
        val nowDone = if (portionIndex in map) {
            map.remove(portionIndex); false
        } else {
            map[portionIndex] = today; true
        }
        prefsRepository.setCustomPlanTimelineState(planId = planId, completedPortionDays = map)
        if (nowDone && map.size >= total) {
            statsRepository.recordKhatmahCompleted()
            return "ما شاء الله! أتممت خطة «${cfg.title}»"
        }
        return if (nowDone) {
            "تم إتمام الجلسة ${(portionIndex + 1).toArabicIndicDigits()} في «${cfg.title}»"
        } else {
            "تم التراجع عن الجلسة ${(portionIndex + 1).toArabicIndicDigits()}"
        }
    }

    suspend fun rescheduleOverdue(state: WirdUiState, planId: String?): String? {
        val today = WirdPreferencesRepository.currentLocalEpochDay()
        if (planId.isNullOrBlank()) {
            val plan = state.plan ?: return null
            val cfg = state.config
            val overrides = WirdTimelineCalculator.computeRescheduleOverdueOverrides(
                totalPortions = plan.totalWirds,
                startEpochDay = cfg.startEpochDay,
                activeDaysMask = ALL_WEEK_DAYS_MASK,
                completedPortionDays = cfg.completedPortionDays,
                legacyCompletedCount = cfg.completedWirdsCount,
                currentOverrides = cfg.portionDayOverrides,
                todayEpochDay = today
            )
            prefsRepository.setMainTimelineState(dayOverrides = overrides)
            return "تمت إعادة جدولة الجلسات المتأخرة بدءاً من اليوم"
        }
        val item = state.customPlans.firstOrNull { it.config.id == planId } ?: return null
        val cfg = item.config
        val overrides = WirdTimelineCalculator.computeRescheduleOverdueOverrides(
            totalPortions = item.summary.totalWirds,
            startEpochDay = cfg.startEpochDay,
            activeDaysMask = cfg.activeDaysMask,
            completedPortionDays = cfg.completedPortionDays,
            legacyCompletedCount = cfg.completedWirdsCount,
            currentOverrides = cfg.portionDayOverrides,
            todayEpochDay = today
        )
        prefsRepository.setCustomPlanTimelineState(planId = planId, portionDayOverrides = overrides)
        return "تمت إعادة جدولة الجلسات المتأخرة لخطة «${cfg.title}»"
    }

    suspend fun deferTodaySessions(state: WirdUiState, planId: String?): String? {
        val today = WirdPreferencesRepository.currentLocalEpochDay()
        if (planId.isNullOrBlank()) {
            val plan = state.plan ?: return null
            val cfg = state.config
            val overrides = WirdTimelineCalculator.computeDeferTodayOverrides(
                totalPortions = plan.totalWirds,
                startEpochDay = cfg.startEpochDay,
                activeDaysMask = ALL_WEEK_DAYS_MASK,
                completedPortionDays = cfg.completedPortionDays,
                legacyCompletedCount = cfg.completedWirdsCount,
                currentOverrides = cfg.portionDayOverrides,
                todayEpochDay = today
            )
            prefsRepository.setMainTimelineState(dayOverrides = overrides)
            return "تم ترحيل جلسات اليوم إلى الغد"
        }
        val item = state.customPlans.firstOrNull { it.config.id == planId } ?: return null
        val cfg = item.config
        val overrides = WirdTimelineCalculator.computeDeferTodayOverrides(
            totalPortions = item.summary.totalWirds,
            startEpochDay = cfg.startEpochDay,
            activeDaysMask = cfg.activeDaysMask,
            completedPortionDays = cfg.completedPortionDays,
            legacyCompletedCount = cfg.completedWirdsCount,
            currentOverrides = cfg.portionDayOverrides,
            todayEpochDay = today
        )
        prefsRepository.setCustomPlanTimelineState(planId = planId, portionDayOverrides = overrides)
        return "تم ترحيل جلسات اليوم لخطة «${cfg.title}»"
    }

    suspend fun resetOverrides(planId: String?): String {
        if (planId.isNullOrBlank()) {
            prefsRepository.setMainTimelineState(dayOverrides = emptyMap())
        } else {
            prefsRepository.setCustomPlanTimelineState(planId = planId, portionDayOverrides = emptyMap())
        }
        return "تمت استعادة الجدول الزمني الأصلي للخطة"
    }

    suspend fun completeMainPortion(state: WirdUiState): Triple<Int, String, Boolean>? {
        val currentPlan = state.plan ?: return null
        val currentIdx = currentPlan.currentWird?.index ?: return null
        val prevCount = currentPlan.completedCount
        val today = WirdPreferencesRepository.currentLocalEpochDay()
        val map = WirdTimelineCalculator.materializeCompletionMap(
            state.config.completedPortionDays, state.config.completedWirdsCount, currentPlan.totalWirds,
            state.config.startEpochDay, ALL_WEEK_DAYS_MASK, today
        )
        map[currentIdx] = today
        prefsRepository.setMainTimelineState(completedDays = map)
        val finishedAll = map.size >= currentPlan.totalWirds
        if (finishedAll) statsRepository.recordKhatmahCompleted()
        val msg = if (finishedAll) "ما شاء الله! أتممت الختمة المباركة بحمد الله."
        else "تقبّل الله طاعتك، تم إتمام الورد ${(currentIdx + 1).toArabicIndicDigits()}"
        return Triple(prevCount, msg, finishedAll)
    }

    suspend fun completeCustomPortion(state: WirdUiState, planId: String): Pair<Int, String>? {
        val item = state.customPlans.firstOrNull { it.config.id == planId } ?: return null
        val currentIdx = item.summary.currentWird?.index ?: return null
        val prevCount = item.summary.completedCount
        val today = WirdPreferencesRepository.currentLocalEpochDay()
        val map = WirdTimelineCalculator.materializeCompletionMap(
            item.config.completedPortionDays, item.config.completedWirdsCount, item.summary.totalWirds,
            item.config.startEpochDay, item.config.activeDaysMask, today
        )
        map[currentIdx] = today
        prefsRepository.setCustomPlanTimelineState(planId = planId, completedPortionDays = map)
        val finishedAll = map.size >= item.summary.totalWirds
        if (finishedAll) statsRepository.recordKhatmahCompleted()
        val msg = if (finishedAll) "ما شاء الله! أتممت خطة «${item.config.title}»"
        else "تم إتمام الورد ${(currentIdx + 1).toArabicIndicDigits()} في خطة «${item.config.title}»"
        return prevCount to msg
    }
}
