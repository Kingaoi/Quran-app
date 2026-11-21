package com.example.ui.components

import android.graphics.RectF
import kotlin.math.cos
import kotlin.math.sin

internal data class SvgTransformMat(
    val a: Float,
    val b: Float,
    val c: Float,
    val d: Float,
    val e: Float,
    val f: Float
) {
    fun mul(o: SvgTransformMat) = SvgTransformMat(
        a * o.a + c * o.b,
        b * o.a + d * o.b,
        a * o.c + c * o.d,
        b * o.c + d * o.d,
        a * o.e + c * o.f + e,
        b * o.e + d * o.f + f
    )

    fun mapX(x: Float, y: Float): Float = a * x + c * y + e
    fun mapY(x: Float, y: Float): Float = b * x + d * y + f

    companion object {
        val IDENTITY = SvgTransformMat(1f, 0f, 0f, 1f, 0f, 0f)

        private val TRANSFORM_FN_REGEX = Regex("""(\w+)\s*\(([^)]+)\)""")
        private val NUM_REGEX = Regex("""-?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?""")

        fun parse(tr: String?): SvgTransformMat {
            if (tr.isNullOrBlank()) return IDENTITY
            var mat = IDENTITY
            for (m in TRANSFORM_FN_REGEX.findAll(tr)) {
                val nums = NUM_REGEX.findAll(m.groupValues[2]).mapNotNull { it.value.toFloatOrNull() }.toList()
                val next = when (m.groupValues[1].lowercase()) {
                    "translate" -> if (nums.isNotEmpty()) {
                        SvgTransformMat(1f, 0f, 0f, 1f, nums[0], nums.getOrElse(1) { 0f })
                    } else null
                    "scale" -> if (nums.isNotEmpty()) {
                        SvgTransformMat(nums[0], 0f, 0f, nums.getOrElse(1) { nums[0] }, 0f, 0f)
                    } else null
                    "rotate" -> if (nums.isNotEmpty()) {
                        val rad = Math.toRadians(nums[0].toDouble())
                        val cosA = cos(rad).toFloat()
                        val sinA = sin(rad).toFloat()
                        if (nums.size >= 3) {
                            val cx = nums[1]
                            val cy = nums[2]
                            SvgTransformMat(1f, 0f, 0f, 1f, cx, cy)
                                .mul(SvgTransformMat(cosA, sinA, -sinA, cosA, 0f, 0f))
                                .mul(SvgTransformMat(1f, 0f, 0f, 1f, -cx, -cy))
                        } else {
                            SvgTransformMat(cosA, sinA, -sinA, cosA, 0f, 0f)
                        }
                    } else null
                    "matrix" -> if (nums.size == 6) {
                        SvgTransformMat(nums[0], nums[1], nums[2], nums[3], nums[4], nums[5])
                    } else null
                    else -> null
                }
                if (next != null) mat = mat.mul(next)
            }
            return mat
        }
    }
}

internal data class ParsedSubpathGeometry(
    val rect: RectF,
    val polygon: FloatArray
)

internal object MushafSvgPathParser {

    fun parsePathSubpaths(d: String, mat: SvgTransformMat): List<ParsedSubpathGeometry> {
        val out = ArrayList<ParsedSubpathGeometry>(4)
        var cx = 0f
        var cy = 0f
        var sx = 0f
        var sy = 0f
        var cmd = '\u0000'
        var i = 0
        val len = d.length

        val pts = ArrayList<Float>(16)
        var minX = Float.POSITIVE_INFINITY
        var maxX = Float.NEGATIVE_INFINITY
        var minY = Float.POSITIVE_INFINITY
        var maxY = Float.NEGATIVE_INFINITY

        fun addPt(px: Float, py: Float, recordVertex: Boolean = true) {
            val gx = mat.mapX(px, py)
            val gy = mat.mapY(px, py)
            if (recordVertex) {
                val n = pts.size
                if (n < 2 || kotlin.math.abs(gx - pts[n - 2]) > 0.05f || kotlin.math.abs(gy - pts[n - 1]) > 0.05f) {
                    pts.add(gx)
                    pts.add(gy)
                }
            }
            if (gx < minX) minX = gx
            if (gx > maxX) maxX = gx
            if (gy < minY) minY = gy
            if (gy > maxY) maxY = gy
        }

        fun flush() {
            if (pts.size >= 4 && minX.isFinite() && minY.isFinite() && maxX > minX && maxY > minY) {
                out.add(ParsedSubpathGeometry(RectF(minX, minY, maxX, maxY), pts.toFloatArray()))
            }
            pts.clear()
            minX = Float.POSITIVE_INFINITY
            maxX = Float.NEGATIVE_INFINITY
            minY = Float.POSITIVE_INFINITY
            maxY = Float.NEGATIVE_INFINITY
        }

        fun skipSep() {
            while (i < len && (d[i] == ',' || d[i].isWhitespace())) i++
        }

        fun nextFloat(): Float {
            skipSep()
            if (i >= len) return 0f
            val s = i
            if (d[i] == '+' || d[i] == '-') i++
            var dot = false
            while (i < len) {
                val c = d[i]
                if (c in '0'..'9') {
                    i++
                } else if (c == '.' && !dot) {
                    dot = true
                    i++
                } else {
                    break
                }
            }
            if (i < len && (d[i] == 'e' || d[i] == 'E')) {
                val ePos = i
                i++
                if (i < len && (d[i] == '+' || d[i] == '-')) i++
                val dPos = i
                while (i < len && d[i] in '0'..'9') i++
                if (i == dPos) i = ePos
            }
            return if (i > s) d.substring(s, i).toFloatOrNull() ?: 0f else 0f
        }

        while (i < len) {
            skipSep()
            if (i >= len) break
            val ch = d[i]
            if (ch in 'A'..'Z' || ch in 'a'..'z') {
                cmd = ch
                i++
                if (cmd == 'Z' || cmd == 'z') {
                    cx = sx
                    cy = sy
                }
                continue
            }
            when (cmd) {
                'M', 'm' -> {
                    flush()
                    val x = nextFloat()
                    val y = nextFloat()
                    if (cmd == 'm') {
                        cx += x
                        cy += y
                        cmd = 'l'
                    } else {
                        cx = x
                        cy = y
                        cmd = 'L'
                    }
                    sx = cx
                    sy = cy
                    addPt(cx, cy)
                }
                'L', 'l', 'T', 't' -> {
                    val x = nextFloat()
                    val y = nextFloat()
                    if (cmd == 'l' || cmd == 't') {
                        cx += x
                        cy += y
                    } else {
                        cx = x
                        cy = y
                    }
                    addPt(cx, cy)
                }
                'H', 'h' -> {
                    val x = nextFloat()
                    cx = if (cmd == 'h') cx + x else x
                    addPt(cx, cy)
                }
                'V', 'v' -> {
                    val y = nextFloat()
                    cy = if (cmd == 'v') cy + y else y
                    addPt(cx, cy)
                }
                'C', 'c' -> {
                    val x1 = nextFloat(); val y1 = nextFloat()
                    val x2 = nextFloat(); val y2 = nextFloat()
                    val x = nextFloat(); val y = nextFloat()
                    if (cmd == 'c') {
                        addPt(cx + x1, cy + y1, recordVertex = false)
                        addPt(cx + x2, cy + y2, recordVertex = false)
                        cx += x; cy += y
                        addPt(cx, cy)
                    } else {
                        addPt(x1, y1, recordVertex = false)
                        addPt(x2, y2, recordVertex = false)
                        cx = x; cy = y
                        addPt(cx, cy)
                    }
                }
                'S', 's', 'Q', 'q' -> {
                    val x1 = nextFloat(); val y1 = nextFloat()
                    val x = nextFloat(); val y = nextFloat()
                    if (cmd == 's' || cmd == 'q') {
                        addPt(cx + x1, cy + y1, recordVertex = false)
                        cx += x; cy += y
                        addPt(cx, cy)
                    } else {
                        addPt(x1, y1, recordVertex = false)
                        cx = x; cy = y
                        addPt(cx, cy)
                    }
                }
                'A', 'a' -> {
                    repeat(5) { nextFloat() }
                    val x = nextFloat(); val y = nextFloat()
                    if (cmd == 'a') {
                        cx += x; cy += y
                    } else {
                        cx = x; cy = y
                    }
                    addPt(cx, cy)
                }
                else -> i++
            }
        }
        flush()
        return out
    }

    fun pointInPolygon(x: Float, y: Float, poly: FloatArray): Boolean {
        val count = poly.size / 2
        if (count < 3) return false
        var inside = false
        var j = count - 1
        for (i in 0 until count) {
            val xi = poly[i * 2]
            val yi = poly[i * 2 + 1]
            val xj = poly[j * 2]
            val yj = poly[j * 2 + 1]
            val intersects = ((yi > y) != (yj > y)) &&
                (x < (xj - xi) * (y - yi) / ((yj - yi).let { if (it == 0f) 1e-6f else it }) + xi)
            if (intersects) inside = !inside
            j = i
        }
        return inside
    }
}
