package com.example.data.local

import android.graphics.drawable.BitmapDrawable
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult

internal data class MushafRasterTag(
    val riwayahSlug: String,
    val pageNumber: Int,
    val widthPx: Int,
    val heightPx: Int,
    val colorKey: String = "def"
)

/**
 * Intercepts Coil image loads tagged with [MushafRasterTag] and asynchronously persists
 * the already-cropped Mushaf Bitmap to [MushafRasterCache] so subsequent loads bypass
 * SVG XML parsing and Canvas rasterization.
 */
class MushafRasterCacheInterceptor(
    private val rasterCache: MushafRasterCache
) : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val result = chain.proceed(chain.request)
        val tag = chain.request.tags.tag(MushafRasterTag::class.java)
        if (tag != null &&
            result is SuccessResult &&
            result.dataSource != DataSource.MEMORY_CACHE &&
            result.dataSource != DataSource.MEMORY
        ) {
            val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
            if (bitmap != null) {
                rasterCache.saveBitmapAsync(
                    riwayahSlug = tag.riwayahSlug,
                    pageNumber = tag.pageNumber,
                    widthPx = tag.widthPx,
                    heightPx = tag.heightPx,
                    bitmap = bitmap,
                    colorKey = tag.colorKey
                )
            }
        }
        return result
    }
}
