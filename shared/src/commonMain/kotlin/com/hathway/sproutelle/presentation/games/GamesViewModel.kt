package com.hathway.sproutelle.presentation.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hathway.sproutelle.domain.model.SubcategoryWithGames
import com.hathway.sproutelle.domain.repository.LearningRepository
import com.hathway.sproutelle.domain.repository.LearningState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface GamesUiState {
    data object Loading : GamesUiState
    data class Success(val subcategoryWithGames: List<SubcategoryWithGames>, val isOffline: Boolean = false) : GamesUiState
    data class Error(val message: String) : GamesUiState
    data object Empty : GamesUiState
}

class GamesViewModel(
    val explorerId: String,
    val explorerTitle: String,
    private val learningRepository: LearningRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GamesUiState>(GamesUiState.Loading)
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    init {
        loadGames()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = GamesUiState.Loading
            try {
                learningRepository.refreshGamesForExplorer(explorerId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    fun loadGames() {
        viewModelScope.launch {
            learningRepository.getGamesForExplorerState(explorerId).collect { state ->
                _uiState.value = when (state) {
                    is LearningState.Loading -> GamesUiState.Loading
                    is LearningState.Cached -> if (state.data.isEmpty() || state.data.all { it.games.isEmpty() }) GamesUiState.Empty else GamesUiState.Success(state.data, isOffline = false)
                    is LearningState.Fresh -> if (state.data.isEmpty() || state.data.all { it.games.isEmpty() }) GamesUiState.Empty else GamesUiState.Success(state.data, isOffline = false)
                    is LearningState.Offline -> if (state.data.isEmpty() || state.data.all { it.games.isEmpty() }) GamesUiState.Empty else GamesUiState.Success(state.data, isOffline = true)
                    is LearningState.Error -> state.cachedData?.let { if (it.isNotEmpty()) GamesUiState.Success(it, isOffline = true) else GamesUiState.Error(state.message) } ?: GamesUiState.Error(state.message)
                }
            }
        }
    }
}
