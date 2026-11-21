package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.domain.wird.KhatmahDuaCatalog
import com.example.ui.components.OrnamentalDivider
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.LocalQuranFontFamily
import com.example.ui.theme.quranColors
import com.example.ui.util.ShareCardGenerator
import com.example.ui.util.copyTextToClipboard
import com.example.ui.util.sharePlainText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KhatmahDuaBottomSheet(
    onCopiedMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val quranFont = LocalQuranFontFamily.current
    val activeTheme = LocalAppTheme.current
    val useWestern = LocalUseWesternDigits.current
    val qc = MaterialTheme.quranColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var fontSizeSp by remember { mutableFloatStateOf(20f) }
    var activeStanza by remember { mutableIntStateOf(0) }
    val totalCount = KhatmahDuaCatalog.paragraphs.size

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = qc.bg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp)
                .testTag("khatmah_dua_sheet"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(qc.primary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, tint = qc.primary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "دعاء ختم القرآن الكريم",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = qc.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${totalCount.toAppDigits(useWestern)} فقرة • المس أي فقرة لتمييز موضعك",
                            style = MaterialTheme.typography.bodySmall,
                            color = qc.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp).testTag("khatmah_dua_close")) {
                    Icon(Icons.Rounded.Close, contentDescription = "إغلاق الدعاء", tint = qc.text)
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = qc.surface,
                border = BorderStroke(1.dp, qc.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalIconButton(
                            onClick = { fontSizeSp = (fontSizeSp - 2f).coerceAtLeast(16f) },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = qc.surface2, contentColor = qc.primary),
                            modifier = Modifier.size(48.dp).testTag("khatmah_dua_font_minus")
                        ) { Icon(Icons.Rounded.Remove, contentDescription = "تصغير الخط") }
                        Text(
                            text = "الخط ${fontSizeSp.toInt().toAppDigits(useWestern)}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = qc.text
                        )
                        FilledTonalIconButton(
                            onClick = { fontSizeSp = (fontSizeSp + 2f).coerceAtMost(30f) },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = qc.surface2, contentColor = qc.primary),
                            modifier = Modifier.size(48.dp).testTag("khatmah_dua_font_plus")
                        ) { Icon(Icons.Rounded.Add, contentDescription = "تكبير الخط") }
                    }

                    Surface(shape = RoundedCornerShape(50), color = qc.primary.copy(alpha = 0.12f)) {
                        Text(
                            text = "${(activeStanza + 1).toAppDigits(useWestern)} / ${totalCount.toAppDigits(useWestern)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = qc.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false).fillMaxWidth().heightIn(max = 430.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(key = "dua_bismillah") {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = quranFont, fontWeight = FontWeight.Bold),
                            color = qc.primary,
                            textAlign = TextAlign.Center
                        )
                        OrnamentalDivider()
                    }
                }

                itemsIndexed(KhatmahDuaCatalog.paragraphs, key = { idx, _ -> idx }) { idx, para ->
                    val isSelected = activeStanza == idx
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) qc.selected.copy(alpha = 0.14f) else qc.surface,
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) qc.primary else qc.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeStanza = idx }
                            .testTag("khatmah_dua_stanza_$idx")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(shape = CircleShape, color = if (isSelected) qc.primary else qc.surface2) {
                                    Text(
                                        text = (idx + 1).toAppDigits(useWestern),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) qc.onPrimary else qc.primary,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        copyTextToClipboard(context, "فقرة من دعاء الختم", para)
                                        onCopiedMessage("تم نسخ الفقرة ${(idx + 1).toAppDigits(useWestern)}")
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = "نسخ الفقرة ${(idx + 1).toAppDigits(useWestern)}",
                                        tint = qc.textMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = para,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = quranFont,
                                    fontSize = fontSizeSp.sp,
                                    lineHeight = (fontSizeSp * 1.8f).sp
                                ),
                                color = qc.text,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }

                item(key = "dua_ameen_footer") {
                    Text(
                        text = "اللَّهُمَّ تَقَبَّلْ مِنَّا وَارْزُقْنَا الْقَبُولَ وَالإِخْلَاصَ • آمِينَ",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = qc.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        copyTextToClipboard(context, "دعاء ختم القرآن", KhatmahDuaCatalog.fullText)
                        onCopiedMessage("تم نسخ دعاء ختم القرآن")
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("khatmah_dua_copy")
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.size(5.dp))
                    Text("نسخ", fontWeight = FontWeight.Bold, maxLines = 1)
                }

                FilledTonalButton(
                    onClick = { sharePlainText(context, "مشاركة دعاء ختم القرآن", KhatmahDuaCatalog.fullText) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("khatmah_dua_share")
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.size(5.dp))
                    Text("مشاركة", fontWeight = FontWeight.Bold, maxLines = 1)
                }

                Button(
                    onClick = {
                        val selectedPara = KhatmahDuaCatalog.paragraphs.getOrElse(activeStanza) { KhatmahDuaCatalog.paragraphs.first() }
                        ShareCardGenerator.shareStyledCard(
                            context = context,
                            headerLabel = "دعاء ختم القرآن الكريم",
                            bodyText = selectedPara,
                            footerLabel = "تطبيق القرآن الكريم",
                            appTheme = activeTheme
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("khatmah_dua_share_card")
                ) {
                    Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.size(5.dp))
                    Text("بطاقة", fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}
