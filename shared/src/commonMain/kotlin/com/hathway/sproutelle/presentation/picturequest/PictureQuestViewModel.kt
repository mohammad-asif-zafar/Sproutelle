package com.hathway.sproutelle.presentation.picturequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hathway.sproutelle.domain.model.GameConfig
import com.hathway.sproutelle.domain.model.GameLevel
import com.hathway.sproutelle.domain.model.GameProgress
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.repository.LearningRepository
import com.hathway.sproutelle.domain.repository.LearningState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PictureQuestUiState {
    data object Loading : PictureQuestUiState
    data class Success(
        val questions: List<Question>,
        val gameConfig: GameConfig? = null,
        val levels: List<GameLevel> = emptyList(),
        val currentLevelIndex: Int = 0,
        val currentQuestionIndex: Int = 0,
        val selectedOptionId: String? = null,
        val isOptionSubmitted: Boolean = false,
        val isCorrect: Boolean? = null,
        val completedQuestionIndices: Set<Int> = emptySet(),
        val score: Int = 0,
        val attemptsCount: Int = 0,
        val showHint: Boolean = false,
        val isAudioMuted: Boolean = false,
        val isOffline: Boolean = false,
        val difficulty: String = "Medium",
        val isGameCompleted: Boolean = false
    ) : PictureQuestUiState
    data class Error(val message: String) : PictureQuestUiState
    data object Empty : PictureQuestUiState
}

class PictureQuestViewModel(
    val explorerId: String,
    val subcategoryId: String,
    val gameId: String,
    val gameName: String = "Picture Quest",
    private val learningRepository: LearningRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PictureQuestUiState>(PictureQuestUiState.Loading)
    val uiState: StateFlow<PictureQuestUiState> = _uiState.asStateFlow()

    private var currentGameConfig: GameConfig? = null
    private var currentGameLevels: List<GameLevel> = emptyList()
    private var allRawQuestions: List<Question> = emptyList()

    init {
        loadGameData()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = PictureQuestUiState.Loading
            try {
                learningRepository.refreshGameConfig(gameId)
                learningRepository.refreshGameLevels(gameId)
                learningRepository.refreshQuestions(subcategoryId, gameId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    private fun loadGameData() {
        viewModelScope.launch {
            // Load Game Config
            launch {
                learningRepository.getGameConfigState(gameId).collect { state ->
                    if (state is LearningState.Fresh) {
                        currentGameConfig = state.data
                    } else if (state is LearningState.Cached) {
                        currentGameConfig = state.data
                    }
                }
            }

            // Load Game Levels
            launch {
                learningRepository.getGameLevelsState(gameId).collect { state ->
                    if (state is LearningState.Fresh) {
                        currentGameLevels = state.data
                        processLoadedQuestions(allRawQuestions, isOffline = (_uiState.value as? PictureQuestUiState.Success)?.isOffline ?: false)
                    } else if (state is LearningState.Cached) {
                        currentGameLevels = state.data
                        processLoadedQuestions(allRawQuestions, isOffline = (_uiState.value as? PictureQuestUiState.Success)?.isOffline ?: false)
                    }
                }
            }

            // Load Questions from Repository
            learningRepository.getQuestionsState(subcategoryId, gameId).collect { state ->
                when (state) {
                    is LearningState.Loading -> {
                        _uiState.value = PictureQuestUiState.Loading
                    }
                    is LearningState.Cached -> {
                        processLoadedQuestions(state.data, isOffline = false)
                    }
                    is LearningState.Fresh -> {
                        processLoadedQuestions(state.data, isOffline = false)
                    }
                    is LearningState.Offline -> {
                        processLoadedQuestions(state.data, isOffline = true)
                    }
                    is LearningState.Error -> {
                        if (!state.cachedData.isNullOrEmpty()) {
                            processLoadedQuestions(state.cachedData, isOffline = true)
                        } else {
                            _uiState.value = PictureQuestUiState.Error(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun processLoadedQuestions(rawQuestions: List<Question>, isOffline: Boolean) {
        allRawQuestions = rawQuestions

        // If there are no levels AND no raw questions, emit PictureQuestUiState.Empty
        if (currentGameLevels.isEmpty() && rawQuestions.isEmpty()) {
            _uiState.value = PictureQuestUiState.Empty
            return
        }

        val config = currentGameConfig
        val currentSuccess = _uiState.value as? PictureQuestUiState.Success
        val selectedLevelIdx = currentSuccess?.currentLevelIndex ?: 0
        val currentLevel = currentGameLevels.getOrNull(selectedLevelIdx)

        // Strict level-based filtering:
        // If the game has defined levels, filter strictly by currentLevel.id.
        // Questions with level_id = null are NOT included in level-based games.
        val levelFiltered = if (currentGameLevels.isNotEmpty() && currentLevel != null) {
            rawQuestions.filter { it.levelId == currentLevel.id }
        } else {
            rawQuestions
        }

        var processedQuestions = levelFiltered

        // Apply randomization if backend config requires it
        if (config?.randomizeQuestionOrder == true && processedQuestions.isNotEmpty()) {
            processedQuestions = processedQuestions.shuffled()
        }

        if (config?.randomizeAnswerOrder == true && processedQuestions.isNotEmpty()) {
            processedQuestions = processedQuestions.map { question ->
                question.copy(options = question.options.shuffled())
            }
        }

        val difficulty = config?.difficulty ?: processedQuestions.firstOrNull()?.difficulty ?: "Medium"

        if (currentSuccess != null) {
            _uiState.value = currentSuccess.copy(
                questions = processedQuestions,
                gameConfig = config ?: currentSuccess.gameConfig,
                levels = currentGameLevels,
                currentLevelIndex = selectedLevelIdx,
                isOffline = isOffline,
                difficulty = difficulty
            )
        } else {
            _uiState.value = PictureQuestUiState.Success(
                questions = processedQuestions,
                gameConfig = config,
                levels = currentGameLevels,
                currentLevelIndex = selectedLevelIdx,
                isOffline = isOffline,
                difficulty = difficulty
            )
        }
    }

    fun toggleAudioMute() {
        val current = _uiState.value as? PictureQuestUiState.Success ?: return
        _uiState.value = current.copy(isAudioMuted = !current.isAudioMuted)
    }

    fun toggleHint() {
        val current = _uiState.value as? PictureQuestUiState.Success ?: return
        if (current.gameConfig?.enableHints == false) return
        _uiState.value = current.copy(showHint = !current.showHint)
    }

    fun selectOption(optionId: String) {
        val current = _uiState.value as? PictureQuestUiState.Success ?: return
        if (current.isOptionSubmitted && current.isCorrect == true) return

        val currentQuestion = current.questions.getOrNull(current.currentQuestionIndex) ?: return
        val option = currentQuestion.options.find { it.id == optionId }
        val isCorrect = option?.isCorrect ?: false

        val pointsToAdd = if (isCorrect) (current.gameConfig?.pointsPerCorrect ?: currentQuestion.points) else 0
        val updatedScore = if (isCorrect) current.score + pointsToAdd else current.score
        val updatedCompleted = if (isCorrect) current.completedQuestionIndices + current.currentQuestionIndex else current.completedQuestionIndices

        _uiState.value = current.copy(
            selectedOptionId = optionId,
            isOptionSubmitted = true,
            isCorrect = isCorrect,
            score = updatedScore,
            completedQuestionIndices = updatedCompleted,
            attemptsCount = current.attemptsCount + 1,
            showHint = false
        )

        // Track Game Progress
        viewModelScope.launch {
            val progress = GameProgress(
                childId = "current_child",
                gameId = gameId,
                levelId = current.levels.getOrNull(current.currentLevelIndex)?.id,
                questionsAttempted = current.attemptsCount + 1,
                correctAnswers = updatedCompleted.size,
                incorrectAnswers = (current.attemptsCount + 1) - updatedCompleted.size,
                score = updatedScore,
                completionStatus = if (updatedCompleted.size == current.questions.size) "completed" else "in_progress",
                bestScore = maxOf(updatedScore, current.score)
            )
            learningRepository.saveGameProgress(progress)
        }

        // Kid-safe interaction auto-clear for incorrect choices
        if (!isCorrect) {
            val submittedLevelIndex = current.currentLevelIndex
            val submittedQuestionIndex = current.currentQuestionIndex
            viewModelScope.launch {
                delay(2000)
                val stateNow = _uiState.value as? PictureQuestUiState.Success
                if (stateNow != null &&
                    stateNow.currentLevelIndex == submittedLevelIndex &&
                    stateNow.currentQuestionIndex == submittedQuestionIndex &&
                    stateNow.selectedOptionId == optionId &&
                    stateNow.isCorrect == false) {
                    _uiState.value = stateNow.copy(
                        selectedOptionId = null,
                        isOptionSubmitted = false,
                        isCorrect = null
                    )
                }
            }
        }
    }

    fun nextQuestion() {
        val current = _uiState.value as? PictureQuestUiState.Success ?: return
        if (current.currentQuestionIndex >= current.questions.lastIndex) {
            _uiState.value = current.copy(isGameCompleted = true)
            return
        }

        _uiState.value = current.copy(
            currentQuestionIndex = current.currentQuestionIndex + 1,
            selectedOptionId = null,
            isOptionSubmitted = false,
            isCorrect = null,
            showHint = false
        )
    }

    fun selectLevel(levelIndex: Int) {
        val current = _uiState.value as? PictureQuestUiState.Success ?: return
        if (levelIndex !in current.levels.indices) return
        val targetLevel = current.levels[levelIndex]

        val levelFiltered = if (current.levels.isNotEmpty()) {
            allRawQuestions.filter { it.levelId == targetLevel.id }
        } else {
            allRawQuestions
        }

        var processed = levelFiltered
        if (current.gameConfig?.randomizeQuestionOrder == true && processed.isNotEmpty()) {
            processed = processed.shuffled()
        }
        if (current.gameConfig?.randomizeAnswerOrder == true && processed.isNotEmpty()) {
            processed = processed.map { q -> q.copy(options = q.options.shuffled()) }
        }

        _uiState.value = current.copy(
            questions = processed,
            currentLevelIndex = levelIndex,
            currentQuestionIndex = 0,
            selectedOptionId = null,
            isOptionSubmitted = false,
            isCorrect = null,
            completedQuestionIndices = emptySet(),
            score = 0,
            attemptsCount = 0,
            showHint = false,
            isGameCompleted = false
        )
    }

    fun restartGame() {
        val current = _uiState.value as? PictureQuestUiState.Success ?: return
        val reshuffled = if (current.gameConfig?.randomizeQuestionOrder == true) {
            current.questions.shuffled()
        } else {
            current.questions
        }
        _uiState.value = current.copy(
            questions = reshuffled,
            currentQuestionIndex = 0,
            selectedOptionId = null,
            isOptionSubmitted = false,
            isCorrect = null,
            completedQuestionIndices = emptySet(),
            score = 0,
            attemptsCount = 0,
            showHint = false,
            isGameCompleted = false
        )
    }
}
