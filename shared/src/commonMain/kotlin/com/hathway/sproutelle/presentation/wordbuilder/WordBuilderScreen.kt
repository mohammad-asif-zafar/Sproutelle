package com.hathway.sproutelle.presentation.wordbuilder

import ScrambledLetterTiles
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hathway.sproutelle.core.responsive.ProvideWindowSizeClass
import com.hathway.sproutelle.presentation.components.EmptyState
import com.hathway.sproutelle.presentation.components.ErrorState
import com.hathway.sproutelle.presentation.components.LoadingState
import com.hathway.sproutelle.presentation.components.QuestHeader
import com.hathway.sproutelle.presentation.components.QuestProgressBar
import com.hathway.sproutelle.presentation.wordbuilder.components.WordBuilderFeedbackBanner
import com.hathway.sproutelle.presentation.wordbuilder.components.WordTargetSlots
import org.jetbrains.compose.resources.painterResource
import sproutelle.shared.generated.resources.Res
import sproutelle.shared.generated.resources.Word_Builder_bg_mobile
import sproutelle.shared.generated.resources.Word_Builder_bg_tablet
import theme.SproutelleDimens

@Composable
fun WordBuilderScreen(
    viewModel: WordBuilderViewModel, onBackClick: () -> Unit, onHomeClick: () -> Unit = onBackClick
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProvideWindowSizeClass { windowSize ->
        WordBuilderContent(
            uiState = uiState,
            isTablet = windowSize.isTablet,
            onRefresh = viewModel::refresh,
            onBackClick = onBackClick,
            onHomeClick = onHomeClick,
            onTileClick = viewModel::selectTile,
            onSlotClick = viewModel::removeSlotLetter,
            onNextQuestion = viewModel::nextQuestion
        )
    }
}

/**
 * Pure stateless UI entry point: receives all state and callbacks via parameters.
 */
@Composable
fun WordBuilderContent(
    uiState: WordBuilderUiState,
    isTablet: Boolean,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTileClick: (String) -> Unit,
    onSlotClick: (Int) -> Unit,
    onNextQuestion: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(), color = Color(0xFFE0F7FA)
    ) {
        when (uiState) {
            is WordBuilderUiState.Loading -> {
                LoadingState(message = "Loading Word Builder...")
            }

            is WordBuilderUiState.Error -> {
                ErrorState(message = uiState.message, onRetryClick = onRefresh)
            }

            is WordBuilderUiState.Empty -> {
                EmptyState(
                    message = "No words available for Word Builder yet",
                    iconEmoji = "🔤",
                    actionLabel = "Refresh",
                    onActionClick = onRefresh
                )
            }

            is WordBuilderUiState.Success -> {
                val currentQuestion = uiState.questions.getOrNull(uiState.currentQuestionIndex)
                if (currentQuestion == null) {
                    EmptyState(
                        message = "No words available for Word Builder yet",
                        iconEmoji = "🔤",
                        actionLabel = "Refresh",
                        onActionClick = onRefresh
                    )
                } else {
                    WordBuilderAdaptiveLayout(
                        state = uiState,
                        currentQuestion = currentQuestion,
                        isTablet = isTablet,
                        onBackClick = onBackClick,
                        onHomeClick = onHomeClick,
                        onTileClick = onTileClick,
                        onSlotClick = onSlotClick,
                        onNextQuestion = onNextQuestion
                    )
                }
            }
        }
    }
}

@Composable
private fun WordBuilderAdaptiveLayout(
    state: WordBuilderUiState.Success,
    currentQuestion: WordQuestion,
    isTablet: Boolean,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTileClick: (String) -> Unit,
    onSlotClick: (Int) -> Unit,
    onNextQuestion: () -> Unit
) {
    val backgroundImage =
        if (isTablet) Res.drawable.Word_Builder_bg_tablet else Res.drawable.Word_Builder_bg_mobile

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(backgroundImage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (isTablet) {
            TabletWordBuilderContent(
                state = state,
                currentQuestion = currentQuestion,
                onBackClick = onBackClick,
                onHomeClick = onHomeClick,
                onTileClick = onTileClick,
                onSlotClick = onSlotClick,
                onNextQuestion = onNextQuestion
            )
        } else {
            MobileWordBuilderContent(
                state = state,
                currentQuestion = currentQuestion,
                onBackClick = onBackClick,
                onHomeClick = onHomeClick,
                onTileClick = onTileClick,
                onSlotClick = onSlotClick,
                onNextQuestion = onNextQuestion
            )
        }
    }
}

@Composable
private fun MobileWordBuilderContent(
    state: WordBuilderUiState.Success,
    currentQuestion: WordQuestion,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTileClick: (String) -> Unit,
    onSlotClick: (Int) -> Unit,
    onNextQuestion: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val topTitleSpace = maxHeight * 0.18f

        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SproutelleDimens.MobileContentPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(topTitleSpace))

            QuestProgressBar(
                currentQuestionIndex = state.currentQuestionIndex,
                totalQuestions = state.questions.size,
                completedIndices = state.completedQuestionIndices
            )

            WordMascotPromptCard(
                promptText = currentQuestion.promptText,
                categoryEmoji = currentQuestion.categoryEmoji,
                imageUrl = currentQuestion.imageUrl,
                contentDescription = currentQuestion.targetWord,
                isTablet = false
            )

            WordTargetSlots(
                placedLetters = state.placedLetters,
                isCorrect = state.isCorrect,
                isTablet = false,
                onSlotClick = onSlotClick
            )

            ScrambledLetterTiles(
                availableTiles = state.availableTiles, isTablet = false, onTileClick = onTileClick
            )

            AnimatedVisibility(
                visible = state.isCorrect != null, enter = fadeIn() + scaleIn(
                    initialScale = 0.85f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ), exit = fadeOut()
            ) {
                state.isCorrect?.let { correct ->
                    WordBuilderFeedbackBanner(
                        isCorrect = correct, targetWord = state.targetWord
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (state.isCorrect == true) 80.dp else 24.dp))
        }

        // Animated Floating Bottom Action Button
        val isLastQuestion = state.currentQuestionIndex == state.questions.lastIndex
        AnimatedVisibility(
            visible = state.isCorrect == true,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
        ) {
            Button(
                onClick = onNextQuestion,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text(
                    text = if (isLastQuestion) "See My Results ➔" else "Next Word ➔",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
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
private fun TabletWordBuilderContent(
    state: WordBuilderUiState.Success,
    currentQuestion: WordQuestion,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTileClick: (String) -> Unit,
    onSlotClick: (Int) -> Unit,
    onNextQuestion: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val topTitleSpace = maxHeight * 0.18f
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding()
                .padding(horizontal = SproutelleDimens.TabletContentPadding)
                .padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(topTitleSpace))
            QuestProgressBar(
                currentQuestionIndex = state.currentQuestionIndex,
                totalQuestions = state.questions.size,
                completedIndices = state.completedQuestionIndices
            )
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
// Left Pane: Mascot & Prompt Card
                Column(
                    modifier = Modifier.weight(0.42f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    WordMascotPromptCard(
                        promptText = currentQuestion.promptText,
                        categoryEmoji = currentQuestion.categoryEmoji,
                        imageUrl = currentQuestion.imageUrl,
                        contentDescription = currentQuestion.targetWord,
                        isTablet = true
                    )
                }
// Right Pane: Slots + Letter Pool + Action Button
                Column(
                    modifier = Modifier.weight(0.58f).fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    WordTargetSlots(
                        placedLetters = state.placedLetters,
                        isCorrect = state.isCorrect,
                        isTablet = true,
                        onSlotClick = onSlotClick
                    )
                    ScrambledLetterTiles(
                        availableTiles = state.availableTiles,
                        isTablet = true,
                        onTileClick = onTileClick
                    )
                    AnimatedVisibility(
                        visible = state.isCorrect != null,
                        enter = fadeIn() + scaleIn(initialScale = 0.85f),
                        exit = fadeOut()
                    ) {
                        state.isCorrect?.let { correct ->
                            WordBuilderFeedbackBanner(
                                isCorrect = correct, targetWord = state.targetWord
                            )
                        }
                    }
                    val isLastQuestion = state.currentQuestionIndex == state.questions.lastIndex
                    AnimatedVisibility(
                        visible = state.isCorrect == true, enter = scaleIn(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        ) + fadeIn(), exit = fadeOut()
                    ) {
                        Button(
                            onClick = onNextQuestion,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(24.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            modifier = Modifier.fillMaxWidth().navigationBarsPadding().height(56.dp)
                        ) {
                            Text(
                                text = if (isLastQuestion) "See My Results ➔" else "Next Word ➔",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
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
// STATELESS PREVIEWS (No ViewModel Needed)
// ==========================================
private fun samplePreviewState(
    placedLetters: List<Char?> = listOf('F', 'O', null), isCorrect: Boolean? = null
): WordBuilderUiState.Success {
    val sampleQuestion = WordQuestion(
        id = "preview_q1",
        targetWord = "FOX",
        promptText = "What do we call this Animal?",
        categoryEmoji = "🦊",
        initialTiles = listOf(
            WordLetterTile("t1", 'S', isUsed = false, tileColorHex = 0xFFFFFFFF),
            WordLetterTile("t2", 'N', isUsed = false, tileColorHex = 0xFFFF5252),
            WordLetterTile(
                "t3", 'F', isUsed = placedLetters.contains('F'), tileColorHex = 0xFF4CAF50
            ),
            WordLetterTile("t4", 'A', isUsed = false, tileColorHex = 0xFFFFFFFF),
            WordLetterTile(
                "t5", 'O', isUsed = placedLetters.contains('O'), tileColorHex = 0xFF4CAF50
            ),
            WordLetterTile(
                "t6", 'X', isUsed = placedLetters.contains('X'), tileColorHex = 0xFFFFFFFF
            ),
            WordLetterTile("t7", 'Z', isUsed = false, tileColorHex = 0xFFFFFFFF)
        )
    )
    return WordBuilderUiState.Success(
        questions = listOf(sampleQuestion, sampleQuestion, sampleQuestion),
        currentQuestionIndex = 0,
        targetWord = "FOX",
        placedLetters = placedLetters,
        availableTiles = sampleQuestion.initialTiles,
        isOptionSubmitted = isCorrect != null,
        isCorrect = isCorrect,
        completedQuestionIndices = if (isCorrect == true) setOf(0) else emptySet(),
        difficulty = "Easy"
    )
}

@Preview(
    name = "Mobile Portrait - Playing",
    device = "spec:width=411dp,height=891dp",
    showBackground = true
)
@Composable
private fun MobileWordBuilderPlayingPreview() {
    MaterialTheme {
        WordBuilderContent(
            uiState = samplePreviewState(placedLetters = listOf('F', 'O', null), isCorrect = null),
            isTablet = false,
            onRefresh = {},
            onBackClick = {},
            onHomeClick = {},
            onTileClick = {},
            onSlotClick = {},
            onNextQuestion = {})
    }
}

@Preview(
    name = "Mobile Portrait - Success",
    device = "spec:width=411dp,height=891dp",
    showBackground = true
)
@Composable
private fun MobileWordBuilderSuccessPreview() {
    MaterialTheme {
        WordBuilderContent(
            uiState = samplePreviewState(placedLetters = listOf('F', 'O', 'X'), isCorrect = true),
            isTablet = false,
            onRefresh = {},
            onBackClick = {},
            onHomeClick = {},
            onTileClick = {},
            onSlotClick = {},
            onNextQuestion = {})
    }
}

@Preview(
    name = "Tablet Landscape - Playing",
    device = "spec:width=1280dp,height=800dp,orientation=landscape",
    showBackground = true
)
@Composable
private fun TabletWordBuilderPreview() {
    MaterialTheme {
        WordBuilderContent(
            uiState = samplePreviewState(placedLetters = listOf('F', null, null), isCorrect = null),
            isTablet = true,
            onRefresh = {},
            onBackClick = {},
            onHomeClick = {},
            onTileClick = {},
            onSlotClick = {},
            onNextQuestion = {})
    }
}
