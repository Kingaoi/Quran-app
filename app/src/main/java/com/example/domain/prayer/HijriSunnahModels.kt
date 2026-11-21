package com.example.domain.prayer

import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import androidx.compose.runtime.Immutable
import com.example.domain.model.toAppDigits
import java.util.Calendar
import java.util.Date

enum class HijriSunnahCategory(val key: String, val titleAr: String) {
    FRIDAY_KAHF("friday_kahf", "سورة الكهف يوم الجمعة"),
    MONDAY_THURSDAY("mon_thu", "صيام الاثنين والخميس"),
    WHITE_DAYS("white_days", "صيام الأيام البيض (١٣-١٥)"),
    OCCASIONS("occasions", "المناسبات الهجرية والسنن السنوية")
}

@Immutable
data class HijriSunnahConfig(
    val enabled: Boolean = false,
    val fridayKahfEnabled: Boolean = true,
    val mondayThursdayEnabled: Boolean = true,
    val whiteDaysEnabled: Boolean = true,
    val occasionsEnabled: Boolean = true,
    val fastingReminderHour: Int = 20,
    val fastingReminderMinute: Int = 30,
    val morningOccasionHour: Int = 8,
    val morningOccasionMinute: Int = 30
)

@Immutable
data class HijriSunnahNotice(
    val id: String,
    val category: HijriSunnahCategory,
    val titleAr: String,
    val bodyAr: String,
    val triggerAtMillis: Long,
    val openKahfOnTap: Boolean = false
)

@Immutable
data class HijriDayInsight(
    val titleAr: String,
    val subtitleAr: String,
    val isSpecialOccasion: Boolean
)

object HijriSunnahCalculator {
    private val ISLAMIC_LOCALE = ULocale("ar@calendar=islamic-umalqura")

    fun inspectToday(
        hijriOffsetDays: Int = 0,
        nowMillis: Long = System.currentTimeMillis(),
        useWesternDigits: Boolean = false
    ): HijriDayInsight? {
        val gregorian = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val hijri = hijriForDate(nowMillis, hijriOffsetDays)
        val hMonth = hijri.get(android.icu.util.Calendar.MONTH)
        val hDay = hijri.get(android.icu.util.Calendar.DAY_OF_MONTH)
        val dow = gregorian.get(Calendar.DAY_OF_WEEK)

        val parts = mutableListOf<String>()
        var isSpecial = false

        when {
            hMonth == 8 && hDay == 1 -> { parts.add("غرة شهر رمضان المبارك"); isSpecial = true }
            hMonth == 8 && hDay in 21..29 && hDay % 2 == 1 -> { parts.add("ليلة وتريّة من العشر الأواخر"); isSpecial = true }
            hMonth == 9 && hDay == 1 -> { parts.add("عيد الفطر المبارك (يحرم صومه)"); isSpecial = true }
            hMonth == 11 && hDay == 9 -> { parts.add("يوم عرفة (يستحب صيامه لغير الحاج)"); isSpecial = true }
            hMonth == 11 && hDay == 10 -> { parts.add("عيد الأضحى المبارك (يحرم صومه)"); isSpecial = true }
            hMonth == 11 && hDay in 11..13 -> { parts.add("أيام التشريق المباركة"); isSpecial = true }
            hMonth == 11 && hDay in 1..8 -> { parts.add("من أيام عشر ذي الحجة المباركة"); isSpecial = true }
            hMonth == 0 && hDay == 10 -> { parts.add("يوم عاشوراء (يستحب صيامه)"); isSpecial = true }
            hMonth == 0 && hDay == 9 -> { parts.add("يوم تاسوعاء (يستحب صيامه مع عاشوراء)"); isSpecial = true }
        }
        if (dow == Calendar.FRIDAY) {
            parts.add("الجمعة: يستحب قراءة سورة الكهف والإكثار من الصلاة على النبي ﷺ")
        }
        if (hDay in 13..15 && !isForbiddenFastingDay(hMonth, hDay)) {
            parts.add("من الأيام البيض (${hDay.toAppDigits(useWesternDigits)})")
        } else if ((dow == Calendar.MONDAY || dow == Calendar.THURSDAY) && !isForbiddenFastingDay(hMonth, hDay)) {
            parts.add("سنّة صيام ${if (dow == Calendar.MONDAY) "الاثنين" else "الخميس"}")
        }

        if (parts.isEmpty()) return null
        return HijriDayInsight(
            titleAr = if (isSpecial) "مناسبة وسنّة اليوم" else "سنّة اليوم",
            subtitleAr = parts.joinToString(" • "),
            isSpecialOccasion = isSpecial
        )
    }

    fun computeNextNotices(
        config: HijriSunnahConfig,
        nowMillis: Long = System.currentTimeMillis(),
        hijriOffsetDays: Int = 0,
        lookaheadDays: Int = 35
    ): List<HijriSunnahNotice> {
        if (!config.enabled) return emptyList()
        val found = mutableListOf<HijriSunnahNotice>()

        if (config.fridayKahfEnabled) {
            findNextFridayKahf(config, nowMillis, lookaheadDays)?.let(found::add)
        }
        if (config.mondayThursdayEnabled) {
            findNextMonThuPrep(config, nowMillis, hijriOffsetDays, lookaheadDays)?.let(found::add)
        }
        if (config.whiteDaysEnabled) {
            findNextWhiteDayPrep(config, nowMillis, hijriOffsetDays, lookaheadDays)?.let(found::add)
        }
        if (config.occasionsEnabled) {
            findNextAnnualOccasion(config, nowMillis, hijriOffsetDays, lookaheadDays)?.let(found::add)
        }
        return found
    }

    private fun findNextFridayKahf(
        config: HijriSunnahConfig,
        nowMillis: Long,
        lookaheadDays: Int
    ): HijriSunnahNotice? {
        for (d in 0..lookaheadDays) {
            val trigger = triggerMillisOnDayOffset(
                nowMillis = nowMillis,
                dayOffset = d,
                hour = config.morningOccasionHour,
                minute = config.morningOccasionMinute
            )
            if (trigger <= nowMillis + 5_000L) continue
            val cal = Calendar.getInstance().apply { timeInMillis = trigger }
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                return HijriSunnahNotice(
                    id = HijriSunnahCategory.FRIDAY_KAHF.key,
                    category = HijriSunnahCategory.FRIDAY_KAHF,
                    titleAr = "سنن يوم الجمعة المبارك",
                    bodyAr = "لا تنسَ قراءة سورة الكهف والإكثار من الصلاة على النبي ﷺ والدعاء قبل الغروب.",
                    triggerAtMillis = trigger,
                    openKahfOnTap = true
                )
            }
        }
        return null
    }

    private fun findNextMonThuPrep(
        config: HijriSunnahConfig,
        nowMillis: Long,
        hijriOffsetDays: Int,
        lookaheadDays: Int
    ): HijriSunnahNotice? {
        for (d in 0..lookaheadDays) {
            val eveTrigger = triggerMillisOnDayOffset(
                nowMillis = nowMillis,
                dayOffset = d,
                hour = config.fastingReminderHour,
                minute = config.fastingReminderMinute
            )
            if (eveTrigger <= nowMillis + 5_000L) continue
            val tomorrowMillis = eveTrigger + ONE_DAY_MS
            val tomorrowDow = Calendar.getInstance().apply { timeInMillis = tomorrowMillis }.get(Calendar.DAY_OF_WEEK)
            if (tomorrowDow != Calendar.MONDAY && tomorrowDow != Calendar.THURSDAY) continue
            val hTomorrow = hijriForDate(tomorrowMillis, hijriOffsetDays)
            val hMonth = hTomorrow.get(android.icu.util.Calendar.MONTH)
            val hDay = hTomorrow.get(android.icu.util.Calendar.DAY_OF_MONTH)
            if (isForbiddenFastingDay(hMonth, hDay) || hMonth == 8) continue
            val dayLabel = if (tomorrowDow == Calendar.MONDAY) "الاثنين" else "الخميس"
            return HijriSunnahNotice(
                id = HijriSunnahCategory.MONDAY_THURSDAY.key,
                category = HijriSunnahCategory.MONDAY_THURSDAY,
                titleAr = "تذكير بصيام غدٍ $dayLabel",
                bodyAr = "تُعرَض الأعمال يومي الاثنين والخميس؛ اعقد النية واستعد للسحور إن تيسّر لك.",
                triggerAtMillis = eveTrigger
            )
        }
        return null
    }

    private fun findNextWhiteDayPrep(
        config: HijriSunnahConfig,
        nowMillis: Long,
        hijriOffsetDays: Int,
        lookaheadDays: Int
    ): HijriSunnahNotice? {
        for (d in 0..lookaheadDays) {
            val eveTrigger = triggerMillisOnDayOffset(
                nowMillis = nowMillis,
                dayOffset = d,
                hour = config.fastingReminderHour,
                minute = config.fastingReminderMinute
            )
            if (eveTrigger <= nowMillis + 5_000L) continue
            val tomorrowMillis = eveTrigger + ONE_DAY_MS
            val hTomorrow = hijriForDate(tomorrowMillis, hijriOffsetDays)
            val hMonth = hTomorrow.get(android.icu.util.Calendar.MONTH)
            val hDay = hTomorrow.get(android.icu.util.Calendar.DAY_OF_MONTH)
            if (hDay in 13..15 && !isForbiddenFastingDay(hMonth, hDay) && hMonth != 8) {
                val monthName = HIJRI_MONTH_NAMES.getOrElse(hMonth) { "الهجري" }
                return HijriSunnahNotice(
                    id = HijriSunnahCategory.WHITE_DAYS.key,
                    category = HijriSunnahCategory.WHITE_DAYS,
                    titleAr = "غداً من الأيام البيض (${hDay.toAppDigits(false)} $monthName)",
                    bodyAr = "يُستحب صيام الأيام البيض الثلاثة (١٣، ١٤، ١٥) من كل شهر هجري؛ تعدل صيام الدهر.",
                    triggerAtMillis = eveTrigger
                )
            }
        }
        return null
    }

    private fun findNextAnnualOccasion(
        config: HijriSunnahConfig,
        nowMillis: Long,
        hijriOffsetDays: Int,
        lookaheadDays: Int
    ): HijriSunnahNotice? {
        for (d in 0..lookaheadDays) {
            val morning = triggerMillisOnDayOffset(
                nowMillis = nowMillis,
                dayOffset = d,
                hour = config.morningOccasionHour,
                minute = config.morningOccasionMinute
            )
            val hToday = hijriForDate(morning, hijriOffsetDays)
            val hMonth = hToday.get(android.icu.util.Calendar.MONTH)
            val hDay = hToday.get(android.icu.util.Calendar.DAY_OF_MONTH)

            val match: Pair<String, String>? = when {
                hMonth == 0 && hDay == 9 -> "اليوم تاسوعاء وغداً عاشوراء" to "صيام تاسوعاء وعاشوراء من أفضل الصيام بعد رمضان."
                hMonth == 0 && hDay == 10 -> "اليوم يوم عاشوراء" to "يحتسب صيام يوم عاشوراء على الله أن يكفّر السنة التي قبله."
                hMonth == 8 && hDay == 1 -> "مبارك عليكم شهر رمضان" to "أقبل شهر القرآن والعتق من النيران؛ ابدأ وردك وتلاوتك بهمة."
                hMonth == 8 && hDay == 21 -> "دخول العشر الأواخر من رمضان" to "اجتهد في القيام والقرآن وتحرّ ليلة القدر في الليالي الوترية."
                hMonth == 9 && hDay == 2 -> "تذكير بصيام الست من شوال" to "من صام رمضان ثم أتبعه ستاً من شوال كان كصيام الدهر."
                hMonth == 11 && hDay == 1 -> "غرة عشر ذي الحجة" to "ما من أيام العمل الصالح فيها أحب إلى الله من هذه الأيام العشر."
                hMonth == 11 && hDay == 8 -> "يوم التروية وغداً عرفة" to "تهيّأ لصيام يوم عرفة غداً لغير الحاج وللإكثار من الذكر والتكبير."
                hMonth == 11 && hDay == 9 -> "اليوم يوم عرفة المبارك" to "أفضل أيام الدعاء وصيامه يكفّر السنة الماضية والباقية."
                else -> null
            }
            if (match != null && morning > nowMillis + 5_000L) {
                return HijriSunnahNotice(
                    id = HijriSunnahCategory.OCCASIONS.key,
                    category = HijriSunnahCategory.OCCASIONS,
                    titleAr = match.first,
                    bodyAr = match.second,
                    triggerAtMillis = morning
                )
            }
        }
        return null
    }

    private fun triggerMillisOnDayOffset(
        nowMillis: Long,
        dayOffset: Int,
        hour: Int,
        minute: Int
    ): Long {
        return Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, dayOffset)
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun hijriForDate(millis: Long, hijriOffsetDays: Int): IslamicCalendar {
        return IslamicCalendar(ISLAMIC_LOCALE).apply {
            time = Date(millis)
            if (hijriOffsetDays != 0) add(android.icu.util.Calendar.DAY_OF_MONTH, hijriOffsetDays)
        }
    }

    private fun isForbiddenFastingDay(hMonth: Int, hDay: Int): Boolean {
        return (hMonth == 9 && hDay == 1) || (hMonth == 11 && hDay in 10..13)
    }

    private const val ONE_DAY_MS = 24 * 60 * 60_000L

    private val HIJRI_MONTH_NAMES = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )
}
