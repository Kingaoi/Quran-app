package com.example.domain.model

import androidx.compose.runtime.Immutable
import com.example.data.local.ReadingStats
import com.example.domain.prayer.SunnahRawatibCatalog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class StatsChartMetric(val labelAr: String, val unitAr: String) {
    QURAN_PAGES("صفحات المصحف", "صفحة"),
    ADHKAR("الأذكار", "ذكر"),
    TASBIH("التسبيح", "تسبيحة")
}

@Immutable
data class StatsDayPoint(
    val epochDay: Long,
    val shortWeekdayAr: String,
    val dateLabelAr: String,
    val pages: Int,
    val adhkar: Int,
    val tasbih: Int
) {
    fun valueFor(metric: StatsChartMetric): Int = when (metric) {
        StatsChartMetric.QURAN_PAGES -> pages
        StatsChartMetric.ADHKAR -> adhkar
        StatsChartMetric.TASBIH -> tasbih
    }
}

@Immutable
data class HeatmapCell(
    val epochDay: Long,
    val dateLabelAr: String,
    val pages: Int,
    val adhkar: Int,
    val tasbih: Int,
    val intensityLevel: Int
)

@Immutable
data class StatsAchievementBadge(
    val id: String,
    val titleAr: String,
    val requirementAr: String,
    val currentValue: Int,
    val targetValue: Int,
    val progressFraction: Float,
    val isUnlocked: Boolean,
    val accentIndex: Int
)

@Immutable
data class AdvancedStatsSnapshot(
    val todayEpochDay: Long,
    val todayPages: Int,
    val dailyGoalPages: Int,
    val quranGoalFraction: Float,
    val adhkarTodayRatio: Float,
    val tasbihTodayRatio: Float,
    val rawatibRakatToday: Int,
    val rawatibRatio: Float,
    val spiritualTodayRatio: Float,
    val overallDailyScorePercent: Int,
    val weeklyPages: Int,
    val previousWeeklyPages: Int,
    val weeklyTrendPercent: Int?,
    val monthlyPages: Int,
    val dailyAverage30d: Float,
    val bestDay30d: StatsDayPoint?,
    val projectedDaysPerKhatmah: Int?,
    val khatmahEquivalentProgress: Float,
    val days7: List<StatsDayPoint>,
    val days30: List<StatsDayPoint>,
    val heatmap84: List<HeatmapCell>,
    val activeDaysLast84: Int,
    val badges: List<StatsAchievementBadge>
)

object StatsAnalyticsCalculator {
    private val arabicLocale = Locale.forLanguageTag("ar")

    fun compute(stats: ReadingStats, todayEpochDay: Long): AdvancedStatsSnapshot {
        val goal = stats.dailyGoalPages.coerceAtLeast(1)
        val todayPages = stats.recentDailyPages[todayEpochDay] ?: 0
        val quranRatio = (todayPages.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
        val adhkarRatio = (stats.adhkarTodayCount.toFloat() / 10f).coerceIn(0f, 1f)
        val tasbihRatio = (stats.tasbihTodayCount.toFloat() / 100f).coerceIn(0f, 1f)
        val spiritualRatio = ((adhkarRatio + tasbihRatio) / 2f).coerceIn(0f, 1f)
        val rawatibRakat = SunnahRawatibCatalog.completedConfirmedRakatCount(stats.rawatibCompletedIdsToday)
        val rawatibRatio = (rawatibRakat.toFloat() / 12f).coerceIn(0f, 1f)
        val overallPct = ((quranRatio * 0.45f + spiritualRatio * 0.30f + rawatibRatio * 0.25f) * 100f)
            .toInt().coerceIn(0, 100)

        val days30 = buildDayRange(stats, todayEpochDay, 30)
        val days7 = days30.takeLast(7)
        val weeklyPages = days7.sumOf { it.pages }
        val prevWeeklyPages = (7..13).sumOf { d -> stats.recentDailyPages[todayEpochDay - d] ?: 0 }
        val trendPercent = when {
            prevWeeklyPages > 0 -> (((weeklyPages - prevWeeklyPages) * 100f) / prevWeeklyPages).toInt()
            weeklyPages > 0 -> 100
            else -> null
        }
        val monthlyPages = days30.sumOf { it.pages }
        val avg30 = monthlyPages / 30f
        val bestDay = days30.filter { it.pages > 0 }.maxByOrNull { it.pages }
        val weeklyPacePerDay = weeklyPages / 7f
        val projectedDays = if (weeklyPacePerDay >= 0.3f) (604f / weeklyPacePerDay).toInt().coerceIn(1, 999) else null
        val khatmahEq = (stats.totalPagesRead % 604).toFloat() / 604f

        val heatmap84 = buildHeatmap84(stats, todayEpochDay)
        val activeDays84 = heatmap84.count { it.intensityLevel > 0 }
        val badges = buildBadges(stats, rawatibRakat)

        return AdvancedStatsSnapshot(
            todayEpochDay = todayEpochDay,
            todayPages = todayPages,
            dailyGoalPages = goal,
            quranGoalFraction = quranRatio,
            adhkarTodayRatio = adhkarRatio,
            tasbihTodayRatio = tasbihRatio,
            rawatibRakatToday = rawatibRakat,
            rawatibRatio = rawatibRatio,
            spiritualTodayRatio = spiritualRatio,
            overallDailyScorePercent = overallPct,
            weeklyPages = weeklyPages,
            previousWeeklyPages = prevWeeklyPages,
            weeklyTrendPercent = trendPercent,
            monthlyPages = monthlyPages,
            dailyAverage30d = avg30,
            bestDay30d = bestDay,
            projectedDaysPerKhatmah = projectedDays,
            khatmahEquivalentProgress = khatmahEq,
            days7 = days7,
            days30 = days30,
            heatmap84 = heatmap84,
            activeDaysLast84 = activeDays84,
            badges = badges
        )
    }

    private fun buildDayRange(stats: ReadingStats, todayEpochDay: Long, count: Int): List<StatsDayPoint> {
        val dateFmt = SimpleDateFormat("d MMM", arabicLocale)
        return ((count - 1) downTo 0).map { delta ->
            val day = todayEpochDay - delta
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -delta) }
            val pages = stats.recentDailyPages[day] ?: 0
            val adhkar = stats.recentDailyAdhkar[day] ?: if (delta == 0) stats.adhkarTodayCount else 0
            val tasbih = stats.recentDailyTasbih[day] ?: if (delta == 0) stats.tasbihTodayCount else 0
            StatsDayPoint(
                epochDay = day,
                shortWeekdayAr = shortWeekday(cal.get(Calendar.DAY_OF_WEEK)),
                dateLabelAr = dateFmt.format(cal.time),
                pages = pages,
                adhkar = adhkar,
                tasbih = tasbih
            )
        }
    }

    private fun buildHeatmap84(stats: ReadingStats, todayEpochDay: Long): List<HeatmapCell> {
        val dateFmt = SimpleDateFormat("d MMM", arabicLocale)
        val goal = stats.dailyGoalPages.coerceAtLeast(1)
        return (83 downTo 0).map { delta ->
            val day = todayEpochDay - delta
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -delta) }
            val pages = stats.recentDailyPages[day] ?: 0
            val adhkar = stats.recentDailyAdhkar[day] ?: if (delta == 0) stats.adhkarTodayCount else 0
            val tasbih = stats.recentDailyTasbih[day] ?: if (delta == 0) stats.tasbihTodayCount else 0
            val weighted = pages.toFloat() / goal + (adhkar / 8f) * 0.35f + (tasbih / 80f) * 0.25f
            val level = when {
                pages == 0 && adhkar == 0 && tasbih == 0 -> 0
                weighted >= 1.15f -> 4
                weighted >= 0.70f -> 3
                weighted >= 0.30f -> 2
                else -> 1
            }
            HeatmapCell(day, dateFmt.format(cal.time), pages, adhkar, tasbih, level)
        }
    }

    private fun buildBadges(stats: ReadingStats, rawatibRakat: Int): List<StatsAchievementBadge> = listOf(
        badge("streak_7", "سلسلة ٧ أيام", "قراءة متواصلة لأسبوع", stats.longestStreakDays, 7, 0),
        badge("pages_100", "قارئ المئة", "قراءة ١٠٠ صفحة في المصحف", stats.totalPagesRead, 100, 1),
        badge("khatmah_1", "حافظ الختمة", "إتمام ختمة كاملة", stats.completedKhatmahsCount, 1, 3),
        badge("tasbih_500", "ذاكرٌ كثيراً", "٥٠٠ تسبيحة مسجّلة", stats.tasbihTotalCount.toInt(), 500, 2),
        badge("rawatib_12", "أهل السنن", "إتمام ١٢ ركعة راتبة اليوم", rawatibRakat, 12, 4),
        badge("streak_30", "همّة الشهر", "سلسلة ٣٠ يوماً متصلة", stats.longestStreakDays, 30, 5)
    )

    private fun badge(id: String, title: String, req: String, current: Int, target: Int, accentIdx: Int): StatsAchievementBadge {
        val rawProgress = if (target > 0) current.toFloat() / target.toFloat() else 0f
        return StatsAchievementBadge(
            id = id, titleAr = title, requirementAr = req,
            currentValue = current.coerceAtLeast(0),
            targetValue = target.coerceAtLeast(1),
            progressFraction = rawProgress.coerceIn(0f, 1f),
            isUnlocked = rawProgress >= 1f, accentIndex = accentIdx
        )
    }

    private fun shortWeekday(dayOfWeek: Int): String = when (dayOfWeek) {
        Calendar.SUNDAY -> "ح"
        Calendar.MONDAY -> "ن"
        Calendar.TUESDAY -> "ث"
        Calendar.WEDNESDAY -> "ر"
        Calendar.THURSDAY -> "خ"
        Calendar.FRIDAY -> "ج"
        else -> "س"
    }
}
