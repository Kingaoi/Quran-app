package com.example.data.prayer

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import com.example.data.remote.QuranWsCdnClient
import com.example.domain.prayer.AdhanVoice
import com.example.domain.prayer.AdhanVoices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File

class AdhanAudioStorage(private val context: Context) {

    private val rootDir: File
        get() = File(context.filesDir, "adhan_voices").apply { if (!exists()) mkdirs() }

    fun getLocalFile(voiceId: String): File? {
        val file = File(rootDir, "$voiceId.mp3")
        return file.takeIf { it.exists() && it.length() > 4096L }
    }

    fun isCached(voiceId: String): Boolean = getLocalFile(voiceId) != null

    fun cachedVoiceIds(): Set<String> =
        AdhanVoices.all.mapNotNull { voice -> voice.id.takeIf { isCached(it) } }.toSet()

    fun getTotalStorageBytes(): Long = runCatching {
        rootDir.listFiles()?.sumOf { f ->
            if (f.isFile && f.name.endsWith(".mp3") && f.length() > 4096L) f.length() else 0L
        } ?: 0L
    }.getOrDefault(0L)

    fun buildMediaItem(voice: AdhanVoice): MediaItem {
        val mediaUri = getLocalFile(voice.id)?.let { Uri.fromFile(it).toString() } ?: voice.audioUrl
        val builder = MediaItem.Builder().setUri(mediaUri)
        voice.maxDurationMs?.takeIf { it > 0L }?.let { maxMs ->
            builder.setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(0L)
                    .setEndPositionMs(maxMs)
                    .build()
            )
        }
        return builder.build()
    }

    suspend fun downloadVoice(voice: AdhanVoice): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val target = File(rootDir, "${voice.id}.mp3")
            if (target.exists() && target.length() > 4096L) return@runCatching target
            val temp = File(rootDir, "${voice.id}.mp3.tmp")
            if (temp.exists()) temp.delete()

            val request = Request.Builder().url(voice.audioUrl).build()
            QuranWsCdnClient.okHttpClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    error("HTTP ${resp.code}")
                }
                val body = resp.body ?: error("Empty response")
                body.byteStream().use { input ->
                    temp.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            if (!temp.exists() || temp.length() <= 4096L) {
                temp.delete()
                error("Invalid audio file")
            }
            if (target.exists()) target.delete()
            if (!temp.renameTo(target)) {
                temp.copyTo(target, overwrite = true)
                temp.delete()
            }
            target
        }
    }

    fun deleteCachedVoice(voiceId: String): Boolean {
        val file = File(rootDir, "$voiceId.mp3")
        return !file.exists() || file.delete()
    }
}
