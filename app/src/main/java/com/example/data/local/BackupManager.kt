package com.example.data.local

import android.content.Context
import com.example.data.adhkar.AdhkarPreferencesRepository
import com.example.data.audio.AudioPlaylistRepository
import org.json.JSONObject

class BackupManager(context: Context) {
    private val readerPrefs = ReaderPreferencesRepository(context)
    private val prayerPrefs = PrayerPreferencesRepository(context)
    private val wirdPrefs = WirdPreferencesRepository(context)
    private val bookmarksRepo = BookmarksRepository(context)
    private val statsRepo = ReadingStatsRepository(context)
    private val adhkarPrefs = AdhkarPreferencesRepository(context)
    private val husunPrefs = HusunPreferencesRepository(context)
    private val playlistsRepo = AudioPlaylistRepository(context)

    suspend fun exportAllJson(): String {
        val root = JSONObject().apply {
            put("version", 1)
            put("exportedAtMillis", System.currentTimeMillis())
            put("reader", readerPrefs.exportJson())
            put("prayer", prayerPrefs.exportJson())
            put("wird", wirdPrefs.exportJson())
            put("husun", husunPrefs.exportJson())
            put("bookmarks", bookmarksRepo.exportJson())
            put("stats", statsRepo.exportJson())
            put("adhkar", adhkarPrefs.exportJson())
            put("playlists", playlistsRepo.exportJson())
        }
        return root.toString(2)
    }

    suspend fun importAllJson(rawJson: String): Result<Unit> = runCatching {
        val root = JSONObject(rawJson)
        root.optJSONObject("reader")?.let { readerPrefs.importJson(it) }
        root.optJSONObject("prayer")?.let { prayerPrefs.importJson(it) }
        root.optJSONObject("wird")?.let { wirdPrefs.importJson(it) }
        root.optJSONObject("husun")?.let { husunPrefs.importJson(it) }
        val bookmarksRaw = root.optString("bookmarks", "")
        if (bookmarksRaw.isNotBlank()) bookmarksRepo.importJson(bookmarksRaw)
        root.optJSONObject("stats")?.let { statsRepo.importJson(it) }
        root.optJSONObject("adhkar")?.let { adhkarPrefs.importJson(it) }
        root.optJSONObject("playlists")?.let { playlistsRepo.importJson(it) }
        Unit
    }
}
