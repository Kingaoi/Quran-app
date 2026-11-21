package com.example.ui.screens.more

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppSourceDetailItem
import com.example.domain.model.AppSourceGroup
import com.example.domain.model.AppSourcesCatalog
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.SourceCategoryId
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors
import com.example.ui.util.copyTextToClipboard

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MoreSourcesSection(
    onCopiedSource: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val useWestern = LocalUseWesternDigits.current
    var selectedFilter by rememberSaveable { mutableStateOf<SourceCategoryId?>(null) }

    val visibleGroups = remember(selectedFilter) {
        if (selectedFilter == null) AppSourcesCatalog.groups
        else AppSourcesCatalog.groups.filter { it.id == selectedFilter }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MoreCategoryHeader(title = "المصادر والمراجع المعتمدة", icon = Icons.Rounded.LibraryBooks)

        SourcesOverviewBanner(
            totalGroups = AppSourcesCatalog.totalGroupsCount,
            totalSources = AppSourcesCatalog.totalSourcesCount,
            useWestern = useWestern
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                colors = quranFilterChipColors(),
                border = quranFilterChipBorder(selectedFilter == null),
                label = { Text("الكل (${AppSourcesCatalog.totalSourcesCount.toAppDigits(useWestern)})") },
                modifier = Modifier.testTag("sources_filter_all")
            )
            SourceCategoryId.entries.forEach { categoryId ->
                val selected = selectedFilter == categoryId
                FilterChip(
                    selected = selected,
                    onClick = { selectedFilter = if (selected) null else categoryId },
                    colors = quranFilterChipColors(),
                    border = quranFilterChipBorder(selected),
                    label = { Text(categoryId.shortLabelAr) },
                    modifier = Modifier.testTag("sources_filter_${categoryId.name.lowercase()}")
                )
            }
        }

        visibleGroups.forEach { group ->
            SourceGroupCard(
                group = group,
                onCopyReference = { item ->
                    copyTextToClipboard(context, item.titleAr, "${item.titleAr}\n${item.referenceText}")
                    onCopiedSource("تم نسخ مرجع «${item.titleAr}»")
                }
            )
        }
    }
}

@Composable
private fun SourcesOverviewBanner(
    totalGroups: Int,
    totalSources: Int,
    useWestern: Boolean
) {
    val qc = MaterialTheme.quranColors
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
    val surfaceLow = MaterialTheme.colorScheme.surfaceContainerLow
    val bannerBrush = remember(primaryContainer, secondaryContainer, surfaceLow) {
        Brush.linearGradient(
            colors = listOf(
                primaryContainer.copy(alpha = 0.45f),
                secondaryContainer.copy(alpha = 0.30f),
                surfaceLow
            )
        )
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = surfaceLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("more_sources_overview_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(bannerBrush)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MoreSettingIconBadge(
                    icon = Icons.Rounded.LibraryBooks,
                    tint = qc.primary,
                    containerColor = qc.surface2
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "بيان جميع مصادر التطبيق ومراجعه",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${totalGroups.toAppDigits(useWestern)} مجالات • ${totalSources.toAppDigits(useWestern)} مصدراً ومرجعاً مفصّلاً",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = "يعرض هذا القسم كل مصادر النصوص القرآنية، وصفحات المصحف، وكتب التفسير، والتلاوات الصوتية، والأذان، والأذكار، والحسابات الفلكية، والخطوط والمكتبات المستخدمة في التطبيق بشفافية وتفصيل.",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SourceGroupCard(
    group: AppSourceGroup,
    onCopyReference: (AppSourceDetailItem) -> Unit
) {
    val qc = MaterialTheme.quranColors
    val accent = qc.categoryPastelAt(group.accentIndex)
    val groupIcon = iconForSourceCategory(group.id)

    MoreSubGroupHeader(title = group.titleAr)
    MoreSettingsGroupCard(
        modifier = Modifier.testTag("source_group_${group.id.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MoreSettingIconBadge(
                icon = groupIcon,
                tint = accent,
                containerColor = accent.copy(alpha = 0.16f)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = group.titleAr,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = group.subtitleAr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        group.items.forEach { item ->
            MoreGroupDivider()
            SourceDetailRow(
                item = item,
                accentColor = accent,
                onCopyReference = { onCopyReference(item) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SourceDetailRow(
    item: AppSourceDetailItem,
    accentColor: Color,
    onCopyReference: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("source_item_${item.id}"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = item.titleAr,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(50),
                color = accentColor.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
            ) {
                Text(
                    text = item.badgeAr,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                )
            }
        }

        Text(
            text = item.roleAr,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = item.detailsAr,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.referenceText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onCopyReference,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("source_copy_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "نسخ مرجع ${item.titleAr}",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun iconForSourceCategory(id: SourceCategoryId): ImageVector = when (id) {
    SourceCategoryId.MUSHAF_TEXT -> Icons.AutoMirrored.Rounded.MenuBook
    SourceCategoryId.TAFSIR_SCIENCES -> Icons.Rounded.AutoStories
    SourceCategoryId.AUDIO_MEDIA -> Icons.Rounded.GraphicEq
    SourceCategoryId.ADHKAR_DUAS -> Icons.Rounded.VolunteerActivism
    SourceCategoryId.PRAYER_CALENDAR -> Icons.Rounded.Mosque
    SourceCategoryId.FONTS_TECH -> Icons.Rounded.FontDownload
}
