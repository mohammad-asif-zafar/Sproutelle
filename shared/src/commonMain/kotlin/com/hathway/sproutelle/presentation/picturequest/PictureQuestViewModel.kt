package com.hathway.sproutelle.presentation.picturequest

/*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.QuestionOption
import com.hathway.sproutelle.domain.repository.LearningRepository
import com.hathway.sproutelle.domain.repository.LearningState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PictureQuestUiState {
    data object Loading : PictureQuestUiState
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

    init {
        loadQuestions()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = PictureQuestUiState.Loading
            try {
                learningRepository.refreshQuestions(subcategoryId, gameId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    fun loadQuestions() {
        val mockData = getLocalMockPictureQuestQuestions()
        viewModelScope.launch {
            learningRepository.getQuestionsState(subcategoryId, gameId).collect { state ->
                _uiState.value = when (state) {
                    is LearningState.Loading -> PictureQuestUiState.Success(mockData, isOffline = false)
                    is LearningState.Cached -> if (state.data.isEmpty()) PictureQuestUiState.Success(mockData, isOffline = false) else PictureQuestUiState.Success(state.data, isOffline = false)
                    is LearningState.Fresh -> if (state.data.isEmpty()) PictureQuestUiState.Success(mockData, isOffline = false) else PictureQuestUiState.Success(state.data, isOffline = false)
                    is LearningState.Offline -> if (state.data.isEmpty()) PictureQuestUiState.Success(mockData, isOffline = true) else PictureQuestUiState.Success(state.data, isOffline = true)
                    is LearningState.Error -> state.cachedData?.let { if (it.isNotEmpty()) PictureQuestUiState.Success(it, isOffline = true) else PictureQuestUiState.Success(mockData, isOffline = true) } ?: PictureQuestUiState.Success(mockData, isOffline = false)
                }
            }
        }
    }

    fun toggleAudioMute() {
        val current = _uiState.value as? PictureQuestUiState.Success ?: return
        _uiState.value = current.copy(isAudioMuted = !current.isAudioMuted)
    }

    fun selectOption(optionId: String) {
        val current = _uiState.value as? PictureQuestUiState.Success ?: return
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
        val current = _uiState.value as? PictureQuestUiState.Success ?: return
        val nextIndex = (current.currentQuestionIndex + 1) % current.questions.size
        _uiState.value = current.copy(
            currentQuestionIndex = nextIndex,
            selectedOptionId = null,
            isOptionSubmitted = false,
            isCorrect = null
        )
    }

    private fun getLocalMockPictureQuestQuestions(): List<Question> {
        return listOf(
            Question(
                id = "pq_q1",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Mushroom",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 1,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o1_1", "pq_q1", "Chili", false, 1),
                    QuestionOption("pq_o1_2", "pq_q1", "Pumpkin", false, 2),
                    QuestionOption("pq_o1_3", "pq_q1", "Mushroom", true, 3),
                    QuestionOption("pq_o1_4", "pq_q1", "Pear", false, 4),
                    QuestionOption("pq_o1_5", "pq_q1", "Orange", false, 5),
                    QuestionOption("pq_o1_6", "pq_q1", "Mango", false, 6)
                )
            ),
            Question(
                id = "pq_q2",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Apple",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 2,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o2_1", "pq_q2", "Apple", true, 1),
                    QuestionOption("pq_o2_2", "pq_q2", "Banana", false, 2),
                    QuestionOption("pq_o2_3", "pq_q2", "Strawberry", false, 3),
                    QuestionOption("pq_o2_4", "pq_q2", "Watermelon", false, 4),
                    QuestionOption("pq_o2_5", "pq_q2", "Mango", false, 5),
                    QuestionOption("pq_o2_6", "pq_q2", "Pear", false, 6)
                )
            ),
            Question(
                id = "pq_q3",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Lion",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 3,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o3_1", "pq_q3", "Dog", false, 1),
                    QuestionOption("pq_o3_2", "pq_q3", "Cat", false, 2),
                    QuestionOption("pq_o3_3", "pq_q3", "Lion", true, 3),
                    QuestionOption("pq_o3_4", "pq_q3", "Bear", false, 4),
                    QuestionOption("pq_o3_5", "pq_q3", "Fox", false, 5),
                    QuestionOption("pq_o3_6", "pq_q3", "Elephant", false, 6)
                )
            ),
            Question(
                id = "pq_q4",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Car",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 4,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o4_1", "pq_q4", "Car", true, 1),
                    QuestionOption("pq_o4_2", "pq_q4", "Plane", false, 2),
                    QuestionOption("pq_o4_3", "pq_q4", "Boat", false, 3),
                    QuestionOption("pq_o4_4", "pq_q4", "Train", false, 4),
                    QuestionOption("pq_o4_5", "pq_q4", "Bus", false, 5),
                    QuestionOption("pq_o4_6", "pq_q4", "Rocket", false, 6)
                )
            ),
            Question(
                id = "pq_q5",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Star",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 5,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o5_1", "pq_q5", "Moon", false, 1),
                    QuestionOption("pq_o5_2", "pq_q5", "Star", true, 2),
                    QuestionOption("pq_o5_3", "pq_q5", "Sun", false, 3),
                    QuestionOption("pq_o5_4", "pq_q5", "Cloud", false, 4),
                    QuestionOption("pq_o5_5", "pq_q5", "Heart", false, 5),
                    QuestionOption("pq_o5_6", "pq_q5", "Rainbow", false, 6)
                )
            ),
            Question(
                id = "pq_q6",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Pumpkin",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 6,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o6_1", "pq_q6", "Pumpkin", true, 1),
                    QuestionOption("pq_o6_2", "pq_q6", "Mushroom", false, 2),
                    QuestionOption("pq_o6_3", "pq_q6", "Chili", false, 3),
                    QuestionOption("pq_o6_4", "pq_q6", "Pear", false, 4),
                    QuestionOption("pq_o6_5", "pq_q6", "Orange", false, 5),
                    QuestionOption("pq_o6_6", "pq_q6", "Mango", false, 6)
                )
            ),
            Question(
                id = "pq_q7",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Strawberry",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 7,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o7_1", "pq_q7", "Apple", false, 1),
                    QuestionOption("pq_o7_2", "pq_q7", "Banana", false, 2),
                    QuestionOption("pq_o7_3", "pq_q7", "Strawberry", true, 3),
                    QuestionOption("pq_o7_4", "pq_q7", "Watermelon", false, 4),
                    QuestionOption("pq_o7_5", "pq_q7", "Pear", false, 5),
                    QuestionOption("pq_o7_6", "pq_q7", "Mango", false, 6)
                )
            ),
            Question(
                id = "pq_q8",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Dog",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 8,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o8_1", "pq_q8", "Dog", true, 1),
                    QuestionOption("pq_o8_2", "pq_q8", "Cat", false, 2),
                    QuestionOption("pq_o8_3", "pq_q8", "Lion", false, 3),
                    QuestionOption("pq_o8_4", "pq_q8", "Bear", false, 4),
                    QuestionOption("pq_o8_5", "pq_q8", "Fox", false, 5),
                    QuestionOption("pq_o8_6", "pq_q8", "Elephant", false, 6)
                )
            ),
            Question(
                id = "pq_q9",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Plane",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 9,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o9_1", "pq_q9", "Car", false, 1),
                    QuestionOption("pq_o9_2", "pq_q9", "Plane", true, 2),
                    QuestionOption("pq_o9_3", "pq_q9", "Boat", false, 3),
                    QuestionOption("pq_o9_4", "pq_q9", "Train", false, 4),
                    QuestionOption("pq_o9_5", "pq_q9", "Bus", false, 5),
                    QuestionOption("pq_o9_6", "pq_q9", "Rocket", false, 6)
                )
            ),
            Question(
                id = "pq_q10",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Watermelon",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 10,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o10_1", "pq_q10", "Watermelon", true, 1),
                    QuestionOption("pq_o10_2", "pq_q10", "Apple", false, 2),
                    QuestionOption("pq_o10_3", "pq_q10", "Banana", false, 3),
                    QuestionOption("pq_o10_4", "pq_q10", "Strawberry", false, 4),
                    QuestionOption("pq_o10_5", "pq_q10", "Mango", false, 5),
                    QuestionOption("pq_o10_6", "pq_q10", "Pear", false, 6)
                )
            )
        )
    }
}
*/

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.QuestionOption
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
        val currentQuestionIndex: Int = 0,
        val selectedOptionId: String? = null,
        val isOptionSubmitted: Boolean = false,
        val isCorrect: Boolean? = null,
        val completedQuestionIndices: Set<Int> = emptySet(),
        val isAudioMuted: Boolean = false,
        val isOffline: Boolean = false,
        val difficulty: String = "Hard"
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

    init {
        loadQuestions()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = PictureQuestUiState.Loading
            try {
                learningRepository.refreshQuestions(subcategoryId, gameId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    fun loadQuestions() {
        val mockData = getLocalMockPictureQuestQuestions()
        viewModelScope.launch {
            learningRepository.getQuestionsState(subcategoryId, gameId).collect { state ->
                _uiState.value = when (state) {
                    is LearningState.Loading -> PictureQuestUiState.Success(mockData, isOffline = false)
                    is LearningState.Cached -> if (state.data.isEmpty()) PictureQuestUiState.Success(mockData, isOffline = false) else PictureQuestUiState.Success(state.data, isOffline = false)
                    is LearningState.Fresh -> if (state.data.isEmpty()) PictureQuestUiState.Success(mockData, isOffline = false) else PictureQuestUiState.Success(state.data, isOffline = false)
                    is LearningState.Offline -> if (state.data.isEmpty()) PictureQuestUiState.Success(mockData, isOffline = true) else PictureQuestUiState.Success(state.data, isOffline = true)
                    is LearningState.Error -> state.cachedData?.let { if (it.isNotEmpty()) PictureQuestUiState.Success(it, isOffline = true) else PictureQuestUiState.Success(mockData, isOffline = true) } ?: PictureQuestUiState.Success(mockData, isOffline = false)
                }
            }
        }
    }

    fun toggleAudioMute() {
        val current = _uiState.value as? PictureQuestUiState.Success ?: return
        _uiState.value = current.copy(isAudioMuted = !current.isAudioMuted)
    }

    fun selectOption(optionId: String) {
        val current = _uiState.value as? PictureQuestUiState.Success ?: return
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

        // Kid-safe interaction auto-clear for incorrect choices
        if (!isCorrect) {
            viewModelScope.launch {
                delay(2000) // Show "Try Again!" overlay context for 2 seconds
                val stateNow = _uiState.value as? PictureQuestUiState.Success
                if (stateNow != null && stateNow.selectedOptionId == optionId && stateNow.isCorrect == false) {
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
        val nextIndex = (current.currentQuestionIndex + 1) % current.questions.size
        _uiState.value = current.copy(
            currentQuestionIndex = nextIndex,
            selectedOptionId = null,
            isOptionSubmitted = false,
            isCorrect = null
        )
    }

    private fun getLocalMockPictureQuestQuestions(): List<Question> {
        return listOf(
            Question(
                id = "pq_q1",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Mushroom",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 1,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o1_1", "pq_q1", "Chili", false, 1),
                    QuestionOption("pq_o1_2", "pq_q1", "Pumpkin", false, 2),
                    QuestionOption("pq_o1_3", "pq_q1", "Mushroom", true, 3),
                    QuestionOption("pq_o1_4", "pq_q1", "Pear", false, 4),
                    QuestionOption("pq_o1_5", "pq_q1", "Orange", false, 5),
                    QuestionOption("pq_o1_6", "pq_q1", "Mango", false, 6)
                )
            ),
            Question(
                id = "pq_q2",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Apple",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 2,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o2_1", "pq_q2", "Apple", true, 1),
                    QuestionOption("pq_o2_2", "pq_q2", "Banana", false, 2),
                    QuestionOption("pq_o2_3", "pq_q2", "Strawberry", false, 3),
                    QuestionOption("pq_o2_4", "pq_q2", "Watermelon", false, 4),
                    QuestionOption("pq_o2_5", "pq_q2", "Mango", false, 5),
                    QuestionOption("pq_o2_6", "pq_q2", "Pear", false, 6)
                )
            ),
            Question(
                id = "pq_q3",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Lion",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 3,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o3_1", "pq_q3", "Dog", false, 1),
                    QuestionOption("pq_o3_2", "pq_q3", "Cat", false, 2),
                    QuestionOption("pq_o3_3", "pq_q3", "Lion", true, 3),
                    QuestionOption("pq_o3_4", "pq_q3", "Bear", false, 4),
                    QuestionOption("pq_o3_5", "pq_q3", "Fox", false, 5),
                    QuestionOption("pq_o3_6", "pq_q3", "Elephant", false, 6)
                )
            ),
            Question(
                id = "pq_q4",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Car",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 4,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o4_1", "pq_q4", "Car", true, 1),
                    QuestionOption("pq_o4_2", "pq_q4", "Plane", false, 2),
                    QuestionOption("pq_o4_3", "pq_q4", "Boat", false, 3),
                    QuestionOption("pq_o4_4", "pq_q4", "Train", false, 4),
                    QuestionOption("pq_o4_5", "pq_q4", "Bus", false, 5),
                    QuestionOption("pq_o4_6", "pq_q4", "Rocket", false, 6)
                )
            ),
            Question(
                id = "pq_q5",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Star",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 5,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o5_1", "pq_q5", "Moon", false, 1),
                    QuestionOption("pq_o5_2", "pq_q5", "Star", true, 2),
                    QuestionOption("pq_o5_3", "pq_q5", "Sun", false,  3),
                    QuestionOption("pq_o5_4", "pq_q5", "Cloud", false, 4),
                    QuestionOption("pq_o5_5", "pq_q5", "Heart", false, 5),
                    QuestionOption("pq_o5_6", "pq_q5", "Rainbow", false, 6)
                )
            ),
            Question(
                id = "pq_q6",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Pumpkin",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 6,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o6_1", "pq_q6", "Pumpkin", true, 1),
                    QuestionOption("pq_o6_2", "pq_q6", "Mushroom", false, 2),
                    QuestionOption("pq_o6_3", "pq_q6", "Chili", false, 3),
                    QuestionOption("pq_o6_4", "pq_q6", "Pear", false, 4),
                    QuestionOption("pq_o6_5", "pq_q6", "Orange", false, 5),
                    QuestionOption("pq_o6_6", "pq_q6", "Mango", false, 6)
                )
            ),
            Question(
                id = "pq_q7",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Strawberry",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 7,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o7_1", "pq_q7", "Apple", false, 1),
                    QuestionOption("pq_o7_2", "pq_q7", "Banana", false, 2),
                    QuestionOption("pq_o7_3", "pq_q7", "Strawberry", true, 3),
                    QuestionOption("pq_o7_4", "pq_q7", "Watermelon", false, 4),
                    QuestionOption("pq_o7_5", "pq_q7", "Pear", false, 5),
                    QuestionOption("pq_o7_6", "pq_q7", "Mango", false, 6)
                )
            ),
            Question(
                id = "pq_q8",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Dog",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 8,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o8_1", "pq_q8", "Dog", true, 1),
                    QuestionOption("pq_o8_2", "pq_q8", "Cat", false, 2),
                    QuestionOption("pq_o8_3", "pq_q8", "Lion", false, 3),
                    QuestionOption("pq_o8_4", "pq_q8", "Bear", false, 4),
                    QuestionOption("pq_o8_5", "pq_q8", "Fox", false, 5),
                    QuestionOption("pq_o8_6", "pq_q8", "Elephant", false, 6)
                )
            ),
            Question(
                id = "pq_q9",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Plane",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 9,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o9_1", "pq_q9", "Car", false, 1),
                    QuestionOption("pq_o9_2", "pq_q9", "Plane", true, 2),
                    QuestionOption("pq_o9_3", "pq_q9", "Boat", false, 3),
                    QuestionOption("pq_o9_4", "pq_q9", "Train", false, 4),
                    QuestionOption("pq_o9_5", "pq_q9", "Bus", false, 5),
                    QuestionOption("pq_o9_6", "pq_q9", "Rocket", false, 6)
                )
            ),
            Question(
                id = "pq_q10",
                gameId = "picture_quest",
                subcategoryId = "sub_1",
                text = "Watermelon",
                type = "image_selection",
                instructions = "Look closely. Find the right picture!",
                sortOrder = 10,
                isDraft = false,
                options = listOf(
                    QuestionOption("pq_o10_1", "pq_q10", "Watermelon", true, 1),
                    QuestionOption("pq_o10_2", "pq_q10", "Apple", false, 2),
                    QuestionOption("pq_o10_3", "pq_q10", "Banana", false, 3),
                    QuestionOption("pq_o10_4", "pq_q10", "Strawberry", false, 4),
                    QuestionOption("pq_o10_5", "pq_q10", "Mango", false, 5),
                    QuestionOption("pq_o10_6", "pq_q10", "Pear", false, 6)
                )
            )
        )
    }
}
