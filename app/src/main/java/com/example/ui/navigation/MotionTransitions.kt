package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation.NavBackStackEntry

@OptIn(ExperimentalSharedTransitionApi::class)
internal val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

internal val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

internal val LocalSharedTransitionReady = compositionLocalOf { true }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun QuranSharedTransitionHost(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { ready = true }
    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this@SharedTransitionLayout,
            LocalSharedTransitionReady provides ready
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}

internal sealed interface QuranSharedKey {
    @Immutable
    data class AdhkarCategoryContainer(val categoryId: String) : QuranSharedKey

    @Immutable
    data class AdhkarCategoryIcon(val categoryId: String) : QuranSharedKey

    @Immutable
    data class AdhkarCategoryTitle(val categoryId: String) : QuranSharedKey

    @Immutable
    data class AdhkarChapterTitle(val categoryId: String, val chapterTitle: String) : QuranSharedKey

    @Immutable
    data class AdhkarItemCard(val itemId: String) : QuranSharedKey

    @Immutable
    data class MoreCategoryCard(val categoryName: String) : QuranSharedKey

    @Immutable
    data class MoreCategoryIcon(val categoryName: String) : QuranSharedKey

    @Immutable
    data class MoreCategoryTitle(val categoryName: String) : QuranSharedKey

    @Immutable
    data object WirdPlansAnchor : QuranSharedKey
}

internal object QuranMotionSpecs {
    private val emphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    fun navEnterTransition(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(durationMillis = 240, easing = emphasizedEasing)) +
            scaleIn(
                initialScale = 0.94f,
                transformOrigin = TransformOrigin.Center,
                animationSpec = tween(durationMillis = 260, easing = emphasizedEasing)
            )
    }

    fun navExitTransition(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(durationMillis = 190, easing = emphasizedEasing)) +
            scaleOut(
                targetScale = 0.96f,
                transformOrigin = TransformOrigin.Center,
                animationSpec = tween(durationMillis = 210, easing = emphasizedEasing)
            )
    }

    fun navPopEnterTransition(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(durationMillis = 220, easing = emphasizedEasing)) +
            scaleIn(
                initialScale = 0.94f,
                transformOrigin = TransformOrigin.Center,
                animationSpec = tween(durationMillis = 240, easing = emphasizedEasing)
            )
    }

    // Center-origin scale+fade works seamlessly whether back swipe originates from left or right edge in RTL
    fun navPopExitTransition(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(durationMillis = 210, easing = emphasizedEasing)) +
            scaleOut(
                targetScale = 0.88f,
                transformOrigin = TransformOrigin.Center,
                animationSpec = tween(durationMillis = 230, easing = emphasizedEasing)
            )
    }

    fun <S> subScreenTransition(isForward: (initial: S, target: S) -> Boolean):
        AnimatedContentTransitionScope<S>.() -> ContentTransform = {
        if (isForward(initialState, targetState)) {
            (fadeIn(tween(240, easing = emphasizedEasing)) +
                scaleIn(initialScale = 0.95f, animationSpec = tween(250, easing = emphasizedEasing)))
                .togetherWith(
                    fadeOut(tween(180, easing = emphasizedEasing)) +
                        scaleOut(targetScale = 0.97f, animationSpec = tween(180, easing = emphasizedEasing))
                )
        } else {
            (fadeIn(tween(220, easing = emphasizedEasing)) +
                scaleIn(initialScale = 0.94f, animationSpec = tween(230, easing = emphasizedEasing)))
                .togetherWith(
                    fadeOut(tween(200, easing = emphasizedEasing)) +
                        scaleOut(targetScale = 0.90f, animationSpec = tween(220, easing = emphasizedEasing))
                )
        }
    }

    fun wizardStepTransition(isRtl: Boolean): AnimatedContentTransitionScope<Int>.() -> ContentTransform = {
        val forward = targetState > initialState
        val dir = if (isRtl) (if (forward) -1 else 1) else (if (forward) 1 else -1)
        (fadeIn(tween(220, easing = emphasizedEasing)) +
            slideInHorizontally(tween(240, easing = emphasizedEasing)) { fullW -> dir * (fullW / 5) })
            .togetherWith(
                fadeOut(tween(160, easing = emphasizedEasing)) +
                    slideOutHorizontally(tween(200, easing = emphasizedEasing)) { fullW -> -dir * (fullW / 6) }
            )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.quranSharedBounds(
    key: QuranSharedKey,
    zIndexInOverlay: Float = 0f,
    useScaleToBounds: Boolean = true
): Modifier {
    if (!LocalSharedTransitionReady.current) return this
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val animatedScope = LocalAnimatedVisibilityScope.current ?: return this
    val resizeMode = if (useScaleToBounds) {
        SharedTransitionScope.ResizeMode.ScaleToBounds()
    } else {
        SharedTransitionScope.ResizeMode.RemeasureToBounds
    }
    return with(sharedScope) {
        this@quranSharedBounds.sharedBounds(
            rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedScope,
            boundsTransform = { _, _ ->
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            },
            resizeMode = resizeMode,
            zIndexInOverlay = zIndexInOverlay
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.quranSharedElement(
    key: QuranSharedKey,
    zIndexInOverlay: Float = 0f
): Modifier {
    if (!LocalSharedTransitionReady.current) return this
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val animatedScope = LocalAnimatedVisibilityScope.current ?: return this
    return with(sharedScope) {
        this@quranSharedElement.sharedElement(
            rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedScope,
            boundsTransform = { _, _ ->
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            },
            zIndexInOverlay = zIndexInOverlay
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.quranFloatingChromeOverlay(zIndexInOverlay: Float = 2f): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    return with(sharedScope) {
        this@quranFloatingChromeOverlay.renderInSharedTransitionScopeOverlay(
            zIndexInOverlay = zIndexInOverlay
        )
    }
}
