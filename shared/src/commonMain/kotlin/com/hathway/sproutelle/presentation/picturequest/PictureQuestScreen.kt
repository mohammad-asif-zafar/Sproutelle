package com.hathway.sproutelle.presentation.picturequest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hathway.sproutelle.core.responsive.ProvideWindowSizeClass
import com.hathway.sproutelle.domain.model.GameLevel
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.QuestionOption
import com.hathway.sproutelle.presentation.components.EmptyState
import com.hathway.sproutelle.presentation.components.ErrorState
import com.hathway.sproutelle.presentation.components.GameLevelBadge
import com.hathway.sproutelle.presentation.components.LoadingState
import com.hathway.sproutelle.presentation.components.PictureQuestQuestionCard
import com.hathway.sproutelle.presentation.components.QuestFeedbackBannerImage
import com.hathway.sproutelle.presentation.components.QuestHeader
import com.hathway.sproutelle.presentation.components.QuestProgressBar
import com.hathway.sproutelle.presentation.components.SingleImageAnswerCard
import org.jetbrains.compose.resources.painterResource
import sproutelle.shared.generated.resources.Picture_Quest_bg_mobile
import sproutelle.shared.generated.resources.Picture_Quest_bg_tablet
import sproutelle.shared.generated.resources.Res
import theme.SproutelleDimens
import theme.SproutelleSpacing

@Composable
fun PictureQuestScreen(
    viewModel: PictureQuestViewModel,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit = onBackClick
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProvideWindowSizeClass { windowSize ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFE0F7FA)
        ) {
            when (val state = uiState) {
                is PictureQuestUiState.Loading -> {
                    LoadingState(message = "Loading Picture Quest...")
                }

                is PictureQuestUiState.Error -> {
                    ErrorState(message = state.message, onRetryClick = viewModel::refresh)
                }

                is PictureQuestUiState.Empty -> {
                    EmptyState(
                        message = "No questions available for Picture Quest yet",
                        iconEmoji = "🖼️",
                        actionLabel = "Refresh",
                        onActionClick = viewModel::refresh
                    )
                }

                is PictureQuestUiState.Success -> {
                    val currentQuestion = state.questions.getOrNull(state.currentQuestionIndex)
                    if (state.isGameCompleted) {
                        PictureQuestCompletionScreen(
                            state = state,
                            onRestart = viewModel::restartGame,
                            onBackClick = onBackClick
                        )
                    } else if (currentQuestion == null) {
                        if (state.levels.isNotEmpty()) {
                            PictureQuestLevelEmptyScreen(
                                state = state,
                                onLevelSelect = viewModel::selectLevel,
                                onBackClick = onBackClick,
                                onHomeClick = onHomeClick
                            )
                        } else {
                            EmptyState(
                                message = "No questions available for Picture Quest yet",
                                iconEmoji = "🖼️",
                                actionLabel = "Refresh",
                                onActionClick = viewModel::refresh
                            )
                        }
                    } else {
                        PictureQuestAdaptiveLayout(
                            state = state,
                            currentQuestion = currentQuestion,
                            isTablet = windowSize.isTablet,
                            onBackClick = onBackClick,
                            onHomeClick = onHomeClick,
                            onAudioToggle = viewModel::toggleAudioMute,
                            onHintToggle = viewModel::toggleHint,
                            onLevelSelect = viewModel::selectLevel,
                            onOptionSelect = viewModel::selectOption,
                            onNextQuestion = viewModel::nextQuestion
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PictureQuestAdaptiveLayout(
    state: PictureQuestUiState.Success,
    currentQuestion: Question,
    isTablet: Boolean,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onAudioToggle: () -> Unit,
    onHintToggle: () -> Unit,
    onLevelSelect: (Int) -> Unit,
    onOptionSelect: (String) -> Unit,
    onNextQuestion: () -> Unit
) {
    val backgroundImage =
        if (isTablet) Res.drawable.Picture_Quest_bg_tablet else Res.drawable.Picture_Quest_bg_mobile

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(backgroundImage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (isTablet) {
            TabletPictureQuestContent(
                state = state,
                currentQuestion = currentQuestion,
                onBackClick = onBackClick,
                onHomeClick = onHomeClick,
                onAudioToggle = onAudioToggle,
                onHintToggle = onHintToggle,
                onLevelSelect = onLevelSelect,
                onOptionSelect = onOptionSelect,
                onNextQuestion = onNextQuestion
            )
        } else {
            MobilePictureQuestContent(
                state = state,
                currentQuestion = currentQuestion,
                onBackClick = onBackClick,
                onHomeClick = onHomeClick,
                onAudioToggle = onAudioToggle,
                onHintToggle = onHintToggle,
                onLevelSelect = onLevelSelect,
                onOptionSelect = onOptionSelect,
                onNextQuestion = onNextQuestion
            )
        }
    }
}

@Composable
private fun MobilePictureQuestContent(
    state: PictureQuestUiState.Success,
    currentQuestion: Question,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onAudioToggle: () -> Unit,
    onHintToggle: () -> Unit,
    onLevelSelect: (Int) -> Unit,
    onOptionSelect: (String) -> Unit,
    onNextQuestion: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val topTitleSpace = maxHeight * 0.14f
        val optionsCount = currentQuestion.options.size

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SproutelleDimens.MobileContentPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(topTitleSpace))

            if (state.levels.size > 1) {
                LevelSelectorBar(
                    levels = state.levels,
                    currentLevelIndex = state.currentLevelIndex,
                    onLevelSelect = onLevelSelect
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuestProgressBar(
                    currentQuestionIndex = state.currentQuestionIndex,
                    totalQuestions = state.questions.size,
                    completedIndices = state.completedQuestionIndices,
                    modifier = Modifier.weight(1f)
                )

                // Score Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFD54F),
                    shadowElevation = 2.dp,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = "⭐ ${state.score}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF5D4037),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            PictureQuestQuestionCard(
                question = currentQuestion,
                isAudioMuted = state.isAudioMuted,
                onAudioToggle = onAudioToggle,
                isTablet = false
            )

            // Hint Button
            if (state.gameConfig?.enableHints == true && !currentQuestion.hint.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF176),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable(onClick = onHintToggle)
                ) {
                    Text(
                        text = if (state.showHint) "💡 Hint: ${currentQuestion.hint}" else "💡 Need a Hint?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF37474F),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // Dynamic Adaptive Row Slicing
            val chunkedOptions = currentQuestion.options.chunked(if (optionsCount == 3) 1 else 2)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                chunkedOptions.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        rowItems.forEach { option ->
                            val isSelected = state.selectedOptionId == option.id
                            SingleImageAnswerCard(
                                option = option,
                                isSelected = isSelected,
                                isCorrect = state.isCorrect,
                                isTablet = false,
                                onClick = { onOptionSelect(option.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowItems.size < (if (optionsCount == 3) 1 else 2)) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (state.isCorrect != null) {
                QuestFeedbackBannerImage(
                    isCorrect = state.isCorrect,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (state.isCorrect == true) {
                Spacer(modifier = Modifier.height(72.dp))
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Pinned Bottom Action Button Area
        if (state.isCorrect == true) {
            val isLastQuestion = state.currentQuestionIndex == state.questions.lastIndex
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                color = Color.Transparent
            ) {
                Button(
                    onClick = onNextQuestion,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = if (isLastQuestion) "See My Results ➔" else "Next Question ➔",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        QuestHeader(
            difficulty = state.difficulty,
            onBackClick = onBackClick,
            onHomeClick = onHomeClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 0.dp)
        )
    }
}

@Composable
private fun TabletPictureQuestContent(
    state: PictureQuestUiState.Success,
    currentQuestion: Question,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onAudioToggle: () -> Unit,
    onHintToggle: () -> Unit,
    onLevelSelect: (Int) -> Unit,
    onOptionSelect: (String) -> Unit,
    onNextQuestion: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val topTitleSpace = maxHeight * 0.20f
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = SproutelleDimens.TabletContentPadding)
                .padding(bottom = SproutelleSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(topTitleSpace))

            if (state.levels.size > 1) {
                LevelSelectorBar(
                    levels = state.levels,
                    currentLevelIndex = state.currentLevelIndex,
                    onLevelSelect = onLevelSelect
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuestProgressBar(
                    currentQuestionIndex = state.currentQuestionIndex,
                    totalQuestions = state.questions.size,
                    completedIndices = state.completedQuestionIndices,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFD54F),
                    shadowElevation = 3.dp,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = "⭐ ${state.score} Points",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF5D4037),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.40f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Top
                ) {
                    PictureQuestQuestionCard(
                        question = currentQuestion,
                        isAudioMuted = state.isAudioMuted,
                        onAudioToggle = onAudioToggle,
                        isTablet = true
                    )

                    if (state.gameConfig?.enableHints == true && !currentQuestion.hint.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFFFF176),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onHintToggle)
                        ) {
                            Text(
                                text = if (state.showHint) "💡 Hint: ${currentQuestion.hint}" else "💡 Need a Hint?",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF37474F),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(0.60f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val options = currentQuestion.options
                    val chunkedOptions = options.chunked(if (options.size <= 4) 2 else 3)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        chunkedOptions.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                rowItems.forEach { option ->
                                    val isSelected = state.selectedOptionId == option.id
                                    SingleImageAnswerCard(
                                        option = option,
                                        isTablet = true,
                                        isSelected = isSelected,
                                        isCorrect = state.isCorrect,
                                        onClick = { onOptionSelect(option.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size < (if (options.size <= 4) 2 else 3)) {
                                    repeat((if (options.size <= 4) 2 else 3) - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    if (state.isCorrect != null) {
                        QuestFeedbackBannerImage(
                            isCorrect = state.isCorrect,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    if (state.isCorrect == true) {
                        val isLastQuestion = state.currentQuestionIndex == state.questions.lastIndex
                        Button(
                            onClick = onNextQuestion,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(24.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .height(56.dp)
                        ) {
                            Text(
                                text = if (isLastQuestion) "See My Results ➔" else "Next Question ➔",
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
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = SproutelleDimens.TabletContentPadding, vertical = 4.dp)
        )
    }
}

@Composable
fun LevelSelectorBar(
    levels: List<GameLevel>,
    currentLevelIndex: Int,
    onLevelSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (levels.size <= 1) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        levels.forEachIndexed { idx, level ->
            val isSelected = idx == currentLevelIndex
            GameLevelBadge(
                levelText = level.name.ifBlank { "Level ${level.levelNumber}" },
                icon = if (isSelected) "⭐" else "🌱",
                backgroundColor = if (isSelected) Color(0xFFFFE0B2) else Color(0xFFF5F5F5),
                borderColor = if (isSelected) Color(0xFFFFB74D) else Color(0xFFE0E0E0),
                textColor = if (isSelected) Color(0xFFE65100) else Color(0xFF616161),
                onClick = { onLevelSelect(idx) }
            )
        }
    }
}

@Composable
private fun PictureQuestLevelEmptyScreen(
    state: PictureQuestUiState.Success,
    onLevelSelect: (Int) -> Unit,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit = onBackClick
) {
    val currentLevel = state.levels.getOrNull(state.currentLevelIndex)
    val levelName = currentLevel?.name?.ifBlank { "Level ${state.currentLevelIndex + 1}" } ?: "Level ${state.currentLevelIndex + 1}"

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            LevelSelectorBar(
                levels = state.levels,
                currentLevelIndex = state.currentLevelIndex,
                onLevelSelect = onLevelSelect
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🌱 $levelName is Empty 🌱",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF0D47A1),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "No questions available for $levelName yet. Please select another level above!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF5D4037),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        QuestHeader(
            difficulty = state.difficulty,
            onBackClick = onBackClick,
            onHomeClick = onHomeClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun PictureQuestCompletionScreen(
    state: PictureQuestUiState.Success,
    onRestart: () -> Unit,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F7FA))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🎉 Great Job! 🎉",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF0D47A1),
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "You completed Picture Quest!",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF37474F),
                    textAlign = TextAlign.Center
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF9C4),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "⭐ Final Score: ${state.score} ⭐",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF5D4037),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Questions Answered: ${state.completedQuestionIndices.size}/${state.questions.size}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF795548)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onRestart,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Play Again 🔄", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Done ✨", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Previews
@Preview(
    name = "Mobile Portrait - Question Unanswered",
    device = "spec:width=411dp,height=891dp",
    showBackground = true
)
@Composable
private fun MobilePictureQuestPreview() {
    val mockState = PictureQuestUiState.Success(
        questions = getMockQuestions(),
        currentQuestionIndex = 0,
        selectedOptionId = null,
        isOptionSubmitted = false,
        isCorrect = null,
        completedQuestionIndices = setOf(1, 2),
        isAudioMuted = false,
        isOffline = false,
        difficulty = "Hard"
    )
    MaterialTheme {
        PictureQuestAdaptiveLayout(
            state = mockState,
            currentQuestion = mockState.questions[0],
            isTablet = false,
            onBackClick = {},
            onHomeClick = {},
            onAudioToggle = {},
            onHintToggle = {},
            onLevelSelect = {},
            onOptionSelect = {},
            onNextQuestion = {})
    }
}

private fun getMockQuestions() = listOf(
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
    )
)
