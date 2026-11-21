package com.example.domain.wird

import androidx.compose.runtime.Immutable
import java.util.Calendar

const val ALL_WEEK_DAYS_MASK = 0b1111111 // bits 0..6 corresponding to Calendar.SUNDAY (1) .. Calendar.SATURDAY (7)

@Immutable
enum class WirdPlanType(val key: String, val labelAr: String, val pastelIndex: Int) {
    MEMORIZATION("hifz", "حفظ", 3),
    REVIEW("review", "مراجعة", 2),
    RECITATION("recitation", "تلاوة", 0),
    MEMORIZATION_AND_REVIEW("hifz_review", "حفظ ومراجعة", 1);

    companion object {
        fun fromKey(key: String?): WirdPlanType = when (key) {
            "hifz", "study" -> MEMORIZATION
            "review" -> REVIEW
            "hifz_review" -> MEMORIZATION_AND_REVIEW
            "recitation", "book", "tadabbur", "family" -> RECITATION
            else -> RECITATION
        }
    }
}

@Immutable
enum class WirdPlanAudience(val key: String, val labelAr: String) {
    FOR_ME("for_me", "لي"),
    FOR_OTHER("for_other", "لغيري");

    companion object {
        fun fromKey(key: String?): WirdPlanAudience =
            entries.firstOrNull { it.key == key } ?: FOR_ME
    }
}

@Immutable
enum class WirdDirection(val key: String, val labelAr: String) {
    FATIHA_TO_NAS("fatiha_to_nas", "من الفاتحة إلى الناس"),
    NAS_TO_FATIHA("nas_to_fatiha", "من الناس إلى الفاتحة");

    companion object {
        fun fromKey(key: String?): WirdDirection =
            entries.firstOrNull { it.key == key } ?: FATIHA_TO_NAS
    }
}

@Immutable
enum class WirdGoalType(val key: String, val labelAr: String) {
    AMOUNT("amount", "مقدار"),
    DURATION("duration", "مدّة"),
    DATE_RANGE("date_range", "نطاق زمني");

    companion object {
        fun fromKey(key: String?): WirdGoalType =
            entries.firstOrNull { it.key == key } ?: AMOUNT
    }
}

@Immutable
enum class WirdDurationUnit(val key: String, val labelAr: String, val daysMultiplier: Int) {
    DAY("day", "يوم", 1),
    WEEK("week", "أسبوع", 7),
    MONTH("month", "شهر", 30);

    companion object {
        fun fromKey(key: String?): WirdDurationUnit =
            entries.firstOrNull { it.key == key } ?: DAY
    }
}

@Immutable
enum class WirdAmountUnit(val key: String, val singularAr: String, val pluralAr: String) {
    PAGE("page", "وجه", "أوجه"),
    QUARTER("quarter", "ربع", "أرباع"),
    HIZB("hizb", "حزب", "أحزاب"),
    JUZ("juz", "جزء", "أجزاء"),
    AYAH("ayah", "آية", "آيات");

    companion object {
        fun fromKey(key: String?): WirdAmountUnit =
            entries.firstOrNull { it.key == key } ?: PAGE
    }
}

@Immutable
enum class WirdSessionTimingMode(val key: String, val labelAr: String) {
    UNSPECIFIED("unspecified", "وقت غير محدّد"),
    SCHEDULED("scheduled", "بوقت محدّد");

    companion object {
        fun fromKey(key: String?): WirdSessionTimingMode =
            entries.firstOrNull { it.key == key } ?: UNSPECIFIED
    }
}

@Immutable
data class WirdSessionSlot(
    val index: Int,
    val startHour: Int = 6,
    val startMinute: Int = 0,
    val endHour: Int = 7,
    val endMinute: Int = 0
)

fun defaultWirdSessions(): List<WirdSessionSlot> = listOf(
    WirdSessionSlot(index = 0, startHour = 6, startMinute = 0, endHour = 7, endMinute = 0),
    WirdSessionSlot(index = 1, startHour = 13, startMinute = 0, endHour = 13, endMinute = 30),
    WirdSessionSlot(index = 2, startHour = 17, startMinute = 0, endHour = 17, endMinute = 30),
    WirdSessionSlot(index = 3, startHour = 20, startMinute = 0, endHour = 20, endMinute = 30),
    WirdSessionSlot(index = 4, startHour = 21, startMinute = 30, endHour = 22, endMinute = 0)
)

fun isDayActiveInMask(mask: Int, calendarDayOfWeek: Int): Boolean {
    val bit = 1 shl ((calendarDayOfWeek - Calendar.SUNDAY).coerceIn(0, 6))
    return (mask and bit) != 0
}

fun toggleDayInMask(mask: Int, calendarDayOfWeek: Int): Int {
    val bit = 1 shl ((calendarDayOfWeek - Calendar.SUNDAY).coerceIn(0, 6))
    val next = mask xor bit
    return if (next == 0) mask else next
}

@Immutable
data class KhatmahPlanConfig(
    val isActive: Boolean = false,
    val startJuz: Int = 1,
    val quartersPerDay: Int = 8,
    val completedWirdsCount: Int = 0,
    val startEpochDay: Long = 0L,
    val lastProgressPage: Int = 0,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val completedPortionDays: Map<Int, Long> = emptyMap(),
    val portionDayOverrides: Map<Int, Long> = emptyMap()
)

@Immutable
data class WirdPortion(
    val index: Int,
    val juzNumber: Int,
    val juzNameAr: String,
    val startSurahNumber: Int,
    val startSurahNameAr: String,
    val startAyahNumber: Int,
    val startPage: Int,
    val endSurahNumber: Int,
    val endSurahNameAr: String,
    val endAyahNumber: Int,
    val endPage: Int,
    val openingAyahText: String,
    val quartersCount: Int
) {
    val pageSpanCount: Int
        get() = (endPage - startPage + 1).coerceAtLeast(1)
}

@Immutable
sealed interface WirdScheduleStatus {
    data object OnTrack : WirdScheduleStatus
    data class Ahead(val days: Int) : WirdScheduleStatus
    data class Behind(val days: Int) : WirdScheduleStatus
}

@Immutable
data class WirdPlanSummary(
    val allWirds: List<WirdPortion>,
    val totalWirds: Int,
    val completedCount: Int,
    val upcomingCount: Int,
    val currentWird: WirdPortion?,
    val previousWirds: List<WirdPortion>,
    val upcomingWirds: List<WirdPortion>,
    val isCompleted: Boolean,
    val progressFraction: Float,
    val scheduleStatus: WirdScheduleStatus,
    val resumePage: Int,
    val hasStartedCurrentWird: Boolean
)

@Immutable
data class NamedWirdPlanConfig(
    val id: String,
    val title: String,
    val startJuz: Int = 1,
    val quartersPerDay: Int = 8,
    val completedWirdsCount: Int = 0,
    val startEpochDay: Long = 0L,
    val lastProgressPage: Int = 0,
    val iconKey: String = "book",
    val isV2Plan: Boolean = false,
    val planType: WirdPlanType = WirdPlanType.RECITATION,
    val audience: WirdPlanAudience = WirdPlanAudience.FOR_ME,
    val allowFollowUp: Boolean = false,
    val direction: WirdDirection = WirdDirection.FATIHA_TO_NAS,
    val fromSurah: Int = 1,
    val fromAyah: Int = 1,
    val toSurah: Int = 1,
    val toAyah: Int = 7,
    val isPeriodicWird: Boolean = false,
    val activeDaysMask: Int = ALL_WEEK_DAYS_MASK,
    val goalType: WirdGoalType = WirdGoalType.AMOUNT,
    val amountValue: Float = 1f,
    val amountUnit: WirdAmountUnit = WirdAmountUnit.PAGE,
    val rabtAmount: Float = 0f,
    val reviewAmountValue: Float = 1f,
    val reviewAmountUnit: WirdAmountUnit = WirdAmountUnit.PAGE,
    val targetDurationDays: Int = 30,
    val durationUnit: WirdDurationUnit = WirdDurationUnit.DAY,
    val endEpochDay: Long = 0L,
    val hasExplicitEndDate: Boolean = false,
    val sessionsPerDay: Int = 1,
    val sessionTimingMode: WirdSessionTimingMode = WirdSessionTimingMode.UNSPECIFIED,
    val sessions: List<WirdSessionSlot> = defaultWirdSessions(),
    val reminderEnabled: Boolean = false,
    val reminderLeadMinutes: Int = 0,
    val completedPortionDays: Map<Int, Long> = emptyMap(),
    val portionDayOverrides: Map<Int, Long> = emptyMap()
) {
    fun toPlanConfig(): KhatmahPlanConfig = KhatmahPlanConfig(
        isActive = true,
        startJuz = startJuz,
        quartersPerDay = quartersPerDay,
        completedWirdsCount = completedWirdsCount,
        startEpochDay = startEpochDay,
        lastProgressPage = lastProgressPage,
        completedPortionDays = completedPortionDays,
        portionDayOverrides = portionDayOverrides
    )
}

@Immutable
data class NamedWirdPlanItem(
    val config: NamedWirdPlanConfig,
    val summary: WirdPlanSummary
)
