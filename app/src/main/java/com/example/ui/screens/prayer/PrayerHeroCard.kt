package com.example.ui.screens.prayer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.localizeDigits
import com.example.domain.model.toAppDigits
import com.example.domain.prayer.CityLocation
import com.example.domain.prayer.PrayerName
import com.example.domain.prayer.PrayerTimeEntry
import com.example.domain.prayer.PrayerTickerState
import com.example.domain.prayer.PrayerTimesCalculator
import com.example.domain.prayer.PresetCities
import com.example.ui.theme.quranColors
import kotlinx.coroutines.flow.StateFlow
import java.util.TimeZone
import kotlin.math.roundToInt

@Composable
internal fun PrayerHeroCard(
    city: CityLocation,
    hijriDateAr: String,
    gregorianDateAr: String,
    dayOffset: Int,
    nextPrayer: PrayerTimeEntry?,
    countdownText: String,
    iqamahStatusText: String? = null,
    intervalProgress: Float,
    qiblaBearingDegrees: Double,
    use24Hour: Boolean,
    methodTitleAr: String,
    onOpenCityPicker: () -> Unit,
    onPrevDay: () -> Unit,
    onToday: () -> Unit,
    onNextDay: () -> Unit,
    onToggleQibla: () -> Unit,
    tickerStateFlow: StateFlow<PrayerTickerState>? = null
) {
    val useWestern = LocalUseWesternDigits.current
    val qc = MaterialTheme.quranColors
    val cardShape = RoundedCornerShape(28.dp)
    val timeZone = remember(city.timeZoneId, city.nameAr, city.countryAr) {
        city.resolveTimeZone()
    }
    val deviceZoneId = remember { TimeZone.getDefault().id }

    val phaseTint = when (nextPrayer?.prayer) {
        PrayerName.FAJR, PrayerName.SUNRISE -> qc.pastelAmber
        PrayerName.DHUHR, PrayerName.ASR -> qc.primary
        PrayerName.MAGHRIB -> qc.pastelPeach
        PrayerName.ISHA, null -> qc.pastelLavender
    }
    val heroBrush = remember(qc.surface, qc.surface2, phaseTint) {
        Brush.linearGradient(
            colors = listOf(
                phaseTint.copy(alpha = 0.16f),
                qc.surface2.copy(alpha = 0.86f),
                qc.surface
            )
        )
    }

    Surface(
        shape = cardShape,
        color = qc.surface,
        border = BorderStroke(1.2.dp, qc.border),
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(heroBrush)
                .drawBehind { drawPrayerMihrabWatermark(qc.accent, qc.border) }
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = qc.bg.copy(alpha = 0.78f),
                    border = BorderStroke(1.dp, qc.border),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onOpenCityPicker)
                        .testTag("prayer_city_picker_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (city.isFromGps) Icons.Rounded.MyLocation else Icons.Rounded.LocationOn,
                            contentDescription = "تغيير المدينة",
                            tint = qc.primary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${city.nameAr}، ${city.countryAr}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = qc.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = qc.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = qc.bg.copy(alpha = 0.78f),
                    border = BorderStroke(1.dp, qc.border),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onToggleQibla)
                        .testTag("prayer_hero_qibla_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Explore,
                            contentDescription = "اتجاه القبلة",
                            tint = qc.accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "القبلة ${qiblaBearingDegrees.roundToInt().toAppDigits(useWestern)}°",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = qc.text
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrayerHeroLiveCountdownSection(
                fallbackNextPrayer = nextPrayer,
                fallbackCountdown = countdownText,
                fallbackIqamahStatus = iqamahStatusText,
                fallbackProgress = intervalProgress,
                timeZone = timeZone,
                use24Hour = use24Hour,
                useWestern = useWestern,
                tickerStateFlow = tickerStateFlow
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = qc.bg.copy(alpha = 0.62f),
                border = BorderStroke(1.dp, qc.border.copy(alpha = 0.85f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    PrayerHeroDateNavRow(
                        hijriDateAr = hijriDateAr.localizeDigits(useWestern),
                        gregorianDateAr = gregorianDateAr.localizeDigits(useWestern),
                        dayOffset = dayOffset,
                        onPrevDay = onPrevDay,
                        onToday = onToday,
                        onNextDay = onNextDay
                    )

                    HorizontalDivider(
                        color = qc.border.copy(alpha = 0.55f),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Public,
                                contentDescription = null,
                                tint = qc.textMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = methodTitleAr,
                                style = MaterialTheme.typography.labelSmall,
                                color = qc.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (timeZone.id != deviceZoneId) {
                            val cityClock = PrayerTimesCalculator.formatTime(
                                timestampMillis = System.currentTimeMillis(),
                                timeZone = timeZone,
                                use24Hour = use24Hour
                            ).localizeDigits(useWestern)
                            val utcLabel = PresetCities.formatUtcOffsetLabel(timeZone)
                            Text(
                                text = "$cityClock ($utcLabel)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = qc.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
