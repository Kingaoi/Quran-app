package com.example.ui.screens.adhkar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.DhikrCategory
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.ui.navigation.QuranSharedKey
import com.example.ui.navigation.quranSharedBounds
import com.example.ui.navigation.quranSharedElement
import com.example.ui.theme.quranColors

internal data class CategoryVisualStyle(
    val accentColor: Color,
    val containerColor: Color,
    val icon: ImageVector
)

@Composable
internal fun resolveCategoryStyle(category: DhikrCategory): CategoryVisualStyle {
    val qc = MaterialTheme.quranColors
    val icon = when (category) {
        DhikrCategory.MORNING -> Icons.Rounded.WbSunny
        DhikrCategory.EVENING -> Icons.Rounded.NightsStay
        DhikrCategory.AFTER_PRAYER -> Icons.Rounded.Mosque
        DhikrCategory.SLEEP -> Icons.Rounded.Bedtime
        DhikrCategory.MOSQUE -> Icons.Rounded.Mosque
        DhikrCategory.WAKE_UP -> Icons.Rounded.AlarmOn
        DhikrCategory.QURANIC_DUAS -> Icons.AutoMirrored.Rounded.MenuBook
        DhikrCategory.PROPHETIC_DUAS -> Icons.Rounded.VolunteerActivism
        DhikrCategory.RUQYAH -> Icons.Rounded.Shield
        DhikrCategory.TRAVEL -> Icons.Rounded.FlightTakeoff
        DhikrCategory.TASBIH -> Icons.Rounded.AllInclusive
        DhikrCategory.DAILY_LIFE -> Icons.Rounded.Home
        DhikrCategory.WORRY_RELIEF -> Icons.Rounded.Favorite
        DhikrCategory.WEATHER_NATURE -> Icons.Rounded.WaterDrop
        DhikrCategory.SOCIAL_ETHICS -> Icons.Rounded.Groups
        DhikrCategory.HAJJ_WORSHIP -> Icons.Rounded.Star
    }
    return CategoryVisualStyle(
        accentColor = qc.categoryPastelAt(category.ordinal),
        containerColor = qc.surface2,
        icon = icon
    )
}

@Composable
internal fun AdhkarCategoryCard(
    category: DhikrCategory,
    completedItems: Int,
    totalItems: Int,
    subChapterCount: Int = 1,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val useWestern = LocalUseWesternDigits.current
    val style = resolveCategoryStyle(category)
    val cardHeight = if (category.isWideCard) 118.dp else 138.dp
    val cardShape = RoundedCornerShape(22.dp)
    val isDone = totalItems > 0 && completedItems >= totalItems
    val progress = if (totalItems > 0) (completedItems.toFloat() / totalItems.toFloat()).coerceIn(0f, 1f) else 0f

    val qc = MaterialTheme.quranColors
    val surfaceLow = qc.surface
    val cardBrush = remember(style.accentColor, surfaceLow) {
        Brush.linearGradient(
            colors = listOf(
                style.accentColor.copy(alpha = 0.14f),
                surfaceLow
            )
        )
    }

    Surface(
        shape = cardShape,
        color = surfaceLow,
        tonalElevation = 1.dp,
        modifier = modifier
            .quranSharedBounds(QuranSharedKey.AdhkarCategoryContainer(category.id))
            .fillMaxWidth()
            .height(cardHeight)
            .clip(cardShape)
            .border(
                width = if (isDone) 1.5.dp else 1.dp,
                color = if (isDone) qc.success else qc.border,
                shape = cardShape
            )
            .clickable(onClick = onClick)
            .testTag("adhkar_cat_${category.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardBrush)
        ) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = if (category.isWideCard) 14.dp else 4.dp, bottom = 4.dp)
                    .size(if (category.isWideCard) 96.dp else 76.dp)
            ) {
                drawCategoryWatermark(
                    category = category,
                    tint = style.accentColor.copy(alpha = 0.22f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .quranSharedElement(QuranSharedKey.AdhkarCategoryIcon(category.id), zIndexInOverlay = 1f)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(style.containerColor)
                            .border(1.dp, style.accentColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = style.icon,
                            contentDescription = null,
                            tint = style.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isDone) {
                            qc.successContainer
                        } else {
                            qc.surface2
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isDone) {
                                qc.success.copy(alpha = 0.45f)
                            } else {
                                qc.border
                            }
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            if (isDone) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "مكتمل",
                                    tint = qc.success,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = when {
                                    isDone -> "مكتمل"
                                    completedItems > 0 -> "${completedItems.toAppDigits(useWestern)}/${totalItems.toAppDigits(useWestern)}"
                                    subChapterCount > 1 && !category.isSequentialLitany ->
                                        "${subChapterCount.toAppDigits(useWestern)} أنواع"
                                    else -> "${totalItems.toAppDigits(useWestern)} ذكر"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isDone) {
                                    qc.success
                                } else {
                                    qc.textMuted
                                }
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(if (category.isWideCard) 0.78f else 0.94f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = category.titleAr,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.quranSharedBounds(
                            QuranSharedKey.AdhkarCategoryTitle(category.id),
                            zIndexInOverlay = 1f
                        )
                    )
                    Text(
                        text = category.subtitleAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (completedItems > 0) {
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            color = style.accentColor,
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }
    }
}
