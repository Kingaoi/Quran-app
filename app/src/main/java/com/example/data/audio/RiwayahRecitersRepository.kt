package com.example.data.audio

import android.content.Context
import com.example.data.local.RiwayahEdition
import com.example.data.remote.QuranWsCdnClient
import com.example.domain.model.QuranReciters
import com.example.domain.model.ReciterOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import java.io.File

class RiwayahRecitersRepository(context: Context) {
    private val cacheFile = File(context.filesDir, "mp3quran_riwayat_reciters_v1.json")

    suspend fun loadMultiRiwayahReciters(forceRefresh: Boolean = false): List<ReciterOption> =
        withContext(Dispatchers.IO) {
            val cachedJson = readCachedJsonIfFresh(forceRefresh)
            val jsonText = cachedJson ?: fetchFromApiAndCache() ?: readAnyCachedJson()
            val parsed = jsonText?.let(::parseRecitersJson).orEmpty()
            if (parsed.isNotEmpty()) {
                QuranReciters.registerDynamicReciters(parsed)
            }
            parsed
        }

    private fun readCachedJsonIfFresh(forceRefresh: Boolean): String? {
        if (forceRefresh || !cacheFile.exists()) return null
        val ageMs = System.currentTimeMillis() - cacheFile.lastModified()
        return if (ageMs in 0L..CACHE_TTL_MS) runCatching { cacheFile.readText() }.getOrNull() else null
    }

    private fun readAnyCachedJson(): String? =
        if (cacheFile.exists()) runCatching { cacheFile.readText() }.getOrNull() else null

    private fun fetchFromApiAndCache(): String? {
        val req = Request.Builder()
            .url(MP3QURAN_RECITERS_API)
            .header("Accept", "application/json")
            .build()
        return runCatching {
            QuranWsCdnClient.okHttpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                val body = resp.body?.string()?.takeIf { it.isNotBlank() } ?: return@use null
                val check = JSONObject(body)
                if (!check.has("reciters")) return@use null
                cacheFile.writeText(body)
                body
            }
        }.getOrNull()
    }

    internal fun parseRecitersJson(rawJson: String): List<ReciterOption> {
        val root = runCatching { JSONObject(rawJson) }.getOrNull() ?: return emptyList()
        val arr = root.optJSONArray("reciters") ?: return emptyList()
        val results = ArrayList<ReciterOption>()

        for (i in 0 until arr.length()) {
            val recObj = arr.optJSONObject(i) ?: continue
            val reciterName = recObj.optString("name").trim()
            if (reciterName.isEmpty()) continue
            val moshafs = recObj.optJSONArray("moshaf") ?: continue

            for (m in 0 until moshafs.length()) {
                val mObj = moshafs.optJSONObject(m) ?: continue
                val moshafId = mObj.optInt("id", 0)
                val moshafName = mObj.optString("name").trim()
                val server = mObj.optString("server").trim()
                val surahTotal = mObj.optInt("surah_total", 0)
                if (moshafId <= 0 || !server.startsWith("https://") || surahTotal < 20) continue

                val edition = detectRiwayah(moshafName) ?: continue
                val cleanServer = if (server.endsWith("/")) server else "$server/"
                val category = classificationFor(edition, moshafName)
                val bio = "تلاوة متقنة بالسورة الكاملة برواية ${edition.titleAr} • متوفر $surahTotal سورة."

                results.add(
                    ReciterOption(
                        id = "mp3q_$moshafId",
                        nameAr = reciterName,
                        styleAr = "${edition.titleAr} • سورة كاملة",
                        everyAyahFolder = "",
                        photoUrl = fallbackIconicPhotoFor(edition),
                        riwayah = edition,
                        categoryAr = category,
                        bioAr = bio,
                        isSurahLevelAudio = true,
                        directBaseUrl = cleanServer,
                        preferMonogramAvatar = true
                    )
                )
            }
        }

        val order = mapOf(
            RiwayahEdition.QALON to 0,
            RiwayahEdition.DOURI to 1,
            RiwayahEdition.SHUBAH to 2,
            RiwayahEdition.WARSH to 3
        )
        return results
            .sortedWith(compareBy<ReciterOption> { order[it.riwayah] ?: 9 }.thenBy { it.nameAr })
            .groupBy { it.riwayah }
            .flatMap { (_, list) -> list.take(MAX_PER_RIWAYAH) }
    }

    private fun detectRiwayah(moshafName: String): RiwayahEdition? = when {
        moshafName.contains("قالون") -> RiwayahEdition.QALON
        moshafName.contains("الدوري") -> RiwayahEdition.DOURI
        moshafName.contains("شعبة") -> RiwayahEdition.SHUBAH
        moshafName.contains("ورش") -> RiwayahEdition.WARSH
        else -> null
    }

    private fun classificationFor(edition: RiwayahEdition, moshafName: String): String = when (edition) {
        RiwayahEdition.QALON -> "قرّاء رواية قالون"
        RiwayahEdition.DOURI -> if (moshafName.contains("الكسائي")) "الدوري عن الكسائي" else "قرّاء رواية الدوري"
        RiwayahEdition.SHUBAH -> "قرّاء رواية شعبة"
        RiwayahEdition.WARSH -> if (moshafName.contains("الأصبهاني")) "ورش • طريق الأصبهاني" else "قرّاء رواية ورش"
        RiwayahEdition.HAFS -> "رواية حفص"
    }

    private fun fallbackIconicPhotoFor(edition: RiwayahEdition): String = when (edition) {
        RiwayahEdition.QALON, RiwayahEdition.DOURI -> "https://commons.wikimedia.org/wiki/Special:FilePath/Hussary.jpg?width=480"
        RiwayahEdition.WARSH -> "https://commons.wikimedia.org/wiki/Special:FilePath/Abdul_Basit_Abd_us-Samad.jpg?width=480"
        RiwayahEdition.SHUBAH, RiwayahEdition.HAFS -> "https://commons.wikimedia.org/wiki/Special:FilePath/Huthaify.jpg?width=480"
    }

    companion object {
        private const val MP3QURAN_RECITERS_API = "https://www.mp3quran.net/api/v3/reciters?language=ar"
        private const val CACHE_TTL_MS = 7L * 24L * 60L * 60L * 1000L
        private const val MAX_PER_RIWAYAH = 12
    }
}
