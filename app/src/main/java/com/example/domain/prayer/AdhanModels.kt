package com.example.domain.prayer

import androidx.compose.runtime.Immutable

@Immutable
data class AdhanVoice(
    val id: String,
    val nameAr: String,
    val muezzinAr: String,
    val locationAr: String,
    val audioUrl: String,
    val isShort: Boolean = false,
    val maxDurationMs: Long? = null,
    val durationBadgeAr: String? = null
)

object AdhanVoices {
    const val SHORT_ADHAN_13S_ID = "short_adhan_13s"
    const val SHORT_ADHAN_13S_DURATION_MS = 13_000L

    val all: List<AdhanVoice> = listOf(
        AdhanVoice(
            id = "makkah_haram",
            nameAr = "أذان الحرم المكي",
            muezzinAr = "المسجد الحرام — مكة المكرمة",
            locationAr = "مكة المكرمة",
            audioUrl = "https://www.islamcan.com/audio/adhan/azan1.mp3"
        ),
        AdhanVoice(
            id = "madinah_nabawi",
            nameAr = "أذان المسجد النبوي",
            muezzinAr = "المسجد النبوي الشريف — المدينة",
            locationAr = "المدينة المنورة",
            audioUrl = "https://www.islamcan.com/audio/adhan/azan2.mp3"
        ),
        AdhanVoice(
            id = "aqsa",
            nameAr = "أذان المسجد الأقصى",
            muezzinAr = "المسجد الأقصى المبارك — القدس",
            locationAr = "القدس الشريف",
            audioUrl = "https://www.islamcan.com/audio/adhan/azan3.mp3"
        ),
        AdhanVoice(
            id = "afasy",
            nameAr = "أذان عذب (خليجي)",
            muezzinAr = "تلاوة أذان خاشعة بصوت ندي",
            locationAr = "الكويت",
            audioUrl = "https://www.islamcan.com/audio/adhan/azan4.mp3"
        ),
        AdhanVoice(
            id = "cairo_egypt",
            nameAr = "أذان مصري كلاسيكي",
            muezzinAr = "المدرسة المصرية الهادئة",
            locationAr = "القاهرة",
            audioUrl = "https://www.islamcan.com/audio/adhan/azan6.mp3"
        ),
        AdhanVoice(
            id = SHORT_ADHAN_13S_ID,
            nameAr = "أذان قصير (١٣ ثانية)",
            muezzinAr = "تكبيرتان مختصرتان — ١٣ ثانية بصوت الحرم المكي",
            locationAr = "١٣ ثانية",
            audioUrl = "https://www.islamcan.com/audio/adhan/azan1.mp3",
            isShort = true,
            maxDurationMs = SHORT_ADHAN_13S_DURATION_MS,
            durationBadgeAr = "١٣ ثانية"
        ),
        AdhanVoice(
            id = "short_takbeer",
            nameAr = "تكبيرة أذان مختصرة",
            muezzinAr = "تنبيه قصير بالتكبير (مناسب للعمل)",
            locationAr = "قصير",
            audioUrl = "https://www.islamcan.com/audio/adhan/azan10.mp3",
            isShort = true,
            durationBadgeAr = "مختصر"
        )
    )

    val defaultVoice: AdhanVoice = all.first()

    fun byId(id: String?): AdhanVoice =
        all.firstOrNull { it.id == id } ?: defaultVoice
}
