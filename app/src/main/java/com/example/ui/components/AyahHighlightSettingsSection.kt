package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppTheme
import com.example.data.local.AyahHighlightColorPreset
import com.example.data.local.AyahHighlightStyle
import com.example.data.local.ReaderSettings
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.LocalQuranFontFamily
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AyahHighlightSettingsSection(
    settings: ReaderSettings,
    onSelectColor: (AyahHighlightColorPreset) -> Unit,
    onSelectStyle: (AyahHighlightStyle) -> Unit,
    onAlphaChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTheme = LocalAppTheme.current
    val isNight = activeTheme == AppTheme.DARK
    val useWestern = LocalUseWesternDigits.current
    val markerAccent = if (isNight) settings.ayahMarkerColor.nightPreviewColor else settings.ayahMarkerColor.lightPreviewColor
    val resolvedColor = settings.resolveAyahHighlightColor(isNight, markerAccent)
    val previewFill = when (settings.ayahHighlightStyle) {
        AyahHighlightStyle.BORDER_ONLY -> Color.Transparent
        AyahHighlightStyle.FILL_AND_BORDER, AyahHighlightStyle.SOFT_FILL -> resolvedColor.copy(alpha = settings.ayahHighlightAlpha)
    }
    val previewBorder = when (settings.ayahHighlightStyle) {
        AyahHighlightStyle.SOFT_FILL -> null
        AyahHighlightStyle.FILL_AND_BORDER -> BorderStroke(1.5.dp, resolvedColor.copy(alpha = (settings.ayahHighlightAlpha + 0.48f).coerceIn(0.55f, 0.92f)))
        AyahHighlightStyle.BORDER_ONLY -> BorderStroke(2.dp, resolvedColor.copy(alpha = (settings.ayahHighlightAlpha + 0.55f).coerceIn(0.65f, 0.96f)))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = previewFill,
            border = previewBorder,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("highlight_live_preview_box")
        ) {
            Text(
                text = "﴿بِسْمِ اللَّهِ الرَّحْمَـٰنِ الرَّحِيمِ ١﴾",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = LocalQuranFontFamily.current,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }

        Text(
            text = "لون التظليل",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val qc = MaterialTheme.quranColors
            AyahHighlightColorPreset.entries.forEach { preset ->
                val selected = preset == settings.ayahHighlightColor
                val swatch = preset.resolveColor(isNight, markerAccent)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) qc.selected else qc.surface,
                    border = BorderStroke(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) qc.selected else qc.border
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectColor(preset) }
                        .testTag("highlight_color_${preset.code}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(swatch)
                                .border(1.dp, qc.onSelected.copy(alpha = 0.65f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                selected -> Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = qc.onSelected,
                                    modifier = Modifier.size(12.dp)
                                )
                                preset == AyahHighlightColorPreset.FOLLOW_MARKER -> Icon(
                                    imageVector = Icons.Rounded.Link,
                                    contentDescription = null,
                                    tint = qc.onSelected,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                        Text(
                            text = preset.labelAr,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) qc.onSelected else qc.text
                        )
                    }
                }
            }
        }

        Text(
            text = "نمط التحديد",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AyahHighlightStyle.entries.forEach { style ->
                val sel = settings.ayahHighlightStyle == style
                FilterChip(
                    selected = sel,
                    onClick = { onSelectStyle(style) },
                    colors = quranFilterChipColors(),
                    border = quranFilterChipBorder(sel),
                    label = { Text(style.labelAr) },
                    modifier = Modifier.testTag("highlight_style_${style.name}")
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (settings.ayahHighlightStyle == AyahHighlightStyle.BORDER_ONLY) "حدّة الإطار" else "شفافية التظليل",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(settings.ayahHighlightAlpha * 100f).roundToInt().toAppDigits(useWestern)}٪",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = settings.ayahHighlightAlpha,
                onValueChange = onAlphaChange,
                valueRange = 0.10f..0.42f,
                modifier = Modifier.testTag("highlight_alpha_slider")
            )
            Spacer(modifier = Modifier.size(2.dp))
        }
    }
}
