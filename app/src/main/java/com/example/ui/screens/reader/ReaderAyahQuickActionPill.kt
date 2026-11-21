package com.example.ui.screens.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranColors

internal enum class AyahDetailCardType {
    TAFSIR,
    ASBAB_GHARIB,
    MUTASHABIHAT,
    WAQF,
    WAQF_PAGE_GUIDE,
    BOOKMARK,
    SHARE
}

@Composable
internal fun AyahQuickPlayButton(
    isThisAyahActive: Boolean,
    isPlaying: Boolean,
    isBuffering: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val container = when {
        isPlaying -> qc.selected
        isThisAyahActive -> qc.surface2
        else -> Color.Transparent
    }
    val tint = if (isPlaying) qc.onSelected else qc.primary
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(containerColor = container, contentColor = tint),
        modifier = modifier.size(48.dp)
    ) {
        when {
            isBuffering -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = tint)
            isPlaying -> Icon(Icons.Rounded.Pause, contentDescription = "إيقاف التلاوة مؤقتاً")
            isThisAyahActive -> Icon(Icons.Rounded.PlayArrow, contentDescription = "استكمال التلاوة")
            else -> Icon(Icons.Rounded.PlayArrow, contentDescription = "استماع للآية")
        }
    }
}

@Composable
internal fun QuickActionPill(
    icon: ImageVector,
    label: String,
    badgeCount: Int = 0,
    highlighted: Boolean = false,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val bgColor = if (highlighted) qc.selected else qc.surface
    val contentTint = if (highlighted) qc.onSelected else qc.text

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (highlighted) qc.selected else qc.border
        ),
        modifier = modifier
            .heightIn(min = 50.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(imageVector = icon, contentDescription = label, tint = contentTint, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp),
                    color = contentTint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (badgeCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = qc.accent,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = badgeCount.toAppDigits(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = qc.onPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.5.dp)
                    )
                }
            }
        }
    }
}
