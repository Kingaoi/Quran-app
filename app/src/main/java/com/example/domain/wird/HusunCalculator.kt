package com.example.domain.wird

import com.example.domain.model.AyahItem
import com.example.domain.model.toArabicIndicDigits

object HusunCalculator {
    private const val TOTAL_PAGES = 604
    private const val TOTAL_HALF_UNITS = TOTAL_PAGES * 2
    private const val NEAR_WINDOW_PAGES = 20

    fun buildDailyPlan(
        config: HusunConfig,
        allAyahs: List<AyahItem>,
        todayEpochDay: Long
    ): HusunDailyPlanUiState? {
        if (!config.isActive) return null
        val ayahsByPage = allAyahs.groupBy { it.pageNumber }
        val activeMask = config.effectiveMask(todayEpochDay)
        val todayRepeats = config.effectiveRepeatCount(todayEpochDay)

        val baseUnits = (config.startingMemorizedPages.coerceIn(0, TOTAL_PAGES - 1) * 2)
        val memorizedUnits = (baseUnits + config.completedNewUnits.coerceAtLeast(0))
            .coerceIn(0, TOTAL_HALF_UNITS)
        val memorizedFullPages = (memorizedUnits / 2).coerceIn(0, TOTAL_PAGES)
        val isAllHifzCompleted = memorizedUnits >= TOTAL_HALF_UNITS

        val f1Continuous = buildContinuousFortress(config, ayahsByPage, activeMask)
        val f2Prep = buildPrepFortress(config, memorizedUnits, ayahsByPage, activeMask)
        val f3New = buildNewHifzFortress(config, memorizedUnits, ayahsByPage, activeMask)
        val f4Near = buildNearReviewFortress(config, memorizedFullPages, ayahsByPage, activeMask)
        val f5Far = buildFarReviewFortress(config, memorizedFullPages, ayahsByPage, activeMask)

        val fortresses = listOf(f1Continuous, f2Prep, f3New, f4Near, f5Far)
        val doneToday = fortresses.count { it.isCompletedToday }

        return HusunDailyPlanUiState(
            config = config,
            totalMemorizedPages = memorizedFullPages,
            totalMemorizedHalfPages = memorizedUnits,
            overallProgressFraction = (memorizedUnits.toFloat() / TOTAL_HALF_UNITS.toFloat()).coerceIn(0f, 1f),
            completedTodayCount = doneToday,
            todayRepeatCount = todayRepeats,
            fortresses = fortresses,
            isAllHifzCompleted = isAllHifzCompleted
        )
    }

    private fun buildContinuousFortress(
        config: HusunConfig,
        ayahsByPage: Map<Int, List<AyahItem>>,
        activeMask: Int
    ): HusunFortressAssignment {
        val span = config.continuousPagesPerDay.coerceIn(10, 80)
        val startOffset = (config.continuousCursorDay.coerceAtLeast(0) * span) % TOTAL_PAGES
        val pages = (0 until span).map { d -> orderedPage(config.direction, (startOffset + d) % TOTAL_PAGES) }
        val firstP = pages.first()
        val lastP = pages.last()
        val surahLabel = describeSurahsAcrossPages(listOf(firstP, lastP), ayahsByPage)
        return HusunFortressAssignment(
            type = HusunFortressType.CONTINUOUS_READING,
            isCompletedToday = (activeMask and HusunFortressType.CONTINUOUS_READING.bitMask) != 0,
            rangeTitleAr = "ص ${firstP.toArabicIndicDigits()} - ${lastP.toArabicIndicDigits()} (${span.toArabicIndicDigits()} وجهاً)",
            detailSubtitleAr = "$surahLabel • قراءة حدر متصلة مع الاستماع",
            targetPage = firstP,
            canOpenReader = true
        )
    }

    private fun buildPrepFortress(
        config: HusunConfig,
        memorizedUnits: Int,
        ayahsByPage: Map<Int, List<AyahItem>>,
        activeMask: Int
    ): HusunFortressAssignment {
        val tomorrowStartUnit = memorizedUnits + config.dailyNewRate.halfPageUnits
        if (tomorrowStartUnit >= TOTAL_HALF_UNITS) {
            val fallbackPage = orderedPage(config.direction, (TOTAL_PAGES - 1).coerceAtLeast(0))
            return HusunFortressAssignment(
                type = HusunFortressType.PREPARATION,
                isCompletedToday = (activeMask and HusunFortressType.PREPARATION.bitMask) != 0,
                rangeTitleAr = "تحضير المراجعة الختامية الشاملة",
                detailSubtitleAr = "أوشكت على إتمام حفظ المصحف الكريم، حضّر مواضع الختام والربط",
                targetPage = fallbackPage,
                canOpenReader = true
            )
        }
        val spanDesc = describeHalfUnitSlice(
            direction = config.direction,
            startUnit = tomorrowStartUnit,
            unitCount = config.dailyNewRate.halfPageUnits,
            ayahsByPage = ayahsByPage
        )
        return HusunFortressAssignment(
            type = HusunFortressType.PREPARATION,
            isCompletedToday = (activeMask and HusunFortressType.PREPARATION.bitMask) != 0,
            rangeTitleAr = "مقطع الغد: ${spanDesc.rangeTitle}",
            detailSubtitleAr = "${spanDesc.detailTitle} • تحضير ليلي وقبلي",
            targetPage = spanDesc.openPage,
            canOpenReader = true
        )
    }

    private fun buildNewHifzFortress(
        config: HusunConfig,
        memorizedUnits: Int,
        ayahsByPage: Map<Int, List<AyahItem>>,
        activeMask: Int
    ): HusunFortressAssignment {
        if (memorizedUnits >= TOTAL_HALF_UNITS) {
            return HusunFortressAssignment(
                type = HusunFortressType.NEW_MEMORIZATION,
                isCompletedToday = true,
                rangeTitleAr = "أتممت حفظ المصحف كاملاً بحمد الله!",
                detailSubtitleAr = "استمر في حصون التثبيت والمراجعة القريبة والبعيدة",
                targetPage = 1,
                canOpenReader = true
            )
        }
        val spanDesc = describeHalfUnitSlice(
            direction = config.direction,
            startUnit = memorizedUnits,
            unitCount = config.dailyNewRate.halfPageUnits,
            ayahsByPage = ayahsByPage
        )
        return HusunFortressAssignment(
            type = HusunFortressType.NEW_MEMORIZATION,
            isCompletedToday = (activeMask and HusunFortressType.NEW_MEMORIZATION.bitMask) != 0,
            rangeTitleAr = spanDesc.rangeTitle,
            detailSubtitleAr = spanDesc.detailTitle,
            targetPage = spanDesc.openPage,
            canOpenReader = true
        )
    }

    private fun buildNearReviewFortress(
        config: HusunConfig,
        memorizedFullPages: Int,
        ayahsByPage: Map<Int, List<AyahItem>>,
        activeMask: Int
    ): HusunFortressAssignment {
        if (memorizedFullPages <= 0) {
            return HusunFortressAssignment(
                type = HusunFortressType.NEAR_REVIEW,
                isCompletedToday = (activeMask and HusunFortressType.NEAR_REVIEW.bitMask) != 0,
                rangeTitleAr = "تبدأ غداً بعد أول حفظ جديد",
                detailSubtitleAr = "يُراجع في هذا الحصن آخر ٢٠ وجهاً تم حفظها متتابعة",
                targetPage = orderedPage(config.direction, 0),
                canOpenReader = false
            )
        }
        val windowCount = minOf(NEAR_WINDOW_PAGES, memorizedFullPages)
        val startOrderIdx = (memorizedFullPages - windowCount).coerceAtLeast(0)
        val endOrderIdx = (memorizedFullPages - 1).coerceAtLeast(startOrderIdx)
        val firstP = orderedPage(config.direction, startOrderIdx)
        val lastP = orderedPage(config.direction, endOrderIdx)
        val surahLabel = describeSurahsAcrossPages(listOf(firstP, lastP), ayahsByPage)
        return HusunFortressAssignment(
            type = HusunFortressType.NEAR_REVIEW,
            isCompletedToday = (activeMask and HusunFortressType.NEAR_REVIEW.bitMask) != 0,
            rangeTitleAr = "ص ${firstP.toArabicIndicDigits()} - ${lastP.toArabicIndicDigits()} (${windowCount.toArabicIndicDigits()} وجهاً)",
            detailSubtitleAr = "$surahLabel • أحدث المحفوظات",
            targetPage = firstP,
            canOpenReader = true
        )
    }

    private fun buildFarReviewFortress(
        config: HusunConfig,
        memorizedFullPages: Int,
        ayahsByPage: Map<Int, List<AyahItem>>,
        activeMask: Int
    ): HusunFortressAssignment {
        val farPoolCount = (memorizedFullPages - NEAR_WINDOW_PAGES).coerceAtLeast(0)
        if (farPoolCount <= 0) {
            return HusunFortressAssignment(
                type = HusunFortressType.FAR_REVIEW,
                isCompletedToday = (activeMask and HusunFortressType.FAR_REVIEW.bitMask) != 0,
                rangeTitleAr = "يبدأ بعد تجاوز ٢٠ وجهاً محفوظاً",
                detailSubtitleAr = "عندما تتعدى حاجز ٢٠ وجهاً يُقسّم القديم إلى أوراد يومية",
                targetPage = orderedPage(config.direction, 0),
                canOpenReader = false
            )
        }
        val divisions = minOf(config.farReviewDivisions.coerceIn(2, 30), farPoolCount)
        val daySlot = config.continuousCursorDay.coerceAtLeast(0) % divisions
        val sliceStart = (daySlot * farPoolCount) / divisions
        val sliceEnd = (((daySlot + 1) * farPoolCount) / divisions - 1).coerceIn(sliceStart, farPoolCount - 1)
        val count = sliceEnd - sliceStart + 1
        val firstP = orderedPage(config.direction, sliceStart)
        val lastP = orderedPage(config.direction, sliceEnd)
        val surahLabel = describeSurahsAcrossPages(listOf(firstP, lastP), ayahsByPage)
        return HusunFortressAssignment(
            type = HusunFortressType.FAR_REVIEW,
            isCompletedToday = (activeMask and HusunFortressType.FAR_REVIEW.bitMask) != 0,
            rangeTitleAr = "ص ${firstP.toArabicIndicDigits()} - ${lastP.toArabicIndicDigits()} (${count.toArabicIndicDigits()} وجهاً)",
            detailSubtitleAr = "$surahLabel • القسم ${(daySlot + 1).toArabicIndicDigits()} من ${divisions.toArabicIndicDigits()}",
            targetPage = firstP,
            canOpenReader = true
        )
    }

    private data class SliceDesc(val rangeTitle: String, val detailTitle: String, val openPage: Int)

    private fun describeHalfUnitSlice(
        direction: WirdDirection,
        startUnit: Int,
        unitCount: Int,
        ayahsByPage: Map<Int, List<AyahItem>>
    ): SliceDesc {
        val clampedStart = startUnit.coerceIn(0, TOTAL_HALF_UNITS - 1)
        val clampedEnd = (clampedStart + unitCount.coerceAtLeast(1) - 1).coerceIn(clampedStart, TOTAL_HALF_UNITS - 1)
        val firstOrderIdx = clampedStart / 2
        val lastOrderIdx = clampedEnd / 2
        val firstPage = orderedPage(direction, firstOrderIdx)
        val lastPage = orderedPage(direction, lastOrderIdx)
        val surahSummary = describeSurahsAcrossPages(listOf(firstPage, lastPage), ayahsByPage)
        val rangeTitle = when {
            clampedStart == clampedEnd && clampedStart % 2 == 0 ->
                "النصف الأول من ص ${firstPage.toArabicIndicDigits()}"
            clampedStart == clampedEnd ->
                "النصف الثاني من ص ${firstPage.toArabicIndicDigits()}"
            firstPage == lastPage ->
                "صفحة ${firstPage.toArabicIndicDigits()} كاملة"
            else ->
                "ص ${firstPage.toArabicIndicDigits()} - ${lastPage.toArabicIndicDigits()}"
        }
        return SliceDesc(rangeTitle = rangeTitle, detailTitle = surahSummary, openPage = firstPage)
    }

    private fun describeSurahsAcrossPages(
        pages: List<Int>,
        ayahsByPage: Map<Int, List<AyahItem>>
    ): String {
        val firstSurah = ayahsByPage[pages.first()]?.firstOrNull()?.surahNameAr
        val lastSurah = ayahsByPage[pages.last()]?.lastOrNull()?.surahNameAr
        return when {
            firstSurah == null -> "المصحف الشريف"
            lastSurah == null || firstSurah == lastSurah -> "سورة $firstSurah"
            else -> "$firstSurah ← $lastSurah"
        }
    }

    private fun orderedPage(direction: WirdDirection, index0Based: Int): Int {
        val clamped = index0Based.coerceIn(0, TOTAL_PAGES - 1)
        return if (direction == WirdDirection.FATIHA_TO_NAS) clamped + 1 else TOTAL_PAGES - clamped
    }
}
