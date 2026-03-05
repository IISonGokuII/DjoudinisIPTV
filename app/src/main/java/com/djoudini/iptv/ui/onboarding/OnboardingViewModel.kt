package com.djoudini.iptv.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djoudini.iptv.data.preferences.SettingsRepository
import com.djoudini.iptv.data.repository.IptvRepository
import com.djoudini.iptv.domain.model.ProviderType
import com.djoudini.iptv.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val iptvRepository: IptvRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun selectProvider(type: ProviderType) {
        _uiState.update { it.copy(currentStep = OnboardingStep.Login(type)) }
    }

    fun loginXtream(host: String, user: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = iptvRepository.loginXtream(host, user, pass)) {
                is Result.Success -> {
                    // Start fetching categories to prepare for Step 3
                    // We need the providerId from the last inserted provider (ideally login returns it)
                    // For now, let's assume we can fetch it or get the latest from DB
                    // Simulating providerId 1 for now, but in reality, it should be the ID from ProviderDao
                    _uiState.update { it.copy(isLoading = false, currentStep = OnboardingStep.CategoryFilter, providerId = 1L) }
                    loadCategories(1L)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun importM3u(name: String, inputStream: InputStream) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = iptvRepository.importM3u(name, inputStream)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, currentStep = OnboardingStep.CategoryFilter, providerId = result.data) }
                    loadCategories(result.data)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    private fun loadCategories(providerId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Fetch all (Live, VOD, Series) and map to SelectableCategory
            // Here we use the repository to fetch and store them in DB first
            iptvRepository.fetchAllXtreamCategories(providerId)
            
            // Collect categories from DB to show in UI
            iptvRepository.getCategories(providerId, "LIVE").first().let { live ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        categories = live.map { SelectableCategory(it, true) }
                    )
                }
            }
        }
    }

    fun toggleCategory(categoryId: String) {
        _uiState.update { state ->
            val updated = state.categories.map { 
                if (it.entity.id == categoryId) it.copy(isSelected = !it.isSelected) else it 
            }
            state.copy(categories = updated)
        }
    }

    fun selectAll(select: Boolean) {
        _uiState.update { state ->
            state.copy(categories = state.categories.map { it.copy(isSelected = select) })
        }
    }

    fun startSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(currentStep = OnboardingStep.Syncing, syncProgress = 0f) }
            // Filter unselected categories in DB
            // Sync streams for selected ones
            _uiState.update { it.copy(syncProgress = 1.0f) }
            finishOnboarding()
        }
    }

    private suspend fun finishOnboarding() {
        settingsRepository.saveOnboardingComplete(true)
        _uiState.update { it.copy(currentStep = OnboardingStep.Complete) }
    }
}
