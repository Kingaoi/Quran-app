package com.example.domain.prayer

import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import com.example.domain.model.localizeDigits
import com.example.domain.model.toArabicIndicDigits
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

object PrayerTimesCalculator {

    private const val KAABA_LAT = 21.422487
    private const val KAABA_LON = 39.826206

    fun calculateForDate(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        method: CalculationMethod,
        asrMethod: AsrJuristicMethod
    ): DailyPrayerTimes {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val timeZoneHours = calendar.timeZone.getOffset(calendar.timeInMillis) / 3_600_000.0

        val jd = julianDate(year, month, day) - longitude / (15.0 * 24.0)

        var fajr = 5.0
        var sunrise = 6.0
        var dhuhr = 12.0
        var asr = 15.5
        var maghrib = 18.0
        var isha = 19.5

        // Two refinement iterations for sub-minute solar declination accuracy
        repeat(2) {
            val fajrDay = jd + fajr / 24.0
            val sunriseDay = jd + sunrise / 24.0
            val dhuhrDay = jd + dhuhr / 24.0
            val asrDay = jd + asr / 24.0
            val maghribDay = jd + maghrib / 24.0
            val ishaDay = jd + isha / 24.0

            dhuhr = computeMidDay(dhuhrDay, longitude, timeZoneHours)
            fajr = computeSunAngleTime(fajrDay, latitude, method.fajrAngle, isMorning = true, dhuhr = dhuhr)
            sunrise = computeSunAngleTime(sunriseDay, latitude, 0.833, isMorning = true, dhuhr = dhuhr)
            asr = computeAsrTime(asrDay, latitude, asrMethod.shadowFactor, dhuhr = dhuhr)
            maghrib = computeSunAngleTime(maghribDay, latitude, 0.833, isMorning = false, dhuhr = dhuhr)

            isha = if (method.ishaMinutesAfterMaghrib != null) {
                maghrib + method.ishaMinutesAfterMaghrib / 60.0
            } else {
                computeSunAngleTime(ishaDay, latitude, method.ishaAngle ?: 17.0, isMorning = false, dhuhr = dhuhr)
            }
        }

        val dayBase = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val entries = listOf(
            buildEntry(PrayerName.FAJR, fajr, dayBase),
            buildEntry(PrayerName.SUNRISE, sunrise, dayBase),
            buildEntry(PrayerName.DHUHR, dhuhr + (1.0 / 60.0), dayBase),
            buildEntry(PrayerName.ASR, asr, dayBase),
            buildEntry(PrayerName.MAGHRIB, maghrib, dayBase),
            buildEntry(PrayerName.ISHA, isha, dayBase)
        )

        val maghribMillis = entries.first { it.prayer == PrayerName.MAGHRIB }.timestampMillis
        val nextFajrMillis = entries.first { it.prayer == PrayerName.FAJR }.timestampMillis + 86_400_000L
        val nightDuration = (nextFajrMillis - maghribMillis).coerceAtLeast(1L)
        val midnightMillis = maghribMillis + nightDuration / 2L
        val lastThirdMillis = maghribMillis + (nightDuration * 2L) / 3L

        return DailyPrayerTimes(
            entries = entries,
            midnightMillis = midnightMillis,
            lastThirdMillis = lastThirdMillis,
            qiblaBearingDegrees = calculateQiblaBearing(latitude, longitude)
        )
    }

    fun calculateQiblaBearing(latitude: Double, longitude: Double): Double {
        val phiK = Math.toRadians(KAABA_LAT)
        val lambdaK = Math.toRadians(KAABA_LON)
        val phi = Math.toRadians(latitude)
        val lambda = Math.toRadians(longitude)
        val deltaLambda = lambdaK - lambda
        val y = sin(deltaLambda) * cos(phiK)
        val x = cos(phi) * sin(phiK) - sin(phi) * cos(phiK) * cos(deltaLambda)
        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360.0) % 360.0
    }

    fun formatTime(timestampMillis: Long, timeZone: TimeZone, use24Hour: Boolean): String {
        val cal = Calendar.getInstance(timeZone).apply { timeInMillis = timestampMillis }
        val h24 = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)
        return formatHourMinute(h24, m, use24Hour)
    }

    fun formatHourMinute(hour24: Int, minute: Int, use24Hour: Boolean): String {
        val mStr = minute.toString().padStart(2, '0').toArabicDigits()
        return if (use24Hour) {
            val hStr = hour24.toString().padStart(2, '0').toArabicDigits()
            "$hStr:$mStr"
        } else {
            val isPm = hour24 >= 12
            val h12 = when (val mod = hour24 % 12) {
                0 -> 12
                else -> mod
            }
            val suffix = if (isPm) "م" else "ص"
            "${h12.toArabicIndicDigits()}:$mStr $suffix"
        }
    }

    fun formatCountdown(diffMillis: Long): String {
        val totalSec = (diffMillis / 1000L).coerceAtLeast(0L)
        val h = (totalSec / 3600L).toInt()
        val m = ((totalSec % 3600L) / 60L).toInt()
        val s = (totalSec % 60L).toInt()
        return "${h.toString().padStart(2, '0').toArabicDigits()}:" +
            "${m.toString().padStart(2, '0').toArabicDigits()}:" +
            s.toString().padStart(2, '0').toArabicDigits()
    }

    fun formatHijriDateAr(calendar: Calendar, hijriOffsetDays: Int = 0): String {
        val adjustedMillis = calendar.timeInMillis + hijriOffsetDays.coerceIn(-2, 2) * 86_400_000L
        val islamic = IslamicCalendar(ULocale("ar@calendar=islamic-umalqura")).apply {
            timeInMillis = adjustedMillis
        }
        val day = islamic.get(IslamicCalendar.DAY_OF_MONTH).toArabicIndicDigits()
        val monthIdx = islamic.get(IslamicCalendar.MONTH).coerceIn(0, 11)
        val year = islamic.get(IslamicCalendar.YEAR).toArabicIndicDigits()
        val months = listOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )
        return "$day ${months[monthIdx]} $year هـ"
    }

    fun formatGregorianDateAr(calendar: Calendar): String {
        val daysOfWeek = mapOf(
            Calendar.SATURDAY to "السبت",
            Calendar.SUNDAY to "الأحد",
            Calendar.MONDAY to "الإثنين",
            Calendar.TUESDAY to "الثلاثاء",
            Calendar.WEDNESDAY to "الأربعاء",
            Calendar.THURSDAY to "الخميس",
            Calendar.FRIDAY to "الجمعة"
        )
        val months = listOf(
            "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        )
        val dow = daysOfWeek[calendar.get(Calendar.DAY_OF_WEEK)] ?: ""
        val day = calendar.get(Calendar.DAY_OF_MONTH).toArabicIndicDigits()
        val month = months[calendar.get(Calendar.MONTH).coerceIn(0, 11)]
        val year = calendar.get(Calendar.YEAR).toArabicIndicDigits()
        return "$dow $day $month $year م"
    }

    private fun buildEntry(prayer: PrayerName, decimalHours: Double, dayBase: Calendar): PrayerTimeEntry {
        val normalized = ((decimalHours % 24.0) + 24.0) % 24.0
        val totalMinutes = kotlin.math.round(normalized * 60.0).toInt()
        val h = (totalMinutes / 60) % 24
        val m = totalMinutes % 60
        val entryCal = (dayBase.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
        }
        return PrayerTimeEntry(
            prayer = prayer,
            hour24 = h,
            minute = m,
            timestampMillis = entryCal.timeInMillis
        )
    }

    private fun computeMidDay(jd: Double, longitude: Double, tzHours: Double): Double {
        val (_, eqTime) = sunPosition(jd)
        return fixHour(12.0 + tzHours - longitude / 15.0 - eqTime)
    }

    private fun computeSunAngleTime(
        jd: Double,
        latitude: Double,
        angleDegrees: Double,
        isMorning: Boolean,
        dhuhr: Double
    ): Double {
        val (decl, _) = sunPosition(jd)
        val latRad = Math.toRadians(latitude)
        val declRad = Math.toRadians(decl)
        val targetRad = Math.toRadians(-angleDegrees)
        val cosH = ((sin(targetRad) - sin(latRad) * sin(declRad)) / (cos(latRad) * cos(declRad)))
            .coerceIn(-0.99999, 0.99999)
        val hourAngleHours = Math.toDegrees(acos(cosH)) / 15.0
        return if (isMorning) dhuhr - hourAngleHours else dhuhr + hourAngleHours
    }

    private fun computeAsrTime(
        jd: Double,
        latitude: Double,
        shadowFactor: Int,
        dhuhr: Double
    ): Double {
        val (decl, _) = sunPosition(jd)
        val latRad = Math.toRadians(latitude)
        val declRad = Math.toRadians(decl)
        val angleRad = atan(1.0 / (shadowFactor + tan(kotlin.math.abs(latRad - declRad))))
        val cosH = ((sin(angleRad) - sin(latRad) * sin(declRad)) / (cos(latRad) * cos(declRad)))
            .coerceIn(-0.99999, 0.99999)
        return dhuhr + Math.toDegrees(acos(cosH)) / 15.0
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val decl = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val eqTime = q / 15.0 - fixHour(ra)
        return decl to eqTime
    }

    private fun fixAngle(deg: Double): Double = ((deg % 360.0) + 360.0) % 360.0
    private fun fixHour(hr: Double): Double = ((hr % 24.0) + 24.0) % 24.0

    private fun String.toArabicDigits(): String = localizeDigits()
}
