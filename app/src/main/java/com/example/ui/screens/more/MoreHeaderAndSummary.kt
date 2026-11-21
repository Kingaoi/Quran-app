package com.example.ui.screens.more

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.data.local.PrayerSettings
import com.example.data.local.ReaderSettings
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.OfflineInventoryUiState
import com.example.domain.model.QuranReciters
import com.example.domain.model.localizeDigits
import com.example.domain.model.toAppDigits
import com.example.domain.wird.KhatmahPlanConfig
import com.example.ui.theme.LocalAppTheme
import kotlin.math.roundToInt

@Composable
internal fun MoreTopBar(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "المزيد والإعدادات",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag("more_screen_title")
        )
        Text(
            text = "التحكم الكامل بالمظهر والمصحف ومواقيت الصلاة والورد اليومي",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun MoreSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("ابحث في الإعدادات (الأرقام، التفسير، الأذان...)") },
        leadingIcon = {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                androidx.compose.material3.IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "مسح")
                }
            }
        } else null,
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("more_settings_search_input")
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MoreQuickSummaryCard(
    readerSettings: ReaderSettings,
    prayerSettings: PrayerSettings,
    wirdConfig: KhatmahPlanConfig,
    offlineHubState: OfflineInventoryUiState = OfflineInventoryUiState(),
    onOpenDownloadHub: () -> Unit = {}
) {
    val activeTheme = LocalAppTheme.current
    val useWestern = LocalUseWesternDigits.current
    val isNight = activeTheme == com.example.data.local.AppTheme.DARK
    val markerColor = if (isNight) {
        readerSettings.ayahMarkerColor.nightPreviewColor
    } else {
        readerSettings.ayahMarkerColor.lightPreviewColor
    }
    val reciter = remember(readerSettings.selectedReciterId) {
        QuranReciters.byId(readerSettings.selectedReciterId)
    }

    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
    val surfaceLow = MaterialTheme.colorScheme.surfaceContainerLow
    val cardBrush = remember(primaryContainer, secondaryContainer, surfaceLow) {
        Brush.linearGradient(
            colors = listOf(
                primaryContainer.copy(alpha = 0.55f),
                secondaryContainer.copy(alpha = 0.38f),
                surfaceLow
            )
        )
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = surfaceLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("more_quick_summary_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "ملخص التفضيلات الحالية",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryBadgeChip(
                    icon = Icons.Rounded.Palette,
                    label = readerSettings.appTheme.labelAr,
                    dotColor = markerColor
                )
                SummaryBadgeChip(
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    label = readerSettings.riwayah.titleAr
                )
                SummaryBadgeChip(
                    icon = Icons.Rounded.RecordVoiceOver,
                    label = reciter.nameAr
                )
                SummaryBadgeChip(
                    icon = Icons.Rounded.LocationOn,
                    label = prayerSettings.city.nameAr
                )
                SummaryBadgeChip(
                    icon = Icons.Rounded.NotificationsActive,
                    label = if (wirdConfig.reminderEnabled) {
                        "الورد ${formatArabicReminderTime(wirdConfig.reminderHour, wirdConfig.reminderMinute, useWestern)}"
                    } else {
                        "تذكير الورد متوقف"
                    }
                )
            }
            Surface(
                onClick = onOpenDownloadHub,
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth().testTag("more_summary_download_hub_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "التنزيل الشامل والمحتوى المحفوظ",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (offlineHubState.isBatchDownloading) {
                                val pct = (offlineHubState.overallBatchProgress * 100f).roundToInt().coerceIn(0, 100)
                                "جاري التنزيل المتزامن (${offlineHubState.activeKeys.size.toAppDigits(useWestern)} عناصر • ${pct.toAppDigits(useWestern)}٪)"
                            } else {
                                offlineHubState.compactSummaryAr(useWestern)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "إدارة",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryBadgeChip(
    icon: ImageVector,
    label: String,
    dotColor: Color? = null
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
internal fun MoreAboutAppCard(onOpenSources: (() -> Unit)? = null) {
    val useWestern = LocalUseWesternDigits.current
    val displayVersion = BuildConfig.VERSION_NAME.localizeDigits(useWestern)

    Surface(
        onClick = { onOpenSources?.invoke() },
        enabled = onOpenSources != null,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().testTag("more_about_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MoreSettingIconBadge(
                icon = Icons.Rounded.Info,
                tint = MaterialTheme.colorScheme.secondary,
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "القرآن الكريم • الإصدار $displayVersion",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "مصحف المدينة بالروايات • التفسير • التلاوة • الأذكار • المواقيت",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onOpenSources != null) {
                Text(
                    text = "المصادر",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
