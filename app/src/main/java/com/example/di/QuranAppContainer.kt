package com.example.di

import android.app.Application
import coil.imageLoader
import coil.request.ImageRequest
import com.example.data.adhkar.HisnMuslimRepository
import com.example.data.audio.AudioPlaylistRepository
import com.example.data.audio.QuranAudioPlayer
import com.example.data.audio.RiwayahRecitersRepository
import com.example.data.audio.SurahAudioStorage
import com.example.data.local.BookmarksRepository
import com.example.data.local.IndexSearchHistoryRepository
import com.example.data.local.MushafOfflineStorage
import com.example.data.local.MushafRasterCache
import com.example.data.local.ReaderPreferencesRepository
import com.example.data.local.ReadingStatsRepository
import com.example.data.prayer.AdhanAudioStorage
import com.example.data.repository.QuranRepository
import com.example.data.tafsir.TafsirRepository
import com.example.data.widget.QuranSummaryWidgetProvider
import com.example.ui.components.MushafCropCoordinateMap
import com.example.ui.util.AppShortcutsHelper

interface QuranSystemBridge {
    suspend fun warmMushafCropMap()
    fun syncDynamicShortcuts(page: Int, surahName: String?, useWesternDigits: Boolean)
    fun prefetchMushafPages(models: List<Any>)
    fun notifyWidgetsChanged()
}

class AndroidQuranSystemBridge(
    private val application: Application
) : QuranSystemBridge {
    override suspend fun warmMushafCropMap() {
        MushafCropCoordinateMap.warmCache(application)
    }

    override fun syncDynamicShortcuts(page: Int, surahName: String?, useWesternDigits: Boolean) {
        AppShortcutsHelper.syncDynamicShortcuts(application, page, surahName, useWesternDigits)
    }

    override fun prefetchMushafPages(models: List<Any>) {
        val loader = application.imageLoader
        models.forEach { model ->
            loader.enqueue(ImageRequest.Builder(application).data(model).build())
        }
    }

    override fun notifyWidgetsChanged() {
        QuranSummaryWidgetProvider.notifyWidgetDataChanged(application)
    }
}

data class QuranViewModelDependencies(
    val repository: QuranRepository,
    val prefsRepository: ReaderPreferencesRepository,
    val offlineStorage: MushafOfflineStorage,
    val rasterCache: MushafRasterCache,
    val systemBridge: QuranSystemBridge,
    val audioPlayer: QuranAudioPlayer,
    val riwayahRecitersRepo: RiwayahRecitersRepository,
    val surahAudioStorage: SurahAudioStorage,
    val hisnRepository: HisnMuslimRepository,
    val adhanAudioStorage: AdhanAudioStorage,
    val bookmarksRepository: BookmarksRepository,
    val searchHistoryRepository: IndexSearchHistoryRepository,
    val readingStatsRepository: ReadingStatsRepository,
    val tafsirRepository: TafsirRepository,
    val audioPlaylistRepository: AudioPlaylistRepository,
    val ownsAudioPlayer: Boolean = false
)

class QuranAppContainer(val application: Application) {
    val mushafRasterCache: MushafRasterCache by lazy { MushafRasterCache(application) }
    val quranRepository: QuranRepository by lazy { QuranRepository(application) }
    val readerPreferencesRepository: ReaderPreferencesRepository by lazy { ReaderPreferencesRepository(application) }
    val mushafOfflineStorage: MushafOfflineStorage by lazy { MushafOfflineStorage(application) }
    val surahAudioStorage: SurahAudioStorage by lazy { SurahAudioStorage(application) }
    val riwayahRecitersRepository: RiwayahRecitersRepository by lazy { RiwayahRecitersRepository(application) }
    val hisnMuslimRepository: HisnMuslimRepository by lazy { HisnMuslimRepository(application) }
    val adhanAudioStorage: AdhanAudioStorage by lazy { AdhanAudioStorage(application) }
    val bookmarksRepository: BookmarksRepository by lazy { BookmarksRepository(application) }
    val indexSearchHistoryRepository: IndexSearchHistoryRepository by lazy { IndexSearchHistoryRepository(application) }
    val readingStatsRepository: ReadingStatsRepository by lazy { ReadingStatsRepository(application) }
    val tafsirRepository: TafsirRepository by lazy { TafsirRepository(application) }
    val audioPlaylistRepository: AudioPlaylistRepository by lazy { AudioPlaylistRepository(application) }
    val audioPlayer: QuranAudioPlayer by lazy {
        QuranAudioPlayer(application, surahAudioStorage) { surahNum ->
            quranRepository.getSurahMetaOrDefault(surahNum)
        }
    }
    val quranSystemBridge: QuranSystemBridge by lazy { AndroidQuranSystemBridge(application) }

    fun quranViewModelDependencies(ownsAudioPlayer: Boolean = false): QuranViewModelDependencies =
        QuranViewModelDependencies(
            repository = quranRepository,
            prefsRepository = readerPreferencesRepository,
            offlineStorage = mushafOfflineStorage,
            rasterCache = mushafRasterCache,
            systemBridge = quranSystemBridge,
            audioPlayer = audioPlayer,
            riwayahRecitersRepo = riwayahRecitersRepository,
            surahAudioStorage = surahAudioStorage,
            hisnRepository = hisnMuslimRepository,
            adhanAudioStorage = adhanAudioStorage,
            bookmarksRepository = bookmarksRepository,
            searchHistoryRepository = indexSearchHistoryRepository,
            readingStatsRepository = readingStatsRepository,
            tafsirRepository = tafsirRepository,
            audioPlaylistRepository = audioPlaylistRepository,
            ownsAudioPlayer = ownsAudioPlayer
        )
}
