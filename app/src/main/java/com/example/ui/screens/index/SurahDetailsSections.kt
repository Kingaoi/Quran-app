package com.example.ui.screens.index

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.Topic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.toAppDigits
import com.example.domain.quran.AsbabEntry
import com.example.domain.quran.SajdahPositionInfo
import com.example.domain.quran.SurahFullDetails
import com.example.ui.theme.LocalQuranFontFamily

@Composable
internal fun SurahSectionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SurahDetailsRichSections(
    details: SurahFullDetails,
    onOpenAyah: (page: Int, surah: Int, ayah: Int) -> Unit
) {
    val surah = details.surah
    val profile = details.profile

    if (surah.openingAyahText.isNotBlank()) {
        SurahSectionCard(
            title = "مطلع السورة",
            icon = Icons.AutoMirrored.Rounded.MenuBook
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "﴿${surah.openingAyahText}﴾",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = LocalQuranFontFamily.current,
                        fontSize = 20.sp,
                        lineHeight = 36.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
    }

    SurahSectionCard(
        title = "معنى اسم السورة وأسماؤها",
        icon = Icons.Rounded.Label
    ) {
        Text(
            text = profile.nameMeaning,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!profile.otherNames.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "أسماء أخرى مأثورة: ${profile.otherNames}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
        }
    }

    SurahSectionCard(
        title = "المقصد العام للسورة",
        icon = Icons.Rounded.Flag
    ) {
        Text(
            text = profile.mainObjective,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    if (profile.keyThemes.isNotEmpty()) {
        SurahSectionCard(
            title = "أبرز موضوعات السورة ومحاورها",
            icon = Icons.Rounded.Topic
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                profile.keyThemes.forEach { theme ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                    ) {
                        Text(
                            text = theme,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }

    SurahSectionCard(
        title = "ملابسات النزول وخصائص السورة",
        icon = Icons.Rounded.HistoryEdu
    ) {
        Text(
            text = profile.revelationNotes,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
        val extraNote = when (surah.number) {
            9 -> "ملاحظة: سورة التوبة هي السورة الوحيدة التي لا تُفتتح بالبسملة في المصحف."
            27 -> "ملاحظة: وردت البسملة في سورة النمل مرتين؛ في مطلعها وفي رسالة نبي الله سليمان عليه السلام (الآية ٣٠)."
            else -> null
        }
        if (extraNote != null) {
            Text(
                text = extraNote,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        details.surahContext?.let { ctx ->
            AsbabMiniCard(entry = ctx)
        }
    }

    if (details.sajdahPositions.isNotEmpty()) {
        SurahSajdahSection(
            sajdahPositions = details.sajdahPositions,
            onOpenAyah = onOpenAyah
        )
    }

    if (details.relatedAsbab.isNotEmpty()) {
        SurahSectionCard(
            title = "من أسباب النزول في السورة (${details.relatedAsbab.size.toAppDigits()})",
            icon = Icons.Rounded.HistoryEdu
        ) {
            details.relatedAsbab.forEach { entry ->
                AsbabMiniCard(entry = entry)
            }
        }
    }
}

@Composable
private fun AsbabMiniCard(entry: AsbabEntry) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = entry.narration,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 21.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "المصدر: ${entry.source}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun SurahSajdahSection(
    sajdahPositions: List<SajdahPositionInfo>,
    onOpenAyah: (page: Int, surah: Int, ayah: Int) -> Unit
) {
    SurahSectionCard(
        title = "مواضع سجدات التلاوة (${sajdahPositions.size.toAppDigits()})",
        icon = Icons.Rounded.Mosque
    ) {
        sajdahPositions.forEach { sajdah ->
            Surface(
                onClick = { onOpenAyah(sajdah.pageNumber, sajdah.surahNumber, sajdah.ayahNumber) },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("surah_detail_sajdah_${sajdah.ayahNumber}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "الآية ${sajdah.ayahNumber.toAppDigits()} • ص ${sajdah.pageNumber.toAppDigits()}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = sajdah.classificationAr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "الانتقال إلى آية السجدة",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
