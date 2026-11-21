package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import com.example.QuranApplication
import com.example.data.adhkar.HisnMuslimRepository
import com.example.data.audio.AudioPlaybackState
import com.example.data.audio.MemorizationLoopConfig
import com.example.data.audio.PlaybackNotificationButton
import com.example.data.audio.QuranAudioPlayer
import com.example.data.audio.RiwayahRecitersRepository
import com.example.data.audio.SurahAudioDownloadState
import com.example.data.audio.SurahAudioStorage
import com.example.data.audio.WirdAudioRange
import com.example.data.local.AppTheme
import com.example.data.local.AyahBookmark
import com.example.data.local.AyahHighlightColorPreset
import com.example.data.local.AyahHighlightStyle
import com.example.data.local.AyahMarkerColorPreset
import com.example.data.local.AyahMarkerColorTarget
import com.example.data.local.MushafOfflineStorage
import com.example.data.local.MushafRasterCache
import com.example.data.local.QuranFontOption
import com.example.data.local.ReaderPreferencesRepository
import com.example.data.local.ReadingStats
import com.example.data.local.RiwayahEdition
import com.example.data.prayer.AdhanAudioStorage
import com.example.data.repository.QuranRepository
import com.example.domain.model.AyahItem
import com.example.domain.model.OfflineInventoryUiState
import com.example.domain.model.QuranReciters
import com.example.domain.model.ReciterOption
import com.example.domain.model.SearchHit
import com.example.domain.quran.AyahWordDiffSegment
import com.example.domain.quran.MutashabihMatch
import com.example.domain.quran.QuranConcordanceReport
import com.example.domain.quran.QuranSearchMode
import com.example.ui.util.AppShortcutsHelper
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

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as? QuranApplication)?.quranRepository ?: QuranRepository(application)
    private val prefsRepository = ReaderPreferencesRepository(application)
    val offlineStorage = MushafOfflineStorage(application)
    private val rasterCache = (application as? QuranApplication)?.mushafRasterCache ?: MushafRasterCache(application)
    private val surahAudioStorage = SurahAudioStorage(application)
    private val riwayahRecitersRepo = RiwayahRecitersRepository(application)
    private val hisnRepository = HisnMuslimRepository(application)
    val extras = QuranExtrasDelegate(application, viewModelScope, surahAudioStorage)
    val offlineHub = OfflineHubDelegate(
        scope = viewModelScope, mushafStorage = offlineStorage, tafsirRepository = extras.tafsirRepository,
        surahAudioStorage = surahAudioStorage, hisnRepository = hisnRepository, rasterCache = rasterCache,
        adhanStorage = AdhanAudioStorage(application), getSvgVersion = { _uiState.value.svgVersion },
        getSurahs = { _uiState.value.surahs.ifEmpty { repository.getSurahs() } },
        getActiveRiwayah = { _uiState.value.settings.riwayah }, getActiveReciterId = { _uiState.value.settings.selectedReciterId },
        onInventoryChanged = { refreshOfflineCount(_uiState.value.settings.riwayah); extras.refreshTafsirCount() }
    )

    private val ownsAudioPlayer = (application as? QuranApplication) == null
    private val audioPlayer = (application as? QuranApplication)?.audioPlayer ?: QuranAudioPlayer(application, surahAudioStorage) { surahNum ->
        repository.getSurahMetaOrDefault(surahNum)
    }

    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    private val _offlineState = MutableStateFlow(OfflineDownloadState())
    val offlineState: StateFlow<OfflineDownloadState> = _offlineState.asStateFlow()
    private val _highlightedSearchAyah = MutableStateFlow<Pair<Int, Int>?>(null)
    val highlightedSearchAyah: StateFlow<Pair<Int, Int>?> = _highlightedSearchAyah.asStateFlow()
    private val _reciterCatalog = MutableStateFlow(QuranReciters.all)
    val reciterCatalog: StateFlow<List<ReciterOption>> = _reciterCatalog.asStateFlow()

    val playbackState: StateFlow<AudioPlaybackState> = audioPlayer.state
    val bookmarksState: StateFlow<List<AyahBookmark>> = extras.bookmarksState
    val statsState: StateFlow<ReadingStats> = extras.statsState
    val tafsirOfflineState = extras.tafsirOfflineState
    val ayahTafsirState: StateFlow<AyahTafsirUiState> = extras.ayahTafsirState
    val surahAudioState: StateFlow<SurahAudioDownloadState> = extras.surahAudioState
    val offlineHubState: StateFlow<OfflineInventoryUiState> = offlineHub.state

    private var downloadJob: Job? = null
    private var highlightClearJob: Job? = null
    private var lastObservedRiwayah: RiwayahEdition? = null
    private var lastObservedReciterId: String? = null
    private var lastShortcutSyncKey: Pair<Int, Boolean>? = null
    private var initializedSettledPage: Int = -1

    init {
        extras.onStorageMutated = { offlineHub.refreshInventory() }
        initialize()
        observeSettings()
    }

    private fun syncShortcuts(page: Int, surahName: String?, useWestern: Boolean) {
        val key = page to useWestern
        if (lastShortcutSyncKey == key) return
        lastShortcutSyncKey = key
        AppShortcutsHelper.syncDynamicShortcuts(getApplication(), page, surahName, useWestern)
    }

    private fun initialize() {
        viewModelScope.launch {
            runCatching {
                repository.ensureLoaded()
                val initial = prefsRepository.settingsFlow.first()
                audioPlayer.setDefaultReciter(initial.selectedReciterId)
                audioPlayer.updateNotificationConfig(initial.playbackNotificationConfig)
                val fontFamily = withContext(Dispatchers.IO) {
                    repository.getUthmanicFontFile()?.takeIf { it.exists() }?.let { runCatching { FontFamily(Font(it)) }.getOrNull() }
                }
                val page = initial.lastPage.coerceIn(1, 604)
                val downloaded = withContext(Dispatchers.IO) {
                    rasterCache.warmIndex()
                    com.example.ui.components.MushafCropCoordinateMap.warmCache(getApplication())
                    offlineStorage.countDownloadedPages(initial.riwayah)
                }
                lastObservedRiwayah = initial.riwayah; lastObservedReciterId = initial.selectedReciterId
                initializedSettledPage = page; _currentPage.value = page
                _offlineState.value = OfflineDownloadState(edition = initial.riwayah, downloadedCount = downloaded)
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
                launch {
                    riwayahRecitersRepo.loadMultiRiwayahReciters()
                    _reciterCatalog.value = QuranReciters.combinedCatalog()
                    audioPlayer.setDefaultReciter(_uiState.value.settings.selectedReciterId)
                }
                val ver = repository.resolveSvgVersion()
                _uiState.update { it.copy(svgVersion = ver) }
                val ctx = getApplication<Application>()
                listOf(page, (page + 1).coerceAtMost(604)).distinct().forEach { p ->
                    val model = offlineStorage.getCachedPageFileFast(initial.riwayah, p) ?: repository.getSvgPageUrl(ver, initial.riwayah, p)
                    ctx.imageLoader.enqueue(coil.request.ImageRequest.Builder(ctx).data(model).build())
                }
            }.onFailure { err -> _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage ?: "حدث خطأ أثناء تحميل بيانات المصحف") } }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            prefsRepository.settingsFlow.collect { settings ->
                _uiState.update { if (it.settings == settings) it else it.copy(settings = settings) }
                audioPlayer.updateNotificationConfig(settings.playbackNotificationConfig)
                if (!_uiState.value.isLoading) syncShortcuts(_currentPage.value, _uiState.value.currentPageSurah?.nameAr, settings.useWesternDigits)
                if (lastObservedReciterId != settings.selectedReciterId) {
                    lastObservedReciterId = settings.selectedReciterId
                    audioPlayer.setDefaultReciter(settings.selectedReciterId)
                    _uiState.value.currentPageSurah?.let { extras.refreshSurahAudioState(settings.selectedReciterId, it.number, it.ayahCount) }
                }
                if (lastObservedRiwayah != settings.riwayah) { lastObservedRiwayah = settings.riwayah; refreshOfflineCount(settings.riwayah) }
            }
        }
    }

    private fun refreshOfflineCount(edition: RiwayahEdition) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) { offlineStorage.countDownloadedPages(edition) }
            _offlineState.update { if (it.isDownloading && it.edition == edition) it else it.copy(edition = edition, downloadedCount = count) }
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
        viewModelScope.launch { prefsRepository.updateLastPosition(clamped, firstAyah?.surahNumber ?: 1, firstAyah?.ayahNumber ?: 1) }
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

    fun startFullMushafDownload() {
        if (_offlineState.value.isDownloading) return
        val edition = _uiState.value.settings.riwayah; val version = _uiState.value.svgVersion
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            _offlineState.value = OfflineDownloadState(edition = edition, downloadedCount = offlineStorage.countDownloadedPages(edition), isDownloading = true)
            val result = offlineStorage.downloadFullMushaf(edition, version) { downloaded, total -> _offlineState.update { it.copy(downloadedCount = downloaded, totalPages = total) } }
            val finalCount = withContext(Dispatchers.IO) { offlineStorage.countDownloadedPages(edition) }
            _offlineState.value = OfflineDownloadState(edition = edition, downloadedCount = finalCount, isDownloading = false, errorMessage = result.exceptionOrNull()?.message)
            offlineHub.refreshInventory()
        }
    }

    fun cancelFullMushafDownload() {
        downloadJob?.cancel(); downloadJob = null; val edition = _uiState.value.settings.riwayah
        viewModelScope.launch {
            _offlineState.value = OfflineDownloadState(edition = edition, downloadedCount = withContext(Dispatchers.IO) { offlineStorage.countDownloadedPages(edition) }, isDownloading = false)
            offlineHub.refreshInventory()
        }
    }

    fun deleteDownloadedMushaf() {
        downloadJob?.cancel(); val edition = _uiState.value.settings.riwayah
        viewModelScope.launch {
            offlineStorage.deleteEdition(edition)
            _offlineState.value = OfflineDownloadState(edition = edition, downloadedCount = 0, isDownloading = false)
            offlineHub.refreshInventory()
        }
    }

    fun toggleBookmark(page: Int) { viewModelScope.launch { prefsRepository.toggleBookmark(page) } }
    fun setRiwayah(edition: RiwayahEdition) { viewModelScope.launch { prefsRepository.setRiwayah(edition) } }
    fun setAppTheme(theme: AppTheme) { viewModelScope.launch { prefsRepository.setAppTheme(theme) } }
    fun setAyahMarkerColor(preset: AyahMarkerColorPreset) { viewModelScope.launch { prefsRepository.setAyahMarkerColor(preset) } }
    fun setAyahMarkerColorTarget(target: AyahMarkerColorTarget) { viewModelScope.launch { prefsRepository.setAyahMarkerColorTarget(target) } }
    fun setAyahHighlightColor(preset: AyahHighlightColorPreset) { viewModelScope.launch { prefsRepository.setAyahHighlightColor(preset) } }
    fun setAyahHighlightStyle(style: AyahHighlightStyle) { viewModelScope.launch { prefsRepository.setAyahHighlightStyle(style) } }
    fun setAyahHighlightAlpha(alpha: Float) { viewModelScope.launch { prefsRepository.setAyahHighlightAlpha(alpha) } }
    fun setSurahBannerEnabled(enabled: Boolean) { viewModelScope.launch { prefsRepository.setSurahBannerEnabled(enabled) } }
    fun setFontSize(sizeSp: Float) { viewModelScope.launch { prefsRepository.setFontSize(sizeSp) } }
    fun setQuranFont(option: QuranFontOption) { viewModelScope.launch { prefsRepository.setQuranFont(option) } }
    fun resetFontSettings() { viewModelScope.launch { prefsRepository.resetFontSettings() } }
    fun setUseWesternDigits(useWestern: Boolean) { viewModelScope.launch { prefsRepository.setUseWesternDigits(useWestern); com.example.data.widget.QuranSummaryWidgetProvider.notifyWidgetDataChanged(getApplication()) } }
    fun setKeepScreenOn(enabled: Boolean) { viewModelScope.launch { prefsRepository.setKeepScreenOn(enabled) } }
    fun setReaderDimmerAlpha(alpha: Float) { viewModelScope.launch { prefsRepository.setReaderDimmerAlpha(alpha) } }
    fun setTwoPageSpreadOnTablet(enabled: Boolean) { viewModelScope.launch { prefsRepository.setTwoPageSpreadOnTablet(enabled) } }
    fun setAutoAdvanceDhikr(enabled: Boolean) { viewModelScope.launch { prefsRepository.setAutoAdvanceDhikr(enabled) } }
    fun setHapticFeedbackEnabled(enabled: Boolean) { viewModelScope.launch { prefsRepository.setHapticFeedbackEnabled(enabled) } }
    fun setWirdPureReadingMode(enabled: Boolean) { viewModelScope.launch { prefsRepository.setWirdPureReadingMode(enabled) } }
    fun setHideWirdBannerOutsideWird(enabled: Boolean) { viewModelScope.launch { prefsRepository.setHideWirdBannerOutsideWird(enabled) } }
    fun setPlaybackNotifEnabled(enabled: Boolean) { viewModelScope.launch { prefsRepository.updatePlaybackNotifConfig { it.copy(enabled = enabled) } } }
    fun togglePlaybackNotifButton(button: PlaybackNotificationButton) { viewModelScope.launch { prefsRepository.updatePlaybackNotifConfig { it.toggledButton(button) } } }
    fun setPlaybackNotifShowWhenPaused(show: Boolean) { viewModelScope.launch { prefsRepository.updatePlaybackNotifConfig { it.copy(showWhenPaused = show) } } }
    fun setReciter(reciterId: String) { viewModelScope.launch { prefsRepository.setReciter(reciterId); audioPlayer.setDefaultReciter(reciterId) } }

    fun playAyah(surah: Int, ayah: Int) = audioPlayer.playAyah(surah, ayah)
    fun togglePlayWirdRange(range: WirdAudioRange) = audioPlayer.togglePlayWirdRange(range)
    fun playPlaylist(playlist: com.example.data.audio.AudioPlaylist, startTrackIndex: Int = 0) = audioPlayer.playPlaylist(playlist, startTrackIndex)
    fun togglePlayPlaylist(playlist: com.example.data.audio.AudioPlaylist) {
        val cur = playbackState.value
        if (cur.isActive && cur.playlistSession?.playlistId == playlist.id) audioPlayer.togglePlayPause() else audioPlayer.playPlaylist(playlist, 0)
    }
    fun nextPlaylistTrack() = audioPlayer.nextPlaylistTrack(); fun previousPlaylistTrack() = audioPlayer.previousPlaylistTrack()
    fun togglePlaylistSessionRepeat() = audioPlayer.togglePlaylistSessionRepeat()
    fun playCurrentPageFirstAyah() { repository.getAyahsForPage(_currentPage.value).firstOrNull()?.let { audioPlayer.playAyah(it.surahNumber, it.ayahNumber) } }
    fun seekAudio(positionMs: Long) = audioPlayer.seekTo(positionMs)
    fun togglePlayPause() = audioPlayer.togglePlayPause()
    fun nextAyah() { if (!playbackState.value.isActive) playCurrentPageFirstAyah() else audioPlayer.nextAyah() }
    fun previousAyah() { if (!playbackState.value.isActive) playCurrentPageFirstAyah() else audioPlayer.previousAyah() }
    fun toggleRepeatAyah() = audioPlayer.toggleRepeatAyah()
    fun setPlaybackSpeed(speed: Float) = audioPlayer.setPlaybackSpeed(speed)
    fun cyclePlaybackSpeed() = audioPlayer.cyclePlaybackSpeed()
    fun startMemorizationLoop(config: MemorizationLoopConfig) = audioPlayer.startMemorizationLoop(config)
    fun clearMemorizationLoop() = audioPlayer.clearMemorizationLoop()
    fun setSleepTimerMinutes(minutes: Int?) = audioPlayer.setSleepTimerMinutes(minutes ?: 0)
    fun toggleStopAfterSurah() = audioPlayer.toggleStopAfterSurah()
    fun stopAudio() = audioPlayer.stop()
    override fun onCleared() { super.onCleared(); if (ownsAudioPlayer) audioPlayer.release() }
}
