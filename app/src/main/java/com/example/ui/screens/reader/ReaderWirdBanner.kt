package com.example.ui.screens.reader

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.domain.wird.WirdPlanSummary
import com.example.domain.wird.WirdPortion

@Composable
internal fun ReaderWirdPureTopBar(
    plan: WirdPlanSummary?,
    targetWird: WirdPortion? = plan?.currentWird,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val useWestern = LocalUseWesternDigits.current
    val activeWird = targetWird ?: plan?.currentWird
    val isThisWirdCompleted = activeWird != null && plan != null && activeWird.index < plan.completedCount
    val showWirdChip = plan != null && activeWird != null && (!plan.isCompleted || isThisWirdCompleted)
    val chipText = if (showWirdChip && activeWird != null) {
        "الورد ${(activeWird.index + 1).toAppDigits(useWestern)} • ص ${activeWird.startPage.toAppDigits(useWestern)}–${activeWird.endPage.toAppDigits(useWestern)}"
    } else {
        "القراءة الصافية"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("reader_wird_pure_top_bar"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("reader_back_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "الرجوع إلى الورد",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 2.dp,
            modifier = Modifier.testTag("reader_pure_mode_chip")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoStories,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = chipText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
internal fun ReaderWirdBanner(
    currentPage: Int,
    plan: WirdPlanSummary?,
    modifier: Modifier = Modifier,
    targetWird: WirdPortion? = plan?.currentWird,
    isThisWirdCompleted: Boolean = targetWird != null && plan != null && targetWird.index < plan.completedCount,
    hasNextWird: Boolean = false,
    onCompleteWird: () -> Unit,
    onAdvanceToNextWird: () -> Unit = {},
    horizontalPadding: Dp = 16.dp
) {
    val activeWird = targetWird ?: plan?.currentWird
    if (plan == null || activeWird == null) return
    if (plan.isCompleted && !isThisWirdCompleted) return

    val useWestern = LocalUseWesternDigits.current
    val totalPages = activeWird.pageSpanCount
    val pagesRead = (currentPage - activeWird.startPage + 1).coerceIn(1, totalPages)
    val remainingPages = (activeWird.endPage - currentPage).coerceIn(0, totalPages)
    val inWirdRange = currentPage in activeWird.startPage..activeWird.endPage
    val reachedEndPage = currentPage >= activeWird.endPage
    val rawProgress = if (isThisWirdCompleted) 1f else (pagesRead.toFloat() / totalPages.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "wird_completion_progress"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .testTag("reader_wird_banner"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.95f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isThisWirdCompleted || reachedEndPage) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.52f)
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            }
        ),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoStories,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(19.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الورد ${(activeWird.index + 1).toAppDigits(useWestern)} • ${pagesRead.toAppDigits(useWestern)}/${totalPages.toAppDigits(useWestern)} ص",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when {
                            isThisWirdCompleted -> "تم إتمام هذا الورد بحمد الله"
                            reachedEndPage && inWirdRange -> "وصلت إلى نهاية الورد • جاهز للإتمام"
                            inWirdRange -> "متبقي ${remainingPages.toAppDigits(useWestern)} صفحة بعد هذه الصفحة"
                            else -> "${activeWird.startSurahNameAr} (${activeWird.startAyahNumber.toAppDigits(useWestern)}) ← ${activeWird.endSurahNameAr}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isThisWirdCompleted || reachedEndPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (isThisWirdCompleted && hasNextWird) {
                    Button(
                        onClick = onAdvanceToNextWird,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("reader_next_wird_button")
                    ) {
                        Text("التالي", style = MaterialTheme.typography.labelMedium)
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 4.dp).size(15.dp)
                        )
                    }
                } else if (!isThisWirdCompleted) {
                    val btnModifier = Modifier.height(36.dp).testTag("reader_complete_wird_button")
                    if (reachedEndPage) {
                        Button(
                            onClick = onCompleteWird,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = btnModifier
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("أتممت", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 4.dp))
                        }
                    } else {
                        FilledTonalButton(
                            onClick = onCompleteWird,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = btnModifier
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("أتممت", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .testTag("reader_wird_animated_progress")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.coerceIn(0.02f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                )
            }
        }
    }
}

internal data class WirdReaderScope(
    val scopedWird: WirdPortion?,
    val boundaryPortion: WirdPortion?,
    val startPage: Int,
    val endPage: Int
) {
    fun pageCount(isTwoPageSpread: Boolean): Int {
        val total = (endPage - startPage + 1).coerceAtLeast(1)
        return if (isTwoPageSpread) ((total + 1) / 2).coerceAtLeast(1) else total
    }

    fun pageToIndex(page: Int, isTwoPageSpread: Boolean): Int {
        val count = pageCount(isTwoPageSpread)
        val offset = page.coerceIn(startPage, endPage) - startPage
        return if (isTwoPageSpread) (offset / 2).coerceIn(0, count - 1) else offset.coerceIn(0, count - 1)
    }

    fun indexToPage(index: Int, isTwoPageSpread: Boolean): Int {
        val count = pageCount(isTwoPageSpread)
        val clamped = index.coerceIn(0, count - 1)
        val offset = if (isTwoPageSpread) clamped * 2 else clamped
        return (startPage + offset).coerceIn(startPage, endPage)
    }
}

internal fun shouldShowWirdBannerInReader(
    openedFromWird: Boolean,
    hideWirdBannerOutsideWird: Boolean
): Boolean = openedFromWird || !hideWirdBannerOutsideWird

internal fun resolveWirdReaderScope(
    openedFromWird: Boolean,
    plan: WirdPlanSummary?,
    wirdIndex: Int
): WirdReaderScope {
    val scoped = if (!openedFromWird || plan == null) null
    else plan.allWirds.firstOrNull { it.index == wirdIndex } ?: plan.currentWird
    val start = (scoped?.startPage ?: 1).coerceIn(1, 604)
    val end = (scoped?.endPage ?: 604).coerceIn(start, 604)
    val boundary = scoped ?: plan?.currentWird?.takeIf { !plan.isCompleted }
    return WirdReaderScope(scopedWird = scoped, boundaryPortion = boundary, startPage = start, endPage = end)
}

