package com.example.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.quranColors

@Composable
internal fun QuranShellNavigationRail(
    selectedTab: HomeTab,
    onSelectTab: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    NavigationRail(
        windowInsets = WindowInsets.safeDrawing,
        containerColor = qc.surface,
        contentColor = qc.text,
        modifier = modifier.fillMaxHeight()
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        HomeTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            val tabLabel = androidx.compose.ui.res.stringResource(tab.labelRes)
            NavigationRailItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tabLabel
                    )
                },
                label = { Text(tabLabel) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = qc.onSelected,
                    selectedTextColor = qc.selected,
                    indicatorColor = qc.selected,
                    unselectedIconColor = qc.textMuted,
                    unselectedTextColor = qc.textMuted
                ),
                modifier = Modifier.testTag("rail_tab_${tab.route}")
            )
        }
    }
}
