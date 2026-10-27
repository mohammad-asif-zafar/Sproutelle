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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.hathway.sproutelle.core.responsive.ProvideWindowSizeClass
import com.hathway.sproutelle.domain.model.ActivityItem
import com.hathway.sproutelle.domain.model.ExplorerItem
import com.hathway.sproutelle.domain.model.HomeData
import com.hathway.sproutelle.domain.model.PlantItem
import org.jetbrains.compose.resources.painterResource
import sproutelle.shared.generated.resources.Res
import sproutelle.shared.generated.resources.sproutelle_home_landscape
import sproutelle.shared.generated.resources.sproutelle_home_portrait
import theme.SproutelleColors
import theme.SproutelleDimens
import theme.SproutelleShapes
import theme.SproutelleSpacing

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val explorerItems by viewModel.explorerItems.collectAsState()
    val explorerError by viewModel.explorerError.collectAsState()

    ProvideWindowSizeClass { windowSize ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SproutelleColors.Cream
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SproutelleColors.LeafGreen)
                    }
                }
                is HomeUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(SproutelleSpacing.XXLarge),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = SproutelleColors.Error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(SproutelleSpacing.Large))
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = SproutelleColors.LeafGreen),
                            shape = SproutelleShapes.Pill
                        ) {
                            Text("Retry", color = SproutelleColors.OnPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                is HomeUiState.Success -> {
                    if (windowSize.isTablet) {
                        TabletHomeContent(
                            data = state.data,
                            explorerItems = explorerItems,
                            explorerError = explorerError,
                            onRefresh = viewModel::refresh,
                            onRetryExplorer = viewModel::loadExplorerItems
                        )
                    } else {
                        PhoneHomeContent(
                            data = state.data,
                            explorerItems = explorerItems,
                            explorerError = explorerError,
                            onRefresh = viewModel::refresh,
                            onRetryExplorer = viewModel::loadExplorerItems
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
    onRetryExplorer: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SproutelleColors.White,
                contentColor = SproutelleColors.Ink
            ) {
                val navItems = listOf("Home" to "🏠", "Games" to "🎮", "Rewards" to "⭐", "Profile" to "👤")
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Text(item.second, fontSize = 20.sp) },
                        label = { Text(item.first) }
                    )
                }
            }
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
                // Portrait Hero Header Background Card for Mobile
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = SproutelleShapes.Large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(Res.drawable.sproutelle_home_portrait),
                            contentDescription = "Home Portrait Background",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(SproutelleSpacing.Large),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(SproutelleColors.White, shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👧", fontSize = 24.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Hello,",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = SproutelleColors.SecondaryText
                                        )
                                        Text(
                                            text = "${data.userName}!",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = SproutelleColors.Ink,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(SproutelleColors.White, shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🔔", fontSize = 16.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(SproutelleColors.White, shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("⚙️", fontSize = 16.sp)
                                    }
                                }
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
                // Exactly two columns on phone, using safe row-chunked layout without clipping
                CategoriesGrid(items = explorerItems, columns = 2)
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
    onRetryExplorer: () -> Unit
) {
    var selectedRailItem by remember { mutableStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(SproutelleColors.Cream)
    ) {
        NavigationRail(
            containerColor = SproutelleColors.White,
            contentColor = SproutelleColors.Ink
        ) {
            Spacer(modifier = Modifier.height(SproutelleSpacing.Large))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(SproutelleColors.LeafGreen, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🌱", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.height(SproutelleSpacing.XXLarge))

            val railItems = listOf("Home" to "🏠", "Games" to "🎮", "Progress" to "📊", "Rewards" to "⭐", "Profile" to "👤")
            railItems.forEachIndexed { index, item ->
                NavigationRailItem(
                    selected = selectedRailItem == index,
                    onClick = { selectedRailItem = index },
                    icon = { Text(item.second, fontSize = 22.sp) },
                    label = { Text(item.first) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(SproutelleDimens.TabletContentPadding),
            verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Large)
        ) {
            item {
                // Top Bar with Sproutelle Branding and Parent button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌱", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sproutelle",
                            style = MaterialTheme.typography.headlineLarge,
                            color = SproutelleColors.LeafGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SproutelleColors.White, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔍", fontSize = 18.sp)
                        }

                        Card(
                            shape = SproutelleShapes.Pill,
                            colors = CardDefaults.cardColors(containerColor = SproutelleColors.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("👤", fontSize = 16.sp)
                                Text(
                                    text = "Parent",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SproutelleColors.Ink,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Landscape Hero Banner Image for Tablet matching design reference
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape = SproutelleShapes.Large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(Res.drawable.sproutelle_home_landscape),
                            contentDescription = "Cheerful Lakeside Explorer Meadow",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(SproutelleSpacing.Large),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(SproutelleColors.White, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("👧", fontSize = 32.sp)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Good Morning,",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = SproutelleColors.SecondaryText
                                    )
                                    Text(
                                        text = "${data.userName}!",
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = SproutelleColors.Ink,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SproutelleSpacing.Large),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(360.dp)) {
                        HeaderGreetingCard(data = data)
                    }
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
            }

            item {
                // Responsive multi-column grid for tablet (4 columns), safe row-chunked layout without clipping
                CategoriesGrid(items = explorerItems, columns = 4)
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
private fun CategoriesGrid(items: List<ExplorerItem>, columns: Int) {
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
                        CategoryCard(item = item)
                    }
                }
                // If last row has fewer items, pad with invisible spacers
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(item: ExplorerItem) {
    val bgColor = parseColorHex(item.backgroundColor)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = SproutelleShapes.Large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(SproutelleColors.White.copy(alpha = 0.6f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                var isError by remember { mutableStateOf(false) }

                if (!item.imageUrl.isNullOrBlank() && !isError) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.titleName,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        onLoading = {
                            println("SproutelleImage: Loading image for '${item.titleName}'")
                        },
                        onSuccess = {
                            println("SproutelleImage: Successfully loaded image for '${item.titleName}'")
                        },
                        onError = { state ->
                            val cause = state.result.throwable
                            println("SproutelleImageError: Failed to load image for '${item.titleName}': ${cause::class.simpleName} - ${cause.message}")
                            isError = true
                        }
                    )
                }

                if (item.imageUrl.isNullOrBlank() || isError) {
                    Text(
                        text = when (item.titleName.lowercase()) {
                            "letters" -> "A"
                            "numbers" -> "123"
                            "animals" -> "🦁"
                            "shapes" -> "🔺"
                            "colours", "colors" -> "🎨"
                            "creativity" -> "✨"
                            else -> "🌟"
                        },
                        fontSize = 26.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.titleName,
                style = MaterialTheme.typography.titleMedium,
                color = SproutelleColors.Ink,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

private fun parseColorHex(hex: String): Color {
    return try {
        val cleaned = hex.removePrefix("#")
        val colorLong = cleaned.toLong(16)
        if (cleaned.length == 6) {
            Color(colorLong or 0xFF000000)
        } else {
            Color(colorLong)
        }
    } catch (e: Exception) {
        SproutelleColors.Peach
    }
}
