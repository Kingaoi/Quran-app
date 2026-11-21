package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AppTheme
import com.example.data.local.AyahMarkerColorPreset
import com.example.data.local.AyahMarkerColorTarget
import com.example.ui.theme.CreamOnBackgroundDark
import com.example.ui.theme.InkOnBackgroundLight
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.MushafPaperNight
import com.example.ui.theme.MushafPaperSepia
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AyahMarkerColorSection(
    selectedColor: AyahMarkerColorPreset,
    selectedTarget: AyahMarkerColorTarget,
    surahBannerEnabled: Boolean,
    onSelectColor: (AyahMarkerColorPreset) -> Unit,
    onSelectTarget: (AyahMarkerColorTarget) -> Unit,
    onToggleSurahBanner: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTheme = LocalAppTheme.current
    val isNight = activeTheme == AppTheme.DARK ||
        (activeTheme == AppTheme.SYSTEM && isSystemInDarkTheme())
    val previewBg = if (isNight) MushafPaperNight else MushafPaperSepia
    val defaultInk = if (isNight) CreamOnBackgroundDark else InkOnBackgroundLight
    val accentColor = if (isNight) selectedColor.nightPreviewColor else selectedColor.lightPreviewColor

    val ornamentColor = when {
        selectedColor == AyahMarkerColorPreset.DEFAULT -> defaultInk
        selectedTarget == AyahMarkerColorTarget.NUMBER_ONLY -> defaultInk
        else -> accentColor
    }
    val numberColor = when {
        selectedColor == AyahMarkerColorPreset.DEFAULT -> defaultInk
        selectedTarget == AyahMarkerColorTarget.ORNAMENT_ONLY -> defaultInk
        else -> accentColor
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.ColorLens,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "الزخرفة ولون الآيات",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            MushafOrnamentPreviewCard(
                previewBg = previewBg,
                defaultInk = defaultInk,
                ornamentColor = ornamentColor,
                numberColor = numberColor,
                bannerColor = accentColor,
                showSurahBanner = surahBannerEnabled
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AyahMarkerColorPreset.entries.forEach { preset ->
                    val isSelected = selectedColor == preset
                    val swatchColor = if (isNight) preset.nightPreviewColor else preset.lightPreviewColor
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectColor(preset) }
                            .padding(vertical = 4.dp)
                            .testTag("ayah_color_${preset.code}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "محدد",
                                    tint = if (isNight) MushafPaperNight else MaterialTheme.quranColors.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = preset.labelAr,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            if (selectedColor != AyahMarkerColorPreset.DEFAULT) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AyahMarkerColorTarget.entries.forEach { target ->
                        val sel = selectedTarget == target
                        FilterChip(
                            selected = sel,
                            onClick = { onSelectTarget(target) },
                            colors = quranFilterChipColors(),
                            border = quranFilterChipBorder(sel),
                            label = { Text(target.labelAr) },
                            modifier = Modifier.testTag("ayah_color_target_${target.code}")
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleSurahBanner(!surahBannerEnabled) }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "إطار مزخرف حول اسم السورة",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "تزيين عناوين السور بإطار مصحف المدينة بنفس اللون المختار",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = surahBannerEnabled,
                    onCheckedChange = onToggleSurahBanner,
                    modifier = Modifier.testTag("surah_banner_switch")
                )
            }
        }
    }
}
