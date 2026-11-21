package com.example.data.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.domain.model.AppDigitConfig
import com.example.domain.model.localizeDigits
import com.example.domain.prayer.AsrJuristicMethod
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.PrayerName
import com.example.domain.prayer.PrayerTimesCalculator
import com.example.domain.prayer.PresetCities
import java.util.Calendar
import java.util.TimeZone

class PrayerTimesWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        private const val PREFS_NAME = "quran_widget_snapshot"
        private const val KEY_CITY_NAME = "city_name"
        private const val KEY_CITY_TZ = "city_tz"
        private const val KEY_LAT = "city_lat"
        private const val KEY_LON = "city_lon"
        private const val KEY_METHOD = "method"
        private const val KEY_ASR = "asr"
        private const val KEY_USE_24H = "use_24h"
        private const val KEY_WESTERN_DIGITS = "western_digits"
        private const val KEY_HIJRI_OFFSET = "hijri_offset_days"

        fun notifyWidgetDataChanged(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val component = ComponentName(context, PrayerTimesWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            for (id in ids) updateWidget(context, manager, id)
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val defaultCity = PresetCities.defaultCity
            val cityName = prefs.getString(KEY_CITY_NAME, defaultCity.nameAr) ?: defaultCity.nameAr
            val lat = if (prefs.contains(KEY_LAT)) java.lang.Double.longBitsToDouble(prefs.getLong(KEY_LAT, 0L)) else defaultCity.latitude
            val lon = if (prefs.contains(KEY_LON)) java.lang.Double.longBitsToDouble(prefs.getLong(KEY_LON, 0L)) else defaultCity.longitude
            val method = runCatching {
                CalculationMethod.valueOf(prefs.getString(KEY_METHOD, CalculationMethod.UMM_AL_QURA.name)!!)
            }.getOrDefault(CalculationMethod.UMM_AL_QURA)
            val asr = runCatching {
                AsrJuristicMethod.valueOf(prefs.getString(KEY_ASR, AsrJuristicMethod.SHAFII.name)!!)
            }.getOrDefault(AsrJuristicMethod.SHAFII)
            val use24Hour = prefs.getBoolean(KEY_USE_24H, false)
            val westernDigits = prefs.getBoolean(KEY_WESTERN_DIGITS, AppDigitConfig.useWesternDigits)
            val hijriOffset = prefs.getInt(KEY_HIJRI_OFFSET, 0).coerceIn(-2, 2)

            val tzId = prefs.getString(KEY_CITY_TZ, null)?.takeIf { it.isNotBlank() }
                ?: PresetCities.findTimeZoneId(cityName, "")
            val cityTimeZone = if (tzId.isNotBlank()) TimeZone.getTimeZone(tzId) else TimeZone.getDefault()

            val now = System.currentTimeMillis()
            val today = Calendar.getInstance(cityTimeZone)
            val daily = PrayerTimesCalculator.calculateForDate(today, lat, lon, method, asr)
            val nextPrayer = daily.entries.firstOrNull { it.prayer.isMandatoryPrayer && it.timestampMillis > now }?.prayer
            val hijriText = PrayerTimesCalculator.formatHijriDateAr(today, hijriOffset).localizeDigits(westernDigits)

            fun formatPrayer(prayer: PrayerName): String {
                val entry = daily.entries.firstOrNull { it.prayer == prayer } ?: return "--:--"
                return PrayerTimesCalculator.formatTime(entry.timestampMillis, cityTimeZone, use24Hour)
                    .localizeDigits(westernDigits)
            }

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId + 9000,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val highlightColor = Color.parseColor("#D4A857")
            val normalLabel = Color.parseColor("#B5A78F")
            val normalTime = Color.parseColor("#F3EBDA")

            val slots = listOf(
                Triple(PrayerName.FAJR, R.id.lbl_fajr, R.id.time_fajr),
                Triple(PrayerName.DHUHR, R.id.lbl_dhuhr, R.id.time_dhuhr),
                Triple(PrayerName.ASR, R.id.lbl_asr, R.id.time_asr),
                Triple(PrayerName.MAGHRIB, R.id.lbl_maghrib, R.id.time_maghrib),
                Triple(PrayerName.ISHA, R.id.lbl_isha, R.id.time_isha)
            )

            val views = RemoteViews(context.packageName, R.layout.widget_prayer_times).apply {
                setTextViewText(R.id.prayer_widget_city, cityName)
                setTextViewText(R.id.prayer_widget_hijri, hijriText)
                for ((prayer, lblId, timeId) in slots) {
                    val isNext = prayer == nextPrayer
                    setTextViewText(timeId, formatPrayer(prayer))
                    setTextColor(lblId, if (isNext) highlightColor else normalLabel)
                    setTextColor(timeId, if (isNext) highlightColor else normalTime)
                }
                setOnClickPendingIntent(R.id.prayer_widget_root, pendingIntent)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
