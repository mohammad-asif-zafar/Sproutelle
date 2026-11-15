package com.hathway.sproutelle.presentation.numberquest

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
import com.hathway.sproutelle.core.audio.rememberWordSpeaker
import com.hathway.sproutelle.core.responsive.ProvideWindowSizeClass
import com.hathway.sproutelle.presentation.components.EmptyState
import com.hathway.sproutelle.presentation.components.ErrorState
import com.hathway.sproutelle.presentation.components.LoadingState
import com.hathway.sproutelle.presentation.components.QuestHeader
import com.hathway.sproutelle.presentation.components.QuestProgressBar
import com.hathway.sproutelle.presentation.numberquest.components.NumberChoiceTiles
import com.hathway.sproutelle.presentation.numberquest.components.NumberCountGallery
import com.hathway.sproutelle.presentation.numberquest.components.NumberQuestFeedbackBanner
import org.jetbrains.compose.resources.painterResource
import sproutelle.shared.generated.resources.Res
import sproutelle.shared.generated.resources.number_quest_bg_mobile
import sproutelle.shared.generated.resources.number_quest_bg_tablet
import theme.SproutelleDimens

@Composable
fun NumberQuestScreen(
    viewModel: NumberQuestViewModel, onBackClick: () -> Unit, onHomeClick: () -> Unit = onBackClick
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val speakText = rememberWordSpeaker()
    val isAudioMuted = (uiState as? NumberQuestUiState.Success)?.isAudioMuted ?: false

    ProvideWindowSizeClass { windowSize ->
        NumberQuestContent(
            uiState = uiState,
            isTablet = windowSize.isTablet,
            onRefresh = viewModel::refresh,
            onBackClick = onBackClick,
            onHomeClick = onHomeClick,
            onCountItemTap = { countNumber ->
                if (!isAudioMuted) speakText(countNumber.toString())
            },
            onChoiceClick = { choiceId ->
                val state = uiState as? NumberQuestUiState.Success
                val q = state?.questions?.getOrNull(state.currentQuestionIndex)
                val chosen = q?.choices?.find { it.id == choiceId }
                if (!isAudioMuted && chosen != null) {
                    speakText(chosen.number.toString())
                }
                viewModel.selectChoice(choiceId)
            },
            onNextQuestion = viewModel::nextQuestion
        )
    }
}

@Composable
fun NumberQuestContent(
    uiState: NumberQuestUiState,
    isTablet: Boolean,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCountItemTap: (Int) -> Unit = {},
    onChoiceClick: (String) -> Unit,
    onNextQuestion: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(), color = Color(0xFFE0F7FA)
    ) {
        when (uiState) {
            is NumberQuestUiState.Loading -> {
                LoadingState(message = "Loading Number Quest...")
            }

            is NumberQuestUiState.Error -> {
                ErrorState(message = uiState.message, onRetryClick = onRefresh)
            }

            is NumberQuestUiState.Empty -> {
                EmptyState(
                    message = "No counting games available yet",
                    iconEmoji = "🔢",
                    actionLabel = "Refresh",
                    onActionClick = onRefresh
                )
            }

            is NumberQuestUiState.Success -> {
                val currentQuestion = uiState.questions.getOrNull(uiState.currentQuestionIndex)
                if (currentQuestion == null) {
                    EmptyState(
                        message = "No counting games available yet",
                        iconEmoji = "🔢",
                        actionLabel = "Refresh",
                        onActionClick = onRefresh
                    )
                } else {
                    NumberQuestAdaptiveLayout(
                        state = uiState,
                        currentQuestion = currentQuestion,
                        isTablet = isTablet,
                        onBackClick = onBackClick,
                        onHomeClick = onHomeClick,
                        onCountItemTap = onCountItemTap,
                        onChoiceClick = onChoiceClick,
                        onNextQuestion = onNextQuestion
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberQuestAdaptiveLayout(
    state: NumberQuestUiState.Success,
    currentQuestion: NumberQuestion,
    isTablet: Boolean,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCountItemTap: (Int) -> Unit,
    onChoiceClick: (String) -> Unit,
    onNextQuestion: () -> Unit
) {
    val backgroundImage =
        if (isTablet) Res.drawable.number_quest_bg_tablet else Res.drawable.number_quest_bg_mobile

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(backgroundImage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (isTablet) {
            TabletNumberQuestContent(
                state = state,
                currentQuestion = currentQuestion,
                onBackClick = onBackClick,
                onHomeClick = onHomeClick,
                onCountItemTap = onCountItemTap,
                onChoiceClick = onChoiceClick,
                onNextQuestion = onNextQuestion
            )
        } else {
            MobileNumberQuestContent(
                state = state,
                currentQuestion = currentQuestion,
                onBackClick = onBackClick,
                onHomeClick = onHomeClick,
                onCountItemTap = onCountItemTap,
                onChoiceClick = onChoiceClick,
                onNextQuestion = onNextQuestion
            )
        }
    }
}

@Composable
private fun MobileNumberQuestContent(
    state: NumberQuestUiState.Success,
    currentQuestion: NumberQuestion,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCountItemTap: (Int) -> Unit,
    onChoiceClick: (String) -> Unit,
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

            NumberCountGallery(
                questionKey = currentQuestion.id,
                promptText = currentQuestion.promptText,
                targetCount = currentQuestion.targetCount,
                itemEmoji = currentQuestion.itemEmoji,
                imageUrl = currentQuestion.imageUrl,
                isTablet = false,
                onCountItemTap = onCountItemTap
            )

            NumberChoiceTiles(
                choices = currentQuestion.choices,
                selectedChoiceId = state.selectedChoiceId,
                isCorrect = state.isCorrect,
                isTablet = false,
                onChoiceClick = onChoiceClick
            )

            AnimatedVisibility(
                visible = state.isCorrect != null, enter = fadeIn() + scaleIn(
                    initialScale = 0.85f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ), exit = fadeOut()
            ) {
                state.isCorrect?.let { correct ->
                    NumberQuestFeedbackBanner(
                        isCorrect = correct,
                        targetCount = currentQuestion.targetCount,
                        itemLabel = currentQuestion.itemLabel
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (state.isCorrect == true) 80.dp else 24.dp))
        }

        // Animated Pinned Floating Bottom Action Button
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text(
                    text = if (isLastQuestion) "See My Results ➔" else "Next ➔",
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
private fun TabletNumberQuestContent(
    state: NumberQuestUiState.Success,
    currentQuestion: NumberQuestion,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCountItemTap: (Int) -> Unit,
    onChoiceClick: (String) -> Unit,
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
                Column(
                    modifier = Modifier.weight(0.48f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    NumberCountGallery(
                        questionKey = currentQuestion.id,
                        promptText = currentQuestion.promptText,
                        targetCount = currentQuestion.targetCount,
                        itemEmoji = currentQuestion.itemEmoji,
                        imageUrl = currentQuestion.imageUrl,
                        isTablet = true,
                        onCountItemTap = onCountItemTap
                    )
                }
                Column(
                    modifier = Modifier.weight(0.52f).fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NumberChoiceTiles(
                        choices = currentQuestion.choices,
                        selectedChoiceId = state.selectedChoiceId,
                        isCorrect = state.isCorrect,
                        isTablet = true,
                        onChoiceClick = onChoiceClick
                    )
                    AnimatedVisibility(
                        visible = state.isCorrect != null,
                        enter = fadeIn() + scaleIn(initialScale = 0.85f),
                        exit = fadeOut()
                    ) {
                        state.isCorrect?.let { correct ->
                            NumberQuestFeedbackBanner(
                                isCorrect = correct,
                                targetCount = currentQuestion.targetCount,
                                itemLabel = currentQuestion.itemLabel
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                            shape = RoundedCornerShape(24.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            modifier = Modifier.fillMaxWidth().navigationBarsPadding().height(56.dp)
                        ) {
                            Text(
                                text = if (isLastQuestion) "See My Results ➔" else "Next ➔",
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
// STATELESS PREVIEWS
// ==========================================
private fun sampleNumberQuestPreviewState(
    selectedChoiceId: String? = "c2", isCorrect: Boolean? = true
): NumberQuestUiState.Success {
    val sampleQuestion = NumberQuestion(
        id = "nq_q1",
        promptText = "Count the lion?",
        targetCount = 3,
        itemEmoji = "🦁",
        itemLabel = "lions",
        choices = listOf(
            NumberChoice("c1", 4, 0xFFAB47BC, false),
            NumberChoice("c2", 3, 0xFFFF9800, true),
            NumberChoice("c3", 2, 0xFF4CAF50, false),
            NumberChoice("c4", 1, 0xFFFFCA28, false)
        )
    )
    return NumberQuestUiState.Success(
        questions = listOf(sampleQuestion, sampleQuestion, sampleQuestion),
        currentQuestionIndex = 1,
        selectedChoiceId = selectedChoiceId,
        selectedNumber = if (selectedChoiceId == "c2") 3 else null,
        isOptionSubmitted = isCorrect != null,
        isCorrect = isCorrect,
        completedQuestionIndices = if (isCorrect == true) setOf(0, 1) else setOf(0),
        difficulty = "Medium"
    )
}

@Preview(
    name = "Number Quest - Mobile Success",
    device = "spec:width=411dp,height=891dp",
    showBackground = true
)
@Composable
private fun MobileNumberQuestSuccessPreview() {
    MaterialTheme {
        NumberQuestContent(
            uiState = sampleNumberQuestPreviewState(selectedChoiceId = "c2", isCorrect = true),
            isTablet = false,
            onRefresh = {},
            onBackClick = {},
            onHomeClick = {},
            onChoiceClick = {},
            onNextQuestion = {})
    }
}

@Preview(
    name = "Number Quest - Mobile Playing",
    device = "spec:width=411dp,height=891dp",
    showBackground = true
)
@Composable
private fun MobileNumberQuestPlayingPreview() {
    MaterialTheme {
        NumberQuestContent(
            uiState = sampleNumberQuestPreviewState(selectedChoiceId = null, isCorrect = null),
            isTablet = false,
            onRefresh = {},
            onBackClick = {},
            onHomeClick = {},
            onChoiceClick = {},
            onNextQuestion = {})
    }
}

@Preview(
    name = "Number Quest - Tablet Landscape",
    device = "spec:width=1280dp,height=800dp,orientation=landscape",
    showBackground = true
)
@Composable
private fun TabletNumberQuestPreview() {
    MaterialTheme {
        NumberQuestContent(
            uiState = sampleNumberQuestPreviewState(selectedChoiceId = "c2", isCorrect = true),
            isTablet = true,
            onRefresh = {},
            onBackClick = {},
            onHomeClick = {},
            onChoiceClick = {},
            onNextQuestion = {})
    }
}
