package com.example.ui.screens.more

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.adhkar.AdhkarReminderConfig
import com.example.data.local.PrayerSettings
import com.example.data.local.ReaderSettings
import com.example.domain.model.AppSourcesCatalog
import com.example.domain.model.QuranReciters
import com.example.domain.model.TafsirOfflineState
import com.example.domain.model.toAppDigits
import com.example.domain.wird.KhatmahPlanConfig
import com.example.ui.navigation.QuranSharedKey
import com.example.ui.navigation.quranSharedBounds
import com.example.ui.navigation.quranSharedElement
import com.example.ui.theme.quranColors

internal enum class MoreCategory(
    val titleAr: String,
    val shortTitleAr: String,
    val icon: ImageVector,
    val subGroupCount: Int,
    val accentIdx: Int,
    val keywords: String
) {
    APPEARANCE(
        titleAr = "المظهر والألوان",
        shortTitleAr = "المظهر",
        icon = Icons.Rounded.Palette,
        subGroupCount = 2,
        accentIdx = 0,
        keywords = "مظهر ثيم ليلي أرقام غربية عربية ألوان آيات إطار السور تظليل تحديد شفافية"
    ),
    MUSHAF_READER(
        titleAr = "المصحف والقراءة",
        shortTitleAr = "المصحف",
        icon = Icons.AutoMirrored.Rounded.MenuBook,
        subGroupCount = 3,
        accentIdx = 1,
        keywords = "مصحف رواية حفص ورش خط حجم شاشة إضاءة لوحي صفحتين خافت سطوع تنزيل شامل أوفلاين"
    ),
    AUDIO_TAFSIR(
        titleAr = "التلاوة والتفسير",
        shortTitleAr = "التلاوة والتفسير",
        icon = Icons.Rounded.RecordVoiceOver,
        subGroupCount = 2,
        accentIdx = 2,
        keywords = "قارئ تلاوة صوت إشعار خلفية أزرار إيقاف مؤقت تفسير ميسر سعدي تنزيل"
    ),
    ADHKAR_WIRD(
        titleAr = "الأذكار والورد",
        shortTitleAr = "الأذكار والورد",
        icon = Icons.Rounded.BookmarkAdded,
        subGroupCount = 2,
        accentIdx = 3,
        keywords = "أذكار انتقال تلقائي اهتزاز تذكير صباح مساء ورد ختمة قراءة صافية شريط فهرس"
    ),
    PRAYER(
        titleAr = "أوقات الصلاة والأذان",
        shortTitleAr = "الصلاة",
        icon = Icons.Rounded.Mosque,
        subGroupCount = 3,
        accentIdx = 4,
        keywords = "صلاة مدينة موقع حساب فلكي هجري عصر ساعة أذان مؤذن صوت إشعار تنبيه دقيق فجر ظهر مغرب عشاء سنن كهف"
    ),
    BACKUP_ABOUT(
        titleAr = "النسخ الاحتياطي والتطبيق",
        shortTitleAr = "النسخ الاحتياطي",
        icon = Icons.Rounded.Backup,
        subGroupCount = 2,
        accentIdx = 5,
        keywords = "نسخ احتياطي تصدير استعادة ملف json عن التطبيق إصدار"
    ),
    SOURCES(
        titleAr = "المصادر والمراجع",
        shortTitleAr = "المصادر",
        icon = Icons.Rounded.LibraryBooks,
        subGroupCount = AppSourcesCatalog.totalGroupsCount,
        accentIdx = 1,
        keywords = "مصادر مراجع مصحف تفسير تلاوة أذان أذكار حصن المسلم خطوط فلك quran.ws everyayah mp3quran hisnmuslim islamcan tafsir_api alquran.cloud wikimedia"
    );

    fun liveSummaryAr(
        reader: ReaderSettings,
        prayer: PrayerSettings,
        wird: KhatmahPlanConfig,
        adhkarReminder: AdhkarReminderConfig,
        tafsirState: TafsirOfflineState,
        useWestern: Boolean
    ): String = when (this) {
        APPEARANCE -> "${reader.appTheme.labelAr} • ${reader.ayahMarkerColor.labelAr}"
        MUSHAF_READER -> "${reader.riwayah.titleAr} • الخط ${reader.fontSizeSp.toInt().toAppDigits(useWestern)}"
        AUDIO_TAFSIR -> "${QuranReciters.byId(reader.selectedReciterId).nameAr} • ${tafsirState.edition.titleAr}"
        ADHKAR_WIRD -> {
            val adhkarStatus = if (adhkarReminder.morningEnabled || adhkarReminder.eveningEnabled) "تذكير الأذكار مفعّل" else "الأذكار بدون تذكير"
            val wirdStatus = if (wird.reminderEnabled) "تذكير الورد مفعّل" else "إعدادات الورد"
            "$adhkarStatus • $wirdStatus"
        }
        PRAYER -> "${prayer.city.nameAr} • ${prayer.adhanSoundMode.labelAr}"
        BACKUP_ABOUT -> "حفظ واستعادة الإعدادات والسجلات"
        SOURCES -> "${AppSourcesCatalog.totalSourcesCount.toAppDigits(useWestern)} مصدراً ومرجعاً علمياً وتقنياً"
    }
}

internal fun androidx.compose.foundation.lazy.LazyListScope.moreCategoryHubItems(
    reader: ReaderSettings,
    prayer: PrayerSettings,
    wird: KhatmahPlanConfig,
    adhkarReminder: AdhkarReminderConfig,
    tafsirState: TafsirOfflineState,
    useWestern: Boolean,
    onSelectCategory: (MoreCategory) -> Unit
) {
    item(key = "hub_header") {
        MoreSubGroupHeader(title = "أقسام الإعدادات (${MoreCategory.entries.size.toAppDigits(useWestern)} أقسام)")
    }
    items(MoreCategory.entries, key = { "hub_${it.name}" }) { cat ->
        val qc = MaterialTheme.quranColors
        val accent = qc.categoryPastelAt(cat.accentIdx)
        Surface(
            onClick = { onSelectCategory(cat) },
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .quranSharedBounds(QuranSharedKey.MoreCategoryCard(cat.name))
                .fillMaxWidth()
                .heightIn(min = 68.dp)
                .testTag("more_category_card_${cat.name.lowercase()}")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MoreSettingIconBadge(
                    icon = cat.icon,
                    tint = accent,
                    containerColor = accent.copy(alpha = 0.14f),
                    modifier = Modifier.quranSharedElement(
                        QuranSharedKey.MoreCategoryIcon(cat.name),
                        zIndexInOverlay = 1f
                    )
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = cat.titleAr,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.quranSharedBounds(
                            QuranSharedKey.MoreCategoryTitle(cat.name),
                            zIndexInOverlay = 1f
                        )
                    )
                    Text(
                        text = cat.liveSummaryAr(reader, prayer, wird, adhkarReminder, tafsirState, useWestern),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(shape = RoundedCornerShape(50), color = qc.surface2) {
                    Text(
                        text = "${cat.subGroupCount.toAppDigits(useWestern)} مجموعات",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = qc.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun MoreCategoryDetailHeader(
    activeCategory: MoreCategory,
    onBack: () -> Unit,
    onSelectCategory: (MoreCategory) -> Unit
) {
    val qc = MaterialTheme.quranColors
    val accent = qc.categoryPastelAt(activeCategory.accentIdx)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .quranSharedBounds(QuranSharedKey.MoreCategoryCard(activeCategory.name))
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalIconButton(
                onClick = onBack,
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = qc.surface2, contentColor = qc.primary),
                modifier = Modifier.size(48.dp).testTag("more_category_back_button")
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "العودة للأقسام")
            }
            MoreSettingIconBadge(
                icon = activeCategory.icon,
                tint = accent,
                containerColor = accent.copy(alpha = 0.14f),
                modifier = Modifier.quranSharedElement(
                    QuranSharedKey.MoreCategoryIcon(activeCategory.name),
                    zIndexInOverlay = 1f
                )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activeCategory.titleAr,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.quranSharedBounds(
                        QuranSharedKey.MoreCategoryTitle(activeCategory.name),
                        zIndexInOverlay = 1f
                    )
                )
                Text(
                    text = "اختر قسماً آخر للتنقل السريع أو اضبط الإعدادات أدناه",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MoreCategory.entries, key = { it.name }) { cat ->
                val selected = cat == activeCategory
                FilterChip(
                    selected = selected,
                    onClick = { onSelectCategory(cat) },
                    leadingIcon = {
                        Icon(cat.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    label = { Text(cat.shortTitleAr, style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = qc.selected,
                        selectedLabelColor = qc.onSelected,
                        selectedLeadingIconColor = qc.onSelected
                    ),
                    modifier = Modifier.testTag("more_category_pill_${cat.name.lowercase()}")
                )
            }
        }
    }
}
