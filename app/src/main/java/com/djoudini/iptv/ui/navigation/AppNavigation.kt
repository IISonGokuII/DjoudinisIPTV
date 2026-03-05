package com.djoudini.iptv.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.djoudini.iptv.ui.dashboard.DashboardScreen
import com.djoudini.iptv.ui.livetv.LiveTvScreen
import com.djoudini.iptv.ui.login.LoginScreen
import com.djoudini.iptv.ui.settings.SettingsScreen
import com.djoudini.iptv.ui.vod.VodScreen
import com.djoudini.iptv.ui.vod.detail.VodDetailScreen
import com.djoudini.iptv.ui.series.SeriesScreen
import com.djoudini.iptv.ui.series.detail.SeriesDetailScreen
import com.djoudini.iptv.ui.trakt.TraktLoginScreen
import com.djoudini.iptv.data.preferences.SettingsRepository
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.first

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Setup : Screen("setup")
    object Dashboard : Screen("dashboard")
    object LiveTv : Screen("live_tv")
    object Movies : Screen("movies")
    object VodDetail : Screen("vod_detail/{vodId}") {
        fun createRoute(vodId: Int) = "vod_detail/$vodId"
    }
    object Series : Screen("series")
    object SeriesDetail : Screen("series_detail/{seriesId}") {
        fun createRoute(seriesId: Int) = "series_detail/$seriesId"
    }
    object Settings : Screen("settings")
    object TraktLogin : Screen("trakt_login")
}

@Composable
fun AppNavigation(
    settingsRepository: SettingsRepository
) {
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }

    // Check for existing credentials on startup
    LaunchedEffect(Unit) {
        val username = settingsRepository.usernameFlow.first()
        startDestination = if (!username.isNullOrBlank()) {
            Screen.Dashboard.route
        } else {
            Screen.Login.route
        }
    }

    // Show nothing (black screen) while checking credentials
    if (startDestination == null) {
        return 
    }

    NavHost(navController = navController, startDestination = startDestination!!) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToLive = { navController.navigate(Screen.LiveTv.route) },
                onNavigateToVod = { navController.navigate(Screen.Movies.route) },
                onNavigateToSeries = { navController.navigate(Screen.Series.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        
        // Content screens
        composable(Screen.LiveTv.route) { LiveTvScreen() }
        composable(Screen.Movies.route) { 
            VodScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { vodId -> 
                    navController.navigate(Screen.VodDetail.createRoute(vodId))
                }
            ) 
        }

        composable(Screen.VodDetail.route) { backStackEntry ->
            val vodId = backStackEntry.arguments?.getString("vodId")?.toIntOrNull() ?: 0
            VodDetailScreen(
                vodId = vodId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Series.route) { 
            SeriesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { seriesId ->
                    navController.navigate(Screen.SeriesDetail.createRoute(seriesId))
                }
            ) 
        }

        composable(Screen.SeriesDetail.route) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getString("seriesId")?.toIntOrNull() ?: 0
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

        composable(Screen.TraktLogin.route) {
            TraktLoginScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
