package com.example.ui.navigation

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Immutable
internal data class PredictiveBackProgressState(
    val progress: Float = 0f,
    val swipeEdgeSign: Float = 1f
) {
    val isActive: Boolean get() = progress > 0.002f
}

@Composable
internal fun rememberPredictiveBackState(
    enabled: Boolean,
    onBack: () -> Unit
): PredictiveBackProgressState {
    val latestOnBack by rememberUpdatedState(onBack)
    val scope = rememberCoroutineScope()
    val progressAnim = remember { Animatable(0f) }
    var swipeEdgeSign by remember { mutableFloatStateOf(1f) }

    PredictiveBackHandler(enabled = enabled) { progressFlow: Flow<BackEventCompat> ->
        try {
            progressFlow.collect { backEvent ->
                swipeEdgeSign = if (backEvent.swipeEdge == BackEventCompat.EDGE_LEFT) 1f else -1f
                progressAnim.snapTo(backEvent.progress.coerceIn(0f, 1f))
            }
            progressAnim.snapTo(0f)
            latestOnBack()
        } catch (e: CancellationException) {
            scope.launch {
                progressAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = 0.85f,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            throw e
        }
    }

    return PredictiveBackProgressState(
        progress = progressAnim.value,
        swipeEdgeSign = swipeEdgeSign
    )
}

internal fun Modifier.predictiveBackTransform(
    state: PredictiveBackProgressState,
    maxCornerRadius: Dp = 28.dp,
    maxScaleDrop: Float = 0.08f,
    maxShiftDp: Dp = 20.dp,
    maxAlphaDrop: Float = 0.10f
): Modifier = this.graphicsLayer {
    val p = state.progress.coerceIn(0f, 1f)
    if (p > 0.002f) {
        val scale = 1f - maxScaleDrop * p
        scaleX = scale
        scaleY = scale
        translationX = state.swipeEdgeSign * maxShiftDp.toPx() * p
        alpha = (1f - maxAlphaDrop * p).coerceIn(0f, 1f)
        transformOrigin = TransformOrigin.Center
        shape = RoundedCornerShape(maxCornerRadius * p)
        clip = true
    }
}
