package com.example.ui.screens.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.data.local.MushafOfflineStorage
import com.example.data.local.MushafRasterCache
import com.example.data.local.RiwayahEdition
import com.example.domain.model.AyahItem
import com.example.domain.model.toAppDigits
import com.example.ui.components.MushafCropCoordinateMap
import com.example.ui.components.MushafSvgAyahMap
import com.example.ui.components.SvgPageAyahMap
import java.io.File

@Composable
internal fun MushafSvgPageView(
    pageNumber: Int,
    riwayah: RiwayahEdition,
    markerColorKey: String,
    ornamentHex: String?,
    numberHex: String?,
    surahBannerHex: String?,
    markerAccentColor: Color,
    svgModel: Any,
    fallbackSvgUrl: String,
    svgImageLoader: ImageLoader,
    rasterCache: MushafRasterCache,
    offlineStorage: MushafOfflineStorage,
    targetWidthPx: Int,
    targetHeightPx: Int,
    paperBg: Color,
    paperInk: Color,
    isNightPaper: Boolean,
    pageAyahs: List<AyahItem>,
    fontSizeSp: Float,
    highlightedAyah: Pair<Int, Int>?,
    highlightColor: Color = markerAccentColor,
    highlightStyle: com.example.data.local.AyahHighlightStyle = com.example.data.local.AyahHighlightStyle.FILL_AND_BORDER,
    highlightAlpha: Float = 0.22f,
    maskState: MemorizationMaskState = MemorizationMaskState(),
    wirdStartAyah: Pair<Int, Int>? = null,
    wirdEndAyah: Pair<Int, Int>? = null,
    onToggleRevealAyah: (Pair<Int, Int>) -> Unit = {},
    onTapAyah: (AyahItem) -> Unit = {},
    onLongPressAyah: (AyahItem) -> Unit,
    onSingleTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var userZoom by remember(pageNumber) { mutableFloatStateOf(1f) }
    var offset by remember(pageNumber) { mutableStateOf(Offset.Zero) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var svgAyahMap by remember(pageNumber, riwayah.slug) { mutableStateOf<SvgPageAyahMap?>(null) }
    var useFallbackUrl by remember(svgModel) { mutableStateOf(false) }
    var bypassRasterCache by remember(pageNumber, riwayah.slug, markerColorKey) { mutableStateOf(false) }
    var retryTick by remember(pageNumber, riwayah.slug, markerColorKey) { mutableIntStateOf(0) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val pageKey = "${riwayah.slug}_$pageNumber"
    val metricsRevision by MushafCropCoordinateMap.metricsRevision.collectAsState()

    val nightFilter = remember(isNightPaper) {
        if (isNightPaper) {
            ColorFilter.colorMatrix(
                ColorMatrix(
                    floatArrayOf(
                        -1f, 0f, 0f, 0f, 243f,
                        0f, -1f, 0f, 0f, 235f,
                        0f, 0f, -1f, 0f, 218f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
        } else {
            null
        }
    }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val nextScale = (userZoom * zoomChange).coerceIn(1f, 2.8f)
        userZoom = nextScale
        if (nextScale > 1.02f) offset += panChange else offset = Offset.Zero
    }

    val activeModel = if (useFallbackUrl) fallbackSvgUrl else svgModel
    val imageRequest = remember(
        activeModel,
        riwayah.slug,
        pageNumber,
        targetWidthPx,
        targetHeightPx,
        markerColorKey,
        ornamentHex,
        numberHex,
        surahBannerHex,
        bypassRasterCache,
        retryTick,
        context
    ) {
        buildMushafImageRequest(
            context = context,
            model = activeModel,
            riwayahSlug = riwayah.slug,
            pageNumber = pageNumber,
            targetWidthPx = targetWidthPx,
            targetHeightPx = targetHeightPx,
            rasterCache = rasterCache,
            bypassRasterCache = bypassRasterCache,
            markerColorKey = markerColorKey,
            ornamentHex = ornamentHex,
            numberHex = numberHex,
            surahBannerHex = surahBannerHex
        )
    }

    val painter = rememberAsyncImagePainter(
        model = imageRequest,
        imageLoader = svgImageLoader,
        contentScale = ContentScale.Fit
    )
    val painterState = painter.state

    LaunchedEffect(pageNumber, riwayah.slug, painterState) {
        MushafCropCoordinateMap.ensureMetricsLoaded(pageKey)
        if (svgAyahMap == null && (painterState is AsyncImagePainter.State.Success || offlineStorage.getCachedPageFileFast(riwayah, pageNumber) != null)) {
            val urls = buildList {
                (svgModel as? String)?.let { add(it) }
                add(fallbackSvgUrl)
            }
            svgAyahMap = MushafSvgAyahMap.loadPageMap(context, offlineStorage, riwayah, pageNumber, urls)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(paperBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 24.dp, bottom = 24.dp)
            .clipToBounds()
            .onSizeChanged { boxSize = it }
            .pointerInput(pageNumber, svgAyahMap, pageAyahs, maskState.hideMode) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        val hit = resolveAyahAtTap(tapOffset, boxSize, userZoom, offset, svgAyahMap, pageKey)
                        if (maskState.hideMode) {
                            if (hit != null) onToggleRevealAyah(hit) else onSingleTap()
                        } else if (hit != null) {
                            val matched = pageAyahs.firstOrNull {
                                it.surahNumber == hit.first && it.ayahNumber == hit.second
                            }
                            if (matched != null) onTapAyah(matched) else onSingleTap()
                        } else {
                            onSingleTap()
                        }
                    },
                    onDoubleTap = {
                        if (userZoom > 1.1f) {
                            userZoom = 1f
                            offset = Offset.Zero
                        } else {
                            userZoom = 1.85f
                        }
                    },
                    onLongPress = { tapOffset ->
                        if (!maskState.hideMode) {
                            resolveLongPressedAyah(
                                tapOffset = tapOffset,
                                boxSize = boxSize,
                                userZoom = userZoom,
                                panOffset = offset,
                                svgMap = svgAyahMap,
                                pageKey = pageKey,
                                pageAyahs = pageAyahs,
                                haptic = haptic,
                                onLongPressAyah = onLongPressAyah
                            )
                        }
                    }
                )
            }
    ) {
        Image(
            painter = painter,
            contentDescription = "صفحة المصحف الشريف رقم $pageNumber",
            contentScale = ContentScale.Fit,
            colorFilter = nightFilter,
            modifier = Modifier
                .fillMaxSize()
                .then(if (userZoom > 1.02f) Modifier.transformable(state = transformState) else Modifier)
                .graphicsLayer {
                    scaleX = userZoom
                    scaleY = userZoom
                    translationX = offset.x
                    translationY = offset.y
                }
        )

        ReaderMemorizationVeilOverlay(
            svgMap = svgAyahMap,
            pageKey = pageKey,
            pageAyahs = pageAyahs,
            maskState = maskState,
            paperBg = paperBg,
            accentColor = markerAccentColor,
            userZoom = userZoom,
            panOffset = offset,
            metricsRevision = metricsRevision
        )

        ReaderSvgAyahHighlightOverlay(
            svgMap = svgAyahMap,
            pageKey = pageKey,
            highlightedAyah = highlightedAyah,
            accentColor = highlightColor,
            highlightStyle = highlightStyle,
            highlightAlpha = highlightAlpha,
            userZoom = userZoom,
            panOffset = offset,
            metricsRevision = metricsRevision
        )

        ReaderWirdMarkersOverlay(
            svgMap = svgAyahMap,
            pageKey = pageKey,
            wirdStartAyah = wirdStartAyah,
            wirdEndAyah = wirdEndAyah,
            startColor = MaterialTheme.colorScheme.primary,
            endColor = MaterialTheme.colorScheme.secondary,
            userZoom = userZoom,
            panOffset = offset,
            metricsRevision = metricsRevision
        )

        if (!isNightPaper) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.03f),
                            0.06f to Color.Transparent,
                            0.94f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.03f)
                        )
                    )
            )
        }

        when (painterState) {
            is AsyncImagePainter.State.Loading -> {
                val hasRaster = remember(riwayah.slug, pageNumber, targetWidthPx, targetHeightPx, markerColorKey, bypassRasterCache) {
                    !bypassRasterCache && rasterCache.getCachedFileIfReady(riwayah.slug, pageNumber, targetWidthPx, targetHeightPx, markerColorKey) != null
                }
                val showSpinner by androidx.compose.runtime.produceState(false, activeModel, pageNumber, hasRaster) {
                    value = false
                    if (activeModel !is File && !hasRaster) {
                        kotlinx.coroutines.delay(140L)
                        value = true
                    }
                }
                if (showSpinner) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("جاري تحميل الصفحة ${pageNumber.toAppDigits()}...", style = MaterialTheme.typography.bodySmall, color = paperInk.copy(alpha = 0.7f))
                        }
                    }
                }
            }
            is AsyncImagePainter.State.Error -> {
                if (!bypassRasterCache &&
                    rasterCache.getCachedFileIfReady(riwayah.slug, pageNumber, targetWidthPx, targetHeightPx, markerColorKey) != null
                ) {
                    LaunchedEffect(pageNumber, riwayah.slug, markerColorKey) {
                        rasterCache.invalidatePage(riwayah.slug, pageNumber, targetWidthPx, targetHeightPx, markerColorKey)
                        bypassRasterCache = true
                        retryTick++
                    }
                } else if (!useFallbackUrl && activeModel != fallbackSvgUrl) {
                    LaunchedEffect(activeModel) { useFallbackUrl = true }
                } else {
                    OfflineMushafPageFallback(pageAyahs, paperInk, markerAccentColor, fontSizeSp)
                }
            }
            else -> Unit
        }
    }
}
