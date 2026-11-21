package com.example.ui.screens.index

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import com.example.domain.model.HizbQuarterInfo
import com.example.domain.model.toAppDigits
import com.example.ui.components.RubElHizbBadge
import com.example.ui.theme.LocalQuranFontFamily
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

internal enum class HizbRangeFilter(val labelAr: String, val range: IntRange) {
    ALL("الكل", 1..60),
    R1("١–١٥", 1..15),
    R2("١٦–٣٠", 16..30),
    R3("٣١–٤٥", 31..45),
    R4("٤٦–٦٠", 46..60)
}

internal fun LazyListScope.indexAhzabSection(
    quartersByHizb: Map<Int, List<HizbQuarterInfo>>,
    selectedRange: HizbRangeFilter,
    expandedHizbMap: MutableMap<Int, Boolean>,
    onSelectRange: (HizbRangeFilter) -> Unit,
    onOpenPage: (Int) -> Unit
) {
    item(key = "ahzab_range_filters") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HizbRangeFilter.entries.forEach { filter ->
                val selected = filter == selectedRange
                FilterChip(
                    selected = selected,
                    onClick = { onSelectRange(filter) },
                    shape = RoundedCornerShape(50),
                    colors = quranFilterChipColors(),
                    border = quranFilterChipBorder(selected),
                    label = {
                        Text(
                            text = filter.labelAr,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    modifier = Modifier.testTag("hizb_filter_${filter.name.lowercase()}")
                )
            }
        }
    }

    val visibleHizbs = quartersByHizb.keys
        .filter { it in selectedRange.range }
        .sorted()

    items(visibleHizbs, key = { "hizb_item_$it" }) { hizbNum ->
        val quarters = quartersByHizb[hizbNum].orEmpty()
        val isExpanded = expandedHizbMap[hizbNum] == true
        HizbGroupCard(
            hizbNumber = hizbNum,
            quarters = quarters,
            isExpanded = isExpanded,
            onToggleExpand = { expandedHizbMap[hizbNum] = !isExpanded },
            onOpenPage = onOpenPage
        )
    }
}

@Composable
private fun HizbGroupCard(
    hizbNumber: Int,
    quarters: List<HizbQuarterInfo>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpenPage: (Int) -> Unit
) {
    val first = quarters.firstOrNull() ?: return
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hizb_accordion_$hizbNumber")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RubElHizbBadge(
                    number = hizbNumber,
                    size = 38.dp,
                    strokeColor = MaterialTheme.colorScheme.primary,
                    fillColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    textColor = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الحزب ${hizbNumber.toAppDigits()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "الجزء ${first.juzNumber.toAppDigits()} • يبدأ في سورة ${first.surahNameAr} • ص ${first.pageNumber.toAppDigits()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "طي الحزب" else "توسيع الحزب",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    quarters.forEachIndexed { idx, q ->
                        val quarterTitle = when (q.quarterInHizb) {
                            0 -> "بداية الحزب ${q.hizbNumber.toAppDigits()}"
                            1 -> "الربع الأول"
                            2 -> "نصف الحزب"
                            else -> "ثلاثة أرباع الحزب"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPage(q.pageNumber) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("hizb_quarter_${q.rubNumber}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
                            ) {
                                Text(
                                    text = q.badgeLabelAr,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = q.openingText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = LocalQuranFontFamily.current,
                                        fontSize = 17.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$quarterTitle • ${q.surahNameAr} (${q.ayahNumber.toAppDigits()}) • ص ${q.pageNumber.toAppDigits()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                contentDescription = "فتح الربع",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (idx < quarters.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
