package com.example.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.data.local.AppTheme
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.quranColors

private val NavBarSpring = spring<Float>(
    dampingRatio = 0.86f,
    stiffness = 620f
)

@Composable
internal fun QuranBottomNavBar(
    selectedTab: HomeTab,
    onSelectTab: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = HomeTab.entries
    val selectedIndex = selectedTab.ordinal
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val visualTargetIndex = if (isRtl) (tabs.lastIndex - selectedIndex) else selectedIndex

    // Read inside draw phase so sliding beam glides smoothly without recomposing items
    val animatedVisualIndex = animateFloatAsState(
        targetValue = visualTargetIndex.toFloat(),
        animationSpec = NavBarSpring,
        label = "nav_spotlight_center"
    )

    val qc = MaterialTheme.quranColors
    val pillTop = qc.surface
    val pillBottom = qc.surface2
    val activeAccent = if (LocalAppTheme.current == AppTheme.DARK) qc.primary else qc.selected
    val goldAccent = qc.accent
    val outlineColor = qc.border
    val pillShape = RoundedCornerShape(26.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = pillShape,
            color = pillBottom,
            border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.82f)),
            shadowElevation = 10.dp,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .height(64.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clip(pillShape)
                    .drawWithCache {
                        val rowSidePadPx = 10.dp.toPx()
                        val slotWidth = (size.width - 2f * rowSidePadPx) / tabs.size.toFloat()
                        val topBarHalfW = 15.dp.toPx()
                        val bottomBeamHalfW = 29.dp.toPx()
                        val barHeightPx = 3.5.dp.toPx()
                        val barRadiusPx = 2.dp.toPx()
                        val haloRadiusPx = 24.dp.toPx()
                        val beamPath = Path()

                        val pillBgBrush = Brush.verticalGradient(
                            colors = listOf(pillTop, pillBottom),
                            startY = 0f,
                            endY = size.height
                        )
                        val beamBrush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to activeAccent.copy(alpha = 0.34f),
                                0.55f to activeAccent.copy(alpha = 0.13f),
                                1.0f to Color.Transparent
                            ),
                            startY = barHeightPx,
                            endY = size.height * 0.94f
                        )
                        val topBarBrush = Brush.horizontalGradient(
                            colors = listOf(activeAccent, goldAccent, activeAccent)
                        )
                        val haloBrush = Brush.radialGradient(
                            colors = listOf(
                                activeAccent.copy(alpha = 0.18f),
                                Color.Transparent
                            ),
                            center = Offset.Zero,
                            radius = haloRadiusPx
                        )
                        val haloTop = size.height * 0.52f

                        onDrawBehind {
                            drawRect(brush = pillBgBrush)

                            val cx = rowSidePadPx + slotWidth * (animatedVisualIndex.value + 0.5f)

                            // Trapezoidal downward spotlight cone matching reference aesthetic
                            beamPath.rewind()
                            beamPath.moveTo(cx - topBarHalfW, barHeightPx * 0.6f)
                            beamPath.lineTo(cx + topBarHalfW, barHeightPx * 0.6f)
                            beamPath.lineTo(cx + bottomBeamHalfW, size.height * 0.94f)
                            beamPath.lineTo(cx - bottomBeamHalfW, size.height * 0.94f)
                            beamPath.close()
                            drawPath(path = beamPath, brush = beamBrush)

                            // Soft halo right behind the active icon center (zero allocation per frame)
                            translate(left = cx, top = haloTop) {
                                drawCircle(
                                    brush = haloBrush,
                                    radius = haloRadiusPx,
                                    center = Offset.Zero
                                )
                            }

                            // Crisp flush top accent bar directly above the active icon
                            drawRoundRect(
                                brush = topBarBrush,
                                topLeft = Offset(cx - topBarHalfW, 0f),
                                size = Size(topBarHalfW * 2f, barHeightPx),
                                cornerRadius = CornerRadius(barRadiusPx, barRadiusPx)
                            )
                        }
                    }
                    .padding(horizontal = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEach { tab ->
                        val tabClick = remember(onSelectTab, tab) { { onSelectTab(tab) } }
                        QuranSpotlightNavItem(
                            tab = tab,
                            isSelected = tab == selectedTab,
                            activeTint = activeAccent,
                            inactiveTint = qc.textMuted.copy(alpha = 0.8f),
                            onClick = tabClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("nav_tab_${tab.route}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuranSpotlightNavItem(
    tab: HomeTab,
    isSelected: Boolean,
    activeTint: Color,
    inactiveTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val selectionProgress: State<Float> = animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "nav_spotlight_item"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(18.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = tab.labelAr
                selected = isSelected
                role = Role.Tab
            }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = activeTint),
                onClick = onClick
            )
    ) {
        Icon(
            imageVector = tab.unselectedIcon,
            contentDescription = null,
            tint = inactiveTint,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    val p = selectionProgress.value
                    val scale = 0.96f + 0.14f * p
                    scaleX = scale
                    scaleY = scale
                    alpha = (1f - p).coerceIn(0f, 1f)
                }
        )
        Icon(
            imageVector = tab.selectedIcon,
            contentDescription = null,
            tint = activeTint,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    val p = selectionProgress.value
                    val scale = 0.96f + 0.14f * p
                    scaleX = scale
                    scaleY = scale
                    alpha = p.coerceIn(0f, 1f)
                }
        )
    }
}
