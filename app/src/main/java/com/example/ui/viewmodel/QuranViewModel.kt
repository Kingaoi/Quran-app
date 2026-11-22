package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.QuranApplication
import com.example.data.audio.AudioPlaybackState
import com.example.data.audio.AudioPlaylist
import com.example.data.audio.MemorizationLoopConfig
import com.example.data.audio.PlaybackNotificationButton
import com.example.data.audio.SurahAudioDownloadState
import com.example.data.audio.WirdAudioRange
import com.example.data.local.AppTheme
import com.example.data.local.AyahBookmark
import com.example.data.local.AyahHighlightColorPreset
import com.example.data.local.AyahHighlightStyle
import com.example.data.local.AyahMarkerColorPreset
import com.example.data.local.AyahMarkerColorTarget
import com.example.data.local.MushafOfflineStorage
import com.example.data.local.QuranFontOption
import com.example.data.local.ReadingStats
import com.example.data.local.RiwayahEdition
import com.example.di.QuranAppContainer
import com.example.di.QuranViewModelDependencies
import com.example.domain.model.AyahItem
import com.example.domain.model.OfflineInventoryUiState
import com.example.domain.model.ReciterOption
import com.example.domain.model.SearchHit
import com.example.domain.quran.AyahWordDiffSegment
import com.example.domain.quran.MutashabihMatch
import com.example.domain.quran.QuranConcordanceReport
import com.example.domain.quran.QuranSearchMode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuranViewModel(
    private val deps: QuranViewModelDependencies,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    autoStart: Boolean = true
) : ViewModel() {

    constructor(application: Application) : this(
        deps = (application as? QuranApplication)?.container?.quranViewModelDependencies()
            ?: QuranAppContainer(application).quranViewModelDependencies(ownsAudioPlayer = true)
    )

    private val repository = deps.repository
    private val prefsRepository = deps.prefsRepository
    val offlineStorage: MushafOfflineStorage = deps.offlineStorage
    private val rasterCache = deps.rasterCache
    private val systemBridge = deps.systemBridge

    val extras = QuranExtrasDelegate(
        scope = viewModelScope,
        surahAudioStorage = deps.surahAudioStorage,
        bookmarksRepository = deps.bookmarksRepository,
        searchHistoryRepository = deps.searchHistoryRepository,
        statsRepository = deps.readingStatsRepository,
        tafsirRepository = deps.tafsirRepository,
        playlists = AudioPlaylistDelegate(deps.audioPlaylistRepository, viewModelScope)
    )

    val mushafDownload = QuranMushafDownloadDelegate(
        scope = viewModelScope,
        offlineStorage = offlineStorage,
        ioDispatcher = ioDispatcher
    )

    val playback = QuranPlaybackDelegate(
        scope = viewModelScope,
        audioPlayer = deps.audioPlayer,
        riwayahRecitersRepo = deps.riwayahRecitersRepo
    )

    val preferences = QuranPreferencesDelegate(
        scope = viewModelScope,
        prefsRepository = prefsRepository,
        systemBridge = systemBridge,
        playbackDelegate = playback
    )

    val offlineHub = OfflineHubDelegate(
        scope = viewModelScope,
        mushafStorage = offlineStorage,
        tafsirRepository = deps.tafsirRepository,
        surahAudioStorage = deps.surahAudioStorage,
        hisnRepository = deps.hisnRepository,
        rasterCache = rasterCache,
        adhanStorage = deps.adhanAudioStorage,
        getSvgVersion = { _uiState.value.svgVersion },
        getSurahs = { _uiState.value.surahs.ifEmpty { repository.getSurahs() } },
        getActiveRiwayah = { _uiState.value.settings.riwayah },
        getActiveReciterId = { _uiState.value.settings.selectedReciterId },
        onInventoryChanged = {
            mushafDownload.refreshCount(_uiState.value.settings.riwayah)
            extras.refreshTafsirCount()
        }
    )

    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    private val _highlightedSearchAyah = MutableStateFlow<Pair<Int, Int>?>(null)
    val highlightedSearchAyah: StateFlow<Pair<Int, Int>?> = _highlightedSearchAyah.asStateFlow()

    val offlineState: StateFlow<OfflineDownloadState> = mushafDownload.offlineState
    val reciterCatalog: StateFlow<List<ReciterOption>> = playback.reciterCatalog
    val playbackState: StateFlow<AudioPlaybackState> = playback.playbackState
    val bookmarksState: StateFlow<List<AyahBookmark>> = extras.bookmarksState
    val statsState: StateFlow<ReadingStats> = extras.statsState
    val tafsirOfflineState = extras.tafsirOfflineState
    val ayahTafsirState: StateFlow<AyahTafsirUiState> = extras.ayahTafsirState
    val surahAudioState: StateFlow<SurahAudioDownloadState> = extras.surahAudioState
    val offlineHubState: StateFlow<OfflineInventoryUiState> = offlineHub.state

    private var highlightClearJob: Job? = null
    private var lastObservedRiwayah: RiwayahEdition? = null
    private var lastObservedReciterId: String? = null
    private var lastShortcutSyncKey: Pair<Int, Boolean>? = null
    private var initializedSettledPage: Int = -1

    init {
        extras.onStorageMutated = { offlineHub.refreshInventory() }
        mushafDownload.onInventoryChanged = { offlineHub.refreshInventory() }
        if (autoStart) {
            initialize()
            observeSettings()
        }
    }

    private fun syncShortcuts(page: Int, surahName: String?, useWestern: Boolean) {
        val key = page to useWestern
        if (lastShortcutSyncKey == key) return
        lastShortcutSyncKey = key
        systemBridge.syncDynamicShortcuts(page, surahName, useWestern)
    }

    private fun initialize() {
        viewModelScope.launch {
            runCatching {
                repository.ensureLoaded()
                val initial = prefsRepository.settingsFlow.first()
                playback.setDefaultReciter(initial.selectedReciterId)
                playback.updateNotificationConfig(initial.playbackNotificationConfig)
                val fontFamily = withContext(ioDispatcher) {
                    repository.getUthmanicFontFile()?.takeIf { it.exists() }?.let { runCatching { FontFamily(Font(it)) }.getOrNull() }
                }
                val page = initial.lastPage.coerceIn(1, 604)
                val downloaded = withContext(ioDispatcher) {
                    rasterCache.warmIndex()
                    systemBridge.warmMushafCropMap()
                    offlineStorage.countDownloadedPages(initial.riwayah)
                }
                lastObservedRiwayah = initial.riwayah
                lastObservedReciterId = initial.selectedReciterId
                initializedSettledPage = page
                _currentPage.value = page
                mushafDownload.setInitialState(initial.riwayah, downloaded)
                val primarySurah = repository.getPagePrimarySurah(page)
                _uiState.update {
                    it.copy(
                        isLoading = false, surahs = repository.getSurahs(), ajza = repository.getAjza(),
                        juzSurahSlices = repository.getJuzSurahSlices(), hizbQuartersByJuz = repository.getHizbQuartersByJuz(),
                        sajdahAyahs = repository.getSajdahPositions(), topicGroups = repository.getTopicGroups(),
                        settings = initial, currentPage = page, currentPageAyahs = repository.getAyahsForPage(page),
                        currentPageSurah = primarySurah, currentPageJuz = repository.getPageJuz(page), uthmanicFontFamily = fontFamily
                    )
                }
                syncShortcuts(page, primarySurah?.nameAr, initial.useWesternDigits)
                primarySurah?.let { extras.refreshSurahAudioState(initial.selectedReciterId, it.number, it.ayahCount) }
                offlineHub.refreshInventory()
                playback.loadMultiRiwayahReciters { _uiState.value.settings.selectedReciterId }
                val ver = repository.resolveSvgVersion()
                _uiState.update { it.copy(svgVersion = ver) }
                val prefetchModels = listOf(page, (page + 1).coerceAtMost(604)).distinct().map { p ->
                    offlineStorage.getCachedPageFileFast(initial.riwayah, p) ?: repository.getSvgPageUrl(ver, initial.riwayah, p)
                }
                systemBridge.prefetchMushafPages(prefetchModels)
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage ?: "حدث خطأ أثناء تحميل بيانات المصحف") }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            prefsRepository.settingsFlow.collect { settings ->
                _uiState.update { if (it.settings == settings) it else it.copy(settings = settings) }
                playback.updateNotificationConfig(settings.playbackNotificationConfig)
                if (!_uiState.value.isLoading) syncShortcuts(_currentPage.value, _uiState.value.currentPageSurah?.nameAr, settings.useWesternDigits)
                if (lastObservedReciterId != settings.selectedReciterId) {
                    lastObservedReciterId = settings.selectedReciterId
                    playback.setDefaultReciter(settings.selectedReciterId)
                    _uiState.value.currentPageSurah?.let { extras.refreshSurahAudioState(settings.selectedReciterId, it.number, it.ayahCount) }
                }
                if (lastObservedRiwayah != settings.riwayah) {
                    lastObservedRiwayah = settings.riwayah
                    mushafDownload.refreshCount(settings.riwayah)
                }
            }
        }
    }

    fun getAyahsForPage(page: Int): List<AyahItem> = repository.getAyahsForPage(page)
    fun getPageForAyah(surah: Int, ayah: Int): Int? = repository.getPageForAyah(surah, ayah)
    fun searchQuran(query: String): List<SearchHit> = repository.searchAyahs(query)
    fun searchConcordance(query: String, mode: QuranSearchMode): QuranConcordanceReport = repository.searchConcordance(query, mode)
    fun getSvgModel(page: Int): Any = offlineStorage.getCachedPageFileFast(_uiState.value.settings.riwayah, page) ?: repository.getSvgPageUrl(_uiState.value.svgVersion, _uiState.value.settings.riwayah, page)
    fun getRawGithubSvgUrl(page: Int): String = repository.getRawGithubSvgUrl(_uiState.value.settings.riwayah, page)

    fun onPageScrolling(page: Int) {
        val clamped = page.coerceIn(1, 604)
        if (_currentPage.value != clamped) _currentPage.value = clamped
    }

    fun onPageSettled(page: Int) {
        val clamped = page.coerceIn(1, 604); _currentPage.value = clamped
        val ayahs = repository.getAyahsForPage(clamped); val firstAyah = ayahs.firstOrNull()
        val primarySurah = repository.getPagePrimarySurah(clamped)
        val pageChanged = initializedSettledPage != clamped; initializedSettledPage = clamped
        _uiState.update {
            if (it.currentPage == clamped) it else it.copy(
                currentPage = clamped, currentPageAyahs = ayahs, currentPageSurah = primarySurah, currentPageJuz = repository.getPageJuz(clamped)
            )
        }
        primarySurah?.let { extras.refreshSurahAudioState(_uiState.value.settings.selectedReciterId, it.number, it.ayahCount) }
        if (pageChanged) extras.recordPageRead()
        syncShortcuts(clamped, primarySurah?.nameAr, _uiState.value.settings.useWesternDigits)
        preferences.updateLastPosition(clamped, firstAyah?.surahNumber ?: 1, firstAyah?.ayahNumber ?: 1)
    }

    fun openPage(page: Int) = onPageSettled(page)
    fun openSurah(surahNumber: Int) { repository.getSurah(surahNumber)?.let { onPageSettled(it.firstPage) } }
    fun getMutashabihatCount(surah: Int, ayah: Int): Int = repository.getMutashabihatCount(surah, ayah)
    fun findMutashabihat(ayah: AyahItem): List<MutashabihMatch> = repository.findMutashabihat(ayah)
    fun compareAyahWords(source: AyahItem, target: AyahItem): List<AyahWordDiffSegment> = repository.compareAyahWords(source, target)

    fun openAyah(page: Int, surah: Int, ayah: Int) {
        onPageSettled(page); highlightClearJob?.cancel(); _highlightedSearchAyah.value = surah to ayah
        highlightClearJob = viewModelScope.launch { delay(4_500L); _highlightedSearchAyah.value = null }
    }

    fun startFullMushafDownload() = mushafDownload.startFullMushafDownload(_uiState.value.settings.riwayah, _uiState.value.svgVersion)
    fun cancelFullMushafDownload() = mushafDownload.cancelFullMushafDownload(_uiState.value.settings.riwayah)
    fun deleteDownloadedMushaf() = mushafDownload.deleteDownloadedMushaf(_uiState.value.settings.riwayah)

    fun toggleBookmark(page: Int) = preferences.toggleBookmark(page)
    fun setRiwayah(edition: RiwayahEdition) = preferences.setRiwayah(edition)
    fun setAppTheme(theme: AppTheme) = preferences.setAppTheme(theme)
    fun setAyahMarkerColor(preset: AyahMarkerColorPreset) = preferences.setAyahMarkerColor(preset)
    fun setAyahMarkerColorTarget(target: AyahMarkerColorTarget) = preferences.setAyahMarkerColorTarget(target)
    fun setAyahHighlightColor(preset: AyahHighlightColorPreset) = preferences.setAyahHighlightColor(preset)
    fun setAyahHighlightStyle(style: AyahHighlightStyle) = preferences.setAyahHighlightStyle(style)
    fun setAyahHighlightAlpha(alpha: Float) = preferences.setAyahHighlightAlpha(alpha)
    fun setSurahBannerEnabled(enabled: Boolean) = preferences.setSurahBannerEnabled(enabled)
    fun setFontSize(sizeSp: Float) = preferences.setFontSize(sizeSp)
    fun setQuranFont(option: QuranFontOption) = preferences.setQuranFont(option)
    fun resetFontSettings() = preferences.resetFontSettings()
    fun setUseWesternDigits(useWestern: Boolean) = preferences.setUseWesternDigits(useWestern)
    fun setKeepScreenOn(enabled: Boolean) = preferences.setKeepScreenOn(enabled)
    fun setReaderDimmerAlpha(alpha: Float) = preferences.setReaderDimmerAlpha(alpha)
    fun setTwoPageSpreadOnTablet(enabled: Boolean) = preferences.setTwoPageSpreadOnTablet(enabled)
    fun setAutoAdvanceDhikr(enabled: Boolean) = preferences.setAutoAdvanceDhikr(enabled)
    fun setHapticFeedbackEnabled(enabled: Boolean) = preferences.setHapticFeedbackEnabled(enabled)
    fun setWirdPureReadingMode(enabled: Boolean) = preferences.setWirdPureReadingMode(enabled)
    fun setHideWirdBannerOutsideWird(enabled: Boolean) = preferences.setHideWirdBannerOutsideWird(enabled)
    fun setPlaybackNotifEnabled(enabled: Boolean) = preferences.setPlaybackNotifEnabled(enabled)
    fun togglePlaybackNotifButton(button: PlaybackNotificationButton) = preferences.togglePlaybackNotifButton(button)
    fun setPlaybackNotifShowWhenPaused(show: Boolean) = preferences.setPlaybackNotifShowWhenPaused(show)
    fun setReciter(reciterId: String) = preferences.setReciter(reciterId)

    fun playAyah(surah: Int, ayah: Int) = playback.playAyah(surah, ayah)
    fun togglePlayWirdRange(range: WirdAudioRange) = playback.togglePlayWirdRange(range)
    fun playPlaylist(playlist: AudioPlaylist, startTrackIndex: Int = 0) = playback.playPlaylist(playlist, startTrackIndex)
    fun togglePlayPlaylist(playlist: AudioPlaylist) = playback.togglePlayPlaylist(playlist)
    fun nextPlaylistTrack() = playback.nextPlaylistTrack()
    fun previousPlaylistTrack() = playback.previousPlaylistTrack()
    fun togglePlaylistSessionRepeat() = playback.togglePlaylistSessionRepeat()
    fun playCurrentPageFirstAyah() { repository.getAyahsForPage(_currentPage.value).firstOrNull()?.let { playback.playAyah(it.surahNumber, it.ayahNumber) } }
    fun seekAudio(positionMs: Long) = playback.seekTo(positionMs)
    fun togglePlayPause() = playback.togglePlayPause()
    fun nextAyah() = playback.nextAyah(::playCurrentPageFirstAyah)
    fun previousAyah() = playback.previousAyah(::playCurrentPageFirstAyah)
    fun toggleRepeatAyah() = playback.toggleRepeatAyah()
    fun setPlaybackSpeed(speed: Float) = playback.setPlaybackSpeed(speed)
    fun cyclePlaybackSpeed() = playback.cyclePlaybackSpeed()
    fun startMemorizationLoop(config: MemorizationLoopConfig) = playback.startMemorizationLoop(config)
    fun clearMemorizationLoop() = playback.clearMemorizationLoop()
    fun setSleepTimerMinutes(minutes: Int?) = playback.setSleepTimerMinutes(minutes)
    fun toggleStopAfterSurah() = playback.toggleStopAfterSurah()
    fun stopAudio() = playback.stop()

    override fun onCleared() {
        super.onCleared()
        if (deps.ownsAudioPlayer) playback.release()
    }

    companion object {
        fun factory(container: QuranAppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    QuranViewModel(container.quranViewModelDependencies(ownsAudioPlayer = false)) as T
            }
    }
}
