package com.example.ui.screens.reader.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.AudioPlaybackState
import com.example.domain.model.AyahItem
import com.example.domain.model.toAppDigits
import com.example.ui.components.OrnamentalDivider
import com.example.ui.theme.LocalQuranFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AyahShareSheet(
    ayah: AyahItem,
    accentColor: Color,
    playbackState: AudioPlaybackState? = null,
    onPlayAyah: ((Int, Int) -> Unit)? = null,
    onTogglePlayPause: (() -> Unit)? = null,
    onShareImageCard: () -> Unit,
    onSharePlainText: () -> Unit,
    onCopyText: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .testTag("dedicated_share_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AyahCardSheetHeader(
                title = "مشاركة الآية",
                subtitle = "سورة ${ayah.surahNameAr} • الآية ${ayah.ayahNumber.toAppDigits()}",
                icon = Icons.Rounded.Share,
                accentColor = accentColor,
                ayah = ayah,
                showAyahQuote = false,
                playbackState = playbackState,
                onPlayAyah = onPlayAyah,
                onTogglePlayPause = onTogglePlayPause,
                onDismiss = onDismiss
            )

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.35f)),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "سورة ${ayah.surahNameAr} • الآية ${ayah.ayahNumber.toAppDigits()}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "﴿${ayah.text}﴾",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = LocalQuranFontFamily.current,
                            fontSize = 22.sp,
                            lineHeight = 40.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OrnamentalDivider()
                    Text(
                        text = "المصحف الشريف",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = { onShareImageCard(); onDismiss() },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("ayah_share_image_card")
            ) {
                Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(19.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("مشاركة كبطاقة صورة", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = { onSharePlainText(); onDismiss() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("ayah_share_plain_text")
                ) {
                    Icon(Icons.Rounded.TextFields, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة كنص")
                }

                OutlinedButton(
                    onClick = { onCopyText(); onDismiss() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("نسخ")
                }
            }
        }
    }
}
