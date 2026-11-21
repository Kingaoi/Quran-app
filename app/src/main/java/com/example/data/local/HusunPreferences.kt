package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.wird.HusunConfig
import com.example.domain.wird.HusunFortressType
import com.example.domain.wird.HusunNewRate
import com.example.domain.wird.WirdDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.husunDataStore by preferencesDataStore(name = "husun_memorization_prefs")

class HusunPreferencesRepository(private val context: Context) {
    private object Keys {
        val IS_ACTIVE = booleanPreferencesKey("husun_is_active")
        val DIRECTION = stringPreferencesKey("husun_direction")
        val STARTING_PAGES = intPreferencesKey("husun_starting_memorized_pages")
        val NEW_RATE = stringPreferencesKey("husun_new_rate")
        val CONTINUOUS_PAGES = intPreferencesKey("husun_continuous_pages")
        val FAR_DIVISIONS = intPreferencesKey("husun_far_divisions")
        val COMPLETED_UNITS = intPreferencesKey("husun_completed_new_units")
        val CONTINUOUS_DAY = intPreferencesKey("husun_continuous_cursor_day")
        val LAST_ACTIVE_DAY = longPreferencesKey("husun_last_active_epoch_day")
        val TODAY_MASK = intPreferencesKey("husun_today_fortress_mask")
        val TODAY_REPEATS = intPreferencesKey("husun_today_new_repeats")
        val START_EPOCH_DAY = longPreferencesKey("husun_start_epoch_day")
    }

    val configFlow: Flow<HusunConfig> = context.husunDataStore.data.map { prefs ->
        HusunConfig(
            isActive = prefs[Keys.IS_ACTIVE] ?: false,
            direction = WirdDirection.fromKey(prefs[Keys.DIRECTION]),
            startingMemorizedPages = (prefs[Keys.STARTING_PAGES] ?: 0).coerceIn(0, 603),
            dailyNewRate = HusunNewRate.fromKey(prefs[Keys.NEW_RATE]),
            continuousPagesPerDay = (prefs[Keys.CONTINUOUS_PAGES] ?: 40).coerceIn(10, 80),
            farReviewDivisions = (prefs[Keys.FAR_DIVISIONS] ?: 7).coerceIn(2, 30),
            completedNewUnits = (prefs[Keys.COMPLETED_UNITS] ?: 0).coerceAtLeast(0),
            continuousCursorDay = (prefs[Keys.CONTINUOUS_DAY] ?: 0).coerceAtLeast(0),
            lastActiveEpochDay = prefs[Keys.LAST_ACTIVE_DAY] ?: 0L,
            todayFortressMask = prefs[Keys.TODAY_MASK] ?: 0,
            todayNewRepeatCount = (prefs[Keys.TODAY_REPEATS] ?: 0).coerceAtLeast(0),
            startEpochDay = prefs[Keys.START_EPOCH_DAY] ?: 0L
        )
    }

    suspend fun configureAndStart(
        direction: WirdDirection,
        startingMemorizedPages: Int,
        dailyNewRate: HusunNewRate,
        continuousPagesPerDay: Int,
        farReviewDivisions: Int,
        todayEpochDay: Long
    ) {
        context.husunDataStore.edit { prefs ->
            val wasActive = prefs[Keys.IS_ACTIVE] ?: false
            prefs[Keys.IS_ACTIVE] = true
            prefs[Keys.DIRECTION] = direction.key
            prefs[Keys.STARTING_PAGES] = startingMemorizedPages.coerceIn(0, 603)
            prefs[Keys.NEW_RATE] = dailyNewRate.key
            prefs[Keys.CONTINUOUS_PAGES] = continuousPagesPerDay.coerceIn(10, 80)
            prefs[Keys.FAR_DIVISIONS] = farReviewDivisions.coerceIn(2, 30)
            if (!wasActive) {
                prefs[Keys.COMPLETED_UNITS] = 0
                prefs[Keys.CONTINUOUS_DAY] = 0
                prefs[Keys.LAST_ACTIVE_DAY] = todayEpochDay
                prefs[Keys.TODAY_MASK] = 0
                prefs[Keys.TODAY_REPEATS] = 0
                prefs[Keys.START_EPOCH_DAY] = todayEpochDay
            }
        }
    }

    suspend fun toggleFortressCompleted(fortress: HusunFortressType, todayEpochDay: Long) {
        context.husunDataStore.edit { prefs ->
            val prevDay = prefs[Keys.LAST_ACTIVE_DAY] ?: 0L
            val curMask = if (prevDay == todayEpochDay) (prefs[Keys.TODAY_MASK] ?: 0) else 0
            val curRepeats = if (prevDay == todayEpochDay) (prefs[Keys.TODAY_REPEATS] ?: 0) else 0
            prefs[Keys.LAST_ACTIVE_DAY] = todayEpochDay
            prefs[Keys.TODAY_MASK] = curMask xor fortress.bitMask
            prefs[Keys.TODAY_REPEATS] = curRepeats
        }
    }

    suspend fun incrementTodayRepeat(todayEpochDay: Long) {
        context.husunDataStore.edit { prefs ->
            val prevDay = prefs[Keys.LAST_ACTIVE_DAY] ?: 0L
            val curMask = if (prevDay == todayEpochDay) (prefs[Keys.TODAY_MASK] ?: 0) else 0
            val curRepeats = if (prevDay == todayEpochDay) (prefs[Keys.TODAY_REPEATS] ?: 0) else 0
            prefs[Keys.LAST_ACTIVE_DAY] = todayEpochDay
            prefs[Keys.TODAY_MASK] = curMask
            prefs[Keys.TODAY_REPEATS] = (curRepeats + 1).coerceAtMost(999)
        }
    }

    suspend fun advanceToNextDay(todayEpochDay: Long) {
        context.husunDataStore.edit { prefs ->
            val rate = HusunNewRate.fromKey(prefs[Keys.NEW_RATE])
            val prevUnits = (prefs[Keys.COMPLETED_UNITS] ?: 0).coerceAtLeast(0)
            val prevContDay = (prefs[Keys.CONTINUOUS_DAY] ?: 0).coerceAtLeast(0)
            prefs[Keys.COMPLETED_UNITS] = prevUnits + rate.halfPageUnits
            prefs[Keys.CONTINUOUS_DAY] = prevContDay + 1
            prefs[Keys.LAST_ACTIVE_DAY] = todayEpochDay
            prefs[Keys.TODAY_MASK] = 0
            prefs[Keys.TODAY_REPEATS] = 0
        }
    }

    suspend fun deactivateAndReset() {
        context.husunDataStore.edit { prefs ->
            prefs[Keys.IS_ACTIVE] = false
            prefs[Keys.COMPLETED_UNITS] = 0
            prefs[Keys.CONTINUOUS_DAY] = 0
            prefs[Keys.TODAY_MASK] = 0
            prefs[Keys.TODAY_REPEATS] = 0
        }
    }

    suspend fun exportJson(): JSONObject {
        val c = configFlow.first()
        return JSONObject().apply {
            put("isActive", c.isActive)
            put("direction", c.direction.key)
            put("startingMemorizedPages", c.startingMemorizedPages)
            put("dailyNewRate", c.dailyNewRate.key)
            put("continuousPagesPerDay", c.continuousPagesPerDay)
            put("farReviewDivisions", c.farReviewDivisions)
            put("completedNewUnits", c.completedNewUnits)
            put("continuousCursorDay", c.continuousCursorDay)
            put("lastActiveEpochDay", c.lastActiveEpochDay)
            put("todayFortressMask", c.todayFortressMask)
            put("todayNewRepeatCount", c.todayNewRepeatCount)
            put("startEpochDay", c.startEpochDay)
        }
    }

    suspend fun importJson(obj: JSONObject) {
        context.husunDataStore.edit { prefs ->
            prefs[Keys.IS_ACTIVE] = obj.optBoolean("isActive", false)
            prefs[Keys.DIRECTION] = WirdDirection.fromKey(obj.optString("direction")).key
            prefs[Keys.STARTING_PAGES] = obj.optInt("startingMemorizedPages", 0).coerceIn(0, 603)
            prefs[Keys.NEW_RATE] = HusunNewRate.fromKey(obj.optString("dailyNewRate")).key
            prefs[Keys.CONTINUOUS_PAGES] = obj.optInt("continuousPagesPerDay", 40).coerceIn(10, 80)
            prefs[Keys.FAR_DIVISIONS] = obj.optInt("farReviewDivisions", 7).coerceIn(2, 30)
            prefs[Keys.COMPLETED_UNITS] = obj.optInt("completedNewUnits", 0).coerceAtLeast(0)
            prefs[Keys.CONTINUOUS_DAY] = obj.optInt("continuousCursorDay", 0).coerceAtLeast(0)
            prefs[Keys.LAST_ACTIVE_DAY] = obj.optLong("lastActiveEpochDay", 0L)
            prefs[Keys.TODAY_MASK] = obj.optInt("todayFortressMask", 0)
            prefs[Keys.TODAY_REPEATS] = obj.optInt("todayNewRepeatCount", 0).coerceAtLeast(0)
            prefs[Keys.START_EPOCH_DAY] = obj.optLong("startEpochDay", 0L)
        }
    }
}
