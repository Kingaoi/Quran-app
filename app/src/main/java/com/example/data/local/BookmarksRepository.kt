package com.example.data.local

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.bookmarksDataStore by preferencesDataStore(name = "quran_bookmarks_store")

enum class BookmarkCategory(val id: String, val labelAr: String) {
    RECITATION("recitation", "تلاوة"),
    MEMORIZATION("memorization", "حفظ"),
    REVISION("revision", "مراجعة"),
    REFLECTION("reflection", "تدبر");

    companion object {
        fun fromId(id: String?): BookmarkCategory =
            entries.firstOrNull { it.id == id } ?: RECITATION
    }
}

@Immutable
data class AyahBookmark(
    val id: String,
    val page: Int,
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahNameAr: String,
    val category: BookmarkCategory = BookmarkCategory.RECITATION,
    val note: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

class BookmarksRepository(private val context: Context) {
    private val bookmarksJsonKey = stringPreferencesKey("bookmarks_json")

    val bookmarksFlow: Flow<List<AyahBookmark>> = context.bookmarksDataStore.data.map { prefs ->
        decodeBookmarks(prefs[bookmarksJsonKey].orEmpty())
    }

    suspend fun upsertBookmark(bookmark: AyahBookmark) {
        context.bookmarksDataStore.edit { prefs ->
            val current = decodeBookmarks(prefs[bookmarksJsonKey].orEmpty()).toMutableList()
            val idx = current.indexOfFirst { it.id == bookmark.id }
            if (idx >= 0) {
                current[idx] = bookmark
            } else {
                current.add(0, bookmark)
            }
            prefs[bookmarksJsonKey] = encodeBookmarks(current)
        }
    }

    suspend fun deleteBookmark(id: String) {
        context.bookmarksDataStore.edit { prefs ->
            val updated = decodeBookmarks(prefs[bookmarksJsonKey].orEmpty()).filterNot { it.id == id }
            prefs[bookmarksJsonKey] = encodeBookmarks(updated)
        }
    }

    suspend fun exportJson(): String {
        return context.bookmarksDataStore.data.first()[bookmarksJsonKey].orEmpty()
    }

    suspend fun importJson(json: String) {
        val parsed = decodeBookmarks(json)
        context.bookmarksDataStore.edit { prefs ->
            prefs[bookmarksJsonKey] = encodeBookmarks(parsed)
        }
    }

    companion object {
        fun buildId(page: Int, surah: Int, ayah: Int): String = "${page}_${surah}_${ayah}"

        fun encodeBookmarks(list: List<AyahBookmark>): String {
            val arr = JSONArray()
            for (b in list) {
                arr.put(
                    JSONObject().apply {
                        put("id", b.id)
                        put("page", b.page)
                        put("surah", b.surahNumber)
                        put("ayah", b.ayahNumber)
                        put("surahName", b.surahNameAr)
                        put("cat", b.category.id)
                        put("note", b.note)
                        put("ts", b.createdAtMillis)
                    }
                )
            }
            return arr.toString()
        }

        fun decodeBookmarks(raw: String): List<AyahBookmark> {
            if (raw.isBlank()) return emptyList()
            return runCatching {
                val arr = JSONArray(raw)
                buildList(arr.length()) {
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        val page = obj.optInt("page", 1).coerceIn(1, 604)
                        val surah = obj.optInt("surah", 1).coerceIn(1, 114)
                        val ayah = obj.optInt("ayah", 1).coerceAtLeast(1)
                        add(
                            AyahBookmark(
                                id = obj.optString("id").ifBlank { buildId(page, surah, ayah) },
                                page = page,
                                surahNumber = surah,
                                ayahNumber = ayah,
                                surahNameAr = obj.optString("surahName", "الفاتحة"),
                                category = BookmarkCategory.fromId(obj.optString("cat")),
                                note = obj.optString("note", ""),
                                createdAtMillis = obj.optLong("ts", 0L)
                            )
                        )
                    }
                }
            }.getOrDefault(emptyList())
        }
    }
}
