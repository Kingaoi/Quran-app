package com.example.ui.screens.index

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.quran.QuranRootLexicon
import com.example.domain.quran.QuranSearchMode
import com.example.domain.quran.QuranSemanticLexicon
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun IndexSearchModeSelector(
    selectedMode: QuranSearchMode,
    onSelectMode: (QuranSearchMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("search_mode_segmented_row")
    ) {
        QuranSearchMode.entries.forEachIndexed { index, mode ->
            val selected = mode == selectedMode
            val icon = when (mode) {
                QuranSearchMode.WORD -> Icons.Rounded.TextFields
                QuranSearchMode.ROOT -> Icons.Rounded.AccountTree
                QuranSearchMode.MEANING -> Icons.Rounded.Lightbulb
            }
            SegmentedButton(
                selected = selected,
                onClick = { onSelectMode(mode) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = QuranSearchMode.entries.size
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = qc.selected,
                    activeContentColor = qc.onSelected,
                    activeBorderColor = qc.selected,
                    inactiveContainerColor = qc.surface,
                    inactiveContentColor = qc.textMuted,
                    inactiveBorderColor = qc.border
                ),
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = {
                    Text(
                        text = mode.labelAr,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("search_mode_${mode.name.lowercase()}")
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun IndexSearchDiscoveryCard(
    mode: QuranSearchMode,
    onSelectSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions: List<Pair<String, String>> = when (mode) {
        QuranSearchMode.ROOT -> QuranRootLexicon.popularRoots
        QuranSearchMode.MEANING -> QuranSemanticLexicon.popularMeanings.map { it to it }
        QuranSearchMode.WORD -> listOf(
            "الله" to "لفظ الجلالة",
            "الرحمن" to "الرحمن",
            "الصلاة" to "الصلاة",
            "الجنة" to "الجنة",
            "بسم الله" to "بسم الله",
            "لا إله إلا الله" to "التوحيد"
        )
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("search_discovery_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ManageSearch,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(19.dp)
                )
                Column {
                    Text(
                        text = when (mode) {
                            QuranSearchMode.ROOT -> "جذور قرآنية مقترحة"
                            QuranSearchMode.MEANING -> "مفاهيم ومعانٍ مقترحة"
                            QuranSearchMode.WORD -> "ألفاظ وعبارات شائعة"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = mode.descriptionAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                suggestions.forEachIndexed { idx, (queryText, caption) ->
                    FilterChip(
                        selected = false,
                        onClick = { onSelectSuggestion(queryText) },
                        shape = RoundedCornerShape(50),
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(false),
                        label = {
                            Text(
                                text = if (mode == QuranSearchMode.ROOT) "$queryText • $caption" else queryText,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        },
                        modifier = Modifier.testTag("search_suggestion_chip_${mode.name.lowercase()}_$idx")
                    )
                }
            }
        }
    }
}

@Composable
internal fun IndexSearchControlsPanel(
    query: String,
    searchMode: QuranSearchMode,
    onQueryChange: (String) -> Unit,
    onSelectMode: (QuranSearchMode) -> Unit,
    onSearchSubmit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        IndexSearchField(
            visible = true,
            query = query,
            placeholder = searchMode.placeholderAr,
            onQueryChange = onQueryChange,
            onSearchSubmit = onSearchSubmit
        )
        IndexSearchModeSelector(
            selectedMode = searchMode,
            onSelectMode = onSelectMode
        )
    }
}

