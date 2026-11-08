package com.hathway.sproutelle.presentation.question

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import theme.SproutelleDimens
import theme.SproutelleShapes
import theme.SproutelleSpacing

@Composable
fun QuestionScreen(
    viewModel: QuestionViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProvideWindowSizeClass { windowSize ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFE0F7FA)
        ) {
            when (val state = uiState) {
                is QuestionUiState.Loading -> {
                    LoadingState(message = "Loading Picture Quest...")
                }
                is QuestionUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetryClick = viewModel::refresh
                    )
                }
                is QuestionUiState.Empty -> {
                    EmptyState(
                        message = "No questions available for Picture Quest yet",
                        iconEmoji = "🖼️",
                        actionLabel = "Refresh",
                        onActionClick = viewModel::refresh
                    )
                }
                is QuestionUiState.Success -> {
                    val currentQuestion = state.questions.getOrNull(state.currentQuestionIndex)
                    if (currentQuestion == null) {
                        EmptyState(
                            message = "No questions available for Picture Quest yet",
                            iconEmoji = "🖼️",
                            actionLabel = "Refresh",
                            onActionClick = viewModel::refresh
                        )
                    } else {
                        if (windowSize.isTablet) {
                            TabletPictureQuestContent(
                                state = state,
                                gameName = viewModel.gameName,
                                currentQuestion = currentQuestion,
                                onBackClick = onBackClick,
                                onAudioToggle = viewModel::toggleAudioMute,
                                onOptionSelect = viewModel::selectOption,
                                onNextQuestion = viewModel::nextQuestion
                            )
                        } else {
                            MobilePictureQuestContent(
                                state = state,
                                gameName = viewModel.gameName,
                                currentQuestion = currentQuestion,
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
}

/**
 * Mobile layout for Picture Quest.
 */
@Composable
private fun MobilePictureQuestContent(
    state: QuestionUiState.Success,
    gameName: String,
    currentQuestion: Question,
    onBackClick: () -> Unit,
    onAudioToggle: () -> Unit,
    onOptionSelect: (String) -> Unit,
    onNextQuestion: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF81D4FA),
                        Color(0xFFE0F7FA),
                        Color(0xFFFFF9E6)
                    )
                )
            )
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
            // 1 & 2. Branding Header & Difficulty Badge
            PictureQuestHeader(
                gameName = gameName,
                difficulty = state.difficulty,
                onBackClick = onBackClick
            )

            // 3. Question Progress Component
            PictureQuestProgressBar(
                currentQuestionIndex = state.currentQuestionIndex,
                totalQuestions = state.questions.size,
                completedIndices = state.completedQuestionIndices
            )

            // 4. Question Panel with Mascot and Audio Toggle
            PictureQuestQuestionCard(
                question = currentQuestion,
                isAudioMuted = state.isAudioMuted,
                onAudioToggle = onAudioToggle,
                isTablet = false
            )

            // Feedback Overlay Banner ("Great Job!" or "Try Again!")
            if (state.isCorrect != null) {
                PictureQuestFeedbackBanner(isCorrect = state.isCorrect)
            }

            // 5. Picture Answer Cards Grid (6 Picture Options)
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

            // Next Question Button when answer is correct
            if (state.isCorrect == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onNextQuestion,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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

/**
 * Tablet landscape composition for Picture Quest.
 */
@Composable
private fun TabletPictureQuestContent(
    state: QuestionUiState.Success,
    gameName: String,
    currentQuestion: Question,
    onBackClick: () -> Unit,
    onAudioToggle: () -> Unit,
    onOptionSelect: (String) -> Unit,
    onNextQuestion: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF81D4FA),
                        Color(0xFFE0F7FA),
                        Color(0xFFFFF9E6)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = SproutelleDimens.TabletContentPadding)
                .padding(bottom = SproutelleSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header & Progress across top
            PictureQuestHeader(
                gameName = gameName,
                difficulty = state.difficulty,
                onBackClick = onBackClick
            )

            PictureQuestProgressBar(
                currentQuestionIndex = state.currentQuestionIndex,
                totalQuestions = state.questions.size,
                completedIndices = state.completedQuestionIndices
            )

            // Landscape 2-Pane Composition
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left Pane: Question Card Panel + Mascot
                Column(
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PictureQuestQuestionCard(
                        question = currentQuestion,
                        isAudioMuted = state.isAudioMuted,
                        onAudioToggle = onAudioToggle,
                        isTablet = true,
                        modifier = Modifier.weight(1f)
                    )

                    if (state.isCorrect != null) {
                        PictureQuestFeedbackBanner(isCorrect = state.isCorrect)
                    }
                }

                // Right Pane: 6 Picture Answer Cards in 3 Columns
                Column(
                    modifier = Modifier
                        .weight(0.58f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val options = currentQuestion.options
                    if (options.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
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
}

/**
 * Picture Quest Branding Header with difficulty badge.
 */
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
        // Home Button
        RoundedIconButton(
            icon = "🏠",
            onClick = onBackClick,
            backgroundColor = Color.White
        )

        // Center Title Branding
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("✨", fontSize = 16.sp)
                Text(
                    text = gameName.ifBlank { "Picture Quest" },
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

        // Difficulty Badge
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

/**
 * Horizontal Progress Bar with Checkmarks and Glowing Active Ring.
 */
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
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
                            // Completed Question: Green checkmark circle
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(Color(0xFF4CAF50), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        index == currentQuestionIndex -> {
                            // Active Question: Glowing blue ring
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(Color(0xFF0288D1), shape = CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.White, shape = CircleShape)
                                )
                            }
                        }
                        else -> {
                            // Upcoming Question: Translucent gray dot
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(Color(0xFFCFD8DC), shape = CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Main Question Panel with Fox Mascot, Text, Audio button and Illustration.
 */
@Composable
private fun PictureQuestQuestionCard(
    question: Question,
    isAudioMuted: Boolean,
    onAudioToggle: () -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isTablet) 200.dp else 160.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fox Mascot Peeking over top right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 12.dp, top = 4.dp)
            ) {
                Text("🦊", fontSize = if (isTablet) 42.sp else 34.sp)
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Question Image / Emoji Illustration
                Box(
                    modifier = Modifier
                        .size(if (isTablet) 72.dp else 60.dp)
                        .background(Color(0xFFE1F5FE), shape = RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    QuestionImageIllustration(imageUrl = question.imageUrl, text = question.text)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Center Question Prompt
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

                // Audio Button
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

/**
 * Picture Answer Card for 6 Options.
 */
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Option Picture Image / Illustration
            if (!option.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = option.imageUrl,
                    contentDescription = option.text,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(pictureEmoji, fontSize = 42.sp)
            }

            // Top Right Checkmark or X Badge
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(
                            if (isCorrect == true) Color(0xFF4CAF50) else Color(0xFFE53935),
                            shape = CircleShape
                        ),
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

/**
 * Celebratory "Great Job!" or "Try Again!" Banner Overlay.
 */
@Composable
private fun PictureQuestFeedbackBanner(
    isCorrect: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isCorrect) "🦊" else "🤔",
                fontSize = 36.sp
            )

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = SproutelleShapes.Pill,
                    color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFE53935)
                ) {
                    Text(
                        text = if (isCorrect) "Great Job! ⭐" else "Try Again!",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isCorrect) "You found the right picture!" else "That's not correct. Look again and choose the right picture!",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCorrect) Color(0xFF1B5E20) else Color(0xFFC62828),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun QuestionImageIllustration(imageUrl: String?, text: String) {
    var isError by remember { mutableStateOf(false) }

    if (!imageUrl.isNullOrBlank() && !isError) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Question Image",
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Fit,
            onError = { isError = true }
        )
    }

    if (imageUrl.isNullOrBlank() || isError) {
        Text(getPictureEmoji(text), fontSize = 32.sp)
    }
}

/**
 * Maps picture option titles/texts to colorful picture emojis for picture quest cards.
 */
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
