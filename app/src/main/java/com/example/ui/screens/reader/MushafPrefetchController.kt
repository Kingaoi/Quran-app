package com.example.ui.screens.reader

import android.content.Context
import coil.ImageLoader
import com.example.data.local.MushafOfflineStorage
import com.example.data.local.MushafRasterCache
import com.example.data.local.RiwayahEdition
import com.example.ui.components.MushafCropCoordinateMap
import com.example.ui.components.MushafSvgAyahMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

internal suspend fun runMushafPredictivePrefetch(
    activePageNum: Int,
    dir: Int,
    riwayah: RiwayahEdition,
    svgVersion: String,
    isTwoPageSpread: Boolean,
    markerColorKey: String,
    ornamentHex: String?,
    numberHex: String?,
    surahBannerHex: String?,
    targetWidthPx: Int,
    targetHeightPx: Int,
    context: Context,
    imageLoader: ImageLoader,
    rasterCache: MushafRasterCache,
    offlineStorage: MushafOfflineStorage,
    getSvgModel: (Int) -> Any,
    getRawGithubSvgUrl: (Int) -> String,
    scopedStartPage: Int,
    scopedEndPage: Int
) = coroutineScope {
    val effectiveWidth = if (isTwoPageSpread) (targetWidthPx / 2).coerceAtLeast(360) else targetWidthPx
    val stage1Offsets = if (isTwoPageSpread) listOf(dir, 2 * dir, -dir) else listOf(dir, -dir)
    val stage1Pages = stage1Offsets.map { activePageNum + it }.filter { it in scopedStartPage..scopedEndPage }.distinct()

    val decodeSemaphore = Semaphore(2)
    suspend fun warmPage(pageNum: Int) {
        decodeSemaphore.withPermit {
            ensureActive()
            val pageKey = "${riwayah.slug}_$pageNum"
            MushafCropCoordinateMap.ensureMetricsLoaded(pageKey)
            val model = getSvgModel(pageNum)
            val req = buildMushafImageRequest(
                context = context,
                model = model,
                riwayahSlug = riwayah.slug,
                pageNumber = pageNum,
                targetWidthPx = effectiveWidth,
                targetHeightPx = targetHeightPx,
                rasterCache = rasterCache,
                markerColorKey = markerColorKey,
                ornamentHex = ornamentHex,
                numberHex = numberHex,
                surahBannerHex = surahBannerHex
            )
            imageLoader.execute(req)
            val urls = buildList {
                (model as? String)?.let { add(it) }
                add(getRawGithubSvgUrl(pageNum))
            }
            MushafSvgAyahMap.loadPageMap(context, offlineStorage, riwayah, pageNum, urls)
        }
    }

    // Stage 1: Immediately pre-warm neighboring page(s) in raster + ayah-map caches
    stage1Pages.map { p -> async(Dispatchers.IO) { runCatching { warmPage(p) } } }.awaitAll()

    // Stage 2: After reader settles briefly, pre-cache extended lookahead SVGs on disk + raster
    delay(90L)
    ensureActive()

    val stage2Offsets = if (isTwoPageSpread) {
        listOf(3 * dir, 4 * dir, 5 * dir, -2 * dir)
    } else {
        listOf(2 * dir, 3 * dir, -2 * dir, 4 * dir)
    }
    val stage2Pages = stage2Offsets.map { activePageNum + it }.filter { it in scopedStartPage..scopedEndPage }.distinct()
    if (stage2Pages.isEmpty()) return@coroutineScope

    launch(Dispatchers.IO) {
        runCatching { offlineStorage.prefetchPagesToDisk(riwayah, svgVersion, stage1Pages + stage2Pages) }
        for (pageNum in stage2Pages) {
            ensureActive()
            runCatching { warmPage(pageNum) }
        }
    }
}
