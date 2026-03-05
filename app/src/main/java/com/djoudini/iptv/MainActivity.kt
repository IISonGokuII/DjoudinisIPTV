package com.djoudini.iptv

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.djoudini.iptv.ui.navigation.AppNavigation
import com.djoudini.iptv.data.preferences.SettingsRepository
import com.djoudini.iptv.ui.onboarding.OnboardingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            val isComplete = settingsRepository.isOnboardingCompleteFlow.first()
            if (!isComplete) {
                startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
                finish()
            }
        }
        
        // A minimal custom dark theme optimized for OLED TVs and immersive viewing
        val IptvDarkColorScheme = darkColorScheme(
            primary = Color(0xFFE50914), // Netflix Red accent
            background = Color(0xFF0A0A0A),
            surface = Color(0xFF141414),
            onPrimary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )

        setContent {
            MaterialTheme(colorScheme = IptvDarkColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(settingsRepository)
                }
            }
        }
    }
}
