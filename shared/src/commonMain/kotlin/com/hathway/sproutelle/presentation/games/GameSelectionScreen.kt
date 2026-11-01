package com.hathway.sproutelle.presentation.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hathway.sproutelle.core.responsive.ProvideWindowSizeClass
import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.presentation.components.EmptyState
import com.hathway.sproutelle.presentation.components.ErrorState
import com.hathway.sproutelle.presentation.components.GameCard
import com.hathway.sproutelle.presentation.components.LoadingState
import com.hathway.sproutelle.presentation.components.ScreenHeader
import theme.SproutelleColors
import theme.SproutelleDimens
import theme.SproutelleSpacing

@Composable
fun GameSelectionScreen(
    viewModel: GameSelectionViewModel,
    onBackClick: () -> Unit,
    onGameClick: (gameId: String, subcategoryId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProvideWindowSizeClass { windowSize ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SproutelleColors.Cream
        ) {
            Scaffold(
                topBar = {
                    ScreenHeader(
                        title = viewModel.subcategoryName,
                        onBackClick = onBackClick,
                        onRefreshClick = viewModel::refresh
                    )
                },
                containerColor = SproutelleColors.Cream
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (val state = uiState) {
                        is GameSelectionUiState.Loading -> {
                            LoadingState(message = "Loading games...")
                        }
                        is GameSelectionUiState.Error -> {
                            ErrorState(
                                message = state.message,
                                onRetryClick = viewModel::refresh
                            )
                        }
                        is GameSelectionUiState.Empty -> {
                            EmptyState(
                                message = "No games published yet for this topic",
                                iconEmoji = "🎮",
                                actionLabel = "Refresh",
                                onActionClick = viewModel::refresh
                            )
                        }
                        is GameSelectionUiState.Success -> {
                            GamesContent(
                                games = state.games,
                                isTablet = windowSize.isTablet,
                                onGameClick = { game -> onGameClick(game.gameId, game.subcategoryId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GamesContent(
    games: List<GameItem>,
    isTablet: Boolean,
    onGameClick: (GameItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SproutelleDimens.MobileContentPadding),
        contentPadding = PaddingValues(bottom = SproutelleSpacing.XXLarge),
        verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Medium)
    ) {
        item {
            Text(
                text = "Select a Game",
                style = MaterialTheme.typography.headlineSmall,
                color = SproutelleColors.Ink,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = SproutelleSpacing.Small)
            )
        }

        items(games.size) { index ->
            GameCard(
                game = games[index],
                isTablet = isTablet,
                onClick = { onGameClick(games[index]) }
            )
        }
    }
}
