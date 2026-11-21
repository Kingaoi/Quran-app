package com.example.data.local

import android.content.Context
import com.example.data.remote.QuranWsCdnClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class MushafOfflineStorage(context: Context) {
    private val rootDir = File(context.filesDir, "mushaf_offline")
    private val cachedPagesIndex = ConcurrentHashMap<String, MutableSet<Int>>()

    fun editionDir(edition: RiwayahEdition): File = File(rootDir, edition.slug)

    fun pageFile(edition: RiwayahEdition, page: Int): File {
        val padded = page.coerceIn(1, TOTAL_PAGES).toString().padStart(3, '0')
        return File(editionDir(edition), "$padded.svg")
    }

    fun getCachedPageFile(edition: RiwayahEdition, page: Int): File? {
        val file = pageFile(edition, page)
        val valid = file.exists() && file.length() > MIN_SVG_BYTES
        if (valid) {
            cachedPagesIndex[edition.slug]?.add(page)
        }
        return if (valid) file else null
    }

    /**
     * Lock-free O(1) memory lookup used during Compose composition / paging
     * to avoid main-thread filesystem syscalls on every page swipe.
     */
    fun getCachedPageFileFast(edition: RiwayahEdition, page: Int): File? {
        val indexedSet = cachedPagesIndex[edition.slug] ?: return null
        return if (indexedSet.contains(page)) pageFile(edition, page) else null
    }

    fun countDownloadedPages(edition: RiwayahEdition): Int {
        val dir = editionDir(edition)
        if (!dir.exists() || !dir.isDirectory) {
            cachedPagesIndex[edition.slug] = ConcurrentHashMap.newKeySet()
            return 0
        }
        val set = ConcurrentHashMap.newKeySet<Int>()
        val files = dir.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isFile && file.name.endsWith(".svg") && file.length() > MIN_SVG_BYTES) {
                    val pageNum = file.nameWithoutExtension.toIntOrNull()
                    if (pageNum != null && pageNum in 1..TOTAL_PAGES) {
                        set.add(pageNum)
                    }
                }
            }
        }
        cachedPagesIndex[edition.slug] = set
        return set.size.coerceAtMost(TOTAL_PAGES)
    }

    fun getEditionStorageBytes(edition: RiwayahEdition): Long {
        val dir = editionDir(edition)
        if (!dir.exists() || !dir.isDirectory) return 0L
        var total = 0L
        dir.listFiles()?.forEach { file ->
            if (file.isFile && file.name.endsWith(".svg") && file.length() > MIN_SVG_BYTES) {
                total += file.length()
            }
        }
        return total
    }

    suspend fun downloadFullMushaf(
        edition: RiwayahEdition,
        version: String,
        onProgress: (downloaded: Int, total: Int) -> Unit
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = editionDir(edition)
            if (!dir.exists()) dir.mkdirs()

            val indexed = cachedPagesIndex.getOrPut(edition.slug) { ConcurrentHashMap.newKeySet() }
            val missingPages = mutableListOf<Int>()
            var alreadyPresent = 0
            for (page in 1..TOTAL_PAGES) {
                if (getCachedPageFile(edition, page) != null) {
                    indexed.add(page)
                    alreadyPresent++
                } else {
                    missingPages.add(page)
                }
            }

            val completed = AtomicInteger(alreadyPresent)
            onProgress(completed.get(), TOTAL_PAGES)
            if (missingPages.isEmpty()) return@runCatching TOTAL_PAGES

            val semaphore = Semaphore(MAX_CONCURRENT_DOWNLOADS)
            val failedCount = AtomicInteger(0)

            coroutineScope {
                missingPages.map { page ->
                    async {
                        semaphore.withPermit {
                            ensureActive()
                            val success = downloadSinglePage(edition, version, page)
                            if (success) {
                                indexed.add(page)
                                val done = completed.incrementAndGet()
                                onProgress(done, TOTAL_PAGES)
                            } else {
                                failedCount.incrementAndGet()
                            }
                        }
                    }
                }.awaitAll()
            }

            val totalDownloaded = countDownloadedPages(edition)
            if (totalDownloaded < TOTAL_PAGES && failedCount.get() > 0) {
                error("تعذّر تنزيل ${failedCount.get()} صفحة؛ تحقق من الاتصال وأعد المحاولة")
            }
            totalDownloaded
        }
    }

    suspend fun prefetchPagesToDisk(
        edition: RiwayahEdition,
        version: String,
        pages: List<Int>
    ): List<Int> = withContext(Dispatchers.IO) {
        val dir = editionDir(edition)
        if (!dir.exists()) dir.mkdirs()
        val indexed = cachedPagesIndex.getOrPut(edition.slug) { ConcurrentHashMap.newKeySet() }
        val semaphore = Semaphore(2)
        val newlyCached = java.util.Collections.synchronizedList(mutableListOf<Int>())
        coroutineScope {
            pages.distinct().filter { it in 1..TOTAL_PAGES }.map { page ->
                async {
                    if (getCachedPageFile(edition, page) != null) return@async
                    semaphore.withPermit {
                        ensureActive()
                        if (downloadSinglePage(edition, version, page)) {
                            indexed.add(page)
                            newlyCached.add(page)
                        }
                    }
                }
            }.awaitAll()
        }
        newlyCached.toList()
    }

    suspend fun deleteEdition(edition: RiwayahEdition) = withContext(Dispatchers.IO) {
        cachedPagesIndex[edition.slug] = ConcurrentHashMap.newKeySet()
        val dir = editionDir(edition)
        if (dir.exists()) {
            dir.deleteRecursively()
        }
    }

    private fun downloadSinglePage(edition: RiwayahEdition, version: String, page: Int): Boolean {
        val target = pageFile(edition, page)
        val tempFile = File(target.parentFile, "${target.name}.tmp")
        val urls = listOf(
            QuranWsCdnClient.buildSvgPageUrl(version, edition.slug, page),
            QuranWsCdnClient.buildRawGithubSvgUrl(edition.slug, page)
        )
        for (url in urls) {
            val ok = runCatching {
                val req = Request.Builder().url(url).build()
                QuranWsCdnClient.okHttpClient.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@use false
                    val body = resp.body ?: return@use false
                    tempFile.outputStream().use { out ->
                        body.byteStream().copyTo(out)
                    }
                    if (tempFile.length() > MIN_SVG_BYTES) {
                        if (target.exists()) target.delete()
                        tempFile.renameTo(target)
                    } else {
                        tempFile.delete()
                        false
                    }
                }
            }.getOrDefault(false)
            if (ok) return true
            if (tempFile.exists()) tempFile.delete()
        }
        return false
    }

    companion object {
        const val TOTAL_PAGES = 604
        private const val MIN_SVG_BYTES = 128L
        private const val MAX_CONCURRENT_DOWNLOADS = 6
    }
}
