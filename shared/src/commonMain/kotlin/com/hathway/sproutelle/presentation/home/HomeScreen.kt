/*
package com.hathway.sproutelle.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hathway.sproutelle.core.responsive.ProvideWindowSizeClass
import com.hathway.sproutelle.domain.model.ExplorerItem
import com.hathway.sproutelle.domain.model.HomeData
import com.hathway.sproutelle.presentation.components.BottomNavigationBar
import com.hathway.sproutelle.presentation.components.CategoryCard
import com.hathway.sproutelle.presentation.components.ErrorState
import com.hathway.sproutelle.presentation.components.LoadingState
import com.hathway.sproutelle.presentation.components.ParentButton
import com.hathway.sproutelle.presentation.components.TabletNavigationRail
import org.jetbrains.compose.resources.painterResource
import sproutelle.shared.generated.resources.Res
import sproutelle.shared.generated.resources.sproutelle_home_landscape
import sproutelle.shared.generated.resources.sproutelle_home_portrait
import theme.SproutelleColors
import theme.SproutelleDimens
import theme.SproutelleShapes
import theme.SproutelleSpacing

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCategoryClick: ((ExplorerItem) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val explorerItems by viewModel.explorerItems.collectAsStateWithLifecycle()
    val explorerError by viewModel.explorerError.collectAsStateWithLifecycle()

    ProvideWindowSizeClass { windowSize ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SproutelleColors.Cream
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    LoadingState(message = "Preparing your garden...")
                }
                is HomeUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetryClick = { viewModel.refresh() }
                    )
                }
                is HomeUiState.Success -> {
                    if (windowSize.isTablet) {
                        TabletHomeContent(
                            data = state.data,
                            explorerItems = explorerItems,
                            explorerError = explorerError,
                            onRefresh = viewModel::refresh,
                            onRetryExplorer = viewModel::loadExplorerItems,
                            onCategoryClick = onCategoryClick
                        )
                    } else {
                        PhoneHomeContent(
                            data = state.data,
                            explorerItems = explorerItems,
                            explorerError = explorerError,
                            onRefresh = viewModel::refresh,
                            onRetryExplorer = viewModel::loadExplorerItems,
                            onCategoryClick = onCategoryClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneHomeContent(
    data: HomeData,
    explorerItems: List<ExplorerItem>,
    explorerError: String?,
    onRefresh: () -> Unit,
    onRetryExplorer: () -> Unit,
    onCategoryClick: ((ExplorerItem) -> Unit)? = null
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SproutelleColors.Cream)
                .padding(innerPadding),
            contentPadding = PaddingValues(SproutelleDimens.MobileContentPadding),
            verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Section)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌱", fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sproutelle",
                            style = MaterialTheme.typography.titleLarge,
                            color = SproutelleColors.LeafGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    ParentButton(onClick = { })
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    shape = SproutelleShapes.Large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(Res.drawable.sproutelle_home_portrait),
                            contentDescription = "Garden Hero",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(SproutelleSpacing.Large),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(SproutelleColors.White, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👧", fontSize = 28.sp)
                            }
                            Column {
                                Text(
                                    text = "Good Morning,",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SproutelleColors.SecondaryText
                                )
                                Text(
                                    text = if (data.userName.isNotBlank()) "${data.userName}!" else "Little Gardener!",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = SproutelleColors.Ink,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Continue Playing",
                    style = MaterialTheme.typography.titleLarge,
                    color = SproutelleColors.Ink,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(SproutelleSpacing.Medium))
                HeaderGreetingCard(data = data)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explore",
                        style = MaterialTheme.typography.titleLarge,
                        color = SproutelleColors.Ink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Refresh",
                        style = MaterialTheme.typography.titleMedium,
                        color = SproutelleColors.LeafGreen,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onRefresh() }
                    )
                }

                if (explorerError != null) {
                    Spacer(modifier = Modifier.height(SproutelleSpacing.Small))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SproutelleColors.Peach)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(SproutelleSpacing.Medium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = explorerError,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SproutelleColors.Error,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = onRetryExplorer,
                                colors = ButtonDefaults.buttonColors(containerColor = SproutelleColors.LeafGreen),
                                shape = SproutelleShapes.Pill
                            ) {
                                Text("Retry", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SproutelleSpacing.Medium))
                CategoriesGrid(items = explorerItems, columns = 2, isTablet = false, onCategoryClick = onCategoryClick)
            }
        }
    }
}

@Composable
private fun TabletHomeContent(
    data: HomeData,
    explorerItems: List<ExplorerItem>,
    explorerError: String?,
    onRefresh: () -> Unit,
    onRetryExplorer: () -> Unit,
    onCategoryClick: ((ExplorerItem) -> Unit)? = null
) {
    var selectedRailItem by remember { mutableStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(SproutelleColors.Cream)
    ) {
        TabletNavigationRail(
            selectedTab = selectedRailItem,
            onTabSelected = { selectedRailItem = it }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(SproutelleDimens.TabletContentPadding),
            verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Section)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌱", fontSize = 30.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sproutelle",
                            style = MaterialTheme.typography.headlineMedium,
                            color = SproutelleColors.LeafGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    ParentButton(onClick = { })
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = SproutelleShapes.Large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(Res.drawable.sproutelle_home_landscape),
                            contentDescription = "Garden Hero Landscape",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(SproutelleSpacing.Large),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(SproutelleColors.White, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👧", fontSize = 36.sp)
                            }
                            Column {
                                Text(
                                    text = "Good Morning,",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = SproutelleColors.SecondaryText
                                )
                                Text(
                                    text = if (data.userName.isNotBlank()) "${data.userName}!" else "Little Gardener!",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = SproutelleColors.Ink,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Continue Playing",
                    style = MaterialTheme.typography.titleLarge,
                    color = SproutelleColors.Ink,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(SproutelleSpacing.Medium))
                Box(modifier = Modifier.fillMaxWidth(0.5f)) {
                    HeaderGreetingCard(data = data)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explore",
                        style = MaterialTheme.typography.titleLarge,
                        color = SproutelleColors.Ink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Refresh",
                        style = MaterialTheme.typography.titleMedium,
                        color = SproutelleColors.LeafGreen,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onRefresh() }
                    )
                }

                if (explorerError != null) {
                    Spacer(modifier = Modifier.height(SproutelleSpacing.Small))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SproutelleColors.Peach)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(SproutelleSpacing.Medium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = explorerError,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SproutelleColors.Error,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = onRetryExplorer,
                                colors = ButtonDefaults.buttonColors(containerColor = SproutelleColors.LeafGreen),
                                shape = SproutelleShapes.Pill
                            ) {
                                Text("Retry", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SproutelleSpacing.Medium))
                CategoriesGrid(items = explorerItems, columns = 3, isTablet = true, onCategoryClick = onCategoryClick)
            }
        }
    }
}

@Composable
private fun HeaderGreetingCard(data: HomeData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SproutelleColors.White),
        shape = SproutelleShapes.Large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SproutelleSpacing.Large),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(SproutelleColors.Mint, shape = SproutelleShapes.Medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🅰️", fontSize = 28.sp)
                }
                Column {
                    Text(
                        text = "Alphabet Fun",
                        style = MaterialTheme.typography.titleLarge,
                        color = SproutelleColors.Ink,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Letters & Sounds",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SproutelleColors.SecondaryText
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(SproutelleColors.LeafGreen, shape = CircleShape)
                    .size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶",
                    style = MaterialTheme.typography.titleMedium,
                    color = SproutelleColors.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CategoriesGrid(
    items: List<ExplorerItem>,
    columns: Int,
    isTablet: Boolean,
    onCategoryClick: ((ExplorerItem) -> Unit)? = null
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No explore categories available",
                style = MaterialTheme.typography.bodyLarge,
                color = SproutelleColors.SecondaryText
            )
        }
        return
    }

    val rows = items.chunked(columns)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Medium)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SproutelleSpacing.Medium)
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        CategoryCard(item = item, isTablet = isTablet, onClick = { onCategoryClick?.invoke(item) })
                    }
                }
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
*/

package com.hathway.sproutelle.presentation.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
import com.hathway.sproutelle.core.responsive.ProvideWindowSizeClass
import com.hathway.sproutelle.domain.model.ActivityItem
import com.hathway.sproutelle.domain.model.ExplorerItem
import com.hathway.sproutelle.domain.model.HomeData
import com.hathway.sproutelle.domain.model.PlantItem
import com.hathway.sproutelle.presentation.components.BottomNavigationBar
import com.hathway.sproutelle.presentation.components.CategoryCard
import com.hathway.sproutelle.presentation.components.ErrorState
import com.hathway.sproutelle.presentation.components.LoadingState
import com.hathway.sproutelle.presentation.components.ParentButton
import com.hathway.sproutelle.presentation.components.TabletNavigationRail
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import sproutelle.shared.generated.resources.Res
import sproutelle.shared.generated.resources.sproutelle_home_landscape
import sproutelle.shared.generated.resources.sproutelle_home_portrait
import theme.SproutelleColors
import theme.SproutelleDimens
import theme.SproutelleShapes
import theme.SproutelleSpacing

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCategoryClick: ((ExplorerItem) -> Unit)? = null,
    onContinueClick: (() -> Unit)? = null,
    onParentClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val explorerItems by viewModel.explorerItems.collectAsStateWithLifecycle()
    val explorerError by viewModel.explorerError.collectAsStateWithLifecycle()

    ProvideWindowSizeClass { windowSize ->
        HomeScreenContent(
            uiState = uiState,
            explorerItems = explorerItems,
            explorerError = explorerError,
            isTablet = windowSize.isTablet,
            onRefresh = viewModel::refresh,
            onRetryExplorer = viewModel::loadExplorerItems,
            onCategoryClick = onCategoryClick,
            onContinueClick = onContinueClick,
            onParentClick = onParentClick
        )
    }
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    explorerItems: List<ExplorerItem>,
    explorerError: String?,
    isTablet: Boolean,
    onRefresh: () -> Unit,
    onRetryExplorer: () -> Unit,
    onCategoryClick: ((ExplorerItem) -> Unit)? = null,
    onContinueClick: (() -> Unit)? = null,
    onParentClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxSize(), color = SproutelleColors.Cream
    ) {
        when (uiState) {
            is HomeUiState.Loading -> {
                LoadingState(message = "Preparing your garden...")
            }

            is HomeUiState.Error -> {
                ErrorState(
                    message = uiState.message, onRetryClick = onRefresh
                )
            }

            is HomeUiState.Success -> {
                if (isTablet) {
                    TabletHomeContent(
                        data = uiState.data,
                        explorerItems = explorerItems,
                        explorerError = explorerError,
                        onRefresh = onRefresh,
                        onRetryExplorer = onRetryExplorer,
                        onCategoryClick = onCategoryClick,
                        onContinueClick = onContinueClick,
                        onParentClick = onParentClick
                    )
                } else {
                    PhoneHomeContent(
                        data = uiState.data,
                        explorerItems = explorerItems,
                        explorerError = explorerError,
                        onRefresh = onRefresh,
                        onRetryExplorer = onRetryExplorer,
                        onCategoryClick = onCategoryClick,
                        onContinueClick = onContinueClick,
                        onParentClick = onParentClick
                    )
                }
            }
        }
    }
}

@Composable
private fun PhoneHomeContent(
    data: HomeData,
    explorerItems: List<ExplorerItem>,
    explorerError: String?,
    onRefresh: () -> Unit,
    onRetryExplorer: () -> Unit,
    onCategoryClick: ((ExplorerItem) -> Unit)? = null,
    onContinueClick: (() -> Unit)? = null,
    onParentClick: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = SproutelleColors.Cream, bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        }) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(SproutelleColors.Cream)
                .padding(innerPadding), contentPadding = PaddingValues(
                horizontal = SproutelleDimens.MobileContentPadding, vertical = 12.dp
            ), verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                HomeTopBrandBar(
                    isTablet = false, onParentClick = { onParentClick?.invoke() })
            }

            item {
                GardenHeroBanner(
                    userName = data.userName, isTablet = false
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Continue Playing",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                        color = Color(0xFF1B365D),
                        fontWeight = FontWeight.Black
                    )
                    ContinuePlayingCard(
                        title = "Alphabet Fun",
                        subtitle = "Letters & Sounds",
                        isTablet = false,
                        onClick = {
                            onContinueClick?.invoke() ?: explorerItems.firstOrNull()
                                ?.let { onCategoryClick?.invoke(it) }
                        })
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExploreSectionHeader(
                        isTablet = false, onRefresh = onRefresh
                    )

                    if (explorerError != null) {
                        ExplorerErrorBanner(
                            errorMessage = explorerError, onRetry = onRetryExplorer
                        )
                    }

                    CategoriesGrid(
                        items = explorerItems,
                        columns = 2,
                        isTablet = false,
                        onCategoryClick = onCategoryClick
                    )
                }
            }
        }
    }
}

@Composable
private fun TabletHomeContent(
    data: HomeData,
    explorerItems: List<ExplorerItem>,
    explorerError: String?,
    onRefresh: () -> Unit,
    onRetryExplorer: () -> Unit,
    onCategoryClick: ((ExplorerItem) -> Unit)? = null,
    onContinueClick: (() -> Unit)? = null,
    onParentClick: (() -> Unit)? = null
) {
    var selectedRailItem by remember { mutableStateOf(0) }

    Row(
        modifier = Modifier.fillMaxSize().background(SproutelleColors.Cream).statusBarsPadding()
    ) {
        TabletNavigationRail(
            selectedTab = selectedRailItem, onTabSelected = { selectedRailItem = it })

        LazyColumn(
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = SproutelleDimens.TabletContentPadding),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                HomeTopBrandBar(
                    isTablet = true, onParentClick = { onParentClick?.invoke() })
            }
// Balanced 2-pane top row: Garden Hero (left) + Continue Playing (right)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1.35f).fillMaxHeight()) {
                        GardenHeroBanner(
                            userName = data.userName,
                            isTablet = true,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Continue Playing",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                            color = Color(0xFF1B365D),
                            fontWeight = FontWeight.Black
                        )
                        ContinuePlayingCard(
                            title = "Alphabet Fun",
                            subtitle = "Letters & Sounds",
                            isTablet = true,
                            modifier = Modifier.weight(1f).padding(top = 8.dp),
                            onClick = {
                                onContinueClick?.invoke() ?: explorerItems.firstOrNull()
                                    ?.let { onCategoryClick?.invoke(it) }
                            })
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ExploreSectionHeader(
                        isTablet = true, onRefresh = onRefresh
                    )
                    if (explorerError != null) {
                        ExplorerErrorBanner(
                            errorMessage = explorerError, onRetry = onRetryExplorer
                        )
                    }
                    CategoriesGrid(
                        items = explorerItems,
                        columns = 3,
                        isTablet = true,
                        onCategoryClick = onCategoryClick
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTopBrandBar(
    isTablet: Boolean, onParentClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(if (isTablet) 42.dp else 36.dp),
                shape = CircleShape,
                color = Color(0xFFE8F5E9),
                border = BorderStroke(2.dp, Color(0xFFA5D6A7))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🌱", fontSize = if (isTablet) 22.sp else 18.sp)
                }
            }
            Text(
                text = "Sproutelle",
                style = (if (isTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge).copy(
                    fontSize = if (isTablet) 26.sp else 22.sp
                ),
                color = SproutelleColors.LeafGreen,
                fontWeight = FontWeight.Black
            )
        }
        ParentButton(onClick = onParentClick)
    }
}

@Composable
private fun GardenHeroBanner(
    userName: String, isTablet: Boolean, modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AvatarBobLoop")
    val avatarFloatY by infiniteTransition.animateFloat(
        initialValue = -2.5f, targetValue = 2.5f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "AvatarFloatY"
    )
    val bannerShape = RoundedCornerShape(if (isTablet) 28.dp else 24.dp)
    val heroPainter = painterResource(
        if (isTablet) Res.drawable.sproutelle_home_landscape else Res.drawable.sproutelle_home_portrait
    )
    Box(modifier = modifier.fillMaxWidth()) {
// Soft 3D bottom rim
        Box(
            modifier = Modifier.fillMaxWidth().height(if (isTablet) 180.dp else 160.dp)
                .offset(y = 4.dp).background(Color(0xFFC5E1A5), shape = bannerShape)
        )
        Card(
            modifier = Modifier.fillMaxWidth().height(if (isTablet) 180.dp else 160.dp),
            shape = bannerShape,
            border = BorderStroke(2.5.dp, Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = heroPainter,
                    contentDescription = "Garden Hero",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
// Subtle left-to-right readability scrim so greeting text always pops
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.72f),
                                Color.White.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
                )
                Row(
                    modifier = Modifier.fillMaxSize()
                        .padding(horizontal = if (isTablet) 22.dp else 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 14.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(if (isTablet) 64.dp else 56.dp)
                            .graphicsLayer { translationY = avatarFloatY },
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(2.5.dp, Color(0xFFFFE082)),
                        shadowElevation = 3.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👧", fontSize = if (isTablet) 34.sp else 28.sp)
                        }
                    }
                    Column {
                        Text(
                            text = "Good Morning,",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = if (isTablet) 15.sp else 13.sp
                            ),
                            color = Color(0xFF455A64),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (userName.isNotBlank()) "$userName!" else "Little Gardener!",
                            style = (if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge).copy(
                                fontSize = if (isTablet) 24.sp else 21.sp
                            ),
                            color = Color(0xFF1B365D),
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinuePlayingCard(
    title: String,
    subtitle: String,
    isTablet: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
        ), label = "ContinueCardScale"
    )
    val cardShape = RoundedCornerShape(if (isTablet) 26.dp else 22.dp)
    Box(
        modifier = modifier.fillMaxWidth().graphicsLayer {
            scaleX = scale
            scaleY = scale
        }.clickable(
            interactionSource = interactionSource, indication = null, onClick = onClick
        )
    ) {
// 3D bottom rim shadow
        Box(
            modifier = Modifier.matchParentSize().offset(y = 4.dp)
                .background(Color(0xFFCFD8DC), shape = cardShape)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = cardShape,
            border = BorderStroke(2.dp, Color(0xFFE8F5E9)),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = if (isTablet) 16.dp else 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(if (isTablet) 58.dp else 52.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE8F5E9),
                        border = BorderStroke(2.dp, Color(0xFFC8E6C9))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🅰️", fontSize = if (isTablet) 30.sp else 26.sp)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = if (isTablet) 19.sp else 17.sp
                            ),
                            color = Color(0xFF1B365D),
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = if (isTablet) 14.sp else 12.sp
                            ),
                            color = Color(0xFF607D8B),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
// Glossy 3D Play Disc
                Box(
                    modifier = Modifier.size(if (isTablet) 50.dp else 46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().offset(y = 2.5.dp)
                            .background(Color(0xFF2E7D32), CircleShape)
                    )
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF81C784), Color(0xFF43A047))
                            ), shape = CircleShape
                        ).border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Continue $title",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreSectionHeader(
    isTablet: Boolean, onRefresh: () -> Unit
) {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Explore", style = MaterialTheme.typography.titleLarge.copy(
                fontSize = if (isTablet) 22.sp else 19.sp
            ), color = Color(0xFF1B365D), fontWeight = FontWeight.Black
        )
        Surface(
            shape = CircleShape,
            color = Color(0xFFE8F5E9),
            border = BorderStroke(1.5.dp, Color(0xFFA5D6A7)),
            modifier = Modifier.clickable {
                scope.launch {
                    rotation.snapTo(0f)
                    rotation.animateTo(360f, animationSpec = tween(450))
                }
                onRefresh()
            }) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Refresh Categories",
                    tint = SproutelleColors.LeafGreen,
                    modifier = Modifier.size(16.dp).rotate(rotation.value)
                )
                Text(
                    text = "Refresh",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                    color = SproutelleColors.LeafGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ExplorerErrorBanner(
    errorMessage: String, onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SproutelleColors.Peach,
        border = BorderStroke(1.5.dp, Color(0xFFFFCCBC))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = SproutelleColors.Error,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = SproutelleColors.LeafGreen),
                shape = SproutelleShapes.Pill
            ) {
                Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CategoriesGrid(
    items: List<ExplorerItem>,
    columns: Int,
    isTablet: Boolean,
    onCategoryClick: ((ExplorerItem) -> Unit)? = null
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No explore categories available",
                style = MaterialTheme.typography.bodyLarge,
                color = SproutelleColors.SecondaryText
            )
        }
        return
    }

    val rows = remember(items, columns) { items.chunked(columns) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Medium)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(SproutelleSpacing.Medium)
            ) {
                rowItems.forEach { item ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        CategoryCard(
                            item = item,
                            isTablet = isTablet,
                            onClick = { onCategoryClick?.invoke(item) }
                        )
                    }
                }
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ==========================================
// STATELESS PREVIEWS
// ==========================================

private fun sampleHomeData() = HomeData(
    userName = "Little Gardener",
    greetingMessage = "Good Morning,",
    streakDays = 4,
    activePlants = listOf(
        PlantItem(
            id = "plant_1",
            name = "Sunny Sprout",
            species = "Sunflower",
            progress = 0.75f,
            statusText = "Growing strong!",
            badgeColorHex = "#4CAF50"
        )
    ),
    categories = emptyList(),
    recentActivities = listOf(
        ActivityItem(
            id = "act_1",
            title = "Alphabet Fun",
            timestamp = "Letters & Sounds",
            isCompleted = false
        )
    )
)
private fun sampleExplorerItems() = listOf(
    ExplorerItem(
        id = "1",
        titleName = "Fruits",
        titleImageAssetId = null,
        backgroundColor = "#FFF3E0",
        categoryId = "cat_1",
        sortOrder = 1,
        isActive = true
    ),
    ExplorerItem(
        id = "2",
        titleName = "Letters & Reading",
        titleImageAssetId = null,
        backgroundColor = "#E8F5E9",
        categoryId = "cat_2",
        sortOrder = 2,
        isActive = true
    ),
    ExplorerItem(
        id = "3",
        titleName = "Shapes & Colours",
        titleImageAssetId = null,
        backgroundColor = "#E1F5FE",
        categoryId = "cat_3",
        sortOrder = 3,
        isActive = true
    ),
    ExplorerItem(
        id = "4",
        titleName = "Creative Studio",
        titleImageAssetId = null,
        backgroundColor = "#FCE4EC",
        categoryId = "cat_4",
        sortOrder = 4,
        isActive = true
    ),
    ExplorerItem(
        id = "5",
        titleName = "Animals & Nature",
        titleImageAssetId = null,
        backgroundColor = "#FFFDE7",
        categoryId = "cat_5",
        sortOrder = 5,
        isActive = true
    ),
    ExplorerItem(
        id = "6",
        titleName = "Numbers & Maths",
        titleImageAssetId = null,
        backgroundColor = "#F3E5F5",
        categoryId = "cat_6",
        sortOrder = 6,
        isActive = true
    )
)


@Preview(
    name = "HomeScreen - Phone Portrait",
    device = "spec:width=411dp,height=891dp",
    showBackground = true
)
@Composable
private fun PhoneHomeScreenPreview() {
    MaterialTheme {
        HomeScreenContent(
            uiState = HomeUiState.Success(sampleHomeData()),
            explorerItems = sampleExplorerItems(),
            explorerError = null,
            isTablet = false,
            onRefresh = {},
            onRetryExplorer = {}
        )
    }
}

@Preview(
    name = "HomeScreen - Tablet Landscape",
    device = "spec:width=1280dp,height=800dp,orientation=landscape",
    showBackground = true
)
@Composable
private fun TabletHomeScreenPreview() {
    MaterialTheme {
        HomeScreenContent(
            uiState = HomeUiState.Success(sampleHomeData()),
            explorerItems = sampleExplorerItems(),
            explorerError = null,
            isTablet = true,
            onRefresh = {},
            onRetryExplorer = {}
        )
    }
}
