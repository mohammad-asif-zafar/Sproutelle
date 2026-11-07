package com.hathway.sproutelle.presentation.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.repository.LearningRepository
import com.hathway.sproutelle.domain.repository.LearningState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface QuestionUiState {
    data object Loading : QuestionUiState
    data class Success(
        val questions: List<Question>,
        val currentQuestionIndex: Int = 0,
        val selectedOptionId: String? = null,
        val isOptionSubmitted: Boolean = false,
        val isCorrect: Boolean? = null,
        val completedQuestionIndices: Set<Int> = emptySet(),
        val isAudioMuted: Boolean = false,
        val isOffline: Boolean = false,
        val difficulty: String = "Hard"
    ) : QuestionUiState
    data class Error(val message: String) : QuestionUiState
    data object Empty : QuestionUiState
}

class QuestionViewModel(
    val explorerId: String,
    val subcategoryId: String,
    val gameId: String,
    val gameName: String,
    private val learningRepository: LearningRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuestionUiState>(QuestionUiState.Loading)
    val uiState: StateFlow<QuestionUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = QuestionUiState.Loading
            try {
                learningRepository.refreshQuestions(subcategoryId, gameId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    fun loadQuestions() {
        viewModelScope.launch {
            learningRepository.getQuestionsState(subcategoryId, gameId).collect { state ->
                _uiState.value = when (state) {
                    is LearningState.Loading -> QuestionUiState.Loading
                    is LearningState.Cached -> if (state.data.isEmpty()) QuestionUiState.Empty else QuestionUiState.Success(state.data, isOffline = false)
                    is LearningState.Fresh -> if (state.data.isEmpty()) QuestionUiState.Empty else QuestionUiState.Success(state.data, isOffline = false)
                    is LearningState.Offline -> if (state.data.isEmpty()) QuestionUiState.Empty else QuestionUiState.Success(state.data, isOffline = true)
                    is LearningState.Error -> state.cachedData?.let { if (it.isNotEmpty()) QuestionUiState.Success(it, isOffline = true) else QuestionUiState.Error(state.message) } ?: QuestionUiState.Error(state.message)
                }
            }
        }
    }

    fun toggleAudioMute() {
        val current = _uiState.value as? QuestionUiState.Success ?: return
        _uiState.value = current.copy(isAudioMuted = !current.isAudioMuted)
    }

    fun selectOption(optionId: String) {
        val current = _uiState.value as? QuestionUiState.Success ?: return
        val currentQuestion = current.questions.getOrNull(current.currentQuestionIndex) ?: return
        val option = currentQuestion.options.find { it.id == optionId }
        val isCorrect = option?.isCorrect ?: false

        val updatedCompleted = if (isCorrect) {
            current.completedQuestionIndices + current.currentQuestionIndex
        } else {
            current.completedQuestionIndices
        }

        _uiState.value = current.copy(
            selectedOptionId = optionId,
            isOptionSubmitted = true,
            isCorrect = isCorrect,
            completedQuestionIndices = updatedCompleted
        )
    }

    fun nextQuestion() {
        val current = _uiState.value as? QuestionUiState.Success ?: return
        val nextIndex = (current.currentQuestionIndex + 1) % current.questions.size
        _uiState.value = current.copy(
            currentQuestionIndex = nextIndex,
            selectedOptionId = null,
            isOptionSubmitted = false,
            isCorrect = null
        )
    }
}
