package com.example.ui.screens.index

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FilterAltOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.localizeDigits
import com.example.domain.model.toAppDigits
import com.example.domain.quran.ConcordanceHit
import com.example.domain.quran.ConcordanceScopeFilter
import com.example.domain.quran.QuranConcordanceReport
import com.example.domain.quran.QuranRootLexicon
import com.example.domain.quran.QuranSearchMode
import com.example.ui.theme.quranColors
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun IndexConcordanceStatsCard(
    report: QuranConcordanceReport,
    scopeFilter: ConcordanceScopeFilter,
    selectedSurahFilter: Int?,
    selectedJuzFilter: Int?,
    selectedFormFilter: String?,
    onSelectScope: (ConcordanceScopeFilter) -> Unit,
    onToggleSurahFilter: (Int) -> Unit,
    onToggleJuzFilter: (Int) -> Unit,
    onToggleFormFilter: (String) -> Unit,
    onClearSubFilters: () -> Unit,
    onOpenHit: (ConcordanceHit) -> Unit,
    modifier: Modifier = Modifier
) {
    if (report.totalMatchingAyahs == 0) return
    val qc = MaterialTheme.quranColors
    var expanded by rememberSaveable(report.query, report.mode) { mutableStateOf(true) }
    val hasActiveSubFilter = scopeFilter != ConcordanceScopeFilter.ALL ||
        selectedSurahFilter != null || selectedJuzFilter != null || selectedFormFilter != null

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("concordance_stats_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .testTag("concordance_stats_toggle"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Column {
                        Text(
                            text = when (report.mode) {
                                QuranSearchMode.ROOT -> {
                                    val label = report.resolvedRoot?.let { r ->
                                        QuranRootLexicon.specByRoot[QuranRootLexicon.normalizeToken(r)]?.displayLabel
                                    }
                                    if (label != null) "إحصاء الجذر (${report.resolvedRoot}) • $label"
                                    else "إحصاء الجذر (${report.resolvedRoot.orEmpty()})"
                                }
                                QuranSearchMode.MEANING -> "الإحصاء الدلالي للمعنى"
                                QuranSearchMode.WORD -> "العدّ الإحصائي للفظ"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val pct = String.format(Locale.US, "%.1f", report.percentOfQuranAyahs).localizeDigits()
                        Text(
                            text = "${report.totalOccurrences.toAppDigits()} تكرار في ${report.totalMatchingAyahs.toAppDigits()} آية ($pct٪ من المصحف)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "طي الإحصاء" else "توسيع الإحصاء",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConcordanceKpiTile(
                    label = "التكرار الكلي",
                    value = report.totalOccurrences.toAppDigits(),
                    modifier = Modifier.weight(1f).testTag("concordance_kpi_occurrences")
                )
                ConcordanceKpiTile(
                    label = "عدد الآيات",
                    value = report.totalMatchingAyahs.toAppDigits(),
                    modifier = Modifier.weight(1f).testTag("concordance_kpi_ayahs")
                )
                ConcordanceKpiTile(
                    label = "السور",
                    value = "${report.totalMatchingSurahs.toAppDigits()}/${114.toAppDigits()}",
                    modifier = Modifier.weight(1f).testTag("concordance_kpi_surahs")
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (report.matchedConcepts.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "المفاهيم القرآنية المطابقة",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                report.matchedConcepts.forEach { concept ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                                    ) {
                                        Text(
                                            text = concept.titleAr,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    ConcordanceMakkiMadaniSection(
                        report = report,
                        scopeFilter = scopeFilter,
                        onSelectScope = onSelectScope
                    )
                    ConcordanceJuzChartSection(
                        juzCounts = report.juzCounts,
                        selectedJuzFilter = selectedJuzFilter,
                        onToggleJuzFilter = onToggleJuzFilter
                    )
                    ConcordanceTopSurahsAndFormsSection(
                        report = report,
                        selectedSurahFilter = selectedSurahFilter,
                        selectedFormFilter = selectedFormFilter,
                        onToggleSurahFilter = onToggleSurahFilter,
                        onToggleFormFilter = onToggleFormFilter
                    )

                    val firstHit = report.firstOccurrence
                    val lastHit = report.lastOccurrence
                    if (firstHit != null && lastHit != null && (firstHit.surahNumber != lastHit.surahNumber || firstHit.ayahNumber != lastHit.ayahNumber)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickJumpOccurrenceChip(
                                title = "أول موضع",
                                subtitle = "${firstHit.surahNameAr} ${firstHit.ayahNumber.toAppDigits()}",
                                onClick = { onOpenHit(firstHit) },
                                modifier = Modifier.weight(1f)
                            )
                            QuickJumpOccurrenceChip(
                                title = "آخر موضع",
                                subtitle = "${lastHit.surahNameAr} ${lastHit.ayahNumber.toAppDigits()}",
                                onClick = { onOpenHit(lastHit) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (hasActiveSubFilter) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildString {
                            append("النتائج مصفاة: ")
                            val parts = mutableListOf<String>()
                            if (scopeFilter != ConcordanceScopeFilter.ALL) parts.add(scopeFilter.labelAr)
                            if (selectedSurahFilter != null) {
                                report.topSurahs.firstOrNull { it.surahNumber == selectedSurahFilter }?.let { parts.add("سورة ${it.surahNameAr}") }
                            }
                            if (selectedJuzFilter != null) parts.add("الجزء ${selectedJuzFilter.toAppDigits()}")
                            if (selectedFormFilter != null) parts.add("صيغة: $selectedFormFilter")
                            append(parts.joinToString(" • "))
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = qc.primary,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = onClearSubFilters,
                        modifier = Modifier.testTag("concordance_clear_filters_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FilterAltOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إظهار الكل", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
