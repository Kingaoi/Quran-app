package com.example.ui.screens.index

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SurahInfo
import com.example.domain.model.toAppDigits
import com.example.domain.quran.SurahGroupsCatalog

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SurahGroupsDetailsSection(
    currentSurahNumber: Int,
    allSurahs: List<SurahInfo>,
    onSelectSurahNumber: (Int) -> Unit
) {
    val groups = remember(currentSurahNumber) {
        SurahGroupsCatalog.groupsForSurah(currentSurahNumber)
    }
    if (groups.isEmpty()) return

    val surahByNum = remember(allSurahs) { allSurahs.associateBy { it.number } }

    SurahSectionCard(
        title = "السور المتآخية والمجموعات القرآنية",
        icon = Icons.Rounded.AutoStories
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            groups.forEach { group ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("surah_group_card_${group.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(11.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = group.titleAr,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = group.categoryLabelAr,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Text(
                            text = group.descriptionAr,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val displayNumbers = group.surahNumbers.take(12)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            displayNumbers.forEach { num ->
                                val isCurrent = num == currentSurahNumber
                                val sisterName = surahByNum[num]?.nameAr ?: "سورة ${num.toAppDigits()}"
                                Surface(
                                    onClick = { if (!isCurrent) onSelectSurahNumber(num) },
                                    enabled = !isCurrent,
                                    shape = RoundedCornerShape(50),
                                    color = if (isCurrent) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier.testTag("sister_surah_pill_${group.id}_$num")
                                ) {
                                    Text(
                                        text = sisterName,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold
                                        ),
                                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                            if (group.surahNumbers.size > displayNumbers.size) {
                                val remaining = group.surahNumbers.size - displayNumbers.size
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Text(
                                        text = "+${remaining.toAppDigits()} سورة",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
