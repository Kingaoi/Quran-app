package com.example.domain.model

import androidx.compose.runtime.Immutable
import com.example.data.audio.SurahAudioStorage
import com.example.data.local.MushafOfflineStorage
import com.example.data.local.RiwayahEdition
import java.util.Locale

enum class DownloadHubCategory(val titleAr: String) {
    ALL("الكل"),
    MUSHAF("المصحف"),
    TAFSIR("التفسير"),
    AUDIO("التلاوات"),
    ADHKAR("الأذكار")
}

object DownloadItemKeys {
    const val ADHKAR = "adhkar:hisn"
    fun riwayah(edition: RiwayahEdition): String = "riwayah:${edition.slug}"
    fun tafsir(edition: TafsirEdition): String = "tafsir:${edition.slug}"
    fun reciter(reciterId: String): String = "audio:$reciterId"

    fun isRiwayah(key: String): Boolean = key.startsWith("riwayah:")
    fun isTafsir(key: String): Boolean = key.startsWith("tafsir:")
    fun isReciter(key: String): Boolean = key.startsWith("audio:")
    fun isAdhkar(key: String): Boolean = key == ADHKAR
}

@Immutable
data class RiwayahDownloadStatus(
    val edition: RiwayahEdition,
    val downloadedPages: Int = 0,
    val totalPages: Int = MushafOfflineStorage.TOTAL_PAGES,
    val sizeBytes: Long = 0L,
    val isDownloading: Boolean = false,
    val errorMessage: String? = null
) {
    val key: String get() = DownloadItemKeys.riwayah(edition)
    val isComplete: Boolean get() = downloadedPages >= totalPages
    val isPartial: Boolean get() = downloadedPages in 1 until totalPages
    val progress: Float get() = if (totalPages <= 0) 0f else (downloadedPages.toFloat() / totalPages.toFloat()).coerceIn(0f, 1f)
}

@Immutable
data class TafsirDownloadStatus(
    val edition: TafsirEdition,
    val downloadedSurahs: Int = 0,
    val totalSurahs: Int = 114,
    val sizeBytes: Long = 0L,
    val isDownloading: Boolean = false,
    val errorMessage: String? = null
) {
    val key: String get() = DownloadItemKeys.tafsir(edition)
    val isComplete: Boolean get() = downloadedSurahs >= totalSurahs
    val isPartial: Boolean get() = downloadedSurahs in 1 until totalSurahs
    val progress: Float get() = if (totalSurahs <= 0) 0f else (downloadedSurahs.toFloat() / totalSurahs.toFloat()).coerceIn(0f, 1f)
}

@Immutable
data class ReciterAudioStatus(
    val reciter: ReciterOption,
    val downloadedAyahs: Int = 0,
    val totalAyahs: Int = SurahAudioStorage.TOTAL_QURAN_AYAHS,
    val sizeBytes: Long = 0L,
    val isDownloading: Boolean = false,
    val errorMessage: String? = null
) {
    val key: String get() = DownloadItemKeys.reciter(reciter.id)
    val isComplete: Boolean get() = downloadedAyahs >= totalAyahs
    val isPartial: Boolean get() = downloadedAyahs in 1 until totalAyahs
    val progress: Float get() = if (totalAyahs <= 0) 0f else (downloadedAyahs.toFloat() / totalAyahs.toFloat()).coerceIn(0f, 1f)
}

@Immutable
data class AdhkarDownloadStatus(
    val isSynced: Boolean = false,
    val downloadedChapters: Int = 0,
    val totalChapters: Int = 132,
    val sizeBytes: Long = 0L,
    val isDownloading: Boolean = false,
    val errorMessage: String? = null
) {
    val key: String get() = DownloadItemKeys.ADHKAR
    val isComplete: Boolean get() = isSynced
    val progress: Float
        get() = when {
            isSynced -> 1f
            totalChapters <= 0 -> 0f
            else -> (downloadedChapters.toFloat() / totalChapters.toFloat()).coerceIn(0f, 1f)
        }
}

@Immutable
data class OfflineInventoryUiState(
    val riwayahStatuses: List<RiwayahDownloadStatus> = RiwayahEdition.entries.map { RiwayahDownloadStatus(edition = it) },
    val tafsirStatuses: List<TafsirDownloadStatus> = TafsirEdition.entries.map { TafsirDownloadStatus(edition = it) },
    val reciterStatuses: List<ReciterAudioStatus> = QuranReciters.all.map { ReciterAudioStatus(reciter = it) },
    val adhkarStatus: AdhkarDownloadStatus = AdhkarDownloadStatus(),
    val rasterCacheBytes: Long = 0L,
    val adhanAudioBytes: Long = 0L,
    val cachedAdhanVoicesCount: Int = 0,
    val activeReciterDownloadedSurahs: Int = 0,
    val activeReciterDownloadedSurahNums: Set<Int> = emptySet(),
    val surahsCatalog: List<SurahInfo> = emptyList(),
    val downloadingSingleSurahNum: Int? = null,
    val selectedKeys: Set<String> = emptySet(),
    val activeKeys: Set<String> = emptySet(),
    val isBatchDownloading: Boolean = false,
    val statusMessage: String? = null
) {
    val completedRiwayahs: List<RiwayahDownloadStatus> get() = riwayahStatuses.filter { it.isComplete }
    val startedRiwayahs: List<RiwayahDownloadStatus> get() = riwayahStatuses.filter { it.downloadedPages > 0 }
    val completedTafsirs: List<TafsirDownloadStatus> get() = tafsirStatuses.filter { it.isComplete }
    val startedReciters: List<ReciterAudioStatus> get() = reciterStatuses.filter { it.downloadedAyahs > 0 }

    val mushafStorageBytes: Long get() = riwayahStatuses.sumOf { it.sizeBytes }
    val tafsirStorageBytes: Long get() = tafsirStatuses.sumOf { it.sizeBytes }
    val audioStorageBytes: Long get() = reciterStatuses.sumOf { it.sizeBytes }
    val adhkarAndAdhanBytes: Long get() = adhkarStatus.sizeBytes + adhanAudioBytes

    val totalStorageBytes: Long
        get() = mushafStorageBytes + tafsirStorageBytes + audioStorageBytes + adhkarStatus.sizeBytes

    val totalManagedStorageBytes: Long
        get() = totalStorageBytes + adhanAudioBytes + rasterCacheBytes

    fun isOfflineCoreReady(activeRiwayah: RiwayahEdition): Boolean =
        completedRiwayahs.any { it.edition == activeRiwayah } &&
            completedTafsirs.isNotEmpty() &&
            adhkarStatus.isSynced

    fun offlineReadinessLabelAr(activeRiwayah: RiwayahEdition): String = when {
        isOfflineCoreReady(activeRiwayah) -> "جاهز للعمل الكامل بدون إنترنت ✓"
        completedRiwayahs.any { it.edition == activeRiwayah } -> "المصحف جاهز • يُنصح بتحميل التفسير والأذكار"
        else -> "يحتاج لتنزيل المصحف الأساسي للعمل بدون إنترنت"
    }

    val selectableSelectedCount: Int
        get() = selectedKeys.count { key ->
            when {
                DownloadItemKeys.isRiwayah(key) -> riwayahStatuses.firstOrNull { it.key == key }?.isComplete == false
                DownloadItemKeys.isTafsir(key) -> tafsirStatuses.firstOrNull { it.key == key }?.isComplete == false
                DownloadItemKeys.isReciter(key) -> reciterStatuses.firstOrNull { it.key == key }?.isComplete == false
                DownloadItemKeys.isAdhkar(key) -> !adhkarStatus.isComplete
                else -> false
            }
        }

    val overallBatchProgress: Float
        get() {
            if (activeKeys.isEmpty()) return 0f
            val progresses = activeKeys.mapNotNull { key ->
                riwayahStatuses.firstOrNull { it.key == key }?.progress
                    ?: tafsirStatuses.firstOrNull { it.key == key }?.progress
                    ?: reciterStatuses.firstOrNull { it.key == key }?.progress
                    ?: if (DownloadItemKeys.isAdhkar(key)) adhkarStatus.progress else null
            }
            if (progresses.isEmpty()) return 0f
            return (progresses.sum() / progresses.size.toFloat()).coerceIn(0f, 1f)
        }

    fun compactSummaryAr(useWestern: Boolean): String {
        val parts = mutableListOf<String>()
        val completeNames = completedRiwayahs.map { it.edition.titleAr.substringBefore(" ") }
        if (completeNames.isNotEmpty()) {
            parts.add("المصحف: ${completeNames.joinToString("، ")}")
        } else {
            val partial = riwayahStatuses.firstOrNull { it.isPartial }
            if (partial != null) {
                val pCount = partial.downloadedPages.toAppDigits(useWestern)
                parts.add("${partial.edition.titleAr.substringBefore(" ")} ($pCount/٦٠٤)".localizeDigits(useWestern))
            }
        }
        val tafsirDone = completedTafsirs.size
        if (tafsirDone > 0) parts.add("التفسير: ${tafsirDone.toAppDigits(useWestern)}")
        val audioDone = startedReciters.size
        if (audioDone > 0) parts.add("تلاوات: ${audioDone.toAppDigits(useWestern)}")
        if (adhkarStatus.isSynced) parts.add("حصن المسلم ✓")
        return if (parts.isEmpty()) {
            "لم يُنزّل أي محتوى بعد • اضغط لإدارة التنزيل الشامل"
        } else {
            parts.joinToString(" • ") + " (${formatStorageSizeAr(totalManagedStorageBytes, useWestern)})"
        }
    }
}

@Immutable
data class DownloadHubActions(
    val onRefresh: () -> Unit,
    val onToggleSelection: (String) -> Unit,
    val onSelectRecommended: () -> Unit,
    val onSelectAllIncomplete: () -> Unit,
    val onClearSelection: () -> Unit,
    val onStartConcurrentDownload: () -> Unit,
    val onCancelAllDownloads: () -> Unit,
    val onCancelSingleDownload: (String) -> Unit,
    val onDeleteItem: (String) -> Unit,
    val onClearStatusMessage: () -> Unit,
    val onPrepareEssentialOffline: () -> Unit = {},
    val onClearRasterCache: () -> Unit = {},
    val onDownloadSingleSurah: (Int) -> Unit = {},
    val onDeleteSingleSurah: (Int) -> Unit = {},
    val onDownloadJuzAmmaAudio: () -> Unit = {}
)

fun formatStorageSizeAr(bytes: Long, useWestern: Boolean): String {
    if (bytes <= 0L) return "٠ ك.ب".localizeDigits(useWestern)
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    val raw = when {
        gb >= 1.0 -> String.format(Locale.US, "%.2f ج.ب", gb)
        mb >= 1.0 -> String.format(Locale.US, "%.1f م.ب", mb)
        else -> String.format(Locale.US, "%.0f ك.ب", kb.coerceAtLeast(1.0))
    }
    return raw.localizeDigits(useWestern)
}
