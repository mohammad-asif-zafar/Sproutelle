package com.hathway.sproutelle.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hathway.sproutelle.domain.model.ExplorerItem
import com.hathway.sproutelle.domain.model.HomeData
import com.hathway.sproutelle.domain.usecase.GetExplorerItemsUseCase
import com.hathway.sproutelle.domain.usecase.GetHomeDataUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val data: HomeData) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val getHomeDataUseCase: GetHomeDataUseCase,
    private val getExplorerItemsUseCase: GetExplorerItemsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _explorerItems = MutableStateFlow<List<ExplorerItem>>(emptyList())
    val explorerItems: StateFlow<List<ExplorerItem>> = _explorerItems.asStateFlow()

    private val _explorerError = MutableStateFlow<String?>(null)
    val explorerError: StateFlow<String?> = _explorerError.asStateFlow()

    init {
        loadHomeData()
        loadExplorerItems()
    }

    fun refresh() {
        loadHomeData()
        loadExplorerItems()
    }

    fun refreshExplorer() {
        loadExplorerItems()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            if (_uiState.value !is HomeUiState.Success) {
                _uiState.value = HomeUiState.Loading
            }
            getHomeDataUseCase()
                .catch { exception ->
                    if (exception is CancellationException) throw exception
                    _uiState.value = HomeUiState.Error(exception.message ?: "Failed to load garden data")
                }
                .collect { homeData ->
                    _uiState.value = HomeUiState.Success(homeData)
                }
        }
    }

    fun loadExplorerItems() {
        viewModelScope.launch {
            _explorerError.value = null
            try {
                getExplorerItemsUseCase()
                    .catch { exception ->
                        if (exception is CancellationException) throw exception
                        _explorerError.value = exception.message ?: "Failed to load explore categories"
                    }
                    .collect { items ->
                        _explorerItems.value = items
                    }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _explorerError.value = e.message ?: "Failed to load explore categories"
            }
        }
    }
}
