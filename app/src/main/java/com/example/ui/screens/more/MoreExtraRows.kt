package com.example.ui.screens.more

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.ui.components.OrnamentalDivider
import com.example.ui.theme.LocalQuranFontFamily

@Composable
internal fun HijriOffsetStepperRow(
    offsetDays: Int,
    onSetOffsetDays: (Int) -> Unit
) {
    val useWestern = LocalUseWesternDigits.current
    val subtitle = when {
        offsetDays == 0 -> "بدون تعديل (حسب تقويم أم القرى)"
        offsetDays > 0 -> "تقديم +${offsetDays.toAppDigits(useWestern)} يوم"
        else -> "تأخير ${offsetDays.toAppDigits(useWestern)} يوم"
    }
    val badgeLabel = when {
        offsetDays > 0 -> "+${offsetDays.toAppDigits(useWestern)}"
        else -> offsetDays.toAppDigits(useWestern)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MoreSettingIconBadge(icon = Icons.Rounded.CalendarMonth)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "تعديل التاريخ الهجري",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilledTonalIconButton(
                onClick = { onSetOffsetDays((offsetDays - 1).coerceIn(-2, 2)) },
                enabled = offsetDays > -2,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("hijri_offset_minus")
            ) {
                Icon(Icons.Rounded.Remove, contentDescription = "تأخير يوم هجري")
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = if (offsetDays == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    text = badgeLabel,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (offsetDays == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
            FilledTonalIconButton(
                onClick = { onSetOffsetDays((offsetDays + 1).coerceIn(-2, 2)) },
                enabled = offsetDays < 2,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("hijri_offset_plus")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "تقديم يوم هجري")
            }
        }
    }
}

@Composable
internal fun AdhkarFontSizeCard(
    fontSizeSp: Float,
    onFontSizeChange: (Float) -> Unit,
    onReset: () -> Unit
) {
    val useWestern = LocalUseWesternDigits.current
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.FormatSize, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حجم خط الأذكار (${fontSizeSp.toInt().toAppDigits(useWestern)})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
                TextButton(onClick = onReset, modifier = Modifier.testTag("more_reset_adhkar_font_button")) {
                    Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("افتراضي")
                }
            }
            OrnamentalDivider()
            Text(
                text = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = LocalQuranFontFamily.current, fontSize = fontSizeSp.sp, lineHeight = (fontSizeSp * 1.6f).sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Slider(value = fontSizeSp, onValueChange = onFontSizeChange, valueRange = 17f..29f, modifier = Modifier.testTag("more_adhkar_font_slider"))
        }
    }
}
