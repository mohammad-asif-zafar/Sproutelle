package com.hathway.sproutelle.presentation.matchanddiscover

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

data class MatchPair(
    val id: String,
    val word: String,
    val emoji: String,
    val imageUrl: String? = null
)

// Inside MatchAndDiscoverViewModel.kt:

data class WordNode(
    val id: String,
    val pairId: String,
    val word: String,
    val isSelected: Boolean = false,
    val isMatched: Boolean = false,
    val matchedPictureId: String? = null,
    val isCorrect: Boolean? = null
)

data class PictureNode(
    val id: String,
    val pairId: String,
    val emoji: String,
    val imageUrl: String? = null,
    val isSelected: Boolean = false,
    val isMatched: Boolean = false,
    val matchedWordId: String? = null,
    val isCorrect: Boolean? = null
)


data class MatchQuestion(
    val id: String,
    val titlePrompt: String = "Match the words with pictures.",
    val pairs: List<MatchPair>
)

sealed interface MatchAndDiscoverUiState {
    data object Loading : MatchAndDiscoverUiState
    data class Success(
        val questions: List<MatchQuestion>,
        val currentQuestionIndex: Int = 0,
        val wordNodes: List<WordNode> = emptyList(),
        val pictureNodes: List<PictureNode> = emptyList(),
        val selectedWordId: String? = null,
        val selectedPictureId: String? = null,
        val matchedPairs: Map<String, String> = emptyMap(), // wordId -> pictureId
        val pairResults: Map<String, Boolean> = emptyMap(), // wordId -> isCorrect
        val completedQuestionIndices: Set<Int> = emptySet(),
        val difficulty: String = "Level 2",
        val stars: Int = 3,
        val isAllMatched: Boolean = false,
        val isAllCorrect: Boolean = false,
        val isAudioMuted: Boolean = false,
        val isOffline: Boolean = false
    ) : MatchAndDiscoverUiState

    data class Error(val message: String) : MatchAndDiscoverUiState
    data object Empty : MatchAndDiscoverUiState
}

class MatchAndDiscoverViewModel(
    val explorerId: String = "",
    val subcategoryId: String = "",
    val gameId: String = "match_and_discover",
    val gameName: String = "Match & Discover",
    private val learningRepository: LearningRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<MatchAndDiscoverUiState>(MatchAndDiscoverUiState.Loading)
    val uiState: StateFlow<MatchAndDiscoverUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = MatchAndDiscoverUiState.Loading
            try {
                learningRepository?.refreshQuestions(subcategoryId, gameId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
            loadQuestions()
        }
    }

    fun loadQuestions() {
        val mockData = getLocalMockMatchQuestions()
        if (learningRepository == null) {
            setupQuestionState(mockData, 0, emptySet(), false)
            return
        }

        viewModelScope.launch {
            learningRepository.getQuestionsState(subcategoryId, gameId).collect { state ->
                val questions = when (state) {
                    is LearningState.Loading -> mockData
                    is LearningState.Cached -> if (state.data.isEmpty()) mockData else mapToMatchQuestions(state.data)
                    is LearningState.Fresh -> if (state.data.isEmpty()) mockData else mapToMatchQuestions(state.data)
                    is LearningState.Offline -> if (state.data.isEmpty()) mockData else mapToMatchQuestions(state.data)
                    is LearningState.Error -> state.cachedData?.let { if (it.isNotEmpty()) mapToMatchQuestions(it) else mockData } ?: mockData
                }
                setupQuestionState(questions, 0, emptySet(), state is LearningState.Offline)
            }
        }
    }

    private fun setupQuestionState(
        questions: List<MatchQuestion>,
        index: Int,
        completed: Set<Int>,
        isOffline: Boolean
    ) {
        val current = questions.getOrNull(index) ?: return
        val words = current.pairs.map {
            WordNode(id = "w_${it.id}", pairId = it.id, word = it.word)
        }.shuffled()
        val pictures = current.pairs.map {
            PictureNode(id = "p_${it.id}", pairId = it.id, emoji = it.emoji, imageUrl = it.imageUrl)
        }.shuffled()

        _uiState.value = MatchAndDiscoverUiState.Success(
            questions = questions,
            currentQuestionIndex = index,
            wordNodes = words,
            pictureNodes = pictures,
            selectedWordId = null,
            selectedPictureId = null,
            matchedPairs = emptyMap(),
            pairResults = emptyMap(),
            completedQuestionIndices = completed,
            difficulty = when (index % 3) {
                0 -> "Easy"
                1 -> "Medium"
                else -> "Hard"
            },
            stars = 3,
            isAllMatched = false,
            isAllCorrect = false,
            isOffline = isOffline
        )
    }

    private fun checkAndCreateMatch() {
        val current = _uiState.value as? MatchAndDiscoverUiState.Success ?: return
        val wordId = current.selectedWordId ?: return
        val pictureId = current.selectedPictureId ?: return

        val wordNode = current.wordNodes.find { it.id == wordId } ?: return
        val pictureNode = current.pictureNodes.find { it.id == pictureId } ?: return

        // Match strictly by pairId so shuffling never affects correctness
        val isCorrect = wordNode.pairId == pictureNode.pairId

        val cleanedPairs = current.matchedPairs
            .filterKeys { it != wordId }
            .filterValues { it != pictureId }
        val updatedMatchedPairs = cleanedPairs + (wordId to pictureId)

        val cleanedResults = current.pairResults.filterKeys { it != wordId }
        val updatedPairResults = cleanedResults + (wordId to isCorrect)

        val updatedWords = current.wordNodes.map { node ->
            when {
                node.id == wordId -> node.copy(
                    isSelected = false,
                    isMatched = true,
                    matchedPictureId = pictureId,
                    isCorrect = isCorrect
                )
                node.matchedPictureId == pictureId -> node.copy(
                    isSelected = false,
                    isMatched = false,
                    matchedPictureId = null,
                    isCorrect = null
                )
                else -> node
            }
        }

        val updatedPictures = current.pictureNodes.map { node ->
            when {
                node.id == pictureId -> node.copy(
                    isSelected = false,
                    isMatched = true,
                    matchedWordId = wordId,
                    isCorrect = isCorrect
                )
                node.matchedWordId == wordId -> node.copy(
                    isSelected = false,
                    isMatched = false,
                    matchedWordId = null,
                    isCorrect = null
                )
                else -> node
            }
        }

        val totalPairs = current.questions[current.currentQuestionIndex].pairs.size
        val allMatched = updatedMatchedPairs.size >= totalPairs
        val allCorrect = allMatched && updatedPairResults.values.all { it }

        val updatedCompleted = if (allCorrect) {
            current.completedQuestionIndices + current.currentQuestionIndex
        } else {
            current.completedQuestionIndices
        }

        _uiState.value = current.copy(
            selectedWordId = null,
            selectedPictureId = null,
            matchedPairs = updatedMatchedPairs,
            pairResults = updatedPairResults,
            wordNodes = updatedWords,
            pictureNodes = updatedPictures,
            isAllMatched = allMatched,
            isAllCorrect = allCorrect,
            completedQuestionIndices = updatedCompleted
        )

        if (!isCorrect) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(1200L)
                val latest = _uiState.value as? MatchAndDiscoverUiState.Success ?: return@launch
                if (latest.pairResults[wordId] == false) {
                    _uiState.value = latest.copy(
                        matchedPairs = latest.matchedPairs - wordId,
                        pairResults = latest.pairResults - wordId,
                        wordNodes = latest.wordNodes.map {
                            if (it.id == wordId) it.copy(isMatched = false, matchedPictureId = null, isCorrect = null) else it
                        },
                        pictureNodes = latest.pictureNodes.map {
                            if (it.id == pictureId) it.copy(isMatched = false, matchedWordId = null, isCorrect = null) else it
                        },
                        isAllMatched = false
                    )
                }
            }
        }
    }

    fun hint() {
        val current = _uiState.value as? MatchAndDiscoverUiState.Success ?: return
        if (current.isAllCorrect) return

        val unmatchedWord = current.wordNodes.firstOrNull { !it.isMatched } ?: return
        selectWord(unmatchedWord.id)
    }


    fun selectWord(wordId: String) {
        val current = _uiState.value as? MatchAndDiscoverUiState.Success ?: return
        if (current.isAllCorrect) return

        val newSelectedWord = if (current.selectedWordId == wordId) null else wordId
        val updatedWords = current.wordNodes.map { it.copy(isSelected = it.id == newSelectedWord) }

        _uiState.value = current.copy(
            selectedWordId = newSelectedWord,
            wordNodes = updatedWords
        )

        checkAndCreateMatch()
    }

    fun selectPicture(pictureId: String) {
        val current = _uiState.value as? MatchAndDiscoverUiState.Success ?: return
        if (current.isAllCorrect) return

        val newSelectedPicture = if (current.selectedPictureId == pictureId) null else pictureId
        val updatedPictures = current.pictureNodes.map { it.copy(isSelected = it.id == newSelectedPicture) }

        _uiState.value = current.copy(
            selectedPictureId = newSelectedPicture,
            pictureNodes = updatedPictures
        )

        checkAndCreateMatch()
    }



    fun shuffle() {
        val current = _uiState.value as? MatchAndDiscoverUiState.Success ?: return
        if (current.isAllCorrect) return

        _uiState.value = current.copy(
            wordNodes = current.wordNodes.shuffled(),
            pictureNodes = current.pictureNodes.shuffled()
        )
    }



    fun nextQuestion() {
        val current = _uiState.value as? MatchAndDiscoverUiState.Success ?: return
        val nextIndex = (current.currentQuestionIndex + 1) % current.questions.size
        setupQuestionState(current.questions, nextIndex, current.completedQuestionIndices, current.isOffline)
    }

    fun toggleAudioMute() {
        val current = _uiState.value as? MatchAndDiscoverUiState.Success ?: return
        _uiState.value = current.copy(isAudioMuted = !current.isAudioMuted)
    }

    private fun mapToMatchQuestions(rawQuestions: List<Question>): List<MatchQuestion> {
        val chunked = rawQuestions.chunked(4)
        return chunked.mapIndexed { idx, group ->
            val pairs = group.mapIndexed { pIdx, q ->
                val emoji = getEmojiForWord(q.text)
                MatchPair(
                    id = "pair_${idx}_$pIdx",
                    word = q.text.capitalizeWord(),
                    emoji = emoji,
                    imageUrl = q.imageUrl
                )
            }
            MatchQuestion(
                id = "match_q_$idx",
                titlePrompt = "Match the words with pictures.",
                pairs = pairs
            )
        }
    }

    private fun String.capitalizeWord(): String {
        return this.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    private fun getEmojiForWord(word: String): String {
        val clean = word.lowercase()
        return when {
            clean.contains("broccoli") -> "🥦"
            clean.contains("cucumber") -> "🥒"
            clean.contains("sweet potato") || clean.contains("potato") -> "🍠"
            clean.contains("orange") -> "🍊"
            clean.contains("mushroom") -> "🍄"
            clean.contains("chili") || clean.contains("pepper") -> "🌶️"
            clean.contains("pumpkin") -> "🎃"
            clean.contains("pear") -> "🍐"
            clean.contains("apple") -> "🍎"
            clean.contains("banana") -> "🍌"
            clean.contains("strawberry") -> "🍓"
            clean.contains("watermelon") -> "🍉"
            clean.contains("dog") -> "🐶"
            clean.contains("cat") -> "🐱"
            clean.contains("lion") -> "🦁"
            clean.contains("bear") -> "🐻"
            clean.contains("car") -> "🚗"
            clean.contains("plane") -> "✈️"
            else -> "🥗"
        }
    }

    private fun getLocalMockMatchQuestions(): List<MatchQuestion> {
        return listOf(
            MatchQuestion(
                id = "mq_1",
                titlePrompt = "Match the words with pictures.",
                pairs = listOf(
                    MatchPair("mp_1", "Broccoli", "🥦"),
                    MatchPair("mp_2", "Cucumber", "🥒"),
                    MatchPair("mp_3", "Sweet Potato", "🍠"),
                    MatchPair("mp_4", "Orange", "🍊")
                )
            ),
            MatchQuestion(
                id = "mq_2",
                titlePrompt = "Match the words with pictures.",
                pairs = listOf(
                    MatchPair("mp_5", "Apple", "🍎"),
                    MatchPair("mp_6", "Banana", "🍌"),
                    MatchPair("mp_7", "Strawberry", "🍓"),
                    MatchPair("mp_8", "Watermelon", "🍉")
                )
            ),
            MatchQuestion(
                id = "mq_3",
                titlePrompt = "Match the animals with pictures.",
                pairs = listOf(
                    MatchPair("mp_9", "Dog", "🐶"),
                    MatchPair("mp_10", "Cat", "🐱"),
                    MatchPair("mp_11", "Lion", "🦁"),
                    MatchPair("mp_12", "Bear", "🐻")
                )
            )
        )
    }
}
