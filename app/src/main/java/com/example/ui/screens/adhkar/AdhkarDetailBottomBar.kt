package com.example.ui.screens.adhkar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.DhikrItem
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits

@Composable
internal fun AdhkarDetailProgressStrip(
    currentIndex: Int,
    totalCount: Int
) {
    val useWestern = LocalUseWesternDigits.current
    val targetProgress = if (totalCount > 0) (currentIndex + 1f) / totalCount.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = targetProgress, label = "adhkar_pager_progress")

    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${(currentIndex + 1).toAppDigits(useWestern)} / ${totalCount.toAppDigits(useWestern)}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            LinearProgressIndicator(
                progress = { animatedProgress },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
internal fun AdhkarDetailBottomBar(
    item: DhikrItem,
    currentCount: Int,
    hasPrevious: Boolean,
    isLastPage: Boolean,
    hapticEnabled: Boolean = true,
    onIncrement: () -> Boolean,
    onReset: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onShareImage: () -> Unit,
    onPrevious: () -> Unit,
    onNextOrFinish: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val useWestern = LocalUseWesternDigits.current
    var showShareMenu by remember { mutableStateOf(false) }
    val remaining = (item.targetCount - currentCount).coerceAtLeast(0)
    val isCompleted = remaining == 0

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isCompleted) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            val justDone = onIncrement()
                            if (hapticEnabled) {
                                haptics.performHapticFeedback(
                                    if (justDone) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
                                )
                            }
                        }
                        .testTag("adhkar_detail_count_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.TouchApp,
                            contentDescription = null,
                            tint = if (isCompleted) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isCompleted -> "تم الذكر بحمد الله"
                                item.targetCount == 1 -> "مرة واحدة"
                                else -> "المتبقي ${remaining.toAppDigits(useWestern)} من ${item.targetCount.toAppDigits(useWestern)}"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isCompleted) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentCount > 0) {
                        IconButton(onClick = onReset) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "إعادة العداد",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "نسخ الذكر",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Box {
                        IconButton(onClick = { showShareMenu = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "مشاركة الذكر",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(expanded = showShareMenu, onDismissRequest = { showShareMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("مشاركة كبطاقة صورة") },
                                onClick = { showShareMenu = false; onShareImage() }
                            )
                            DropdownMenuItem(
                                text = { Text("مشاركة كنص") },
                                onClick = { showShareMenu = false; onShare() }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (hasPrevious) {
                    OutlinedButton(
                        onClick = onPrevious,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(
                            text = "السابق",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
                Button(
                    onClick = onNextOrFinish,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("adhkar_detail_next_button")
                ) {
                    Text(
                        text = if (isLastPage) "إتمام القسم ✓" else "التالي",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
