package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.wird.KhatmahPlanConfig
import com.example.domain.wird.NamedWirdPlanConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit

private val Context.wirdDataStore by preferencesDataStore(name = "wird_khatmah_prefs")

class WirdPreferencesRepository(private val context: Context) {
    private object Keys {
        val IS_ACTIVE = booleanPreferencesKey("wird_is_active")
        val START_JUZ = intPreferencesKey("wird_start_juz")
        val QUARTERS_PER_DAY = intPreferencesKey("wird_quarters_per_day")
        val COMPLETED_WIRDS = intPreferencesKey("wird_completed_count")
        val START_EPOCH_DAY = longPreferencesKey("wird_start_epoch_day")
        val LAST_PROGRESS_PAGE = intPreferencesKey("wird_last_progress_page")
        val REMINDER_ENABLED = booleanPreferencesKey("wird_reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("wird_reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("wird_reminder_minute")
        val COMPLETED_PORTION_DAYS_JSON = stringPreferencesKey("wird_completed_portion_days_json")
        val PORTION_DAY_OVERRIDES_JSON = stringPreferencesKey("wird_portion_day_overrides_json")
        val CUSTOM_PLANS_JSON = stringPreferencesKey("wird_custom_plans_json")
    }

    val configFlow: Flow<KhatmahPlanConfig> = context.wirdDataStore.data.map { prefs ->
        KhatmahPlanConfig(
            isActive = prefs[Keys.IS_ACTIVE] ?: false,
            startJuz = (prefs[Keys.START_JUZ] ?: 1).coerceIn(1, 30),
            quartersPerDay = (prefs[Keys.QUARTERS_PER_DAY] ?: 8).coerceIn(1, 240),
            completedWirdsCount = (prefs[Keys.COMPLETED_WIRDS] ?: 0).coerceAtLeast(0),
            startEpochDay = prefs[Keys.START_EPOCH_DAY] ?: currentLocalEpochDay(),
            lastProgressPage = (prefs[Keys.LAST_PROGRESS_PAGE] ?: 0).coerceIn(0, 604),
            reminderEnabled = prefs[Keys.REMINDER_ENABLED] ?: false,
            reminderHour = (prefs[Keys.REMINDER_HOUR] ?: 8).coerceIn(0, 23),
            reminderMinute = (prefs[Keys.REMINDER_MINUTE] ?: 0).coerceIn(0, 59),
            completedPortionDays = WirdTimelineCodec.decodeIntLongMap(prefs[Keys.COMPLETED_PORTION_DAYS_JSON]),
            portionDayOverrides = WirdTimelineCodec.decodeIntLongMap(prefs[Keys.PORTION_DAY_OVERRIDES_JSON])
        )
    }

    val customPlansFlow: Flow<List<NamedWirdPlanConfig>> = context.wirdDataStore.data.map { prefs ->
        WirdCustomPlansCodec.decode(prefs[Keys.CUSTOM_PLANS_JSON])
    }

    suspend fun startNewPlan(startJuz: Int, quartersPerDay: Int) {
        val today = currentLocalEpochDay()
        context.wirdDataStore.edit { prefs ->
            prefs[Keys.IS_ACTIVE] = true
            prefs[Keys.START_JUZ] = startJuz.coerceIn(1, 30)
            prefs[Keys.QUARTERS_PER_DAY] = quartersPerDay.coerceIn(1, 240)
            prefs[Keys.COMPLETED_WIRDS] = 0
            prefs[Keys.START_EPOCH_DAY] = today
            prefs[Keys.LAST_PROGRESS_PAGE] = 0
            prefs.remove(Keys.COMPLETED_PORTION_DAYS_JSON)
            prefs.remove(Keys.PORTION_DAY_OVERRIDES_JSON)
        }
    }

    suspend fun setCompletedWirdsCount(count: Int) {
        context.wirdDataStore.edit { prefs ->
            prefs[Keys.COMPLETED_WIRDS] = count.coerceAtLeast(0)
            prefs[Keys.LAST_PROGRESS_PAGE] = 0
            prefs.remove(Keys.COMPLETED_PORTION_DAYS_JSON)
        }
    }

    suspend fun setMainTimelineState(completedDays: Map<Int, Long>? = null, dayOverrides: Map<Int, Long>? = null) {
        context.wirdDataStore.edit { prefs ->
            if (completedDays != null) {
                prefs[Keys.COMPLETED_PORTION_DAYS_JSON] = WirdTimelineCodec.encodeIntLongMapString(completedDays)
                prefs[Keys.COMPLETED_WIRDS] = completedDays.size
                prefs[Keys.LAST_PROGRESS_PAGE] = 0
            }
            if (dayOverrides != null) {
                prefs[Keys.PORTION_DAY_OVERRIDES_JSON] = WirdTimelineCodec.encodeIntLongMapString(dayOverrides)
            }
        }
    }

    suspend fun setLastProgressPage(page: Int) {
        context.wirdDataStore.edit { prefs -> prefs[Keys.LAST_PROGRESS_PAGE] = page.coerceIn(0, 604) }
    }

    suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.wirdDataStore.edit { prefs ->
            prefs[Keys.REMINDER_ENABLED] = enabled
            prefs[Keys.REMINDER_HOUR] = hour.coerceIn(0, 23)
            prefs[Keys.REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun addCustomPlan(planConfig: NamedWirdPlanConfig): String {
        val cleanTitle = planConfig.title.trim().ifBlank { "خطة ورد جديدة" }
        val newId = planConfig.id.ifBlank { UUID.randomUUID().toString() }
        val stored = planConfig.copy(id = newId, title = cleanTitle, iconKey = planConfig.planType.key)
        context.wirdDataStore.edit { prefs ->
            val current = WirdCustomPlansCodec.decode(prefs[Keys.CUSTOM_PLANS_JSON])
            prefs[Keys.CUSTOM_PLANS_JSON] = WirdCustomPlansCodec.encode(current + stored)
        }
        return newId
    }

    suspend fun updateCustomPlan(planConfig: NamedWirdPlanConfig) {
        val cleanTitle = planConfig.title.trim().ifBlank { "خطة ورد جديدة" }
        context.wirdDataStore.edit { prefs ->
            val updated = WirdCustomPlansCodec.decode(prefs[Keys.CUSTOM_PLANS_JSON]).map { existing ->
                if (existing.id == planConfig.id) {
                    planConfig.copy(
                        title = cleanTitle, iconKey = planConfig.planType.key,
                        completedWirdsCount = existing.completedWirdsCount,
                        lastProgressPage = existing.lastProgressPage,
                        completedPortionDays = existing.completedPortionDays,
                        portionDayOverrides = existing.portionDayOverrides
                    )
                } else existing
            }
            prefs[Keys.CUSTOM_PLANS_JSON] = WirdCustomPlansCodec.encode(updated)
        }
    }

    suspend fun addCustomPlan(title: String, startJuz: Int, quartersPerDay: Int, iconKey: String = "book"): String {
        val cleanTitle = title.trim().ifBlank { "خطة ورد إضافية" }
        val newId = UUID.randomUUID().toString()
        val plan = NamedWirdPlanConfig(
            id = newId, title = cleanTitle, startJuz = startJuz.coerceIn(1, 30),
            quartersPerDay = quartersPerDay.coerceIn(1, 240), completedWirdsCount = 0,
            startEpochDay = currentLocalEpochDay(), lastProgressPage = 0, iconKey = iconKey, isV2Plan = false
        )
        context.wirdDataStore.edit { prefs ->
            val current = WirdCustomPlansCodec.decode(prefs[Keys.CUSTOM_PLANS_JSON])
            prefs[Keys.CUSTOM_PLANS_JSON] = WirdCustomPlansCodec.encode(current + plan)
        }
        return newId
    }

    suspend fun setCustomPlanCompletedCount(planId: String, count: Int) {
        context.wirdDataStore.edit { prefs ->
            val updated = WirdCustomPlansCodec.decode(prefs[Keys.CUSTOM_PLANS_JSON]).map { plan ->
                if (plan.id == planId) {
                    plan.copy(completedWirdsCount = count.coerceAtLeast(0), lastProgressPage = 0, completedPortionDays = emptyMap())
                } else plan
            }
            prefs[Keys.CUSTOM_PLANS_JSON] = WirdCustomPlansCodec.encode(updated)
        }
    }

    suspend fun setCustomPlanTimelineState(
        planId: String,
        completedPortionDays: Map<Int, Long>? = null,
        portionDayOverrides: Map<Int, Long>? = null
    ) {
        context.wirdDataStore.edit { prefs ->
            val updated = WirdCustomPlansCodec.decode(prefs[Keys.CUSTOM_PLANS_JSON]).map { plan ->
                if (plan.id == planId) {
                    plan.copy(
                        completedPortionDays = completedPortionDays ?: plan.completedPortionDays,
                        completedWirdsCount = completedPortionDays?.size ?: plan.completedWirdsCount,
                        portionDayOverrides = portionDayOverrides ?: plan.portionDayOverrides,
                        lastProgressPage = if (completedPortionDays != null) 0 else plan.lastProgressPage
                    )
                } else plan
            }
            prefs[Keys.CUSTOM_PLANS_JSON] = WirdCustomPlansCodec.encode(updated)
        }
    }

    suspend fun setCustomPlanLastProgressPage(planId: String, page: Int) {
        context.wirdDataStore.edit { prefs ->
            val updated = WirdCustomPlansCodec.decode(prefs[Keys.CUSTOM_PLANS_JSON]).map { plan ->
                if (plan.id == planId) plan.copy(lastProgressPage = page.coerceIn(0, 604)) else plan
            }
            prefs[Keys.CUSTOM_PLANS_JSON] = WirdCustomPlansCodec.encode(updated)
        }
    }

    suspend fun setCustomPlanReminderEnabled(planId: String, enabled: Boolean) {
        context.wirdDataStore.edit { prefs ->
            val updated = WirdCustomPlansCodec.decode(prefs[Keys.CUSTOM_PLANS_JSON]).map { plan ->
                if (plan.id == planId) plan.copy(reminderEnabled = enabled) else plan
            }
            prefs[Keys.CUSTOM_PLANS_JSON] = WirdCustomPlansCodec.encode(updated)
        }
    }

    suspend fun deleteCustomPlan(planId: String) {
        context.wirdDataStore.edit { prefs ->
            val updated = WirdCustomPlansCodec.decode(prefs[Keys.CUSTOM_PLANS_JSON]).filterNot { it.id == planId }
            prefs[Keys.CUSTOM_PLANS_JSON] = WirdCustomPlansCodec.encode(updated)
        }
    }

    suspend fun exportJson(): JSONObject {
        val c = configFlow.first()
        val custom = customPlansFlow.first()
        return JSONObject().apply {
            put("isActive", c.isActive); put("startJuz", c.startJuz); put("quartersPerDay", c.quartersPerDay)
            put("completedWirds", c.completedWirdsCount); put("startEpochDay", c.startEpochDay)
            put("lastProgressPage", c.lastProgressPage); put("reminderEnabled", c.reminderEnabled)
            put("reminderHour", c.reminderHour); put("reminderMinute", c.reminderMinute)
            if (c.completedPortionDays.isNotEmpty()) put("completedPortionDays", WirdTimelineCodec.encodeIntLongMap(c.completedPortionDays))
            if (c.portionDayOverrides.isNotEmpty()) put("portionDayOverrides", WirdTimelineCodec.encodeIntLongMap(c.portionDayOverrides))
            put("customPlans", WirdCustomPlansCodec.encodeArray(custom))
        }
    }

    suspend fun importJson(obj: JSONObject) {
        context.wirdDataStore.edit { prefs ->
            prefs[Keys.IS_ACTIVE] = obj.optBoolean("isActive", false)
            prefs[Keys.START_JUZ] = obj.optInt("startJuz", 1).coerceIn(1, 30)
            prefs[Keys.QUARTERS_PER_DAY] = obj.optInt("quartersPerDay", 8).coerceIn(1, 240)
            prefs[Keys.COMPLETED_WIRDS] = obj.optInt("completedWirds", 0).coerceAtLeast(0)
            prefs[Keys.START_EPOCH_DAY] = obj.optLong("startEpochDay", currentLocalEpochDay())
            prefs[Keys.LAST_PROGRESS_PAGE] = obj.optInt("lastProgressPage", 0).coerceIn(0, 604)
            prefs[Keys.REMINDER_ENABLED] = obj.optBoolean("reminderEnabled", false)
            prefs[Keys.REMINDER_HOUR] = obj.optInt("reminderHour", 8).coerceIn(0, 23)
            prefs[Keys.REMINDER_MINUTE] = obj.optInt("reminderMinute", 0).coerceIn(0, 59)
            prefs[Keys.COMPLETED_PORTION_DAYS_JSON] =
                WirdTimelineCodec.encodeIntLongMapString(WirdTimelineCodec.decodeIntLongMap(obj.optJSONObject("completedPortionDays")))
            prefs[Keys.PORTION_DAY_OVERRIDES_JSON] =
                WirdTimelineCodec.encodeIntLongMapString(WirdTimelineCodec.decodeIntLongMap(obj.optJSONObject("portionDayOverrides")))
            obj.optJSONArray("customPlans")?.let { arr ->
                prefs[Keys.CUSTOM_PLANS_JSON] = WirdCustomPlansCodec.encode(WirdCustomPlansCodec.decodeArray(arr))
            }
        }
    }

    companion object {
        fun currentLocalEpochDay(nowMillis: Long = System.currentTimeMillis()): Long {
            val cal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            return TimeUnit.MILLISECONDS.toDays(cal.timeInMillis + cal.timeZone.getOffset(cal.timeInMillis))
        }
    }
}
