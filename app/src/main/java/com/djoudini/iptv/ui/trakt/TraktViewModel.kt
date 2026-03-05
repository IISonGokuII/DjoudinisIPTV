package com.djoudini.iptv.ui.trakt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djoudini.iptv.data.preferences.SettingsRepository
import com.djoudini.iptv.data.repository.TraktAuthStatus
import com.djoudini.iptv.data.repository.TraktRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TraktUiState(
    val userCode: String? = null,
    val verificationUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val authStatus: TraktAuthStatus? = null,
    val loggedInUser: String? = null
)

@HiltViewModel
class TraktViewModel @Inject constructor(
    private val traktRepository: TraktRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TraktUiState())
    val uiState: StateFlow<TraktUiState> = _uiState.asStateFlow()

    val traktUserName: StateFlow<String?> = settingsRepository.traktUserNameFlow.stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )

    fun startDeviceAuth() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = traktRepository.generateDeviceCode()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userCode = response.userCode,
                    verificationUrl = response.verificationUrl
                )
                
                // Start polling
                traktRepository.pollForAccessToken(response.deviceCode, response.interval).collect { status ->
                    _uiState.value = _uiState.value.copy(authStatus = status)
                    if (status is TraktAuthStatus.Error) {
                        _uiState.value = _uiState.value.copy(error = status.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            traktRepository.logout()
        }
    }
}
