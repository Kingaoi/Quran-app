package com.example.ui.screens.index

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.audio.AudioPlaybackState
import com.example.domain.model.SurahInfo
import com.example.domain.model.toAppDigits
import com.example.domain.quran.SurahDetailedCatalog
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

internal enum class SurahRevelationFilter(val labelAr: String) {
    ALL("الكل"),
    MAKKI("مكية"),
    MADANI("مدنية")
}

internal enum class SurahSortOption(val labelAr: String) {
    MUSHAF("ترتيب المصحف"),
    REVELATION("ترتيب النزول"),
    MOST_AYAHS("الأكثر آيات"),
    FEWEST_AYAHS("الأقل آيات")
}

internal fun sortSurahs(
    surahs: List<SurahInfo>,
    sortOption: SurahSortOption
): List<SurahInfo> = when (sortOption) {
    SurahSortOption.MUSHAF -> surahs.sortedBy { it.number }
    SurahSortOption.REVELATION -> surahs.sortedBy { SurahDetailedCatalog.getProfile(it.number).revelationOrder }
    SurahSortOption.MOST_AYAHS -> surahs.sortedWith(compareByDescending<SurahInfo> { it.ayahCount }.thenBy { it.number })
    SurahSortOption.FEWEST_AYAHS -> surahs.sortedWith(compareBy<SurahInfo> { it.ayahCount }.thenBy { it.number })
}

internal fun LazyListScope.indexSurahsSection(
    surahs: List<SurahInfo>,
    selectedFilter: SurahRevelationFilter,
    selectedSort: SurahSortOption,
    playbackState: AudioPlaybackState,
    onSelectFilter: (SurahRevelationFilter) -> Unit,
    onOpenSurahPage: (Int) -> Unit,
    onTogglePlaySurah: (Int) -> Unit,
    onShowSurahInfo: (SurahInfo) -> Unit
) {
    val makkiCount = surahs.count { it.isMakki }
    val madaniCount = surahs.size - makkiCount

    item(key = "surahs_filter_row") {
        SurahRevelationFilterRow(
            totalCount = surahs.size,
            makkiCount = makkiCount,
            madaniCount = madaniCount,
            selectedFilter = selectedFilter,
            onSelectFilter = onSelectFilter
        )
    }

    val filtered = when (selectedFilter) {
        SurahRevelationFilter.ALL -> surahs
        SurahRevelationFilter.MAKKI -> surahs.filter { it.isMakki }
        SurahRevelationFilter.MADANI -> surahs.filter { !it.isMakki }
    }
    val visibleSurahs = sortSurahs(filtered, selectedSort)

    items(visibleSurahs, key = { "direct_surah_${it.number}" }) { surah ->
        val isActiveSurah = playbackState.isActive && playbackState.currentSurah == surah.number
        val revOrder = if (selectedSort == SurahSortOption.REVELATION) {
            SurahDetailedCatalog.getProfile(surah.number).revelationOrder
        } else {
            null
        }
        SurahDirectCard(
            surah = surah,
            revelationOrder = revOrder,
            isActiveSurah = isActiveSurah,
            isPlayingSurah = isActiveSurah && playbackState.isPlaying,
            onOpenSurah = { onOpenSurahPage(surah.firstPage) },
            onTogglePlaySurah = { onTogglePlaySurah(surah.number) },
            onShowInfo = { onShowSurahInfo(surah) }
        )
    }
}

@Composable
private fun SurahRevelationFilterRow(
    totalCount: Int,
    makkiCount: Int,
    madaniCount: Int,
    selectedFilter: SurahRevelationFilter,
    onSelectFilter: (SurahRevelationFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SurahRevelationFilter.entries.forEach { filter ->
            val count = when (filter) {
                SurahRevelationFilter.ALL -> totalCount
                SurahRevelationFilter.MAKKI -> makkiCount
                SurahRevelationFilter.MADANI -> madaniCount
            }
            val selected = filter == selectedFilter
            FilterChip(
                selected = selected,
                onClick = { onSelectFilter(filter) },
                shape = RoundedCornerShape(50),
                colors = quranFilterChipColors(),
                border = quranFilterChipBorder(selected),
                label = {
                    Text(
                        text = "${filter.labelAr} (${count.toAppDigits()})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                },
                modifier = Modifier.testTag("surah_filter_${filter.name.lowercase()}")
            )
        }
    }
}

@Composable
internal fun SurahSortCornerButton(
    selectedSort: SurahSortOption,
    onSelectSort: (SurahSortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val isNonDefaultSort = selectedSort != SurahSortOption.MUSHAF

    Box(modifier = modifier) {
        Surface(
            onClick = { sortMenuExpanded = true },
            shape = RoundedCornerShape(50),
            color = if (isNonDefaultSort) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
            border = BorderStroke(
                1.dp,
                if (isNonDefaultSort) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("surah_sort_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Sort,
                    contentDescription = "فرز السور",
                    tint = if (isNonDefaultSort) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = selectedSort.labelAr,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isNonDefaultSort) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        DropdownMenu(
            expanded = sortMenuExpanded,
            onDismissRequest = { sortMenuExpanded = false }
        ) {
            SurahSortOption.entries.forEach { option ->
                val isSelected = option == selectedSort
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.labelAr,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    trailingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        onSelectSort(option)
                        sortMenuExpanded = false
                    },
                    modifier = Modifier.testTag("surah_sort_option_${option.name.lowercase()}")
                )
            }
        }
    }
}
