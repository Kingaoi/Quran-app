package com.example.domain.prayer

import androidx.compose.runtime.Immutable
import java.util.TimeZone
import kotlin.math.abs

enum class PrayerName(
    val id: String,
    val titleAr: String,
    val isMandatoryPrayer: Boolean
) {
    FAJR("fajr", "الفجر", true),
    SUNRISE("sunrise", "الشروق", false),
    DHUHR("dhuhr", "الظهر", true),
    ASR("asr", "العصر", true),
    MAGHRIB("maghrib", "المغرب", true),
    ISHA("isha", "العشاء", true)
}

enum class CalculationMethod(
    val titleAr: String,
    val subtitleAr: String,
    val fajrAngle: Double,
    val ishaAngle: Double? = null,
    val ishaMinutesAfterMaghrib: Int? = null
) {
    UMM_AL_QURA(
        titleAr = "أم القرى (مكة)",
        subtitleAr = "الفجر ١٨.٥° • العشاء ٩٠ دقيقة",
        fajrAngle = 18.5,
        ishaMinutesAfterMaghrib = 90
    ),
    MWL(
        titleAr = "رابطة العالم الإسلامي",
        subtitleAr = "الفجر ١٨° • العشاء ١٧°",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    EGYPT(
        titleAr = "الهيئة المصرية العامة",
        subtitleAr = "الفجر ١٩.٥° • العشاء ١٧.٥°",
        fajrAngle = 19.5,
        ishaAngle = 17.5
    ),
    DUBAI(
        titleAr = "الإمارات ودبي",
        subtitleAr = "الفجر ١٨.٢° • العشاء ١٨.٢°",
        fajrAngle = 18.2,
        ishaAngle = 18.2
    ),
    KARACHI(
        titleAr = "جامعة كراتشي",
        subtitleAr = "الفجر ١٨° • العشاء ١٨°",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    ),
    ISNA(
        titleAr = "أمريكا الشمالية (ISNA)",
        subtitleAr = "الفجر ١٥° • العشاء ١٥°",
        fajrAngle = 15.0,
        ishaAngle = 15.0
    )
}

enum class AsrJuristicMethod(val titleAr: String, val shadowFactor: Int) {
    SHAFII("الجمهور (الشافعي والمالكي والحنبلي)", 1),
    HANAFI("المذهب الحنفي", 2)
}

enum class PrayerRegion(val titleAr: String) {
    GULF("السعودية والخليج"),
    LEVANT_IRAQ("الشام والعراق"),
    EGYPT_RED_SEA("مصر والسودان واليمن"),
    MAGHREB("المغرب العربي"),
    ASIA_TURKEY("تركيا وآسيا"),
    EUROPE("أوروبا"),
    AMERICAS_OCEANIA("الأمريكتان وأوقيانوسيا")
}

@Immutable
data class CityLocation(
    val nameAr: String,
    val countryAr: String,
    val latitude: Double,
    val longitude: Double,
    val region: PrayerRegion = PrayerRegion.GULF,
    val timeZoneId: String = "",
    val isFromGps: Boolean = false
) {
    fun resolveTimeZone(): TimeZone {
        val candidate = timeZoneId.ifBlank {
            PresetCities.findTimeZoneId(nameAr, countryAr)
        }
        if (candidate.isNotBlank()) {
            val tz = TimeZone.getTimeZone(candidate)
            if (candidate.equals("GMT", ignoreCase = true) ||
                candidate.equals("UTC", ignoreCase = true) ||
                tz.id != "GMT"
            ) {
                return tz
            }
        }
        return TimeZone.getDefault()
    }
}

@Immutable
data class PrayerTimeEntry(
    val prayer: PrayerName,
    val hour24: Int,
    val minute: Int,
    val timestampMillis: Long
)

@Immutable
data class DailyPrayerTimes(
    val entries: List<PrayerTimeEntry>,
    val midnightMillis: Long,
    val lastThirdMillis: Long,
    val qiblaBearingDegrees: Double
)

fun normalizeArabicQuery(text: String): String {
    val sb = StringBuilder(text.length)
    for (ch in text.trim()) {
        when (ch) {
            '\u0640' -> Unit
            in '\u064B'..'\u065F', '\u0670', in '\u06D6'..'\u06ED' -> Unit
            'أ', 'إ', 'آ', 'ٱ' -> sb.append('ا')
            'ة' -> sb.append('ه')
            'ى' -> sb.append('ي')
            'ؤ' -> sb.append('و')
            'ئ' -> sb.append('ي')
            else -> sb.append(ch.lowercaseChar())
        }
    }
    return sb.toString()
}

object PresetCities {
    val defaultCity: CityLocation by lazy { all.first() }

    val all: List<CityLocation> by lazy {
        ArabPrayerCities.all + WorldPrayerCities.all
    }

    fun byRegion(region: PrayerRegion?): List<CityLocation> =
        if (region == null) all else all.filter { it.region == region }

    fun search(query: String, region: PrayerRegion? = null): List<CityLocation> {
        val pool = byRegion(region)
        val normalized = normalizeArabicQuery(query)
        if (normalized.isEmpty()) return pool
        return pool.filter { city ->
            normalizeArabicQuery(city.nameAr).contains(normalized) ||
                normalizeArabicQuery(city.countryAr).contains(normalized) ||
                normalizeArabicQuery(city.region.titleAr).contains(normalized)
        }
    }

    fun findTimeZoneId(nameAr: String, countryAr: String): String {
        val exact = all.firstOrNull {
            it.nameAr == nameAr && (countryAr.isBlank() || it.countryAr == countryAr)
        }
        if (exact != null) return exact.timeZoneId
        return all.firstOrNull { it.nameAr == nameAr }?.timeZoneId.orEmpty()
    }

    fun findRegion(nameAr: String, countryAr: String): PrayerRegion {
        val exact = all.firstOrNull {
            it.nameAr == nameAr && (countryAr.isBlank() || it.countryAr == countryAr)
        }
        return exact?.region ?: PrayerRegion.GULF
    }

    fun formatUtcOffsetLabel(timeZone: TimeZone, atMillis: Long = System.currentTimeMillis()): String {
        val offsetMillis = timeZone.getOffset(atMillis)
        val totalMinutes = offsetMillis / 60_000
        val sign = if (totalMinutes >= 0) "+" else "-"
        val absMinutes = abs(totalMinutes)
        val hours = absMinutes / 60
        val mins = absMinutes % 60
        return if (mins == 0) {
            "UTC$sign$hours"
        } else {
            "UTC$sign$hours:${mins.toString().padStart(2, '0')}"
        }
    }
}

@androidx.compose.runtime.Immutable
data class PrayerTickerState(
    val nextPrayer: PrayerTimeEntry? = null,
    val countdownText: String = "٠٠:٠٠:٠٠",
    val iqamahStatusText: String? = null,
    val intervalProgress: Float = 0f
)

