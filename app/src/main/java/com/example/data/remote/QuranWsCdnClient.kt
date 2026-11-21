package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object QuranWsCdnClient {
    private const val DEFAULT_SVG_VERSION = "v1.1.1"

    @Volatile
    private var resolvedVersion: String? = null

    val okHttpClient: OkHttpClient by lazy {
        val dispatcher = Dispatcher().apply {
            maxRequests = 24
            maxRequestsPerHost = 10
        }
        OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .build()
    }

    suspend fun getSvgVersion(): String = withContext(Dispatchers.IO) {
        resolvedVersion?.let { return@withContext it }
        runCatching {
            val req = Request.Builder()
                .url("https://cdn.quran.ws/svg/pages/latest.json")
                .build()
            okHttpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string().orEmpty()
                    val json = JSONObject(body)
                    val ver = json.optString("version")
                        .ifBlank { json.optString("latest") }
                        .ifBlank { json.optString("tag") }
                    if (ver.isNotBlank()) {
                        resolvedVersion = ver
                        return@withContext ver
                    }
                }
            }
        }
        DEFAULT_SVG_VERSION
    }

    fun buildSvgPageUrl(version: String, editionSlug: String, page: Int): String {
        val padded = page.coerceIn(1, 604).toString().padStart(3, '0')
        return "https://cdn.quran.ws/svg/pages/$version/$editionSlug/$padded.svg"
    }

    fun buildRawGithubSvgUrl(editionSlug: String, page: Int): String {
        val padded = page.coerceIn(1, 604).toString().padStart(3, '0')
        return "https://raw.githubusercontent.com/quran-ws/quran-svg/main/mushafs/$editionSlug/svg/$padded.svg"
    }
}
