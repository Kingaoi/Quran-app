package com.example.domain.wird

import com.example.domain.model.AyahItem
import com.example.domain.model.HizbQuarterInfo
import com.example.domain.model.JuzInfo
import com.example.domain.model.toArabicIndicDigits
import kotlin.math.ceil

object WirdPlanCalculator {
    const val TOTAL_QUARTERS = 240
    const val QUARTERS_PER_JUZ = 8
    const val MAX_JUZ_PER_DAY = 10

    fun remainingQuartersFromJuz(startJuz: Int): Int {
        val juz = startJuz.coerceIn(1, 30)
        return (31 - juz) * QUARTERS_PER_JUZ
    }

    fun quartersFromDays(totalQuarters: Int, days: Int): Int {
        val safeTotal = totalQuarters.coerceAtLeast(1)
        val safeDays = days.coerceIn(1, safeTotal)
        return ceil(safeTotal.toDouble() / safeDays.toDouble()).toInt().coerceIn(1, safeTotal)
    }

    fun daysFromQuarters(totalQuarters: Int, quartersPerDay: Int): Int {
        val safeTotal = totalQuarters.coerceAtLeast(1)
        val safeStep = quartersPerDay.coerceIn(1, safeTotal)
        return ceil(safeTotal.toDouble() / safeStep.toDouble()).toInt().coerceAtLeast(1)
    }

    fun buildPlan(
        config: KhatmahPlanConfig,
        quarters: List<HizbQuarterInfo>,
        allAyahs: List<AyahItem>,
        ajza: List<JuzInfo>,
        todayEpochDay: Long
    ): WirdPlanSummary? {
        if (!config.isActive || quarters.isEmpty() || allAyahs.isEmpty()) return null

        val juzNameByNum = ajza.associate { it.number to it.nameAr }
        val ayahIdxByKey = HashMap<Long, Int>(allAyahs.size * 2)
        allAyahs.forEachIndexed { idx, ayah ->
            ayahIdxByKey[ayahKey(ayah.surahNumber, ayah.ayahNumber)] = idx
        }

        val startQuarterIdx = ((config.startJuz.coerceIn(1, 30) - 1) * QUARTERS_PER_JUZ)
            .coerceIn(0, quarters.lastIndex)
        val step = config.quartersPerDay.coerceIn(1, quarters.size - startQuarterIdx)

        val portions = ArrayList<WirdPortion>()
        var cursor = startQuarterIdx
        var wirdIndex = 0
        while (cursor < quarters.size) {
            val endQuarterIdx = (cursor + step - 1).coerceAtMost(quarters.lastIndex)
            val startQ = quarters[cursor]
            val endAyah = if (endQuarterIdx >= quarters.lastIndex) {
                allAyahs.last()
            } else {
                val nextQ = quarters[endQuarterIdx + 1]
                val nextAyahPos = ayahIdxByKey[ayahKey(nextQ.surahNumber, nextQ.ayahNumber)]
                if (nextAyahPos != null && nextAyahPos > 0) {
                    allAyahs[nextAyahPos - 1]
                } else {
                    allAyahs.last()
                }
            }

            portions.add(
                WirdPortion(
                    index = wirdIndex,
                    juzNumber = startQ.juzNumber,
                    juzNameAr = juzNameByNum[startQ.juzNumber] ?: "الجزء ${startQ.juzNumber.toArabicIndicDigits()}",
                    startSurahNumber = startQ.surahNumber,
                    startSurahNameAr = startQ.surahNameAr,
                    startAyahNumber = startQ.ayahNumber,
                    startPage = startQ.pageNumber,
                    endSurahNumber = endAyah.surahNumber,
                    endSurahNameAr = endAyah.surahNameAr,
                    endAyahNumber = endAyah.ayahNumber,
                    endPage = endAyah.pageNumber,
                    openingAyahText = formatOpeningExcerpt(startQ.openingText),
                    quartersCount = endQuarterIdx - cursor + 1
                )
            )
            cursor = endQuarterIdx + 1
            wirdIndex++
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

        val elapsedDays = if (config.startEpochDay > 0L && todayEpochDay >= config.startEpochDay) {
            (todayEpochDay - config.startEpochDay).toInt()
        } else {
            0
        }
        val delta = completed - elapsedDays
        val scheduleStatus = when {
            isCompleted -> WirdScheduleStatus.OnTrack
            delta < 0 -> WirdScheduleStatus.Behind(-delta)
            delta > 0 -> WirdScheduleStatus.Ahead(delta)
            else -> WirdScheduleStatus.OnTrack
        }

        val hasStarted = current != null &&
            config.lastProgressPage in (current.startPage + 1)..current.endPage
        val resumePage = when {
            current == null -> 1
            config.lastProgressPage in current.startPage..current.endPage -> config.lastProgressPage
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

    fun formatStartPointAr(startJuz: Int, ajza: List<JuzInfo>): String {
        val juz = startJuz.coerceIn(1, 30)
        if (juz == 1) return "بداية المصحف"
        return ajza.firstOrNull { it.number == juz }?.nameAr
            ?: "الجزء ${juz.toArabicIndicDigits()}"
    }

    fun formatJuzCountAr(juzCount: Int): String = when (juzCount) {
        0 -> "-"
        1 -> "جزء"
        2 -> "جزءان"
        in 3..10 -> "${juzCount.toArabicIndicDigits()} أجزاء"
        else -> "${juzCount.toArabicIndicDigits()} جزءاً"
    }

    fun formatRubCountAr(rubCount: Int): String = when (rubCount) {
        0 -> "-"
        1 -> "ربع"
        2 -> "ربعان"
        3 -> "٣ أرباع"
        4 -> "حزب"
        5 -> "٥ أرباع"
        6 -> "٦ أرباع"
        7 -> "٧ أرباع"
        else -> "${rubCount.toArabicIndicDigits()} أرباع"
    }

    fun formatDurationDaysAr(days: Int): String = when {
        days <= 1 -> "يوم واحد"
        days == 2 -> "يومان"
        days in 3..10 -> "${days.toArabicIndicDigits()} أيام"
        else -> "${days.toArabicIndicDigits()} يوماً"
    }

    fun formatScheduleStatusAr(status: WirdScheduleStatus): String = when (status) {
        WirdScheduleStatus.OnTrack -> "أنت مواكب لختمتك بانتظام"
        is WirdScheduleStatus.Behind -> when (status.days) {
            1 -> "أنت متأخر عن ختمتك بيوم واحد"
            2 -> "أنت متأخر عن ختمتك بيومين"
            in 3..10 -> "أنت متأخر عن ختمتك بـ ${status.days.toArabicIndicDigits()} أيام"
            else -> "أنت متأخر عن ختمتك بـ ${status.days.toArabicIndicDigits()} يوماً"
        }
        is WirdScheduleStatus.Ahead -> when (status.days) {
            1 -> "أنت متقدم عن ختمتك بيوم واحد"
            2 -> "أنت متقدم عن ختمتك بيومين"
            in 3..10 -> "أنت متقدم عن ختمتك بـ ${status.days.toArabicIndicDigits()} أيام"
            else -> "أنت متقدم عن ختمتك بـ ${status.days.toArabicIndicDigits()} يوماً"
        }
    }

    fun formatDailyPortionSummaryAr(quartersPerDay: Int): String {
        val clamped = quartersPerDay.coerceAtLeast(1)
        val juzCount = clamped / QUARTERS_PER_JUZ
        val rubCount = clamped % QUARTERS_PER_JUZ
        val juzLabel = when (juzCount) {
            0 -> null
            1 -> "جزء واحد"
            2 -> "جزءان"
            in 3..10 -> "${juzCount.toArabicIndicDigits()} أجزاء"
            else -> "${juzCount.toArabicIndicDigits()} جزءاً"
        }
        val rubLabel = if (rubCount == 0) null else formatRubCountAr(rubCount)
        return when {
            juzLabel != null && rubLabel != null -> "$juzLabel و$rubLabel"
            juzLabel != null -> juzLabel
            rubLabel != null -> rubLabel
            else -> "ربع واحد"
        }
    }

    private fun formatOpeningExcerpt(rawText: String): String {
        val cleaned = rawText
            .replace("\u06DD", "")
            .replace(Regex("\\s+"), " ")
            .trim()
        val words = cleaned.split(' ').filter { it.isNotBlank() }
        if (words.size <= 5) return cleaned
        return words.take(5).joinToString(" ") + " ..."
    }

    private fun ayahKey(surah: Int, ayah: Int): Long = (surah.toLong() shl 16) or (ayah.toLong() and 0xFFFFL)
}
