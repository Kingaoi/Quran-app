package com.example.data.wird

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.domain.model.toArabicIndicDigits
import com.example.domain.wird.ALL_WEEK_DAYS_MASK
import com.example.ui.navigation.AppLaunchDestination

class WirdNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val hour = intent.getIntExtra(EXTRA_HOUR, 8)
        val minute = intent.getIntExtra(EXTRA_MINUTE, 0)
        val customPlanId = intent.getStringExtra(EXTRA_CUSTOM_PLAN_ID).orEmpty()

        ensureChannel(context)
        if (customPlanId.isNotBlank()) {
            val planTitle = intent.getStringExtra(EXTRA_CUSTOM_PLAN_TITLE).orEmpty().ifBlank { "خطة الورد" }
            val sessionIdx = intent.getIntExtra(EXTRA_SESSION_INDEX, 0)
            val mask = intent.getIntExtra(EXTRA_ACTIVE_DAYS_MASK, ALL_WEEK_DAYS_MASK)
            val reqCode = intent.getIntExtra(EXTRA_REQUEST_CODE, NOTIFICATION_ID)
            val summary = intent.getStringExtra(EXTRA_SESSION_SUMMARY).orEmpty()
            WirdReminderScheduler(context).scheduleNextCustomSession(
                requestCode = reqCode,
                planId = customPlanId,
                planTitle = planTitle,
                sessionIndex = sessionIdx,
                hour = hour,
                minute = minute,
                activeDaysMask = mask,
                sessionSummary = summary
            )
            val sessionLine = "حان موعد الجلسة ${(sessionIdx + 1).toArabicIndicDigits()} من وردك اليومي."
            val fullBody = if (summary.isNotBlank()) "$sessionLine\n$summary" else sessionLine
            postNotification(
                context = context,
                notificationId = reqCode,
                title = "تذكير خطة: $planTitle",
                body = fullBody,
                customPlanId = customPlanId
            )
        } else {
            WirdReminderScheduler(context).scheduleNextDaily(hour, minute)
            postNotification(
                context = context,
                notificationId = NOTIFICATION_ID,
                title = "موعد وردك القرآني اليومي",
                body = "افتح المصحف وأتمم ورد اليوم المبارك من كتاب الله.",
                customPlanId = null
            )
        }
    }

    private fun postNotification(
        context: Context,
        notificationId: Int,
        title: String,
        body: String,
        customPlanId: String?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (!customPlanId.isNullOrBlank()) {
                action = AppLaunchDestination.ACTION_OPEN_CUSTOM_PLAN
                putExtra(AppLaunchDestination.EXTRA_CUSTOM_PLAN_ID, customPlanId)
                putExtra(EXTRA_OPEN_WIRD_TAB, true)
            } else {
                action = AppLaunchDestination.ACTION_QUICK_WIRD
                putExtra(EXTRA_OPEN_WIRD_TAB, true)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (!customPlanId.isNullOrBlank()) {
            val readNowIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                action = AppLaunchDestination.ACTION_OPEN_CUSTOM_PLAN
                putExtra(AppLaunchDestination.EXTRA_CUSTOM_PLAN_ID, customPlanId)
                putExtra(AppLaunchDestination.EXTRA_OPEN_PLAN_READER, true)
            }
            val readPending = PendingIntent.getActivity(
                context,
                notificationId + 100_000,
                readNowIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "قراءة الورد الآن", readPending)
        }

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    companion object {
        const val CHANNEL_ID = "quran_ws_wird_alerts"
        const val NOTIFICATION_ID = 5042
        const val EXTRA_HOUR = "extra_wird_hour"
        const val EXTRA_MINUTE = "extra_wird_minute"
        const val EXTRA_OPEN_WIRD_TAB = "extra_open_wird_tab"
        const val EXTRA_CUSTOM_PLAN_ID = "extra_custom_plan_id"
        const val EXTRA_CUSTOM_PLAN_TITLE = "extra_custom_plan_title"
        const val EXTRA_SESSION_INDEX = "extra_session_index"
        const val EXTRA_ACTIVE_DAYS_MASK = "extra_active_days_mask"
        const val EXTRA_REQUEST_CODE = "extra_request_code"
        const val EXTRA_SESSION_SUMMARY = "extra_session_summary"

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "تذكير الورد اليومي",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "تنبيهات يومية لقراءة الورد القرآني"
                }
                manager.createNotificationChannel(channel)
            }
        }
    }
}
