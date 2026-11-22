package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.data.audio.QuranAudioPlayer
import com.example.data.local.MushafRasterCache
import com.example.data.local.MushafRasterCacheInterceptor
import com.example.data.remote.QuranWsCdnClient
import com.example.data.repository.QuranRepository
import com.example.di.QuranAppContainer
import com.example.ui.components.MushafColoredSvgDecoder
import kotlinx.coroutines.Dispatchers

class QuranApplication : Application(), ImageLoaderFactory {
    val container: QuranAppContainer by lazy { QuranAppContainer(this) }

    val mushafRasterCache: MushafRasterCache get() = container.mushafRasterCache
    val quranRepository: QuranRepository get() = container.quranRepository
    val audioPlayer: QuranAudioPlayer get() = container.audioPlayer

    override fun newImageLoader(): ImageLoader {
        val decodeDispatcher = Dispatchers.IO.limitedParallelism(2)
        val transformDispatcher = Dispatchers.Default.limitedParallelism(2)
        return ImageLoader.Builder(this)
            .okHttpClient(QuranWsCdnClient.okHttpClient)
            .decoderDispatcher(decodeDispatcher)
            .transformationDispatcher(transformDispatcher)
            .components {
                add(MushafRasterCacheInterceptor(mushafRasterCache))
                add(MushafColoredSvgDecoder.Factory())
                add(SvgDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.32)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_mushaf_cache"))
                    .maxSizeBytes(250L * 1024L * 1024L)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(false)
            .build()
    }
}
