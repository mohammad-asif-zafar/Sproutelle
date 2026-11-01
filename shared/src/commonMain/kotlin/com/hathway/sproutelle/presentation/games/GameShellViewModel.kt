package com.hathway.sproutelle.presentation.games

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GameShellUiState(
    val gameName: String,
    val subcategoryId: String,
    val gameId: String,
    val isAudioMuted: Boolean = false
)

class GameShellViewModel(
    val subcategoryId: String,
    val gameId: String,
    val initialGameName: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GameShellUiState(
            gameName = initialGameName,
            subcategoryId = subcategoryId,
            gameId = gameId
        )
    )
    val uiState: StateFlow<GameShellUiState> = _uiState.asStateFlow()

    fun toggleAudioMute() {
        _uiState.value = _uiState.value.copy(
            isAudioMuted = !_uiState.value.isAudioMuted
        )
    }
}
