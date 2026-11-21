package com.example.data.prayer

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.adhkar.AdhkarPreferencesRepository
import com.example.data.adhkar.AdhkarReminderScheduler
import com.example.data.local.PrayerPreferencesRepository
import com.example.data.local.WirdPreferencesRepository
import com.example.data.wird.WirdReminderScheduler
import com.example.data.widget.QuranSummaryWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppAlarmRescheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action !in SUPPORTED_ACTIONS) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prayerSettings = PrayerPreferencesRepository(context).settingsFlow.first()
                PrayerAlarmScheduler(context).syncAlarms(prayerSettings)
                HijriSunnahScheduler(context).syncReminders(prayerSettings)

                val wirdRepo = WirdPreferencesRepository(context)
                val wirdConfig = wirdRepo.configFlow.first()
                val customPlans = wirdRepo.customPlansFlow.first()
                WirdReminderScheduler(context).apply {
                    syncReminder(wirdConfig)
                    syncCustomPlanReminders(customPlans)
                }

                val adhkarConfig = AdhkarPreferencesRepository(context).configFlow.first()
                AdhkarReminderScheduler(context).syncReminders(adhkarConfig)

                QuranSummaryWidgetProvider.notifyWidgetDataChanged(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private val SUPPORTED_ACTIONS = buildSet {
            add(Intent.ACTION_BOOT_COMPLETED)
            add(Intent.ACTION_MY_PACKAGE_REPLACED)
            add(Intent.ACTION_TIMEZONE_CHANGED)
            add(Intent.ACTION_TIME_CHANGED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)
            }
        }
    }
}
