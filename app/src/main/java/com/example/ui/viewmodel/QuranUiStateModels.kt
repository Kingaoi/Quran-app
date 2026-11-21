package com.example.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.FontFamily
import com.example.data.local.MushafOfflineStorage
import com.example.data.local.ReaderSettings
import com.example.data.local.RiwayahEdition
import com.example.domain.model.AyahItem
import com.example.domain.model.HizbQuarterInfo
import com.example.domain.model.JuzInfo
import com.example.domain.model.JuzSurahSlice
import com.example.domain.model.SurahInfo
import com.example.domain.quran.QuranTopicGroup
import com.example.domain.quran.SajdahPositionInfo

@Immutable
data class OfflineDownloadState(
    val edition: RiwayahEdition = RiwayahEdition.HAFS,
    val downloadedCount: Int = 0,
    val totalPages: Int = MushafOfflineStorage.TOTAL_PAGES,
    val isDownloading: Boolean = false,
    val errorMessage: String? = null
) {
    val isComplete: Boolean get() = downloadedCount >= totalPages
}

@Immutable
data class QuranUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val surahs: List<SurahInfo> = emptyList(),
    val ajza: List<JuzInfo> = emptyList(),
    val juzSurahSlices: Map<Int, List<JuzSurahSlice>> = emptyMap(),
    val hizbQuartersByJuz: Map<Int, List<HizbQuarterInfo>> = emptyMap(),
    val sajdahAyahs: List<SajdahPositionInfo> = emptyList(),
    val topicGroups: List<QuranTopicGroup> = emptyList(),
    val settings: ReaderSettings = ReaderSettings(),
    val svgVersion: String = "v1.1.1",
    val currentPage: Int = 1,
    val currentPageAyahs: List<AyahItem> = emptyList(),
    val currentPageSurah: SurahInfo? = null,
    val currentPageJuz: Int = 1,
    val uthmanicFontFamily: FontFamily? = null
)
