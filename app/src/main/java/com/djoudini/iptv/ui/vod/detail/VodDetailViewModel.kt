package com.djoudini.iptv.ui.vod.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djoudini.iptv.data.remote.XtreamVodInfo
import com.djoudini.iptv.data.repository.IptvRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VodDetailUiState(
    val vodInfo: XtreamVodInfo? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class VodDetailViewModel @Inject constructor(
    private val iptvRepository: IptvRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VodDetailUiState())
    val uiState: StateFlow<VodDetailUiState> = _uiState.asStateFlow()

    fun loadVodInfo(vodId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val info = iptvRepository.getVodInfo(vodId)
                _uiState.value = _uiState.value.copy(vodInfo = info, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Failed to load movie details"
                )
            }
        }
    }
}
