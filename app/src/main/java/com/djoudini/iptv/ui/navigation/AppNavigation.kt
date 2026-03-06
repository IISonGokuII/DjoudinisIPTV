package com.djoudini.iptv.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.djoudini.iptv.ui.dashboard.DashboardScreen
import com.djoudini.iptv.ui.favorites.FavoritesScreen
import com.djoudini.iptv.ui.livetv.LiveTvScreen
import com.djoudini.iptv.ui.onboarding.OnboardingContent
import com.djoudini.iptv.ui.series.SeriesScreen
import com.djoudini.iptv.ui.series.detail.SeriesDetailScreen
import com.djoudini.iptv.ui.settings.SettingsScreen
import com.djoudini.iptv.ui.trakt.TraktLoginScreen
import com.djoudini.iptv.ui.vod.VodScreen
import com.djoudini.iptv.ui.vod.detail.VodDetailScreen
import com.djoudini.iptv.data.preferences.SettingsRepository
import kotlinx.coroutines.flow.first

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object LiveTv : Screen("live_tv")
    object Vod : Screen("vod")
    object VodDetail : Screen("vod_detail/{vodId}") {
        fun createRoute(vodId: Int) = "vod_detail/$vodId"
    }
    object Series : Screen("series")
    object SeriesDetail : Screen("series_detail/{seriesId}") {
        fun createRoute(seriesId: Int) = "series_detail/$seriesId"
    }
    object Settings : Screen("settings")
    object TraktLogin : Screen("trakt_login")
    object Favorites : Screen("favorites")
}

@Composable
fun AppNavigation(
    settingsRepository: SettingsRepository
) {
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val username = settingsRepository.usernameFlow.first()
        startDestination = if (!username.isNullOrBlank()) {
            Screen.Dashboard.route
        } else {
            Screen.Onboarding.route
        }
    }

    startDestination?.let { destination ->
        NavHost(navController = navController, startDestination = destination) {
            composable(Screen.Onboarding.route) {
                OnboardingContent(
                    onOnboardingComplete = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToLive = { navController.navigate(Screen.LiveTv.route) },
                    onNavigateToVod = { navController.navigate(Screen.Vod.route) },
                    onNavigateToSeries = { navController.navigate(Screen.Series.route) },
                    onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.LiveTv.route) {
                LiveTvScreen()
            }
            composable(Screen.Vod.route) {
                VodScreen(
                    onNavigateToVodDetail = { vodId -> navController.navigate(Screen.VodDetail.createRoute(vodId)) }
                )
            }
            composable(
                route = Screen.VodDetail.route,
                arguments = listOf(navArgument("vodId") { type = NavType.IntType })
            ) { backStackEntry ->
                val vodId = backStackEntry.arguments?.getInt("vodId") ?: 0
                VodDetailScreen(
                    vodId = vodId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Series.route) {
                SeriesScreen(
                    onNavigateToSeriesDetail = { seriesId -> navController.navigate(Screen.SeriesDetail.createRoute(seriesId)) }
                )
            }
            composable(
                route = Screen.SeriesDetail.route,
                arguments = listOf(navArgument("seriesId") { type = NavType.IntType })
            ) { backStackEntry ->
                val seriesId = backStackEntry.arguments?.getInt("seriesId") ?: 0
                SeriesDetailScreen(
                    seriesId = seriesId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTrakt = { navController.navigate(Screen.TraktLogin.route) }
                )
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlayer = { /* TODO: Navigate to player */ },
                    onNavigateToSeriesDetail = { seriesId -> navController.navigate(Screen.SeriesDetail.createRoute(seriesId.toInt())) }
                )
            }
            composable(Screen.TraktLogin.route) {
                TraktLoginScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
