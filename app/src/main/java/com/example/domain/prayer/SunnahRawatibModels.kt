package com.example.domain.prayer

import androidx.compose.runtime.Immutable

@Immutable
data class RawatibPrayerItem(
    val id: String,
    val titleAr: String,
    val rakatLabelAr: String,
    val rakatCount: Int,
    val isRawatibConfirmed: Boolean = true
)

object SunnahRawatibCatalog {
    val dailyItems: List<RawatibPrayerItem> = listOf(
        RawatibPrayerItem("fajr_before", "سنة الفجر", "ركعتان قبلها", 2, true),
        RawatibPrayerItem("dhuhr_before", "قبلية الظهر", "٤ ركعات قبلها", 4, true),
        RawatibPrayerItem("dhuhr_after", "بعدية الظهر", "ركعتان بعدها", 2, true),
        RawatibPrayerItem("maghrib_after", "سنة المغرب", "ركعتان بعدها", 2, true),
        RawatibPrayerItem("isha_after", "سنة العشاء", "ركعتان بعدها", 2, true),
        RawatibPrayerItem("duha", "صلاة الضحى", "ركعتان فأكثر", 2, false),
        RawatibPrayerItem("witr", "صلاة الوتر", "ركعة فأكثر", 1, false)
    )

    fun completedConfirmedRakatCount(completedIds: Set<String>): Int =
        dailyItems.filter { it.isRawatibConfirmed && it.id in completedIds }.sumOf { it.rakatCount }
}
