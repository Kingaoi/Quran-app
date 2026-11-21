package com.example.ui.screens.index

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.toAppDigits
import com.example.domain.quran.QuranConcordanceReport
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ConcordanceTopSurahsAndFormsSection(
    report: QuranConcordanceReport,
    selectedSurahFilter: Int?,
    selectedFormFilter: String?,
    onToggleSurahFilter: (Int) -> Unit,
    onToggleFormFilter: (String) -> Unit
) {
    if (report.topSurahs.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "أكثر السور ذكراً (اضغط للفلترة)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                report.topSurahs.take(6).forEach { sStat ->
                    val isSelected = selectedSurahFilter == sStat.surahNumber
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggleSurahFilter(sStat.surahNumber) },
                        shape = RoundedCornerShape(50),
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(isSelected),
                        label = {
                            Text(
                                text = "${sStat.surahNameAr} (${sStat.occurrenceCount.toAppDigits()})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        modifier = Modifier.testTag("concordance_top_surah_${sStat.surahNumber}")
                    )
                }
            }
        }
    }

    if (report.derivedForms.size > 1) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "الصيغ والمشتقات الواردة (${report.derivedForms.size.toAppDigits()})",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                report.derivedForms.take(12).forEachIndexed { idx, formStat ->
                    val isSelected = selectedFormFilter == formStat.form
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggleFormFilter(formStat.form) },
                        shape = RoundedCornerShape(50),
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(isSelected),
                        label = {
                            Text(
                                text = "${formStat.form} (${formStat.occurrenceCount.toAppDigits()})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        modifier = Modifier.testTag("concordance_form_chip_$idx")
                    )
                }
            }
        }
    }
}

@Composable
internal fun ConcordanceKpiTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun QuickJumpOccurrenceChip(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
