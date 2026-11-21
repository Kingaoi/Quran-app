package com.example

import com.example.domain.model.AyahItem
import com.example.domain.model.HizbQuarterInfo
import com.example.domain.model.JuzInfo
import com.example.domain.wird.KhatmahDuaCatalog
import com.example.domain.wird.KhatmahPlanConfig
import com.example.domain.wird.WirdPlanCalculator
import com.example.domain.wird.WirdScheduleStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WirdPlanCalculatorTest {

    @Test
    fun durationAndQuartersConversions_stayConsistent() {
        assertEquals(240, WirdPlanCalculator.remainingQuartersFromJuz(1))
        assertEquals(16, WirdPlanCalculator.remainingQuartersFromJuz(29))
        assertEquals(8, WirdPlanCalculator.quartersFromDays(240, 30))
        assertEquals(30, WirdPlanCalculator.daysFromQuarters(240, 8))
        assertEquals(4, WirdPlanCalculator.quartersFromDays(240, 60))
        assertEquals(60, WirdPlanCalculator.daysFromQuarters(240, 4))
    }

    @Test
    fun buildPlan_partitionsExactAyahRangesTracksProgressBehindAheadAndCompletion() {
        val sampleAyahs = (1..240).map { qIdx ->
            val juz = ((qIdx - 1) / 8) + 1
            AyahItem(
                surahNumber = juz,
                surahNameAr = "سورة $juz",
                ayahNumber = qIdx,
                text = "آية رقم $qIdx من القرآن الكريم المبارك",
                imlaiText = "اية رقم $qIdx",
                marker = "۝",
                pageNumber = (qIdx * 2).coerceAtMost(604),
                juzNumber = juz,
                hasSajdah = false
            )
        }
        val sampleQuarters = (1..240).map { qIdx ->
            val juz = ((qIdx - 1) / 8) + 1
            HizbQuarterInfo(
                rubNumber = qIdx,
                juzNumber = juz,
                hizbNumber = ((qIdx - 1) / 4) + 1,
                quarterInHizb = (qIdx - 1) % 4,
                surahNumber = juz,
                surahNameAr = "سورة $juz",
                ayahNumber = qIdx,
                pageNumber = (qIdx * 2).coerceAtMost(604),
                openingText = "بداية الربع رقم $qIdx من المصحف الشريف"
            )
        }
        val sampleAjza = (1..30).map { j ->
            JuzInfo(
                number = j,
                nameAr = "الجزء $j",
                startSurahNumber = j,
                startSurahNameAr = "سورة $j",
                startAyahNumber = (j - 1) * 8 + 1,
                startPage = ((j - 1) * 8 + 1) * 2
            )
        }

        // Start from Juz 1 with 11 quarters/day -> ceil(240 / 11) = 22 -> wait: 21*11 + 9 = 240 -> 22 Wirds; let's check 10/day = 24 Wirds
        val baseConfig = KhatmahPlanConfig(
            isActive = true,
            startJuz = 1,
            quartersPerDay = 8,
            completedWirdsCount = 1,
            startEpochDay = 100L,
            lastProgressPage = 22
        )
        val behindPlan = WirdPlanCalculator.buildPlan(
            config = baseConfig,
            quarters = sampleQuarters,
            allAyahs = sampleAyahs,
            ajza = sampleAjza,
            todayEpochDay = 103L // elapsed = 3 days, completed = 1 -> 2 days behind
        )!!
        assertEquals(30, behindPlan.totalWirds)
        assertEquals(1, behindPlan.completedCount)
        assertEquals(28, behindPlan.upcomingCount)
        assertFalse(behindPlan.isCompleted)
        assertEquals(WirdScheduleStatus.Behind(2), behindPlan.scheduleStatus)
        assertEquals("أنت متأخر عن ختمتك بيومين", WirdPlanCalculator.formatScheduleStatusAr(behindPlan.scheduleStatus))
        assertNotNull(behindPlan.currentWird)
        assertEquals(18, behindPlan.currentWird!!.startPage) // quarter #9 -> page 18
        assertEquals(32, behindPlan.currentWird!!.endPage)   // ends before quarter #17 -> ayah 16 -> page 32
        assertTrue(behindPlan.hasStartedCurrentWird)
        assertEquals(22, behindPlan.resumePage)

        // Ahead by 1 day
        val aheadPlan = WirdPlanCalculator.buildPlan(
            config = baseConfig.copy(completedWirdsCount = 4),
            quarters = sampleQuarters,
            allAyahs = sampleAyahs,
            ajza = sampleAjza,
            todayEpochDay = 103L
        )!!
        assertEquals(WirdScheduleStatus.Ahead(1), aheadPlan.scheduleStatus)

        // Completed Khatmah
        val finishedPlan = WirdPlanCalculator.buildPlan(
            config = baseConfig.copy(completedWirdsCount = 30),
            quarters = sampleQuarters,
            allAyahs = sampleAyahs,
            ajza = sampleAjza,
            todayEpochDay = 130L
        )!!
        assertTrue(finishedPlan.isCompleted)
        assertNull(finishedPlan.currentWird)
        assertEquals(0, finishedPlan.upcomingCount)
        assertEquals(1f, finishedPlan.progressFraction, 0.001f)
    }

    @Test
    fun arabicFormattersAndDuaCatalog_areValid() {
        assertEquals("جزء واحد", WirdPlanCalculator.formatDailyPortionSummaryAr(8))
        assertEquals("حزب", WirdPlanCalculator.formatDailyPortionSummaryAr(4))
        assertEquals("جزء واحد وربع", WirdPlanCalculator.formatDailyPortionSummaryAr(9))
        assertEquals("جزءان وحزب", WirdPlanCalculator.formatDailyPortionSummaryAr(20))
        assertEquals("٣٠ يوماً", WirdPlanCalculator.formatDurationDaysAr(30))
        assertTrue(KhatmahDuaCatalog.paragraphs.size >= 8)
        assertTrue(KhatmahDuaCatalog.fullText.contains("اللَّهُمَّ ارْحَمْنِي بِالْقُرْآنِ"))
    }

    @Test
    fun customWirdPlanCalculator_handlesReverseDirectionAndThreeGoalModes() {
        val sampleAyahs = (1..30).map { idx ->
            val surah = ((idx - 1) / 10) + 1
            val ayahInSurah = ((idx - 1) % 10) + 1
            AyahItem(
                surahNumber = surah,
                surahNameAr = "سورة $surah",
                ayahNumber = ayahInSurah,
                text = "آية $idx",
                imlaiText = "اية $idx",
                marker = "۝",
                pageNumber = ((idx - 1) / 5) + 1,
                juzNumber = 1,
                hasSajdah = false
            )
        }
        val sampleAjza = listOf(
            JuzInfo(1, "الجزء الأول", 1, "سورة 1", 1, 1)
        )

        val nasToFatiha = com.example.domain.wird.NamedWirdPlanConfig(
            id = "rev_plan",
            title = "حفظ عكسي",
            isV2Plan = true,
            planType = com.example.domain.wird.WirdPlanType.MEMORIZATION,
            direction = com.example.domain.wird.WirdDirection.NAS_TO_FATIHA,
            fromSurah = 3,
            fromAyah = 1,
            toSurah = 1,
            toAyah = 10,
            goalType = com.example.domain.wird.WirdGoalType.AMOUNT,
            amountValue = 2f,
            amountUnit = com.example.domain.wird.WirdAmountUnit.PAGE,
            startEpochDay = 20_000L,
            sessionsPerDay = 2
        )
        val summary = com.example.domain.wird.CustomWirdPlanCalculator.buildPlan(
            config = nasToFatiha,
            allAyahs = sampleAyahs,
            ajza = sampleAjza,
            todayEpochDay = 20_000L
        )!!
        assertEquals(3, summary.totalWirds)
        assertEquals(3, summary.currentWird!!.startSurahNumber)
        assertEquals(1, summary.allWirds.last().endSurahNumber)

        val durationMetrics = com.example.domain.wird.CustomWirdPlanCalculator.calculateMetrics(
            config = nasToFatiha.copy(
                goalType = com.example.domain.wird.WirdGoalType.DURATION,
                targetDurationDays = 6
            ),
            allAyahs = sampleAyahs,
            surahs = emptyList()
        )
        assertEquals(6, durationMetrics.studyDaysCount)
        assertEquals(1.0, durationMetrics.dailyPagesExact, 0.05)
        assertEquals(0.5, durationMetrics.perSessionInUnit, 0.05)

        val rangeMetrics = com.example.domain.wird.CustomWirdPlanCalculator.calculateMetrics(
            config = nasToFatiha.copy(
                goalType = com.example.domain.wird.WirdGoalType.DATE_RANGE,
                startEpochDay = 20_000L,
                endEpochDay = 20_002L
            ),
            allAyahs = sampleAyahs,
            surahs = emptyList()
        )
        assertEquals(3, rangeMetrics.studyDaysCount)
        assertEquals(2.0, rangeMetrics.dailyPagesExact, 0.05)

        val monthMetrics = com.example.domain.wird.CustomWirdPlanCalculator.calculateMetrics(
            config = nasToFatiha.copy(
                goalType = com.example.domain.wird.WirdGoalType.DURATION,
                targetDurationDays = 1,
                durationUnit = com.example.domain.wird.WirdDurationUnit.MONTH
            ),
            allAyahs = sampleAyahs,
            surahs = emptyList()
        )
        assertEquals(30, monthMetrics.studyDaysCount)
        assertEquals("٣٠ يوماً", com.example.domain.wird.CustomWirdPlanCalculator.formatDurationUnitAr(30, com.example.domain.wird.WirdDurationUnit.DAY))
        assertEquals("يومان", com.example.domain.wird.CustomWirdPlanCalculator.formatDurationUnitAr(2, com.example.domain.wird.WirdDurationUnit.DAY))
        assertEquals("٣ أسابيع", com.example.domain.wird.CustomWirdPlanCalculator.formatDurationUnitAr(3, com.example.domain.wird.WirdDurationUnit.WEEK))
        assertEquals("جلسة واحدة", com.example.domain.wird.CustomWirdPlanCalculator.formatSessionsCountAr(1))
        assertEquals("جلستان", com.example.domain.wird.CustomWirdPlanCalculator.formatSessionsCountAr(2))
        val weekdayFormatted = com.example.domain.wird.CustomWirdPlanCalculator.formatEpochDayWithWeekdayAr(20_000L)
        assertTrue(weekdayFormatted.isNotBlank() && weekdayFormatted.contains(" "))
    }
}
