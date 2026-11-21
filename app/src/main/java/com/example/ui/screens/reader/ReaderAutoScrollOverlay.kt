package com.example.ui.screens.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

internal enum class AutoScrollSpeedPreset(
    val secondsPerPage: Int,
    val labelAr: String
) {
    FAST(15, "سريع"),
    MEDIUM(25, "متوسط"),
    CALM(40, "هادئ"),
    SLOW(60, "بطيء"),
    QIYAM(90, "قيام")
}

@Stable
internal class ReaderAutoScrollState(
    initialEnabled: Boolean = false,
    initialSecondsPerPage: Int = AutoScrollSpeedPreset.MEDIUM.secondsPerPage
) {
    var enabled by mutableStateOf(initialEnabled)
    var userPaused by mutableStateOf(false)
    var secondsPerPage by mutableIntStateOf(initialSecondsPerPage)

    fun toggle() {
        enabled = !enabled
        if (enabled) userPaused = false
    }

    fun close() {
        enabled = false
        userPaused = false
    }
}

@Composable
internal fun rememberReaderAutoScrollState(): ReaderAutoScrollState {
    var savedEnabled by rememberSaveable { mutableStateOf(false) }
    var savedSeconds by rememberSaveable { mutableIntStateOf(AutoScrollSpeedPreset.MEDIUM.secondsPerPage) }
    val state = remember { ReaderAutoScrollState(savedEnabled, savedSeconds) }
    LaunchedEffect(state.enabled, state.secondsPerPage) {
        savedEnabled = state.enabled
        savedSeconds = state.secondsPerPage
    }
    return state
}

@Composable
internal fun ReaderAutoScrollHost(
    autoScrollState: ReaderAutoScrollState,
    isBlockedByOverlay: Boolean,
    isTwoPageSpread: Boolean,
    pagerState: PagerState,
    pageCount: Int,
    showControls: Boolean,
    isPureWirdMode: Boolean,
    showBottomWirdBanner: Boolean,
    showAudioSheet: Boolean,
    onReachedEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = autoScrollState.enabled
    val userPaused = autoScrollState.userPaused
    val secondsPerPage = autoScrollState.secondsPerPage
    var progress by remember { mutableFloatStateOf(0f) }
    val effectiveSeconds = secondsPerPage * if (isTwoPageSpread) 2 else 1
    val effectiveIntervalMs = (effectiveSeconds * 1000L).coerceAtLeast(5_000L)
    val isMovingManually = pagerState.isScrollInProgress

    LaunchedEffect(enabled, pagerState.currentPage) {
        progress = 0f
    }

    LaunchedEffect(
        enabled,
        userPaused,
        isBlockedByOverlay,
        isMovingManually,
        effectiveIntervalMs,
        pagerState.currentPage,
        pageCount
    ) {
        if (!enabled || userPaused || isBlockedByOverlay || isMovingManually) return@LaunchedEffect
        val stepMs = 100L
        while (enabled && !userPaused && !isBlockedByOverlay && !pagerState.isScrollInProgress) {
            delay(stepMs)
            val nextProgress = (progress + stepMs.toFloat() / effectiveIntervalMs.toFloat()).coerceIn(0f, 1f)
            progress = nextProgress
            if (nextProgress >= 1f) {
                val targetIndex = pagerState.currentPage + 1
                if (targetIndex < pageCount) {
                    progress = 0f
                    pagerState.animateScrollToPage(
                        page = targetIndex,
                        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
                    )
                } else {
                    progress = 0f
                    autoScrollState.close()
                    onReachedEnd()
                }
                break
            }
        }
    }

    val bottomClearance by animateDpAsState(
        targetValue = when {
            showControls && !isPureWirdMode && showBottomWirdBanner -> 200.dp
            showControls && !isPureWirdMode -> 118.dp
            showControls && isPureWirdMode && showBottomWirdBanner -> 100.dp
            else -> 14.dp
        },
        label = "auto_scroll_bottom_offset"
    )

    AnimatedVisibility(
        visible = enabled && !(showAudioSheet && !isPureWirdMode),
        enter = slideInVertically { it / 2 } + fadeIn(),
        exit = slideOutVertically { it / 2 } + fadeOut(),
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = bottomClearance)
    ) {
        ReaderAutoScrollFloatingBar(
            progress = progress,
            userPaused = userPaused,
            pausedByOverlay = isBlockedByOverlay,
            secondsPerPage = secondsPerPage,
            effectiveSeconds = effectiveSeconds,
            onTogglePlayPause = { autoScrollState.userPaused = !autoScrollState.userPaused },
            onChangeSecondsPerPage = { autoScrollState.secondsPerPage = it },
            onClose = autoScrollState::close
        )
    }
}
