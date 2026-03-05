package com.djoudini.iptv.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djoudini.iptv.data.preferences.SettingsRepository
import com.djoudini.iptv.data.preferences.ViewType
import com.djoudini.iptv.data.repository.IptvRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userAgent: String = SettingsRepository.DEFAULT_USER_AGENT,
    val bufferSize: Int = SettingsRepository.DEFAULT_BUFFER_SIZE,
    val viewType: ViewType = ViewType.GRID,
    val hardwareDecoding: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val iptvRepository: IptvRepository
) : ViewModel() {

    // Combine flows from dataStore into a single UI State
    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.userAgentFlow,
        settingsRepository.bufferSizeFlow,
        settingsRepository.viewPreferenceFlow
    ) { userAgent, bufferSize, viewType ->
        SettingsUiState(
            userAgent = userAgent,
            bufferSize = bufferSize,
            viewType = viewType,
            hardwareDecoding = true // We can add this to DataStore later
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun updateUserAgent(newUserAgent: String) {
        viewModelScope.launch {
            settingsRepository.saveUserAgent(newUserAgent)
        }
    }

    fun updateBufferSize(sizeMs: Int) {
        viewModelScope.launch {
            settingsRepository.saveBufferSize(sizeMs)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            iptvRepository.clearAllCache()
        }
    }

    fun updateViewType(viewType: ViewType) {
        viewModelScope.launch {
            settingsRepository.saveViewPreference(viewType)
        }
    }
}
