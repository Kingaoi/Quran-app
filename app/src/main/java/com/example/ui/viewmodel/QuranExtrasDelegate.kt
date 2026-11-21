package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import com.example.data.audio.SurahAudioDownloadState
import com.example.data.audio.SurahAudioStorage
import com.example.data.local.AyahBookmark
import com.example.data.local.BookmarksRepository
import com.example.data.local.IndexSearchHistoryRepository
import com.example.data.local.ReadingStats
import com.example.data.local.ReadingStatsRepository
import com.example.data.tafsir.TafsirRepository
import com.example.domain.model.QuranReciters
import com.example.domain.model.TafsirEdition
import com.example.domain.model.TafsirOfflineState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
data class AyahTafsirUiState(
    val surahNumber: Int = 1,
    val ayahNumber: Int = 1,
    val edition: TafsirEdition = TafsirEdition.MUYASSAR,
    val isLoading: Boolean = false,
    val text: String? = null,
    val errorMessage: String? = null
)

class QuranExtrasDelegate(
    application: Application,
    private val scope: CoroutineScope,
    val surahAudioStorage: SurahAudioStorage
) {
    private val bookmarksRepository = BookmarksRepository(application)
    private val searchHistoryRepository = IndexSearchHistoryRepository(application)
    val statsRepository = ReadingStatsRepository(application)
    val tafsirRepository = TafsirRepository(application)
    val playlists = AudioPlaylistDelegate(application, scope)
    var onStorageMutated: () -> Unit = {}

    val bookmarksState: StateFlow<List<AyahBookmark>> = bookmarksRepository.bookmarksFlow
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentSearchesState: StateFlow<List<String>> = searchHistoryRepository.recentSearchesFlow
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val statsState: StateFlow<ReadingStats> = statsRepository.statsFlow
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), ReadingStats())

    private val _tafsirOfflineState = MutableStateFlow(TafsirOfflineState())
    val tafsirOfflineState: StateFlow<TafsirOfflineState> = _tafsirOfflineState.asStateFlow()

    private val _ayahTafsirState = MutableStateFlow(AyahTafsirUiState())
    val ayahTafsirState: StateFlow<AyahTafsirUiState> = _ayahTafsirState.asStateFlow()

    private val _surahAudioState = MutableStateFlow(SurahAudioDownloadState())
    val surahAudioState: StateFlow<SurahAudioDownloadState> = _surahAudioState.asStateFlow()

    private var tafsirDownloadJob: Job? = null
    private var tafsirLoadJob: Job? = null
    private var audioDownloadJob: Job? = null

    init {
        refreshTafsirCount(TafsirEdition.MUYASSAR)
    }

    fun selectTafsirOfflineEdition(edition: TafsirEdition) {
        if (_tafsirOfflineState.value.isDownloading) return
        refreshTafsirCount(edition)
    }

    fun refreshTafsirCount(edition: TafsirEdition = _tafsirOfflineState.value.edition) {
        scope.launch {
            val count = withContext(Dispatchers.IO) { tafsirRepository.countDownloadedSurahs(edition) }
            _tafsirOfflineState.update {
                if (it.isDownloading) it else TafsirOfflineState(edition = edition, downloadedSurahs = count)
            }
        }
    }

    fun loadAyahTafsir(surah: Int, ayah: Int, edition: TafsirEdition) {
        tafsirLoadJob?.cancel()
        _ayahTafsirState.value = AyahTafsirUiState(
            surahNumber = surah,
            ayahNumber = ayah,
            edition = edition,
            isLoading = true
        )
        tafsirLoadJob = scope.launch {
            val res = tafsirRepository.getAyahTafsir(edition, surah, ayah)
            _ayahTafsirState.value = AyahTafsirUiState(
                surahNumber = surah,
                ayahNumber = ayah,
                edition = edition,
                isLoading = false,
                text = res.getOrNull(),
                errorMessage = res.exceptionOrNull()?.localizedMessage
            )
        }
    }

    fun downloadFullTafsir(edition: TafsirEdition = _tafsirOfflineState.value.edition) {
        if (_tafsirOfflineState.value.isDownloading) return
        tafsirDownloadJob?.cancel()
        tafsirDownloadJob = scope.launch {
            _tafsirOfflineState.value = TafsirOfflineState(
                edition = edition,
                downloadedSurahs = withContext(Dispatchers.IO) { tafsirRepository.countDownloadedSurahs(edition) },
                isDownloading = true
            )
            val result = tafsirRepository.downloadFullTafsir(edition) { done, total ->
                _tafsirOfflineState.update { it.copy(downloadedSurahs = done, totalSurahs = total) }
            }
            val finalCount = withContext(Dispatchers.IO) { tafsirRepository.countDownloadedSurahs(edition) }
            _tafsirOfflineState.value = TafsirOfflineState(
                edition = edition,
                downloadedSurahs = finalCount,
                isDownloading = false,
                errorMessage = result.exceptionOrNull()?.localizedMessage
            )
            onStorageMutated()
        }
    }

    fun cancelTafsirDownload() {
        tafsirDownloadJob?.cancel()
        val ed = _tafsirOfflineState.value.edition
        refreshTafsirCount(ed)
        onStorageMutated()
    }

    fun deleteTafsirEdition(edition: TafsirEdition = _tafsirOfflineState.value.edition) {
        tafsirDownloadJob?.cancel()
        scope.launch {
            tafsirRepository.deleteEdition(edition)
            _tafsirOfflineState.value = TafsirOfflineState(edition = edition, downloadedSurahs = 0)
            onStorageMutated()
        }
    }

    fun refreshSurahAudioState(reciterId: String, surahNumber: Int, totalAyahs: Int) {
        if (_surahAudioState.value.isDownloading &&
            _surahAudioState.value.reciterId == reciterId &&
            _surahAudioState.value.surahNumber == surahNumber
        ) {
            return
        }
        scope.launch {
            val count = withContext(Dispatchers.IO) {
                surahAudioStorage.countDownloadedAyahsForSurah(reciterId, surahNumber, totalAyahs)
            }
            _surahAudioState.value = SurahAudioDownloadState(
                reciterId = reciterId,
                surahNumber = surahNumber,
                downloadedAyahs = count,
                totalAyahs = totalAyahs
            )
        }
    }

    fun downloadSurahAudio(reciterId: String, surahNumber: Int, totalAyahs: Int) {
        if (_surahAudioState.value.isDownloading) return
        val reciter = QuranReciters.byId(reciterId)
        audioDownloadJob?.cancel()
        audioDownloadJob = scope.launch {
            _surahAudioState.value = SurahAudioDownloadState(
                reciterId = reciterId,
                surahNumber = surahNumber,
                downloadedAyahs = 0,
                totalAyahs = totalAyahs,
                isDownloading = true
            )
            val result = surahAudioStorage.downloadSurah(reciter, surahNumber, totalAyahs) { done, total ->
                _surahAudioState.update { it.copy(downloadedAyahs = done, totalAyahs = total) }
            }
            val finalCount = withContext(Dispatchers.IO) {
                surahAudioStorage.countDownloadedAyahsForSurah(reciterId, surahNumber, totalAyahs)
            }
            _surahAudioState.value = SurahAudioDownloadState(
                reciterId = reciterId,
                surahNumber = surahNumber,
                downloadedAyahs = finalCount,
                totalAyahs = totalAyahs,
                isDownloading = false,
                errorMessage = result.exceptionOrNull()?.localizedMessage
            )
            onStorageMutated()
        }
    }

    fun cancelSurahAudioDownload(reciterId: String, surahNumber: Int, totalAyahs: Int) {
        audioDownloadJob?.cancel()
        audioDownloadJob = null
        _surahAudioState.update { it.copy(isDownloading = false) }
        refreshSurahAudioState(reciterId, surahNumber, totalAyahs)
        onStorageMutated()
    }

    fun deleteSurahAudio(reciterId: String, surahNumber: Int, totalAyahs: Int) {
        audioDownloadJob?.cancel()
        scope.launch {
            surahAudioStorage.deleteSurah(reciterId, surahNumber, totalAyahs)
            _surahAudioState.value = SurahAudioDownloadState(
                reciterId = reciterId,
                surahNumber = surahNumber,
                downloadedAyahs = 0,
                totalAyahs = totalAyahs
            )
            onStorageMutated()
        }
    }

    fun upsertBookmark(bookmark: AyahBookmark) {
        scope.launch { bookmarksRepository.upsertBookmark(bookmark) }
    }

    fun deleteBookmark(id: String) {
        scope.launch { bookmarksRepository.deleteBookmark(id) }
    }

    fun recordPageRead() {
        scope.launch { statsRepository.recordPageSettled() }
    }

    fun incrementTasbih() {
        scope.launch { statsRepository.incrementTasbih() }
    }

    fun addCustomTasbihPhrase(phrase: String) {
        scope.launch { statsRepository.addCustomTasbihPhrase(phrase) }
    }

    fun removeCustomTasbihPhrase(phrase: String) {
        scope.launch { statsRepository.removeCustomTasbihPhrase(phrase) }
    }

    fun recordIndexSearch(query: String) {
        scope.launch { searchHistoryRepository.recordQuery(query) }
    }

    fun removeIndexSearch(query: String) {
        scope.launch { searchHistoryRepository.removeQuery(query) }
    }

    fun clearIndexSearches() {
        scope.launch { searchHistoryRepository.clearAll() }
    }

    fun setDailyGoalPages(pages: Int) {
        scope.launch { statsRepository.setDailyGoalPages(pages) }
    }

    fun toggleRawatibItem(itemId: String) {
        scope.launch { statsRepository.toggleRawatibItem(itemId) }
    }
}
