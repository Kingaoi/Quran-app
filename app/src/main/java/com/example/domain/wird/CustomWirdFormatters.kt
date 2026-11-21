package com.example.domain.wird

import com.example.domain.model.toArabicIndicDigits
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.roundToInt

object CustomWirdFormatters {
    private val UTC_TZ: TimeZone = TimeZone.getTimeZone("UTC")
    private val ArabicMonthNames = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )
    private val ArabicWeekdayNames = mapOf(
        Calendar.SUNDAY to "الأحد",
        Calendar.MONDAY to "الاثنين",
        Calendar.TUESDAY to "الثلاثاء",
        Calendar.WEDNESDAY to "الأربعاء",
        Calendar.THURSDAY to "الخميس",
        Calendar.FRIDAY to "الجمعة",
        Calendar.SATURDAY to "السبت"
    )

    fun advanceByActiveStudyDays(startEpochDay: Long, studyDaysNeeded: Int, mask: Int): Long {
        val safeMask = if (mask == 0) ALL_WEEK_DAYS_MASK else mask
        var remaining = studyDaysNeeded.coerceAtLeast(1)
        var day = startEpochDay
        var guard = 0
        while (guard < 4000) {
            if (isDayActiveInMask(safeMask, dayOfWeekForEpochDay(day))) {
                remaining--
                if (remaining <= 0) return day
            }
            day++
            guard++
        }
        return startEpochDay + studyDaysNeeded - 1L
    }

    fun countActiveDaysInRange(startEpochDay: Long, endEpochDay: Long, mask: Int): Int {
        if (endEpochDay < startEpochDay) return 0
        val safeMask = if (mask == 0) ALL_WEEK_DAYS_MASK else mask
        var count = 0
        var day = startEpochDay
        val limit = min(endEpochDay, startEpochDay + 3650L)
        while (day <= limit) {
            if (isDayActiveInMask(safeMask, dayOfWeekForEpochDay(day))) count++
            day++
        }
        return count
    }

    fun dayOfWeekForEpochDay(epochDay: Long): Int {
        val cal = Calendar.getInstance(UTC_TZ)
        cal.timeInMillis = TimeUnit.DAYS.toMillis(epochDay)
        return cal.get(Calendar.DAY_OF_WEEK)
    }

    fun currentEpochDay(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val offset = cal.timeZone.getOffset(cal.timeInMillis)
        return TimeUnit.MILLISECONDS.toDays(cal.timeInMillis + offset)
    }

    fun yearOfEpochDay(epochDay: Long): Int {
        val cal = Calendar.getInstance(UTC_TZ).apply {
            timeInMillis = TimeUnit.DAYS.toMillis(epochDay)
        }
        return cal.get(Calendar.YEAR)
    }

    fun formatEpochDayAr(epochDay: Long): String {
        val cal = Calendar.getInstance(UTC_TZ).apply {
            timeInMillis = TimeUnit.DAYS.toMillis(epochDay)
        }
        val d = cal.get(Calendar.DAY_OF_MONTH).toArabicIndicDigits()
        val m = ArabicMonthNames.getOrElse(cal.get(Calendar.MONTH)) { "" }
        val y = cal.get(Calendar.YEAR).toArabicIndicDigits()
        return "$d $m $y"
    }

    fun formatCompactEpochDayAr(epochDay: Long, referenceEpochDay: Long): String {
        val cal = Calendar.getInstance(UTC_TZ).apply {
            timeInMillis = TimeUnit.DAYS.toMillis(epochDay)
        }
        val d = cal.get(Calendar.DAY_OF_MONTH).toArabicIndicDigits()
        val m = ArabicMonthNames.getOrElse(cal.get(Calendar.MONTH)) { "" }
        val year = cal.get(Calendar.YEAR)
        return if (year == yearOfEpochDay(referenceEpochDay)) {
            "$d $m"
        } else {
            "$d $m ${year.toArabicIndicDigits()}"
        }
    }

    fun formatEpochDayWithWeekdayAr(epochDay: Long): String {
        val cal = Calendar.getInstance(UTC_TZ).apply {
            timeInMillis = TimeUnit.DAYS.toMillis(epochDay)
        }
        val weekday = ArabicWeekdayNames[cal.get(Calendar.DAY_OF_WEEK)].orEmpty()
        return "$weekday ${formatEpochDayAr(epochDay)}".trim()
    }

    fun formatCompactWeekdayDateAr(epochDay: Long, referenceEpochDay: Long): String {
        val cal = Calendar.getInstance(UTC_TZ).apply {
            timeInMillis = TimeUnit.DAYS.toMillis(epochDay)
        }
        val weekday = ArabicWeekdayNames[cal.get(Calendar.DAY_OF_WEEK)].orEmpty()
        return "$weekday ${formatCompactEpochDayAr(epochDay, referenceEpochDay)}".trim()
    }

    fun formatRelativeDayAgeAr(completedEpochDay: Long, todayEpochDay: Long): String {
        val diff = (todayEpochDay - completedEpochDay).toInt()
        return when {
            diff <= 0 -> "اليوم"
            diff == 1 -> "أمس"
            diff == 2 -> "منذ يومين"
            diff in 3..10 -> "منذ ${diff.toArabicIndicDigits()} أيام"
            else -> "منذ ${diff.toArabicIndicDigits()} يوماً"
        }
    }

    fun formatCadenceAr(activeDaysMask: Int): String {
        val safeMask = if (activeDaysMask == 0) ALL_WEEK_DAYS_MASK else activeDaysMask
        val count = (Calendar.SUNDAY..Calendar.SATURDAY).count { isDayActiveInMask(safeMask, it) }
        return when (count) {
            7 -> "كل يوم"
            1 -> "يوم واحد أسبوعياً"
            2 -> "يومان أسبوعياً"
            else -> "${count.toArabicIndicDigits()} أيام أسبوعياً"
        }
    }

    fun formatDurationUnitAr(count: Int, unit: WirdDurationUnit): String = when (unit) {
        WirdDurationUnit.DAY -> formatArabicCountedNoun(count, "يوم", "يومان", "أيام", "يوماً")
        WirdDurationUnit.WEEK -> formatArabicCountedNoun(count, "أسبوع", "أسبوعان", "أسابيع", "أسبوعاً")
        WirdDurationUnit.MONTH -> formatArabicCountedNoun(count, "شهر", "شهران", "أشهر", "شهراً")
    }

    fun formatSessionsCountAr(count: Int): String = when (count) {
        1 -> "جلسة واحدة"
        2 -> "جلستان"
        in 3..5 -> "${count.toArabicIndicDigits()} جلسات"
        else -> "${count.toArabicIndicDigits()} جلسة"
    }

    fun formatArabicCountedNoun(
        count: Int,
        singular: String,
        dual: String,
        plural3to10: String,
        accusative11Plus: String
    ): String {
        val n = count.coerceAtLeast(1)
        val mod100 = n % 100
        return when {
            n == 1 -> "$singular واحد"
            n == 2 -> dual
            mod100 in 3..10 -> "${n.toArabicIndicDigits()} $plural3to10"
            mod100 in 11..99 -> "${n.toArabicIndicDigits()} $accusative11Plus"
            else -> "${n.toArabicIndicDigits()} $singular"
        }
    }

    fun formatAmountAr(value: Double, unit: WirdAmountUnit): String {
        val rounded = (value * 10.0).roundToInt() / 10.0
        val whole = rounded.toInt()
        val isWhole = kotlin.math.abs(rounded - whole) < 0.05
        if (isWhole) {
            val digits = whole.toArabicIndicDigits()
            return when (unit) {
                WirdAmountUnit.PAGE -> when {
                    whole == 1 -> "١ وجه"
                    whole == 2 -> "وجهان"
                    whole % 100 in 3..10 -> "$digits أوجه"
                    whole % 100 in 11..99 -> "$digits وجهاً"
                    else -> "$digits وجه"
                }
                WirdAmountUnit.AYAH -> when {
                    whole == 1 -> "١ آية"
                    whole == 2 -> "آيتان"
                    whole % 100 in 3..10 -> "$digits آيات"
                    whole % 100 in 11..99 -> "$digits آية"
                    else -> "$digits آية"
                }
                WirdAmountUnit.QUARTER -> when {
                    whole == 1 -> "١ ربع"
                    whole == 2 -> "ربعان"
                    whole % 100 in 3..10 -> "$digits أرباع"
                    else -> "$digits ربعاً"
                }
                WirdAmountUnit.HIZB -> when {
                    whole == 1 -> "١ حزب"
                    whole == 2 -> "حزبان"
                    whole % 100 in 3..10 -> "$digits أحزاب"
                    else -> "$digits حزباً"
                }
                WirdAmountUnit.JUZ -> when {
                    whole == 1 -> "١ جزء"
                    whole == 2 -> "جزءان"
                    whole % 100 in 3..10 -> "$digits أجزاء"
                    else -> "$digits جزءاً"
                }
            }
        }
        val intPart = whole.toArabicIndicDigits()
        val dec = ((rounded - whole) * 10.0).roundToInt().coerceIn(1, 9).toArabicIndicDigits()
        return "$intPart٫$dec ${unit.singularAr}"
    }

    fun formatTime12hAr(hour: Int, minute: Int): String {
        val h = hour.coerceIn(0, 23)
        val m = minute.coerceIn(0, 59)
        val isPm = h >= 12
        val h12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        val suffix = if (isPm) "م" else "ص"
        val mStr = m.toString().padStart(2, '0').map { c ->
            (c - '0').toArabicIndicDigits()
        }.joinToString("")
        return "${h12.toArabicIndicDigits()}:$mStr $suffix"
    }

    fun formatReminderLeadAr(leadMinutes: Int): String = when (leadMinutes) {
        0 -> "في وقتها"
        60 -> "قبل ساعة"
        else -> "قبل ${leadMinutes.toArabicIndicDigits()} د"
    }
}
