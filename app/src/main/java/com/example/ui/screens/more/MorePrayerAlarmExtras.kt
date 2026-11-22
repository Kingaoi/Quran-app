package com.example.ui.screens.more

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.data.local.AdhanSoundMode
import com.example.data.local.PrayerSettings
import com.example.data.prayer.ExactAlarmCompat
import com.example.domain.prayer.AdhanVoices
import com.example.ui.theme.quranColors

@Composable
internal fun MorePrayerAlarmExtrasGroup(
    prayerSettings: PrayerSettings,
    cachedAdhanVoiceIds: Set<String>,
    onOpenAdhanVoicePicker: () -> Unit,
    onTestPrayerAlert: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var canExact by remember(context) { mutableStateOf(ExactAlarmCompat.canScheduleExactAlarms(context)) }
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        onTestPrayerAlert()
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canExact = ExactAlarmCompat.canScheduleExactAlarms(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val selectedVoice = remember(prayerSettings.selectedAdhanVoiceId) {
        AdhanVoices.byId(prayerSettings.selectedAdhanVoiceId)
    }
    val isVoiceCached = selectedVoice.id in cachedAdhanVoiceIds
    val cachedSuffix = if (isVoiceCached) stringResource(R.string.more_adhan_voice_cached_suffix) else ""
    val fullOnlySuffix = if (prayerSettings.adhanSoundMode != AdhanSoundMode.FULL_ADHAN) stringResource(R.string.more_adhan_voice_full_only_suffix) else ""
    val subtitleText = selectedVoice.nameAr + cachedSuffix + fullOnlySuffix

    val qc = MaterialTheme.quranColors

    MoreGroupDivider()

    MoreSettingActionRow(
        title = stringResource(R.string.more_adhan_voice_title),
        subtitle = subtitleText,
        icon = Icons.Rounded.GraphicEq,
        iconTint = qc.categoryPastelAt(4),
        onClick = onOpenAdhanVoicePicker,
        testTagKey = "more_adhan_voice_row"
    )

    MoreGroupDivider()

    MoreSettingActionRow(
        title = stringResource(R.string.more_exact_alarm_title),
        subtitle = stringResource(
            if (canExact) R.string.more_exact_alarm_enabled_sub else R.string.more_exact_alarm_disabled_sub
        ),
        icon = Icons.Rounded.AlarmOn,
        iconTint = if (canExact) qc.success else qc.danger,
        onClick = {
            if (!canExact) {
                runCatching { context.startActivity(ExactAlarmCompat.buildExactAlarmSettingsIntent(context)) }
            }
        },
        testTagKey = "more_exact_alarm_row",
        trailingAccessory = {
            Surface(
                shape = RoundedCornerShape(50),
                color = if (canExact) qc.successContainer else qc.dangerContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (canExact) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = if (canExact) qc.success else qc.danger,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(
                            if (canExact) R.string.more_exact_alarm_badge_on else R.string.more_exact_alarm_badge_off
                        ),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (canExact) qc.success else qc.danger
                    )
                }
            }
        }
    )

    MoreGroupDivider()

    MoreSettingActionRow(
        title = stringResource(R.string.more_test_prayer_alert_title),
        subtitle = stringResource(R.string.more_test_prayer_alert_sub),
        icon = Icons.Rounded.NotificationsActive,
        iconTint = qc.categoryPastelAt(0),
        onClick = {
            val needsPerm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            if (needsPerm) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onTestPrayerAlert()
            }
        },
        testTagKey = "more_test_prayer_alert_row"
    )
}

@Composable
fun ExactAlarmWarningBanner(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    Surface(
        onClick = onOpenSettings,
        shape = RoundedCornerShape(18.dp),
        color = qc.dangerContainer,
        border = BorderStroke(1.dp, qc.danger.copy(alpha = 0.45f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = qc.danger,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.exact_alarm_warning_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = qc.danger
                )
                Text(
                    text = stringResource(R.string.exact_alarm_warning_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = qc.text
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ChipChoiceRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    chips: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MoreSettingIconBadge(icon = icon)
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) { chips() }
    }
}
