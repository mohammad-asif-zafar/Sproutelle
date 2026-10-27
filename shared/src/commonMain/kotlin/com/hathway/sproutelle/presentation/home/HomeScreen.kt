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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.hathway.sproutelle.domain.model.ExplorerItem
import com.hathway.sproutelle.domain.model.HomeData
import org.jetbrains.compose.resources.painterResource
import sproutelle.shared.generated.resources.Res
import sproutelle.shared.generated.resources.sproutelle_home_landscape
import sproutelle.shared.generated.resources.sproutelle_home_portrait
import theme.SproutelleColors
import theme.SproutelleDimens
import theme.SproutelleShapes
import theme.SproutelleSpacing
import theme.SproutelleTheme

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
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
                val navItems = listOf("Home" to "🏠", "Games" to "🎮", "Progress" to "📊", "Profile" to "👤")
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
                // Top Header: Logo left, Parent button right
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

                    Card(
                        shape = SproutelleShapes.Pill,
                        colors = CardDefaults.cardColors(containerColor = SproutelleColors.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("👤", fontSize = 14.sp)
                            Text(
                                text = "Parent",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SproutelleColors.Ink,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                // Garden Hero Banner
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
                // Two-column vertical category cards grid for phone matching reference image 2 exactly
                CategoriesGrid(items = explorerItems, columns = 2, isTablet = false)
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
            verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Section)
        ) {
            item {
                // Top Header: Logo left, Parent button right
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

                    Card(
                        shape = SproutelleShapes.Pill,
                        colors = CardDefaults.cardColors(containerColor = SproutelleColors.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
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

            item {
                // Landscape Garden Hero Banner for Tablet matching reference
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
                                    text = "${data.userName}!",
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
                // Three-column grid of wide horizontal cards matching reference image 3 for tablet
                CategoriesGrid(items = explorerItems, columns = 3, isTablet = true)
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
private fun CategoriesGrid(items: List<ExplorerItem>, columns: Int, isTablet: Boolean) {
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
                        CategoryCard(item = item, isTablet = isTablet)
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
private fun CategoryCard(item: ExplorerItem, isTablet: Boolean) {
    val bgColor = parseColorHex(item.backgroundColor)

    if (isTablet) {
        // Tablet: Wide horizontal card (Reference Image 3)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            shape = SproutelleShapes.Large,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CategoryIcon(item = item, size = 170.dp)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.titleName,
                        style = MaterialTheme.typography.titleMedium,
                        color = SproutelleColors.Ink,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    val description = getCategoryDescription(item.titleName)
                    if (description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = SproutelleColors.SecondaryText,
                            maxLines = 2,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = SproutelleShapes.Pill,
                        color = Color(0xFFFFEAD0)
                    ) {
                        Text(
                            text = "Ages 3–8",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB35900),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SproutelleColors.White, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "›",
                        color = SproutelleColors.Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }
            }
        }
    } else {
        // Phone: Vertical card matching Reference Image 2 perfectly (illustration top, title, ages badge, arrow)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            shape = SproutelleShapes.Large,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Illustration tile in rounded white container
                CategoryIcon(item = item, size = 100.dp)

                // Middle: Title (max 2 lines, centered)
                Text(
                    text = item.titleName,
                    style = MaterialTheme.typography.titleSmall,
                    color = SproutelleColors.Ink,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 16.sp
                )

                // Bottom: Ages 3-8 badge and arrow button in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = SproutelleShapes.Pill,
                        color = Color(0xFFFFEAD0)
                    ) {
                        Text(
                            text = "Ages 3–8",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB35900),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(SproutelleColors.White, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "›",
                            color = SproutelleColors.Ink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryIcon(item: ExplorerItem, size: androidx.compose.ui.unit.Dp) {
    var isError by remember { mutableStateOf(false) }

    if (!item.imageUrl.isNullOrBlank() && !isError) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.titleName,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit,
            onError = { isError = true }
        )
    }

    if (item.imageUrl.isNullOrBlank() || isError) {
        Text(
            text = when (item.titleName.lowercase()) {
                "letters", "letters & reading" -> "ABC"
                "numbers", "numbers & maths" -> "123"
                "animals", "animals & nature" -> "🦁"
                "shapes", "shapes & colours" -> "🎨"
                "colours", "colors" -> "🎨"
                "creativity", "creative studio" -> "✨"
                else -> "🌟"
            },
            fontSize = (size.value * 0.4f).sp
        )
    }
}

private fun getCategoryDescription(title: String): String {
    return when (title.lowercase()) {
        "numbers & maths" -> "Counting, number sense, early arithmetic and patterns"
        "letters & reading" -> "Alphabet, phonics, vocabulary and early reading"
        "animals & nature" -> "Animal knowledge, natural environments and living things"
        "shapes & colours" -> "Visual recognition, classification and patterns"
        "creative studio" -> "Drawing, colouring, creating and imaginative play"
        "everyday world" -> "Familiar objects, daily routines and the world around us"
        "memory & logic" -> "Memory, reasoning, classification and sequences"
        "music & sounds" -> "Listening, sound recognition, rhythm and musical exploration"
        "puzzles & thinking" -> "Spatial reasoning, spatial problem-solving and puzzle challenges"
        "science & discovery" -> "Nature, simple scientific concepts and curiosity-led learning"
        "social & emotional skills" -> "Emotions, empathy, friendship and everyday independence"
        "languages" -> "Vocabulary, listening, pronunciation and bilingual learning"
        else -> "Fun interactive learning for kids"
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

// ==================== COMPOSE PREVIEWS ====================

@Preview(showBackground = true, backgroundColor = 0xFFFFF8E8, name = "Category Card - Phone")
@Composable
private fun CategoryCardPhonePreview() {
    SproutelleTheme {
        Box(modifier = Modifier.padding(16.dp).width(180.dp)) {
            CategoryCard(
                item = ExplorerItem(
                    id = "1",
                    titleName = "Everyday World",
                    titleImageAssetId = null,
                    backgroundColor = "#E6F7E8",
                    categoryId = "cat_1",
                    sortOrder = 1,
                    isActive = true,
                    imageUrl = null
                ),
                isTablet = false
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFF8E8, name = "Category Card - Tablet")
@Composable
private fun CategoryCardTabletPreview() {
    SproutelleTheme {
        Box(modifier = Modifier.padding(16.dp).width(360.dp)) {
            CategoryCard(
                item = ExplorerItem(
                    id = "1",
                    titleName = "Everyday World",
                    titleImageAssetId = null,
                    backgroundColor = "#E6F7E8",
                    categoryId = "cat_1",
                    sortOrder = 1,
                    isActive = true,
                    imageUrl = null
                ),
                isTablet = true
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFF8E8, name = "Continue Playing Card")
@Composable
private fun HeaderGreetingCardPreview() {
    SproutelleTheme {
        Box(modifier = Modifier.padding(16.dp).width(400.dp)) {
            HeaderGreetingCard(
                data = HomeData(
                    userName = "Emma",
                    greetingMessage = "Good Morning",
                    streakDays = 5,
                    activePlants = emptyList(),
                    categories = emptyList(),
                    recentActivities = emptyList()
                )
            )
        }
    }
}
