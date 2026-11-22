package com.example.ui.viewmodel

import com.example.data.local.MushafOfflineStorage
import com.example.data.local.RiwayahEdition
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuranMushafDownloadDelegate(
    private val scope: CoroutineScope,
    private val offlineStorage: MushafOfflineStorage,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val _offlineState = MutableStateFlow(OfflineDownloadState())
    val offlineState: StateFlow<OfflineDownloadState> = _offlineState.asStateFlow()

    var onInventoryChanged: () -> Unit = {}
    private var downloadJob: Job? = null

    fun setInitialState(edition: RiwayahEdition, downloadedCount: Int) {
        _offlineState.value = OfflineDownloadState(edition = edition, downloadedCount = downloadedCount)
    }

    fun refreshCount(edition: RiwayahEdition) {
        scope.launch {
            val count = withContext(ioDispatcher) { offlineStorage.countDownloadedPages(edition) }
            _offlineState.update {
                if (it.isDownloading && it.edition == edition) it
                else it.copy(edition = edition, downloadedCount = count)
            }
        }
    }

    fun startFullMushafDownload(edition: RiwayahEdition, svgVersion: String) {
        if (_offlineState.value.isDownloading) return
        downloadJob?.cancel()
        downloadJob = scope.launch {
            val currentCount = withContext(ioDispatcher) { offlineStorage.countDownloadedPages(edition) }
            _offlineState.value = OfflineDownloadState(
                edition = edition,
                downloadedCount = currentCount,
                isDownloading = true
            )
            val result = offlineStorage.downloadFullMushaf(edition, svgVersion) { downloaded, total ->
                _offlineState.update { it.copy(downloadedCount = downloaded, totalPages = total) }
            }
            val finalCount = withContext(ioDispatcher) { offlineStorage.countDownloadedPages(edition) }
            _offlineState.value = OfflineDownloadState(
                edition = edition,
                downloadedCount = finalCount,
                isDownloading = false,
                errorMessage = result.exceptionOrNull()?.message
            )
            onInventoryChanged()
        }
    }

    fun cancelFullMushafDownload(edition: RiwayahEdition) {
        downloadJob?.cancel()
        downloadJob = null
        scope.launch {
            val count = withContext(ioDispatcher) { offlineStorage.countDownloadedPages(edition) }
            _offlineState.value = OfflineDownloadState(
                edition = edition,
                downloadedCount = count,
                isDownloading = false
            )
            onInventoryChanged()
        }
    }

    fun deleteDownloadedMushaf(edition: RiwayahEdition) {
        downloadJob?.cancel()
        downloadJob = null
        scope.launch {
            offlineStorage.deleteEdition(edition)
            _offlineState.value = OfflineDownloadState(
                edition = edition,
                downloadedCount = 0,
                isDownloading = false
            )
            onInventoryChanged()
        }
    }
}
