package com.example.ui.screens.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.wird.WirdPlanSummary
import com.example.domain.wird.WirdPortion

@Composable
internal fun ReaderTopChromeHost(
    isPureWirdMode: Boolean,
    activeWirdPlan: WirdPlanSummary?,
    targetWird: WirdPortion? = activeWirdPlan?.currentWird,
    showAudioSheet: Boolean,
    isCurrentPageBookmarked: Boolean,
    isDimmerActive: Boolean,
    isMemorizationHideMode: Boolean,
    memorizationMaskStyle: MemorizationMaskStyle = MemorizationMaskStyle.FULL_BLANK,
    revealedCountOnPage: Int = 0,
    totalCountOnPage: Int = 0,
    onBack: (() -> Unit)?,
    onOpenDimmer: () -> Unit,
    onToggleMemorizationHideMode: () -> Unit,
    onToggleMemorizationMaskStyle: () -> Unit = {},
    onRevealNextMemorization: () -> Unit = {},
    onToggleAudioSheet: () -> Unit,
    onToggleBookmark: () -> Unit,
    onRevealAllMemorization: () -> Unit,
    onHideAllMemorization: () -> Unit
) {
    if (isPureWirdMode) {
        ReaderWirdPureTopBar(
            plan = activeWirdPlan,
            targetWird = targetWird,
            onBack = onBack
        )
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ReaderTopFloatingBar(
                showAudioSheet = showAudioSheet,
                isCurrentPageBookmarked = isCurrentPageBookmarked,
                isDimmerActive = isDimmerActive,
                isMemorizationHideMode = isMemorizationHideMode,
                onBack = onBack,
                onOpenDimmer = onOpenDimmer,
                onToggleMemorizationHideMode = onToggleMemorizationHideMode,
                onToggleAudioSheet = onToggleAudioSheet,
                onToggleBookmark = onToggleBookmark
            )
            if (!showAudioSheet && isMemorizationHideMode) {
                Spacer(modifier = Modifier.height(2.dp))
                ReaderMemorizationControlStrip(
                    maskStyle = memorizationMaskStyle,
                    revealedCount = revealedCountOnPage,
                    totalCount = totalCountOnPage,
                    onToggleMaskStyle = onToggleMemorizationMaskStyle,
                    onRevealNext = onRevealNextMemorization,
                    onRevealAll = onRevealAllMemorization,
                    onHideAll = onHideAllMemorization
                )
            }
        }
    }
}

@Composable
internal fun ReaderTopFloatingBar(
    showAudioSheet: Boolean,
    isCurrentPageBookmarked: Boolean,
    isDimmerActive: Boolean = false,
    isMemorizationHideMode: Boolean = false,
    onBack: (() -> Unit)?,
    onOpenDimmer: () -> Unit = {},
    onToggleMemorizationHideMode: () -> Unit = {},
    onToggleAudioSheet: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
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
                        contentDescription = stringResource(R.string.reader_back_to_index_cd),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onToggleMemorizationHideMode,
                shape = CircleShape,
                color = if (isMemorizationHideMode) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.94f)
                },
                border = BorderStroke(
                    1.dp,
                    if (isMemorizationHideMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                ),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("reader_memorization_hide_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isMemorizationHideMode) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = stringResource(
                            if (isMemorizationHideMode) R.string.reader_memorization_stop_cd else R.string.reader_memorization_start_cd
                        ),
                        tint = if (isMemorizationHideMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                onClick = onOpenDimmer,
                shape = CircleShape,
                color = if (isDimmerActive) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.94f)
                },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("reader_dimmer_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.BrightnessMedium,
                        contentDescription = stringResource(R.string.reader_dimmer_cd),
                        tint = if (isDimmerActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                onClick = onToggleAudioSheet,
                shape = CircleShape,
                color = if (showAudioSheet) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
                border = if (showAudioSheet) BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)) else null,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("reader_audio_toggle_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (showAudioSheet) Icons.Rounded.Close else Icons.Rounded.Headphones,
                        contentDescription = stringResource(
                            if (showAudioSheet) R.string.reader_audio_close_cd else R.string.reader_audio_open_cd
                        ),
                        tint = if (showAudioSheet) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            Surface(
                onClick = onToggleBookmark,
                shape = CircleShape,
                color = if (isCurrentPageBookmarked) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.94f)
                },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("reader_bookmark_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isCurrentPageBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = stringResource(R.string.reader_bookmark_cd),
                        tint = if (isCurrentPageBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
