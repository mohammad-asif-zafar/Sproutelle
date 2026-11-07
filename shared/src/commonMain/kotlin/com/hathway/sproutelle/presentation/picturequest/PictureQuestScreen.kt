package com.hathway.sproutelle.presentation.picturequest

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.hathway.sproutelle.core.responsive.ProvideWindowSizeClass
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.QuestionOption
import com.hathway.sproutelle.presentation.components.EmptyState
import com.hathway.sproutelle.presentation.components.ErrorState
import com.hathway.sproutelle.presentation.components.LoadingState
import com.hathway.sproutelle.presentation.components.RoundedIconButton
import org.jetbrains.compose.resources.painterResource
import sproutelle.shared.generated.resources.Picture_Quest_bg_mobile
import sproutelle.shared.generated.resources.Picture_Quest_bg_tablet
import sproutelle.shared.generated.resources.Res
import sproutelle.shared.generated.resources.fox_great_job
import sproutelle.shared.generated.resources.fox_try_again
import theme.SproutelleDimens
import theme.SproutelleSpacing

@Composable
fun PictureQuestScreen(
    viewModel: PictureQuestViewModel,
    onBackClick: () -> Unit
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
                    ErrorState(
                        message = state.message,
                        onRetryClick = viewModel::refresh
                    )
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
                            gameName = viewModel.gameName.ifBlank { "Picture Quest" },
                            currentQuestion = currentQuestion,
                            isTablet = windowSize.isTablet,
                            onBackClick = onBackClick,
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
    gameName: String,
    currentQuestion: Question,
    isTablet: Boolean,
    onBackClick: () -> Unit,
    onAudioToggle: () -> Unit,
    onOptionSelect: (String) -> Unit,
    onNextQuestion: () -> Unit
) {
    val isPreviewMode = LocalInspectionMode.current

    Box(modifier = Modifier.fillMaxSize()) {
        if (isPreviewMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF81D4FA), Color(0xFFE0F7FA), Color(0xFFFFF9E6))
                        )
                    )
            )
        } else {
            val backgroundImage = if (isTablet) Res.drawable.Picture_Quest_bg_tablet else Res.drawable.Picture_Quest_bg_mobile
            Image(
                painter = painterResource(backgroundImage),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        if (isTablet) {
            TabletPictureQuestContent(
                state = state,
                gameName = gameName,
                currentQuestion = currentQuestion,
                onBackClick = onBackClick,
                onAudioToggle = onAudioToggle,
                onOptionSelect = onOptionSelect,
                onNextQuestion = onNextQuestion
            )
        } else {
            MobilePictureQuestContent(
                state = state,
                gameName = gameName,
                currentQuestion = currentQuestion,
                onBackClick = onBackClick,
                onAudioToggle = onAudioToggle,
                onOptionSelect = onOptionSelect,
                onNextQuestion = onNextQuestion
            )
        }

        // Bouncy Feedback Overlay Layer for Kid Interaction Loops
        if (state.isCorrect != null) {
            PictureQuestFeedbackOverlay(
                isCorrect = state.isCorrect,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun MobilePictureQuestContent(
    state: PictureQuestUiState.Success,
    gameName: String,
    currentQuestion: Question,
    onBackClick: () -> Unit,
    onAudioToggle: () -> Unit,
    onOptionSelect: (String) -> Unit,
    onNextQuestion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = SproutelleDimens.MobileContentPadding)
            .padding(bottom = SproutelleSpacing.XXLarge),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PictureQuestHeader(gameName = gameName, difficulty = state.difficulty, onBackClick = onBackClick)
        PictureQuestProgressBar(
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

        val options = currentQuestion.options
        if (options.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(options) { option ->
                    val isSelected = state.selectedOptionId == option.id
                    PictureAnswerCard(
                        option = option,
                        isSelected = isSelected,
                        isCorrect = state.isCorrect,
                        onClick = { onOptionSelect(option.id) }
                    )
                }
            }
        }

        if (state.isCorrect == true) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onNextQuestion,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text(
                    text = if (state.currentQuestionIndex == state.questions.lastIndex) "Finish Game 🎉" else "Next Question ➔",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
private fun TabletPictureQuestContent(
    state: PictureQuestUiState.Success,
    gameName: String,
    currentQuestion: Question,
    onBackClick: () -> Unit,
    onAudioToggle: () -> Unit,
    onOptionSelect: (String) -> Unit,
    onNextQuestion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = SproutelleDimens.TabletContentPadding)
            .padding(bottom = SproutelleSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PictureQuestHeader(gameName = gameName, difficulty = state.difficulty, onBackClick = onBackClick)
        PictureQuestProgressBar(
            currentQuestionIndex = state.currentQuestionIndex,
            totalQuestions = state.questions.size,
            completedIndices = state.completedQuestionIndices
        )

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                modifier = Modifier.weight(0.42f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PictureQuestQuestionCard(
                    question = currentQuestion,
                    isAudioMuted = state.isAudioMuted,
                    onAudioToggle = onAudioToggle,
                    isTablet = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Column(
                modifier = Modifier.weight(0.58f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val options = currentQuestion.options
                if (options.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(options) { option ->
                            val isSelected = state.selectedOptionId == option.id
                            PictureAnswerCard(
                                option = option,
                                isSelected = isSelected,
                                isCorrect = state.isCorrect,
                                onClick = { onOptionSelect(option.id) }
                            )
                        }
                    }
                }

                if (state.isCorrect == true) {
                    Button(
                        onClick = onNextQuestion,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(24.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text(
                            text = if (state.currentQuestionIndex == state.questions.lastIndex) "Finish Game 🎉" else "Next Question ➔",
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
}

@Composable
private fun PictureQuestHeader(
    gameName: String,
    difficulty: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBackClick
                )
        ) {
            RoundedIconButton(
                icon = "🏠",
                onClick = onBackClick,
                backgroundColor = Color.White
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("✨", fontSize = 16.sp)
                Text(
                    text = gameName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF0288D1),
                    fontWeight = FontWeight.Black
                )
                Text("✨", fontSize = 16.sp)
            }
            Text(
                text = "Look closely. Find the right picture!",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF455A64),
                fontWeight = FontWeight.Medium
            )
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFFF176),
            shadowElevation = 2.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("⭐", fontSize = 12.sp)
                Text(
                    text = difficulty,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF5D4037),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PictureQuestProgressBar(
    currentQuestionIndex: Int,
    totalQuestions: Int,
    completedIndices: Set<Int>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question ${currentQuestionIndex + 1} of $totalQuestions",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1B365D),
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalQuestions) { index ->
                    when {
                        completedIndices.contains(index) -> {
                            Box(
                                modifier = Modifier.size(20.dp).background(Color(0xFF4CAF50), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        index == currentQuestionIndex -> {
                            Box(
                                modifier = Modifier.size(22.dp).background(Color(0xFF0288D1), shape = CircleShape).border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(Color.White, shape = CircleShape))
                            }
                        }
                        else -> {
                            Box(modifier = Modifier.size(16.dp).background(Color(0xFFCFD8DC), shape = CircleShape))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PictureQuestQuestionCard(
    question: Question,
    isAudioMuted: Boolean,
    onAudioToggle: () -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().height(if (isTablet) 200.dp else 160.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(end = 12.dp, top = 4.dp)) {
                Text("🦊", fontSize = if (isTablet) 42.sp else 34.sp)
            }

            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.size(if (isTablet) 72.dp else 60.dp).background(Color(0xFFE1F5FE), shape = RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    QuestionImageIllustration(imageUrl = question.imageUrl, text = question.text)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Which one is a",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF0288D1),
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = question.text.uppercase(),
                        style = if (isTablet) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF0D47A1),
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(if (isAudioMuted) Color(0xFFFFCC80) else Color(0xFF29B6F6), shape = CircleShape)
                        .clickable { onAudioToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isAudioMuted) "🔇" else "🔊",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PictureAnswerCard(
    option: QuestionOption,
    isSelected: Boolean,
    isCorrect: Boolean?,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected && isCorrect == true -> Color(0xFF4CAF50)
            isSelected && isCorrect == false -> Color(0xFFE53935)
            else -> Color(0xFFE0E0E0)
        }
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected && isCorrect == true -> Color(0xFFE8F5E9)
            isSelected && isCorrect == false -> Color(0xFFFFEBEE)
            else -> Color.White
        }
    )

    val pictureEmoji = getPictureEmoji(option.text)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (!option.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = option.imageUrl,
                    contentDescription = option.text,
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(pictureEmoji, fontSize = 42.sp)
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(if (isCorrect == true) Color(0xFF4CAF50) else Color(0xFFE53935), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCorrect == true) "✓" else "✕",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PictureQuestFeedbackOverlay(
    isCorrect: Boolean,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(0.3f) }

    LaunchedEffect(isCorrect) {
        scaleAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Box(
        modifier = modifier.background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        val feedbackImage = if (isCorrect) Res.drawable.fox_great_job else Res.drawable.fox_try_again

        Image(
            painter = painterResource(feedbackImage),
            contentDescription = if (isCorrect) "Great Job!" else "Try Again!",
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(2f)
                .scale(scaleAnim.value),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun QuestionImageIllustration(imageUrl: String?, text: String) {
    var isError by remember { mutableStateOf(false) }

    if (!imageUrl.isNullOrBlank() && !isError) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Question Image",
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Fit,
            onError = { isError = true }
        )
    }

    if (imageUrl.isNullOrBlank() || isError) {
        Text(getPictureEmoji(text), fontSize = 32.sp)
    }
}

private fun getPictureEmoji(text: String): String {
    val clean = text.lowercase()
    return when {
        clean.contains("mushroom") -> "🍄"
        clean.contains("chili") || clean.contains("pepper") -> "🌶️"
        clean.contains("pumpkin") -> "🎃"
        clean.contains("pear") -> "🍐"
        clean.contains("orange") -> "🍊"
        clean.contains("mango") -> "🥭"
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
        clean.contains("star") -> "⭐"
        clean.contains("ball") -> "⚽"
        else -> "🖼️"
    }
}

// ==========================================
// PREVIEWS WITH REALISTIC MOCK DATA
// ==========================================

@Preview(name = "Mobile Portrait - Question Unanswered", device = "spec:width=411dp,height=891dp", showBackground = true)
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
            gameName = "Picture Quest",
            currentQuestion = mockState.questions[0],
            isTablet = false,
            onBackClick = {},
            onAudioToggle = {},
            onOptionSelect = {},
            onNextQuestion = {}
        )
    }
}

@Preview(name = "Mobile Portrait - Answer Correct", device = "spec:width=411dp,height=891dp", showBackground = true)
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
            gameName = "Picture Quest",
            currentQuestion = mockState.questions[0],
            isTablet = false,
            onBackClick = {},
            onAudioToggle = {},
            onOptionSelect = {},
            onNextQuestion = {}
        )
    }
}

@Preview(name = "Mobile Portrait - Answer Incorrect", device = "spec:width=411dp,height=891dp", showBackground = true)
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
            gameName = "Picture Quest",
            currentQuestion = mockState.questions[0],
            isTablet = false,
            onBackClick = {},
            onAudioToggle = {},
            onOptionSelect = {},
            onNextQuestion = {}
        )
    }
}

@Preview(name = "Tablet Landscape - Question Unanswered", device = "spec:width=1280dp,height=800dp,orientation=landscape", showBackground = true)
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
            gameName = "Picture Quest",
            currentQuestion = mockState.questions[0],
            isTablet = true,
            onBackClick = {},
            onAudioToggle = {},
            onOptionSelect = {},
            onNextQuestion = {}
        )
    }
}

@Preview(name = "Tablet Landscape - Answer Correct", device = "spec:width=1280dp,height=800dp,orientation=landscape", showBackground = true)
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
            gameName = "Picture Quest",
            currentQuestion = mockState.questions[0],
            isTablet = true,
            onBackClick = {},
            onAudioToggle = {},
            onOptionSelect = {},
            onNextQuestion = {}
        )
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
