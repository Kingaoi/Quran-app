package com.example.data.adhkar

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.data.prayer.ExactAlarmCompat
import java.util.Calendar

class AdhkarReminderScheduler(private val context: Context) {

    fun syncReminders(config: AdhkarReminderConfig) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        AdhkarNotificationReceiver.ensureChannel(context)

        syncSlot(
            alarmManager = alarmManager,
            slot = SLOT_MORNING,
            enabled = config.morningEnabled,
            hour = config.morningHour,
            minute = config.morningMinute
        )
        syncSlot(
            alarmManager = alarmManager,
            slot = SLOT_EVENING,
            enabled = config.eveningEnabled,
            hour = config.eveningHour,
            minute = config.eveningMinute
        )
    }

    fun scheduleNextSlot(slot: String, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        syncSlot(alarmManager, slot, enabled = true, hour = hour, minute = minute)
    }

    private fun syncSlot(
        alarmManager: AlarmManager,
        slot: String,
        enabled: Boolean,
        hour: Int,
        minute: Int
    ) {
        val pending = buildPendingIntent(slot, hour, minute)
        if (!enabled) {
            alarmManager.cancel(pending)
            return
        }
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

    private fun buildPendingIntent(slot: String, hour: Int, minute: Int): PendingIntent {
        val requestCode = if (slot == SLOT_MORNING) 6101 else 6102
        val intent = Intent(context, AdhkarNotificationReceiver::class.java).apply {
            action = "com.example.ADHKAR_REMINDER_$slot"
            putExtra(AdhkarNotificationReceiver.EXTRA_SLOT, slot)
            putExtra(AdhkarNotificationReceiver.EXTRA_HOUR, hour)
            putExtra(AdhkarNotificationReceiver.EXTRA_MINUTE, minute)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val SLOT_MORNING = "morning"
        const val SLOT_EVENING = "evening"
    }
}
