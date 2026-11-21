package com.example.ui.screens.reader

import android.content.Context
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.example.data.local.AyahHighlightStyle
import com.example.data.local.MushafOfflineStorage
import com.example.data.local.MushafRasterCache
import com.example.data.local.RiwayahEdition
import com.example.domain.model.AyahItem
import com.example.domain.wird.WirdPortion

@Composable
internal fun MushafPagerViewport(
    pagerState: PagerState,
    isTwoPageSpread: Boolean,
    isNightPaper: Boolean,
    riwayah: RiwayahEdition,
    markerColorKey: String,
    ornamentHex: String?,
    numberHex: String?,
    surahBannerHex: String?,
    markerAccentColor: Color,
    fontSizeSp: Float,
    svgImageLoader: ImageLoader,
    rasterCache: MushafRasterCache,
    offlineStorage: MushafOfflineStorage,
    targetWidthPx: Int,
    targetHeightPx: Int,
    paperBg: Color,
    paperInk: Color,
    subtleCornerInk: Color,
    cornerShiftProgress: () -> Float,
    bottomCornerShiftProgress: () -> Float = cornerShiftProgress,
    highlightedAyah: Pair<Int, Int>?,
    highlightColor: Color = markerAccentColor,
    highlightStyle: AyahHighlightStyle = AyahHighlightStyle.FILL_AND_BORDER,
    highlightAlpha: Float = 0.22f,
    maskState: MemorizationMaskState = MemorizationMaskState(),
    scopedStartPage: Int = 1,
    scopedEndPage: Int = 604,
    wirdBoundaryPortion: WirdPortion? = null,
    onToggleRevealAyah: (Pair<Int, Int>) -> Unit = {},
    getAyahsForPage: (Int) -> List<AyahItem>,
    getSvgModel: (Int) -> Any,
    getRawGithubSvgUrl: (Int) -> String,
    onTapAyah: (AyahItem) -> Unit = {},
    onLongPressAyah: (AyahItem) -> Unit,
    onSingleTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = pagerState,
        key = { it },
        modifier = modifier.fillMaxSize(),
        beyondViewportPageCount = 1,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            pagerSnapDistance = PagerSnapDistance.atMost(2),
            snapAnimationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 800f),
            snapPositionalThreshold = 0.25f
        )
    ) { pagerIndex ->
        if (!isTwoPageSpread) {
            val logicalPage = (scopedStartPage + pagerIndex).coerceIn(scopedStartPage, scopedEndPage)
            MushafPagerPageItem(
                pageNum = logicalPage, isNightPaper = isNightPaper, riwayah = riwayah,
                markerColorKey = markerColorKey, ornamentHex = ornamentHex, numberHex = numberHex, surahBannerHex = surahBannerHex,
                markerAccentColor = markerAccentColor, fontSizeSp = fontSizeSp, svgImageLoader = svgImageLoader, rasterCache = rasterCache,
                offlineStorage = offlineStorage, targetWidthPx = targetWidthPx, targetHeightPx = targetHeightPx, paperBg = paperBg,
                paperInk = paperInk, subtleCornerInk = subtleCornerInk, cornerShiftProgress = cornerShiftProgress,
                bottomCornerShiftProgress = bottomCornerShiftProgress, highlightedAyah = highlightedAyah, highlightColor = highlightColor, highlightStyle = highlightStyle,
                highlightAlpha = highlightAlpha, maskState = maskState, wirdBoundaryPortion = wirdBoundaryPortion, onToggleRevealAyah = onToggleRevealAyah,
                getAyahsForPage = getAyahsForPage, getSvgModel = getSvgModel, getRawGithubSvgUrl = getRawGithubSvgUrl,
                onTapAyah = onTapAyah, onLongPressAyah = onLongPressAyah, onSingleTap = onSingleTap
            )
        } else {
            val rightPage = (scopedStartPage + pagerIndex * 2).coerceIn(scopedStartPage, scopedEndPage)
            val leftPage = rightPage + 1
            Row(modifier = Modifier.fillMaxSize()) {
                MushafPagerPageItem(
                    pageNum = rightPage, isNightPaper = isNightPaper, riwayah = riwayah,
                    markerColorKey = markerColorKey, ornamentHex = ornamentHex, numberHex = numberHex, surahBannerHex = surahBannerHex,
                    markerAccentColor = markerAccentColor, fontSizeSp = fontSizeSp, svgImageLoader = svgImageLoader, rasterCache = rasterCache,
                    offlineStorage = offlineStorage, targetWidthPx = targetWidthPx / 2, targetHeightPx = targetHeightPx, paperBg = paperBg,
                    paperInk = paperInk, subtleCornerInk = subtleCornerInk, cornerShiftProgress = cornerShiftProgress,
                    bottomCornerShiftProgress = bottomCornerShiftProgress, highlightedAyah = highlightedAyah, highlightColor = highlightColor, highlightStyle = highlightStyle,
                    highlightAlpha = highlightAlpha, maskState = maskState, wirdBoundaryPortion = wirdBoundaryPortion, onToggleRevealAyah = onToggleRevealAyah,
                    getAyahsForPage = getAyahsForPage, getSvgModel = getSvgModel, getRawGithubSvgUrl = getRawGithubSvgUrl,
                    onTapAyah = onTapAyah, onLongPressAyah = onLongPressAyah, onSingleTap = onSingleTap,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Box(
                    modifier = Modifier.width(10.dp).fillMaxHeight().background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = if (isNightPaper) 0.35f else 0.10f), Color.Transparent)
                        )
                    )
                )
                if (leftPage <= scopedEndPage) {
                    MushafPagerPageItem(
                        pageNum = leftPage, isNightPaper = isNightPaper, riwayah = riwayah,
                        markerColorKey = markerColorKey, ornamentHex = ornamentHex, numberHex = numberHex, surahBannerHex = surahBannerHex,
                        markerAccentColor = markerAccentColor, fontSizeSp = fontSizeSp, svgImageLoader = svgImageLoader, rasterCache = rasterCache,
                        offlineStorage = offlineStorage, targetWidthPx = targetWidthPx / 2, targetHeightPx = targetHeightPx, paperBg = paperBg,
                        paperInk = paperInk, subtleCornerInk = subtleCornerInk, cornerShiftProgress = cornerShiftProgress,
                        bottomCornerShiftProgress = bottomCornerShiftProgress, highlightedAyah = highlightedAyah, highlightColor = highlightColor, highlightStyle = highlightStyle,
                        highlightAlpha = highlightAlpha, maskState = maskState, wirdBoundaryPortion = wirdBoundaryPortion, onToggleRevealAyah = onToggleRevealAyah,
                        getAyahsForPage = getAyahsForPage, getSvgModel = getSvgModel, getRawGithubSvgUrl = getRawGithubSvgUrl,
                        onTapAyah = onTapAyah, onLongPressAyah = onLongPressAyah, onSingleTap = onSingleTap,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                } else {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(paperBg))
                }
            }
        }
    }
}

@Composable
internal fun MushafPredictivePrefetchEffect(
    activePageNum: Int,
    riwayah: RiwayahEdition,
    isTwoPageSpread: Boolean,
    svgVersion: String,
    markerColorKey: String,
    ornamentHex: String?,
    numberHex: String?,
    surahBannerHex: String?,
    targetWidthPx: Int,
    targetHeightPx: Int,
    context: Context,
    imageLoader: ImageLoader,
    rasterCache: MushafRasterCache,
    offlineStorage: MushafOfflineStorage,
    getSvgModel: (Int) -> Any,
    getRawGithubSvgUrl: (Int) -> String,
    scopedStartPage: Int = 1,
    scopedEndPage: Int = 604
) {
    var lastPrefetchAnchor by remember { mutableIntStateOf(activePageNum) }
    LaunchedEffect(activePageNum, riwayah.slug, isTwoPageSpread, svgVersion, markerColorKey, targetWidthPx, targetHeightPx, scopedStartPage, scopedEndPage) {
        val dir = if (activePageNum >= lastPrefetchAnchor) 1 else -1
        lastPrefetchAnchor = activePageNum
        runMushafPredictivePrefetch(
            activePageNum = activePageNum,
            dir = dir,
            riwayah = riwayah,
            svgVersion = svgVersion,
            isTwoPageSpread = isTwoPageSpread,
            markerColorKey = markerColorKey,
            ornamentHex = ornamentHex,
            numberHex = numberHex,
            surahBannerHex = surahBannerHex,
            targetWidthPx = targetWidthPx,
            targetHeightPx = targetHeightPx,
            context = context,
            imageLoader = imageLoader,
            rasterCache = rasterCache,
            offlineStorage = offlineStorage,
            getSvgModel = getSvgModel,
            getRawGithubSvgUrl = getRawGithubSvgUrl,
            scopedStartPage = scopedStartPage,
            scopedEndPage = scopedEndPage
        )
    }
}
