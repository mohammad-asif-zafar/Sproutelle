package com.hathway.sproutelle.presentation.picturequest

import androidx.compose.foundation.Image
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hathway.sproutelle.core.responsive.ProvideWindowSizeClass
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.QuestionOption
import com.hathway.sproutelle.presentation.components.EmptyState
import com.hathway.sproutelle.presentation.components.ErrorState
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
                    if (currentQuestion == null) {
                        EmptyState(
                            message = "No questions available for Picture Quest yet",
                            iconEmoji = "🖼️",
                            actionLabel = "Refresh",
                            onActionClick = viewModel::refresh
                        )
                    } else {
                        PictureQuestAdaptiveLayout(
                            state = state,
                            currentQuestion = currentQuestion,
                            isTablet = windowSize.isTablet,
                            onBackClick = onBackClick,
                            onHomeClick = onHomeClick,
                            onAudioToggle = viewModel::toggleAudioMute,
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
    onOptionSelect: (String) -> Unit,
    onNextQuestion: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val topTitleSpace = maxHeight * 0.16f
        val optionsCount = currentQuestion.options.size

        // Single Master Scrolling Layout completely eliminates items getting cut off or hidden behind overlays
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SproutelleDimens.MobileContentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(topTitleSpace))

            QuestProgressBar(
                currentQuestionIndex = state.currentQuestionIndex,
                totalQuestions = state.questions.size,
                completedIndices = state.completedQuestionIndices
            )

            PictureQuestQuestionCard(
                question = currentQuestion,
                isAudioMuted = state.isAudioMuted,
                onAudioToggle = onAudioToggle,
                isTablet = false
            )

            // Dynamic Adaptive Row Slicing: Prevents layout flaws across irregular dataset lengths
            val chunkedOptions = currentQuestion.options.chunked(if (optionsCount == 3) 1 else 2)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                chunkedOptions.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { option ->
                            val isSelected = state.selectedOptionId == option.id
                            SingleImageAnswerCard(
                                option = option,
                                isSelected = isSelected,
                                isCorrect = state.isCorrect,
                                isTablet = false,
                                onClick = { onOptionSelect(option.id) },
                                modifier = Modifier.weight(1f) // Evenly spans columns perfectly
                            )
                        }
                        // Invisible filler balancing odd counts tails
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

            // Interactive Button embedded at the end of the scroll chain guarantees absolute safety visibility
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
                        .padding(vertical = 12.dp)
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
            } else {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        QuestHeader(
            difficulty = state.difficulty,
            onBackClick = onBackClick,
            onHomeClick = onHomeClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 4.dp)
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
            QuestProgressBar(
                currentQuestionIndex = state.currentQuestionIndex,
                totalQuestions = state.questions.size,
                completedIndices = state.completedQuestionIndices
            )
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

// ==========================================
// PREVIEWS WITH REALISTIC MOCK DATA
// ==========================================

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
            onOptionSelect = {},
            onNextQuestion = {})
    }
}

@Preview(
    name = "Mobile Portrait - Answer Correct",
    device = "spec:width=411dp,height=891dp",
    showBackground = true
)
@Composable
private fun MobilePictureQuestCorrectPreview() {
    val mockState = PictureQuestUiState.Success(
        questions = getMockQuestions(),
        currentQuestionIndex = 0,
        selectedOptionId = "pq_o1_3",
        isOptionSubmitted = true,
        isCorrect = true,
        completedQuestionIndices = setOf(0),
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
            onOptionSelect = {},
            onNextQuestion = {})
    }
}

@Preview(
    name = "Mobile Portrait - Answer Incorrect",
    device = "spec:width=411dp,height=891dp",
    showBackground = true
)
@Composable
private fun MobilePictureQuestIncorrectPreview() {
    val mockState = PictureQuestUiState.Success(
        questions = getMockQuestions(),
        currentQuestionIndex = 0,
        selectedOptionId = "pq_o1_1",
        isOptionSubmitted = true,
        isCorrect = false,
        completedQuestionIndices = emptySet(),
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
            onOptionSelect = {},
            onNextQuestion = {})
    }
}

@Preview(
    name = "Tablet Landscape - Question Unanswered",
    device = "spec:width=1280dp,height=800dp,orientation=landscape",
    showBackground = true
)
@Composable
private fun TabletPictureQuestPreview() {
    val mockState = PictureQuestUiState.Success(
        questions = getMockQuestions(),
        currentQuestionIndex = 0,
        selectedOptionId = null,
        isOptionSubmitted = false,
        isCorrect = null,
        completedQuestionIndices = emptySet(),
        isAudioMuted = false,
        isOffline = false,
        difficulty = "Hard"
    )
    MaterialTheme {
        PictureQuestAdaptiveLayout(
            state = mockState,
            currentQuestion = mockState.questions[0],
            isTablet = true,
            onBackClick = {},
            onHomeClick = {},
            onAudioToggle = {},
            onOptionSelect = {},
            onNextQuestion = {})
    }
}

@Preview(
    name = "Tablet Landscape - Answer Correct",
    device = "spec:width=1280dp,height=800dp,orientation=landscape",
    showBackground = true
)
@Composable
private fun TabletPictureQuestCorrectPreview() {
    val mockState = PictureQuestUiState.Success(
        questions = getMockQuestions(),
        currentQuestionIndex = 0,
        selectedOptionId = "pq_o1_3",
        isOptionSubmitted = true,
        isCorrect = true,
        completedQuestionIndices = setOf(0),
        isAudioMuted = false,
        isOffline = false,
        difficulty = "Hard"
    )
    MaterialTheme {
        PictureQuestAdaptiveLayout(
            state = mockState,
            currentQuestion = mockState.questions[0],
            isTablet = true,
            onBackClick = {},
            onHomeClick = {},
            onAudioToggle = {},
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
