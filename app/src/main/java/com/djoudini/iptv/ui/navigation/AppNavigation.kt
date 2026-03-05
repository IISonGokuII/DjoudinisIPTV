package com.djoudini.iptv.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.djoudini.iptv.ui.dashboard.DashboardScreen
import com.djoudini.iptv.ui.onboarding.OnboardingActivity
import com.djoudini.iptv.data.preferences.SettingsRepository
import kotlinx.coroutines.flow.first

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
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
            Screen.Onboarding.route
        }
    }

    startDestination?.let { destination ->
        NavHost(navController = navController, startDestination = destination) {
            composable(Screen.Onboarding.route) {
                com.djoudini.iptv.ui.onboarding.OnboardingContent(
                    onOnboardingComplete = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToLive = { /* TODO */ },
                    onNavigateToVod = { /* TODO */ },
                    onNavigateToSeries = { /* TODO */ },
                    onNavigateToSettings = { /* TODO */ }
                )
            }
        }
    }
}
