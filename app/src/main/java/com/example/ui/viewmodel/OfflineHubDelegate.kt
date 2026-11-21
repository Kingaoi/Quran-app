package com.example.ui.viewmodel

import com.example.data.adhkar.HisnMuslimRepository
import com.example.data.audio.SurahAudioStorage
import com.example.data.local.MushafOfflineStorage
import com.example.data.local.MushafRasterCache
import com.example.data.local.RiwayahEdition
import com.example.data.prayer.AdhanAudioStorage
import com.example.data.tafsir.TafsirRepository
import com.example.domain.model.DownloadHubActions
import com.example.domain.model.DownloadItemKeys
import com.example.domain.model.OfflineInventoryUiState
import com.example.domain.model.QuranReciters
import com.example.domain.model.SurahInfo
import com.example.domain.model.TafsirEdition
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.ConcurrentHashMap

class OfflineHubDelegate(
    private val scope: CoroutineScope,
    mushafStorage: MushafOfflineStorage,
    tafsirRepository: TafsirRepository,
    private val surahAudioStorage: SurahAudioStorage,
    hisnRepository: HisnMuslimRepository,
    private val rasterCache: MushafRasterCache? = null,
    adhanStorage: AdhanAudioStorage? = null,
    getSvgVersion: () -> String,
    private val getSurahs: () -> List<SurahInfo>,
    private val getActiveRiwayah: () -> RiwayahEdition,
    private val getActiveReciterId: () -> String,
    private val onInventoryChanged: () -> Unit = {}
) {
    private val worker = OfflineHubWorker(
        mushafStorage = mushafStorage,
        tafsirRepository = tafsirRepository,
        surahAudioStorage = surahAudioStorage,
        hisnRepository = hisnRepository,
        rasterCache = rasterCache,
        adhanStorage = adhanStorage,
        getSvgVersion = getSvgVersion,
        getSurahs = getSurahs
    )
    private val _state = MutableStateFlow(OfflineInventoryUiState())
    val state: StateFlow<OfflineInventoryUiState> = _state.asStateFlow()

    private val activeJobs = ConcurrentHashMap<String, Job>()
    private var batchJob: Job? = null
    private var singleSurahJob: Job? = null
    private var hasSeededDefaultSelection = false

    fun refreshInventory() {
        scope.launch {
            val snapshot = worker.readDiskSnapshot(_state.value, activeJobs.keys, getActiveReciterId())
            _state.update { cur ->
                val seeded = if (!hasSeededDefaultSelection && cur.selectedKeys.isEmpty()) {
                    hasSeededDefaultSelection = true
                    computeRecommendedKeys(snapshot, getActiveRiwayah(), getActiveReciterId())
                } else {
                    cur.selectedKeys
                }
                snapshot.copy(
                    selectedKeys = seeded,
                    activeKeys = activeJobs.keys.toSet(),
                    isBatchDownloading = activeJobs.isNotEmpty(),
                    statusMessage = cur.statusMessage
                )
            }
        }
    }

    fun toggleSelection(key: String) {
        _state.update { cur ->
            val next = if (key in cur.selectedKeys) cur.selectedKeys - key else cur.selectedKeys + key
            cur.copy(selectedKeys = next)
        }
    }

    fun selectRecommended() {
        _state.update { cur -> cur.copy(selectedKeys = computeRecommendedKeys(cur, getActiveRiwayah(), getActiveReciterId())) }
    }

    fun selectAllIncomplete() {
        _state.update { cur ->
            val allIncomplete = buildSet {
                cur.riwayahStatuses.filter { !it.isComplete }.forEach { add(it.key) }
                cur.tafsirStatuses.filter { !it.isComplete }.forEach { add(it.key) }
                cur.reciterStatuses.filter { !it.isComplete }.forEach { add(it.key) }
                if (!cur.adhkarStatus.isComplete) add(cur.adhkarStatus.key)
            }
            cur.copy(selectedKeys = allIncomplete)
        }
    }

    fun clearSelection() = _state.update { it.copy(selectedKeys = emptySet()) }

    private suspend fun applyRefreshedSnapshot(message: String? = null, clearDownloadingSingle: Boolean = false) {
        val refreshed = worker.readDiskSnapshot(_state.value, activeJobs.keys, getActiveReciterId())
        _state.update { prev ->
            refreshed.copy(
                selectedKeys = prev.selectedKeys,
                activeKeys = activeJobs.keys.toSet(),
                isBatchDownloading = activeJobs.isNotEmpty(),
                downloadingSingleSurahNum = if (clearDownloadingSingle) null else refreshed.downloadingSingleSurahNum,
                statusMessage = message
            )
        }
    }

    fun prepareEssentialOfflinePackage() {
        val st = _state.value
        val essentialKeys = listOf(
            DownloadItemKeys.riwayah(getActiveRiwayah()),
            DownloadItemKeys.tafsir(TafsirEdition.MUYASSAR),
            DownloadItemKeys.ADHKAR
        ).filter { !isItemComplete(st, it) }.toSet()
        if (essentialKeys.isEmpty()) {
            _state.update { it.copy(statusMessage = "الباقة الأساسية جاهزة بالكامل للعمل دون إنترنت ✓") }
            return
        }
        _state.update { it.copy(selectedKeys = it.selectedKeys + essentialKeys) }
        startConcurrentDownloads()
    }

    fun clearTemporaryRasterCache() {
        scope.launch {
            rasterCache?.clearAllCache()
            applyRefreshedSnapshot(message = "تم تفريغ الذاكرة المؤقتة للصفحات")
        }
    }

    fun downloadSingleSurah(surahNumber: Int) {
        if (singleSurahJob?.isActive == true) return
        val surah = getSurahs().firstOrNull { it.number == surahNumber } ?: return
        val reciter = QuranReciters.byId(getActiveReciterId())
        singleSurahJob = scope.launch {
            _state.update { it.copy(downloadingSingleSurahNum = surahNumber, statusMessage = null) }
            val res = surahAudioStorage.downloadSurah(reciter, surah.number, surah.ayahCount) { _, _ -> }
            applyRefreshedSnapshot(
                message = if (res.isSuccess) "تم تنزيل سورة ${surah.nameAr}" else "تعذّر تنزيل سورة ${surah.nameAr}",
                clearDownloadingSingle = true
            )
            onInventoryChanged()
        }
    }

    fun deleteSingleSurah(surahNumber: Int) {
        val surah = getSurahs().firstOrNull { it.number == surahNumber } ?: return
        val reciterId = getActiveReciterId()
        scope.launch {
            surahAudioStorage.deleteSurah(reciterId, surah.number, surah.ayahCount)
            applyRefreshedSnapshot(message = "تم حذف تلاوة سورة ${surah.nameAr}")
            onInventoryChanged()
        }
    }

    fun downloadJuzAmmaAudio() {
        if (singleSurahJob?.isActive == true) return
        val reciter = QuranReciters.byId(getActiveReciterId())
        val targets = getSurahs().filter { it.number in 78..114 && it.number !in _state.value.activeReciterDownloadedSurahNums }
        if (targets.isEmpty()) {
            _state.update { it.copy(statusMessage = "جزء عمّ منزّل بالكامل لهذا القارئ ✓") }
            return
        }
        singleSurahJob = scope.launch {
            var okCount = 0
            for (s in targets) {
                _state.update { it.copy(downloadingSingleSurahNum = s.number) }
                if (surahAudioStorage.downloadSurah(reciter, s.number, s.ayahCount) { _, _ -> }.isSuccess) okCount++
            }
            applyRefreshedSnapshot(message = "تم تنزيل $okCount سورة من جزء عمّ", clearDownloadingSingle = true)
            onInventoryChanged()
        }
    }

    fun startConcurrentDownloads() {
        val current = _state.value
        val targets = current.selectedKeys.filter { !activeJobs.containsKey(it) && !isItemComplete(current, it) }
        if (targets.isEmpty()) {
            _state.update { it.copy(statusMessage = "حدد عنصراً غير مكتمل أولاً لبدء التنزيل") }
            return
        }

        batchJob?.cancel()
        batchJob = scope.launch {
            val streamSemaphore = Semaphore(MAX_CONCURRENT_STREAMS)
            _state.update { st ->
                markDownloadingState(st, targets.toSet(), downloading = true).copy(
                    isBatchDownloading = true,
                    activeKeys = activeJobs.keys.toSet() + targets,
                    statusMessage = null
                )
            }
            val successCount = java.util.concurrent.atomic.AtomicInteger(0)
            val failCount = java.util.concurrent.atomic.AtomicInteger(0)
            try {
                coroutineScope {
                    targets.map { key ->
                        val job = async {
                            streamSemaphore.withPermit {
                                val res = worker.downloadItem(key, _state)
                                if (res.isSuccess) successCount.incrementAndGet() else failCount.incrementAndGet()
                            }
                        }
                        activeJobs[key] = job
                        job
                    }.awaitAll()
                }
            } catch (_: CancellationException) {
                // Partial files remain safely cached for instant resume
            } finally {
                targets.forEach { activeJobs.remove(it) }
                val refreshed = worker.readDiskSnapshot(_state.value, activeJobs.keys, getActiveReciterId())
                val ok = successCount.get()
                val err = failCount.get()
                val msg = when {
                    err > 0 && ok > 0 -> "اكتمل تنزيل $ok عناصر وتعذّر $err (يمكنك الاستكمال)"
                    err > 0 -> "تعذّر استكمال التنزيل، تحقق من اتصال الإنترنت"
                    ok > 0 -> "اكتمل تنزيل جميع العناصر المحددة بنجاح"
                    else -> null
                }
                _state.update { prev ->
                    refreshed.copy(
                        selectedKeys = prev.selectedKeys.filterNot { isItemComplete(refreshed, it) }.toSet(),
                        activeKeys = activeJobs.keys.toSet(),
                        isBatchDownloading = activeJobs.isNotEmpty(),
                        statusMessage = msg
                    )
                }
                onInventoryChanged()
            }
        }
    }

    fun cancelAllDownloads() {
        batchJob?.cancel(); batchJob = null
        singleSurahJob?.cancel(); singleSurahJob = null
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
        refreshInventory()
    }

    fun cancelSingleDownload(key: String) {
        activeJobs.remove(key)?.cancel()
        refreshInventory()
    }

    fun deleteItem(key: String) {
        activeJobs.remove(key)?.cancel()
        scope.launch {
            worker.deleteItem(key)
            applyRefreshedSnapshot(message = "تم حذف الملفات المحفوظة")
            onInventoryChanged()
        }
    }

    fun clearStatusMessage() = _state.update { it.copy(statusMessage = null) }

    fun createActions(): DownloadHubActions = DownloadHubActions(
        onRefresh = ::refreshInventory,
        onToggleSelection = ::toggleSelection,
        onSelectRecommended = ::selectRecommended,
        onSelectAllIncomplete = ::selectAllIncomplete,
        onClearSelection = ::clearSelection,
        onStartConcurrentDownload = ::startConcurrentDownloads,
        onCancelAllDownloads = ::cancelAllDownloads,
        onCancelSingleDownload = ::cancelSingleDownload,
        onDeleteItem = ::deleteItem,
        onClearStatusMessage = ::clearStatusMessage,
        onPrepareEssentialOffline = ::prepareEssentialOfflinePackage,
        onClearRasterCache = ::clearTemporaryRasterCache,
        onDownloadSingleSurah = ::downloadSingleSurah,
        onDeleteSingleSurah = ::deleteSingleSurah,
        onDownloadJuzAmmaAudio = ::downloadJuzAmmaAudio
    )

    private fun markDownloadingState(state: OfflineInventoryUiState, keys: Set<String>, downloading: Boolean): OfflineInventoryUiState =
        state.copy(
            riwayahStatuses = state.riwayahStatuses.map { if (it.key in keys) it.copy(isDownloading = downloading, errorMessage = null) else it },
            tafsirStatuses = state.tafsirStatuses.map { if (it.key in keys) it.copy(isDownloading = downloading, errorMessage = null) else it },
            reciterStatuses = state.reciterStatuses.map { if (it.key in keys) it.copy(isDownloading = downloading, errorMessage = null) else it },
            adhkarStatus = if (DownloadItemKeys.ADHKAR in keys) state.adhkarStatus.copy(isDownloading = downloading, errorMessage = null) else state.adhkarStatus
        )

    private fun computeRecommendedKeys(snapshot: OfflineInventoryUiState, activeRiwayah: RiwayahEdition, activeReciterId: String): Set<String> = buildSet {
        snapshot.riwayahStatuses.firstOrNull { it.edition == activeRiwayah && !it.isComplete }?.let { add(it.key) }
        snapshot.tafsirStatuses.firstOrNull { it.edition == TafsirEdition.MUYASSAR && !it.isComplete }?.let { add(it.key) }
        if (!snapshot.adhkarStatus.isComplete) add(snapshot.adhkarStatus.key)
        if (isEmpty()) snapshot.reciterStatuses.firstOrNull { it.reciter.id == activeReciterId && !it.isComplete }?.let { add(it.key) }
    }

    private fun isItemComplete(state: OfflineInventoryUiState, key: String): Boolean = when {
        DownloadItemKeys.isRiwayah(key) -> state.riwayahStatuses.firstOrNull { it.key == key }?.isComplete == true
        DownloadItemKeys.isTafsir(key) -> state.tafsirStatuses.firstOrNull { it.key == key }?.isComplete == true
        DownloadItemKeys.isReciter(key) -> state.reciterStatuses.firstOrNull { it.key == key }?.isComplete == true
        DownloadItemKeys.isAdhkar(key) -> state.adhkarStatus.isComplete
        else -> false
    }

    companion object {
        private const val MAX_CONCURRENT_STREAMS = 3
    }
}
