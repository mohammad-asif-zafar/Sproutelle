package com.hathway.sproutelle.presentation.numberquest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.repository.LearningRepository
import com.hathway.sproutelle.domain.repository.LearningState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NumberChoice(
    val id: String,
    val number: Int,
    val colorHex: Long,
    val isCorrect: Boolean
)

data class NumberQuestion(
    val id: String,
    val promptText: String,
    val targetCount: Int,
    val itemEmoji: String,
    val itemLabel: String = "",
    val imageUrl: String? = null,
    val choices: List<NumberChoice>
)


sealed interface NumberQuestUiState {
    data object Loading : NumberQuestUiState
    data class Success(
        val questions: List<NumberQuestion>,
        val currentQuestionIndex: Int = 0,
        val selectedChoiceId: String? = null,
        val selectedNumber: Int? = null,
        val isOptionSubmitted: Boolean = false,
        val isCorrect: Boolean? = null,
        val completedQuestionIndices: Set<Int> = emptySet(),
        val difficulty: String = "Level 2",
        val isAudioMuted: Boolean = false,
        val isOffline: Boolean = false
    ) : NumberQuestUiState

    data class Error(val message: String) : NumberQuestUiState
    data object Empty : NumberQuestUiState
}

class NumberQuestViewModel(
    val explorerId: String = "",
    val subcategoryId: String = "",
    val gameId: String = "number_quest",
    val gameName: String = "Number Quest",
    private val learningRepository: LearningRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<NumberQuestUiState>(NumberQuestUiState.Loading)
    val uiState: StateFlow<NumberQuestUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = NumberQuestUiState.Loading
            try {
                learningRepository?.refreshQuestions(subcategoryId, gameId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
            loadQuestions()
        }
    }

    fun loadQuestions() {
        val mockData = getLocalMockNumberQuestions()
        if (learningRepository == null) {
            setupQuestionState(mockData, 0, emptySet(), false)
            return
        }

        viewModelScope.launch {
            learningRepository.getQuestionsState(subcategoryId, gameId).collect { state ->
                val questions = when (state) {
                    is LearningState.Loading -> mockData
                    is LearningState.Cached -> if (state.data.isEmpty()) mockData else mapToNumberQuestions(state.data)
                    is LearningState.Fresh -> if (state.data.isEmpty()) mockData else mapToNumberQuestions(state.data)
                    is LearningState.Offline -> if (state.data.isEmpty()) mockData else mapToNumberQuestions(state.data)
                    is LearningState.Error -> state.cachedData?.let { if (it.isNotEmpty()) mapToNumberQuestions(it) else mockData } ?: mockData
                }
                setupQuestionState(questions, 0, emptySet(), state is LearningState.Offline)
            }
        }
    }

    private fun setupQuestionState(
        questions: List<NumberQuestion>,
        index: Int,
        completed: Set<Int>,
        isOffline: Boolean
    ) {
        val current = questions.getOrNull(index) ?: return

        _uiState.value = NumberQuestUiState.Success(
            questions = questions,
            currentQuestionIndex = index,
            selectedChoiceId = null,
            selectedNumber = null,
            isOptionSubmitted = false,
            isCorrect = null,
            completedQuestionIndices = completed,
            isOffline = isOffline,
            difficulty = "Level ${index + 1}"
        )
    }

    fun selectChoice(choiceId: String) {
        val current = _uiState.value as? NumberQuestUiState.Success ?: return
        val currentQuestion = current.questions.getOrNull(current.currentQuestionIndex) ?: return
        val choice = currentQuestion.choices.find { it.id == choiceId } ?: return

        val isCorrect = choice.isCorrect
        val updatedCompleted = if (isCorrect) {
            current.completedQuestionIndices + current.currentQuestionIndex
        } else {
            current.completedQuestionIndices
        }

        _uiState.value = current.copy(
            selectedChoiceId = choiceId,
            selectedNumber = choice.number,
            isOptionSubmitted = true,
            isCorrect = isCorrect,
            completedQuestionIndices = updatedCompleted
        )

        if (!isCorrect) {
            viewModelScope.launch {
                delay(1800)
                val stateNow = _uiState.value as? NumberQuestUiState.Success
                if (stateNow != null && stateNow.selectedChoiceId == choiceId && stateNow.isCorrect == false) {
                    _uiState.value = stateNow.copy(
                        selectedChoiceId = null,
                        selectedNumber = null,
                        isOptionSubmitted = false,
                        isCorrect = null
                    )
                }
            }
        }
    }

    fun nextQuestion() {
        val current = _uiState.value as? NumberQuestUiState.Success ?: return
        val nextIndex = (current.currentQuestionIndex + 1) % current.questions.size
        setupQuestionState(current.questions, nextIndex, current.completedQuestionIndices, current.isOffline)
    }

    fun toggleAudioMute() {
        val current = _uiState.value as? NumberQuestUiState.Success ?: return
        _uiState.value = current.copy(isAudioMuted = !current.isAudioMuted)
    }

    private fun mapToNumberQuestions(rawQuestions: List<Question>): List<NumberQuestion> {
        return rawQuestions.mapIndexed { idx, q ->
            val count = (q.options.find { it.isCorrect }?.text?.toIntOrNull() ?: (idx + 1)).coerceIn(1, 8)
            val emoji = getItemEmojiForWord(q.text)
            val choices = generateChoices(q.id, count)
            NumberQuestion(
                id = q.id,
                promptText = "Count the ${q.text.lowercase()}?",
                targetCount = count,
                itemEmoji = emoji,
                imageUrl = q.imageUrl,
                choices = choices
            )
        }
    }

    private fun generateChoices(questionId: String, targetCount: Int): List<NumberChoice> {
        val numbers = mutableSetOf(targetCount)
        val candidatePool = (1..9).filter { it != targetCount }.shuffled()
        numbers.addAll(candidatePool.take(3))

        val tileColors = listOf(
            0xFFAB47BC, // Vibrant Purple [ 4 ]
            0xFFFF9800, // Vibrant Orange [ 3 ]
            0xFF4CAF50, // Vibrant Green  [ 2 ]
            0xFFFFCA28  // Vibrant Yellow [ 1 ]
        )

        return numbers.toList().shuffled().mapIndexed { i, num ->
            NumberChoice(
                id = "${questionId}_num_$num",
                number = num,
                colorHex = tileColors[i % tileColors.size],
                isCorrect = num == targetCount
            )
        }
    }

    private fun getItemEmojiForWord(word: String): String {
        val clean = word.lowercase()
        return when {
            clean.contains("lion") -> "🦁"
            clean.contains("apple") -> "🍎"
            clean.contains("star") -> "⭐"
            clean.contains("pumpkin") -> "🎃"
            clean.contains("bear") -> "🐻"
            clean.contains("cat") -> "🐱"
            clean.contains("dog") -> "🐶"
            clean.contains("pear") -> "🍐"
            clean.contains("orange") -> "🍊"
            else -> "🦁"
        }
    }

    private fun getLocalMockNumberQuestions(): List<NumberQuestion> {
        return listOf(
            NumberQuestion(
                id = "nq_q1",
                promptText = "Count the lion?",
                targetCount = 3,
                itemEmoji = "🦁",
                choices = listOf(
                    NumberChoice("c1", 4, 0xFFAB47BC, false),
                    NumberChoice("c2", 3, 0xFFFF9800, true),
                    NumberChoice("c3", 2, 0xFF4CAF50, false),
                    NumberChoice("c4", 1, 0xFFFFCA28, false)
                )
            ),
            NumberQuestion(
                id = "nq_q2",
                promptText = "Count the apples?",
                targetCount = 4,
                itemEmoji = "🍎",
                choices = listOf(
                    NumberChoice("c2_1", 4, 0xFFAB47BC, true),
                    NumberChoice("c2_2", 2, 0xFFFF9800, false),
                    NumberChoice("c2_3", 5, 0xFF4CAF50, false),
                    NumberChoice("c2_4", 3, 0xFFFFCA28, false)
                )
            ),
            NumberQuestion(
                id = "nq_q3",
                promptText = "Count the stars?",
                targetCount = 2,
                itemEmoji = "⭐",
                choices = listOf(
                    NumberChoice("c3_1", 1, 0xFFAB47BC, false),
                    NumberChoice("c3_2", 2, 0xFF4CAF50, true),
                    NumberChoice("c3_3", 3, 0xFFFF9800, false),
                    NumberChoice("c3_4", 4, 0xFFFFCA28, false)
                )
            )
        )
    }
}
