package com.example.ui.components

import kotlin.math.max
import kotlin.math.min

/**
 * Scans `<g id="content">` vector paths inside quran.ws SVG pages (with all ancestor SVG
 * `transform` matrices applied) to locate the exact vertical center of each Surah title line.
 */
internal object MushafSvgContentTextScanner {

    internal data class ContentBox(val minX: Float, val maxX: Float, val minY: Float, val maxY: Float) {
        val centerX: Float get() = (minX + maxX) * 0.5f
        val centerY: Float get() = (minY + maxY) * 0.5f
    }

    private data class Mat(val a: Float, val b: Float, val c: Float, val d: Float, val e: Float, val f: Float) {
        fun mul(o: Mat) = Mat(
            a * o.a + c * o.b, b * o.a + d * o.b,
            a * o.c + c * o.d, b * o.c + d * o.d,
            a * o.e + c * o.f + e, b * o.e + d * o.f + f
        )
        fun mapX(x: Float, y: Float) = a * x + c * y + e
        fun mapY(x: Float, y: Float) = b * x + d * y + f
        companion object { val ID = Mat(1f, 0f, 0f, 1f, 0f, 0f) }
    }

    private val TRANSFORM_FN_REGEX = Regex("""(\w+)\s*\(([^)]+)\)""")
    private val NUMBER_REGEX = Regex("""-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?""")

    fun findSurahTitleCenterY(
        contentBoxes: List<ContentBox>,
        surah: Int,
        prevMaxY: Float?,
        a1MinY: Float,
        viewBoxMinX: Float,
        viewBoxMinY: Float,
        viewBoxWidth: Float
    ): Float? {
        if (contentBoxes.isEmpty()) return null
        val midX = viewBoxMinX + viewBoxWidth * 0.5f
        val winLo = if (surah == 9) {
            prevMaxY?.plus(2f) ?: viewBoxMinY
        } else {
            prevMaxY?.plus(2f) ?: max(viewBoxMinY, a1MinY - 78f)
        }
        val winHi = if (surah == 9) a1MinY - 2f else a1MinY - 31f
        if (winHi <= winLo) return null

        val candidates = contentBoxes.filter {
            it.centerY in winLo..winHi && it.centerX in (midX - 80f)..(midX + 80f)
        }.sortedBy { it.centerY }
        if (candidates.size < 6) return null

        val clusters = mutableListOf<MutableList<ContentBox>>()
        for (box in candidates) {
            val last = clusters.lastOrNull()
            if (last == null || box.centerY - last.last().centerY > 10f) {
                clusters.add(mutableListOf(box))
            } else {
                last.add(box)
            }
        }
        val best = clusters.filter { it.size >= 6 }.maxByOrNull { it.size } ?: return null
        return (best.minOf { it.minY } + best.maxOf { it.maxY }) * 0.5f
    }

    fun extractContentBoxes(svg: String): List<ContentBox> {
        val contentIdx = svg.indexOf("<g id=\"content\"").takeIf { it >= 0 }
            ?: svg.indexOf("<g id='content'").takeIf { it >= 0 } ?: return emptyList()
        val svgOpen = svg.indexOf("<svg", ignoreCase = true)
        if (svgOpen < 0 || svgOpen >= contentIdx) return emptyList()
        val svgOpenEnd = svg.indexOf('>', svgOpen)
        if (svgOpenEnd < 0 || svgOpenEnd >= contentIdx) return emptyList()

        val stack = ArrayList<Mat>(8).apply { add(Mat.ID) }
        var pos = svgOpenEnd + 1
        while (pos < contentIdx) {
            val lt = svg.indexOf('<', pos)
            if (lt < 0 || lt >= contentIdx) break
            val gt = svg.indexOf('>', lt + 1)
            if (gt < 0 || gt > contentIdx) break
            if (isClosingGroup(svg, lt)) {
                if (stack.size > 1) stack.removeAt(stack.lastIndex)
            } else if (isOpeningGroup(svg, lt) && svg[gt - 1] != '/') {
                val tr = extractAttr(svg, lt, gt, "transform")
                stack.add(if (tr != null) stack.last().mul(parseTransform(tr)) else stack.last())
            }
            pos = gt + 1
        }

        val contentGt = svg.indexOf('>', contentIdx)
        if (contentGt < 0) return emptyList()
        val cTr = extractAttr(svg, contentIdx, contentGt, "transform")
        stack.add(if (cTr != null) stack.last().mul(parseTransform(cTr)) else stack.last())

        val out = ArrayList<ContentBox>(2048)
        var depth = 1
        pos = contentGt + 1
        while (pos < svg.length && depth > 0) {
            val lt = svg.indexOf('<', pos)
            if (lt < 0) break
            val gt = svg.indexOf('>', lt + 1)
            if (gt < 0) break
            if (isClosingGroup(svg, lt)) {
                depth--
                if (stack.size > 1) stack.removeAt(stack.lastIndex)
            } else if (isOpeningGroup(svg, lt) && svg[gt - 1] != '/') {
                depth++
                val tr = extractAttr(svg, lt, gt, "transform")
                stack.add(if (tr != null) stack.last().mul(parseTransform(tr)) else stack.last())
            } else if (isPathTag(svg, lt)) {
                val d = extractAttr(svg, lt, gt, "d")
                if (!d.isNullOrEmpty()) {
                    val tr = extractAttr(svg, lt, gt, "transform")
                    val mat = if (tr != null) stack.last().mul(parseTransform(tr)) else stack.last()
                    parsePathSubpathBoxes(d, mat, out)
                }
            }
            pos = gt + 1
        }
        return out
    }

    private fun isOpeningGroup(svg: String, lt: Int) =
        lt + 2 < svg.length && (svg[lt + 1] == 'g' || svg[lt + 1] == 'G') &&
            svg[lt + 2].let { it == '>' || it == '/' || it.isWhitespace() }

    private fun isClosingGroup(svg: String, lt: Int) =
        lt + 3 < svg.length && svg[lt + 1] == '/' && (svg[lt + 2] == 'g' || svg[lt + 2] == 'G') &&
            svg[lt + 3].let { it == '>' || it.isWhitespace() }

    private fun isPathTag(svg: String, lt: Int) =
        lt + 5 < svg.length && svg.regionMatches(lt + 1, "path", 0, 4, ignoreCase = true) &&
            svg[lt + 5].let { it == '>' || it == '/' || it.isWhitespace() }

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

    private fun parseTransform(tr: String): Mat {
        var mat = Mat.ID
        for (m in TRANSFORM_FN_REGEX.findAll(tr)) {
            val nums = NUMBER_REGEX.findAll(m.groupValues[2]).mapNotNull { it.value.toFloatOrNull() }.toList()
            val next = when (m.groupValues[1].lowercase()) {
                "translate" -> if (nums.isNotEmpty()) Mat(1f, 0f, 0f, 1f, nums[0], nums.getOrElse(1) { 0f }) else null
                "scale" -> if (nums.isNotEmpty()) Mat(nums[0], 0f, 0f, nums.getOrElse(1) { nums[0] }, 0f, 0f) else null
                "matrix" -> if (nums.size == 6) Mat(nums[0], nums[1], nums[2], nums[3], nums[4], nums[5]) else null
                else -> null
            }
            if (next != null) mat = mat.mul(next)
        }
        return mat
    }

    private fun parsePathSubpathBoxes(d: String, mat: Mat, out: MutableList<ContentBox>) {
        var cx = 0f; var cy = 0f; var sx = 0f; var sy = 0f
        var cmd = '\u0000'; var i = 0; val len = d.length
        var hasBox = false
        var minX = Float.POSITIVE_INFINITY; var maxX = Float.NEGATIVE_INFINITY
        var minY = Float.POSITIVE_INFINITY; var maxY = Float.NEGATIVE_INFINITY

        fun addPt(px: Float, py: Float) {
            val gx = mat.mapX(px, py); val gy = mat.mapY(px, py)
            if (!hasBox) {
                hasBox = true; minX = gx; maxX = gx; minY = gy; maxY = gy
            } else {
                minX = min(minX, gx); maxX = max(maxX, gx); minY = min(minY, gy); maxY = max(maxY, gy)
            }
        }
        fun flush() { if (hasBox) { out.add(ContentBox(minX, maxX, minY, maxY)); hasBox = false } }
        fun skipSep() { while (i < len && (d[i] == ',' || d[i].isWhitespace())) i++ }
        fun nextFloat(): Float {
            skipSep()
            if (i >= len) return 0f
            val s = i
            if (d[i] == '+' || d[i] == '-') i++
            var dot = false
            while (i < len) {
                val c = d[i]
                if (c in '0'..'9') i++ else if (c == '.' && !dot) { dot = true; i++ } else break
            }
            if (i < len && (d[i] == 'e' || d[i] == 'E')) {
                val ePos = i; i++
                if (i < len && (d[i] == '+' || d[i] == '-')) i++
                val dPos = i; while (i < len && d[i] in '0'..'9') i++
                if (i == dPos) i = ePos
            }
            return if (i > s) d.substring(s, i).toFloatOrNull() ?: 0f else 0f
        }

        while (i < len) {
            skipSep()
            if (i >= len) break
            val ch = d[i]
            if (ch in 'A'..'Z' || ch in 'a'..'z') {
                cmd = ch; i++
                if (cmd == 'Z' || cmd == 'z') { cx = sx; cy = sy }
                continue
            }
            when (cmd) {
                'M', 'm' -> {
                    flush()
                    val x = nextFloat(); val y = nextFloat()
                    if (cmd == 'm') { cx += x; cy += y; cmd = 'l' } else { cx = x; cy = y; cmd = 'L' }
                    sx = cx; sy = cy; addPt(cx, cy)
                }
                'L', 'l', 'T', 't' -> {
                    val x = nextFloat(); val y = nextFloat()
                    if (cmd == 'l' || cmd == 't') { cx += x; cy += y } else { cx = x; cy = y }
                    addPt(cx, cy)
                }
                'H', 'h' -> { val x = nextFloat(); cx = if (cmd == 'h') cx + x else x; addPt(cx, cy) }
                'V', 'v' -> { val y = nextFloat(); cy = if (cmd == 'v') cy + y else y; addPt(cx, cy) }
                'C', 'c' -> {
                    val x1 = nextFloat(); val y1 = nextFloat(); val x2 = nextFloat(); val y2 = nextFloat()
                    val x = nextFloat(); val y = nextFloat()
                    if (cmd == 'c') {
                        addPt(cx + x1, cy + y1); addPt(cx + x2, cy + y2); cx += x; cy += y; addPt(cx, cy)
                    } else {
                        addPt(x1, y1); addPt(x2, y2); cx = x; cy = y; addPt(cx, cy)
                    }
                }
                'S', 's', 'Q', 'q' -> {
                    val x1 = nextFloat(); val y1 = nextFloat(); val x = nextFloat(); val y = nextFloat()
                    if (cmd == 's' || cmd == 'q') {
                        addPt(cx + x1, cy + y1); cx += x; cy += y; addPt(cx, cy)
                    } else {
                        addPt(x1, y1); cx = x; cy = y; addPt(cx, cy)
                    }
                }
                'A', 'a' -> {
                    repeat(5) { nextFloat() }
                    val x = nextFloat(); val y = nextFloat()
                    if (cmd == 'a') { cx += x; cy += y } else { cx = x; cy = y }
                    addPt(cx, cy)
                }
                else -> i++
            }
        }
        flush()
    }
}
