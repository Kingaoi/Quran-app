package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppTheme
import com.example.ui.components.RubElHizbBadge
import com.example.ui.navigation.AppLaunchDestination
import com.example.ui.navigation.QuranAppShell
import com.example.ui.theme.QuranWsTheme
import com.example.ui.theme.resolveEffectiveAppTheme
import com.example.ui.util.HighRefreshRateHelper
import com.example.ui.viewmodel.QuranViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val launchEventFlow = MutableStateFlow<Pair<Int, AppLaunchDestination>?>(null)
    private var launchSeq = 0

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) dispatchLaunchIntent(intent)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        HighRefreshRateHelper.observeAndLock(this, this)
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val appContainer = (application as QuranApplication).container
            val viewModel: QuranViewModel = viewModel(factory = QuranViewModel.factory(appContainer))
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val offlineState by viewModel.offlineState.collectAsStateWithLifecycle()
            val launchEvent by launchEventFlow.collectAsStateWithLifecycle()

            val effectiveTheme = resolveEffectiveAppTheme(uiState.settings.appTheme)
            val useDarkTheme = effectiveTheme == AppTheme.DARK

            SideEffect {
                val barStyle = if (useDarkTheme) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                }
                enableEdgeToEdge(
                    statusBarStyle = barStyle,
                    navigationBarStyle = barStyle
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
            }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                QuranWsTheme(
                    appTheme = effectiveTheme,
                    quranFontOption = uiState.settings.quranFont,
                    uthmanicFontFamily = uiState.uthmanicFontFamily,
                    useWesternDigits = uiState.settings.useWesternDigits
                ) {
                    if (uiState.isLoading) {
                        LoadingSplashView()
                    } else {
                        QuranAppShell(
                            useNavigationRail = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded,
                            openWirdOnLaunch = launchEvent?.second is AppLaunchDestination.OpenWirdTab,
                            launchDestinationEvent = launchEvent,
                            viewModel = viewModel,
                            uiState = uiState,
                            offlineState = offlineState
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        dispatchLaunchIntent(intent)
    }

    private fun dispatchLaunchIntent(intent: Intent?) {
        AppLaunchDestination.fromIntent(intent)?.let { dest ->
            launchEventFlow.value = (++launchSeq) to dest
        }
    }
}

@Composable
private fun LoadingSplashView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RubElHizbBadge(number = 114, size = 72.dp)
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.splash_loading_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}
