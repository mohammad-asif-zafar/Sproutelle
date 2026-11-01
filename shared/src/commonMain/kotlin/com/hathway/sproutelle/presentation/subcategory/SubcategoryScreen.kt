package com.hathway.sproutelle.presentation.subcategory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hathway.sproutelle.core.responsive.ProvideWindowSizeClass
import org.jetbrains.compose.resources.painterResource

import com.hathway.sproutelle.domain.model.Subcategory
import com.hathway.sproutelle.presentation.components.EmptyState
import com.hathway.sproutelle.presentation.components.ErrorState
import com.hathway.sproutelle.presentation.components.LoadingState
import com.hathway.sproutelle.presentation.components.RoundedIconButton
import sproutelle.shared.generated.resources.Res
import sproutelle.shared.generated.resources.Subcategories_bg_mobile
import sproutelle.shared.generated.resources.Subcategories_bg_tablet
import theme.SproutelleColors
import theme.SproutelleDimens
import theme.SproutelleSpacing

/**
 * Platform-agnostic hex color parser for Compose Multiplatform (Android & iOS).
 * Safely parses "#RRGGBB" or "#AARRGGBB" formats without using Android SDK graphics packages.
 */
fun String.toComposeColor(): Color {
    return try {
        val cleanColor = this.removePrefix("#").trim()
        when (cleanColor.length) {
            6 -> {
                Color(
                    red = cleanColor.substring(0, 2).toInt(16) / 255f,
                    green = cleanColor.substring(2, 4).toInt(16) / 255f,
                    blue = cleanColor.substring(4, 6).toInt(16) / 255f,
                    alpha = 1.0f
                )
            }
            8 -> {
                Color(
                    alpha = cleanColor.substring(0, 2).toInt(16) / 255f,
                    red = cleanColor.substring(2, 4).toInt(16) / 255f,
                    green = cleanColor.substring(4, 6).toInt(16) / 255f,
                    blue = cleanColor.substring(6, 8).toInt(16) / 255f
                )
            }
            else -> Color(0xFFE2F0D9)
        }
    } catch (e: Exception) {
        Color(0xFFE2F0D9)
    }
}

@Composable
fun SubcategoryScreen(
    viewModel: SubcategoryViewModel,
    onBackClick: () -> Unit,
    onSubcategoryClick: (Subcategory) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProvideWindowSizeClass { windowSize ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SproutelleColors.Cream
        ) {
            when (val state = uiState) {
                is SubcategoryUiState.Loading -> {
                    LoadingState(message = "Loading topics...")
                }
                is SubcategoryUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetryClick = viewModel::refresh
                    )
                }
                is SubcategoryUiState.Empty -> {
                    EmptyState(
                        message = "No topics available yet in this category",
                        iconEmoji = "🌱",
                        actionLabel = "Refresh",
                        onActionClick = viewModel::refresh
                    )
                }
                is SubcategoryUiState.Success -> {
                    if (windowSize.isTablet) {
                        TabletSubcategoryContent(
                            categoryTitle = viewModel.explorerTitle,
                            subcategories = state.subcategories,
                            onBackClick = onBackClick,
                            onSubcategoryClick = onSubcategoryClick
                        )
                    } else {
                        MobileSubcategoryContent(
                            categoryTitle = viewModel.explorerTitle,
                            subcategories = state.subcategories,
                            onBackClick = onBackClick,
                            onSubcategoryClick = onSubcategoryClick
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun MobileSubcategoryContent(
    categoryTitle: String,
    subcategories: List<Subcategory>,
    onBackClick: () -> Unit,
    onSubcategoryClick: (Subcategory) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(Res.drawable.Subcategories_bg_mobile),
            contentDescription = "Category Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top 20% empty space showing the background image with back button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.30f)
                    .padding(start = 16.dp, top = 12.dp)
            ) {
                RoundedIconButton(
                    icon = "←",
                    onClick = onBackClick
                )
            }

            // Content below the 20% empty space
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = SproutelleDimens.MobileContentPadding)
            ) {
                Text(
                    text = categoryTitle,
                    style = MaterialTheme.typography.headlineMedium,
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
                        count = subcategories.size,
                        key = { index -> subcategories[index].id }
                    ) { index ->
                        MobileSubcategoryCard(
                            item = subcategories[index],
                            onClick = { onSubcategoryClick(subcategories[index]) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabletSubcategoryContent(
    categoryTitle: String,
    subcategories: List<Subcategory>,
    onBackClick: () -> Unit,
    onSubcategoryClick: (Subcategory) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(Res.drawable.Subcategories_bg_tablet),
            contentDescription = "Category Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top 20% empty space for tablet banner visibility with back button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.30f)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                RoundedIconButton(
                    icon = "←",
                    onClick = onBackClick
                )
            }

            // Content below
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(SproutelleDimens.TabletContentPadding)
                    .padding(top = 0.dp)
            ) {
                Text(
                    text = categoryTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = SproutelleColors.Ink,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = SproutelleSpacing.Medium)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(SproutelleSpacing.Medium),
                    verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Medium),
                    contentPadding = PaddingValues(bottom = SproutelleSpacing.XXLarge)
                ) {
                    items(
                        count = subcategories.size,
                        key = { index -> subcategories[index].id }
                    ) { index ->
                        TabletSubcategoryCard(
                            item = subcategories[index],
                            onClick = { onSubcategoryClick(subcategories[index]) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubcategoryCardIllustration(imageUrl: String?, title: String, size: Dp) {
    var isError by remember { mutableStateOf(false) }

    if (!imageUrl.isNullOrBlank() && !isError) {
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit,
            onError = { isError = true }
        )
    }

    if (imageUrl.isNullOrBlank() || isError) {
        Text(
            text = "🌱",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun MobileSubcategoryCard(
    item: Subcategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(item.backgroundColor.toComposeColor()),
                contentAlignment = Alignment.Center
            ) {
                SubcategoryCardIllustration(
                    imageUrl = item.imageUrl,
                    title = item.name,
                    size = 68.dp
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ages 3–8",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun TabletSubcategoryCard(
    item: Subcategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(item.backgroundColor.toComposeColor()),
                contentAlignment = Alignment.Center
            ) {
                SubcategoryCardIllustration(
                    imageUrl = item.imageUrl,
                    title = item.name,
                    size = 86.dp
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ages 3–8",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }
    }
}



// ==========================================
// MOCK DATA & COMPOSE PREVIEWS AREA
// ==========================================

private val KeyboardArrowRight: ImageVector
    get() = ImageVector.Builder(
        name = "AutoMirrored.Filled.KeyboardArrowRight",
        defaultWidth = 24.0.dp,
        defaultHeight = 24.0.dp,
        viewportWidth = 24.0f,
        viewportHeight = 24.0f,
        autoMirror = true
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1.0f,
            stroke = null,
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(8.59f, 16.59f)
            lineTo(13.17f, 12.0f)
            lineTo(8.59f, 7.41f)
            lineTo(10.0f, 6.0f)
            lineToRelative(6.0f, 6.0f)
            lineToRelative(-6.0f, 6.0f)
            lineToRelative(-1.41f, -1.41f)
            close()
        }
    }.build()

object MockSubcategoryData {
    val sampleTitle = "Nature & Gardening"
    val sampleList = listOf(
        Subcategory("1", "exp_1", "Indoor Plants", null, "#E2F0D9", null, 1, true),
        Subcategory("2", "exp_1", "Soil & Nutrition", null, "#FCE4D6", null, 2, true),
        Subcategory("3", "exp_1", "Pest Management", null, "#FFF2CC", null, 3, true),
        Subcategory("4", "exp_1", "Hydroponics", null, "#E1F5FE", null, 4, true),
        Subcategory("5", "exp_1", "Seasonal Blooms", null, "#F8BBD0", null, 5, true)
    )
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Mobile - Subcategory Content",
    showBackground = true,
    device = "spec:width=360dp,height=800dp,dpi=440"
)
@Composable
fun MobileSubcategoryContentPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            MobileSubcategoryContent(
                categoryTitle = MockSubcategoryData.sampleTitle,
                subcategories = MockSubcategoryData.sampleList,
                onBackClick = {},
                onSubcategoryClick = {}
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Tablet - Subcategory Content",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240"
)
@Composable
fun TabletSubcategoryContentPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            TabletSubcategoryContent(
                categoryTitle = MockSubcategoryData.sampleTitle,
                subcategories = MockSubcategoryData.sampleList,
                onBackClick = {},
                onSubcategoryClick = {}
            )
        }
    }
}