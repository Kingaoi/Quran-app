package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.QuranFontOption
import com.example.data.local.ReaderSettings
import com.example.domain.model.toArabicIndicDigits
import com.example.ui.theme.LocalQuranFontFamily
import com.example.ui.theme.QURAN_LINE_HEIGHT_RATIO
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors
import com.example.ui.theme.resolveQuranFontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FontSettingsSection(
    settings: ReaderSettings,
    onSelectQuranFont: (QuranFontOption) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onResetFontSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeQuranFont = LocalQuranFontFamily.current
    val containerShape = RoundedCornerShape(18.dp)

    Surface(
        shape = containerShape,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.TextFields,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "خط القرآن والأذكار",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                TextButton(
                    onClick = onResetFontSettings,
                    modifier = Modifier.testTag("reset_font_settings_button")
                ) {
                    Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("افتراضي", style = MaterialTheme.typography.labelMedium)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                val previewSize = settings.fontSizeSp.coerceIn(18f, 30f)
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝ قُلْ هُوَ اللَّهُ أَحَدٌ",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = activeQuranFont,
                        fontSize = previewSize.sp,
                        lineHeight = (previewSize * QURAN_LINE_HEIGHT_RATIO).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                QuranFontOption.entries.forEach { option ->
                    val sel = settings.quranFont == option
                    FilterChip(
                        selected = sel,
                        onClick = { onSelectQuranFont(option) },
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(sel),
                        label = {
                            Text(
                                text = option.labelAr,
                                fontFamily = resolveQuranFontFamily(option)
                            )
                        },
                        modifier = Modifier.testTag("quran_font_${option.name}")
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.FormatSize,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حجم خط الآيات", style = MaterialTheme.typography.titleSmall)
                    }
                    Text(
                        text = settings.fontSizeSp.toInt().toArabicIndicDigits(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = settings.fontSizeSp,
                    onValueChange = onFontSizeChange,
                    valueRange = 18f..38f,
                    modifier = Modifier.testTag("font_size_slider")
                )
            }
        }
    }
}
