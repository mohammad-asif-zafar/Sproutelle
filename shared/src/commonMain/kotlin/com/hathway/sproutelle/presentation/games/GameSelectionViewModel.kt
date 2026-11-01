package com.hathway.sproutelle.presentation.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.domain.repository.LearningRepository
import com.hathway.sproutelle.domain.repository.LearningState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface GameSelectionUiState {
    data object Loading : GameSelectionUiState
    data class Success(val games: List<GameItem>, val isOffline: Boolean = false) : GameSelectionUiState
    data class Error(val message: String) : GameSelectionUiState
    data object Empty : GameSelectionUiState
}

class GameSelectionViewModel(
    val explorerId: String,
    val subcategoryId: String,
    val subcategoryName: String,
    private val learningRepository: LearningRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GameSelectionUiState>(GameSelectionUiState.Loading)
    val uiState: StateFlow<GameSelectionUiState> = _uiState.asStateFlow()

    init {
        loadGames()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = GameSelectionUiState.Loading
            try {
                learningRepository.refreshGamesForSubcategory(subcategoryId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    fun loadGames() {
        viewModelScope.launch {
            learningRepository.getGamesForSubcategoryState(subcategoryId).collect { state ->
                _uiState.value = when (state) {
                    is LearningState.Loading -> GameSelectionUiState.Loading
                    is LearningState.Cached -> if (state.data.isEmpty()) GameSelectionUiState.Empty else GameSelectionUiState.Success(state.data, isOffline = false)
                    is LearningState.Fresh -> if (state.data.isEmpty()) GameSelectionUiState.Empty else GameSelectionUiState.Success(state.data, isOffline = false)
                    is LearningState.Offline -> if (state.data.isEmpty()) GameSelectionUiState.Empty else GameSelectionUiState.Success(state.data, isOffline = true)
                    is LearningState.Error -> state.cachedData?.let { if (it.isNotEmpty()) GameSelectionUiState.Success(it, isOffline = true) else GameSelectionUiState.Error(state.message) } ?: GameSelectionUiState.Error(state.message)
                }
            }
        }
    }
}
