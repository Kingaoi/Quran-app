package com.example.data.local

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Disk-backed raster cache for already-decoded and symmetrically-cropped Mushaf SVG pages.
 * Eliminates expensive XML SVG parsing and Canvas curve rasterization on revisited or
 * memory-evicted pages, enabling ~10ms native PNG hardware-bitmap decoding.
 */
class MushafRasterCache(context: Context) {
    private val rootDir = File(context.cacheDir, "mushaf_raster")
    private val readyKeys = ConcurrentHashMap.newKeySet<String>()
    private val pendingWrites = ConcurrentHashMap.newKeySet<String>()
    private val ioDispatcher = Dispatchers.IO.limitedParallelism(2)
    private val writeScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    @Volatile
    private var indexed = false

    fun warmIndex() {
        if (indexed) return
        runCatching {
            val dirs = rootDir.listFiles() ?: emptyArray()
            for (dir in dirs) {
                if (!dir.isDirectory) continue
                val bucket = dir.name
                val files = dir.listFiles() ?: continue
                for (file in files) {
                    if (file.isFile && file.name.endsWith(".png") && file.length() > MIN_PNG_BYTES) {
                        val pageNum = file.nameWithoutExtension.toIntOrNull() ?: continue
                        readyKeys.add(makeKey(bucket, pageNum))
                    }
                }
            }
            indexed = true
        }
    }

    fun getCachedFileIfReady(
        riwayahSlug: String,
        pageNumber: Int,
        widthPx: Int,
        heightPx: Int,
        colorKey: String = "def"
    ): File? {
        val bucket = makeBucket(riwayahSlug, colorKey, widthPx, heightPx)
        val key = makeKey(bucket, pageNumber)
        if (!readyKeys.contains(key)) return null
        return pageFile(bucket, pageNumber)
    }

    fun getTotalSizeBytes(): Long = runCatching {
        if (!rootDir.exists() || !rootDir.isDirectory) return 0L
        var total = 0L
        rootDir.listFiles()?.forEach { dir ->
            if (dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    if (file.isFile && file.name.endsWith(".png") && file.length() > MIN_PNG_BYTES) {
                        total += file.length()
                    }
                }
            }
        }
        total
    }.getOrDefault(0L)

    suspend fun clearAllCache() = kotlinx.coroutines.withContext(Dispatchers.IO) {
        readyKeys.clear()
        pendingWrites.clear()
        indexed = false
        runCatching {
            if (rootDir.exists()) {
                rootDir.deleteRecursively()
            }
        }
    }

    fun invalidatePage(
        riwayahSlug: String,
        pageNumber: Int,
        widthPx: Int,
        heightPx: Int,
        colorKey: String = "def"
    ) {
        val bucket = makeBucket(riwayahSlug, colorKey, widthPx, heightPx)
        val key = makeKey(bucket, pageNumber)
        readyKeys.remove(key)
        pendingWrites.remove(key)
        writeScope.launch {
            runCatching { pageFile(bucket, pageNumber).delete() }
        }
    }

    fun saveBitmapAsync(
        riwayahSlug: String,
        pageNumber: Int,
        widthPx: Int,
        heightPx: Int,
        bitmap: Bitmap,
        colorKey: String = "def"
    ) {
        if (bitmap.isRecycled || bitmap.width < 16 || bitmap.height < 16) return
        val bucket = makeBucket(riwayahSlug, colorKey, widthPx, heightPx)
        val key = makeKey(bucket, pageNumber)
        if (readyKeys.contains(key) || !pendingWrites.add(key)) return

        // Copy only if hardware; software ARGB_8888 bitmaps in Coil's cache are immutable
        val snapshot = runCatching {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
                bitmap.config == Bitmap.Config.HARDWARE
            ) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                bitmap
            }
        }.getOrNull() ?: run {
            pendingWrites.remove(key)
            return
        }

        writeScope.launch {
            try {
                val target = pageFile(bucket, pageNumber)
                target.parentFile?.mkdirs()
                if (target.exists() && target.length() > MIN_PNG_BYTES) {
                    readyKeys.add(key)
                    return@launch
                }
                val tmp = File(target.parentFile, "${target.name}.tmp")
                val ok = tmp.outputStream().buffered(16 * 1024).use { out ->
                    snapshot.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                if (ok && tmp.length() > MIN_PNG_BYTES) {
                    if (target.exists()) target.delete()
                    if (tmp.renameTo(target)) {
                        readyKeys.add(key)
                    } else {
                        tmp.delete()
                    }
                } else {
                    tmp.delete()
                }
            } catch (_: Throwable) {
                // Non-fatal cache write failure; SVG source remains intact
            } finally {
                if (snapshot !== bitmap && !snapshot.isRecycled) {
                    snapshot.recycle()
                }
                pendingWrites.remove(key)
            }
        }
    }

    private fun pageFile(bucket: String, pageNumber: Int): File {
        val padded = pageNumber.coerceIn(1, 604).toString().padStart(3, '0')
        return File(File(rootDir, bucket), "$padded.png")
    }

    private fun makeBucket(
        riwayahSlug: String,
        colorKey: String,
        widthPx: Int,
        heightPx: Int
    ): String = if (colorKey == "def") {
        "${riwayahSlug}_${widthPx}x${heightPx}"
    } else {
        "${riwayahSlug}_${colorKey}_${widthPx}x${heightPx}"
    }

    private fun makeKey(bucket: String, pageNumber: Int): String =
        "${bucket}_$pageNumber"

    companion object {
        private const val MIN_PNG_BYTES = 512L
    }
}
