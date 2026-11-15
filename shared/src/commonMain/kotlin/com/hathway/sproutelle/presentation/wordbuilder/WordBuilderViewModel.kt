package com.hathway.sproutelle.presentation.wordbuilder

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

/**
 * Represents a single choice letter tile in the scrambled pool.
 */
data class WordLetterTile(
    val id: String,
    val letter: Char,
    val isUsed: Boolean = false,
    val tileColorHex: Long = 0xFFFFFFFF
)

/**
 * Represents a word builder question item.
 */
data class WordQuestion(
    val id: String,
    val targetWord: String,
    val promptText: String,
    val categoryEmoji: String = "🦊",
    val imageUrl: String? = null,
    val initialTiles: List<WordLetterTile>
)

sealed interface WordBuilderUiState {
    data object Loading : WordBuilderUiState
    data class Success(
        val questions: List<WordQuestion>,
        val currentQuestionIndex: Int = 0,
        val targetWord: String = "",
        val placedLetters: List<Char?> = emptyList(),
        val availableTiles: List<WordLetterTile> = emptyList(),
        val isOptionSubmitted: Boolean = false,
        val isCorrect: Boolean? = null,
        val completedQuestionIndices: Set<Int> = emptySet(),
        val isAudioMuted: Boolean = false,
        val isOffline: Boolean = false,
        val difficulty: String = "Level 3"
    ) : WordBuilderUiState

    data class Error(val message: String) : WordBuilderUiState
    data object Empty : WordBuilderUiState
}

class WordBuilderViewModel(
    val explorerId: String = "",
    val subcategoryId: String = "",
    val gameId: String = "word_builder",
    val gameName: String = "Word Builder",
    private val learningRepository: LearningRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<WordBuilderUiState>(WordBuilderUiState.Loading)
    val uiState: StateFlow<WordBuilderUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = WordBuilderUiState.Loading
            try {
                learningRepository?.refreshQuestions(subcategoryId, gameId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
            loadQuestions()
        }
    }

    fun loadQuestions() {
        val mockData = getLocalMockWordQuestions()
        if (learningRepository == null) {
            setupQuestionState(mockData, 0, emptySet(), false)
            return
        }

        viewModelScope.launch {
            learningRepository.getQuestionsState(subcategoryId, gameId).collect { state ->
                val questions = when (state) {
                    is LearningState.Loading -> mockData
                    is LearningState.Cached -> if (state.data.isEmpty()) mockData else mapToWordQuestions(state.data)
                    is LearningState.Fresh -> if (state.data.isEmpty()) mockData else mapToWordQuestions(state.data)
                    is LearningState.Offline -> if (state.data.isEmpty()) mockData else mapToWordQuestions(state.data)
                    is LearningState.Error -> state.cachedData?.let { if (it.isNotEmpty()) mapToWordQuestions(it) else mockData } ?: mockData
                }
                setupQuestionState(questions, 0, emptySet(), state is LearningState.Offline)
            }
        }
    }

    private fun setupQuestionState(
        questions: List<WordQuestion>,
        index: Int,
        completed: Set<Int>,
        isOffline: Boolean
    ) {
        val current = questions.getOrNull(index) ?: return
        val emptySlots = List(current.targetWord.length) { null as Char? }

        _uiState.value = WordBuilderUiState.Success(
            questions = questions,
            currentQuestionIndex = index,
            targetWord = current.targetWord,
            placedLetters = emptySlots,
            availableTiles = current.initialTiles,
            isOptionSubmitted = false,
            isCorrect = null,
            completedQuestionIndices = completed,
            isOffline = isOffline,
            difficulty = "Level ${index + 1}"
        )
    }

    fun selectTile(tileId: String) {
        val current = _uiState.value as? WordBuilderUiState.Success ?: return
        if (current.isOptionSubmitted && current.isCorrect == true) return // Lock when correct

        val tileIndex = current.availableTiles.indexOfFirst { it.id == tileId }
        if (tileIndex == -1) return
        val tile = current.availableTiles[tileIndex]
        if (tile.isUsed) return // Tile already placed in a slot

        // Find first empty target slot
        val emptySlotIndex = current.placedLetters.indexOfFirst { it == null }
        if (emptySlotIndex == -1) return // All slots filled

        val updatedSlots = current.placedLetters.toMutableList()
        updatedSlots[emptySlotIndex] = tile.letter

        val updatedTiles = current.availableTiles.toMutableList()
        updatedTiles[tileIndex] = tile.copy(isUsed = true)

        val isAllFilled = updatedSlots.none { it == null }
        var isCorrect: Boolean? = null
        var updatedCompleted = current.completedQuestionIndices

        if (isAllFilled) {
            val wordSpelled = updatedSlots.joinToString("")
            isCorrect = wordSpelled.equals(current.targetWord, ignoreCase = true)
            if (isCorrect) {
                updatedCompleted = current.completedQuestionIndices + current.currentQuestionIndex
            } else {
                // Auto-clear incorrect attempts after delay
                viewModelScope.launch {
                    delay(1800)
                    val stateNow = _uiState.value as? WordBuilderUiState.Success
                    if (stateNow != null && stateNow.isCorrect == false) {
                        resetCurrentQuestionSlots(stateNow)
                    }
                }
            }
        }

        _uiState.value = current.copy(
            placedLetters = updatedSlots,
            availableTiles = updatedTiles,
            isOptionSubmitted = isAllFilled,
            isCorrect = isCorrect,
            completedQuestionIndices = updatedCompleted
        )
    }

    fun removeSlotLetter(slotIndex: Int) {
        val current = _uiState.value as? WordBuilderUiState.Success ?: return
        if (current.isOptionSubmitted && current.isCorrect == true) return

        val letterToRemove = current.placedLetters.getOrNull(slotIndex) ?: return

        // Find matching used tile and release it
        val tileIndexToRelease = current.availableTiles.indexOfFirst { it.isUsed && it.letter == letterToRemove }
        if (tileIndexToRelease == -1) return

        val updatedSlots = current.placedLetters.toMutableList()
        updatedSlots[slotIndex] = null

        val updatedTiles = current.availableTiles.toMutableList()
        updatedTiles[tileIndexToRelease] = updatedTiles[tileIndexToRelease].copy(isUsed = false)

        _uiState.value = current.copy(
            placedLetters = updatedSlots,
            availableTiles = updatedTiles,
            isOptionSubmitted = false,
            isCorrect = null
        )
    }

    private fun resetCurrentQuestionSlots(state: WordBuilderUiState.Success) {
        val current = state.questions.getOrNull(state.currentQuestionIndex) ?: return
        val emptySlots = List(current.targetWord.length) { null as Char? }
        val resetTiles = current.initialTiles.map { it.copy(isUsed = false) }

        _uiState.value = state.copy(
            placedLetters = emptySlots,
            availableTiles = resetTiles,
            isOptionSubmitted = false,
            isCorrect = null
        )
    }

    fun nextQuestion() {
        val current = _uiState.value as? WordBuilderUiState.Success ?: return
        val nextIndex = (current.currentQuestionIndex + 1) % current.questions.size
        setupQuestionState(current.questions, nextIndex, current.completedQuestionIndices, current.isOffline)
    }

    fun toggleAudioMute() {
        val current = _uiState.value as? WordBuilderUiState.Success ?: return
        _uiState.value = current.copy(isAudioMuted = !current.isAudioMuted)
    }

    private fun mapToWordQuestions(rawQuestions: List<Question>): List<WordQuestion> {
        return rawQuestions.mapIndexed { idx, q ->
            val word = q.text.uppercase().filter { it.isLetter() }.ifBlank { "FOX" }
            val prompt = q.instructions ?: "What do we call this Animal?"
            val categoryEmoji = getCategoryEmoji(word)
            val tiles = generateScrambledTiles(q.id, word)
            WordQuestion(
                id = q.id,
                targetWord = word,
                promptText = prompt,
                categoryEmoji = categoryEmoji,
                imageUrl = q.imageUrl,
                initialTiles = tiles
            )
        }
    }

    private fun generateScrambledTiles(questionId: String, word: String): List<WordLetterTile> {
        val letters = word.toList().toMutableList()
        val distractorPool = listOf('A', 'E', 'I', 'O', 'U', 'B', 'C', 'D', 'F', 'G', 'L', 'M', 'N', 'P', 'R', 'S', 'T', 'W', 'X', 'Z')
        val neededExtra = (7 - letters.size).coerceAtLeast(2)
        val extras = distractorPool.shuffled().take(neededExtra)
        letters.addAll(extras)

        val colors = listOf(
            0xFFFFFFFF, // White
            0xFFFF5252, // Vibrant Red (matching 'N' tile in design)
            0xFF4CAF50, // Vibrant Green (matching 'F'/'O' tiles)
            0xFFFFFFFF, // White
            0xFFE0F7FA, // Soft Cyan
            0xFFFFFFFF  // White
        )

        return letters.shuffled().mapIndexed { i, char ->
            WordLetterTile(
                id = "${questionId}_tile_$i",
                letter = char,
                isUsed = false,
                tileColorHex = colors[i % colors.size]
            )
        }
    }

    private fun getLocalMockWordQuestions(): List<WordQuestion> {
        return listOf(
            WordQuestion(
                id = "wb_q1",
                targetWord = "FOX",
                promptText = "What do we call this Animal?",
                categoryEmoji = "🦊",
                initialTiles = listOf(
                    WordLetterTile("t1", 'S', false, 0xFFFFFFFF),
                    WordLetterTile("t2", 'N', false, 0xFFFF5252),
                    WordLetterTile("t3", 'F', false, 0xFF4CAF50),
                    WordLetterTile("t4", 'A', false, 0xFFFFFFFF),
                    WordLetterTile("t5", 'O', false, 0xFF4CAF50),
                    WordLetterTile("t6", 'X', false, 0xFFFFFFFF),
                    WordLetterTile("t7", 'Z', false, 0xFFFFFFFF)
                )
            ),
            WordQuestion(
                id = "wb_q2",
                targetWord = "CAT",
                promptText = "What do we call this Pet?",
                categoryEmoji = "🐱",
                initialTiles = listOf(
                    WordLetterTile("wb2_1", 'C', false, 0xFF4CAF50),
                    WordLetterTile("wb2_2", 'R', false, 0xFFFFFFFF),
                    WordLetterTile("wb2_3", 'A', false, 0xFFFFFFFF),
                    WordLetterTile("wb2_4", 'M', false, 0xFFFF5252),
                    WordLetterTile("wb2_5", 'T', false, 0xFF4CAF50),
                    WordLetterTile("wb2_6", 'P', false, 0xFFFFFFFF),
                    WordLetterTile("wb2_7", 'S', false, 0xFFFFFFFF)
                )
            ),
            WordQuestion(
                id = "wb_q3",
                targetWord = "LION",
                promptText = "What do we call this Wild Animal?",
                categoryEmoji = "🦁",
                initialTiles = listOf(
                    WordLetterTile("wb3_1", 'L', false, 0xFF4CAF50),
                    WordLetterTile("wb3_2", 'I', false, 0xFFFFFFFF),
                    WordLetterTile("wb3_3", 'O', false, 0xFF4CAF50),
                    WordLetterTile("wb3_4", 'N', false, 0xFFFF5252),
                    WordLetterTile("wb3_5", 'E', false, 0xFFFFFFFF),
                    WordLetterTile("wb3_6", 'B', false, 0xFFFFFFFF),
                    WordLetterTile("wb3_7", 'T', false, 0xFFFFFFFF)
                )
            ),
            WordQuestion(
                id = "wb_q4",
                targetWord = "BEAR",
                promptText = "What do we call this Animal?",
                categoryEmoji = "🐻",
                initialTiles = listOf(
                    WordLetterTile("wb4_1", 'B', false, 0xFFFFFFFF),
                    WordLetterTile("wb4_2", 'E', false, 0xFF4CAF50),
                    WordLetterTile("wb4_3", 'A', false, 0xFFFFFFFF),
                    WordLetterTile("wb4_4", 'R', false, 0xFFFF5252),
                    WordLetterTile("wb4_5", 'K', false, 0xFFFFFFFF),
                    WordLetterTile("wb4_6", 'S', false, 0xFFFFFFFF)
                )
            ),
            WordQuestion(
                id = "wb_q5",
                targetWord = "STAR",
                promptText = "Look closely and spell the Shape!",
                categoryEmoji = "⭐",
                initialTiles = listOf(
                    WordLetterTile("wb5_1", 'S', false, 0xFF4CAF50),
                    WordLetterTile("wb5_2", 'T', false, 0xFFFFFFFF),
                    WordLetterTile("wb5_3", 'A', false, 0xFFFF5252),
                    WordLetterTile("wb5_4", 'R', false, 0xFFFFFFFF),
                    WordLetterTile("wb5_5", 'O', false, 0xFFFFFFFF),
                    WordLetterTile("wb5_6", 'N', false, 0xFFFFFFFF)
                )
            )
        )
    }

    private fun getCategoryEmoji(word: String): String {
        val clean = word.lowercase()
        return when {
            clean.contains("fox") -> "🦊"
            clean.contains("cat") -> "🐱"
            clean.contains("dog") -> "🐶"
            clean.contains("lion") -> "🦁"
            clean.contains("bear") -> "🐻"
            clean.contains("star") -> "⭐"
            clean.contains("apple") -> "🍎"
            clean.contains("banana") -> "🍌"
            else -> "✨"
        }
    }
}
