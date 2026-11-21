package com.example

import com.example.domain.prayer.AsrJuristicMethod
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.PrayerName
import com.example.domain.prayer.PrayerRegion
import com.example.domain.prayer.PrayerTimesCalculator
import com.example.domain.prayer.PresetCities
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ExpandedPrayerRegionsTest {

    @Test
    fun presetCities_coverAllRegionsWithValidCoordinatesTimeZonesAndNoDuplicates() {
        val cities = PresetCities.all
        assertTrue("Expected expanded city catalog >= 120, was ${cities.size}", cities.size >= 120)

        for (region in PrayerRegion.entries) {
            val count = PresetCities.byRegion(region).size
            assertTrue("Region ${region.name} should have at least 15 cities, had $count", count >= 15)
        }

        val availableZones = TimeZone.getAvailableIDs().toSet()
        val seenKeys = mutableSetOf<String>()

        for (city in cities) {
            assertTrue("Invalid latitude for ${city.nameAr}", city.latitude in -90.0..90.0)
            assertTrue("Invalid longitude for ${city.nameAr}", city.longitude in -180.0..180.0)
            assertTrue("Blank timeZoneId for ${city.nameAr}", city.timeZoneId.isNotBlank())
            assertTrue(
                "Invalid timeZoneId '${city.timeZoneId}' for ${city.nameAr}",
                availableZones.contains(city.timeZoneId)
            )
            assertEquals(city.timeZoneId, city.resolveTimeZone().id)

            val uniqueKey = "${city.nameAr}|${city.countryAr}"
            assertTrue("Duplicate city found: $uniqueKey", seenKeys.add(uniqueKey))
        }
    }

    @Test
    fun arabicSearchNormalization_matchesHamzaTashkeelAndTaMarbutaVariants() {
        assertTrue(PresetCities.search("اسطنبول").any { it.nameAr == "إسطنبول" })
        assertTrue(PresetCities.search("ابوظبي").any { it.nameAr == "أبوظبي" })
        assertTrue(PresetCities.search("مكه").any { it.nameAr == "مكة المكرمة" })
        assertTrue(PresetCities.search("عمان").any { it.nameAr == "عمّان" })
        assertTrue(PresetCities.search("طوكيو", PrayerRegion.ASIA_TURKEY).any { it.nameAr == "طوكيو" })
        assertTrue(PresetCities.search("طوكيو", PrayerRegion.EUROPE).isEmpty())
    }

    @Test
    fun prayerCalculator_usesCityTimeZoneInsteadOfDeviceDefaultTimeZone() {
        val prevDefault = TimeZone.getDefault()
        try {
            // Force host JVM into Pacific/Honolulu to ensure calculation isn't tied to host TZ
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Honolulu"))

            val testCities = listOf("مكة المكرمة", "لندن", "نيويورك", "طوكيو", "سيدني")
            for (name in testCities) {
                val city = PresetCities.all.first { it.nameAr == name }
                val cal = Calendar.getInstance(city.resolveTimeZone()).apply {
                    set(2025, Calendar.MARCH, 21, 12, 0, 0)
                }
                val daily = PrayerTimesCalculator.calculateForDate(
                    calendar = cal,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    method = CalculationMethod.MWL,
                    asrMethod = AsrJuristicMethod.SHAFII
                )
                val fajr = daily.entries.first { it.prayer == PrayerName.FAJR }
                val dhuhr = daily.entries.first { it.prayer == PrayerName.DHUHR }
                val maghrib = daily.entries.first { it.prayer == PrayerName.MAGHRIB }

                assertTrue("$name Fajr hour=${fajr.hour24}", fajr.hour24 in 3..6)
                assertTrue("$name Dhuhr hour=${dhuhr.hour24}", dhuhr.hour24 in 11..14)
                assertTrue("$name Maghrib hour=${maghrib.hour24}", maghrib.hour24 in 17..20)
            }
        } finally {
            TimeZone.setDefault(prevDefault)
        }
    }
}
