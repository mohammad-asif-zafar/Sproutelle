package com.hathway.sproutelle.presentation.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.hathway.sproutelle.core.responsive.ProvideWindowSizeClass
import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.presentation.components.EmptyState
import com.hathway.sproutelle.presentation.components.ErrorState
import com.hathway.sproutelle.presentation.components.LoadingState
import com.hathway.sproutelle.presentation.components.RoundedIconButton
import kotlinx.coroutines.delay
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
    onGameClick: (gameId: String, subcategoryId: String, gameName: String, subcategoryName: String) -> Unit,
    explorerSubtitle: String? = "Create, colour and explore!"
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProvideWindowSizeClass { windowSize ->
        GamesScreenContent(
            uiState = uiState,
            explorerTitle = viewModel.explorerTitle,
            explorerSubtitle = explorerSubtitle,
            isTablet = windowSize.isTablet,
            onRefresh = viewModel::refresh,
            onBackClick = onBackClick,
            onGameClick = onGameClick
        )
    }
}

@Composable
fun GamesScreenContent(
    uiState: GamesUiState,
    explorerTitle: String,
    explorerSubtitle: String? = null,
    isTablet: Boolean,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
    onGameClick: (gameId: String, subcategoryId: String, gameName: String, subcategoryName: String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(), color = SproutelleColors.Cream
    ) {
        when (uiState) {
            is GamesUiState.Loading -> {
                LoadingState(message = "Loading games...")
            }

            is GamesUiState.Error -> {
                ErrorState(
                    message = uiState.message, onRetryClick = onRefresh
                )
            }

            is GamesUiState.Empty -> {
                GamesEmptyState(onRefresh = onRefresh)
            }

            is GamesUiState.Success -> {
                val allGamesWithSubcategory = remember(uiState.subcategoryWithGames) {
                    uiState.subcategoryWithGames.flatMap { group -> group.games.map { game -> game to group.subcategory.name } }
                        .distinctBy { (game, _) -> game.assignmentId.ifEmpty { "${game.subcategoryId}_${game.gameId}" } }
                }

                if (allGamesWithSubcategory.isEmpty()) {
                    GamesEmptyState(onRefresh = onRefresh)
                } else {
                    GamesContent(
                        explorerTitle = explorerTitle,
                        explorerSubtitle = explorerSubtitle,
                        allGamesWithSubcategory = allGamesWithSubcategory,
                        isTablet = isTablet,
                        onBackClick = onBackClick,
                        onGameClick = onGameClick
                    )
                }
            }
        }
    }
}

@Composable
private fun GamesContent(
    explorerTitle: String,
    explorerSubtitle: String?,
    allGamesWithSubcategory: List<Pair<GameItem, String>>,
    isTablet: Boolean,
    onBackClick: () -> Unit,
    onGameClick: (gameId: String, subcategoryId: String, gameName: String, subcategoryName: String) -> Unit
) {
    val backgroundImage =
        if (isTablet) Res.drawable.Subcategories_bg_tablet else Res.drawable.Subcategories_bg_mobile
    val contentPadding =
        if (isTablet) SproutelleDimens.TabletContentPadding else SproutelleDimens.MobileContentPadding

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(backgroundImage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding()
        ) {
            // Top Navigation Row with generous toddler touch area
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = if (isTablet) 24.dp else 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundedIconButton(
                    icon = "←", onClick = onBackClick
                )
            }

            // Responsive sky spacing above the cloud title banner
            Spacer(modifier = Modifier.height(if (isTablet) 24.dp else 36.dp))

            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = contentPadding)
            ) {
                // Playful White Cloud Banner matching Creative Studio reference
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .padding(bottom = if (isTablet) 24.dp else 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(36.dp),
                        color = Color.White,
                        border = BorderStroke(2.5.dp, Color(0xFFE1F5FE)),
                        shadowElevation = 6.dp
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(
                                horizontal = if (isTablet) 36.dp else 28.dp,
                                vertical = if (isTablet) 14.dp else 10.dp
                            )
                        ) {
                            Text(
                                text = explorerTitle,
                                style = (if (isTablet) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium).copy(
                                    fontSize = if (isTablet) 30.sp else 24.sp
                                ),
                                color = Color(0xFF1B365D),
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (!explorerSubtitle.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = explorerSubtitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = if (isTablet) 15.sp else 12.sp
                                    ),
                                    color = Color(0xFF546E7A),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Adaptive Game Rows (1 column on phone portrait, 2 columns on tablet)
                LazyVerticalGrid(
                    columns = if (isTablet) GridCells.Fixed(2) else GridCells.Fixed(1),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = SproutelleSpacing.XXLarge),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp)
                ) {
                    itemsIndexed(
                        items = allGamesWithSubcategory,
                        key = { _, (game) -> game.assignmentId.ifEmpty { "${game.subcategoryId}${game.gameId}" } }) { index, (game, subcategoryName) ->
                        GameListRowCard(
                            game = game, index = index, isTablet = isTablet, onClick = {
                                onGameClick(
                                    game.gameId, game.subcategoryId, game.name, subcategoryName
                                )
                            })
                    }
                }
            }
        }
    }
}

private data class GameRowPalette(
    val containerColor: Color,
    val borderColor: Color,
    val shadowRimColor: Color,
    val iconTileBg: Color,
    val fallbackEmoji: String
)

private val StudioRowPalettes = listOf(
    GameRowPalette(
        containerColor = Color(0xFFFFF8E7), // Warm Cream (Picture Quest)
        borderColor = Color(0xFFFFE0B2),
        shadowRimColor = Color(0xFFE6C896),
        iconTileBg = Color(0xFFE1F5FE),
        fallbackEmoji = "🖼️"
    ), GameRowPalette(
        containerColor = Color(0xFFF3EEFF), // Soft Lavender (Word Builder)
        borderColor = Color(0xFFD1C4E9),
        shadowRimColor = Color(0xFFB39DDB),
        iconTileBg = Color(0xFFEDE7F6),
        fallbackEmoji = "🔤"
    ), GameRowPalette(
        containerColor = Color(0xFFE6F4FF), // Sky Blue (Match & Discover)
        borderColor = Color(0xFFB3E5FC),
        shadowRimColor = Color(0xFF81D4FA),
        iconTileBg = Color(0xFFE0F7FA),
        fallbackEmoji = "🍎"
    ), GameRowPalette(
        containerColor = Color(0xFFFCEBF3), // Blush Pink (Picture Finder)
        borderColor = Color(0xFFF8BBD0),
        shadowRimColor = Color(0xFFF48FB1),
        iconTileBg = Color(0xFFFCE4EC),
        fallbackEmoji = "🎧"
    ), GameRowPalette(
        containerColor = Color(0xFFE8F7EB), // Mint Green (Number Quest)
        borderColor = Color(0xFFC8E6C9),
        shadowRimColor = Color(0xFFA5D6A7),
        iconTileBg = Color(0xFFE8F5E9),
        fallbackEmoji = "🔢"
    )
)

@Composable
private fun GameListRowCard(
    game: GameItem, index: Int, isTablet: Boolean, onClick: () -> Unit
) {
// Staggered spring entrance animation
    val entranceScale = remember(game.assignmentId, game.gameId) { Animatable(0.88f) }
    LaunchedEffect(game.assignmentId, game.gameId) {
        delay(index * 55L)
        entranceScale.animateTo(
            targetValue = 1f, animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
        ), label = "GameRowPressScale"
    )
    val palette = StudioRowPalettes[index % StudioRowPalettes.size]
    val cardHeight = if (isTablet) 96.dp else 82.dp
    val cornerRadius = if (isTablet) 28.dp else 24.dp
    val ageLabel = remember(game.minAge, game.maxAge) {
        val min = game.minAge ?: 3
        val max = game.maxAge ?: 8
        "Ages $min – $max"
    }
    val badgeEmoji = remember(game.slug, game.gameType, index) {
        val key = "${game.slug}_${game.gameType}".lowercase()
        when {
            key.contains("picture_quest") || key.contains("image") -> "🖼️"
            key.contains("word") || key.contains("spell") -> "🔤"
            key.contains("match") -> "🍎"
            key.contains("finder") || key.contains("audio") -> "🎧"
            key.contains("number") || key.contains("count") -> "🔢"
            else -> palette.fallbackEmoji
        }
    }
    Box(
        modifier = Modifier.fillMaxWidth().height(cardHeight + 4.dp).graphicsLayer {
            val combined = entranceScale.value * pressScale
            scaleX = combined
            scaleY = combined
        }.clickable(
            interactionSource = interactionSource, indication = null, onClick = onClick
        )
    ) {
// 3D Bottom Rim Shadow
        Box(
            modifier = Modifier.fillMaxWidth().height(cardHeight).offset(y = 4.dp)
                .background(palette.shadowRimColor, shape = RoundedCornerShape(cornerRadius))
        )
// Foreground Pastel Pill Card
        Surface(
            modifier = Modifier.fillMaxWidth().height(cardHeight),
            shape = RoundedCornerShape(cornerRadius),
            color = palette.containerColor,
            border = BorderStroke(2.dp, palette.borderColor),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
                    .padding(horizontal = if (isTablet) 16.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp)
            ) {
// Left Rounded Icon Tile
                Surface(
                    modifier = Modifier.size(if (isTablet) 64.dp else 56.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = palette.iconTileBg,
                    border = BorderStroke(2.dp, Color.White),
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val iconUrl = game.thumbnailUrl
                        var imageLoadFailed by remember(iconUrl) { mutableStateOf(false) }
                        if (!iconUrl.isNullOrBlank() && !imageLoadFailed) {
                            AsyncImage(
                                model = iconUrl,
                                contentDescription = game.name,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop,
                                onError = { imageLoadFailed = true })
                        } else {
                            Text(
                                text = badgeEmoji, fontSize = if (isTablet) 32.sp else 28.sp
                            )
                        }
                    }
                }
// Center Title + Age Range Pill
                Column(
                    modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = if (isTablet) 20.sp else 17.sp
                        ),
                        color = Color(0xFF1B365D),
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = Color.White.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, palette.borderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "👥", fontSize = 11.sp)
                            Text(
                                text = ageLabel, style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = if (isTablet) 13.sp else 11.sp
                                ), color = Color(0xFF37474F), fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
// Right Glossy 3D Green Play Button
                Box(
                    modifier = Modifier.size(if (isTablet) 52.dp else 46.dp),
                    contentAlignment = Alignment.Center
                ) {
// Darker green bottom rim
                    Box(
                        modifier = Modifier.fillMaxSize().offset(y = 2.5.dp)
                            .background(Color(0xFF2E7D32), CircleShape)
                    )
// Glossy gradient green disc
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF81C784), Color(0xFF43A047)
                                )
                            ), shape = CircleShape
                        ).border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Play ${game.name}",
                            tint = Color.White,
                            modifier = Modifier.size(if (isTablet) 30.dp else 26.dp)
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
@Preview(
    name = "Creative Studio - Phone",
    device = "spec:width=411dp,height=891dp",
    showBackground = true
)
@Composable
private fun MobileGamesContentPreview() {
    MaterialTheme {
        GamesContent(
            explorerTitle = "Creative Studio",
            explorerSubtitle = "Create, colour and explore!",
            allGamesWithSubcategory = getMockGamesData(),
            isTablet = false,
            onBackClick = {},
            onGameClick = { _, _, _, _ -> })
    }
}

@Preview(
    name = "Creative Studio - Tablet",
    device = "spec:width=1280dp,height=800dp",
    showBackground = true
)
@Composable
private fun TabletGamesContentPreview() {
    MaterialTheme {
        GamesContent(
            explorerTitle = "Creative Studio",
            explorerSubtitle = "Create, colour and explore!",
            allGamesWithSubcategory = getMockGamesData(),
            isTablet = true,
            onBackClick = {},
            onGameClick = { _, _, _, _ -> })
    }
}

private fun getMockGamesData() = listOf(
    GameItem(
        assignmentId = "a1",
        subcategoryId = "sub_1",
        gameId = "1",
        name = "Picture Quest",
        slug = "picture-quest",
        description = null,
        gameType = "picture_quest",
        isPublished = true,
        sortOrder = 1,
        isActive = true,
        minAge = 3,
        maxAge = 8
    ) to "Studio", GameItem(
        assignmentId = "a2",
        subcategoryId = "sub_1",
        gameId = "2",
        name = "Word Builder",
        slug = "word-builder",
        description = null,
        gameType = "word_builder",
        isPublished = true,
        sortOrder = 2,
        isActive = true,
        minAge = 3,
        maxAge = 8
    ) to "Studio", GameItem(
        assignmentId = "a3",
        subcategoryId = "sub_1",
        gameId = "3",
        name = "Match & Discover",
        slug = "match-and-discover",
        description = null,
        gameType = "match_and_discover",
        isPublished = true,
        sortOrder = 3,
        isActive = true,
        minAge = 3,
        maxAge = 8
    ) to "Studio", GameItem(
        assignmentId = "a4",
        subcategoryId = "sub_1",
        gameId = "4",
        name = "Picture Finder",
        slug = "picture-finder",
        description = null,
        gameType = "picture_finder",
        isPublished = true,
        sortOrder = 4,
        isActive = true,
        minAge = 3,
        maxAge = 8
    ) to "Studio", GameItem(
        assignmentId = "a5",
        subcategoryId = "sub_1",
        gameId = "5",
        name = "Number Quest",
        slug = "number-quest",
        description = null,
        gameType = "number_quest",
        isPublished = true,
        sortOrder = 5,
        isActive = true,
        minAge = 3,
        maxAge = 8
    ) to "Studio"
)