package com.example.ui.screens.index

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.toAppDigits
import com.example.domain.quran.ConcordanceScopeFilter
import com.example.domain.quran.QuranConcordanceReport
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

@Composable
internal fun ConcordanceMakkiMadaniSection(
    report: QuranConcordanceReport,
    scopeFilter: ConcordanceScopeFilter,
    onSelectScope: (ConcordanceScopeFilter) -> Unit
) {
    val qc = MaterialTheme.quranColors
    val totalOcc = report.totalOccurrences.coerceAtLeast(1)
    val makkiFraction = (report.makkiOccurrences.toFloat() / totalOcc.toFloat()).coerceIn(0f, 1f)
    val madaniFraction = (report.madaniOccurrences.toFloat() / totalOcc.toFloat()).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "التوزيع المكي والمدني",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "مكي ${report.makkiSharePercent.toAppDigits()}٪ • مدني ${(100 - report.makkiSharePercent).toAppDigits()}٪",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (makkiFraction > 0f) {
                Box(
                    modifier = Modifier
                        .weight(makkiFraction.coerceAtLeast(0.02f))
                        .fillMaxHeight()
                        .background(qc.primary)
                )
            }
            if (madaniFraction > 0f) {
                Box(
                    modifier = Modifier
                        .weight(madaniFraction.coerceAtLeast(0.02f))
                        .fillMaxHeight()
                        .background(qc.accent)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ConcordanceScopeFilter.entries.forEach { scope ->
                val selected = scope == scopeFilter
                val label = when (scope) {
                    ConcordanceScopeFilter.ALL -> "الكل (${report.totalMatchingAyahs.toAppDigits()})"
                    ConcordanceScopeFilter.MAKKI -> "المكي (${report.makkiAyahs.toAppDigits()})"
                    ConcordanceScopeFilter.MADANI -> "المدني (${report.madaniAyahs.toAppDigits()})"
                }
                FilterChip(
                    selected = selected,
                    onClick = { onSelectScope(scope) },
                    shape = RoundedCornerShape(50),
                    colors = quranFilterChipColors(),
                    border = quranFilterChipBorder(selected),
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    },
                    modifier = Modifier.testTag("concordance_scope_${scope.name.lowercase()}")
                )
            }
        }
    }
}

@Composable
internal fun ConcordanceJuzChartSection(
    juzCounts: List<Int>,
    selectedJuzFilter: Int?,
    onToggleJuzFilter: (Int) -> Unit
) {
    val maxCount = (juzCounts.maxOrNull() ?: 0).coerceAtLeast(1)
    val peakIndex = juzCounts.indices.maxByOrNull { juzCounts[it] } ?: 0
    val peakCount = juzCounts.getOrElse(peakIndex) { 0 }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "التوزيع عبر الأجزاء (١ – ٣٠)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (peakCount > 0) {
                Text(
                    text = "الذروة: ج ${(peakIndex + 1).toAppDigits()} (${peakCount.toAppDigits()})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("concordance_juz_chart")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    juzCounts.forEachIndexed { idx, count ->
                        val juzNum = idx + 1
                        val isSelected = selectedJuzFilter == juzNum
                        val heightFraction = if (count == 0) 0.1f else (0.22f + 0.78f * (count.toFloat() / maxCount.toFloat()))
                        val barColor = when {
                            isSelected -> MaterialTheme.colorScheme.tertiary
                            count == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                            idx == peakIndex -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.58f)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(enabled = count > 0) { onToggleJuzFilter(juzNum) }
                                .testTag("concordance_juz_bar_$juzNum"),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(heightFraction)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(barColor)
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ج ${1.toAppDigits()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("ج ${15.toAppDigits()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("ج ${30.toAppDigits()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
