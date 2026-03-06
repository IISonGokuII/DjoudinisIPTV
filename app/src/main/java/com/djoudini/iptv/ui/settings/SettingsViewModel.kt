package com.djoudini.iptv.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djoudini.iptv.data.preferences.SettingsRepository
import com.djoudini.iptv.domain.model.ViewType
import com.djoudini.iptv.domain.model.BufferSizeDefaults
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userAgent: String = SettingsRepository.DEFAULT_USER_AGENT,
    val bufferSize: Int = BufferSizeDefaults.DEFAULT_BUFFER_SIZE,
    val viewType: ViewType = ViewType.GRID,
    val hardwareDecoding: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.userAgentFlow,
        settingsRepository.customBufferMsFlow,
        settingsRepository.viewPreferenceFlow
    ) { userAgent, bufferSize, viewType ->
        SettingsUiState(
            userAgent = userAgent ?: SettingsRepository.DEFAULT_USER_AGENT,
            bufferSize = bufferSize,
            viewType = ViewType.valueOf(viewType ?: ViewType.GRID.name),
            hardwareDecoding = true
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
            settingsRepository.saveBufferSize(com.djoudini.iptv.domain.model.BufferSize.CUSTOM, sizeMs)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            settingsRepository.clearAllCache()
        }
    }

    fun updateViewType(viewType: ViewType) {
        viewModelScope.launch {
            settingsRepository.saveViewPreference(viewType.name)
        }
    }
}
