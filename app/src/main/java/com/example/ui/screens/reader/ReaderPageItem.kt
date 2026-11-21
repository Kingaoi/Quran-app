package com.example.ui.screens.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import com.example.data.local.MushafOfflineStorage
import com.example.data.local.MushafRasterCache
import com.example.data.local.RiwayahEdition
import com.example.domain.model.AyahItem
import com.example.domain.model.toAppDigits
import com.example.domain.wird.WirdPortion

@Composable
internal fun MushafPagerPageItem(
    pageNum: Int,
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
    highlightStyle: com.example.data.local.AyahHighlightStyle = com.example.data.local.AyahHighlightStyle.FILL_AND_BORDER,
    highlightAlpha: Float = 0.22f,
    maskState: MemorizationMaskState = MemorizationMaskState(),
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
    val pageAyahs = remember(pageNum) { getAyahsForPage(pageNum) }
    val pageSurahName = pageAyahs.firstOrNull()?.surahNameAr ?: ""
    val pageJuz = pageAyahs.firstOrNull()?.juzNumber ?: 1

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        MushafSvgPageView(
            pageNumber = pageNum,
            riwayah = riwayah,
            markerColorKey = markerColorKey,
            ornamentHex = ornamentHex,
            numberHex = numberHex,
            surahBannerHex = surahBannerHex,
            markerAccentColor = markerAccentColor,
            svgModel = getSvgModel(pageNum),
            fallbackSvgUrl = getRawGithubSvgUrl(pageNum),
            svgImageLoader = svgImageLoader,
            rasterCache = rasterCache,
            offlineStorage = offlineStorage,
            targetWidthPx = targetWidthPx,
            targetHeightPx = targetHeightPx,
            paperBg = paperBg,
            paperInk = paperInk,
            isNightPaper = isNightPaper,
            pageAyahs = pageAyahs,
            fontSizeSp = fontSizeSp,
            highlightedAyah = highlightedAyah,
            highlightColor = highlightColor,
            highlightStyle = highlightStyle,
            highlightAlpha = highlightAlpha,
            maskState = maskState,
            wirdStartAyah = wirdBoundaryPortion?.takeIf { pageNum == it.startPage }?.let { it.startSurahNumber to it.startAyahNumber },
            wirdEndAyah = wirdBoundaryPortion?.takeIf { pageNum == it.endPage }?.let { it.endSurahNumber to it.endAyahNumber },
            onToggleRevealAyah = onToggleRevealAyah,
            onTapAyah = onTapAyah,
            onLongPressAyah = onLongPressAyah,
            onSingleTap = onSingleTap,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "الجزء ${pageJuz.toAppDigits()}",
                style = MaterialTheme.typography.labelMedium,
                color = subtleCornerInk,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .graphicsLayer { translationY = cornerShiftProgress() * 54.dp.toPx() }
            )
            if (pageSurahName.isNotEmpty()) {
                Text(
                    text = "سورة $pageSurahName",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                    color = subtleCornerInk,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .graphicsLayer { translationY = cornerShiftProgress() * 54.dp.toPx() }
                )
            }
            Text(
                text = pageNum.toAppDigits(),
                style = MaterialTheme.typography.labelMedium,
                color = subtleCornerInk,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .graphicsLayer { translationY = -bottomCornerShiftProgress() * 100.dp.toPx() }
            )
        }
    }
}
