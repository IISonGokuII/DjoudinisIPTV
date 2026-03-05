package com.djoudini.iptv.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djoudini.iptv.data.preferences.SettingsRepository
import com.djoudini.iptv.data.repository.IptvRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DashboardUiState(
    val username: String = "...",
    val expirationDate: String = "...",
    val activeConnections: String = "...",
    val liveCount: String = "0",
    val vodCount: String = "0",
    val seriesCount: String = "0",
    val isLiveLoading: Boolean = true,
    val isVodLoading: Boolean = true,
    val isSeriesLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val iptvRepository: IptvRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // 1. Get cached data immediately for instant UI
            val username = settingsRepository.usernameFlow.first() ?: "Benutzer"
            val host = settingsRepository.hostUrlFlow.first()
            val pass = settingsRepository.passwordFlow.first()
            val expDateString = settingsRepository.expDateFlow.first() 
            val connections = settingsRepository.activeConsFlow.first()
            val maxConnections = settingsRepository.maxConnectionsFlow.first()
            
            val cachedLive = settingsRepository.cachedLiveCountFlow.first()
            val cachedVod = settingsRepository.cachedVodCountFlow.first()
            val cachedSeries = settingsRepository.cachedSeriesCountFlow.first()

            val formattedDate = if (!expDateString.isNullOrBlank() && expDateString.toLongOrNull() != null) {
                try {
                    val timestamp = expDateString.toLong()
                    val millis = if (timestamp < 10_000_000_000L) timestamp * 1000 else timestamp
                    val sdf = SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN)
                    sdf.format(Date(millis))
                } catch (e: Exception) {
                    "Unbegrenzt"
                }
            } else if (expDateString == "0") {
                 "Unbegrenzt"
            } else {
                "Unbegrenzt"
            }

            // Update UI with cached counts immediately
            _uiState.update { it.copy(
                username = username,
                expirationDate = formattedDate,
                activeConnections = "$connections / $maxConnections",
                liveCount = cachedLive,
                vodCount = cachedVod,
                seriesCount = cachedSeries,
                // Still show loading bars if counts are 0 or we want to refresh
                isLiveLoading = cachedLive == "0",
                isVodLoading = cachedVod == "0",
                isSeriesLoading = cachedSeries == "0"
            )}

            if (!host.isNullOrBlank() && !username.isNullOrBlank() && !pass.isNullOrBlank()) {
                fetchCountsParallel()
            } else {
                _uiState.update { it.copy(
                    isLiveLoading = false,
                    isVodLoading = false,
                    isSeriesLoading = false
                )}
            }
        }
    }

    private fun fetchCountsParallel() {
        // Run sequentially to prevent OutOfMemory errors on TV devices 
        // when parsing 3 massive JSON streams (Live, VOD, Series) concurrently.
        viewModelScope.launch {
            // Live
            try {
                val liveCount = iptvRepository.fetchLiveStreamsCount()
                _uiState.update { it.copy(liveCount = liveCount.toString(), isLiveLoading = false) }
                saveFinalCounts()
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLiveLoading = false) }
            }

            // VOD
            try {
                val vodCount = iptvRepository.fetchVodStreamsCount()
                _uiState.update { it.copy(vodCount = vodCount.toString(), isVodLoading = false) }
                saveFinalCounts()
            } catch (t: Throwable) {
                _uiState.update { it.copy(isVodLoading = false) }
            }

            // Series
            try {
                val seriesCount = iptvRepository.fetchSeriesCount()
                _uiState.update { it.copy(seriesCount = seriesCount.toString(), isSeriesLoading = false) }
                saveFinalCounts()
            } catch (t: Throwable) {
                _uiState.update { it.copy(isSeriesLoading = false) }
            }
        }
    }

    private suspend fun saveFinalCounts() {
        // Save to cache when all or some are done
        val s = _uiState.value
        settingsRepository.saveCounts(s.liveCount, s.vodCount, s.seriesCount)
    }
}
