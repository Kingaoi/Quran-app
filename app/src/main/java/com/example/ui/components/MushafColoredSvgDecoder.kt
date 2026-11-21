package com.example.ui.components

import coil.ImageLoader
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.ImageSource
import coil.decode.SvgDecoder
import coil.fetch.SourceResult
import coil.request.Options
import okio.Buffer

data class MushafSvgColorTag(
    val ornamentHex: String?,
    val numberHex: String?,
    val surahBannerHex: String? = null
)

/**
 * Custom Coil Decoder that recolors ayah-marker ornaments/numbers and injects decorative Surah
 * header frames inside quran.ws SVG pages before delegating vector rasterization to Coil's [SvgDecoder].
 */
class MushafColoredSvgDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val colorTag: MushafSvgColorTag
) : Decoder {

    override suspend fun decode(): DecodeResult {
        val rawSvg = source.source().use { it.readUtf8() }
        val recoloredSvg = MushafSvgMarkerRecolorer.recolorAyahMarkers(
            svg = rawSvg,
            ornamentHex = colorTag.ornamentHex,
            numberHex = colorTag.numberHex
        )
        val bannerSvg = if (colorTag.surahBannerHex != null) {
            val template = MushafSvgSurahBannerInjector.loadBannerTemplate(options.context)
            MushafSvgSurahBannerInjector.injectSurahBanners(
                svg = recoloredSvg,
                bannerTemplate = template,
                bannerHex = colorTag.surahBannerHex
            )
        } else {
            recoloredSvg
        }
        val buffer = Buffer().writeUtf8(bannerSvg)
        val modifiedSource = ImageSource(buffer, options.context)
        return SvgDecoder(modifiedSource, options).decode()
    }

    class Factory : Decoder.Factory {
        override fun create(
            result: SourceResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? {
            val tag = options.tags.tag(MushafSvgColorTag::class.java) ?: return null
            if (tag.ornamentHex == null && tag.numberHex == null && tag.surahBannerHex == null) {
                return null
            }
            return MushafColoredSvgDecoder(result.source, options, tag)
        }
    }
}
