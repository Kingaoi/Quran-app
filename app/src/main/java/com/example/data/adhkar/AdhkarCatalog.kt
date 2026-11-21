package com.example.data.adhkar

import com.example.domain.model.DhikrCategory
import com.example.domain.model.DhikrItem

object AdhkarCatalog {

    val all: List<DhikrItem> =
        MorningEveningAdhkarData.morningItems +
            MorningEveningAdhkarData.eveningItems +
            OtherAdhkarData.afterPrayerItems +
            OtherAdhkarData.sleepItems +
            MosqueAndWakeAdhkarData.mosqueItems +
            MosqueAndWakeAdhkarData.wakeUpItems +
            DuasAndRuqyahAdhkarData.quranicDuasItems +
            DuasAndRuqyahAdhkarData.propheticDuasItems +
            DuasAndRuqyahAdhkarData.ruqyahItems +
            MosqueAndWakeAdhkarData.travelItems +
            OtherAdhkarData.tasbihItems +
            ExtraAdhkarFallbackData.dailyLifeItems +
            ExtraAdhkarFallbackData.worryReliefItems +
            ExtraAdhkarFallbackData.weatherNatureItems +
            ExtraAdhkarFallbackData.socialEthicsItems +
            ExtraAdhkarFallbackData.hajjWorshipItems

    fun byCategory(category: DhikrCategory): List<DhikrItem> =
        when (category) {
            DhikrCategory.MORNING -> MorningEveningAdhkarData.morningItems
            DhikrCategory.EVENING -> MorningEveningAdhkarData.eveningItems
            DhikrCategory.AFTER_PRAYER -> OtherAdhkarData.afterPrayerItems
            DhikrCategory.SLEEP -> OtherAdhkarData.sleepItems
            DhikrCategory.MOSQUE -> MosqueAndWakeAdhkarData.mosqueItems
            DhikrCategory.WAKE_UP -> MosqueAndWakeAdhkarData.wakeUpItems
            DhikrCategory.QURANIC_DUAS -> DuasAndRuqyahAdhkarData.quranicDuasItems
            DhikrCategory.PROPHETIC_DUAS -> DuasAndRuqyahAdhkarData.propheticDuasItems
            DhikrCategory.RUQYAH -> DuasAndRuqyahAdhkarData.ruqyahItems
            DhikrCategory.TRAVEL -> MosqueAndWakeAdhkarData.travelItems
            DhikrCategory.TASBIH -> OtherAdhkarData.tasbihItems
            DhikrCategory.DAILY_LIFE -> ExtraAdhkarFallbackData.dailyLifeItems
            DhikrCategory.WORRY_RELIEF -> ExtraAdhkarFallbackData.worryReliefItems
            DhikrCategory.WEATHER_NATURE -> ExtraAdhkarFallbackData.weatherNatureItems
            DhikrCategory.SOCIAL_ETHICS -> ExtraAdhkarFallbackData.socialEthicsItems
            DhikrCategory.HAJJ_WORSHIP -> ExtraAdhkarFallbackData.hajjWorshipItems
        }

    fun defaultMap(): Map<DhikrCategory, List<DhikrItem>> =
        DhikrCategory.entries.associateWith { byCategory(it) }
}
