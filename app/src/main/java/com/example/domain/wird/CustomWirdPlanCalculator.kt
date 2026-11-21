package com.example.domain.wird

import androidx.compose.runtime.Immutable
import com.example.domain.model.AyahItem
import com.example.domain.model.JuzInfo
import com.example.domain.model.SurahInfo
import com.example.domain.model.toArabicIndicDigits
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

@Immutable
data class CustomPlanMetrics(
    val totalAyahs: Int,
    val totalPagesExact: Double,
    val studyDaysCount: Int,
    val calendarSpanDays: Int,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val dailyPagesExact: Double,
    val dailyAyahsExact: Double,
    val dailyInUnit: Double,
    val perSessionInUnit: Double,
    val perSessionAyahs: Double
)

object CustomWirdPlanCalculator {
    private const val PAGES_TOTAL = 604.0
    private const val QUARTERS_TOTAL = 240.0
    private const val HIZBS_TOTAL = 60.0
    private const val JUZ_TOTAL = 30.0

    fun extractRangeAyahs(
        allAyahs: List<AyahItem>,
        direction: WirdDirection,
        fromSurah: Int,
        fromAyah: Int,
        toSurah: Int,
        toAyah: Int
    ): List<AyahItem> {
        if (allAyahs.isEmpty()) return emptyList()
        return if (direction == WirdDirection.FATIHA_TO_NAS) {
            val startKey = ayahRank(fromSurah, fromAyah)
            val endKey = ayahRank(toSurah, toAyah)
            val low = min(startKey, endKey)
            val high = max(startKey, endKey)
            allAyahs.filter { ayahRank(it.surahNumber, it.ayahNumber) in low..high }
        } else {
            val highSurah = max(fromSurah, toSurah)
            val lowSurah = min(fromSurah, toSurah)
            val bySurah = allAyahs.groupBy { it.surahNumber }
            buildList {
                for (s in highSurah downTo lowSurah) {
                    val sAyahs = bySurah[s].orEmpty()
                    val filtered = when {
                        highSurah == lowSurah -> sAyahs.filter { it.ayahNumber in min(fromAyah, toAyah)..max(fromAyah, toAyah) }
                        s == fromSurah -> sAyahs.filter { it.ayahNumber >= fromAyah }
                        s == toSurah -> sAyahs.filter { it.ayahNumber <= toAyah }
                        else -> sAyahs
                    }
                    addAll(filtered)
                }
            }
        }
    }

    fun calculateMetrics(
        config: NamedWirdPlanConfig,
        allAyahs: List<AyahItem>,
        surahs: List<SurahInfo>
    ): CustomPlanMetrics {
        val rangeAyahs = extractRangeAyahs(
            allAyahs, config.direction, config.fromSurah, config.fromAyah, config.toSurah, config.toAyah
        )
        val totalAyahs: Int
        val totalPages: Double
        if (rangeAyahs.isNotEmpty()) {
            totalAyahs = rangeAyahs.size
            val countsByPage = allAyahs.groupingBy { it.pageNumber }.eachCount()
            val rawPages = rangeAyahs.sumOf { ayah ->
                1.0 / (countsByPage[ayah.pageNumber] ?: 15).coerceAtLeast(1)
            }
            totalPages = (kotlin.math.round(rawPages * 10_000.0) / 10_000.0).coerceAtLeast(0.1)
        } else {
            val lowS = min(config.fromSurah, config.toSurah).coerceIn(1, 114)
            val highS = max(config.fromSurah, config.toSurah).coerceIn(1, 114)
            val fallbackSurahs = surahs.filter { it.number in lowS..highS }
            if (fallbackSurahs.isNotEmpty()) {
                totalAyahs = fallbackSurahs.sumOf { it.ayahCount }.coerceAtLeast(1)
                totalPages = ((fallbackSurahs.maxOf { it.lastPage } - fallbackSurahs.minOf { it.firstPage } + 1))
                    .toDouble().coerceAtLeast(1.0)
            } else {
                val spanSurahs = (highS - lowS + 1).coerceAtLeast(1)
                totalPages = (spanSurahs * (PAGES_TOTAL / 114.0)).coerceAtLeast(1.0)
                totalAyahs = (spanSurahs * 54).coerceAtLeast(1)
            }
        }

        val totalUnits = convertPagesToUnit(totalPages, totalAyahs, config.amountUnit).coerceAtLeast(0.1)
        val mask = if (config.activeDaysMask == 0) ALL_WEEK_DAYS_MASK else config.activeDaysMask
        val safeStart = if (config.startEpochDay > 0L) config.startEpochDay else currentEpochDay()

        val studyDays: Int
        val resolvedEndDay: Long
        when (config.goalType) {
            WirdGoalType.AMOUNT -> {
                val perDay = config.amountValue.toDouble().coerceAtLeast(0.1)
                studyDays = ceil((totalUnits / perDay) - 1e-6).toInt().coerceIn(1, 1825)
                resolvedEndDay = advanceByActiveStudyDays(safeStart, studyDays, mask)
            }
            WirdGoalType.DURATION -> {
                val rawDays = config.targetDurationDays.coerceAtLeast(1) * config.durationUnit.daysMultiplier
                val targetCalendarDays = rawDays.coerceIn(1, 1825)
                resolvedEndDay = safeStart + targetCalendarDays - 1L
                studyDays = countActiveDaysInRange(safeStart, resolvedEndDay, mask).coerceAtLeast(1)
            }
            WirdGoalType.DATE_RANGE -> {
                resolvedEndDay = max(safeStart, config.endEpochDay)
                studyDays = countActiveDaysInRange(safeStart, resolvedEndDay, mask).coerceAtLeast(1)
            }
        }

        val calendarSpan = (resolvedEndDay - safeStart + 1L).toInt().coerceAtLeast(1)
        val dailyPages = totalPages / studyDays.toDouble()
        val dailyAyahs = totalAyahs.toDouble() / studyDays.toDouble()
        val dailyInUnit = if (config.goalType == WirdGoalType.AMOUNT) {
            config.amountValue.toDouble().coerceAtLeast(0.1)
        } else {
            totalUnits / studyDays.toDouble()
        }
        val sessions = config.sessionsPerDay.coerceIn(1, 5).toDouble()

        return CustomPlanMetrics(
            totalAyahs = totalAyahs,
            totalPagesExact = totalPages,
            studyDaysCount = studyDays,
            calendarSpanDays = calendarSpan,
            startEpochDay = safeStart,
            endEpochDay = resolvedEndDay,
            dailyPagesExact = dailyPages,
            dailyAyahsExact = dailyAyahs,
            dailyInUnit = dailyInUnit,
            perSessionInUnit = dailyInUnit / sessions,
            perSessionAyahs = dailyAyahs / sessions
        )
    }

    fun buildPlan(
        config: NamedWirdPlanConfig,
        allAyahs: List<AyahItem>,
        ajza: List<JuzInfo>,
        todayEpochDay: Long
    ): WirdPlanSummary? {
        if (allAyahs.isEmpty()) return null
        val rangeAyahs = extractRangeAyahs(
            allAyahs, config.direction, config.fromSurah, config.fromAyah, config.toSurah, config.toAyah
        )
        if (rangeAyahs.isEmpty()) return null

        val metrics = calculateMetrics(config, allAyahs, emptyList())
        val totalPortions = metrics.studyDaysCount.coerceIn(1, rangeAyahs.size)
        val juzNameByNum = ajza.associate { it.number to it.nameAr }
        val countsByPage = allAyahs.groupingBy { it.pageNumber }.eachCount()

        val cumWeights = DoubleArray(rangeAyahs.size)
        var acc = 0.0
        rangeAyahs.forEachIndexed { idx, ayah ->
            acc += if (config.amountUnit == WirdAmountUnit.AYAH) 1.0
            else 1.0 / (countsByPage[ayah.pageNumber] ?: 15).coerceAtLeast(1)
            cumWeights[idx] = acc
        }
        val totalWeight = acc.coerceAtLeast(1.0)

        val portions = ArrayList<WirdPortion>(totalPortions)
        var cursor = 0
        for (pIdx in 0 until totalPortions) {
            if (cursor >= rangeAyahs.size) break
            val targetWeight = totalWeight * ((pIdx + 1).toDouble() / totalPortions.toDouble())
            var endIdx = cursor
            if (pIdx == totalPortions - 1) {
                endIdx = rangeAyahs.lastIndex
            } else {
                val maxAllowedIdx = (rangeAyahs.size - 1 - (totalPortions - 1 - pIdx)).coerceAtLeast(cursor)
                while (endIdx < maxAllowedIdx && cumWeights[endIdx] < targetWeight) {
                    endIdx++
                }
            }
            val startAyah = rangeAyahs[cursor]
            val endAyah = rangeAyahs[endIdx]
            val pageSpan = kotlin.math.abs(endAyah.pageNumber - startAyah.pageNumber) + 1
            portions.add(
                WirdPortion(
                    index = portions.size,
                    juzNumber = startAyah.juzNumber,
                    juzNameAr = juzNameByNum[startAyah.juzNumber] ?: "الجزء ${startAyah.juzNumber.toArabicIndicDigits()}",
                    startSurahNumber = startAyah.surahNumber,
                    startSurahNameAr = startAyah.surahNameAr,
                    startAyahNumber = startAyah.ayahNumber,
                    startPage = startAyah.pageNumber,
                    endSurahNumber = endAyah.surahNumber,
                    endSurahNameAr = endAyah.surahNameAr,
                    endAyahNumber = endAyah.ayahNumber,
                    endPage = endAyah.pageNumber,
                    openingAyahText = formatOpeningExcerpt(startAyah.text),
                    quartersCount = ((pageSpan * 240) / 604).coerceAtLeast(1)
                )
            )
            cursor = endIdx + 1
        }

        val total = portions.size
        val completedSet = if (config.completedPortionDays.isNotEmpty()) {
            config.completedPortionDays.keys.filterTo(HashSet()) { it in 0 until total }
        } else {
            (0 until config.completedWirdsCount.coerceIn(0, total)).toHashSet()
        }
        val completed = completedSet.size
        val isCompleted = total > 0 && completed >= total
        val current = portions.firstOrNull { it.index !in completedSet }
        val previous = portions.filter { it.index in completedSet }
        val upcoming = portions.filter { it.index !in completedSet && it.index != current?.index }

        val expectedByToday = if (config.startEpochDay > 0L && todayEpochDay > config.startEpochDay) {
            countActiveDaysInRange(config.startEpochDay, todayEpochDay - 1L, config.activeDaysMask)
        } else 0
        val delta = completed - expectedByToday
        val scheduleStatus = when {
            isCompleted -> WirdScheduleStatus.OnTrack
            delta < 0 -> WirdScheduleStatus.Behind(-delta)
            delta > 0 -> WirdScheduleStatus.Ahead(delta)
            else -> WirdScheduleStatus.OnTrack
        }

        val hasStarted = current != null && config.lastProgressPage != 0 && config.lastProgressPage != current.startPage
        val resumePage = when {
            current == null -> 1
            config.lastProgressPage in min(current.startPage, current.endPage)..max(current.startPage, current.endPage) -> config.lastProgressPage
            else -> current.startPage
        }

        return WirdPlanSummary(
            allWirds = portions,
            totalWirds = total,
            completedCount = completed,
            upcomingCount = upcoming.size,
            currentWird = current,
            previousWirds = previous,
            upcomingWirds = upcoming,
            isCompleted = isCompleted,
            progressFraction = if (total == 0) 0f else (completed.toFloat() / total.toFloat()).coerceIn(0f, 1f),
            scheduleStatus = scheduleStatus,
            resumePage = resumePage,
            hasStartedCurrentWird = hasStarted
        )
    }

    fun convertPagesToUnit(pages: Double, ayahs: Int, unit: WirdAmountUnit): Double = when (unit) {
        WirdAmountUnit.PAGE -> pages
        WirdAmountUnit.QUARTER -> pages * (QUARTERS_TOTAL / PAGES_TOTAL)
        WirdAmountUnit.HIZB -> pages * (HIZBS_TOTAL / PAGES_TOTAL)
        WirdAmountUnit.JUZ -> pages * (JUZ_TOTAL / PAGES_TOTAL)
        WirdAmountUnit.AYAH -> ayahs.toDouble()
    }

    fun advanceByActiveStudyDays(startEpochDay: Long, studyDaysNeeded: Int, mask: Int) =
        CustomWirdFormatters.advanceByActiveStudyDays(startEpochDay, studyDaysNeeded, mask)

    fun countActiveDaysInRange(startEpochDay: Long, endEpochDay: Long, mask: Int) =
        CustomWirdFormatters.countActiveDaysInRange(startEpochDay, endEpochDay, mask)

    fun currentEpochDay() = CustomWirdFormatters.currentEpochDay()
    fun formatEpochDayAr(epochDay: Long) = CustomWirdFormatters.formatEpochDayAr(epochDay)
    fun formatEpochDayWithWeekdayAr(epochDay: Long) = CustomWirdFormatters.formatEpochDayWithWeekdayAr(epochDay)
    fun formatDurationUnitAr(count: Int, unit: WirdDurationUnit) = CustomWirdFormatters.formatDurationUnitAr(count, unit)
    fun formatSessionsCountAr(count: Int) = CustomWirdFormatters.formatSessionsCountAr(count)
    fun formatAmountAr(value: Double, unit: WirdAmountUnit) = CustomWirdFormatters.formatAmountAr(value, unit)
    fun formatTime12hAr(hour: Int, minute: Int) = CustomWirdFormatters.formatTime12hAr(hour, minute)
    fun formatReminderLeadAr(leadMinutes: Int) = CustomWirdFormatters.formatReminderLeadAr(leadMinutes)

    private fun ayahRank(surah: Int, ayah: Int): Int = surah * 1000 + ayah

    private fun formatOpeningExcerpt(rawText: String): String {
        val cleaned = rawText.replace("\u06DD", "").replace(Regex("\\s+"), " ").trim()
        val words = cleaned.split(' ').filter { it.isNotBlank() }
        return if (words.size <= 5) cleaned else words.take(5).joinToString(" ") + " ..."
    }
}
