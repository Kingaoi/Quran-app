package com.example.ui.viewmodel

import com.example.data.adhkar.HisnMuslimRepository
import com.example.data.audio.SurahAudioStorage
import com.example.data.local.MushafOfflineStorage
import com.example.data.local.MushafRasterCache
import com.example.data.local.RiwayahEdition
import com.example.data.prayer.AdhanAudioStorage
import com.example.data.tafsir.TafsirRepository
import com.example.domain.model.AdhkarDownloadStatus
import com.example.domain.model.DownloadItemKeys
import com.example.domain.model.OfflineInventoryUiState
import com.example.domain.model.QuranReciters
import com.example.domain.model.ReciterAudioStatus
import com.example.domain.model.RiwayahDownloadStatus
import com.example.domain.model.SurahInfo
import com.example.domain.model.TafsirDownloadStatus
import com.example.domain.model.TafsirEdition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicLong

internal class OfflineHubWorker(
    private val mushafStorage: MushafOfflineStorage,
    private val tafsirRepository: TafsirRepository,
    private val surahAudioStorage: SurahAudioStorage,
    private val hisnRepository: HisnMuslimRepository,
    private val rasterCache: MushafRasterCache? = null,
    private val adhanStorage: AdhanAudioStorage? = null,
    private val getSvgVersion: () -> String,
    private val getSurahs: () -> List<SurahInfo>
) {
    suspend fun readDiskSnapshot(
        prev: OfflineInventoryUiState,
        activeKeys: Set<String>,
        activeReciterId: String = QuranReciters.all.first().id
    ): OfflineInventoryUiState = withContext(Dispatchers.IO) {
        val surahsList = getSurahs()
        val riwayahs = RiwayahEdition.entries.map { ed ->
            val old = prev.riwayahStatuses.firstOrNull { it.edition == ed }
            RiwayahDownloadStatus(
                edition = ed,
                downloadedPages = mushafStorage.countDownloadedPages(ed),
                sizeBytes = mushafStorage.getEditionStorageBytes(ed),
                isDownloading = DownloadItemKeys.riwayah(ed) in activeKeys,
                errorMessage = old?.errorMessage
            )
        }
        val tafsirs = TafsirEdition.entries.map { ed ->
            val old = prev.tafsirStatuses.firstOrNull { it.edition == ed }
            TafsirDownloadStatus(
                edition = ed,
                downloadedSurahs = tafsirRepository.countDownloadedSurahs(ed),
                sizeBytes = tafsirRepository.getEditionStorageBytes(ed),
                isDownloading = DownloadItemKeys.tafsir(ed) in activeKeys,
                errorMessage = old?.errorMessage
            )
        }
        val totalAyahs = surahsList.sumOf { it.ayahCount }.takeIf { it > 0 } ?: SurahAudioStorage.TOTAL_QURAN_AYAHS
        val reciters = QuranReciters.all.map { rec ->
            val old = prev.reciterStatuses.firstOrNull { it.reciter.id == rec.id }
            ReciterAudioStatus(
                reciter = rec,
                downloadedAyahs = surahAudioStorage.countDownloadedAyahsForReciter(rec.id),
                totalAyahs = totalAyahs,
                sizeBytes = surahAudioStorage.getReciterStorageBytes(rec.id),
                isDownloading = DownloadItemKeys.reciter(rec.id) in activeKeys,
                errorMessage = old?.errorMessage
            )
        }
        val (hasHisnCache, hisnBytes) = hisnRepository.getCacheStatus()
        val adhkarStatus = AdhkarDownloadStatus(
            isSynced = hasHisnCache,
            sizeBytes = hisnBytes,
            isDownloading = DownloadItemKeys.ADHKAR in activeKeys,
            errorMessage = prev.adhkarStatus.errorMessage
        )
        val downloadedSurahNums = if (surahsList.isNotEmpty()) {
            surahAudioStorage.getDownloadedSurahNumbers(activeReciterId, surahsList)
        } else {
            prev.activeReciterDownloadedSurahNums
        }
        OfflineInventoryUiState(
            riwayahStatuses = riwayahs,
            tafsirStatuses = tafsirs,
            reciterStatuses = reciters,
            adhkarStatus = adhkarStatus,
            rasterCacheBytes = rasterCache?.getTotalSizeBytes() ?: 0L,
            adhanAudioBytes = adhanStorage?.getTotalStorageBytes() ?: 0L,
            cachedAdhanVoicesCount = adhanStorage?.cachedVoiceIds()?.size ?: 0,
            activeReciterDownloadedSurahs = downloadedSurahNums.size,
            activeReciterDownloadedSurahNums = downloadedSurahNums,
            surahsCatalog = surahsList,
            downloadingSingleSurahNum = prev.downloadingSingleSurahNum
        )
    }

    suspend fun downloadItem(
        key: String,
        stateFlow: MutableStateFlow<OfflineInventoryUiState>
    ): Result<Int> {
        val lastEmitMs = AtomicLong(0L)
        return when {
            DownloadItemKeys.isRiwayah(key) -> downloadRiwayah(key.removePrefix("riwayah:"), lastEmitMs, stateFlow)
            DownloadItemKeys.isTafsir(key) -> downloadTafsir(key.removePrefix("tafsir:"), lastEmitMs, stateFlow)
            DownloadItemKeys.isReciter(key) -> downloadReciter(key.removePrefix("audio:"), lastEmitMs, stateFlow)
            DownloadItemKeys.isAdhkar(key) -> downloadAdhkar(lastEmitMs, stateFlow)
            else -> Result.success(0)
        }
    }

    private suspend fun downloadRiwayah(
        slug: String,
        lastEmitMs: AtomicLong,
        stateFlow: MutableStateFlow<OfflineInventoryUiState>
    ): Result<Int> {
        val edition = RiwayahEdition.fromSlug(slug)
        val res = mushafStorage.downloadFullMushaf(edition, getSvgVersion()) { done, total ->
            if (shouldEmit(done, total, lastEmitMs)) {
                stateFlow.update { st ->
                    st.copy(
                        riwayahStatuses = st.riwayahStatuses.map {
                            if (it.edition == edition) it.copy(downloadedPages = done, totalPages = total, isDownloading = true) else it
                        }
                    )
                }
            }
        }
        val bytes = withContext(Dispatchers.IO) { mushafStorage.getEditionStorageBytes(edition) }
        val count = withContext(Dispatchers.IO) { mushafStorage.countDownloadedPages(edition) }
        stateFlow.update { st ->
            st.copy(
                riwayahStatuses = st.riwayahStatuses.map {
                    if (it.edition == edition) {
                        it.copy(downloadedPages = count, sizeBytes = bytes, isDownloading = false, errorMessage = res.exceptionOrNull()?.message)
                    } else it
                }
            )
        }
        return res
    }

    private suspend fun downloadTafsir(
        slug: String,
        lastEmitMs: AtomicLong,
        stateFlow: MutableStateFlow<OfflineInventoryUiState>
    ): Result<Int> {
        val edition = TafsirEdition.entries.firstOrNull { it.slug == slug }
            ?: return Result.failure(IllegalArgumentException("Unknown tafsir"))
        val res = tafsirRepository.downloadFullTafsir(edition) { done, total ->
            if (shouldEmit(done, total, lastEmitMs)) {
                stateFlow.update { st ->
                    st.copy(
                        tafsirStatuses = st.tafsirStatuses.map {
                            if (it.edition == edition) it.copy(downloadedSurahs = done, totalSurahs = total, isDownloading = true) else it
                        }
                    )
                }
            }
        }
        val bytes = withContext(Dispatchers.IO) { tafsirRepository.getEditionStorageBytes(edition) }
        val count = withContext(Dispatchers.IO) { tafsirRepository.countDownloadedSurahs(edition) }
        stateFlow.update { st ->
            st.copy(
                tafsirStatuses = st.tafsirStatuses.map {
                    if (it.edition == edition) {
                        it.copy(downloadedSurahs = count, sizeBytes = bytes, isDownloading = false, errorMessage = res.exceptionOrNull()?.message)
                    } else it
                }
            )
        }
        return res
    }

    private suspend fun downloadReciter(
        reciterId: String,
        lastEmitMs: AtomicLong,
        stateFlow: MutableStateFlow<OfflineInventoryUiState>
    ): Result<Int> {
        val reciter = QuranReciters.byId(reciterId)
        val surahs = getSurahs()
        val res = surahAudioStorage.downloadFullReciterAudio(reciter, surahs) { done, total ->
            if (shouldEmit(done, total, lastEmitMs)) {
                stateFlow.update { st ->
                    st.copy(
                        reciterStatuses = st.reciterStatuses.map {
                            if (it.reciter.id == reciter.id) it.copy(downloadedAyahs = done, totalAyahs = total, isDownloading = true) else it
                        }
                    )
                }
            }
        }
        val bytes = withContext(Dispatchers.IO) { surahAudioStorage.getReciterStorageBytes(reciter.id) }
        val count = withContext(Dispatchers.IO) { surahAudioStorage.countDownloadedAyahsForReciter(reciter.id) }
        stateFlow.update { st ->
            st.copy(
                reciterStatuses = st.reciterStatuses.map {
                    if (it.reciter.id == reciter.id) {
                        it.copy(downloadedAyahs = count, sizeBytes = bytes, isDownloading = false, errorMessage = res.exceptionOrNull()?.message)
                    } else it
                }
            )
        }
        return res
    }

    private suspend fun downloadAdhkar(
        lastEmitMs: AtomicLong,
        stateFlow: MutableStateFlow<OfflineInventoryUiState>
    ): Result<Int> {
        val res = hisnRepository.fetchAndCacheFromApi { done, total ->
            if (shouldEmit(done, total, lastEmitMs)) {
                stateFlow.update { st ->
                    st.copy(
                        adhkarStatus = st.adhkarStatus.copy(
                            downloadedChapters = done,
                            totalChapters = total,
                            isDownloading = true
                        )
                    )
                }
            }
        }
        val (exists, bytes) = withContext(Dispatchers.IO) { hisnRepository.getCacheStatus() }
        stateFlow.update { st ->
            st.copy(
                adhkarStatus = st.adhkarStatus.copy(
                    isSynced = exists,
                    sizeBytes = bytes,
                    isDownloading = false,
                    errorMessage = res.exceptionOrNull()?.message
                )
            )
        }
        return res.map { it.values.sumOf { list -> list.size } }
    }

    suspend fun deleteItem(key: String) {
        when {
            DownloadItemKeys.isRiwayah(key) -> mushafStorage.deleteEdition(RiwayahEdition.fromSlug(key.removePrefix("riwayah:")))
            DownloadItemKeys.isTafsir(key) -> {
                val slug = key.removePrefix("tafsir:")
                TafsirEdition.entries.firstOrNull { it.slug == slug }?.let { tafsirRepository.deleteEdition(it) }
            }
            DownloadItemKeys.isReciter(key) -> surahAudioStorage.deleteReciter(key.removePrefix("audio:"))
            DownloadItemKeys.isAdhkar(key) -> hisnRepository.clearCache()
        }
    }

    private fun shouldEmit(done: Int, total: Int, lastEmitMs: AtomicLong): Boolean {
        val now = System.currentTimeMillis()
        if (done >= total || now - lastEmitMs.get() >= 140L) {
            lastEmitMs.set(now)
            return true
        }
        return false
    }
}
