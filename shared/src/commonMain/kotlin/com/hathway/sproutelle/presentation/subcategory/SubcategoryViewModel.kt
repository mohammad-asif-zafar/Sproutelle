package com.hathway.sproutelle.presentation.subcategory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hathway.sproutelle.domain.model.Subcategory
import com.hathway.sproutelle.domain.repository.LearningRepository
import com.hathway.sproutelle.domain.repository.LearningState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SubcategoryUiState {
    data object Loading : SubcategoryUiState
    data class Success(val subcategories: List<Subcategory>, val isOffline: Boolean = false) : SubcategoryUiState
    data class Error(val message: String) : SubcategoryUiState
    data object Empty : SubcategoryUiState
}

class SubcategoryViewModel(
    val explorerId: String,
    val explorerTitle: String,
    private val learningRepository: LearningRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SubcategoryUiState>(SubcategoryUiState.Loading)
    val uiState: StateFlow<SubcategoryUiState> = _uiState.asStateFlow()

    init {
        loadSubcategories()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = SubcategoryUiState.Loading
            try {
                learningRepository.refreshSubcategories(explorerId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    fun loadSubcategories() {
        viewModelScope.launch {
            learningRepository.getSubcategoriesState(explorerId).collect { state ->
                _uiState.value = when (state) {
                    is LearningState.Loading -> SubcategoryUiState.Loading
                    is LearningState.Cached -> if (state.data.isEmpty()) SubcategoryUiState.Empty else SubcategoryUiState.Success(state.data, isOffline = false)
                    is LearningState.Fresh -> if (state.data.isEmpty()) SubcategoryUiState.Empty else SubcategoryUiState.Success(state.data, isOffline = false)
                    is LearningState.Offline -> if (state.data.isEmpty()) SubcategoryUiState.Empty else SubcategoryUiState.Success(state.data, isOffline = true)
                    is LearningState.Error -> state.cachedData?.let { if (it.isNotEmpty()) SubcategoryUiState.Success(it, isOffline = true) else SubcategoryUiState.Error(state.message) } ?: SubcategoryUiState.Error(state.message)
                }
            }
        }
    }
}
