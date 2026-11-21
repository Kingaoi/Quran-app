package com.example.ui.screens.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PauseCircleOutline
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.audio.PlaybackNotificationButton
import com.example.data.audio.PlaybackNotificationConfig

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PlaybackNotificationSettingsRows(
    config: PlaybackNotificationConfig,
    onSetEnabled: (Boolean) -> Unit,
    onToggleButton: (PlaybackNotificationButton) -> Unit,
    onSetShowWhenPaused: (Boolean) -> Unit
) {
    MoreGroupDivider()

    MoreSettingSwitchRow(
        title = "إشعار التلاوة في الخلفية",
        subtitle = "استمرار التلاوة والتحكم بها من شريط الإشعارات وشاشة القفل",
        icon = Icons.Rounded.NotificationsActive,
        checked = config.enabled,
        onCheckedChange = onSetEnabled,
        testTagKey = "more_playback_notif_enabled_switch"
    )

    if (config.enabled) {
        MoreGroupDivider()

        MoreSettingSwitchRow(
            title = "إبقاء الإشعار عند الإيقاف المؤقت",
            subtitle = "إتاحة استئناف التلاوة مباشرة بعد إيقافها مؤقتاً",
            icon = Icons.Rounded.PauseCircleOutline,
            checked = config.showWhenPaused,
            onCheckedChange = onSetShowWhenPaused,
            testTagKey = "more_playback_notif_paused_switch"
        )

        MoreGroupDivider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MoreSettingIconBadge(icon = Icons.Rounded.Tune)
                Column {
                    Text(
                        text = "أزرار إشعار التلاوة",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "اختر الأزرار التي تظهر بجانب زر التشغيل/الإيقاف في الإشعار",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PlaybackNotificationButton.entries.forEach { btn ->
                    FilterChip(
                        selected = config.hasButton(btn),
                        onClick = { onToggleButton(btn) },
                        label = { Text(btn.labelAr) },
                        modifier = Modifier.testTag("notif_btn_chip_${btn.id}")
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlaybackNotifCustomizeSheet(
    config: PlaybackNotificationConfig,
    onSetEnabled: (Boolean) -> Unit,
    onToggleButton: (PlaybackNotificationButton) -> Unit,
    onSetShowWhenPaused: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "تخصيص إشعار التلاوة",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            MoreSettingsGroupCard {
                PlaybackNotificationSettingsRows(
                    config = config,
                    onSetEnabled = onSetEnabled,
                    onToggleButton = onToggleButton,
                    onSetShowWhenPaused = onSetShowWhenPaused
                )
            }
        }
    }
}
