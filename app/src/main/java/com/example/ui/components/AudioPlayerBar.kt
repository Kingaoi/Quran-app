package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.audio.AudioPlaybackState
import com.example.domain.model.toArabicIndicDigits

@Composable
fun MiniAudioPlayerBar(
    playbackState: AudioPlaybackState,
    onTogglePlayPause: () -> Unit,
    onNextAyah: () -> Unit,
    onPreviousAyah: () -> Unit,
    onToggleRepeat: () -> Unit,
    onOpenReciterPicker: () -> Unit,
    onOpenPlaylists: (() -> Unit)? = null,
    onJumpToMushaf: ((surah: Int, ayah: Int) -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = playbackState.isActive,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReciterPortraitAvatar(
                        reciter = playbackState.reciter,
                        isSelected = true,
                        size = 40.dp,
                        modifier = Modifier.clickable(onClick = onOpenReciterPicker)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    val playlistSession = playbackState.playlistSession
                    val sleepMin = playbackState.sleepTimerRemainingSec?.let { ((it + 59) / 60).coerceAtLeast(1) }
                    val statusSuffix = buildString {
                        if (playbackState.memorizationLoop.enabled) append(" • 🔁 تحفيظ")
                        if (sleepMin != null) append(" • 🌙 ${sleepMin.toArabicIndicDigits()}د")
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = {
                                if (onJumpToMushaf != null) onJumpToMushaf(playbackState.currentSurah, playbackState.currentAyah)
                                else if (playlistSession != null && onOpenPlaylists != null) onOpenPlaylists()
                                else onOpenReciterPicker()
                            })
                            .testTag("mini_player_jump_to_mushaf")
                    ) {
                        Text(
                            text = "سورة ${playbackState.surahNameAr} • آية ${playbackState.currentAyah.toArabicIndicDigits()}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val subtitle = when {
                            playbackState.errorMessage != null -> playbackState.errorMessage
                            playlistSession != null -> "${playlistSession.playlistTitleAr} (${(playlistSession.currentTrackIndex + 1).toArabicIndicDigits()}/${playlistSession.totalTracks.toArabicIndicDigits()}) • ${playbackState.reciter.nameAr}$statusSuffix"
                            else -> "${playbackState.reciter.nameAr}$statusSuffix"
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                playbackState.errorMessage != null -> MaterialTheme.colorScheme.error
                                playlistSession != null -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = if (playlistSession != null && onOpenPlaylists != null) {
                                Modifier.clickable(onClick = onOpenPlaylists)
                            } else Modifier
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = onToggleRepeat,
                            modifier = Modifier.testTag("player_repeat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.RepeatOne,
                                contentDescription = "تكرار الآية",
                                tint = if (playbackState.repeatAyah) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        IconButton(
                            onClick = onPreviousAyah,
                            modifier = Modifier.testTag("player_prev_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                                contentDescription = "الآية السابقة"
                            )
                        }

                        Surface(
                            onClick = onTogglePlayPause,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("player_play_pause_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (playbackState.isBuffering) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (playbackState.isPlaying) {
                                            Icons.Rounded.Pause
                                        } else {
                                            Icons.Rounded.PlayArrow
                                        },
                                        contentDescription = if (playbackState.isPlaying) "إيقاف مؤقت" else "تشغيل",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onNextAyah,
                            modifier = Modifier.testTag("player_next_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                                contentDescription = "الآية التالية"
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("player_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "إغلاق المشغل",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
