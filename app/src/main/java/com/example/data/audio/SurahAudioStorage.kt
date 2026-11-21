package com.example.data.audio

import android.content.Context
import com.example.data.remote.QuranWsCdnClient
import com.example.domain.model.ReciterOption
import com.example.domain.model.SurahInfo
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
import java.util.concurrent.atomic.AtomicInteger

class SurahAudioStorage(context: Context) {
    private val rootDir = File(context.filesDir, "audio_offline")

    fun reciterDir(reciterId: String): File = File(rootDir, reciterId)

    fun ayahAudioFile(reciterId: String, surah: Int, ayah: Int): File {
        val sss = surah.toString().padStart(3, '0')
        val aaa = ayah.toString().padStart(3, '0')
        return File(reciterDir(reciterId), "$sss$aaa.mp3")
    }

    fun surahAudioFile(reciterId: String, surah: Int): File {
        val sss = surah.toString().padStart(3, '0')
        return File(reciterDir(reciterId), "$sss.mp3")
    }

    fun getLocalSurahFile(reciterId: String, surah: Int): File? {
        val f = surahAudioFile(reciterId, surah)
        return if (f.exists() && f.length() > MIN_MP3_BYTES) f else null
    }

    fun getLocalAyahFile(reciterId: String, surah: Int, ayah: Int): File? {
        val f = ayahAudioFile(reciterId, surah, ayah)
        return if (f.exists() && f.length() > MIN_MP3_BYTES) f else null
    }

    fun countDownloadedAyahsForSurah(reciterId: String, surah: Int, totalAyahs: Int): Int {
        if (getLocalSurahFile(reciterId, surah) != null) return totalAyahs
        var count = 0
        for (a in 1..totalAyahs) {
            if (getLocalAyahFile(reciterId, surah, a) != null) count++
        }
        return count
    }

    fun countDownloadedAyahsForReciter(reciterId: String): Int {
        val dir = reciterDir(reciterId)
        if (!dir.exists() || !dir.isDirectory) return 0
        var count = 0
        dir.listFiles()?.forEach { file ->
            if (file.isFile && file.name.endsWith(".mp3") && file.length() > MIN_MP3_BYTES) {
                count++
            }
        }
        return count.coerceAtMost(TOTAL_QURAN_AYAHS)
    }

    fun getReciterStorageBytes(reciterId: String): Long {
        val dir = reciterDir(reciterId)
        if (!dir.exists() || !dir.isDirectory) return 0L
        var total = 0L
        dir.listFiles()?.forEach { file ->
            if (file.isFile && file.name.endsWith(".mp3") && file.length() > MIN_MP3_BYTES) {
                total += file.length()
            }
        }
        return total
    }

    fun getDownloadedSurahNumbers(reciterId: String, surahs: List<SurahInfo>): Set<Int> {
        val dir = reciterDir(reciterId)
        if (!dir.exists() || !dir.isDirectory) return emptySet()
        val files = dir.listFiles()?.mapNotNull { f ->
            if (f.isFile && f.name.endsWith(".mp3") && f.length() > MIN_MP3_BYTES) f.name else null
        }?.toHashSet() ?: return emptySet()
        return buildSet {
            for (s in surahs) {
                val sss = s.number.toString().padStart(3, '0')
                if ("$sss.mp3" in files) {
                    add(s.number)
                } else if (s.ayahCount > 0) {
                    var allPresent = true
                    for (a in 1..s.ayahCount) {
                        val aaa = a.toString().padStart(3, '0')
                        if ("$sss$aaa.mp3" !in files) {
                            allPresent = false
                            break
                        }
                    }
                    if (allPresent) add(s.number)
                }
            }
        }
    }

    fun countTotalDownloadedFiles(): Int {
        if (!rootDir.exists()) return 0
        return rootDir.walkTopDown().count { it.isFile && it.name.endsWith(".mp3") && it.length() > MIN_MP3_BYTES }
    }

    suspend fun downloadSurah(
        reciter: ReciterOption,
        surah: Int,
        totalAyahs: Int,
        onProgress: (downloaded: Int, total: Int) -> Unit
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = reciterDir(reciter.id)
            if (!dir.exists()) dir.mkdirs()

            if (reciter.isSurahLevelAudio) {
                if (getLocalSurahFile(reciter.id, surah) == null) {
                    val target = surahAudioFile(reciter.id, surah)
                    val ok = downloadFileToTarget(reciter.ayahAudioUrl(surah, 1), target, dir)
                    if (!ok) error("تعذّر تنزيل تلاوة السورة")
                }
                onProgress(totalAyahs, totalAyahs)
                return@runCatching totalAyahs
            }

            var downloaded = 0
            for (ayah in 1..totalAyahs) {
                ensureActive()
                if (getLocalAyahFile(reciter.id, surah, ayah) != null) {
                    downloaded++
                    onProgress(downloaded, totalAyahs)
                    continue
                }
                if (downloadSingleAyahFile(reciter, dir, surah, ayah)) {
                    downloaded++
                }
                onProgress(downloaded, totalAyahs)
            }
            if (downloaded < totalAyahs) {
                error("تعذّر تنزيل كل آيات السورة ($downloaded/$totalAyahs)")
            }
            downloaded
        }
    }

    suspend fun downloadFullReciterAudio(
        reciter: ReciterOption,
        surahs: List<SurahInfo>,
        onProgress: (downloaded: Int, total: Int) -> Unit
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = reciterDir(reciter.id)
            if (!dir.exists()) dir.mkdirs()

            val existingNames = HashSet<String>(TOTAL_QURAN_AYAHS)
            dir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".mp3") && file.length() > MIN_MP3_BYTES) {
                    existingNames.add(file.name)
                }
            }

            val totalCount = surahs.sumOf { it.ayahCount }.coerceAtLeast(TOTAL_QURAN_AYAHS)
            val missingPairs = ArrayList<Pair<Int, Int>>()
            var alreadyPresent = 0
            for (surah in surahs) {
                val sss = surah.number.toString().padStart(3, '0')
                for (ayah in 1..surah.ayahCount) {
                    val aaa = ayah.toString().padStart(3, '0')
                    if ("$sss$aaa.mp3" in existingNames) {
                        alreadyPresent++
                    } else {
                        missingPairs.add(surah.number to ayah)
                    }
                }
            }

            val completed = AtomicInteger(alreadyPresent)
            onProgress(completed.get(), totalCount)
            if (missingPairs.isEmpty()) return@runCatching totalCount

            val semaphore = Semaphore(MAX_CONCURRENT_AUDIO_DOWNLOADS)
            val failedCount = AtomicInteger(0)

            coroutineScope {
                missingPairs.map { (surahNum, ayahNum) ->
                    async {
                        semaphore.withPermit {
                            ensureActive()
                            if (downloadSingleAyahFile(reciter, dir, surahNum, ayahNum)) {
                                val done = completed.incrementAndGet()
                                onProgress(done.coerceAtMost(totalCount), totalCount)
                            } else {
                                failedCount.incrementAndGet()
                            }
                        }
                    }
                }.awaitAll()
            }

            val finalDownloaded = countDownloadedAyahsForReciter(reciter.id)
            if (finalDownloaded < totalCount && failedCount.get() > 0) {
                error("تعذّر تنزيل ${failedCount.get()} آية لقارئ ${reciter.nameAr}")
            }
            finalDownloaded
        }
    }

    private fun downloadSingleAyahFile(
        reciter: ReciterOption,
        dir: File,
        surah: Int,
        ayah: Int
    ): Boolean = downloadFileToTarget(
        url = reciter.ayahAudioUrl(surah, ayah),
        target = ayahAudioFile(reciter.id, surah, ayah),
        dir = dir
    )

    private fun downloadFileToTarget(
        url: String,
        target: File,
        dir: File
    ): Boolean {
        val temp = File(dir, "${target.name}.tmp")
        val req = Request.Builder().url(url).build()
        val ok = runCatching {
            QuranWsCdnClient.okHttpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use false
                val body = resp.body ?: return@use false
                temp.outputStream().use { out -> body.byteStream().copyTo(out) }
                if (temp.length() > MIN_MP3_BYTES) {
                    if (target.exists()) target.delete()
                    temp.renameTo(target)
                } else {
                    temp.delete()
                    false
                }
            }
        }.getOrDefault(false)
        if (temp.exists()) temp.delete()
        return ok
    }

    suspend fun deleteSurah(reciterId: String, surah: Int, totalAyahs: Int) = withContext(Dispatchers.IO) {
        surahAudioFile(reciterId, surah).takeIf { it.exists() }?.delete()
        for (a in 1..totalAyahs) {
            val file = ayahAudioFile(reciterId, surah, a)
            if (file.exists()) file.delete()
        }
    }

    suspend fun deleteReciter(reciterId: String) = withContext(Dispatchers.IO) {
        val dir = reciterDir(reciterId)
        if (dir.exists()) dir.deleteRecursively()
    }

    suspend fun clearAllAudio() = withContext(Dispatchers.IO) {
        if (rootDir.exists()) rootDir.deleteRecursively()
    }

    companion object {
        const val TOTAL_QURAN_AYAHS = 6236
        private const val MIN_MP3_BYTES = 512L
        private const val MAX_CONCURRENT_AUDIO_DOWNLOADS = 4
    }
}
