package com.example.ui.screens.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.toAppDigits
import com.example.domain.wird.WirdPlanSummary
import com.example.domain.wird.WirdPortion

@Composable
internal fun ReaderBottomChromeHost(
    isPureWirdMode: Boolean,
    currentPage: Int,
    currentSurahLabel: String,
    firstAyahNumber: Int,
    minPage: Int,
    maxPage: Int,
    activeWirdPlan: WirdPlanSummary?,
    targetWird: WirdPortion?,
    onCompleteWird: () -> Unit,
    onAdvanceToNextWird: () -> Unit,
    onOpenPageAyahs: () -> Unit,
    onOpenQuickJump: () -> Unit,
    onOpenPageJumpDialog: () -> Unit,
    onStepPreviousPage: () -> Unit,
    onStepNextPage: () -> Unit,
    onScrubToPage: (Int) -> Unit,
    autoScrollEnabled: Boolean = false,
    onToggleAutoScroll: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val resolvedWird = targetWird ?: activeWirdPlan?.currentWird
    val isThisWirdCompleted = resolvedWird != null && activeWirdPlan != null && resolvedWird.index < activeWirdPlan.completedCount
    val hasNextWird = activeWirdPlan != null && !activeWirdPlan.isCompleted &&
        activeWirdPlan.currentWird != null && resolvedWird?.index != activeWirdPlan.currentWird.index
    val showWirdBanner = activeWirdPlan != null && resolvedWird != null && (!activeWirdPlan.isCompleted || isThisWirdCompleted)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showWirdBanner) {
            ReaderWirdBanner(
                currentPage = currentPage,
                plan = activeWirdPlan,
                targetWird = resolvedWird,
                isThisWirdCompleted = isThisWirdCompleted,
                hasNextWird = hasNextWird,
                onCompleteWird = onCompleteWird,
                onAdvanceToNextWird = onAdvanceToNextWird,
                horizontalPadding = 0.dp
            )
        }
        if (!isPureWirdMode) {
            ReaderBottomScrubberPill(
                currentPage = currentPage,
                currentSurahLabel = currentSurahLabel,
                firstAyahNumber = firstAyahNumber,
                minPage = minPage,
                maxPage = maxPage,
                applyOuterPadding = false,
                onOpenPageAyahs = onOpenPageAyahs,
                onOpenQuickJump = onOpenQuickJump,
                onOpenPageJumpDialog = onOpenPageJumpDialog,
                onStepPreviousPage = onStepPreviousPage,
                onStepNextPage = onStepNextPage,
                onScrubToPage = onScrubToPage,
                autoScrollEnabled = autoScrollEnabled,
                onToggleAutoScroll = onToggleAutoScroll
            )
        }
    }
}

@Composable
internal fun ReaderBottomScrubberPill(
    currentPage: Int,
    currentSurahLabel: String,
    firstAyahNumber: Int,
    minPage: Int = 1,
    maxPage: Int = 604,
    applyOuterPadding: Boolean = true,
    onOpenPageAyahs: () -> Unit,
    onOpenQuickJump: () -> Unit,
    onOpenPageJumpDialog: () -> Unit,
    onStepPreviousPage: () -> Unit,
    onStepNextPage: () -> Unit,
    onScrubToPage: (Int) -> Unit,
    autoScrollEnabled: Boolean = false,
    onToggleAutoScroll: () -> Unit = {}
) {
    val safeMin = minPage.coerceIn(1, 604)
    val safeMax = maxPage.coerceIn(safeMin, 604)
    var sliderPage by remember(currentPage, safeMin, safeMax) {
        mutableFloatStateOf(currentPage.coerceIn(safeMin, safeMax).toFloat())
    }
    var isScrubbing by remember { mutableStateOf(false) }

    val cardModifier = if (applyOuterPadding) {
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    } else {
        Modifier.fillMaxWidth()
    }

    Surface(
        modifier = cardModifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 4.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onOpenPageAyahs,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.testTag("open_page_ayahs_button")
                ) {
                    Text(
                        text = "آيات الصفحة",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenQuickJump)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("reader_header_jump")
                ) {
                    Text(
                        text = if (isScrubbing) {
                            "صفحة ${sliderPage.toInt().toAppDigits()}"
                        } else {
                            "سورة $currentSurahLabel - الآية (${firstAyahNumber.toAppDigits()})"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "الانتقال السريع",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Surface(
                    onClick = onOpenPageJumpDialog,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.testTag("open_page_jump_button")
                ) {
                    Text(
                        text = "ص ${currentPage.toAppDigits()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = onStepPreviousPage,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("page_prev_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = "الصفحة السابقة",
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Slider(
                    value = sliderPage,
                    onValueChange = { v ->
                        isScrubbing = true
                        sliderPage = v
                    },
                    onValueChangeFinished = {
                        isScrubbing = false
                        onScrubToPage(sliderPage.toInt().coerceIn(safeMin, safeMax))
                    },
                    enabled = safeMax > safeMin,
                    valueRange = safeMin.toFloat()..(if (safeMax > safeMin) safeMax.toFloat() else (safeMin + 1).toFloat()),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onSurface,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .testTag("mushaf_page_slider")
                )

                Surface(
                    onClick = onStepNextPage,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("page_next_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = "الصفحة التالية",
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    onClick = onToggleAutoScroll,
                    shape = RoundedCornerShape(50),
                    color = if (autoScrollEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = if (autoScrollEnabled) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("reader_autoscroll_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SlowMotionVideo,
                            contentDescription = if (autoScrollEnabled) "إيقاف التمرير التلقائي" else "تفعيل التمرير التلقائي",
                            tint = if (autoScrollEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "تلقائي",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (autoScrollEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
