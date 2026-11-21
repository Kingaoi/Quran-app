package com.example.ui.screens.reader.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.AudioPlaybackState
import com.example.domain.model.AyahItem
import com.example.domain.model.toAppDigits
import com.example.domain.quran.AyahWordDiffSegment
import com.example.domain.quran.MutashabihMatch
import com.example.ui.components.OrnamentalDivider
import com.example.ui.theme.LocalQuranFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AyahMutashabihatSheet(
    ayah: AyahItem,
    accentColor: Color,
    playbackState: AudioPlaybackState? = null,
    onPlayAyah: ((Int, Int) -> Unit)? = null,
    onTogglePlayPause: (() -> Unit)? = null,
    findMutashabihat: (AyahItem) -> List<MutashabihMatch>,
    compareWords: (AyahItem, AyahItem) -> List<AyahWordDiffSegment>,
    onJumpToAyah: (page: Int, surah: Int, ayahNum: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val matches = remember(ayah.surahNumber, ayah.ayahNumber) {
        findMutashabihat(ayah)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .testTag("dedicated_mutashabihat_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AyahCardSheetHeader(
                title = "المتشابهات القرآنية",
                subtitle = if (matches.isEmpty()) {
                    "موضع فريد في القرآن الكريم"
                } else {
                    "وُجد ${matches.size.toAppDigits()} موضعاً متشابهاً في المصحف"
                },
                icon = Icons.Rounded.CompareArrows,
                accentColor = accentColor,
                ayah = ayah,
                playbackState = playbackState,
                onPlayAyah = onPlayAyah,
                onTogglePlayPause = onTogglePlayPause,
                onDismiss = onDismiss
            )

            OrnamentalDivider()

            if (matches.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "لم يُسجّل تشابه لفظي مطابق لهذه الآية",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "تتميّز هذه الآية بصياغتها المنفردة دون آيات مقاربة لها في السور الأخرى.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "الكلمات الملوّنة تُظهر مواضع الاختلاف الدقيقة بين الآيتين لتثبيت الحفظ.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                matches.forEach { match ->
                    val diffSegments = remember(ayah, match.target) {
                        compareWords(ayah, match.target)
                    }
                    Surface(
                        onClick = {
                            onDismiss()
                            onJumpToAyah(match.target.pageNumber, match.target.surahNumber, match.target.ayahNumber)
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "سورة ${match.target.surahNameAr} • الآية ${match.target.ayahNumber.toAppDigits()}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${match.matchBadge} • ${match.similarityPercent.toAppDigits()}٪",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                        contentDescription = "الانتقال للموضع",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            val primaryColor = MaterialTheme.colorScheme.onSurface
                            val diffColor = MaterialTheme.colorScheme.error
                            val diffBg = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            Text(
                                text = buildAnnotatedString {
                                    diffSegments.forEachIndexed { idx, seg ->
                                        if (idx > 0) append(' ')
                                        if (seg.isShared) {
                                            withStyle(SpanStyle(color = primaryColor)) { append(seg.word) }
                                        } else {
                                            withStyle(
                                                SpanStyle(
                                                    color = diffColor,
                                                    background = diffBg,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            ) { append(seg.word) }
                                        }
                                    }
                                    append(' ')
                                    withStyle(SpanStyle(color = accentColor)) { append(match.target.marker) }
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = LocalQuranFontFamily.current,
                                    fontSize = 19.sp,
                                    lineHeight = 34.sp
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "صفحة ${match.target.pageNumber.toAppDigits()} • الجزء ${match.target.juzNumber.toAppDigits()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "اضغط للانتقال",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
