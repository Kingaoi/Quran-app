package com.example.ui.screens.wird

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ReadingStats
import com.example.data.local.WirdPreferencesRepository
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.StatsChartMetric
import com.example.domain.model.toAppDigits
import com.example.domain.model.StatsAnalyticsCalculator
import com.example.domain.wird.WirdPlanSummary
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.quranColors
import com.example.ui.util.ShareCardGenerator

private enum class StatsScopeTab(val titleAr: String) {
    ALL("الكل"),
    QURAN("القرآن"),
    HABITS("العبادات")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WirdStatsBottomSheet(
    stats: ReadingStats,
    activeWirdPlan: WirdPlanSummary? = null,
    onSetDailyGoalPages: (Int) -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activeTheme = LocalAppTheme.current
    val qc = MaterialTheme.quranColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val useWestern = LocalUseWesternDigits.current
    var scopeTab by remember { mutableStateOf(StatsScopeTab.ALL) }

    val todayEpoch = remember { WirdPreferencesRepository.currentLocalEpochDay() }
    val snapshot = remember(stats, todayEpoch) { StatsAnalyticsCalculator.compute(stats, todayEpoch) }
    val goalProgress by animateFloatAsState(targetValue = snapshot.quranGoalFraction, label = "daily_goal_progress")

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = qc.bg) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp)
                .testTag("wird_stats_sheet"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(qc.primary.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.BarChart, contentDescription = null, tint = qc.primary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "لوحة الإحصاءات",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = qc.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "آخر ٧ أيام: ${snapshot.weeklyPages.toAppDigits(useWestern)} ص • التوازن: ${snapshot.overallDailyScorePercent.toAppDigits(useWestern)}٪",
                            style = MaterialTheme.typography.bodySmall,
                            color = qc.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(
                        onClick = {
                            val summary = buildString {
                                append("السلسلة الحالية: ${stats.currentStreakDays.toAppDigits(useWestern)} يوماً\n")
                                append("إجمالي القراءة: ${stats.totalPagesRead.toAppDigits(useWestern)} صفحة\n")
                                append("التوازن اليومي: ${snapshot.overallDailyScorePercent.toAppDigits(useWestern)}٪ • الختمات: ${stats.completedKhatmahsCount.toAppDigits(useWestern)}")
                            }
                            ShareCardGenerator.shareStyledCard(
                                context = context,
                                headerLabel = "إنجازي القرآني",
                                bodyText = summary,
                                footerLabel = "رفيق القرآن والأذكار",
                                appTheme = activeTheme
                            )
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = qc.surface2, contentColor = qc.primary),
                        modifier = Modifier.size(48.dp).testTag("wird_stats_share_button")
                    ) { Icon(Icons.Rounded.Share, contentDescription = "مشاركة بطاقة الإنجاز") }

                    FilledTonalIconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = qc.surface2, contentColor = qc.text),
                        modifier = Modifier.size(48.dp).testTag("wird_stats_close_button")
                    ) { Icon(Icons.Rounded.Close, contentDescription = "إغلاق الإحصائيات") }
                }
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                StatsScopeTab.entries.forEachIndexed { idx, tab ->
                    SegmentedButton(
                        selected = scopeTab == tab,
                        onClick = { scopeTab = tab },
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = StatsScopeTab.entries.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = qc.selected,
                            activeContentColor = qc.onSelected,
                            inactiveContainerColor = qc.surface,
                            inactiveContentColor = qc.textMuted
                        ),
                        modifier = Modifier.testTag("stats_scope_tab_${tab.name.lowercase()}")
                    ) {
                        Text(
                            text = tab.titleAr,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            val suggestedMetric = when (scopeTab) {
                StatsScopeTab.ALL -> null
                StatsScopeTab.QURAN -> StatsChartMetric.QURAN_PAGES
                StatsScopeTab.HABITS -> StatsChartMetric.ADHKAR
            }

            Column(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (scopeTab) {
                    StatsScopeTab.ALL -> {
                        WirdStatsCard(stats = stats)
                        WirdStatsHeroRingsCard(snapshot = snapshot, activeWirdPlan = activeWirdPlan)
                        WirdStatsChartCard(snapshot = snapshot, suggestedMetric = suggestedMetric)
                        DailyReadingGoalCard(
                            todayPages = snapshot.todayPages,
                            goalTarget = snapshot.dailyGoalPages,
                            goalProgress = goalProgress,
                            useWestern = useWestern,
                            onSetDailyGoalPages = onSetDailyGoalPages
                        )
                        WirdPaceInsightsCard(snapshot = snapshot, totalPagesRead = stats.totalPagesRead, useWestern = useWestern)
                        WirdStatsHeatmapCard(snapshot = snapshot)
                        WirdStatsMilestonesCard(snapshot = snapshot)
                        StatsTasbihStripCard(stats = stats, useWestern = useWestern)
                    }
                    StatsScopeTab.QURAN -> {
                        WirdStatsCard(stats = stats)
                        WirdStatsChartCard(snapshot = snapshot, suggestedMetric = suggestedMetric)
                        DailyReadingGoalCard(
                            todayPages = snapshot.todayPages,
                            goalTarget = snapshot.dailyGoalPages,
                            goalProgress = goalProgress,
                            useWestern = useWestern,
                            onSetDailyGoalPages = onSetDailyGoalPages
                        )
                        WirdPaceInsightsCard(snapshot = snapshot, totalPagesRead = stats.totalPagesRead, useWestern = useWestern)
                        WirdStatsHeatmapCard(snapshot = snapshot)
                    }
                    StatsScopeTab.HABITS -> {
                        WirdStatsHeroRingsCard(snapshot = snapshot, activeWirdPlan = activeWirdPlan)
                        StatsHabitsSummaryCard(stats = stats, snapshot = snapshot, useWestern = useWestern)
                        WirdStatsChartCard(snapshot = snapshot, suggestedMetric = suggestedMetric)
                        WirdStatsMilestonesCard(snapshot = snapshot)
                        StatsTasbihStripCard(stats = stats, useWestern = useWestern)
                    }
                }
            }
        }
    }
}
