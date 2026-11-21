package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ReadingStats
import com.example.domain.model.AdvancedStatsSnapshot
import com.example.domain.model.localizeDigits
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranColors
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DailyReadingGoalCard(
    todayPages: Int,
    goalTarget: Int,
    goalProgress: Float,
    useWestern: Boolean,
    onSetDailyGoalPages: (Int) -> Unit
) {
    val qc = MaterialTheme.quranColors
    val remaining = (goalTarget - todayPages).coerceAtLeast(0)
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = qc.surface,
        border = BorderStroke(1.dp, qc.border),
        modifier = Modifier.fillMaxWidth().testTag("daily_reading_goal_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Flag, contentDescription = null, tint = qc.primary, modifier = Modifier.size(18.dp))
                    Text("هدف القراءة اليومي", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = qc.text)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (todayPages >= goalTarget) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = "تم تحقيق الهدف", tint = qc.success, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "${todayPages.toAppDigits(useWestern)} / ${goalTarget.toAppDigits(useWestern)} صفحة",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (todayPages >= goalTarget) qc.success else qc.textMuted
                    )
                }
            }
            LinearProgressIndicator(
                progress = { goalProgress },
                color = if (todayPages >= goalTarget) qc.success else qc.primary,
                trackColor = qc.border.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp))
            )
            Text(
                text = if (remaining == 0) "ما شاء الله، أتممت هدفك اليومي بالكامل"
                else "متبقي ${remaining.toAppDigits(useWestern)} صفحة للوصول لهدف اليوم",
                style = MaterialTheme.typography.labelSmall,
                color = if (remaining == 0) qc.success else qc.textMuted
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    listOf(2, 4, 10, 20).forEach { goal ->
                        FilterChip(
                            selected = goalTarget == goal,
                            onClick = { onSetDailyGoalPages(goal) },
                            label = { Text("${goal.toAppDigits(useWestern)} ص", style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = qc.selected, selectedLabelColor = qc.onSelected),
                            modifier = Modifier.testTag("daily_goal_chip_$goal")
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(
                        onClick = { onSetDailyGoalPages((goalTarget - 1).coerceAtLeast(1)) },
                        enabled = goalTarget > 1,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = qc.surface2, contentColor = qc.primary),
                        modifier = Modifier.size(48.dp).testTag("daily_goal_minus")
                    ) { Icon(Icons.Rounded.Remove, contentDescription = "تقليل الهدف اليومي") }
                    FilledTonalIconButton(
                        onClick = { onSetDailyGoalPages((goalTarget + 1).coerceAtMost(60)) },
                        enabled = goalTarget < 60,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = qc.surface2, contentColor = qc.primary),
                        modifier = Modifier.size(48.dp).testTag("daily_goal_plus")
                    ) { Icon(Icons.Rounded.Add, contentDescription = "زيادة الهدف اليومي") }
                }
            }
        }
    }
}

@Composable
internal fun WirdPaceInsightsCard(
    snapshot: AdvancedStatsSnapshot,
    totalPagesRead: Int,
    useWestern: Boolean
) {
    val qc = MaterialTheme.quranColors
    val avg30Rounded = ((snapshot.dailyAverage30d * 10f).roundToInt() / 10f).toString().localizeDigits(useWestern)
    val bestText = snapshot.bestDay30d?.let { "${it.pages.toAppDigits(useWestern)} ص (${it.dateLabelAr})" } ?: "لا يوجد سجل بعد"
    val khatmahPct = (snapshot.khatmahEquivalentProgress * 100f).roundToInt().coerceIn(0, 100)
    val pagesIntoCycle = totalPagesRead % 604

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = qc.surface,
        border = BorderStroke(1.dp, qc.border),
        modifier = Modifier.fillMaxWidth().testTag("wird_pace_insights_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Insights, contentDescription = null, tint = qc.primary, modifier = Modifier.size(19.dp))
                Text("تحليل وتيرة التلاوة (٣٠ يوماً)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = qc.text)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaceStatBox("المعدل اليومي", "$avg30Rounded ص/يوم", Modifier.weight(1f))
                PaceStatBox("أعلى يوم", bestText, Modifier.weight(1f))
                PaceStatBox(
                    label = "ختمة بالوتيرة",
                    value = snapshot.projectedDaysPerKhatmah?.let { "~${it.toAppDigits(useWestern)} ي" } ?: "—",
                    modifier = Modifier.weight(1f)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("التقدم التراكمي نحو الختمة القادمة", style = MaterialTheme.typography.labelSmall, color = qc.textMuted)
                    Text(
                        text = "${pagesIntoCycle.toAppDigits(useWestern)} / ${604.toAppDigits(useWestern)} ص (${khatmahPct.toAppDigits(useWestern)}٪)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = qc.primary
                    )
                }
                LinearProgressIndicator(
                    progress = { snapshot.khatmahEquivalentProgress },
                    color = qc.primary,
                    trackColor = qc.border.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
private fun PaceStatBox(label: String, value: String, modifier: Modifier = Modifier) {
    val qc = MaterialTheme.quranColors
    Surface(shape = RoundedCornerShape(12.dp), color = qc.surface2.copy(alpha = 0.65f), modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = qc.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, style = MaterialTheme.typography.labelSmall, color = qc.textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun StatsHabitsSummaryCard(
    stats: ReadingStats,
    snapshot: AdvancedStatsSnapshot,
    useWestern: Boolean
) {
    val qc = MaterialTheme.quranColors
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = qc.surface,
        border = BorderStroke(1.dp, qc.border),
        modifier = Modifier.fillMaxWidth().testTag("stats_habits_summary_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.VolunteerActivism, contentDescription = null, tint = qc.accent, modifier = Modifier.size(19.dp))
                Text("ملخص العبادات والأذكار", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = qc.text)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaceStatBox("أذكار اليوم / الكلي", "${stats.adhkarTodayCount.toAppDigits(useWestern)} / ${stats.adhkarTotalCount.toAppDigits(useWestern)}", Modifier.weight(1f))
                PaceStatBox("التسبيح اليوم / الكلي", "${stats.tasbihTodayCount.toAppDigits(useWestern)} / ${stats.tasbihTotalCount.toInt().toAppDigits(useWestern)}", Modifier.weight(1f))
                PaceStatBox("السنن الرواتب", "${snapshot.rawatibRakatToday.toAppDigits(useWestern)}/${12.toAppDigits(useWestern)} ركعة", Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun StatsTasbihStripCard(stats: ReadingStats, useWestern: Boolean) {
    val qc = MaterialTheme.quranColors
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = qc.surface,
        border = BorderStroke(1.dp, qc.border),
        modifier = Modifier.fillMaxWidth().testTag("wird_stats_tasbih_strip")
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(qc.primary.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.TouchApp, contentDescription = null, tint = qc.primary, modifier = Modifier.size(18.dp))
                }
                Text("التسبيح اليوم: ${stats.tasbihTodayCount.toAppDigits(useWestern)}", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = qc.text)
            }
            Surface(shape = RoundedCornerShape(50), color = qc.surface2, border = BorderStroke(1.dp, qc.border)) {
                Text(
                    text = "الإجمالي: ${stats.tasbihTotalCount.toInt().toAppDigits(useWestern)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = qc.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}
