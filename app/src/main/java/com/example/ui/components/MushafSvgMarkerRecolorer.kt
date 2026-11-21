package com.example.ui.components

import android.graphics.Color

/**
 * Rewrites the fill color of ayah marker ornaments (<g class="ayah-mark-ornament">)
 * and/or ayah number glyphs (the sibling <g ... ayah:x="...">) inside quran.ws SVG pages
 * in linear O(N) time without loading a DOM tree.
 */
object MushafSvgMarkerRecolorer {
    private val FILL_ATTR_REGEX = Regex("""\bfill\s*=\s*(["'])(?!none\1)([^"']+)\1""", RegexOption.IGNORE_CASE)
    private val STYLE_FILL_REGEX = Regex("""(\bfill\s*:\s*)(?!none\b)([^;"']+)""", RegexOption.IGNORE_CASE)
    private val ST1_CLASS_REGEX = Regex("""\bclass\s*=\s*(["'])st1\1""", RegexOption.IGNORE_CASE)
    private val SHAPE_TAG_REGEX = Regex("""<(path|circle|polygon|rect|ellipse)\b([^>]*)(/?)>""", RegexOption.IGNORE_CASE)

    fun recolorAyahMarkers(
        svg: String,
        ornamentHex: String?,
        numberHex: String?
    ): String {
        if (ornamentHex == null && numberHex == null) return svg
        if (!svg.contains("ayah-mark")) return svg

        val sb = StringBuilder(svg.length + 1024)
        var cursor = 0
        val len = svg.length

        while (cursor < len) {
            val gStart = findNextAyahMarkGroupStart(svg, cursor)
            if (gStart < 0) {
                sb.append(svg, cursor, len)
                break
            }
            val gEnd = findMatchingGroupEnd(svg, gStart)
            if (gEnd < 0) {
                sb.append(svg, cursor, len)
                break
            }

            sb.append(svg, cursor, gStart)
            val block = svg.substring(gStart, gEnd)
            sb.append(recolorSingleAyahMarkBlock(block, ornamentHex, numberHex))
            cursor = gEnd
        }

        return sb.toString()
    }

    /**
     * Pre-inverts a desired Night Mode color against the Reader's ColorMatrix filter
     * (R' = 243 - R, G' = 235 - G, B' = 218 - B) so the rendered night pixel matches
     * [desiredNightHex] cleanly on dark paper.
     */
    fun invertHexForNightFilter(desiredNightHex: String): String {
        val parsed = runCatching { Color.parseColor(desiredNightHex) }.getOrNull()
            ?: return desiredNightHex
        val r = (243 - Color.red(parsed)).coerceIn(0, 255)
        val g = (235 - Color.green(parsed)).coerceIn(0, 255)
        val b = (218 - Color.blue(parsed)).coerceIn(0, 255)
        return String.format("#%02x%02x%02x", r, g, b)
    }

    private fun recolorSingleAyahMarkBlock(
        block: String,
        ornamentHex: String?,
        numberHex: String?
    ): String {
        val ornIdx = block.indexOf("ayah-mark-ornament")
        if (ornIdx < 0) {
            return if (ornamentHex != null) recolorShapeFills(block, ornamentHex) else block
        }
        val ornGroupStart = block.lastIndexOf("<g", ornIdx)
        val ornGroupEnd = if (ornGroupStart >= 0) findMatchingGroupEnd(block, ornGroupStart) else -1
        if (ornGroupStart < 0 || ornGroupEnd < 0) {
            return if (ornamentHex != null) recolorShapeFills(block, ornamentHex) else block
        }

        val prefix = block.substring(0, ornGroupStart)
        val ornamentPart = block.substring(ornGroupStart, ornGroupEnd)
        val numberPart = block.substring(ornGroupEnd)

        val newOrnament = if (ornamentHex != null) {
            recolorShapeFills(ornamentPart, ornamentHex)
        } else {
            ornamentPart
        }
        val newNumber = if (numberHex != null) {
            recolorShapeFills(numberPart, numberHex)
        } else {
            numberPart
        }
        return prefix + newOrnament + newNumber
    }

    private fun recolorShapeFills(segment: String, hexColor: String): String {
        var updated = FILL_ATTR_REGEX.replace(segment) { """fill="$hexColor"""" }
        updated = STYLE_FILL_REGEX.replace(updated) { "${it.groupValues[1]}$hexColor" }
        updated = ST1_CLASS_REGEX.replace(updated) { """fill="$hexColor"""" }
        updated = SHAPE_TAG_REGEX.replace(updated) { match ->
            val tagName = match.groupValues[1]
            val attrs = match.groupValues[2]
            val selfClose = match.groupValues[3]
            if (attrs.contains("fill=", ignoreCase = true) || attrs.contains("fill:", ignoreCase = true)) {
                match.value
            } else {
                "<$tagName fill=\"$hexColor\"$attrs$selfClose>"
            }
        }
        return updated
    }

    private fun findNextAyahMarkGroupStart(svg: String, fromIndex: Int): Int {
        var searchPos = fromIndex
        while (true) {
            val hit = svg.indexOf("ayah-mark", searchPos)
            if (hit < 0) return -1
            val after = hit + "ayah-mark".length
            val nextCh = if (after < svg.length) svg[after] else '\u0000'
            val prevCh = if (hit > 0) svg[hit - 1] else '\u0000'
            val isExactClassToken =
                (prevCh == '"' || prevCh == '\'' || prevCh.isWhitespace()) &&
                    (nextCh == '"' || nextCh == '\'' || nextCh.isWhitespace())
            if (isExactClassToken) {
                val gStart = svg.lastIndexOf("<g", hit)
                if (gStart >= fromIndex) {
                    val tagClose = svg.indexOf('>', gStart)
                    if (tagClose > hit) return gStart
                }
            }
            searchPos = after
        }
    }

    private fun findMatchingGroupEnd(svg: String, groupStart: Int): Int {
        var pos = groupStart
        var depth = 0
        val len = svg.length

        while (pos < len) {
            val nextOpen = svg.indexOf("<g", pos)
            val nextClose = svg.indexOf("</g>", pos)
            if (nextClose < 0) return -1

            if (nextOpen in 0 until nextClose) {
                val afterTag = if (nextOpen + 2 < len) svg[nextOpen + 2] else '\u0000'
                if (afterTag == '>' || afterTag.isWhitespace()) {
                    depth++
                }
                pos = nextOpen + 2
            } else {
                depth--
                val closeEnd = nextClose + 4
                if (depth <= 0) return closeEnd
                pos = closeEnd
            }
        }
        return -1
    }
}
