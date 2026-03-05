package com.djoudini.iptv.ui.livetv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.djoudini.iptv.data.local.CategoryEntity
import com.djoudini.iptv.data.local.ChannelEntity
import com.djoudini.iptv.data.repository.IptvRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.djoudini.iptv.data.preferences.SettingsRepository

data class LiveTvUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val currentCategory: CategoryEntity? = null,
    val channels: Flow<PagingData<ChannelEntity>> = flowOf(PagingData.empty()),
    val currentChannel: ChannelEntity? = null,
    val isLoading: Boolean = true,
    val isFullScreen: Boolean = false,
    val bufferSize: Int = 5000 // Default buffer size in milliseconds
)

@HiltViewModel
class LiveTvViewModel @Inject constructor(
    private val iptvRepository: IptvRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveTvUiState())
    val uiState: StateFlow<LiveTvUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.onboardingCompleteFlow.collect { onboardingComplete ->
                if (onboardingComplete) {
                    loadCategories()
                } else {
                    // Handle case where onboarding is not complete
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            iptvRepository.getLiveCategoriesFlow().collect { categories ->
                _uiState.update { it.copy(categories = categories, isLoading = false) }
                if (categories.isNotEmpty() && _uiState.value.currentCategory == null) {
                    selectCategory(categories.first())
                }
            }
        }
    }

    fun selectCategory(category: CategoryEntity) {
        _uiState.update { it.copy(currentCategory = category) }
        // Note: Actual paging implementation would go here
    }

    fun selectChannel(channel: ChannelEntity) {
        _uiState.update { it.copy(currentChannel = channel) }
    }
}
