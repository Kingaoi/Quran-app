package com.example.ui.screens.prayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.domain.prayer.CityLocation
import com.example.domain.prayer.PrayerRegion
import com.example.domain.prayer.PresetCities

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CityPickerBottomSheet(
    currentCity: CityLocation,
    isLocatingGps: Boolean,
    use24Hour: Boolean = false,
    onRequestGpsLocation: () -> Unit,
    onSelectCity: (CityLocation) -> Unit,
    onDismiss: () -> Unit
) {
    val useWestern = LocalUseWesternDigits.current
    var query by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf<PrayerRegion?>(null) }
    val nowMillis = remember { System.currentTimeMillis() }

    val filteredCities = remember(query, selectedRegion) {
        PresetCities.search(query = query, region = selectedRegion)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "اختيار المدينة والموقع",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${filteredCities.size.toAppDigits(useWestern)} مدينة",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    onRequestGpsLocation()
                    onDismiss()
                },
                enabled = !isLocatingGps,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("prayer_use_gps_button")
            ) {
                if (isLocatingGps) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(imageVector = Icons.Rounded.MyLocation, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "تحديد موقعي الحالي تلقائياً")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("ابحث عن المدينة، الدولة أو المنطقة...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { query = "" }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "مسح البحث")
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prayer_city_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item(key = "ALL") {
                    FilterChip(
                        selected = selectedRegion == null,
                        onClick = { selectedRegion = null },
                        label = { Text("الكل") },
                        leadingIcon = if (selectedRegion == null) {
                            { Icon(Icons.Rounded.Public, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .testTag("prayer_region_chip_all")
                    )
                }
                items(PrayerRegion.entries, key = { it.name }) { region ->
                    val isRegionSelected = selectedRegion == region
                    FilterChip(
                        selected = isRegionSelected,
                        onClick = { selectedRegion = if (isRegionSelected) null else region },
                        label = { Text(region.titleAr) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .testTag("prayer_region_chip_${region.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredCities.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "لا توجد مدينة مطابقة لبحثك",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            query = ""
                            selectedRegion = null
                        },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("إظهار جميع المدن")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredCities,
                        key = { "${it.nameAr}_${it.countryAr}_${it.region.name}" }
                    ) { city ->
                        val isSelected = city.nameAr == currentCity.nameAr &&
                            city.countryAr == currentCity.countryAr &&
                            !currentCity.isFromGps
                        CityPickerItemCard(
                            city = city,
                            isSelected = isSelected,
                            showRegionLabel = selectedRegion == null,
                            nowMillis = nowMillis,
                            use24Hour = use24Hour,
                            useWesternDigits = useWestern,
                            onClick = {
                                onSelectCity(city)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}
