package com.example.data.adhkar

import androidx.compose.runtime.Immutable
import com.example.data.repository.QuranRepository
import com.example.domain.model.DhikrCategory
import com.example.domain.model.DhikrItem

const val HISN_MUSLIM_INDEX_URL = "https://www.hisnmuslim.com/api/ar/husn_ar.json"

@Immutable
data class HisnChapterEntry(
    val id: Int,
    val title: String,
    val textUrl: String,
    val audioUrl: String = ""
)

@Immutable
data class CachedHisnCatalog(
    val cachedAtMillis: Long,
    val itemsByCategory: Map<DhikrCategory, List<DhikrItem>>
)

object HisnCategoryMapper {

    fun mapChapterToCategories(chapter: HisnChapterEntry): List<DhikrCategory> {
        val norm = QuranRepository.normalizeArabic(chapter.title)
        val matched = mutableListOf<DhikrCategory>()

        if (chapter.id == 27 || (norm.contains("الصباح") && norm.contains("المساء"))) {
            matched.add(DhikrCategory.MORNING)
            matched.add(DhikrCategory.EVENING)
        } else {
            if (norm.contains("الصباح")) matched.add(DhikrCategory.MORNING)
            if (norm.contains("المساء")) matched.add(DhikrCategory.EVENING)
        }

        if (chapter.id == 25 || norm.contains("بعد السلام من الصلا") || norm.contains("بعد الصلا")) {
            matched.add(DhikrCategory.AFTER_PRAYER)
        }

        if (chapter.id == 28 || norm == "اذكار النوم" || norm.startsWith("اذكار النوم")) {
            matched.add(DhikrCategory.SLEEP)
        }

        if (chapter.id in setOf(18, 19, 20) || norm.contains("المسجد")) {
            matched.add(DhikrCategory.MOSQUE)
        }

        if (chapter.id == 1 || norm.contains("الاستيقاظ من النوم")) {
            matched.add(DhikrCategory.WAKE_UP)
        }

        if (norm.contains("ادعية قرانية") || norm.contains("قرانية")) {
            matched.add(DhikrCategory.QURANIC_DUAS)
        }

        if (chapter.id in setOf(26, 34, 35, 42, 53) ||
            norm.contains("الهم والحزن") ||
            norm.contains("دعاء الكرب") ||
            norm.contains("الاستخارة") ||
            norm.contains("قضاء الدين") ||
            norm.contains("استصعب عليه امر")
        ) {
            matched.add(DhikrCategory.PROPHETIC_DUAS)
        }

        if (chapter.id in setOf(46, 47, 122, 124) ||
            norm.contains("الرقية") ||
            norm.contains("تعويذ الاولاد") ||
            norm.contains("للمريض") ||
            norm.contains("المحتضر") ||
            norm.contains("احس وجعا في جسده")
        ) {
            matched.add(DhikrCategory.RUQYAH)
        }

        if (chapter.id in setOf(95, 96, 99, 100, 101, 104) ||
            norm.contains("السفر") ||
            norm.contains("الركوب") ||
            norm.contains("القرية") ||
            norm.contains("البلدة") ||
            norm.contains("المسافر") ||
            norm.contains("مبيت")
        ) {
            matched.add(DhikrCategory.TRAVEL)
        }

        if (chapter.id in setOf(129, 130, 131) ||
            norm.contains("التسبيح") ||
            norm.contains("الاستغفار") ||
            norm.contains("التوبة") ||
            norm.contains("الصلاة على النبي") ||
            norm.contains("فضل الذكر")
        ) {
            matched.add(DhikrCategory.TASBIH)
        }

        if (norm.contains("الهم") || norm.contains("الحزن") || norm.contains("الكرب") ||
            norm.contains("عدو") || norm.contains("سلطان") || norm.contains("الدين") ||
            norm.contains("فزع") || norm.contains("وسوس") || norm.contains("استصعب") ||
            norm.contains("ذنب") || norm.contains("مصيبة") || norm.contains("الغضب")
        ) {
            matched.add(DhikrCategory.WORRY_RELIEF)
        }

        if (norm.contains("الريح") || norm.contains("الرعد") || norm.contains("المطر") ||
            norm.contains("استسقاء") || norm.contains("صحا") || norm.contains("الهلال") ||
            norm.contains("باكور")
        ) {
            matched.add(DhikrCategory.WEATHER_NATURE)
        }

        if (norm.contains("صوم") || norm.contains("صائم") || norm.contains("افطر") ||
            norm.contains("تلبية") || norm.contains("الحجر") || norm.contains("الصفا") ||
            norm.contains("المروة") || norm.contains("عرفة") || norm.contains("المشعر") ||
            norm.contains("الجمار") || norm.contains("الذبح") || norm.contains("الاستخارة")
        ) {
            matched.add(DhikrCategory.HAJJ_WORSHIP)
        }

        if (norm.contains("ضيف") || norm.contains("مولود") || norm.contains("المتزوج") ||
            norm.contains("النكاح") || norm.contains("عطس") || norm.contains("كفارة") ||
            norm.contains("المجلس") || norm.contains("السلام") || norm.contains("الميت") ||
            norm.contains("التعزية") || norm.contains("القبر") || norm.contains("القبور") ||
            norm.contains("الديك") || norm.contains("الكلاب") || norm.contains("طيرة") ||
            norm.contains("معروف") || norm.contains("حبك")
        ) {
            matched.add(DhikrCategory.SOCIAL_ETHICS)
        }

        if (norm.contains("لبس") || norm.contains("الثوب") || norm.contains("الخلاء") ||
            norm.contains("الوضوء") || norm.contains("المنزل") || norm.contains("البيت") ||
            norm.contains("الطعام") || norm.contains("الشراب") || norm.contains("السوق") ||
            norm.contains("الدابة") || norm.contains("تعس") || norm.contains("شكر") ||
            norm.contains("نعم")
        ) {
            matched.add(DhikrCategory.DAILY_LIFE)
        }

        if (matched.isEmpty()) {
            matched.add(DhikrCategory.PROPHETIC_DUAS)
        }

        return matched.distinct()
    }
}
