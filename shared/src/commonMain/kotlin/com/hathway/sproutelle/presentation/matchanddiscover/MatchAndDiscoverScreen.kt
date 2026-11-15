package com.hathway.sproutelle.presentation.matchanddiscover

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hathway.sproutelle.core.audio.rememberWordSpeaker
import com.hathway.sproutelle.core.responsive.ProvideWindowSizeClass
import com.hathway.sproutelle.presentation.components.EmptyState
import com.hathway.sproutelle.presentation.components.ErrorState
import com.hathway.sproutelle.presentation.components.LoadingState
import com.hathway.sproutelle.presentation.components.QuestHeader
import com.hathway.sproutelle.presentation.components.QuestProgressBar
import com.hathway.sproutelle.presentation.matchanddiscover.components.MatchBottomActionBar
import com.hathway.sproutelle.presentation.matchanddiscover.components.MatchFeedbackMascot
import com.hathway.sproutelle.presentation.matchanddiscover.components.MatchPromptCard
import com.hathway.sproutelle.presentation.matchanddiscover.components.MatchingBoard
import org.jetbrains.compose.resources.painterResource
import sproutelle.shared.generated.resources.Picture_Quest_bg_tablet
import sproutelle.shared.generated.resources.Res
import sproutelle.shared.generated.resources.match_and_discover_bg_mobile
import theme.SproutelleDimens

@Composable
fun MatchAndDiscoverScreen(
    viewModel: MatchAndDiscoverViewModel,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit = onBackClick
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val speakWord = rememberWordSpeaker()
    val isAudioMuted = (uiState as? MatchAndDiscoverUiState.Success)?.isAudioMuted ?: false

    ProvideWindowSizeClass { windowSize ->
        MatchAndDiscoverContent(
            uiState = uiState,
            isTablet = windowSize.isTablet,
            onRefresh = viewModel::refresh,
            onBackClick = onBackClick,
            onHomeClick = onHomeClick,
            onWordClick = viewModel::selectWord,
            onPictureClick = viewModel::selectPicture,
            onSpeakWord = { word ->
                if (!isAudioMuted) speakWord(word)
            },
            onShuffleClick = viewModel::shuffle,
            onHintClick = viewModel::hint,
            onNextQuestion = viewModel::nextQuestion
        )
    }
}

@Composable
fun MatchAndDiscoverContent(
    uiState: MatchAndDiscoverUiState,
    isTablet: Boolean,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onWordClick: (String) -> Unit,
    onPictureClick: (String) -> Unit,
    onSpeakWord: (String) -> Unit = {},
    onShuffleClick: () -> Unit,
    onHintClick: () -> Unit,
    onNextQuestion: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(), color = Color(0xFFE0F7FA)
    ) {
        when (uiState) {
            is MatchAndDiscoverUiState.Loading -> {
                LoadingState(message = "Loading Match & Discover...")
            }

            is MatchAndDiscoverUiState.Error -> {
                ErrorState(message = uiState.message, onRetryClick = onRefresh)
            }

            is MatchAndDiscoverUiState.Empty -> {
                EmptyState(
                    message = "No matching games available yet",
                    iconEmoji = "🧩",
                    actionLabel = "Refresh",
                    onActionClick = onRefresh
                )
            }

            is MatchAndDiscoverUiState.Success -> {
                val currentQuestion = uiState.questions.getOrNull(uiState.currentQuestionIndex)
                if (currentQuestion == null) {
                    EmptyState(
                        message = "No matching games available yet",
                        iconEmoji = "🧩",
                        actionLabel = "Refresh",
                        onActionClick = onRefresh
                    )
                } else {
                    MatchAndDiscoverAdaptiveLayout(
                        state = uiState,
                        currentQuestion = currentQuestion,
                        isTablet = isTablet,
                        onBackClick = onBackClick,
                        onHomeClick = onHomeClick,
                        onWordClick = onWordClick,
                        onPictureClick = onPictureClick,
                        onSpeakWord = onSpeakWord,
                        onShuffleClick = onShuffleClick,
                        onHintClick = onHintClick,
                        onNextQuestion = onNextQuestion
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchAndDiscoverAdaptiveLayout(
    state: MatchAndDiscoverUiState.Success,
    currentQuestion: MatchQuestion,
    isTablet: Boolean,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onWordClick: (String) -> Unit,
    onPictureClick: (String) -> Unit,
    onSpeakWord: (String) -> Unit,
    onShuffleClick: () -> Unit,
    onHintClick: () -> Unit,
    onNextQuestion: () -> Unit
) {
    val backgroundImage =
        if (isTablet) Res.drawable.Picture_Quest_bg_tablet else Res.drawable.match_and_discover_bg_mobile

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(backgroundImage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (isTablet) {
            TabletMatchAndDiscoverContent(
                state = state,
                currentQuestion = currentQuestion,
                onBackClick = onBackClick,
                onHomeClick = onHomeClick,
                onWordClick = onWordClick,
                onPictureClick = onPictureClick,
                onSpeakWord = onSpeakWord,
                onShuffleClick = onShuffleClick,
                onHintClick = onHintClick,
                onNextQuestion = onNextQuestion
            )
        } else {
            MobileMatchAndDiscoverContent(
                state = state,
                currentQuestion = currentQuestion,
                onBackClick = onBackClick,
                onHomeClick = onHomeClick,
                onWordClick = onWordClick,
                onPictureClick = onPictureClick,
                onSpeakWord = onSpeakWord,
                onShuffleClick = onShuffleClick,
                onHintClick = onHintClick,
                onNextQuestion = onNextQuestion
            )
        }
    }
}

@Composable
private fun MobileMatchAndDiscoverContent(
    state: MatchAndDiscoverUiState.Success,
    currentQuestion: MatchQuestion,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onWordClick: (String) -> Unit,
    onPictureClick: (String) -> Unit,
    onSpeakWord: (String) -> Unit,
    onShuffleClick: () -> Unit,
    onHintClick: () -> Unit,
    onNextQuestion: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val topTitleSpace = maxHeight * 0.188f

        Surface(
            modifier = Modifier.fillMaxSize().statusBarsPadding()
                .padding(horizontal = SproutelleDimens.MobileContentPadding)
                .padding(top = topTitleSpace, bottom = 12.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuestProgressBar(
                    currentQuestionIndex = state.currentQuestionIndex,
                    totalQuestions = state.questions.size,
                    completedIndices = state.completedQuestionIndices
                )

                MatchPromptCard(
                    promptText = currentQuestion.titlePrompt, isTablet = false
                )

                MatchingBoard(
                    wordNodes = state.wordNodes,
                    pictureNodes = state.pictureNodes,
                    matchedPairs = state.matchedPairs,
                    pairResults = state.pairResults,
                    isTablet = false,
                    onWordClick = onWordClick,
                    onPictureClick = onPictureClick,
                    onSpeakWord = onSpeakWord
                )

                MatchFeedbackMascot(
                    pairResults = state.pairResults,
                    isAllCorrect = state.isAllCorrect,
                    isTablet = false
                )

                Spacer(modifier = Modifier.weight(1f))

                val isLastQuestion = state.currentQuestionIndex == state.questions.lastIndex
                MatchBottomActionBar(
                    isAllCorrect = state.isAllCorrect,
                    isLastQuestion = isLastQuestion,
                    stars = state.stars,
                    onShuffleClick = onShuffleClick,
                    onHintClick = onHintClick,
                    onNextClick = onNextQuestion,
                    isTablet = false
                )
            }
        }

        QuestHeader(
            difficulty = state.difficulty,
            onBackClick = onBackClick,
            onHomeClick = onHomeClick,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 0.dp)
        )
    }
}
@Composable
private fun TabletMatchAndDiscoverContent(
    state: MatchAndDiscoverUiState.Success,
    currentQuestion: MatchQuestion,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onWordClick: (String) -> Unit,
    onPictureClick: (String) -> Unit,
    onSpeakWord: (String) -> Unit,
    onShuffleClick: () -> Unit,
    onHintClick: () -> Unit,
    onNextQuestion: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val topTitleSpace = maxHeight * 0.18f
        Surface(
            modifier = Modifier.fillMaxSize().statusBarsPadding()
                .padding(horizontal = SproutelleDimens.TabletContentPadding)
                .padding(top = topTitleSpace, bottom = 16.dp),
            shape = RoundedCornerShape(36.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuestProgressBar(
                    currentQuestionIndex = state.currentQuestionIndex,
                    totalQuestions = state.questions.size,
                    completedIndices = state.completedQuestionIndices
                )
                MatchPromptCard(
                    promptText = currentQuestion.titlePrompt, isTablet = true
                )
                MatchingBoard(
                    wordNodes = state.wordNodes,
                    pictureNodes = state.pictureNodes,
                    matchedPairs = state.matchedPairs,
                    pairResults = state.pairResults,
                    isTablet = true,
                    onWordClick = onWordClick,
                    onPictureClick = onPictureClick,
                    onSpeakWord = onSpeakWord
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MatchFeedbackMascot(
                        pairResults = state.pairResults,
                        isAllCorrect = state.isAllCorrect,
                        isTablet = true
                    )
                    val isLastQuestion = state.currentQuestionIndex == state.questions.lastIndex
                    MatchBottomActionBar(
                        isAllCorrect = state.isAllCorrect,
                        isLastQuestion = isLastQuestion,
                        stars = state.stars,
                        onShuffleClick = onShuffleClick,
                        onHintClick = onHintClick,
                        onNextClick = onNextQuestion,
                        isTablet = true,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }
        QuestHeader(
            difficulty = state.difficulty,
            onBackClick = onBackClick,
            onHomeClick = onHomeClick,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding()
                .padding(horizontal = SproutelleDimens.TabletContentPadding, vertical = 0.dp)
        )
    }
}
// ==========================================
// STATELESS PREVIEWS
// ==========================================
private fun sampleMatchPreviewState(): MatchAndDiscoverUiState.Success {
    val sampleQuestion = MatchQuestion(
        id = "mq_1",
        titlePrompt = "Match the words with pictures.",
        pairs = listOf(
            MatchPair("mp_1", "Broccoli", "🥦"),
            MatchPair("mp_2", "Cucumber", "🥒"),
            MatchPair("mp_3", "Sweet Potato", "🍠"),
            MatchPair("mp_4", "Orange", "🍊")
        )
    )
    return MatchAndDiscoverUiState.Success(
        questions = listOf(sampleQuestion, sampleQuestion, sampleQuestion),
        currentQuestionIndex = 2,
        wordNodes = listOf(
            WordNode(
                id = "w_mp_1",
                pairId = "mp_1",
                word = "Broccoli",
                isMatched = true,
                matchedPictureId = "p_mp_1",
                isCorrect = true
            ),
            WordNode(
                id = "w_mp_2",
                pairId = "mp_2",
                word = "Cucumber",
                isMatched = true,
                matchedPictureId = "p_mp_2",
                isCorrect = true
            ),
            WordNode(
                id = "w_mp_3",
                pairId = "mp_3",
                word = "Sweet Potato",
                isMatched = true,
                matchedPictureId = "p_mp_3",
                isCorrect = true
            ),
            WordNode(
                id = "w_mp_4",
                pairId = "mp_4",
                word = "Orange",
                isMatched = true,
                matchedPictureId = "p_mp_4",
                isCorrect = true
            )
        ),
        pictureNodes = listOf(
            PictureNode(
                id = "p_mp_3",
                pairId = "mp_3",
                emoji = "🍠",
                isMatched = true,
                matchedWordId = "w_mp_3",
                isCorrect = true
            ),
            PictureNode(
                id = "p_mp_4",
                pairId = "mp_4",
                emoji = "🍊",
                isMatched = true,
                matchedWordId = "w_mp_4",
                isCorrect = true
            ),
            PictureNode(
                id = "p_mp_1",
                pairId = "mp_1",
                emoji = "🥦",
                isMatched = true,
                matchedWordId = "w_mp_1",
                isCorrect = true
            ),
            PictureNode(
                id = "p_mp_2",
                pairId = "mp_2",
                emoji = "🥒",
                isMatched = true,
                matchedWordId = "w_mp_2",
                isCorrect = true
            )
        ),
        matchedPairs = mapOf(
            "w_mp_1" to "p_mp_1",
            "w_mp_2" to "p_mp_2",
            "w_mp_3" to "p_mp_3",
            "w_mp_4" to "p_mp_4"
        ),
        pairResults = mapOf(
            "w_mp_1" to true,
            "w_mp_2" to true,
            "w_mp_3" to true,
            "w_mp_4" to true
        ),
        completedQuestionIndices = setOf(0, 1, 2),
        difficulty = "Medium",
        stars = 2,
        isAllMatched = true,
        isAllCorrect = true
    )
}
@Preview(
    name = "Match & Discover - Mobile with Strings",
    device = "spec:width=411dp,height=891dp",
    showBackground = true
)
@Composable
private fun MobileMatchAndDiscoverStringsPreview() {
    MaterialTheme {
        MatchAndDiscoverContent(
            uiState = sampleMatchPreviewState(),
            isTablet = false,
            onRefresh = {},
            onBackClick = {},
            onHomeClick = {},
            onWordClick = {},
            onPictureClick = {},
            onSpeakWord = {},
            onShuffleClick = {},
            onHintClick = {},
            onNextQuestion = {}
        )
    }
}
@Preview(
    name = "Match & Discover - Tablet Landscape with Strings",
    device = "spec:width=1280dp,height=800dp,orientation=landscape",
    showBackground = true
)
@Composable
private fun TabletMatchAndDiscoverPreview() {
    MaterialTheme {
        MatchAndDiscoverContent(
            uiState = sampleMatchPreviewState(),
            isTablet = true,
            onRefresh = {},
            onBackClick = {},
            onHomeClick = {},
            onWordClick = {},
            onPictureClick = {},
            onSpeakWord = {},
            onShuffleClick = {},
            onHintClick = {},
            onNextQuestion = {}
        )
    }
}
