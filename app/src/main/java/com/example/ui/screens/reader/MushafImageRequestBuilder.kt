package com.example.ui.screens.reader

import android.content.Context
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.example.data.local.MushafRasterCache
import com.example.data.local.MushafRasterTag
import com.example.ui.components.MushafCenterCropTransformation
import com.example.ui.components.MushafCropCoordinateMap
import com.example.ui.components.MushafSvgColorTag

internal fun buildMushafImageRequest(
    context: Context,
    model: Any,
    riwayahSlug: String,
    pageNumber: Int,
    targetWidthPx: Int,
    targetHeightPx: Int,
    rasterCache: MushafRasterCache? = null,
    bypassRasterCache: Boolean = false,
    markerColorKey: String = "def",
    ornamentHex: String? = null,
    numberHex: String? = null,
    surahBannerHex: String? = null
): ImageRequest {
    MushafCropCoordinateMap.init(context)
    val w = targetWidthPx.coerceAtLeast(360)
    val h = targetHeightPx.coerceAtLeast(520)
    val pageKey = "${riwayahSlug}_$pageNumber"
    val cachedRaster = if (bypassRasterCache) {
        null
    } else {
        rasterCache?.getCachedFileIfReady(riwayahSlug, pageNumber, w, h, colorKey = markerColorKey)
    }
    val rasterFile = if (cachedRaster != null && MushafCropCoordinateMap.getMetrics(pageKey) != null) {
        cachedRaster
    } else {
        null
    }
    val effectiveModel = rasterFile ?: model
    val isRasterizedPng = rasterFile != null
    val memoryKey = "mushaf_page_${pageKey}_${markerColorKey}_${w}x${h}"
    val rawSvgDiskKey = "mushaf_svg_raw_$pageKey"

    return ImageRequest.Builder(context)
        .data(effectiveModel)
        .size(w, h)
        .precision(Precision.INEXACT)
        .memoryCacheKey(memoryKey)
        .diskCacheKey(rawSvgDiskKey)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(if (isRasterizedPng) CachePolicy.DISABLED else CachePolicy.ENABLED)
        .allowHardware(isRasterizedPng)
        .crossfade(false)
        .apply {
            if (!isRasterizedPng) {
                val cropKey = if (surahBannerHex != null) "${pageKey}_b" else pageKey
                transformations(MushafCenterCropTransformation(pageKey = cropKey))
                if (ornamentHex != null || numberHex != null || surahBannerHex != null) {
                    tag(
                        MushafSvgColorTag::class.java,
                        MushafSvgColorTag(
                            ornamentHex = ornamentHex,
                            numberHex = numberHex,
                            surahBannerHex = surahBannerHex
                        )
                    )
                }
                if (rasterCache != null) {
                    tag(
                        MushafRasterTag::class.java,
                        MushafRasterTag(
                            riwayahSlug = riwayahSlug,
                            pageNumber = pageNumber,
                            widthPx = w,
                            heightPx = h,
                            colorKey = markerColorKey
                        )
                    )
                }
            }
        }
        .build()
}
