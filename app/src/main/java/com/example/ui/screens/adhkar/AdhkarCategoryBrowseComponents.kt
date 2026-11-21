package com.example.ui.screens.adhkar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.adhkar.DhikrAudioState
import com.example.domain.model.DhikrCategory
import com.example.domain.model.DhikrItem
import com.example.domain.model.DhikrSubChapterGroup
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.ui.navigation.QuranSharedKey
import com.example.ui.navigation.quranSharedBounds
import com.example.ui.navigation.quranSharedElement
import com.example.ui.theme.LocalQuranFontFamily
import com.example.ui.theme.quranColors

@Composable
internal fun AdhkarBrowseHeroBanner(
    category: DhikrCategory,
    completedCount: Int,
    totalCount: Int,
    subChapterCount: Int,
    firstIncompleteIndex: Int,
    onStartSequence: (Int) -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    val style = resolveCategoryStyle(category)
    val progress = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f
    val heroBrush = remember(style.accentColor, qc.surface) {
        Brush.linearGradient(colors = listOf(style.accentColor.copy(alpha = 0.16f), qc.surface))
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = qc.surface,
        border = BorderStroke(1.dp, qc.border),
        modifier = Modifier
            .quranSharedBounds(QuranSharedKey.AdhkarCategoryContainer(category.id))
            .fillMaxWidth()
            .testTag("adhkar_browse_hero")
    ) {
        Box(modifier = Modifier.fillMaxWidth().background(heroBrush)) {
            Canvas(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 10.dp, bottom = 6.dp).size(92.dp)) {
                drawCategoryWatermark(category = category, tint = style.accentColor.copy(alpha = 0.18f))
            }
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .quranSharedElement(QuranSharedKey.AdhkarCategoryIcon(category.id), zIndexInOverlay = 1f)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(style.accentColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(style.icon, contentDescription = null, tint = style.accentColor, modifier = Modifier.size(24.dp))
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = category.subtitleAr,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = qc.text
                            )
                            Text(
                                text = if (subChapterCount > 1 && !category.isSequentialLitany) {
                                    "${subChapterCount.toAppDigits(useWestern)} أنواع فرعية • ${totalCount.toAppDigits(useWestern)} ذكر"
                                } else {
                                    "أنجزت ${completedCount.toAppDigits(useWestern)} من ${totalCount.toAppDigits(useWestern)} ذكر"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = qc.textMuted
                            )
                        }
                    }
                    Surface(shape = RoundedCornerShape(50), color = qc.surface2, border = BorderStroke(1.dp, qc.border)) {
                        Text(
                            text = "${completedCount.toAppDigits(useWestern)} / ${totalCount.toAppDigits(useWestern)}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (totalCount > 0 && completedCount >= totalCount) qc.success else style.accentColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { progress },
                    color = style.accentColor,
                    trackColor = qc.border.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                )

                val seqLabel = when {
                    category.isSequentialLitany && completedCount in 1 until totalCount ->
                        "أكمل التلاوة من الذكر ${(firstIncompleteIndex + 1).toAppDigits(useWestern)}"
                    category.isSequentialLitany -> "ابدأ التلاوة المتتابعة"
                    else -> "قراءة جميع أذكار القسم متتابعة"
                }

                if (category.isSequentialLitany || subChapterCount <= 1) {
                    Button(
                        onClick = { onStartSequence(firstIncompleteIndex) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = qc.primary, contentColor = qc.onPrimary),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("adhkar_browse_start_sequence")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(seqLabel, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                } else {
                    OutlinedButton(
                        onClick = { onStartSequence(firstIncompleteIndex) },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, style.accentColor.copy(alpha = 0.45f)),
                        modifier = Modifier.height(48.dp).testTag("adhkar_browse_start_sequence")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, tint = style.accentColor, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(seqLabel, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = style.accentColor)
                    }
                }
            }
        }
    }
}

@Composable
internal fun AdhkarSubChapterCard(
    index: Int,
    categoryId: String = "",
    group: DhikrSubChapterGroup,
    countsById: Map<String, Int>,
    favoriteIds: Set<String>,
    audioState: DhikrAudioState,
    hapticEnabled: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpenChapterReader: () -> Unit,
    onOpenItemInChapterReader: (Int) -> Unit,
    onIncrementItem: (DhikrItem) -> Boolean,
    onToggleFavorite: (String) -> Unit,
    onTogglePlayAudio: (DhikrItem) -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    val completed = group.items.count { (countsById[it.id] ?: 0) >= it.targetCount }
    val isDone = group.items.isNotEmpty() && completed >= group.items.size
    val accent = qc.categoryPastelAt(index)
    val previewItem = group.items.firstOrNull()

    Surface(
        onClick = onOpenChapterReader,
        shape = RoundedCornerShape(22.dp),
        color = qc.surface,
        border = BorderStroke(if (isDone) 1.5.dp else 1.dp, if (isDone) qc.success else qc.border),
        modifier = Modifier.fillMaxWidth().testTag("adhkar_subchapter_$index")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(34.dp).clip(CircleShape).background(if (isDone) qc.successContainer else accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = "مكتمل", tint = qc.success, modifier = Modifier.size(18.dp))
                        } else {
                            Text(
                                text = (index + 1).toAppDigits(useWestern),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = accent
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        val resolvedCatId = categoryId.ifBlank { group.items.firstOrNull()?.category?.id.orEmpty() }
                        Text(
                            text = group.chapterTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = qc.text,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = if (resolvedCatId.isNotBlank()) {
                                Modifier.quranSharedBounds(
                                    QuranSharedKey.AdhkarChapterTitle(resolvedCatId, group.chapterTitle),
                                    zIndexInOverlay = 1f
                                )
                            } else Modifier
                        )
                        Text(
                            text = "${group.items.size.toAppDigits(useWestern)} ذكر • مكتمل ${completed.toAppDigits(useWestern)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDone) qc.success else qc.textMuted
                        )
                    }
                }

                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(48.dp).testTag("adhkar_expand_chapter_$index")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (isExpanded) "إخفاء الأذكار" else "معاينة أذكار النوع",
                        tint = qc.textMuted
                    )
                }
            }

            if (!isExpanded && previewItem != null) {
                Text(
                    text = previewItem.arabicText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = LocalQuranFontFamily.current),
                    color = qc.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = onOpenChapterReader,
                    shape = RoundedCornerShape(12.dp),
                    color = qc.primary.copy(alpha = 0.12f),
                    modifier = Modifier.testTag("adhkar_open_chapter_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, tint = qc.primary, modifier = Modifier.size(16.dp))
                        Text("فتح للقراءة", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = qc.primary)
                    }
                }
                Text(
                    text = if (isExpanded) "إغلاق المعاينة" else "عرض الأذكار بالداخل",
                    style = MaterialTheme.typography.labelSmall,
                    color = qc.textMuted
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = qc.border.copy(alpha = 0.6f))
                    group.items.forEachIndexed { itemIdx, item ->
                        AdhkarBrowseItemCard(
                            item = item,
                            currentCount = countsById[item.id] ?: 0,
                            isFavorite = item.id in favoriteIds,
                            audioState = audioState,
                            hapticEnabled = hapticEnabled,
                            showChapterTag = false,
                            onIncrement = { onIncrementItem(item) },
                            onToggleFavorite = { onToggleFavorite(item.id) },
                            onTogglePlayAudio = { onTogglePlayAudio(item) },
                            onOpenReader = { onOpenItemInChapterReader(itemIdx) }
                        )
                    }
                }
            }
        }
    }
}
