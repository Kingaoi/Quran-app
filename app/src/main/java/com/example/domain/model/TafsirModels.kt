package com.example.domain.model

import androidx.compose.runtime.Immutable

enum class TafsirEdition(
    val slug: String,
    val titleAr: String,
    val subtitleAr: String
) {
    MUYASSAR(
        slug = "ar-tafsir-muyassar",
        titleAr = "التفسير الميسر",
        subtitleAr = "تفسير ميسّر ومختصر لمعاني الآيات"
    ),
    SAADI(
        slug = "ar-tafseer-al-saddi",
        titleAr = "تفسير السعدي",
        subtitleAr = "تيسير الكريم الرحمن للشيخ السعدي"
    ),
    IBN_KATHIR(
        slug = "ar-tafsir-ibn-kathir",
        titleAr = "تفسير ابن كثير",
        subtitleAr = "التفسير بالمأثور وذكر أسباب النزول"
    ),
    BAGHAWI(
        slug = "ar-tafsir-al-baghawi",
        titleAr = "تفسير البغوي",
        subtitleAr = "معالم التنزيل للإمام البغوي رحمه الله"
    )
}

@Immutable
data class TafsirOfflineState(
    val edition: TafsirEdition = TafsirEdition.MUYASSAR,
    val downloadedSurahs: Int = 0,
    val totalSurahs: Int = 114,
    val isDownloading: Boolean = false,
    val errorMessage: String? = null
) {
    val isComplete: Boolean get() = downloadedSurahs >= totalSurahs
}
