package com.example.domain.quran

import androidx.compose.runtime.Immutable

enum class QuranSearchMode(
    val labelAr: String,
    val placeholderAr: String,
    val descriptionAr: String
) {
    WORD(
        labelAr = "باللفظ",
        placeholderAr = "ابحث عن كلمة أو عبارة أو اسم سورة...",
        descriptionAr = "مطابقة دقيقة للألفاظ والعبارات القرآنية مع عدّ التكرارات"
    ),
    ROOT(
        labelAr = "بالجذر",
        placeholderAr = "اكتب جذراً ثلاثياً مثل: رحم، صبر، علم، أو كلمة مشتقة...",
        descriptionAr = "استخراج جميع المشتقات والصيغ القرآنية للجذر مع إحصاء شامل"
    ),
    MEANING(
        labelAr = "بالمعنى",
        placeholderAr = "اكتب مفهوماً مثل: السكينة، بر الوالدين، الرزق، التوبة...",
        descriptionAr = "بحث دلالي في المفاهيم والموضوعات القرآنية والآيات المرتبطة"
    )
}

enum class ConcordanceScopeFilter(val labelAr: String) {
    ALL("الكل"),
    MAKKI("المكي"),
    MADANI("المدني")
}

@Immutable
data class ConcordanceSurahStat(
    val surahNumber: Int,
    val surahNameAr: String,
    val isMakki: Boolean,
    val ayahCount: Int,
    val occurrenceCount: Int
)

@Immutable
data class ConcordanceFormStat(
    val form: String,
    val occurrenceCount: Int,
    val ayahCount: Int
)

@Immutable
data class ConcordanceConceptInfo(
    val id: String,
    val titleAr: String,
    val descriptionAr: String,
    val matchedKeywords: List<String>
)

@Immutable
data class ConcordanceHit(
    val surahNumber: Int,
    val surahNameAr: String,
    val isMakki: Boolean,
    val ayahNumber: Int,
    val pageNumber: Int,
    val juzNumber: Int,
    val ayahText: String,
    val imlaiText: String,
    val matchedTokens: List<String>,
    val matchCountInAyah: Int,
    val relevanceScore: Int,
    val matchedConceptTitle: String? = null
)

@Immutable
data class QuranConcordanceReport(
    val query: String,
    val normalizedQuery: String,
    val mode: QuranSearchMode,
    val resolvedRoot: String? = null,
    val matchedConcepts: List<ConcordanceConceptInfo> = emptyList(),
    val totalOccurrences: Int = 0,
    val totalMatchingAyahs: Int = 0,
    val totalMatchingSurahs: Int = 0,
    val makkiOccurrences: Int = 0,
    val madaniOccurrences: Int = 0,
    val makkiAyahs: Int = 0,
    val madaniAyahs: Int = 0,
    val juzCounts: List<Int> = List(30) { 0 },
    val topSurahs: List<ConcordanceSurahStat> = emptyList(),
    val derivedForms: List<ConcordanceFormStat> = emptyList(),
    val firstOccurrence: ConcordanceHit? = null,
    val lastOccurrence: ConcordanceHit? = null,
    val hits: List<ConcordanceHit> = emptyList()
) {
    val percentOfQuranAyahs: Float
        get() = (totalMatchingAyahs.toFloat() / TOTAL_QURAN_AYAHS.toFloat()) * 100f

    val makkiSharePercent: Int
        get() = if (totalOccurrences <= 0) 0 else ((makkiOccurrences * 100f) / totalOccurrences).toInt().coerceIn(0, 100)

    companion object {
        const val TOTAL_QURAN_AYAHS = 6236

        fun empty(query: String = "", mode: QuranSearchMode = QuranSearchMode.WORD): QuranConcordanceReport =
            QuranConcordanceReport(
                query = query,
                normalizedQuery = query.trim(),
                mode = mode
            )
    }
}
