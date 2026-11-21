package com.example.ui.components

import android.content.Context
import android.graphics.RectF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

data class CropTransformMetrics(
    val inputW: Int,
    val inputH: Int,
    val srcLeft: Int,
    val srcTop: Int,
    val srcW: Int,
    val srcH: Int,
    val dstLeft: Int,
    val dstTop: Int,
    val dstW: Int,
    val dstH: Int,
    val targetW: Int,
    val targetH: Int
)

data class SvgViewBoxRect(
    val minX: Float,
    val minY: Float,
    val width: Float,
    val height: Float
)

object MushafCropCoordinateMap {
    private val metricsCache = ConcurrentHashMap<String, CropTransformMetrics>()
    private val _metricsRevision = MutableStateFlow(0)
    val metricsRevision: StateFlow<Int> = _metricsRevision.asStateFlow()

    @Volatile
    private var cacheDir: File? = null

    @Volatile
    private var warmed = false

    fun init(context: Context) {
        if (cacheDir == null) {
            cacheDir = File(context.applicationContext.cacheDir, "mushaf_crop_metrics").apply { mkdirs() }
        }
    }

    suspend fun warmCache(context: Context) = withContext(Dispatchers.IO) {
        init(context)
        if (warmed) return@withContext
        val dir = cacheDir ?: return@withContext
        val files = dir.listFiles() ?: run { warmed = true; return@withContext }
        var loadedAny = false
        for (file in files) {
            if (!file.isFile || !file.name.endsWith(".txt")) continue
            val key = file.nameWithoutExtension
            if (metricsCache.containsKey(key)) continue
            parseMetricsFile(file)?.let { parsed ->
                metricsCache[key] = parsed
                loadedAny = true
            }
        }
        warmed = true
        if (loadedAny) {
            _metricsRevision.update { it + 1 }
        }
    }

    suspend fun ensureMetricsLoaded(pageKey: String) {
        val baseKey = normalizeBaseKey(pageKey)
        if (metricsCache.containsKey(pageKey) || metricsCache.containsKey(baseKey)) return
        val loaded = withContext(Dispatchers.IO) { loadPersistedMetrics(baseKey) } ?: return
        val prev = metricsCache.putIfAbsent(baseKey, loaded)
        metricsCache[pageKey] = loaded
        if (prev != loaded) {
            _metricsRevision.update { it + 1 }
        }
    }

    fun recordMetrics(pageKey: String, metrics: CropTransformMetrics) {
        val baseKey = normalizeBaseKey(pageKey)
        val prev = metricsCache[baseKey]
        metricsCache[pageKey] = metrics
        metricsCache[baseKey] = metrics
        if (prev != metrics) {
            persistMetrics(baseKey, metrics)
            _metricsRevision.update { it + 1 }
        }
    }

    /**
     * Lock-free O(1) memory lookup safe for Compose composition & Canvas draw.
     */
    fun getMetrics(pageKey: String): CropTransformMetrics? {
        val baseKey = normalizeBaseKey(pageKey)
        return metricsCache[pageKey] ?: metricsCache[baseKey]
    }

    fun svgRectToViewport(
        svgRect: RectF,
        viewBox: SvgViewBoxRect,
        metrics: CropTransformMetrics?,
        boxWidthPx: Float,
        boxHeightPx: Float
    ): RectF {
        val out = FloatArray(4)
        writeSvgPointToViewport(svgRect.left, svgRect.top, viewBox, metrics, boxWidthPx, boxHeightPx, out, 0)
        writeSvgPointToViewport(svgRect.right, svgRect.bottom, viewBox, metrics, boxWidthPx, boxHeightPx, out, 2)
        return RectF(out[0], out[1], out[2], out[3])
    }

    fun svgPointToViewport(
        svgX: Float,
        svgY: Float,
        viewBox: SvgViewBoxRect,
        metrics: CropTransformMetrics?,
        boxWidthPx: Float,
        boxHeightPx: Float
    ): Pair<Float, Float> {
        val out = FloatArray(2)
        writeSvgPointToViewport(svgX, svgY, viewBox, metrics, boxWidthPx, boxHeightPx, out, 0)
        return out[0] to out[1]
    }

    fun writeSvgPointToViewport(
        svgX: Float,
        svgY: Float,
        viewBox: SvgViewBoxRect,
        metrics: CropTransformMetrics?,
        boxWidthPx: Float,
        boxHeightPx: Float,
        out: FloatArray,
        outOffset: Int = 0
    ) {
        if (viewBox.width <= 0f || viewBox.height <= 0f || boxWidthPx <= 0f || boxHeightPx <= 0f) {
            out[outOffset] = 0f
            out[outOffset + 1] = 0f
            return
        }
        val m = metrics ?: identityMetrics(boxWidthPx.toInt().coerceAtLeast(1), boxHeightPx.toInt().coerceAtLeast(1))
        val svgScale = min(m.inputW.toFloat() / viewBox.width, m.inputH.toFloat() / viewBox.height)
        val svgOffX = (m.inputW - viewBox.width * svgScale) / 2f
        val svgOffY = (m.inputH - viewBox.height * svgScale) / 2f
        val inputX = svgOffX + (svgX - viewBox.minX) * svgScale
        val inputY = svgOffY + (svgY - viewBox.minY) * svgScale

        val outX = m.dstLeft + ((inputX - m.srcLeft) / m.srcW.coerceAtLeast(1).toFloat()) * m.dstW
        val outY = m.dstTop + ((inputY - m.srcTop) / m.srcH.coerceAtLeast(1).toFloat()) * m.dstH

        val fitScale = min(boxWidthPx / m.targetW.toFloat(), boxHeightPx / m.targetH.toFloat())
        val fitOffX = (boxWidthPx - m.targetW * fitScale) / 2f
        val fitOffY = (boxHeightPx - m.targetH * fitScale) / 2f

        out[outOffset] = fitOffX + outX * fitScale
        out[outOffset + 1] = fitOffY + outY * fitScale
    }

    fun viewportPointToSvg(
        vx: Float,
        vy: Float,
        viewBox: SvgViewBoxRect,
        metrics: CropTransformMetrics?,
        boxWidthPx: Float,
        boxHeightPx: Float
    ): Pair<Float, Float>? {
        if (viewBox.width <= 0f || viewBox.height <= 0f || boxWidthPx <= 0f || boxHeightPx <= 0f) {
            return null
        }
        val m = metrics ?: identityMetrics(boxWidthPx.toInt().coerceAtLeast(1), boxHeightPx.toInt().coerceAtLeast(1))
        val fitScale = min(boxWidthPx / m.targetW.toFloat(), boxHeightPx / m.targetH.toFloat())
        if (fitScale <= 0f) return null
        val fitOffX = (boxWidthPx - m.targetW * fitScale) / 2f
        val fitOffY = (boxHeightPx - m.targetH * fitScale) / 2f

        val outX = (vx - fitOffX) / fitScale
        val outY = (vy - fitOffY) / fitScale

        val inputX = m.srcLeft + ((outX - m.dstLeft) / m.dstW.coerceAtLeast(1).toFloat()) * m.srcW
        val inputY = m.srcTop + ((outY - m.dstTop) / m.dstH.coerceAtLeast(1).toFloat()) * m.srcH

        val svgScale = min(m.inputW.toFloat() / viewBox.width, m.inputH.toFloat() / viewBox.height)
        if (svgScale <= 0f) return null
        val svgOffX = (m.inputW - viewBox.width * svgScale) / 2f
        val svgOffY = (m.inputH - viewBox.height * svgScale) / 2f

        val svgX = viewBox.minX + (inputX - svgOffX) / svgScale
        val svgY = viewBox.minY + (inputY - svgOffY) / svgScale
        return svgX to svgY
    }

    private fun identityMetrics(w: Int, h: Int): CropTransformMetrics =
        CropTransformMetrics(w, h, 0, 0, w, h, 0, 0, w, h, w, h)

    private fun normalizeBaseKey(pageKey: String): String =
        if (pageKey.endsWith("_b")) pageKey.dropLast(2) else pageKey

    private fun persistMetrics(baseKey: String, m: CropTransformMetrics) {
        val dir = cacheDir ?: return
        runCatching {
            val safe = baseKey.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val file = File(dir, "$safe.txt")
            file.writeText(
                "${m.inputW},${m.inputH},${m.srcLeft},${m.srcTop},${m.srcW},${m.srcH},${m.dstLeft},${m.dstTop},${m.dstW},${m.dstH},${m.targetW},${m.targetH}"
            )
        }
    }

    private fun loadPersistedMetrics(baseKey: String): CropTransformMetrics? {
        val dir = cacheDir ?: return null
        val safe = baseKey.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return parseMetricsFile(File(dir, "$safe.txt"))
    }

    private fun parseMetricsFile(file: File): CropTransformMetrics? = runCatching {
        if (!file.exists()) return null
        val parts = file.readText().trim().split(',').map { it.toInt() }
        if (parts.size != 12) return null
        CropTransformMetrics(
            inputW = parts[0],
            inputH = parts[1],
            srcLeft = parts[2],
            srcTop = parts[3],
            srcW = parts[4],
            srcH = parts[5],
            dstLeft = parts[6],
            dstTop = parts[7],
            dstW = parts[8],
            dstH = parts[9],
            targetW = parts[10],
            targetH = parts[11]
        )
    }.getOrNull()
}
