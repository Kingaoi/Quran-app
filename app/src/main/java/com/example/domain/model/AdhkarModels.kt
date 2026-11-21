package com.example.domain.model

import androidx.compose.runtime.Immutable

enum class DhikrCategory(
    val id: String,
    val titleAr: String,
    val shortTitleAr: String,
    val subtitleAr: String,
    val isWideCard: Boolean = false
) {
    MORNING(
        id = "morning",
        titleAr = "أذكار الصباح",
        shortTitleAr = "الصباح",
        subtitleAr = "من طلوع الفجر إلى الضحى",
        isWideCard = true
    ),
    EVENING(
        id = "evening",
        titleAr = "أذكار المساء",
        shortTitleAr = "المساء",
        subtitleAr = "من العصر إلى غروب الشمس",
        isWideCard = true
    ),
    AFTER_PRAYER(
        id = "after_prayer",
        titleAr = "بعد الصلاة",
        shortTitleAr = "بعد الصلاة",
        subtitleAr = "الأذكار المأثورة دبر كل صلاة مكتوبة"
    ),
    SLEEP(
        id = "sleep",
        titleAr = "أذكار النوم",
        shortTitleAr = "النوم",
        subtitleAr = "أدعية التحصين والسكينة عند المنام"
    ),
    MOSQUE(
        id = "mosque",
        titleAr = "أذكار المسجد",
        shortTitleAr = "المسجد",
        subtitleAr = "أدعية الذهاب إلى المسجد ودخوله والخروج منه"
    ),
    WAKE_UP(
        id = "wake_up",
        titleAr = "الاستيقاظ",
        shortTitleAr = "الاستيقاظ",
        subtitleAr = "أذكار الاستيقاظ من النوم وحمد الله"
    ),
    QURANIC_DUAS(
        id = "quranic_duas",
        titleAr = "أدعية قرآنية",
        shortTitleAr = "قرآنية",
        subtitleAr = "مختارات من جوامع الدعاء في كتاب الله"
    ),
    PROPHETIC_DUAS(
        id = "prophetic_duas",
        titleAr = "أدعية مأثورة",
        shortTitleAr = "مأثورة",
        subtitleAr = "من أصح أدعية النبي ﷺ وجوامع الكلم"
    ),
    RUQYAH(
        id = "ruqyah",
        titleAr = "الرقية الشرعية",
        shortTitleAr = "الرقية",
        subtitleAr = "الرقية الثابتة من القرآن والسنة النبوية"
    ),
    TRAVEL(
        id = "travel",
        titleAr = "دعاء السفر",
        shortTitleAr = "السفر",
        subtitleAr = "دعاء الركوب والسفر ورجوع المسافر"
    ),
    TASBIH(
        id = "tasbih",
        titleAr = "تسابيح وأدعية",
        shortTitleAr = "تسابيح",
        subtitleAr = "باقة من جوامع الدعاء والذكر المطلق",
        isWideCard = true
    ),
    DAILY_LIFE(
        id = "daily_life",
        titleAr = "اليوم والبيت والطعام",
        shortTitleAr = "اليوم والبيت",
        subtitleAr = "أذكار دخول المنزل والطعام واللباس والوضوء"
    ),
    WORRY_RELIEF(
        id = "worry_relief",
        titleAr = "الهم والكرب والدَّين",
        shortTitleAr = "الهم والكرب",
        subtitleAr = "أدعية تفريج الهموم والشدائد وقضاء الديون"
    ),
    WEATHER_NATURE(
        id = "weather_nature",
        titleAr = "الرياح والمطر والهلال",
        shortTitleAr = "المطر والرياح",
        subtitleAr = "أذكار الريح والرعد ونزول الغيث ورؤية الهلال"
    ),
    SOCIAL_ETHICS(
        id = "social_ethics",
        titleAr = "المجالس والتهنئة والعطاس",
        shortTitleAr = "الآداب والمجالس",
        subtitleAr = "أدعية الضيافة والنكاح والمولود وكفارة المجلس"
    ),
    HAJJ_WORSHIP(
        id = "hajj_worship",
        titleAr = "الصيام والحج والعمرة",
        shortTitleAr = "الحج والصيام",
        subtitleAr = "التلبية والطواف وعرفة وأذكار الإفطار والعبادات"
    );

    val isSequentialLitany: Boolean
        get() = this == MORNING || this == EVENING || this == AFTER_PRAYER || this == SLEEP

    companion object {
        fun fromId(id: String?): DhikrCategory =
            entries.firstOrNull { it.id == id } ?: MORNING
    }
}

@Immutable
data class DhikrItem(
    val id: String,
    val category: DhikrCategory,
    val title: String,
    val arabicText: String,
    val virtueOrNote: String,
    val targetCount: Int,
    val reference: String,
    val audioUrl: String = "",
    val isCustom: Boolean = false,
    val chapterTitle: String = ""
) {
    fun resolvedChapterTitle(): String {
        if (chapterTitle.isNotBlank()) return chapterTitle.trim()
        if (reference.contains("•") && reference.substringBefore("•").contains("حصن المسلم")) {
            val extracted = reference.substringAfter("•").trim()
            if (extracted.isNotBlank()) return extracted
        }
        return if (category.isSequentialLitany) category.titleAr else title
    }
}

@Immutable
data class DhikrSubChapterGroup(
    val chapterTitle: String,
    val items: List<DhikrItem>
)

fun List<DhikrItem>.groupBySubChapter(): List<DhikrSubChapterGroup> {
    if (isEmpty()) return emptyList()
    val grouped = linkedMapOf<String, MutableList<DhikrItem>>()
    for (item in this) {
        grouped.getOrPut(item.resolvedChapterTitle()) { mutableListOf() }.add(item)
    }
    return grouped.entries.map { (title, list) ->
        DhikrSubChapterGroup(chapterTitle = title, items = list)
    }
}
