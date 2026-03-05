package com.djoudini.iptv.ui.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.djoudini.iptv.data.local.CategoryEntity
import com.djoudini.iptv.data.remote.XtreamSeries
import com.djoudini.iptv.data.repository.IptvRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeriesUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val currentCategory: CategoryEntity? = null,
    val series: Flow<PagingData<XtreamSeries>> = flowOf(PagingData.empty()),
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val iptvRepository: IptvRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeriesUiState())
    val uiState: StateFlow<SeriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            iptvRepository.getSeriesCategoriesFlow().collect { categories ->
                if (categories.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
                        isLoading = false
                    )
                    if (_uiState.value.currentCategory == null) {
                        selectCategory(categories.first())
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun selectCategory(category: CategoryEntity) {
        _uiState.value = _uiState.value.copy(currentCategory = category, isLoading = false)

        // Set the Paging Flow from local DB (Instant)
        val pagingFlow = iptvRepository.getSeriesByCategoryPaging(category.id)
            .map { pagingData ->
                pagingData.map { entity ->
                    XtreamSeries(
                        num = 0,
                        name = entity.name,
                        seriesId = entity.seriesId,
                        cover = entity.cover,
                        plot = null,
                        cast = null,
                        director = null,
                        genre = null,
                        releaseDate = null,
                        lastModified = null,
                        rating = entity.rating,
                        rating5Based = null,
                        backdropPath = null,
                        youtubeTrailer = null,
                        episodeRunTime = null,
                        categoryId = entity.categoryId
                    )
                }
            }
            .cachedIn(viewModelScope)

        _uiState.value = _uiState.value.copy(series = pagingFlow)
    }

    fun syncCurrentCategory() {
        val category = _uiState.value.currentCategory ?: return
        
        _uiState.value = _uiState.value.copy(isSyncing = true)
        viewModelScope.launch {
            try {
                iptvRepository.syncSeriesForCategory(category.id)
            } catch (e: Exception) {
                // Handle error if needed
            } finally {
                _uiState.value = _uiState.value.copy(isSyncing = false)
            }
        }
    }
}
