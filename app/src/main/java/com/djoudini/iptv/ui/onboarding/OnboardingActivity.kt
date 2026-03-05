package com.djoudini.iptv.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.djoudini.iptv.MainActivity
import com.djoudini.iptv.domain.model.ProviderType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: OnboardingViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            when (val step = uiState.currentStep) {
                is OnboardingStep.ProviderSelect -> {
                    ProviderSelectScreen(onSelect = { viewModel.selectProvider(it) })
                }
                is OnboardingStep.Login -> {
                    if (step.type == ProviderType.XTREAM) {
                        LoginScreen(
                            isLoading = uiState.isLoading,
                            error = uiState.error,
                            onLogin = { h, u, p -> viewModel.loginXtream(h, u, p) }
                        )
                    } else {
                        // M3U Screen (Simplified for now)
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator() // Placeholder
                        }
                    }
                }
                is OnboardingStep.CategoryFilter -> {
                    CategorySelectScreen(
                        categories = uiState.categories,
                        onToggle = { viewModel.toggleCategory(it) },
                        onSelectAll = { viewModel.selectAll(it) },
                        onNext = { viewModel.startSync() }
                    )
                }
                is OnboardingStep.Syncing -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is OnboardingStep.Complete -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}
