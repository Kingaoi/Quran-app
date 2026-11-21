package com.example.data.tafsir

import android.content.Context
import com.example.data.remote.QuranWsCdnClient
import com.example.domain.model.TafsirEdition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class TafsirRepository(context: Context) {
    private val rootDir = File(context.filesDir, "tafsir_cache")
    private val memoryCache = ConcurrentHashMap<String, Map<Int, String>>()

    fun editionDir(edition: TafsirEdition): File = File(rootDir, edition.slug)

    fun surahFile(edition: TafsirEdition, surah: Int): File =
        File(editionDir(edition), "$surah.json")

    fun countDownloadedSurahs(edition: TafsirEdition): Int {
        val dir = editionDir(edition)
        if (!dir.exists() || !dir.isDirectory) return 0
        return dir.listFiles()
            ?.count { it.isFile && it.name.endsWith(".json") && it.length() > 30L }
            ?.coerceAtMost(114) ?: 0
    }

    fun getEditionStorageBytes(edition: TafsirEdition): Long {
        val dir = editionDir(edition)
        if (!dir.exists() || !dir.isDirectory) return 0L
        var total = 0L
        dir.listFiles()?.forEach { file ->
            if (file.isFile && file.name.endsWith(".json") && file.length() > 30L) {
                total += file.length()
            }
        }
        return total
    }

    suspend fun getAyahTafsir(
        edition: TafsirEdition,
        surah: Int,
        ayah: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val cacheKey = "${edition.slug}:$surah"
            memoryCache[cacheKey]?.get(ayah)?.let { return@runCatching it }

            val file = surahFile(edition, surah)
            if (file.exists() && file.length() > 30L) {
                val parsed = parseSurahTafsirJson(file.readText())
                if (parsed.isNotEmpty()) {
                    memoryCache[cacheKey] = parsed
                    parsed[ayah]?.let { return@runCatching it }
                }
            }

            // Fetch full Surah JSON from spa5k tafsir_api CDN and cache for offline access
            val surahMap = fetchAndCacheSurah(edition, surah)
            surahMap[ayah]?.let { return@runCatching it }

            // Fallback single-ayah endpoint for Muyassar
            if (edition == TafsirEdition.MUYASSAR) {
                fetchSingleAyahFallback(surah, ayah)?.let { return@runCatching it }
            }

            error("تعذّر العثور على تفسير هذه الآية حالياً")
        }
    }

    suspend fun downloadFullTafsir(
        edition: TafsirEdition,
        onProgress: (downloaded: Int, total: Int) -> Unit
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = editionDir(edition)
            if (!dir.exists()) dir.mkdirs()

            val missingSurahs = mutableListOf<Int>()
            var alreadyPresent = 0
            for (surah in 1..114) {
                val file = surahFile(edition, surah)
                if (file.exists() && file.length() > 30L) {
                    alreadyPresent++
                } else {
                    missingSurahs.add(surah)
                }
            }

            val completed = AtomicInteger(alreadyPresent)
            onProgress(completed.get(), 114)
            if (missingSurahs.isEmpty()) return@runCatching 114

            val semaphore = Semaphore(4)
            coroutineScope {
                missingSurahs.map { surah ->
                    async {
                        semaphore.withPermit {
                            ensureActive()
                            runCatching { fetchAndCacheSurah(edition, surah) }
                            val file = surahFile(edition, surah)
                            if (file.exists() && file.length() > 30L) {
                                val done = completed.incrementAndGet()
                                onProgress(done.coerceAtMost(114), 114)
                            }
                        }
                    }
                }.awaitAll()
            }

            val finalCount = countDownloadedSurahs(edition)
            if (finalCount == 0) {
                error("تعذّر تنزيل التفسير، تحقق من اتصال الإنترنت")
            }
            if (finalCount < 114) {
                error("اكتمل تنزيل $finalCount من ١١٤ سورة؛ أعد المحاولة للاستكمال")
            }
            finalCount
        }
    }

    suspend fun deleteEdition(edition: TafsirEdition) = withContext(Dispatchers.IO) {
        val dir = editionDir(edition)
        if (dir.exists()) dir.deleteRecursively()
        memoryCache.keys.removeAll { it.startsWith("${edition.slug}:") }
    }

    private fun fetchAndCacheSurah(edition: TafsirEdition, surah: Int): Map<Int, String> {
        val url = "https://cdn.jsdelivr.net/gh/spa5k/tafsir_api@main/tafsir/${edition.slug}/$surah.json"
        val req = Request.Builder().url(url).build()
        QuranWsCdnClient.okHttpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return emptyMap()
            val raw = resp.body?.string().orEmpty()
            val parsed = parseSurahTafsirJson(raw)
            if (parsed.isNotEmpty()) {
                val dir = editionDir(edition)
                if (!dir.exists()) dir.mkdirs()
                surahFile(edition, surah).writeText(raw)
                memoryCache["${edition.slug}:$surah"] = parsed
            }
            return parsed
        }
    }

    private fun fetchSingleAyahFallback(surah: Int, ayah: Int): String? {
        val url = "https://api.alquran.cloud/v1/ayah/$surah:$ayah/ar.muyassar"
        val req = Request.Builder().url(url).build()
        return runCatching {
            QuranWsCdnClient.okHttpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                val body = resp.body?.string().orEmpty()
                val data = JSONObject(body).optJSONObject("data") ?: return@use null
                data.optString("text").takeIf { it.isNotBlank() }
            }
        }.getOrNull()
    }

    private fun parseSurahTafsirJson(raw: String): Map<Int, String> {
        if (raw.isBlank()) return emptyMap()
        return runCatching {
            val arr: JSONArray = when {
                raw.trimStart().startsWith("[") -> JSONArray(raw)
                else -> JSONObject(raw).optJSONArray("ayahs") ?: JSONArray()
            }
            buildMap(arr.length()) {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val ayahNum = obj.optInt("ayah", -1)
                        .let { if (it > 0) it else obj.optInt("numberInSurah", -1) }
                    val text = obj.optString("text").trim()
                    if (ayahNum > 0 && text.isNotBlank()) {
                        put(ayahNum, text)
                    }
                }
            }
        }.getOrDefault(emptyMap())
    }
}
