package com.example.ui.navigation

import android.net.Uri
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.domain.model.DhikrCategory
import com.example.ui.screens.adhkar.AdhkarCategoryBrowseScreen
import com.example.ui.screens.adhkar.AdhkarDetailScreen
import com.example.ui.viewmodel.AdhkarUiState
import com.example.ui.viewmodel.AdhkarViewModel
import com.example.ui.viewmodel.QuranUiState

internal fun NavGraphBuilder.addAdhkarNavigationRoutes(
    navController: NavHostController,
    adhkarViewModel: AdhkarViewModel,
    adhkarState: AdhkarUiState,
    uiState: QuranUiState
) {
    composable(
        route = ROUTE_ADHKAR_BROWSE,
        arguments = listOf(navArgument(ROUTE_ADHKAR_ARG) { type = NavType.StringType })
    ) { backStackEntry ->
        val category = DhikrCategory.fromId(backStackEntry.arguments?.getString(ROUTE_ADHKAR_ARG))
        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
            AdhkarCategoryBrowseScreen(
                category = category,
                uiState = adhkarState,
                hapticEnabled = uiState.settings.hapticFeedbackEnabled,
                onIncrementDhikr = adhkarViewModel::incrementDhikr,
                onResetCategory = adhkarViewModel::resetCategory,
                onToggleFavorite = adhkarViewModel::toggleFavorite,
                onTogglePlayAudio = adhkarViewModel::togglePlayDhikrAudio,
                onStopAudio = adhkarViewModel::stopDhikrAudio,
                onOpenReader = { chapterTitle, initialIndex ->
                    navController.navigate(
                        adhkarDetailRoute(
                            categoryId = category.id,
                            chapterTitle = chapterTitle,
                            initialIndex = initialIndex
                        )
                    ) { launchSingleTop = true }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }

    composable(
        route = ROUTE_ADHKAR_DETAIL,
        arguments = listOf(
            navArgument(ROUTE_ADHKAR_ARG) { type = NavType.StringType },
            navArgument(ROUTE_ADHKAR_CHAPTER_ARG) { type = NavType.StringType; defaultValue = "" },
            navArgument(ROUTE_ADHKAR_INDEX_ARG) { type = NavType.IntType; defaultValue = 0 }
        )
    ) { backStackEntry ->
        val category = DhikrCategory.fromId(backStackEntry.arguments?.getString(ROUTE_ADHKAR_ARG))
        val encodedChapter = backStackEntry.arguments?.getString(ROUTE_ADHKAR_CHAPTER_ARG).orEmpty()
        val chapterTitle = Uri.decode(encodedChapter)
        val initialIndex = backStackEntry.arguments?.getInt(ROUTE_ADHKAR_INDEX_ARG) ?: 0
        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
            AdhkarDetailScreen(
                category = category,
                uiState = adhkarState,
                chapterTitle = chapterTitle,
                initialIndex = initialIndex,
                autoAdvanceDhikr = uiState.settings.autoAdvanceDhikr,
                hapticEnabled = uiState.settings.hapticFeedbackEnabled,
                onIncrementDhikr = adhkarViewModel::incrementDhikr,
                onResetDhikr = adhkarViewModel::resetDhikr,
                onResetCategory = adhkarViewModel::resetCategory,
                onResetItems = adhkarViewModel::resetItems,
                onAdjustFontSize = adhkarViewModel::adjustFontSize,
                onToggleFavorite = adhkarViewModel::toggleFavorite,
                onTogglePlayAudio = adhkarViewModel::togglePlayDhikrAudio,
                onStopAudio = adhkarViewModel::stopDhikrAudio,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
