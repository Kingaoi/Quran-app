package com.example.data.local

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.prayer.AdhanVoices
import com.example.domain.prayer.AsrJuristicMethod
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.CityLocation
import com.example.domain.prayer.HijriSunnahConfig
import com.example.domain.prayer.PrayerName
import com.example.domain.prayer.PrayerRegion
import com.example.domain.prayer.PresetCities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.prayerDataStore by preferencesDataStore(name = "prayer_times_prefs")

enum class AdhanSoundMode(val labelAr: String) {
    FULL_ADHAN("أذان كامل (بصوت المؤذن)"),
    DEFAULT("تنبيه صوتي قياسي"),
    SOFT("نغمة هادئة"),
    SILENT_VIBRATE("صامت مع اهتزاز")
}

@Immutable
data class PrayerSettings(
    val city: CityLocation = PresetCities.defaultCity,
    val calculationMethod: CalculationMethod = CalculationMethod.UMM_AL_QURA,
    val asrMethod: AsrJuristicMethod = AsrJuristicMethod.SHAFII,
    val use24Hour: Boolean = false,
    val enabledAlerts: Set<String> = emptySet(),
    val preAdhanMinutes: Int = 0,
    val adhanSoundMode: AdhanSoundMode = AdhanSoundMode.FULL_ADHAN,
    val selectedAdhanVoiceId: String = AdhanVoices.defaultVoice.id,
    val hijriOffsetDays: Int = 0,
    val hijriSunnahConfig: HijriSunnahConfig = HijriSunnahConfig()
) {
    fun isAlertEnabled(prayer: PrayerName): Boolean = enabledAlerts.contains(prayer.id)
}

class PrayerPreferencesRepository(private val context: Context) {
    private object Keys {
        val CITY_NAME = stringPreferencesKey("city_name_ar")
        val CITY_COUNTRY = stringPreferencesKey("city_country_ar")
        val LATITUDE = doublePreferencesKey("city_lat")
        val LONGITUDE = doublePreferencesKey("city_lon")
        val CITY_REGION = stringPreferencesKey("city_region")
        val CITY_TZ = stringPreferencesKey("city_tz")
        val IS_GPS = booleanPreferencesKey("city_is_gps")
        val METHOD = stringPreferencesKey("calculation_method")
        val ASR_METHOD = stringPreferencesKey("asr_method")
        val USE_24H = booleanPreferencesKey("use_24h")
        val ENABLED_ALERTS = stringSetPreferencesKey("enabled_prayer_alerts")
        val PRE_ADHAN_MINUTES = intPreferencesKey("pre_adhan_minutes")
        val ADHAN_SOUND_MODE = stringPreferencesKey("adhan_sound_mode")
        val ADHAN_VOICE_ID = stringPreferencesKey("adhan_voice_id")
        val HIJRI_OFFSET_DAYS = intPreferencesKey("hijri_offset_days")
        val SUNNAH_ENABLED = booleanPreferencesKey("sunnah_enabled")
        val SUNNAH_KAHF = booleanPreferencesKey("sunnah_kahf")
        val SUNNAH_MON_THU = booleanPreferencesKey("sunnah_mon_thu")
        val SUNNAH_WHITE = booleanPreferencesKey("sunnah_white")
        val SUNNAH_OCCASIONS = booleanPreferencesKey("sunnah_occasions")
        val SUNNAH_FAST_HOUR = intPreferencesKey("sunnah_fast_hour")
        val SUNNAH_FAST_MINUTE = intPreferencesKey("sunnah_fast_minute")
    }

    val settingsFlow: Flow<PrayerSettings> = context.prayerDataStore.data.map { prefs ->
        val default = PresetCities.defaultCity
        val cityName = prefs[Keys.CITY_NAME] ?: default.nameAr
        val cityCountry = prefs[Keys.CITY_COUNTRY] ?: default.countryAr
        val cityRegion = runCatching {
            PrayerRegion.valueOf(prefs[Keys.CITY_REGION] ?: "")
        }.getOrElse { PresetCities.findRegion(cityName, cityCountry) }
        val cityTz = prefs[Keys.CITY_TZ]?.takeIf { it.isNotBlank() }
            ?: PresetCities.findTimeZoneId(cityName, cityCountry)
        val city = CityLocation(
            nameAr = cityName,
            countryAr = cityCountry,
            latitude = prefs[Keys.LATITUDE] ?: default.latitude,
            longitude = prefs[Keys.LONGITUDE] ?: default.longitude,
            region = cityRegion,
            timeZoneId = cityTz,
            isFromGps = prefs[Keys.IS_GPS] ?: false
        )
        val method = runCatching {
            CalculationMethod.valueOf(prefs[Keys.METHOD] ?: CalculationMethod.UMM_AL_QURA.name)
        }.getOrDefault(CalculationMethod.UMM_AL_QURA)
        val asr = runCatching {
            AsrJuristicMethod.valueOf(prefs[Keys.ASR_METHOD] ?: AsrJuristicMethod.SHAFII.name)
        }.getOrDefault(AsrJuristicMethod.SHAFII)
        val soundMode = runCatching {
            AdhanSoundMode.valueOf(prefs[Keys.ADHAN_SOUND_MODE] ?: AdhanSoundMode.FULL_ADHAN.name)
        }.getOrDefault(AdhanSoundMode.FULL_ADHAN)
        val voiceId = AdhanVoices.byId(prefs[Keys.ADHAN_VOICE_ID]).id
        PrayerSettings(
            city = city,
            calculationMethod = method,
            asrMethod = asr,
            use24Hour = prefs[Keys.USE_24H] ?: false,
            enabledAlerts = prefs[Keys.ENABLED_ALERTS] ?: emptySet(),
            preAdhanMinutes = (prefs[Keys.PRE_ADHAN_MINUTES] ?: 0).coerceIn(0, 30),
            adhanSoundMode = soundMode,
            selectedAdhanVoiceId = voiceId,
            hijriOffsetDays = (prefs[Keys.HIJRI_OFFSET_DAYS] ?: 0).coerceIn(-2, 2),
            hijriSunnahConfig = HijriSunnahConfig(
                enabled = prefs[Keys.SUNNAH_ENABLED] ?: false,
                fridayKahfEnabled = prefs[Keys.SUNNAH_KAHF] ?: true,
                mondayThursdayEnabled = prefs[Keys.SUNNAH_MON_THU] ?: true,
                whiteDaysEnabled = prefs[Keys.SUNNAH_WHITE] ?: true,
                occasionsEnabled = prefs[Keys.SUNNAH_OCCASIONS] ?: true,
                fastingReminderHour = (prefs[Keys.SUNNAH_FAST_HOUR] ?: 20).coerceIn(0, 23),
                fastingReminderMinute = (prefs[Keys.SUNNAH_FAST_MINUTE] ?: 30).coerceIn(0, 59)
            )
        )
    }

    suspend fun setCity(city: CityLocation) {
        context.prayerDataStore.edit { prefs ->
            prefs[Keys.CITY_NAME] = city.nameAr
            prefs[Keys.CITY_COUNTRY] = city.countryAr
            prefs[Keys.LATITUDE] = city.latitude
            prefs[Keys.LONGITUDE] = city.longitude
            prefs[Keys.CITY_REGION] = city.region.name
            prefs[Keys.CITY_TZ] = city.resolveTimeZone().id
            prefs[Keys.IS_GPS] = city.isFromGps
        }
    }

    suspend fun setCalculationMethod(method: CalculationMethod) {
        context.prayerDataStore.edit { it[Keys.METHOD] = method.name }
    }

    suspend fun setAsrMethod(method: AsrJuristicMethod) {
        context.prayerDataStore.edit { it[Keys.ASR_METHOD] = method.name }
    }

    suspend fun setUse24Hour(use24Hour: Boolean) {
        context.prayerDataStore.edit { it[Keys.USE_24H] = use24Hour }
    }

    suspend fun setPreAdhanMinutes(minutes: Int) {
        context.prayerDataStore.edit { it[Keys.PRE_ADHAN_MINUTES] = minutes.coerceIn(0, 30) }
    }

    suspend fun setAdhanSoundMode(mode: AdhanSoundMode) {
        context.prayerDataStore.edit { it[Keys.ADHAN_SOUND_MODE] = mode.name }
    }

    suspend fun setAdhanVoiceId(voiceId: String) {
        val validId = AdhanVoices.byId(voiceId).id
        context.prayerDataStore.edit { it[Keys.ADHAN_VOICE_ID] = validId }
    }

    suspend fun setHijriOffsetDays(offset: Int) {
        context.prayerDataStore.edit { it[Keys.HIJRI_OFFSET_DAYS] = offset.coerceIn(-2, 2) }
    }

    suspend fun updateHijriSunnahConfig(transform: (HijriSunnahConfig) -> HijriSunnahConfig) {
        context.prayerDataStore.edit { prefs ->
            val current = HijriSunnahConfig(
                enabled = prefs[Keys.SUNNAH_ENABLED] ?: false,
                fridayKahfEnabled = prefs[Keys.SUNNAH_KAHF] ?: true,
                mondayThursdayEnabled = prefs[Keys.SUNNAH_MON_THU] ?: true,
                whiteDaysEnabled = prefs[Keys.SUNNAH_WHITE] ?: true,
                occasionsEnabled = prefs[Keys.SUNNAH_OCCASIONS] ?: true,
                fastingReminderHour = (prefs[Keys.SUNNAH_FAST_HOUR] ?: 20).coerceIn(0, 23),
                fastingReminderMinute = (prefs[Keys.SUNNAH_FAST_MINUTE] ?: 30).coerceIn(0, 59)
            )
            val next = transform(current)
            prefs[Keys.SUNNAH_ENABLED] = next.enabled
            prefs[Keys.SUNNAH_KAHF] = next.fridayKahfEnabled
            prefs[Keys.SUNNAH_MON_THU] = next.mondayThursdayEnabled
            prefs[Keys.SUNNAH_WHITE] = next.whiteDaysEnabled
            prefs[Keys.SUNNAH_OCCASIONS] = next.occasionsEnabled
            prefs[Keys.SUNNAH_FAST_HOUR] = next.fastingReminderHour.coerceIn(0, 23)
            prefs[Keys.SUNNAH_FAST_MINUTE] = next.fastingReminderMinute.coerceIn(0, 59)
        }
    }

    suspend fun togglePrayerAlert(prayer: PrayerName): Boolean {
        var enabledNow = false
        context.prayerDataStore.edit { prefs ->
            val current = prefs[Keys.ENABLED_ALERTS]?.toMutableSet() ?: mutableSetOf()
            enabledNow = if (current.add(prayer.id)) true else {
                current.remove(prayer.id)
                false
            }
            prefs[Keys.ENABLED_ALERTS] = current
        }
        return enabledNow
    }

    suspend fun exportJson(): JSONObject {
        val s = settingsFlow.first()
        return JSONObject().apply {
            put("cityName", s.city.nameAr)
            put("cityCountry", s.city.countryAr)
            put("lat", s.city.latitude)
            put("lon", s.city.longitude)
            put("cityRegion", s.city.region.name)
            put("cityTz", s.city.resolveTimeZone().id)
            put("gps", s.city.isFromGps)
            put("method", s.calculationMethod.name)
            put("asr", s.asrMethod.name)
            put("use24h", s.use24Hour)
            put("preAdhan", s.preAdhanMinutes)
            put("soundMode", s.adhanSoundMode.name)
            put("adhanVoiceId", s.selectedAdhanVoiceId)
            put("hijriOffset", s.hijriOffsetDays)
            put("alerts", JSONArray().apply { s.enabledAlerts.forEach(::put) })
            put("sunnahEnabled", s.hijriSunnahConfig.enabled)
            put("sunnahKahf", s.hijriSunnahConfig.fridayKahfEnabled)
            put("sunnahMonThu", s.hijriSunnahConfig.mondayThursdayEnabled)
            put("sunnahWhite", s.hijriSunnahConfig.whiteDaysEnabled)
            put("sunnahOccasions", s.hijriSunnahConfig.occasionsEnabled)
            put("sunnahFastHour", s.hijriSunnahConfig.fastingReminderHour)
            put("sunnahFastMinute", s.hijriSunnahConfig.fastingReminderMinute)
        }
    }

    suspend fun importJson(obj: JSONObject) {
        val alertsArr = obj.optJSONArray("alerts")
        val alertsSet = buildSet {
            if (alertsArr != null) {
                for (i in 0 until alertsArr.length()) {
                    alertsArr.optString(i).takeIf { it.isNotBlank() }?.let(::add)
                }
            }
        }
        val importedName = obj.optString("cityName", PresetCities.defaultCity.nameAr)
        val importedCountry = obj.optString("cityCountry", PresetCities.defaultCity.countryAr)
        val importedRegion = runCatching {
            PrayerRegion.valueOf(obj.optString("cityRegion", ""))
        }.getOrElse { PresetCities.findRegion(importedName, importedCountry) }
        val importedTz = obj.optString("cityTz", "").ifBlank {
            PresetCities.findTimeZoneId(importedName, importedCountry)
        }
        context.prayerDataStore.edit { prefs ->
            prefs[Keys.CITY_NAME] = importedName
            prefs[Keys.CITY_COUNTRY] = importedCountry
            prefs[Keys.LATITUDE] = obj.optDouble("lat", PresetCities.defaultCity.latitude)
            prefs[Keys.LONGITUDE] = obj.optDouble("lon", PresetCities.defaultCity.longitude)
            prefs[Keys.CITY_REGION] = importedRegion.name
            prefs[Keys.CITY_TZ] = importedTz
            prefs[Keys.IS_GPS] = obj.optBoolean("gps", false)
            prefs[Keys.METHOD] = obj.optString("method", CalculationMethod.UMM_AL_QURA.name)
            prefs[Keys.ASR_METHOD] = obj.optString("asr", AsrJuristicMethod.SHAFII.name)
            prefs[Keys.USE_24H] = obj.optBoolean("use24h", false)
            prefs[Keys.PRE_ADHAN_MINUTES] = obj.optInt("preAdhan", 0).coerceIn(0, 30)
            prefs[Keys.ADHAN_SOUND_MODE] = obj.optString("soundMode", AdhanSoundMode.FULL_ADHAN.name)
            prefs[Keys.ADHAN_VOICE_ID] = AdhanVoices.byId(obj.optString("adhanVoiceId", "")).id
            prefs[Keys.HIJRI_OFFSET_DAYS] = obj.optInt("hijriOffset", 0).coerceIn(-2, 2)
            prefs[Keys.ENABLED_ALERTS] = alertsSet
            prefs[Keys.SUNNAH_ENABLED] = obj.optBoolean("sunnahEnabled", false)
            prefs[Keys.SUNNAH_KAHF] = obj.optBoolean("sunnahKahf", true)
            prefs[Keys.SUNNAH_MON_THU] = obj.optBoolean("sunnahMonThu", true)
            prefs[Keys.SUNNAH_WHITE] = obj.optBoolean("sunnahWhite", true)
            prefs[Keys.SUNNAH_OCCASIONS] = obj.optBoolean("sunnahOccasions", true)
            prefs[Keys.SUNNAH_FAST_HOUR] = obj.optInt("sunnahFastHour", 20).coerceIn(0, 23)
            prefs[Keys.SUNNAH_FAST_MINUTE] = obj.optInt("sunnahFastMinute", 30).coerceIn(0, 59)
        }
    }
}
