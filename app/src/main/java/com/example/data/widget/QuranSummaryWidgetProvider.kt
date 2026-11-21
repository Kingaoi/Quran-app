package com.example.data.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.domain.model.AppDigitConfig
import com.example.domain.model.localizeDigits
import com.example.domain.prayer.AsrJuristicMethod
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.PrayerTimesCalculator
import com.example.domain.prayer.PresetCities
import java.util.Calendar
import java.util.TimeZone

class QuranSummaryWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateSingleWidget(context, appWidgetManager, appWidgetId)
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
        private const val KEY_WIRD_TEXT = "wird_summary"
        private const val KEY_HIJRI_OFFSET = "hijri_offset_days"

        fun savePrayerSnapshot(
            context: Context,
            cityName: String,
            lat: Double,
            lon: Double,
            cityTz: String = PresetCities.defaultCity.timeZoneId,
            method: CalculationMethod,
            asr: AsrJuristicMethod,
            use24Hour: Boolean,
            westernDigits: Boolean = AppDigitConfig.useWesternDigits,
            hijriOffsetDays: Int = 0
        ) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putString(KEY_CITY_NAME, cityName)
                .putString(KEY_CITY_TZ, cityTz)
                .putLong(KEY_LAT, java.lang.Double.doubleToRawLongBits(lat))
                .putLong(KEY_LON, java.lang.Double.doubleToRawLongBits(lon))
                .putString(KEY_METHOD, method.name)
                .putString(KEY_ASR, asr.name)
                .putBoolean(KEY_USE_24H, use24Hour)
                .putBoolean(KEY_WESTERN_DIGITS, westernDigits)
                .putInt(KEY_HIJRI_OFFSET, hijriOffsetDays.coerceIn(-2, 2))
                .apply()
            notifyWidgetDataChanged(context)
        }

        fun saveWirdSnapshot(context: Context, wirdSummary: String) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putString(KEY_WIRD_TEXT, wirdSummary)
                .apply()
            notifyWidgetDataChanged(context)
        }

        fun notifyWidgetDataChanged(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            if (manager != null) {
                val component = ComponentName(context, QuranSummaryWidgetProvider::class.java)
                val ids = manager.getAppWidgetIds(component)
                for (id in ids) updateSingleWidget(context, manager, id)
            }
            PrayerTimesWidgetProvider.notifyWidgetDataChanged(context)
        }

        internal fun updateSingleWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val defaultCity = PresetCities.defaultCity
            val cityName = prefs.getString(KEY_CITY_NAME, defaultCity.nameAr) ?: defaultCity.nameAr
            val lat = if (prefs.contains(KEY_LAT)) {
                java.lang.Double.longBitsToDouble(prefs.getLong(KEY_LAT, 0L))
            } else {
                defaultCity.latitude
            }
            val lon = if (prefs.contains(KEY_LON)) {
                java.lang.Double.longBitsToDouble(prefs.getLong(KEY_LON, 0L))
            } else {
                defaultCity.longitude
            }
            val method = runCatching {
                CalculationMethod.valueOf(prefs.getString(KEY_METHOD, CalculationMethod.UMM_AL_QURA.name)!!)
            }.getOrDefault(CalculationMethod.UMM_AL_QURA)
            val asr = runCatching {
                AsrJuristicMethod.valueOf(prefs.getString(KEY_ASR, AsrJuristicMethod.SHAFII.name)!!)
            }.getOrDefault(AsrJuristicMethod.SHAFII)
            val use24Hour = prefs.getBoolean(KEY_USE_24H, false)
            val westernDigits = prefs.getBoolean(KEY_WESTERN_DIGITS, AppDigitConfig.useWesternDigits)
            val wirdText = prefs.getString(KEY_WIRD_TEXT, null)
                ?: "تابع وردك اليومي من كتاب الله"

            val tzId = prefs.getString(KEY_CITY_TZ, null)?.takeIf { it.isNotBlank() }
                ?: PresetCities.findTimeZoneId(cityName, "")
            val cityTimeZone = if (tzId.isNotBlank()) TimeZone.getTimeZone(tzId) else TimeZone.getDefault()

            val now = System.currentTimeMillis()
            val today = Calendar.getInstance(cityTimeZone)
            val todayTimes = PrayerTimesCalculator.calculateForDate(today, lat, lon, method, asr)
            val upcoming = todayTimes.entries.firstOrNull { it.timestampMillis > now }
                ?: PrayerTimesCalculator.calculateForDate(
                    (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) },
                    lat,
                    lon,
                    method,
                    asr
                ).entries.firstOrNull()

            val nextLabel = upcoming?.let { "صلاة ${it.prayer.titleAr}" } ?: "مواقيت الصلاة"
            val nextTimeRaw = upcoming?.let {
                PrayerTimesCalculator.formatTime(it.timestampMillis, cityTimeZone, use24Hour)
            } ?: "--:--"
            val nextTime = nextTimeRaw.localizeDigits(westernDigits)
            val localizedWird = wirdText.localizeDigits(westernDigits)

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId + 8000,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(context.packageName, R.layout.widget_quran_summary).apply {
                setTextViewText(R.id.widget_city, cityName)
                setTextViewText(R.id.widget_next_prayer_label, nextLabel)
                setTextViewText(R.id.widget_next_prayer_time, nextTime)
                setTextViewText(R.id.widget_wird_summary, localizedWird)
                setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
