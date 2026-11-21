package com.example.data.local

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.statsDataStore by preferencesDataStore(name = "quran_reading_stats")

@Immutable
data class ReadingStats(
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val lastReadEpochDay: Long = 0L,
    val totalPagesRead: Int = 0,
    val completedKhatmahsCount: Int = 0,
    val recentDailyPages: Map<Long, Int> = emptyMap(),
    val tasbihTotalCount: Long = 0L,
    val tasbihTodayCount: Int = 0,
    val tasbihTodayEpochDay: Long = 0L,
    val customTasbihPhrases: List<String> = emptyList(),
    val adhkarTotalCount: Int = 0,
    val adhkarTodayCount: Int = 0,
    val recentDailyAdhkar: Map<Long, Int> = emptyMap(),
    val recentDailyTasbih: Map<Long, Int> = emptyMap(),
    val dailyGoalPages: Int = 4,
    val rawatibCompletedIdsToday: Set<String> = emptySet(),
    val rawatibTodayEpochDay: Long = 0L
)

class ReadingStatsRepository(private val context: Context) {
    private object Keys {
        val CURRENT_STREAK = intPreferencesKey("streak_current")
        val LONGEST_STREAK = intPreferencesKey("streak_longest")
        val LAST_READ_DAY = longPreferencesKey("last_read_epoch_day")
        val TOTAL_PAGES_READ = intPreferencesKey("total_pages_read")
        val COMPLETED_KHATMAHS = intPreferencesKey("completed_khatmahs_count")
        val DAILY_PAGES_JSON = stringPreferencesKey("daily_pages_json")
        val TASBIH_TOTAL = longPreferencesKey("tasbih_total_count")
        val TASBIH_TODAY = intPreferencesKey("tasbih_today_count")
        val TASBIH_TODAY_DAY = longPreferencesKey("tasbih_today_day")
        val CUSTOM_TASBIH_JSON = stringPreferencesKey("custom_tasbih_phrases_json")
        val ADHKAR_TOTAL = intPreferencesKey("adhkar_total_count")
        val DAILY_ADHKAR_JSON = stringPreferencesKey("daily_adhkar_json")
        val DAILY_TASBIH_JSON = stringPreferencesKey("daily_tasbih_json")
        val DAILY_GOAL_PAGES = intPreferencesKey("daily_goal_pages")
        val RAWATIB_TODAY_JSON = stringPreferencesKey("rawatib_today_json")
        val RAWATIB_TODAY_DAY = longPreferencesKey("rawatib_today_day")
    }

    val statsFlow: Flow<ReadingStats> = context.statsDataStore.data.map(::mapPrefs)

    private fun mapPrefs(prefs: Preferences): ReadingStats {
        val today = WirdPreferencesRepository.currentLocalEpochDay()
        val storedTasbihDay = prefs[Keys.TASBIH_TODAY_DAY] ?: today
        val storedRawatibDay = prefs[Keys.RAWATIB_TODAY_DAY] ?: today
        val dailyAdhkar = decodeDailyPages(prefs[Keys.DAILY_ADHKAR_JSON].orEmpty())
        val dailyTasbih = decodeDailyPages(prefs[Keys.DAILY_TASBIH_JSON].orEmpty())
        val resolvedTasbihToday = dailyTasbih[today] ?: if (storedTasbihDay == today) (prefs[Keys.TASBIH_TODAY] ?: 0) else 0
        return ReadingStats(
            currentStreakDays = prefs[Keys.CURRENT_STREAK] ?: 0,
            longestStreakDays = prefs[Keys.LONGEST_STREAK] ?: 0,
            lastReadEpochDay = prefs[Keys.LAST_READ_DAY] ?: 0L,
            totalPagesRead = prefs[Keys.TOTAL_PAGES_READ] ?: 0,
            completedKhatmahsCount = prefs[Keys.COMPLETED_KHATMAHS] ?: 0,
            recentDailyPages = decodeDailyPages(prefs[Keys.DAILY_PAGES_JSON].orEmpty()),
            tasbihTotalCount = prefs[Keys.TASBIH_TOTAL] ?: 0L,
            tasbihTodayCount = resolvedTasbihToday,
            tasbihTodayEpochDay = storedTasbihDay,
            customTasbihPhrases = decodeStringList(prefs[Keys.CUSTOM_TASBIH_JSON].orEmpty()),
            adhkarTotalCount = prefs[Keys.ADHKAR_TOTAL] ?: 0,
            adhkarTodayCount = dailyAdhkar[today] ?: 0,
            recentDailyAdhkar = dailyAdhkar,
            recentDailyTasbih = dailyTasbih,
            dailyGoalPages = (prefs[Keys.DAILY_GOAL_PAGES] ?: 4).coerceIn(1, 100),
            rawatibCompletedIdsToday = if (storedRawatibDay == today) {
                decodeStringList(prefs[Keys.RAWATIB_TODAY_JSON].orEmpty()).toSet()
            } else {
                emptySet()
            },
            rawatibTodayEpochDay = storedRawatibDay
        )
    }

    suspend fun recordPageSettled(epochDay: Long = WirdPreferencesRepository.currentLocalEpochDay()) {
        context.statsDataStore.edit { prefs ->
            val lastDay = prefs[Keys.LAST_READ_DAY] ?: 0L
            val curStreak = prefs[Keys.CURRENT_STREAK] ?: 0
            val nextStreak = when {
                lastDay == epochDay -> curStreak.coerceAtLeast(1)
                lastDay == epochDay - 1L -> curStreak + 1
                else -> 1
            }
            val longest = maxOf(prefs[Keys.LONGEST_STREAK] ?: 0, nextStreak)
            val daily = decodeDailyPages(prefs[Keys.DAILY_PAGES_JSON].orEmpty()).toMutableMap()
            daily[epochDay] = (daily[epochDay] ?: 0) + 1
            val trimmed = daily.entries.sortedByDescending { it.key }.take(90).associate { it.key to it.value }

            prefs[Keys.LAST_READ_DAY] = epochDay
            prefs[Keys.CURRENT_STREAK] = nextStreak
            prefs[Keys.LONGEST_STREAK] = longest
            prefs[Keys.TOTAL_PAGES_READ] = (prefs[Keys.TOTAL_PAGES_READ] ?: 0) + 1
            prefs[Keys.DAILY_PAGES_JSON] = encodeDailyPages(trimmed)
        }
    }

    suspend fun recordKhatmahCompleted() {
        context.statsDataStore.edit { prefs ->
            prefs[Keys.COMPLETED_KHATMAHS] = (prefs[Keys.COMPLETED_KHATMAHS] ?: 0) + 1
        }
    }

    suspend fun incrementTasbih(epochDay: Long = WirdPreferencesRepository.currentLocalEpochDay()) {
        context.statsDataStore.edit { prefs ->
            val storedDay = prefs[Keys.TASBIH_TODAY_DAY] ?: 0L
            val currentToday = if (storedDay == epochDay) (prefs[Keys.TASBIH_TODAY] ?: 0) else 0
            val nextToday = currentToday + 1
            val daily = decodeDailyPages(prefs[Keys.DAILY_TASBIH_JSON].orEmpty()).toMutableMap()
            daily[epochDay] = maxOf(daily[epochDay] ?: 0, currentToday) + 1
            val trimmed = daily.entries.sortedByDescending { it.key }.take(90).associate { it.key to it.value }
            prefs[Keys.TASBIH_TOTAL] = (prefs[Keys.TASBIH_TOTAL] ?: 0L) + 1L
            prefs[Keys.TASBIH_TODAY] = nextToday
            prefs[Keys.TASBIH_TODAY_DAY] = epochDay
            prefs[Keys.DAILY_TASBIH_JSON] = encodeDailyPages(trimmed)
        }
    }

    suspend fun recordAdhkarCompleted(epochDay: Long = WirdPreferencesRepository.currentLocalEpochDay()) {
        context.statsDataStore.edit { prefs ->
            val daily = decodeDailyPages(prefs[Keys.DAILY_ADHKAR_JSON].orEmpty()).toMutableMap()
            daily[epochDay] = (daily[epochDay] ?: 0) + 1
            val trimmed = daily.entries.sortedByDescending { it.key }.take(90).associate { it.key to it.value }
            prefs[Keys.ADHKAR_TOTAL] = (prefs[Keys.ADHKAR_TOTAL] ?: 0) + 1
            prefs[Keys.DAILY_ADHKAR_JSON] = encodeDailyPages(trimmed)
        }
    }

    suspend fun addCustomTasbihPhrase(phrase: String) {
        val clean = phrase.trim()
        if (clean.isEmpty()) return
        context.statsDataStore.edit { prefs ->
            val list = decodeStringList(prefs[Keys.CUSTOM_TASBIH_JSON].orEmpty()).toMutableList()
            if (clean !in list) list.add(clean)
            prefs[Keys.CUSTOM_TASBIH_JSON] = encodeStringList(list)
        }
    }

    suspend fun removeCustomTasbihPhrase(phrase: String) {
        context.statsDataStore.edit { prefs ->
            val list = decodeStringList(prefs[Keys.CUSTOM_TASBIH_JSON].orEmpty()).filterNot { it == phrase }
            prefs[Keys.CUSTOM_TASBIH_JSON] = encodeStringList(list)
        }
    }

    suspend fun setDailyGoalPages(pages: Int) {
        context.statsDataStore.edit { prefs -> prefs[Keys.DAILY_GOAL_PAGES] = pages.coerceIn(1, 100) }
    }

    suspend fun toggleRawatibItem(itemId: String, epochDay: Long = WirdPreferencesRepository.currentLocalEpochDay()) {
        context.statsDataStore.edit { prefs ->
            val storedDay = prefs[Keys.RAWATIB_TODAY_DAY] ?: 0L
            val current = if (storedDay == epochDay) decodeStringList(prefs[Keys.RAWATIB_TODAY_JSON].orEmpty()).toMutableSet() else mutableSetOf()
            if (itemId in current) current.remove(itemId) else current.add(itemId)
            prefs[Keys.RAWATIB_TODAY_DAY] = epochDay
            prefs[Keys.RAWATIB_TODAY_JSON] = encodeStringList(current.toList())
        }
    }

    suspend fun exportJson(): JSONObject {
        val stats = mapPrefs(context.statsDataStore.data.first())
        return JSONObject().apply {
            put("streak", stats.currentStreakDays)
            put("longest", stats.longestStreakDays)
            put("lastDay", stats.lastReadEpochDay)
            put("totalPages", stats.totalPagesRead)
            put("khatmahs", stats.completedKhatmahsCount)
            put("dailyPages", encodeDailyPages(stats.recentDailyPages))
            put("tasbihTotal", stats.tasbihTotalCount)
            put("customTasbih", encodeStringList(stats.customTasbihPhrases))
            put("adhkarTotal", stats.adhkarTotalCount)
            put("dailyAdhkar", encodeDailyPages(stats.recentDailyAdhkar))
            put("dailyTasbih", encodeDailyPages(stats.recentDailyTasbih))
            put("dailyGoalPages", stats.dailyGoalPages)
            put("rawatibToday", encodeStringList(stats.rawatibCompletedIdsToday.toList()))
            put("rawatibDay", stats.rawatibTodayEpochDay)
        }
    }

    suspend fun importJson(obj: JSONObject) {
        context.statsDataStore.edit { prefs ->
            prefs[Keys.CURRENT_STREAK] = obj.optInt("streak", 0)
            prefs[Keys.LONGEST_STREAK] = obj.optInt("longest", 0)
            prefs[Keys.LAST_READ_DAY] = obj.optLong("lastDay", 0L)
            prefs[Keys.TOTAL_PAGES_READ] = obj.optInt("totalPages", 0)
            prefs[Keys.COMPLETED_KHATMAHS] = obj.optInt("khatmahs", 0)
            prefs[Keys.DAILY_PAGES_JSON] = obj.optString("dailyPages", "")
            prefs[Keys.TASBIH_TOTAL] = obj.optLong("tasbihTotal", 0L)
            prefs[Keys.CUSTOM_TASBIH_JSON] = obj.optString("customTasbih", "")
            prefs[Keys.ADHKAR_TOTAL] = obj.optInt("adhkarTotal", 0)
            prefs[Keys.DAILY_ADHKAR_JSON] = obj.optString("dailyAdhkar", "")
            prefs[Keys.DAILY_TASBIH_JSON] = obj.optString("dailyTasbih", "")
            prefs[Keys.DAILY_GOAL_PAGES] = obj.optInt("dailyGoalPages", 4).coerceIn(1, 100)
            if (obj.has("rawatibToday")) {
                prefs[Keys.RAWATIB_TODAY_JSON] = obj.optString("rawatibToday", "")
                prefs[Keys.RAWATIB_TODAY_DAY] = obj.optLong("rawatibDay", 0L)
            }
        }
    }

    private fun encodeDailyPages(map: Map<Long, Int>): String {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k.toString(), v) }
        return obj.toString()
    }

    private fun decodeDailyPages(raw: String): Map<Long, Int> {
        if (raw.isBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(raw)
            buildMap {
                obj.keys().forEach { key ->
                    key.toLongOrNull()?.let { day -> put(day, obj.optInt(key, 0)) }
                }
            }
        }.getOrDefault(emptyMap())
    }

    private fun encodeStringList(list: List<String>): String =
        JSONArray().apply { list.forEach(::put) }.toString()

    private fun decodeStringList(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { arr.optString(it) }.filter { it.isNotBlank() }
        }.getOrDefault(emptyList())
    }
}
