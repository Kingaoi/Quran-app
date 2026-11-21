package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

private val Context.searchHistoryDataStore by preferencesDataStore(name = "quran_index_search_history")

class IndexSearchHistoryRepository(private val context: Context) {
    private val queriesKey = stringPreferencesKey("recent_queries_json")

    val recentSearchesFlow: Flow<List<String>> = context.searchHistoryDataStore.data.map { prefs ->
        decodeQueries(prefs[queriesKey].orEmpty())
    }

    suspend fun recordQuery(rawQuery: String) {
        val cleaned = rawQuery.trim()
        if (cleaned.isEmpty()) return
        if (cleaned.length < 2 && cleaned.toIntOrNull() == null) return
        context.searchHistoryDataStore.edit { prefs ->
            val current = decodeQueries(prefs[queriesKey].orEmpty())
                .filterNot { it.equals(cleaned, ignoreCase = true) }
            val updated = (listOf(cleaned) + current).take(MAX_RECENT_QUERIES)
            prefs[queriesKey] = encodeQueries(updated)
        }
    }

    suspend fun removeQuery(query: String) {
        context.searchHistoryDataStore.edit { prefs ->
            val updated = decodeQueries(prefs[queriesKey].orEmpty())
                .filterNot { it == query }
            prefs[queriesKey] = encodeQueries(updated)
        }
    }

    suspend fun clearAll() {
        context.searchHistoryDataStore.edit { prefs ->
            prefs.remove(queriesKey)
        }
    }

    companion object {
        const val MAX_RECENT_QUERIES = 8

        internal fun encodeQueries(queries: List<String>): String {
            val array = JSONArray()
            queries.forEach { array.put(it) }
            return array.toString()
        }

        internal fun decodeQueries(raw: String): List<String> {
            if (raw.isBlank()) return emptyList()
            return runCatching {
                val array = JSONArray(raw)
                buildList(array.length()) {
                    for (i in 0 until array.length()) {
                        val value = array.optString(i).trim()
                        if (value.isNotEmpty()) add(value)
                    }
                }.take(MAX_RECENT_QUERIES)
            }.getOrDefault(emptyList())
        }
    }
}
