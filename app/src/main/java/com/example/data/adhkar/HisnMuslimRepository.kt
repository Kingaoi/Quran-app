package com.example.data.adhkar

import android.content.Context
import com.example.data.remote.QuranWsCdnClient
import com.example.domain.model.DhikrCategory
import com.example.domain.model.DhikrItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File

class HisnMuslimRepository(context: Context) {
    private val cacheFile = File(File(context.filesDir, "adhkar_hisn_cache"), "hisn_ar_v1.json")

    fun getCacheStatus(): Pair<Boolean, Long> {
        val exists = cacheFile.exists() && cacheFile.length() > 32L
        return exists to (if (exists) cacheFile.length() else 0L)
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        if (cacheFile.exists()) cacheFile.delete()
    }

    suspend fun loadCachedCatalog(): CachedHisnCatalog? = withContext(Dispatchers.IO) {
        if (!cacheFile.exists() || cacheFile.length() < 32L) return@withContext null
        runCatching { HisnMuslimParser.decodeCache(cacheFile.readText()) }.getOrNull()
    }

    suspend fun fetchAndCacheFromApi(
        onProgress: ((downloadedChapters: Int, totalChapters: Int) -> Unit)? = null
    ): Result<Map<DhikrCategory, List<DhikrItem>>> = withContext(Dispatchers.IO) {
        runCatching {
            val indexRaw = fetchUrlText(HISN_MUSLIM_INDEX_URL)
                ?: error("تعذّر الاتصال بمصدر حصن المسلم")
            val allChapters = HisnMuslimParser.parseIndex(indexRaw)
            if (allChapters.isEmpty()) {
                error("تعذّر قراءة فهرس الأذكار من حصن المسلم")
            }

            val matchedPairs = allChapters.mapNotNull { ch ->
                val cats = HisnCategoryMapper.mapChapterToCategories(ch)
                if (cats.isEmpty()) null else ch to cats
            }

            val uniqueUrls = matchedPairs.map { it.first.textUrl }.distinct()
            val totalUrls = uniqueUrls.size.coerceAtLeast(1)
            val completedCount = java.util.concurrent.atomic.AtomicInteger(0)
            onProgress?.invoke(0, totalUrls)

            val semaphore = Semaphore(MAX_CONCURRENT_CHAPTER_FETCHES)
            val payloadsByUrl = coroutineScope {
                uniqueUrls.map { url ->
                    async {
                        semaphore.withPermit {
                            val text = fetchUrlText(url)
                            val done = completedCount.incrementAndGet()
                            onProgress?.invoke(done, totalUrls)
                            url to text
                        }
                    }
                }.awaitAll().toMap()
            }

            val grouped = DhikrCategory.entries.associateWith { mutableListOf<DhikrItem>() }
            for ((chapter, categories) in matchedPairs) {
                val chapterJson = payloadsByUrl[chapter.textUrl] ?: continue
                for (category in categories) {
                    val items = HisnMuslimParser.parseChapterItems(chapterJson, chapter, category)
                    grouped[category]?.addAll(items)
                }
            }

            val merged = DhikrCategory.entries.associateWith { cat ->
                val remoteItems = grouped[cat].orEmpty()
                if (remoteItems.isNotEmpty()) remoteItems else AdhkarCatalog.byCategory(cat)
            }

            val remoteCount = grouped.values.count { it.isNotEmpty() }
            if (remoteCount == 0) {
                error("لم يتم العثور على أبواب مطابقة في المصدر")
            }

            saveCacheAtomically(System.currentTimeMillis(), merged)
            merged
        }
    }

    private fun fetchUrlText(url: String): String? = runCatching {
        val req = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .build()
        QuranWsCdnClient.okHttpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@use null
            resp.body?.string()?.takeIf { it.isNotBlank() }
        }
    }.getOrNull()

    private fun saveCacheAtomically(timestampMillis: Long, map: Map<DhikrCategory, List<DhikrItem>>) {
        cacheFile.parentFile?.mkdirs()
        val temp = File(cacheFile.parentFile, "${cacheFile.name}.tmp")
        temp.writeText(HisnMuslimParser.encodeCache(timestampMillis, map))
        if (cacheFile.exists()) cacheFile.delete()
        temp.renameTo(cacheFile)
    }

    companion object {
        const val CACHE_MAX_AGE_MS = 7L * 24L * 60L * 60L * 1000L
        private const val MAX_CONCURRENT_CHAPTER_FETCHES = 6
    }
}
