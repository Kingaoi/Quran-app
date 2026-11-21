package com.example.ui.screens.index

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SearchHit
import com.example.domain.model.SurahInfo
import com.example.domain.model.toAppDigits
import com.example.domain.quran.ConcordanceHit
import com.example.ui.components.RubElHizbBadge
import com.example.ui.theme.LocalQuranFontFamily

@Composable
internal fun ConcordanceHitCard(
    hit: ConcordanceHit,
    showConceptTag: Boolean,
    onClick: () -> Unit
) {
    val highlightedText = buildHighlightedAyahText(
        ayahText = hit.ayahText,
        matchedTokens = hit.matchedTokens,
        highlightColor = MaterialTheme.colorScheme.primary,
        highlightBackground = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
    )
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("concordance_hit_${hit.surahNumber}_${hit.ayahNumber}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سورة ${hit.surahNameAr} • الآية ${hit.ayahNumber.toAppDigits()}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hit.matchCountInAyah > 1) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${hit.matchCountInAyah.toAppDigits()} مواضع",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${if (hit.isMakki) "مكية" else "مدنية"} • ص ${hit.pageNumber.toAppDigits()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            if (showConceptTag && !hit.matchedConceptTitle.isNullOrBlank()) {
                Text(
                    text = hit.matchedConceptTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = highlightedText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = LocalQuranFontFamily.current,
                    fontSize = 19.sp,
                    lineHeight = 33.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
internal fun SearchSurahCard(
    surah: SurahInfo,
    isActiveSurah: Boolean,
    isPlayingSurah: Boolean,
    onClick: () -> Unit,
    onTogglePlaySurah: () -> Unit,
    onShowInfo: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RubElHizbBadge(number = surah.number, size = 38.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "سورة ${surah.nameAr}",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${surah.revelationType} • ${surah.ayahCount.toAppDigits()} آية • ص ${surah.firstPage.toAppDigits()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SurahPlayActionButton(surah.nameAr, surah.number, isActiveSurah, isPlayingSurah, onClick = onTogglePlaySurah)
                SurahInfoActionButton(surah.nameAr, surah.number, onClick = onShowInfo)
            }
        }
    }
}

@Composable
internal fun SearchHitCard(hit: SearchHit, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("سورة ${hit.surahNameAr} • الآية ${hit.ayahNumber.toAppDigits()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text("ص ${hit.pageNumber.toAppDigits()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
            Text(
                text = hit.ayahText,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = LocalQuranFontFamily.current, fontSize = 19.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
