package com.example.data.wird

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.data.prayer.ExactAlarmCompat
import com.example.domain.wird.ALL_WEEK_DAYS_MASK
import com.example.domain.wird.KhatmahPlanConfig
import com.example.domain.wird.NamedWirdPlanConfig
import com.example.domain.wird.NamedWirdPlanItem
import com.example.domain.wird.WirdSessionTimingMode
import com.example.domain.wird.isDayActiveInMask
import java.util.Calendar
import kotlin.math.abs

class WirdReminderScheduler(private val context: Context) {

    fun syncReminder(config: KhatmahPlanConfig) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        WirdNotificationReceiver.ensureChannel(context)

        val pending = buildPendingIntent(config.reminderHour, config.reminderMinute)
        if (!config.reminderEnabled || !config.isActive) {
            alarmManager.cancel(pending)
            return
        }
        scheduleNextDaily(config.reminderHour, config.reminderMinute)
    }

    fun syncCustomPlanReminders(plans: List<NamedWirdPlanConfig>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        WirdNotificationReceiver.ensureChannel(context)
        plans.forEach { plan ->
            cancelCustomPlanReminders(plan.id)
            if (plan.reminderEnabled) {
                scheduleCustomPlanAlarms(alarmManager, plan, "")
            }
        }
    }

    fun syncCustomPlanItems(items: List<NamedWirdPlanItem>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        WirdNotificationReceiver.ensureChannel(context)
        items.forEach { item ->
            cancelCustomPlanReminders(item.config.id)
            if (item.config.reminderEnabled && !item.summary.isCompleted) {
                val cur = item.summary.currentWird
                val portionSummary = cur?.let {
                    "من ${it.startSurahNameAr} (${it.startAyahNumber}) إلى ${it.endSurahNameAr} (${it.endAyahNumber})"
                }.orEmpty()
                scheduleCustomPlanAlarms(alarmManager, item.config, portionSummary)
            }
        }
    }

    fun cancelCustomPlanReminders(planId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        for (slotIdx in 0 until 5) {
            val reqCode = customRequestCode(planId, slotIdx)
            val pending = buildCustomPendingIntent(
                requestCode = reqCode,
                planId = planId,
                planTitle = "",
                sessionIndex = slotIdx,
                hour = 7,
                minute = 0,
                activeDaysMask = ALL_WEEK_DAYS_MASK,
                sessionSummary = ""
            )
            alarmManager.cancel(pending)
        }
    }

    private fun scheduleCustomPlanAlarms(
        alarmManager: AlarmManager,
        plan: NamedWirdPlanConfig,
        sessionSummary: String
    ) {
        val mask = if (plan.activeDaysMask == 0) ALL_WEEK_DAYS_MASK else plan.activeDaysMask
        if (plan.sessionTimingMode == WirdSessionTimingMode.UNSPECIFIED) {
            val reqCode = customRequestCode(plan.id, 0)
            scheduleNextForMask(
                alarmManager = alarmManager,
                requestCode = reqCode,
                planId = plan.id,
                planTitle = plan.title,
                sessionIndex = 0,
                hour = 7,
                minute = 0,
                activeDaysMask = mask,
                sessionSummary = sessionSummary
            )
        } else {
            val count = plan.sessionsPerDay.coerceIn(1, 5)
            plan.sessions.take(count).forEachIndexed { idx, slot ->
                val rawMin = slot.startHour * 60 + slot.startMinute - plan.reminderLeadMinutes
                val wrapped = ((rawMin % 1440) + 1440) % 1440
                scheduleNextForMask(
                    alarmManager = alarmManager,
                    requestCode = customRequestCode(plan.id, idx),
                    planId = plan.id,
                    planTitle = plan.title,
                    sessionIndex = idx,
                    hour = wrapped / 60,
                    minute = wrapped % 60,
                    activeDaysMask = mask,
                    sessionSummary = sessionSummary
                )
            }
        }
    }

    fun scheduleNextCustomSession(
        requestCode: Int,
        planId: String,
        planTitle: String,
        sessionIndex: Int,
        hour: Int,
        minute: Int,
        activeDaysMask: Int,
        sessionSummary: String = ""
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        scheduleNextForMask(alarmManager, requestCode, planId, planTitle, sessionIndex, hour, minute, activeDaysMask, sessionSummary)
    }

    private fun scheduleNextForMask(
        alarmManager: AlarmManager,
        requestCode: Int,
        planId: String,
        planTitle: String,
        sessionIndex: Int,
        hour: Int,
        minute: Int,
        activeDaysMask: Int,
        sessionSummary: String
    ) {
        val mask = if (activeDaysMask == 0) ALL_WEEK_DAYS_MASK else activeDaysMask
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now + 5_000L) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
            var guard = 0
            while (!isDayActiveInMask(mask, get(Calendar.DAY_OF_WEEK)) && guard < 8) {
                add(Calendar.DAY_OF_YEAR, 1)
                guard++
            }
        }
        val pending = buildCustomPendingIntent(requestCode, planId, planTitle, sessionIndex, hour, minute, mask, sessionSummary)
        ExactAlarmCompat.scheduleExactOrAllowIdle(context, alarmManager, cal.timeInMillis, pending)
    }

    fun scheduleNextDaily(hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pending = buildPendingIntent(hour, minute)

        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now + 5_000L) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        ExactAlarmCompat.scheduleExactOrAllowIdle(context, alarmManager, cal.timeInMillis, pending)
    }

    private fun buildPendingIntent(hour: Int, minute: Int): PendingIntent {
        val intent = Intent(context, WirdNotificationReceiver::class.java).apply {
            action = "com.example.WIRD_DAILY_REMINDER"
            putExtra(WirdNotificationReceiver.EXTRA_HOUR, hour)
            putExtra(WirdNotificationReceiver.EXTRA_MINUTE, minute)
        }
        return PendingIntent.getBroadcast(
            context,
            5100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildCustomPendingIntent(
        requestCode: Int,
        planId: String,
        planTitle: String,
        sessionIndex: Int,
        hour: Int,
        minute: Int,
        activeDaysMask: Int,
        sessionSummary: String
    ): PendingIntent {
        val intent = Intent(context, WirdNotificationReceiver::class.java).apply {
            action = "com.example.WIRD_CUSTOM_PLAN_REMINDER"
            putExtra(WirdNotificationReceiver.EXTRA_CUSTOM_PLAN_ID, planId)
            putExtra(WirdNotificationReceiver.EXTRA_CUSTOM_PLAN_TITLE, planTitle)
            putExtra(WirdNotificationReceiver.EXTRA_SESSION_INDEX, sessionIndex)
            putExtra(WirdNotificationReceiver.EXTRA_HOUR, hour)
            putExtra(WirdNotificationReceiver.EXTRA_MINUTE, minute)
            putExtra(WirdNotificationReceiver.EXTRA_ACTIVE_DAYS_MASK, activeDaysMask)
            putExtra(WirdNotificationReceiver.EXTRA_REQUEST_CODE, requestCode)
            putExtra(WirdNotificationReceiver.EXTRA_SESSION_SUMMARY, sessionSummary)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun customRequestCode(planId: String, sessionIndex: Int): Int {
        val base = abs(planId.hashCode() % 100_000)
        return 20_000 + base * 10 + sessionIndex
    }
}
