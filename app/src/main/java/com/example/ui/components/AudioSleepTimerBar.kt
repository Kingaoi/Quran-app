package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.audio.AudioPlaybackState
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.localizeDigits
import com.example.domain.model.toAppDigits

private val SLEEP_PRESETS_MIN = listOf(15, 30, 45, 60)

@Composable
internal fun AudioSleepTimerBar(
    playbackState: AudioPlaybackState,
    onSetSleepTimerMinutes: (Int?) -> Unit,
    onToggleStopAfterSurah: () -> Unit,
    modifier: Modifier = Modifier
) {
    val useWestern = LocalUseWesternDigits.current
    val remainingSec = playbackState.sleepTimerRemainingSec
    val activeCountdownMin = remainingSec?.let { ((it + 59) / 60) }
    val isActive = remainingSec != null || playbackState.stopAfterCurrentSurah

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bedtime,
                    contentDescription = null,
                    tint = if (isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "مؤقت النوم",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (remainingSec != null) {
                Surface(
                    onClick = { onSetSleepTimerMinutes(null) },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                    modifier = Modifier.testTag("sleep_timer_cancel_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        val mm = (remainingSec / 60).toString().padStart(2, '0')
                        val ss = (remainingSec % 60).toString().padStart(2, '0')
                        Text(
                            text = "${mm}:${ss}".localizeDigits(useWestern),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "إلغاء مؤقت النوم",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SLEEP_PRESETS_MIN.forEach { minutes ->
                val isSelected = remainingSec != null && activeCountdownMin in (minutes - 1)..minutes
                FilterChip(
                    selected = isSelected,
                    onClick = { onSetSleepTimerMinutes(if (isSelected) null else minutes) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                    ),
                    label = {
                        Text(
                            text = "${minutes.toAppDigits(useWestern)} د",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    modifier = Modifier.testTag("sleep_timer_${minutes}m_chip")
                )
            }
            FilterChip(
                selected = playbackState.stopAfterCurrentSurah,
                onClick = onToggleStopAfterSurah,
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = playbackState.stopAfterCurrentSurah,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)
                ),
                label = {
                    Text(
                        text = "نهاية السورة",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                modifier = Modifier.testTag("sleep_timer_end_surah_chip")
            )
        }
    }
}
