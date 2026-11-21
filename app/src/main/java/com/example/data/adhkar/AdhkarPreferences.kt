package com.example.data.adhkar

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.DhikrCategory
import com.example.domain.model.DhikrItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.adhkarReminderDataStore by preferencesDataStore(name = "adhkar_reminder_prefs")

@Immutable
data class AdhkarReminderConfig(
    val morningEnabled: Boolean = false,
    val morningHour: Int = 6,
    val morningMinute: Int = 0,
    val eveningEnabled: Boolean = false,
    val eveningHour: Int = 16,
    val eveningMinute: Int = 30
)

class AdhkarPreferencesRepository(private val context: Context) {
    private object Keys {
        val MORNING_ENABLED = booleanPreferencesKey("adhkar_morning_enabled")
        val MORNING_HOUR = intPreferencesKey("adhkar_morning_hour")
        val MORNING_MINUTE = intPreferencesKey("adhkar_morning_minute")
        val EVENING_ENABLED = booleanPreferencesKey("adhkar_evening_enabled")
        val EVENING_HOUR = intPreferencesKey("adhkar_evening_hour")
        val EVENING_MINUTE = intPreferencesKey("adhkar_evening_minute")
        val FAVORITE_IDS = stringSetPreferencesKey("adhkar_favorite_ids")
        val CUSTOM_DUAS_JSON = stringPreferencesKey("adhkar_custom_duas_json")
    }

    val configFlow: Flow<AdhkarReminderConfig> = context.adhkarReminderDataStore.data.map { prefs ->
        AdhkarReminderConfig(
            morningEnabled = prefs[Keys.MORNING_ENABLED] ?: false,
            morningHour = (prefs[Keys.MORNING_HOUR] ?: 6).coerceIn(0, 23),
            morningMinute = (prefs[Keys.MORNING_MINUTE] ?: 0).coerceIn(0, 59),
            eveningEnabled = prefs[Keys.EVENING_ENABLED] ?: false,
            eveningHour = (prefs[Keys.EVENING_HOUR] ?: 16).coerceIn(0, 23),
            eveningMinute = (prefs[Keys.EVENING_MINUTE] ?: 30).coerceIn(0, 59)
        )
    }

    val favoriteIdsFlow: Flow<Set<String>> = context.adhkarReminderDataStore.data.map { prefs ->
        prefs[Keys.FAVORITE_IDS] ?: emptySet()
    }

    val customDuasFlow: Flow<List<DhikrItem>> = context.adhkarReminderDataStore.data.map { prefs ->
        decodeCustomDuas(prefs[Keys.CUSTOM_DUAS_JSON].orEmpty())
    }

    suspend fun setMorningReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.adhkarReminderDataStore.edit { prefs ->
            prefs[Keys.MORNING_ENABLED] = enabled
            prefs[Keys.MORNING_HOUR] = hour.coerceIn(0, 23)
            prefs[Keys.MORNING_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun setEveningReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.adhkarReminderDataStore.edit { prefs ->
            prefs[Keys.EVENING_ENABLED] = enabled
            prefs[Keys.EVENING_HOUR] = hour.coerceIn(0, 23)
            prefs[Keys.EVENING_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun toggleFavorite(id: String) {
        context.adhkarReminderDataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_IDS]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(id)) current.remove(id)
            prefs[Keys.FAVORITE_IDS] = current
        }
    }

    suspend fun addCustomDua(title: String, arabicText: String, targetCount: Int, note: String) {
        val cleanText = arabicText.trim()
        if (cleanText.isEmpty()) return
        val item = DhikrItem(
            id = "custom_${UUID.randomUUID().toString().take(8)}",
            category = DhikrCategory.PROPHETIC_DUAS,
            title = title.trim().ifBlank { "دعاء خاص" },
            arabicText = cleanText,
            virtueOrNote = note.trim().ifBlank { "دعاء أضفته إلى مفضلتك الخاصة" },
            targetCount = targetCount.coerceIn(1, 100),
            reference = "أدعيتي الخاصة",
            isCustom = true
        )
        context.adhkarReminderDataStore.edit { prefs ->
            val list = decodeCustomDuas(prefs[Keys.CUSTOM_DUAS_JSON].orEmpty())
            prefs[Keys.CUSTOM_DUAS_JSON] = encodeCustomDuas(listOf(item) + list)
        }
    }

    suspend fun deleteCustomDua(id: String) {
        context.adhkarReminderDataStore.edit { prefs ->
            val list = decodeCustomDuas(prefs[Keys.CUSTOM_DUAS_JSON].orEmpty()).filterNot { it.id == id }
            prefs[Keys.CUSTOM_DUAS_JSON] = encodeCustomDuas(list)
            prefs[Keys.FAVORITE_IDS] = (prefs[Keys.FAVORITE_IDS] ?: emptySet()) - id
        }
    }

    suspend fun exportJson(): JSONObject {
        val c = configFlow.first()
        val favs = favoriteIdsFlow.first()
        val custom = customDuasFlow.first()
        return JSONObject().apply {
            put("morningEnabled", c.morningEnabled)
            put("morningHour", c.morningHour)
            put("morningMinute", c.morningMinute)
            put("eveningEnabled", c.eveningEnabled)
            put("eveningHour", c.eveningHour)
            put("eveningMinute", c.eveningMinute)
            put("favoriteIds", JSONArray().apply { favs.forEach(::put) })
            put("customDuas", encodeCustomDuas(custom))
        }
    }

    suspend fun importJson(obj: JSONObject) {
        val favArr = obj.optJSONArray("favoriteIds")
        val favSet = buildSet {
            if (favArr != null) {
                for (i in 0 until favArr.length()) favArr.optString(i).takeIf { it.isNotBlank() }?.let(::add)
            }
        }
        context.adhkarReminderDataStore.edit { prefs ->
            prefs[Keys.MORNING_ENABLED] = obj.optBoolean("morningEnabled", false)
            prefs[Keys.MORNING_HOUR] = obj.optInt("morningHour", 6).coerceIn(0, 23)
            prefs[Keys.MORNING_MINUTE] = obj.optInt("morningMinute", 0).coerceIn(0, 59)
            prefs[Keys.EVENING_ENABLED] = obj.optBoolean("eveningEnabled", false)
            prefs[Keys.EVENING_HOUR] = obj.optInt("eveningHour", 16).coerceIn(0, 23)
            prefs[Keys.EVENING_MINUTE] = obj.optInt("eveningMinute", 30).coerceIn(0, 59)
            prefs[Keys.FAVORITE_IDS] = favSet
            obj.optString("customDuas", "").takeIf { it.isNotBlank() }?.let { prefs[Keys.CUSTOM_DUAS_JSON] = it }
        }
    }

    private fun encodeCustomDuas(list: List<DhikrItem>): String {
        val arr = JSONArray()
        for (item in list) {
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("arabicText", item.arabicText)
                    put("virtueOrNote", item.virtueOrNote)
                    put("targetCount", item.targetCount)
                }
            )
        }
        return arr.toString()
    }

    private fun decodeCustomDuas(raw: String): List<DhikrItem> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList(arr.length()) {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val id = o.optString("id")
                    val text = o.optString("arabicText")
                    if (id.isNotBlank() && text.isNotBlank()) {
                        add(
                            DhikrItem(
                                id = id,
                                category = DhikrCategory.PROPHETIC_DUAS,
                                title = o.optString("title", "دعاء خاص"),
                                arabicText = text,
                                virtueOrNote = o.optString("virtueOrNote", "دعاء أضفته إلى مفضلتك الخاصة"),
                                targetCount = o.optInt("targetCount", 1).coerceIn(1, 100),
                                reference = "أدعيتي الخاصة",
                                isCustom = true
                            )
                        )
                    }
                }
            }
        }.getOrDefault(emptyList())
    }
}
