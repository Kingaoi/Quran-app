package com.example.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ChromeReaderMode
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.BrightnessLow
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.Highlight
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.ScreenLockPortrait
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AppTheme
import com.example.data.local.ReaderSettings
import com.example.data.local.RiwayahEdition
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.OfflineInventoryUiState
import com.example.domain.model.toAppDigits
import com.example.ui.components.AyahHighlightSettingsSection
import com.example.ui.components.AyahMarkerColorSection
import com.example.ui.components.FontSettingsSection
import com.example.ui.components.OfflineDownloadSection
import com.example.ui.components.UnifiedThemeSection
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors
import com.example.ui.viewmodel.OfflineDownloadState
import kotlin.math.roundToInt

@Composable
internal fun MoreAppearanceSection(
    settings: ReaderSettings,
    actions: MoreSettingsActions
) {
    var expandOrnamentColors by rememberSaveable { mutableStateOf(false) }
    var expandHighlightStyle by rememberSaveable { mutableStateOf(false) }
    val activeTheme = LocalAppTheme.current
    val useWestern = LocalUseWesternDigits.current
    val isNight = activeTheme == AppTheme.DARK
    val markerPreviewColor = if (isNight) {
        settings.ayahMarkerColor.nightPreviewColor
    } else {
        settings.ayahMarkerColor.lightPreviewColor
    }
    val qc = MaterialTheme.quranColors

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MoreCategoryHeader(title = "المظهر والألوان", icon = Icons.Rounded.Palette)

        MoreSubGroupHeader(title = "ثيم التطبيق والأرقام")
        UnifiedThemeSection(selectedTheme = settings.appTheme, onSelectTheme = actions.onSelectAppTheme)
        MoreSettingsGroupCard {
            MoreSettingSwitchRow(
                title = "الأرقام الغربية (123 بدل ١٢٣)",
                subtitle = if (settings.useWesternDigits) "مفعّلة: 1 2 3 4 5" else "الأرقام العربية الشرقية: ١ ٢ ٣ ٤ ٥",
                icon = Icons.Rounded.Dialpad,
                checked = settings.useWesternDigits,
                iconTint = qc.categoryPastelAt(2),
                onCheckedChange = actions.onSetUseWesternDigits,
                testTagKey = "more_western_digits_switch"
            )
        }

        MoreSubGroupHeader(title = "زخرفة المصحف وتظليل الآيات")
        MoreSettingsGroupCard {
            val bannerStateText = if (settings.surahBannerEnabled) "إطار السور مفعّل" else "بدون إطار السور"
            MoreExpandableSectionRow(
                title = "ألوان الآيات وإطار السور",
                subtitle = "${settings.ayahMarkerColor.labelAr} • ${settings.ayahMarkerColorTarget.labelAr} • $bannerStateText",
                icon = Icons.Rounded.Brush,
                iconTint = qc.categoryPastelAt(0),
                expanded = expandOrnamentColors,
                onToggleExpand = { expandOrnamentColors = !expandOrnamentColors },
                testTagKey = "more_expand_ornaments",
                badgeContent = {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(markerPreviewColor)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    )
                }
            ) {
                AyahMarkerColorSection(
                    selectedColor = settings.ayahMarkerColor,
                    selectedTarget = settings.ayahMarkerColorTarget,
                    surahBannerEnabled = settings.surahBannerEnabled,
                    onSelectColor = actions.onSelectAyahMarkerColor,
                    onSelectTarget = actions.onSelectAyahMarkerColorTarget,
                    onToggleSurahBanner = actions.onToggleSurahBanner
                )
            }

            MoreGroupDivider()

            val hlColor = settings.resolveAyahHighlightColor(isNight, markerPreviewColor)
            val hlPct = (settings.ayahHighlightAlpha * 100f).roundToInt().toAppDigits(useWestern)
            MoreExpandableSectionRow(
                title = "مظهر تظليل الآية",
                subtitle = "${settings.ayahHighlightColor.labelAr} • ${settings.ayahHighlightStyle.labelAr} • $hlPct٪",
                icon = Icons.Rounded.Highlight,
                iconTint = qc.categoryPastelAt(3),
                expanded = expandHighlightStyle,
                onToggleExpand = { expandHighlightStyle = !expandHighlightStyle },
                testTagKey = "more_expand_highlight",
                badgeContent = {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(hlColor)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    )
                }
            ) {
                AyahHighlightSettingsSection(
                    settings = settings,
                    onSelectColor = actions.onSelectAyahHighlightColor,
                    onSelectStyle = actions.onSelectAyahHighlightStyle,
                    onAlphaChange = actions.onSetAyahHighlightAlpha
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MoreMushafReaderSection(
    settings: ReaderSettings,
    adhkarFontSizeSp: Float,
    offlineState: OfflineDownloadState,
    offlineHubState: OfflineInventoryUiState,
    actions: MoreSettingsActions,
    onOpenComprehensiveHub: () -> Unit
) {
    val useWestern = LocalUseWesternDigits.current
    var expandRiwayah by rememberSaveable { mutableStateOf(false) }
    var expandFonts by rememberSaveable { mutableStateOf(false) }
    val qc = MaterialTheme.quranColors

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MoreCategoryHeader(title = "المصحف والخطوط والقراءة", icon = Icons.AutoMirrored.Rounded.MenuBook)

        MoreSubGroupHeader(title = "الرواية القرآنية والخطوط")
        MoreSettingsGroupCard {
            MoreExpandableSectionRow(
                title = "رواية المصحف",
                subtitle = settings.riwayah.titleAr,
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                iconTint = qc.categoryPastelAt(1),
                expanded = expandRiwayah,
                onToggleExpand = { expandRiwayah = !expandRiwayah },
                testTagKey = "more_expand_riwayah"
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RiwayahEdition.entries.forEach { edition ->
                        val sel = settings.riwayah == edition
                        FilterChip(
                            selected = sel,
                            onClick = { actions.onSelectRiwayah(edition) },
                            colors = quranFilterChipColors(),
                            border = quranFilterChipBorder(sel),
                            label = { Text(edition.titleAr) },
                            modifier = Modifier.testTag("riwayah_${edition.slug}")
                        )
                    }
                }
            }

            MoreGroupDivider()

            MoreExpandableSectionRow(
                title = "الخطوط وحجم النص",
                subtitle = "${settings.quranFont.labelAr} • المصحف ${settings.fontSizeSp.toInt().toAppDigits(useWestern)} • الأذكار ${adhkarFontSizeSp.toInt().toAppDigits(useWestern)}",
                icon = Icons.Rounded.TextFields,
                expanded = expandFonts,
                onToggleExpand = { expandFonts = !expandFonts },
                testTagKey = "more_expand_fonts"
            ) {
                FontSettingsSection(
                    settings = settings,
                    onSelectQuranFont = actions.onSelectQuranFont,
                    onFontSizeChange = actions.onQuranFontSizeChange,
                    onResetFontSettings = actions.onResetQuranFontSettings
                )
                AdhkarFontSizeCard(
                    fontSizeSp = adhkarFontSizeSp,
                    onFontSizeChange = actions.onAdhkarFontSizeChange,
                    onReset = actions.onResetAdhkarFontSize
                )
            }
        }

        MoreSubGroupHeader(title = "شاشة القراءة والإضاءة")
        MoreSettingsGroupCard {
            MoreSettingSwitchRow(
                title = "إبقاء الشاشة مضاءة أثناء القراءة",
                subtitle = "منع انطفاء الشاشة تلقائياً داخل المصحف",
                icon = Icons.Rounded.ScreenLockPortrait,
                checked = settings.keepScreenOn,
                onCheckedChange = actions.onSetKeepScreenOn,
                testTagKey = "more_keep_screen_on_switch"
            )
            MoreGroupDivider()
            MoreSettingSwitchRow(
                title = "عرض صفحتين على الأجهزة اللوحية",
                subtitle = "فتح صفحتين متقابلتين تلقائياً في الشاشات العريضة",
                icon = Icons.AutoMirrored.Rounded.ChromeReaderMode,
                checked = settings.twoPageSpreadOnTablet,
                onCheckedChange = actions.onSetTwoPageSpreadOnTablet,
                testTagKey = "more_tablet_spread_switch"
            )
            MoreGroupDivider()
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MoreSettingIconBadge(icon = Icons.Rounded.BrightnessLow, tint = qc.categoryPastelAt(3))
                        Text(
                            text = "خافت إضاءة القراءة",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "${(settings.readerDimmerAlpha * 100f).roundToInt().toAppDigits(useWestern)}٪",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = settings.readerDimmerAlpha,
                    onValueChange = actions.onSetReaderDimmerAlpha,
                    valueRange = 0f..0.75f,
                    modifier = Modifier.testTag("more_reader_dimmer_slider")
                )
            }
        }

        MoreSubGroupHeader(title = "التنزيل والعمل بدون إنترنت")
        MoreCategoryHeader(title = "مركز التنزيل", icon = Icons.Rounded.CloudDownload)
        OfflineDownloadSection(
            offlineState = offlineState,
            onStartDownload = actions.onStartFullDownload,
            onCancelDownload = actions.onCancelFullDownload,
            onDeleteDownload = actions.onDeleteFullDownload,
            offlineHubState = offlineHubState,
            onOpenComprehensiveHub = onOpenComprehensiveHub
        )
    }
}
