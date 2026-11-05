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
