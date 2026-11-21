package com.example.data.prayer

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.example.domain.prayer.CityLocation
import com.example.domain.prayer.PresetCities
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

internal object PrayerLocationResolver {

    @SuppressLint("MissingPermission")
    fun readBestLastLocation(context: Context): Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        return providers.mapNotNull { provider ->
            runCatching { if (lm.isProviderEnabled(provider)) lm.getLastKnownLocation(provider) else null }.getOrNull()
        }.maxByOrNull { it.time }
    }

    fun resolveCityFromCoordinates(context: Context, lat: Double, lon: Double): CityLocation {
        val deviceTzId = TimeZone.getDefault().id
        val nearest = PresetCities.all.minByOrNull { city -> abs(city.latitude - lat) + abs(city.longitude - lon) }
        val nearPreset = nearest != null && (abs(nearest.latitude - lat) + abs(nearest.longitude - lon)) < 0.8
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            @Suppress("DEPRECATION")
            val geo = runCatching { Geocoder(context, Locale("ar")).getFromLocation(lat, lon, 1)?.firstOrNull() }.getOrNull()
            val locality = geo?.locality ?: geo?.subAdminArea ?: geo?.adminArea
            val country = geo?.countryName ?: "الموقع الحالي"
            if (!locality.isNullOrBlank()) {
                return CityLocation(locality, country, lat, lon, nearest?.region ?: PresetCities.defaultCity.region, deviceTzId, isFromGps = true)
            }
        }
        return if (nearPreset && nearest != null) {
            nearest.copy(latitude = lat, longitude = lon, isFromGps = true)
        } else {
            CityLocation(
                nameAr = "موقعي الحالي",
                countryAr = "${"%.2f".format(Locale.US, lat)}°, ${"%.2f".format(Locale.US, lon)}°",
                latitude = lat,
                longitude = lon,
                region = nearest?.region ?: PresetCities.defaultCity.region,
                timeZoneId = deviceTzId,
                isFromGps = true
            )
        }
    }
}
