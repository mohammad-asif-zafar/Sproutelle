package com.hathway.sproutelle.presentation.games

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hathway.sproutelle.core.responsive.ProvideWindowSizeClass
import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.presentation.components.EmptyState
import com.hathway.sproutelle.presentation.components.ErrorState
import com.hathway.sproutelle.presentation.components.GameCard
import com.hathway.sproutelle.presentation.components.LoadingState
import com.hathway.sproutelle.presentation.components.RoundedIconButton
import org.jetbrains.compose.resources.painterResource
import sproutelle.shared.generated.resources.Res
import sproutelle.shared.generated.resources.Subcategories_bg_mobile
import sproutelle.shared.generated.resources.Subcategories_bg_tablet
import theme.SproutelleColors
import theme.SproutelleDimens
import theme.SproutelleSpacing

@Composable
fun GamesScreen(
    viewModel: GamesViewModel,
    onBackClick: () -> Unit,
    onGameClick: (gameId: String, subcategoryId: String, gameName: String, subcategoryName: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProvideWindowSizeClass { windowSize ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SproutelleColors.Cream
        ) {
            when (val state = uiState) {
                is GamesUiState.Loading -> {
                    LoadingState(message = "Loading games...")
                }
                is GamesUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetryClick = viewModel::refresh
                    )
                }
                is GamesUiState.Empty -> {
                    GamesEmptyState(onRefresh = viewModel::refresh)
                }
                is GamesUiState.Success -> {
                    val allGamesWithSubcategory = remember(state.subcategoryWithGames) {
                        state.subcategoryWithGames
                            .flatMap { group -> group.games.map { game -> game to group.subcategory.name } }
                            .distinctBy { (game, _) -> game.assignmentId.ifEmpty { "${game.subcategoryId}_${game.gameId}" } }
                    }

                    if (allGamesWithSubcategory.isEmpty()) {
                        GamesEmptyState(onRefresh = viewModel::refresh)
                    } else {
                        GamesContent(
                            explorerTitle = viewModel.explorerTitle,
                            allGamesWithSubcategory = allGamesWithSubcategory,
                            isTablet = windowSize.isTablet,
                            onBackClick = onBackClick,
                            onGameClick = onGameClick
                        )
                    }
                }
            }
        }
    }
}

/**
 * Unified content view displaying all games in a single collection without subcategory headers.
 */
@Composable
private fun GamesContent(
    explorerTitle: String,
    allGamesWithSubcategory: List<Pair<GameItem, String>>,
    isTablet: Boolean,
    onBackClick: () -> Unit,
    onGameClick: (gameId: String, subcategoryId: String, gameName: String, subcategoryName: String) -> Unit
) {
    val backgroundImage = if (isTablet) Res.drawable.Subcategories_bg_tablet else Res.drawable.Subcategories_bg_mobile
    val contentPadding = if (isTablet) SproutelleDimens.TabletContentPadding else SproutelleDimens.MobileContentPadding
    val titleStyle = if (isTablet) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium
    val headerPadding = if (isTablet) PaddingValues(horizontal = 24.dp, vertical = 16.dp) else PaddingValues(start = 16.dp, top = 12.dp)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(backgroundImage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top ~20% empty header area displaying the background illustration with the back button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(if (isTablet) 0.25f else 0.20f)
                    .padding(headerPadding),
                contentAlignment = Alignment.TopStart
            ) {
                RoundedIconButton(
                    icon = "←",
                    onClick = onBackClick
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = contentPadding)
            ) {
                Text(
                    text = explorerTitle,
                    style = titleStyle,
                    color = SproutelleColors.Ink,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = SproutelleSpacing.Medium)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = SproutelleSpacing.XXLarge),
                    verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Medium)
                ) {
                    items(
                        items = allGamesWithSubcategory,
                        key = { (game, _) -> game.assignmentId.ifEmpty { "${game.subcategoryId}_${game.gameId}" } }
                    ) { (game, subcategoryName) ->
                        GameCard(
                            game = game,
                            isTablet = isTablet,
                            onClick = {
                                onGameClick(game.gameId, game.subcategoryId, game.name, subcategoryName)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GamesEmptyState(onRefresh: () -> Unit) {
    EmptyState(
        message = "No games available yet in this explorer",
        iconEmoji = "🎮",
        actionLabel = "Refresh",
        onActionClick = onRefresh
    )
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview(name = "Phone Format", device = "spec:width=411dp,height=891dp", showBackground = true)
@Composable
private fun MobileGamesContentPreview() {
    MaterialTheme {
        Surface(color = SproutelleColors.Cream) {
            GamesContent(
                explorerTitle = "Math Quest",
                allGamesWithSubcategory = getMockGamesData(),
                isTablet = false,
                onBackClick = {},
                onGameClick = { _, _, _, _ -> }
            )
        }
    }
}


@Preview(name = "Tablet Format", device = "spec:width=1280dp,height=800dp", showBackground = true)
@Composable
private fun TabletGamesContentPreview() {
    MaterialTheme {
        Surface(color = SproutelleColors.Cream) {
            GamesContent(
                explorerTitle = "Math Quest Explorer",
                allGamesWithSubcategory = getMockGamesData(),
                isTablet = true,
                onBackClick = {},
                onGameClick = { _, _, _, _ -> }
            )
        }
    }
}

private fun getMockGamesData() = listOf(
    GameItem(
        assignmentId = "a1",
        subcategoryId = "sub_1",
        gameId = "1",
        name = "Counting Stars",
        slug = "counting-stars",
        description = null,
        gameType = "quiz",
        isPublished = true,
        sortOrder = 1,
        isActive = true
    ) to "Numbers",
    GameItem(
        assignmentId = "a2",
        subcategoryId = "sub_1",
        gameId = "2",
        name = "Addition Puzzle",
        slug = "addition-puzzle",
        description = null,
        gameType = "quiz",
        isPublished = true,
        sortOrder = 2,
        isActive = true
    ) to "Numbers"
)
