package com.djoudini.iptv.ui.onboarding

import com.djoudini.iptv.data.local.CategoryEntity
import com.djoudini.iptv.domain.model.ProviderType

sealed class OnboardingStep {
    object ProviderSelect : OnboardingStep()
    data class Login(val type: ProviderType) : OnboardingStep()
    object CategoryFilter : OnboardingStep()
    object Syncing : OnboardingStep()
    object Complete : OnboardingStep()
}

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.ProviderSelect,
    val isLoading: Boolean = false,
    val error: String? = null,
    val categories: List<SelectableCategory> = emptyList(),
    val syncProgress: Float = 0f,
    val providerId: Long? = null
)

data class SelectableCategory(
    val entity: CategoryEntity,
    val isSelected: Boolean = true
)
