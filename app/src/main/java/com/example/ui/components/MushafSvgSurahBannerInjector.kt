package com.example.ui.components

import android.content.Context
import kotlin.math.max
import kotlin.math.min

/**
 * Injects an authentic decorative Madinah Mushaf cartouche behind Surah titles on quran.ws SVG
 * pages (003..604 across all 5 Riwayat), leaving opening illuminated pages (001..002) untouched.
 */
object MushafSvgSurahBannerInjector {
    private const val ASSET_NAME = "surah_header_madinah.txt"
    private const val VIEWBOX_WIDTH = 811.409f
    private const val VIEWBOX_HEIGHT = 100f
    private const val BANNER_HEIGHT = 35f

    private val VIEWBOX_REGEX = Regex("""\bviewBox\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val AYAH_POLYGON_REGEX = Regex("""<path\b[^>]*\bclass\s*=\s*["'][^"']*\bayahPolygon\b[^"']*["'][^>]*>""", RegexOption.IGNORE_CASE)
    private val D_ATTR_REGEX = Regex("""\bd\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val AYAH_ATTR_REGEX = Regex("""\bayah\s*=\s*["'](\d+)["']""", RegexOption.IGNORE_CASE)
    private val SURAH_ATTR_REGEX = Regex("""\bsurah\s*=\s*["'](\d+)["']""", RegexOption.IGNORE_CASE)
    private val NUMBER_REGEX = Regex("""-?\d+(?:\.\d+)?""")

    @Volatile
    private var cachedTemplate: String? = null

    fun loadBannerTemplate(context: Context): String {
        cachedTemplate?.let { return it }
        val loaded = runCatching {
            context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
        }.getOrDefault("")
        if (loaded.isNotEmpty()) {
            cachedTemplate = loaded
        }
        return loaded
    }

    fun injectSurahBanners(
        svg: String,
        bannerTemplate: String,
        bannerHex: String?
    ): String {
        if (bannerHex == null || bannerTemplate.isBlank()) return svg
        if (!svg.contains("ayahPolygon")) return svg

        val viewBox = parseViewBox(svg) ?: return svg
        // Pages 001 & 002 carry native full-page illuminated borders with viewBox minY < -50
        if (viewBox.minY < -20f) return svg

        val placements = computeBannerPlacements(svg, viewBox)
        if (placements.isEmpty()) return svg

        var result = ensureXlinkNamespace(svg)
        val coloredDefs = bannerTemplate
            .replace("{{SLOT_FILL}}", bannerHex)
            .replace("{{INK_FILL}}", bannerHex)

        val bannerMarkup = buildString(coloredDefs.length + placements.size * 512) {
            append("<defs>").append(coloredDefs).append("</defs>")
            append("<g id=\"surah_header_banners\">")
            placements.forEach { box ->
                val sx = box.width / VIEWBOX_WIDTH
                val sy = BANNER_HEIGHT / VIEWBOX_HEIGHT
                val topY = box.centerY - BANNER_HEIGHT / 2f
                append("<g transform=\"translate(${fmt(box.leftX)},${fmt(topY)}) scale(${fmt(sx)},${fmt(sy)})\">")
                append("<use xlink:href=\"#q_sh_m\"/>")
                append("<use xlink:href=\"#q_sh_m\" transform=\"matrix(-1 0 0 1 811.41 0)\"/>")
                append("<use xlink:href=\"#q_sh_m\" transform=\"matrix(1 0 0 -1 0 100)\"/>")
                append("<use xlink:href=\"#q_sh_m\" transform=\"rotate(180 405.705 50)\"/>")
                append("</g>")
            }
            append("</g>")
        }

        val insertIdx = findBannerInsertIndex(result) ?: return svg
        return buildString(result.length + bannerMarkup.length) {
            append(result, 0, insertIdx)
            append(bannerMarkup)
            append(result, insertIdx, result.length)
        }
    }

    internal fun computeBannerPlacements(
        svg: String,
        viewBox: SvgViewBox? = parseViewBox(svg)
    ): List<BannerPlacement> {
        val vb = viewBox ?: SvgViewBox(0f, 0f, 345f, 550f)
        val polygons = AYAH_POLYGON_REGEX.findAll(svg).mapNotNull { match ->
            val tag = match.value
            val d = D_ATTR_REGEX.find(tag)?.groupValues?.getOrNull(1) ?: return@mapNotNull null
            val ayah = AYAH_ATTR_REGEX.find(tag)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
            val surah = SURAH_ATTR_REGEX.find(tag)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
            val subpaths = parseSubpathBounds(d)
            if (subpaths.isEmpty()) return@mapNotNull null
            ParsedPolygon(surah = surah, ayah = ayah, subpaths = subpaths)
        }.toList()

        if (polygons.isEmpty()) return emptyList()

        var pageMinX = Float.POSITIVE_INFINITY
        var pageMaxX = Float.NEGATIVE_INFINITY
        polygons.forEach { poly ->
            poly.subpaths.forEach { sub ->
                pageMinX = min(pageMinX, sub.minX)
                pageMaxX = max(pageMaxX, sub.maxX)
            }
        }

        val hasFullSpan = pageMinX.isFinite() && (pageMaxX - pageMinX) >= 240f
        val leftX = if (hasFullSpan) pageMinX else vb.minX + 2f
        val width = if (hasFullSpan) {
            (pageMaxX - pageMinX).coerceIn(280f, 344f)
        } else {
            (vb.width - 4f).coerceIn(280f, 344f)
        }
        val effectiveMaxX = if (pageMaxX.isFinite()) pageMaxX else leftX + width

        val hasSurahStart = polygons.any { it.ayah == 1 && it.surah > 2 }
        if (!hasSurahStart) return emptyList()

        val contentBoxes = MushafSvgContentTextScanner.extractContentBoxes(svg)
        val placements = mutableListOf<BannerPlacement>()
        var lastSurah: Int? = null
        var prevOtherMaxY: Float? = null

        polygons.forEachIndexed { idx, poly ->
            if (lastSurah != null && poly.surah != lastSurah) {
                prevOtherMaxY = polygons[idx - 1].subpaths.maxOf { it.maxY }
            }
            lastSurah = poly.surah

            if (poly.ayah == 1 && poly.surah > 2) {
                val filtered = poly.subpaths.filter { sub ->
                    val abovePrevSurah = prevOtherMaxY?.let { sub.minY < it - 4f } ?: false
                    val leftSliverOnly = poly.subpaths.size > 1 && sub.maxX < effectiveMaxX - 40f
                    !abovePrevSurah && !leftSliverOnly
                }.ifEmpty { poly.subpaths }

                val a1MinY = filtered.minOf { it.minY }
                val prevMax = prevOtherMaxY
                val detectedCenterY = MushafSvgContentTextScanner.findSurahTitleCenterY(
                    contentBoxes = contentBoxes,
                    surah = poly.surah,
                    prevMaxY = prevMax,
                    a1MinY = a1MinY,
                    viewBoxMinX = vb.minX,
                    viewBoxMinY = vb.minY,
                    viewBoxWidth = vb.width
                )
                val centerY = detectedCenterY ?: if (poly.surah == 9) {
                    if (prevMax != null) (prevMax + a1MinY) * 0.5f else max(24f, a1MinY - 14.5f)
                } else {
                    val offsetFromAyah1 = if (vb.minX < 0f) 53.5f else 49.5f
                    if (prevMax != null) {
                        max(prevMax + 18f, a1MinY - offsetFromAyah1)
                    } else {
                        max(24f, a1MinY - offsetFromAyah1)
                    }
                }
                placements.add(BannerPlacement(surah = poly.surah, leftX = leftX, width = width, centerY = centerY))
            }
        }
        return placements
    }

    private fun parseSubpathBounds(d: String): List<SubpathBox> {
        val result = mutableListOf<SubpathBox>()
        var idx = 0
        val len = d.length
        while (idx < len) {
            val ch = d[idx]
            if (ch == 'M' || ch == 'm') {
                val nextM = d.indexOfAny(charArrayOf('M', 'm'), idx + 1).let { if (it < 0) len else it }
                val segment = d.substring(idx + 1, nextM)
                val nums = NUMBER_REGEX.findAll(segment).mapNotNull { it.value.toFloatOrNull() }.toList()
                var minX = Float.POSITIVE_INFINITY
                var maxX = Float.NEGATIVE_INFINITY
                var minY = Float.POSITIVE_INFINITY
                var maxY = Float.NEGATIVE_INFINITY
                var i = 0
                while (i + 1 < nums.size) {
                    val x = nums[i]
                    val y = nums[i + 1]
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                    i += 2
                }
                if (minX.isFinite() && minY.isFinite()) {
                    result.add(SubpathBox(minX, maxX, minY, maxY))
                }
                idx = nextM
            } else {
                idx++
            }
        }
        return result
    }

    private fun parseViewBox(svg: String): SvgViewBox? {
        val raw = VIEWBOX_REGEX.find(svg)?.groupValues?.getOrNull(1) ?: return null
        val nums = NUMBER_REGEX.findAll(raw).mapNotNull { it.value.toFloatOrNull() }.toList()
        if (nums.size < 4) return null
        return SvgViewBox(nums[0], nums[1], nums[2], nums[3])
    }

    private fun ensureXlinkNamespace(svg: String): String {
        if (svg.contains("xmlns:xlink")) return svg
        val svgOpen = svg.indexOf("<svg", ignoreCase = true)
        if (svgOpen < 0) return svg
        val svgClose = svg.indexOf('>', svgOpen)
        if (svgClose < 0) return svg
        return buildString(svg.length + 46) {
            append(svg, 0, svgClose)
            append(""" xmlns:xlink="http://www.w3.org/1999/xlink"""")
            append(svg, svgClose, svg.length)
        }
    }

    private fun findBannerInsertIndex(svg: String): Int? {
        val svgOpen = svg.indexOf("<svg", ignoreCase = true)
        if (svgOpen < 0) return null
        val svgClose = svg.indexOf('>', svgOpen)
        return if (svgClose >= 0) svgClose + 1 else null
    }

    private fun fmt(value: Float): String = String.format(java.util.Locale.US, "%.3f", value)

    internal data class SvgViewBox(val minX: Float, val minY: Float, val width: Float, val height: Float)
    private data class SubpathBox(val minX: Float, val maxX: Float, val minY: Float, val maxY: Float)
    private data class ParsedPolygon(val surah: Int, val ayah: Int, val subpaths: List<SubpathBox>)
    internal data class BannerPlacement(val surah: Int, val leftX: Float, val width: Float, val centerY: Float)
}
