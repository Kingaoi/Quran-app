package com.example.ui.components

import android.content.Context
import android.graphics.RectF
import android.util.LruCache
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.example.data.local.MushafOfflineStorage
import com.example.data.local.RiwayahEdition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

data class SvgAyahBox(
    val surahNumber: Int,
    val ayahNumber: Int,
    val rects: List<RectF>,
    val subpathPolygons: List<FloatArray> = emptyList()
)

data class SvgPageAyahMap(
    val pageKey: String,
    val viewBox: SvgViewBoxRect,
    val ayahs: List<SvgAyahBox>
) {
    fun findAyahAt(svgX: Float, svgY: Float): SvgAyahBox? {
        for (entry in ayahs) {
            if (entry.subpathPolygons.isNotEmpty()) {
                for (idx in entry.subpathPolygons.indices) {
                    val r = entry.rects.getOrNull(idx)
                    if (r != null && (svgX < r.left - 2f || svgX > r.right + 2f || svgY < r.top - 2f || svgY > r.bottom + 2f)) {
                        continue
                    }
                    if (MushafSvgPathParser.pointInPolygon(svgX, svgY, entry.subpathPolygons[idx])) {
                        return entry
                    }
                }
            } else {
                for (r in entry.rects) {
                    if (svgX >= r.left - 3f && svgX <= r.right + 3f && svgY >= r.top - 3f && svgY <= r.bottom + 3f) {
                        return entry
                    }
                }
            }
        }
        for (entry in ayahs) {
            if (entry.subpathPolygons.size <= 1) {
                val poly = entry.subpathPolygons.firstOrNull()
                if (poly != null && poly.size >= 6) continue
            }
            for (r in entry.rects) {
                if (svgX >= r.left - 3f && svgX <= r.right + 3f && svgY >= r.top - 3f && svgY <= r.bottom + 3f) {
                    return entry
                }
            }
        }
        return ayahs.minByOrNull { entry ->
            entry.rects.minOfOrNull { r ->
                val dx = when {
                    svgX < r.left -> r.left - svgX
                    svgX > r.right -> svgX - r.right
                    else -> 0f
                }
                val dy = when {
                    svgY < r.top -> r.top - svgY
                    svgY > r.bottom -> svgY - r.bottom
                    else -> 0f
                }
                dx * dx + dy * dy
            } ?: Float.MAX_VALUE
        }?.takeIf { candidate ->
            candidate.rects.any { r ->
                val dx = maxOf(0f, r.left - svgX, svgX - r.right)
                val dy = maxOf(0f, r.top - svgY, svgY - r.bottom)
                dx <= 16f && dy <= 16f
            }
        }
    }

    fun rectsForAyah(surah: Int, ayah: Int): List<RectF> =
        ayahs.filter { it.surahNumber == surah && it.ayahNumber == ayah }.flatMap { it.rects }

    fun polygonsForAyah(surah: Int, ayah: Int): List<FloatArray> =
        ayahs.filter { it.surahNumber == surah && it.ayahNumber == ayah }.flatMap { it.subpathPolygons }
}

object MushafSvgAyahMap {
    private val memoryCache = LruCache<String, SvgPageAyahMap>(48)

    private val VIEWBOX_REGEX = Regex("""\bviewBox\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val AYAH_ATTR_REGEX = Regex("""\bayah\s*=\s*["'](\d+)["']""", RegexOption.IGNORE_CASE)
    private val SURAH_ATTR_REGEX = Regex("""\bsurah\s*=\s*["'](\d+)["']""", RegexOption.IGNORE_CASE)
    private val NUMBER_ATTR_REGEX = Regex("""\bnumber\s*=\s*["'](\d{6})["']""", RegexOption.IGNORE_CASE)
    private val CLASS_AYAH_POLYGON_REGEX = Regex("""\bclass\s*=\s*["'][^"']*\bayahPolygon\b[^"']*["']""", RegexOption.IGNORE_CASE)
    private val NUMBER_REGEX = Regex("""-?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?""")

    private val fallbackClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .build()
    }

    @OptIn(ExperimentalCoilApi::class)
    suspend fun loadPageMap(
        context: Context,
        offlineStorage: MushafOfflineStorage,
        edition: RiwayahEdition,
        pageNumber: Int,
        fallbackUrls: List<String> = emptyList()
    ): SvgPageAyahMap? = withContext(Dispatchers.IO) {
        val pageKey = "${edition.slug}_$pageNumber"
        memoryCache.get(pageKey)?.let { return@withContext it }

        val mapCacheFile = File(File(context.cacheDir, "mushaf_svg_maps").apply { mkdirs() }, "$pageKey.svg")
        val svgText = offlineStorage.getCachedPageFile(edition, pageNumber)?.let { file ->
            runCatching { file.readText() }.getOrNull()
        }
            ?: readFromCoilDiskCache(context, "mushaf_svg_raw_$pageKey")
            ?: mapCacheFile.takeIf { it.exists() && it.length() > 512L }?.let { runCatching { it.readText() }.getOrNull() }
            ?: fetchAndCacheRawSvg(mapCacheFile, fallbackUrls)

        if (svgText.isNullOrBlank() || !svgText.contains("ayahPolygon")) {
            return@withContext null
        }

        parseSvg(pageKey, svgText)?.also { parsed ->
            memoryCache.put(pageKey, parsed)
        }
    }

    private fun fetchAndCacheRawSvg(cacheFile: File, urls: List<String>): String? {
        for (url in urls) {
            if (!url.startsWith("http")) continue
            val body = runCatching {
                fallbackClient.newCall(Request.Builder().url(url).build()).execute().use { resp ->
                    if (!resp.isSuccessful) null else resp.body?.string()
                }
            }.getOrNull()
            if (!body.isNullOrBlank() && body.contains("ayahPolygon")) {
                runCatching { cacheFile.writeText(body) }
                return body
            }
        }
        return null
    }

    @OptIn(ExperimentalCoilApi::class)
    private fun readFromCoilDiskCache(context: Context, diskKey: String): String? {
        return runCatching {
            val diskCache = context.imageLoader.diskCache ?: return null
            diskCache.openSnapshot(diskKey)?.use { snapshot ->
                snapshot.data.toFile().readText()
            }
        }.getOrNull()
    }

    internal fun parseSvg(pageKey: String, svg: String): SvgPageAyahMap? {
        val svgOpen = svg.indexOf("<svg", ignoreCase = true)
        val svgOpenEnd = if (svgOpen >= 0) svg.indexOf('>', svgOpen) else -1
        val svgHeader = if (svgOpen >= 0 && svgOpenEnd > svgOpen) svg.substring(svgOpen, svgOpenEnd + 1) else svg
        val rawVb = VIEWBOX_REGEX.find(svgHeader)?.groupValues?.getOrNull(1)
            ?: VIEWBOX_REGEX.find(svg)?.groupValues?.getOrNull(1)
        val vbNums = rawVb?.let { raw ->
            NUMBER_REGEX.findAll(raw).mapNotNull { it.value.toFloatOrNull() }.toList()
        }.orEmpty()
        val viewBox = if (vbNums.size >= 4) {
            SvgViewBoxRect(vbNums[0], vbNums[1], vbNums[2], vbNums[3])
        } else {
            SvgViewBoxRect(0f, 0f, 345f, 550f)
        }

        val entries = ArrayList<SvgAyahBox>(32)
        val stack = ArrayList<SvgTransformMat>(8).apply { add(SvgTransformMat.IDENTITY) }
        var pos = if (svgOpenEnd >= 0) svgOpenEnd + 1 else 0
        val len = svg.length

        while (pos < len) {
            val lt = svg.indexOf('<', pos)
            if (lt < 0) break
            val gt = svg.indexOf('>', lt + 1)
            if (gt < 0) break

            if (isClosingGroup(svg, lt)) {
                if (stack.size > 1) stack.removeAt(stack.lastIndex)
            } else if (isOpeningGroup(svg, lt) && svg[gt - 1] != '/') {
                val tr = extractAttr(svg, lt, gt, "transform")
                stack.add(if (tr != null) stack.last().mul(SvgTransformMat.parse(tr)) else stack.last())
            } else if (isAyahShapeTag(svg, lt)) {
                val tag = svg.substring(lt, gt + 1)
                if (CLASS_AYAH_POLYGON_REGEX.containsMatchIn(tag)) {
                    parseAyahTag(svg, lt, gt, tag, stack.last())?.let { entries.add(it) }
                }
            }
            pos = gt + 1
        }

        if (entries.isEmpty()) return null
        return SvgPageAyahMap(pageKey = pageKey, viewBox = viewBox, ayahs = entries)
    }

    private fun parseAyahTag(
        svg: String,
        lt: Int,
        gt: Int,
        tag: String,
        parentMat: SvgTransformMat
    ): SvgAyahBox? {
        val numberStr = NUMBER_ATTR_REGEX.find(tag)?.groupValues?.getOrNull(1)
        val surah = SURAH_ATTR_REGEX.find(tag)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: numberStr?.take(3)?.toIntOrNull()
            ?: return null
        val ayah = AYAH_ATTR_REGEX.find(tag)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: numberStr?.drop(3)?.toIntOrNull()
            ?: return null

        val selfTr = extractAttr(svg, lt, gt, "transform")
        val mat = if (selfTr != null) parentMat.mul(SvgTransformMat.parse(selfTr)) else parentMat

        val subpaths: List<ParsedSubpathGeometry> = when {
            svg.regionMatches(lt + 1, "path", 0, 4, ignoreCase = true) -> {
                val d = extractAttr(svg, lt, gt, "d") ?: return null
                MushafSvgPathParser.parsePathSubpaths(d, mat)
            }
            svg.regionMatches(lt + 1, "polygon", 0, 7, ignoreCase = true) -> {
                val ptsAttr = extractAttr(svg, lt, gt, "points") ?: return null
                parsePolygonPoints(ptsAttr, mat)?.let { listOf(it) }.orEmpty()
            }
            svg.regionMatches(lt + 1, "rect", 0, 4, ignoreCase = true) -> {
                val x = extractAttr(svg, lt, gt, "x")?.toFloatOrNull() ?: 0f
                val y = extractAttr(svg, lt, gt, "y")?.toFloatOrNull() ?: 0f
                val w = extractAttr(svg, lt, gt, "width")?.toFloatOrNull() ?: return null
                val h = extractAttr(svg, lt, gt, "height")?.toFloatOrNull() ?: return null
                val dRect = "M $x $y L ${x + w} $y L ${x + w} ${y + h} L $x ${y + h} Z"
                MushafSvgPathParser.parsePathSubpaths(dRect, mat)
            }
            else -> emptyList()
        }
        if (subpaths.isEmpty()) return null
        return SvgAyahBox(
            surahNumber = surah,
            ayahNumber = ayah,
            rects = subpaths.map { it.rect },
            subpathPolygons = subpaths.map { it.polygon }
        )
    }

    private fun parsePolygonPoints(points: String, mat: SvgTransformMat): ParsedSubpathGeometry? {
        val nums = NUMBER_REGEX.findAll(points).mapNotNull { it.value.toFloatOrNull() }.toList()
        if (nums.size < 6) return null
        val poly = FloatArray((nums.size / 2) * 2)
        var minX = Float.POSITIVE_INFINITY; var maxX = Float.NEGATIVE_INFINITY
        var minY = Float.POSITIVE_INFINITY; var maxY = Float.NEGATIVE_INFINITY
        var i = 0
        while (i + 1 < nums.size) {
            val gx = mat.mapX(nums[i], nums[i + 1])
            val gy = mat.mapY(nums[i], nums[i + 1])
            poly[i] = gx
            poly[i + 1] = gy
            if (gx < minX) minX = gx; if (gx > maxX) maxX = gx
            if (gy < minY) minY = gy; if (gy > maxY) maxY = gy
            i += 2
        }
        if (!minX.isFinite() || maxX <= minX || maxY <= minY) return null
        return ParsedSubpathGeometry(RectF(minX, minY, maxX, maxY), poly)
    }

    private fun isOpeningGroup(svg: String, lt: Int) =
        lt + 2 < svg.length && (svg[lt + 1] == 'g' || svg[lt + 1] == 'G') &&
            svg[lt + 2].let { it == '>' || it == '/' || it.isWhitespace() }

    private fun isClosingGroup(svg: String, lt: Int) =
        lt + 3 < svg.length && svg[lt + 1] == '/' && (svg[lt + 2] == 'g' || svg[lt + 2] == 'G') &&
            svg[lt + 3].let { it == '>' || it.isWhitespace() }

    private fun isAyahShapeTag(svg: String, lt: Int): Boolean {
        val s = lt + 1
        return (s + 4 < svg.length && svg.regionMatches(s, "path", 0, 4, true) && svg[s + 4].let { it == '>' || it == '/' || it.isWhitespace() }) ||
            (s + 7 < svg.length && svg.regionMatches(s, "polygon", 0, 7, true) && svg[s + 7].let { it == '>' || it == '/' || it.isWhitespace() }) ||
            (s + 4 < svg.length && svg.regionMatches(s, "rect", 0, 4, true) && svg[s + 4].let { it == '>' || it == '/' || it.isWhitespace() })
    }

    private fun extractAttr(svg: String, start: Int, end: Int, name: String): String? {
        var i = start
        val nLen = name.length
        while (i + nLen + 2 < end) {
            if (svg.regionMatches(i, name, 0, nLen, ignoreCase = true) && (i == start || svg[i - 1].isWhitespace())) {
                var j = i + nLen
                while (j < end && svg[j].isWhitespace()) j++
                if (j < end && svg[j] == '=') {
                    j++
                    while (j < end && svg[j].isWhitespace()) j++
                    if (j < end && (svg[j] == '"' || svg[j] == '\'')) {
                        val vEnd = svg.indexOf(svg[j], j + 1)
                        if (vEnd in (j + 1)..end) return svg.substring(j + 1, vEnd)
                    }
                }
            }
            i++
        }
        return null
    }
}
