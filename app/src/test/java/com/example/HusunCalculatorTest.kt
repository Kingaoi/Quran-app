package com.example

import com.example.domain.model.AyahItem
import com.example.domain.wird.HusunCalculator
import com.example.domain.wird.HusunConfig
import com.example.domain.wird.HusunFortressType
import com.example.domain.wird.HusunNewRate
import com.example.domain.wird.WirdDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HusunCalculatorTest {

    private val sampleAyahs = (1..604).map { page ->
        AyahItem(
            surahNumber = if (page == 1) 1 else if (page <= 49) 2 else 78,
            surahNameAr = if (page == 1) "الفاتحة" else if (page <= 49) "البقرة" else "النبأ",
            ayahNumber = 1,
            text = "آية اختبارية",
            imlaiText = "اية اختبارية",
            marker = "١",
            pageNumber = page,
            juzNumber = ((page - 1) / 20) + 1,
            hasSajdah = false
        )
    }

    @Test
    fun `inactive config returns null daily plan`() {
        val plan = HusunCalculator.buildDailyPlan(
            config = HusunConfig(isActive = false),
            allAyahs = sampleAyahs,
            todayEpochDay = 20000L
        )
        assertNull(plan)
    }

    @Test
    fun `new beginner plan computes all five fortresses accurately`() {
        val plan = HusunCalculator.buildDailyPlan(
            config = HusunConfig(
                isActive = true,
                direction = WirdDirection.FATIHA_TO_NAS,
                startingMemorizedPages = 0,
                dailyNewRate = HusunNewRate.ONE_PAGE,
                continuousPagesPerDay = 40,
                farReviewDivisions = 7,
                completedNewUnits = 0,
                continuousCursorDay = 0,
                lastActiveEpochDay = 20000L,
                todayFortressMask = HusunFortressType.CONTINUOUS_READING.bitMask
            ),
            allAyahs = sampleAyahs,
            todayEpochDay = 20000L
        )
        assertNotNull(plan)
        plan!!
        assertEquals(5, plan.fortresses.size)
        assertEquals(1, plan.completedTodayCount)
        assertEquals(0, plan.totalMemorizedPages)

        val f1 = plan.fortresses[0]
        assertTrue(f1.isCompletedToday)
        assertEquals(1, f1.targetPage)
        assertTrue(f1.canOpenReader)

        val f2 = plan.fortresses[1]
        assertEquals(2, f2.targetPage)
        assertTrue(f2.canOpenReader)

        val f3 = plan.fortresses[2]
        assertEquals(1, f3.targetPage)
        assertTrue(f3.canOpenReader)

        val f4 = plan.fortresses[3]
        assertFalse(f4.canOpenReader)

        val f5 = plan.fortresses[4]
        assertFalse(f5.canOpenReader)
    }

    @Test
    fun `experienced hafiz in reverse direction activates both near and far review and resets mask on new day`() {
        val config = HusunConfig(
            isActive = true,
            direction = WirdDirection.NAS_TO_FATIHA,
            startingMemorizedPages = 50,
            dailyNewRate = HusunNewRate.HALF_PAGE,
            continuousPagesPerDay = 20,
            farReviewDivisions = 5,
            completedNewUnits = 0,
            continuousCursorDay = 1,
            lastActiveEpochDay = 20000L,
            todayFortressMask = 0b11111,
            todayNewRepeatCount = 18
        )
        val planNextDay = HusunCalculator.buildDailyPlan(
            config = config,
            allAyahs = sampleAyahs,
            todayEpochDay = 20001L
        )
        assertNotNull(planNextDay)
        planNextDay!!
        assertEquals(0, planNextDay.completedTodayCount)
        assertEquals(0, planNextDay.todayRepeatCount)
        assertEquals(50, planNextDay.totalMemorizedPages)

        val f3 = planNextDay.fortresses[2]
        assertEquals(554, f3.targetPage)
        assertTrue(f3.rangeTitleAr.contains("النصف الأول"))

        val f4 = planNextDay.fortresses[3]
        assertTrue(f4.canOpenReader)

        val f5 = planNextDay.fortresses[4]
        assertTrue(f5.canOpenReader)
    }
}
