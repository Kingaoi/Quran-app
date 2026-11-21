package com.example.data.local

import com.example.domain.wird.ALL_WEEK_DAYS_MASK
import com.example.domain.wird.NamedWirdPlanConfig
import com.example.domain.wird.WirdAmountUnit
import com.example.domain.wird.WirdDirection
import com.example.domain.wird.WirdDurationUnit
import com.example.domain.wird.WirdGoalType
import com.example.domain.wird.WirdPlanAudience
import com.example.domain.wird.WirdPlanType
import com.example.domain.wird.WirdSessionSlot
import com.example.domain.wird.WirdSessionTimingMode
import com.example.domain.wird.defaultWirdSessions
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

internal object WirdCustomPlansCodec {
    fun decode(raw: String?): List<NamedWirdPlanConfig> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { decodeArray(JSONArray(raw)) }.getOrDefault(emptyList())
    }

    fun decodeArray(arr: JSONArray): List<NamedWirdPlanConfig> = buildList {
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val id = o.optString("id").takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
            val title = o.optString("title").trim().ifBlank { continue }
            val iconKey = o.optString("iconKey", "book").ifBlank { "book" }
            val sessionsArr = o.optJSONArray("sessions")
            val decodedSessions = if (sessionsArr != null && sessionsArr.length() > 0) {
                (0 until sessionsArr.length()).mapNotNull { idx ->
                    val s = sessionsArr.optJSONObject(idx) ?: return@mapNotNull null
                    WirdSessionSlot(
                        index = s.optInt("index", idx),
                        startHour = s.optInt("startHour", 6).coerceIn(0, 23),
                        startMinute = s.optInt("startMinute", 0).coerceIn(0, 59),
                        endHour = s.optInt("endHour", 7).coerceIn(0, 23),
                        endMinute = s.optInt("endMinute", 0).coerceIn(0, 59)
                    )
                }
            } else {
                defaultWirdSessions()
            }
            add(
                NamedWirdPlanConfig(
                    id = id,
                    title = title,
                    startJuz = o.optInt("startJuz", 1).coerceIn(1, 30),
                    quartersPerDay = o.optInt("quartersPerDay", 8).coerceIn(1, 240),
                    completedWirdsCount = o.optInt("completedWirds", 0).coerceAtLeast(0),
                    startEpochDay = o.optLong("startEpochDay", WirdPreferencesRepository.currentLocalEpochDay()),
                    lastProgressPage = o.optInt("lastProgressPage", 0).coerceIn(0, 604),
                    iconKey = iconKey,
                    isV2Plan = o.optBoolean("isV2Plan", false),
                    planType = WirdPlanType.fromKey(o.optString("planType", iconKey)),
                    audience = WirdPlanAudience.fromKey(o.optString("audience")),
                    allowFollowUp = o.optBoolean("allowFollowUp", false),
                    direction = WirdDirection.fromKey(o.optString("direction")),
                    fromSurah = o.optInt("fromSurah", 1).coerceIn(1, 114),
                    fromAyah = o.optInt("fromAyah", 1).coerceAtLeast(1),
                    toSurah = o.optInt("toSurah", 114).coerceIn(1, 114),
                    toAyah = o.optInt("toAyah", 6).coerceAtLeast(1),
                    isPeriodicWird = o.optBoolean("isPeriodicWird", false),
                    activeDaysMask = o.optInt("activeDaysMask", ALL_WEEK_DAYS_MASK).let { if (it == 0) ALL_WEEK_DAYS_MASK else it },
                    goalType = WirdGoalType.fromKey(o.optString("goalType")),
                    amountValue = o.optDouble("amountValue", 1.0).toFloat().coerceAtLeast(0.1f),
                    amountUnit = WirdAmountUnit.fromKey(o.optString("amountUnit")),
                    rabtAmount = o.optDouble("rabtAmount", 0.0).toFloat().coerceAtLeast(0f),
                    reviewAmountValue = o.optDouble("reviewAmountValue", 1.0).toFloat().coerceAtLeast(0.1f),
                    reviewAmountUnit = WirdAmountUnit.fromKey(o.optString("reviewAmountUnit")),
                    targetDurationDays = o.optInt("targetDurationDays", 30).coerceIn(1, 1825),
                    durationUnit = WirdDurationUnit.fromKey(o.optString("durationUnit")),
                    endEpochDay = o.optLong("endEpochDay", 0L),
                    hasExplicitEndDate = o.optBoolean("hasExplicitEndDate", false),
                    sessionsPerDay = o.optInt("sessionsPerDay", 1).coerceIn(1, 5),
                    sessionTimingMode = WirdSessionTimingMode.fromKey(o.optString("sessionTimingMode")),
                    sessions = decodedSessions,
                    reminderEnabled = o.optBoolean("reminderEnabled", false),
                    reminderLeadMinutes = o.optInt("reminderLeadMinutes", 0).coerceAtLeast(0),
                    completedPortionDays = WirdTimelineCodec.decodeIntLongMap(o.optJSONObject("completedPortionDays")),
                    portionDayOverrides = WirdTimelineCodec.decodeIntLongMap(o.optJSONObject("portionDayOverrides"))
                )
            )
        }
    }

    fun encode(plans: List<NamedWirdPlanConfig>): String = encodeArray(plans).toString()

    fun encodeArray(plans: List<NamedWirdPlanConfig>): JSONArray = JSONArray().apply {
        plans.forEach { p ->
            put(JSONObject().apply {
                put("id", p.id); put("title", p.title); put("startJuz", p.startJuz)
                put("quartersPerDay", p.quartersPerDay); put("completedWirds", p.completedWirdsCount)
                put("startEpochDay", p.startEpochDay); put("lastProgressPage", p.lastProgressPage)
                put("iconKey", p.iconKey); put("isV2Plan", p.isV2Plan); put("planType", p.planType.key)
                put("audience", p.audience.key); put("allowFollowUp", p.allowFollowUp)
                put("direction", p.direction.key); put("fromSurah", p.fromSurah); put("fromAyah", p.fromAyah)
                put("toSurah", p.toSurah); put("toAyah", p.toAyah); put("isPeriodicWird", p.isPeriodicWird)
                put("activeDaysMask", p.activeDaysMask); put("goalType", p.goalType.key)
                put("amountValue", p.amountValue.toDouble()); put("amountUnit", p.amountUnit.key)
                put("rabtAmount", p.rabtAmount.toDouble()); put("reviewAmountValue", p.reviewAmountValue.toDouble())
                put("reviewAmountUnit", p.reviewAmountUnit.key); put("targetDurationDays", p.targetDurationDays)
                put("durationUnit", p.durationUnit.key); put("endEpochDay", p.endEpochDay)
                put("hasExplicitEndDate", p.hasExplicitEndDate); put("sessionsPerDay", p.sessionsPerDay)
                put("sessionTimingMode", p.sessionTimingMode.key)
                put("sessions", JSONArray().apply {
                    p.sessions.forEach { s ->
                        put(JSONObject().apply {
                            put("index", s.index); put("startHour", s.startHour); put("startMinute", s.startMinute)
                            put("endHour", s.endHour); put("endMinute", s.endMinute)
                        })
                    }
                })
                put("reminderEnabled", p.reminderEnabled); put("reminderLeadMinutes", p.reminderLeadMinutes)
                if (p.completedPortionDays.isNotEmpty()) {
                    put("completedPortionDays", WirdTimelineCodec.encodeIntLongMap(p.completedPortionDays))
                }
                if (p.portionDayOverrides.isNotEmpty()) {
                    put("portionDayOverrides", WirdTimelineCodec.encodeIntLongMap(p.portionDayOverrides))
                }
            })
        }
    }
}
