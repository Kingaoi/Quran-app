package com.example.ui.screens.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

@Composable
internal fun ReaderAutoScrollFloatingBar(
    progress: Float,
    userPaused: Boolean,
    pausedByOverlay: Boolean,
    secondsPerPage: Int,
    effectiveSeconds: Int,
    onTogglePlayPause: () -> Unit,
    onChangeSecondsPerPage: (Int) -> Unit,
    onClose: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 6.dp,
        shadowElevation = 4.dp,
        modifier = Modifier
            .widthIn(max = 480.dp)
            .fillMaxWidth()
            .testTag("reader_autoscroll_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                color = if (userPaused || pausedByOverlay) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        onClick = onTogglePlayPause,
                        shape = CircleShape,
                        color = if (userPaused) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("autoscroll_play_pause_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (userPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                contentDescription = if (userPaused) "متابعة التمرير التلقائي" else "إيقاف التمرير التلقائي مؤقتاً",
                                tint = if (userPaused) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = { expanded = !expanded },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                        modifier = Modifier.testTag("autoscroll_speed_expand_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = when {
                                    userPaused -> "متوقف • ${effectiveSeconds.toAppDigits()} ث"
                                    pausedByOverlay -> "معلّق • ${effectiveSeconds.toAppDigits()} ث"
                                    else -> "تمرير • ${effectiveSeconds.toAppDigits()} ث/صفحة"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                                contentDescription = "خيارات سرعة التمرير",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onChangeSecondsPerPage((secondsPerPage - 5).coerceAtLeast(10)) },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("autoscroll_faster_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "تسريع التمرير",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { onChangeSecondsPerPage((secondsPerPage + 5).coerceAtMost(180)) },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("autoscroll_slower_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "إبطاء التمرير",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("autoscroll_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "إغلاق التمرير التلقائي",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AutoScrollSpeedPreset.entries.forEach { preset ->
                        val selected = preset.secondsPerPage == secondsPerPage
                        FilterChip(
                            selected = selected,
                            onClick = { onChangeSecondsPerPage(preset.secondsPerPage) },
                            shape = RoundedCornerShape(50),
                            colors = quranFilterChipColors(),
                            border = quranFilterChipBorder(selected),
                            label = {
                                Text(
                                    text = "${preset.labelAr} (${preset.secondsPerPage.toAppDigits()} ث)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            modifier = Modifier.testTag("autoscroll_preset_${preset.secondsPerPage}")
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}
