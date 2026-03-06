package com.djoudini.iptv.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djoudini.iptv.data.local.ChannelEntity
import com.djoudini.iptv.data.local.FavoriteEntity
import com.djoudini.iptv.data.local.SeriesEntity
import com.djoudini.iptv.data.local.VodEntity
import com.djoudini.iptv.data.repository.IptvRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoriteWithDetails(
    val streamId: String,
    val name: String,
    val type: String, // LIVE, VOD, SERIES
    val logoUrl: String?,
    val streamUrl: String?,
    val addedAt: Long
)

data class FavoritesUiState(
    val allFavorites: List<FavoriteWithDetails> = emptyList(),
    val liveFavorites: List<FavoriteWithDetails> = emptyList(),
    val vodFavorites: List<FavoriteWithDetails> = emptyList(),
    val seriesFavorites: List<FavoriteWithDetails> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val iptvRepository: IptvRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            iptvRepository.getAllFavorites().collect { favorites ->
                val favoritesWithDetails = favorites.mapNotNull { favorite ->
                    loadFavoriteDetails(favorite)
                }
                
                _uiState.update { state ->
                    state.copy(
                        allFavorites = favoritesWithDetails,
                        liveFavorites = favoritesWithDetails.filter { it.type == "LIVE" },
                        vodFavorites = favoritesWithDetails.filter { it.type == "VOD" },
                        seriesFavorites = favoritesWithDetails.filter { it.type == "SERIES" },
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun loadFavoriteDetails(favorite: FavoriteEntity): FavoriteWithDetails? {
        return when (favorite.streamType) {
            "LIVE" -> {
                val channel = iptvRepository.getChannelById(favorite.streamId)
                channel?.let {
                    FavoriteWithDetails(
                        streamId = favorite.streamId,
                        name = it.name,
                        type = "LIVE",
                        logoUrl = it.logoUrl,
                        streamUrl = it.streamUrl,
                        addedAt = favorite.addedAt
                    )
                }
            }
            "VOD" -> {
                val vod = iptvRepository.getVodById(favorite.streamId)
                vod?.let {
                    FavoriteWithDetails(
                        streamId = favorite.streamId,
                        name = it.name,
                        type = "VOD",
                        logoUrl = it.streamIcon,
                        streamUrl = it.directSource,
                        addedAt = favorite.addedAt
                    )
                }
            }
            "SERIES" -> {
                val series = iptvRepository.getSeriesById(favorite.streamId)
                series?.let {
                    FavoriteWithDetails(
                        streamId = favorite.streamId,
                        name = it.name,
                        type = "SERIES",
                        logoUrl = it.cover,
                        streamUrl = null,
                        addedAt = favorite.addedAt
                    )
                }
            }
            else -> null
        }
    }

    fun removeFavorite(streamId: String) {
        viewModelScope.launch {
            iptvRepository.removeFavorite(streamId)
        }
    }

    fun isFavorite(streamId: String): Flow<Boolean> {
        return iptvRepository.isFavorite(streamId)
    }

    suspend fun toggleFavorite(streamId: String, streamType: String) {
        iptvRepository.toggleFavorite(streamId, streamType)
    }
}
