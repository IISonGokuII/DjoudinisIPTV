package com.djoudini.iptv.ui.vod

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.djoudini.iptv.data.local.CategoryEntity
import com.djoudini.iptv.data.remote.XtreamVodStream
import com.djoudini.iptv.data.repository.IptvRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VodUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val currentCategory: CategoryEntity? = null,
    val movies: Flow<PagingData<XtreamVodStream>> = flowOf(PagingData.empty()),
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VodViewModel @Inject constructor(
    private val iptvRepository: IptvRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VodUiState())
    val uiState: StateFlow<VodUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            iptvRepository.getVodCategoriesFlow().collect { categories ->
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
        val pagingFlow = iptvRepository.getVodsByCategoryPaging(category.id)
            .map { pagingData ->
                pagingData.map { entity ->
                    XtreamVodStream(
                        num = 0,
                        name = entity.name,
                        streamType = "movie",
                        streamId = entity.streamId,
                        streamIcon = entity.streamIcon,
                        rating = entity.rating,
                        rating5Based = null,
                        added = null,
                        categoryId = entity.categoryId,
                        customSid = null,
                        directSource = entity.directSource,
                        containerExtension = entity.containerExtension
                    )
                }
            }
            .cachedIn(viewModelScope)

        _uiState.value = _uiState.value.copy(movies = pagingFlow)
    }

    fun syncCurrentCategory() {
        val category = _uiState.value.currentCategory ?: return
        
        _uiState.value = _uiState.value.copy(isSyncing = true)
        viewModelScope.launch {
            try {
                iptvRepository.syncVodStreamsForCategory(category.id)
            } catch (e: Exception) {
                // Handle error if needed
            } finally {
                _uiState.value = _uiState.value.copy(isSyncing = false)
            }
        }
    }
}
