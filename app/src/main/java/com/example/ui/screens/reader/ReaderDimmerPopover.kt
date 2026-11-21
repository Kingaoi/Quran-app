package com.example.ui.screens.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.toAppDigits
import com.example.ui.components.QuranDialogPresetChip
import com.example.ui.components.QuranStyledDialog
import kotlin.math.roundToInt

private val DIMMER_PRESETS = listOf(
    0f to "عادي",
    0.18f to "خفيف",
    0.38f to "مريح",
    0.58f to "ليلي"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ReaderDimmerPopover(
    dimmerAlpha: Float,
    keepScreenOn: Boolean,
    onChangeDimmer: (Float) -> Unit,
    onChangeKeepScreenOn: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val dimPercent = ((dimmerAlpha / 0.65f) * 100f).roundToInt().coerceIn(0, 100)

    QuranStyledDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Rounded.BrightnessMedium,
        title = "راحة العين والسطوع",
        subtitle = "خفّف إضاءة الشاشة للقراءة المريحة وحافظ على إضاءتها",
        confirmLabel = "تم",
        dismissLabel = null,
        onConfirm = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "خافت الإضاءة الليلي",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${dimPercent.toAppDigits()}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                Slider(
                    value = dimmerAlpha,
                    onValueChange = onChangeDimmer,
                    valueRange = 0f..0.65f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reader_dimmer_slider")
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DIMMER_PRESETS.forEach { (presetAlpha, label) ->
                        val isClosest = kotlin.math.abs(dimmerAlpha - presetAlpha) < 0.05f
                        QuranDialogPresetChip(
                            label = label,
                            selected = isClosest,
                            onClick = { onChangeDimmer(presetAlpha) }
                        )
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChangeKeepScreenOn(!keepScreenOn) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LightMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Column {
                        Text(
                            text = "إبقاء الشاشة مضاءة",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "عدم إطفاء الشاشة تلقائياً أثناء القراءة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = keepScreenOn,
                    onCheckedChange = onChangeKeepScreenOn,
                    modifier = Modifier.testTag("reader_keep_screen_on_switch")
                )
            }
        }
    }
}
