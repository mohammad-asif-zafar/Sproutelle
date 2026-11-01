package com.hathway.sproutelle.presentation.question

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.hathway.sproutelle.presentation.components.PrimaryButton
import com.hathway.sproutelle.presentation.components.RoundedIconButton
import com.hathway.sproutelle.presentation.components.SproutelleProgressIndicator
import theme.SproutelleColors
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
            color = SproutelleColors.Cream
        ) {
            when (val state = uiState) {
                is QuestionUiState.Loading -> {
                    LoadingState(message = "Loading questions...")
                }
                is QuestionUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetryClick = viewModel::refresh
                    )
                }
                is QuestionUiState.Empty -> {
                    EmptyState(
                        message = "No questions available for this game yet",
                        iconEmoji = "❓",
                        actionLabel = "Refresh",
                        onActionClick = viewModel::refresh
                    )
                }
                is QuestionUiState.Success -> {
                    val currentQuestion = state.questions.getOrNull(state.currentQuestionIndex)
                    if (currentQuestion == null) {
                        EmptyState(
                            message = "No questions available for this game yet",
                            iconEmoji = "❓",
                            actionLabel = "Refresh",
                            onActionClick = viewModel::refresh
                        )
                    } else {
                        Scaffold(
                            topBar = {
                                QuestionHeader(
                                    gameName = viewModel.gameName,
                                    currentQuestionIndex = state.currentQuestionIndex + 1,
                                    totalQuestions = state.questions.size,
                                    isAudioMuted = state.isAudioMuted,
                                    onBackClick = onBackClick,
                                    onAudioToggle = viewModel::toggleAudioMute
                                )
                            },
                            bottomBar = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(SproutelleDimens.MobileContentPadding)
                                ) {
                                    PrimaryButton(
                                        text = if (state.currentQuestionIndex == state.questions.lastIndex) "Finish Game 🎉" else "Next Question ▶",
                                        onClick = viewModel::nextQuestion
                                    )
                                }
                            },
                            containerColor = SproutelleColors.Cream
                        ) { innerPadding ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .padding(horizontal = SproutelleDimens.MobileContentPadding),
                                verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Large),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Question Text & Optional Image Container Card
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (windowSize.isTablet) 220.dp else 170.dp),
                                    colors = CardDefaults.cardColors(containerColor = SproutelleColors.White),
                                    shape = SproutelleShapes.Large,
                                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(SproutelleSpacing.Large),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        QuestionImage(
                                            imageUrl = currentQuestion.imageUrl,
                                            size = if (windowSize.isTablet) 72.dp else 56.dp
                                        )

                                        Spacer(modifier = Modifier.height(SproutelleSpacing.Small))

                                        Text(
                                            text = currentQuestion.text,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = SproutelleColors.Ink,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )

                                        if (!currentQuestion.instructions.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = currentQuestion.instructions,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = SproutelleColors.SecondaryText,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                // Answer Options Grid
                                val options = currentQuestion.options
                                val gridColumns = if (windowSize.isTablet && options.size > 2) 4 else 2

                                if (options.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No options available for this question",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = SproutelleColors.SecondaryText
                                        )
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(gridColumns),
                                        horizontalArrangement = Arrangement.spacedBy(SproutelleSpacing.Medium),
                                        verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Medium),
                                        contentPadding = PaddingValues(vertical = SproutelleSpacing.Small)
                                    ) {
                                        items(options.size) { index ->
                                            val option = options[index]
                                            val isSelected = state.selectedOptionId == option.id
                                            val borderColor by animateColorAsState(
                                                targetValue = when {
                                                    isSelected && state.isCorrect == true -> SproutelleColors.Success
                                                    isSelected && state.isCorrect == false -> SproutelleColors.CoralOrange
                                                    isSelected -> SproutelleColors.SkyBlue
                                                    else -> SproutelleColors.Outline
                                                }
                                            )

                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(110.dp)
                                                    .border(
                                                        width = if (isSelected) 3.dp else 1.dp,
                                                        color = borderColor,
                                                        shape = SproutelleShapes.Large
                                                    )
                                                    .clickable { viewModel.selectOption(option.id) },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) SproutelleColors.Mint else SproutelleColors.White
                                                ),
                                                shape = SproutelleShapes.Large,
                                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        OptionImage(imageUrl = option.imageUrl)
                                                        Text(
                                                            text = option.text,
                                                            style = MaterialTheme.typography.titleMedium,
                                                            color = SproutelleColors.Ink,
                                                            fontWeight = FontWeight.Bold,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier.padding(horizontal = 8.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionHeader(
    gameName: String,
    currentQuestionIndex: Int,
    totalQuestions: Int,
    isAudioMuted: Boolean,
    onBackClick: () -> Unit,
    onAudioToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SproutelleColors.Cream)
            .padding(
                horizontal = SproutelleDimens.MobileContentPadding,
                vertical = SproutelleSpacing.Medium
            ),
        verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundedIconButton(
                icon = "←",
                onClick = onBackClick
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = gameName,
                    style = MaterialTheme.typography.titleLarge,
                    color = SproutelleColors.LeafGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Question $currentQuestionIndex of $totalQuestions",
                    style = MaterialTheme.typography.bodySmall,
                    color = SproutelleColors.SecondaryText
                )
            }

            RoundedIconButton(
                icon = if (isAudioMuted) "🔇" else "🔊",
                onClick = onAudioToggle,
                backgroundColor = if (isAudioMuted) SproutelleColors.Peach else SproutelleColors.Mint
            )
        }

        val progress = if (totalQuestions > 0) currentQuestionIndex.toFloat() / totalQuestions else 0f
        SproutelleProgressIndicator(progress = progress)
    }
}

@Composable
private fun QuestionImage(imageUrl: String?, size: androidx.compose.ui.unit.Dp) {
    var isError by remember { mutableStateOf(false) }

    if (!imageUrl.isNullOrBlank() && !isError) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Question Image",
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit,
            onError = { isError = true }
        )
    }

    if (imageUrl.isNullOrBlank() || isError) {
        Text("❓", fontSize = (size.value * 0.6f).sp)
    }
}

@Composable
private fun OptionImage(imageUrl: String?) {
    var isError by remember { mutableStateOf(false) }

    if (!imageUrl.isNullOrBlank() && !isError) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Option Image",
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit,
            onError = { isError = true }
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}
