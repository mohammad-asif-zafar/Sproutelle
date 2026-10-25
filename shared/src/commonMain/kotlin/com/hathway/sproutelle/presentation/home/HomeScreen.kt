package com.hathway.sproutelle.presentation.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hathway.sproutelle.core.responsive.ProvideWindowSizeClass
import com.hathway.sproutelle.domain.model.ActivityItem
import com.hathway.sproutelle.domain.model.CategoryItem
import com.hathway.sproutelle.domain.model.HomeData
import com.hathway.sproutelle.domain.model.PlantItem
import theme.SproutelleColors
import theme.SproutelleDimens
import theme.SproutelleShapes
import theme.SproutelleSpacing

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()

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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = SproutelleColors.Error
                        )
                    }
                }
                is HomeUiState.Success -> {
                    if (windowSize.isTablet) {
                        TabletHomeContent(data = state.data)
                    } else {
                        PhoneHomeContent(data = state.data)
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneHomeContent(data: HomeData) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SproutelleColors.White,
                contentColor = SproutelleColors.Ink
            ) {
                val navItems = listOf("Home" to "🏠", "Garden" to "🪴", "Calendar" to "📅", "Profile" to "👤")
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
                HeaderGreetingCard(data = data)
            }

            item {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleLarge,
                    color = SproutelleColors.Ink,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(SproutelleSpacing.Medium))
                CategoriesRow(categories = data.categories)
            }

            item {
                Text(
                    text = "My Active Plants",
                    style = MaterialTheme.typography.titleLarge,
                    color = SproutelleColors.Ink,
                    fontWeight = FontWeight.Bold
                )
            }

            items(data.activePlants) { plant ->
                PlantCardItem(plant = plant)
            }

            item {
                Text(
                    text = "Recent Garden Activity",
                    style = MaterialTheme.typography.titleLarge,
                    color = SproutelleColors.Ink,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(SproutelleSpacing.Medium))
                ActivitiesSection(activities = data.recentActivities)
            }
        }
    }
}

@Composable
private fun TabletHomeContent(data: HomeData) {
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

            val railItems = listOf("Home" to "🏠", "Garden" to "🪴", "Calendar" to "📅", "Profile" to "👤")
            railItems.forEachIndexed { index, item ->
                NavigationRailItem(
                    selected = selectedRailItem == index,
                    onClick = { selectedRailItem = index },
                    icon = { Text(item.second, fontSize = 22.sp) },
                    label = { Text(item.first) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(SproutelleDimens.TabletContentPadding),
            horizontalArrangement = Arrangement.spacedBy(SproutelleSpacing.XXLarge)
        ) {
            // Left Column: Greeting & Active Plants Grid
            LazyColumn(
                modifier = Modifier.weight(1.2f),
                verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Large)
            ) {
                item {
                    HeaderGreetingCard(data = data)
                }

                item {
                    Text(
                        text = "Active Plants Overview",
                        style = MaterialTheme.typography.titleLarge,
                        color = SproutelleColors.Ink,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(data.activePlants) { plant ->
                    PlantCardItem(plant = plant)
                }
            }

            // Right Column: Categories Grid & Recent Activities
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Large)
            ) {
                item {
                    Text(
                        text = "Plant Categories",
                        style = MaterialTheme.typography.titleLarge,
                        color = SproutelleColors.Ink,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(SproutelleSpacing.Medium))
                    CategoriesRow(categories = data.categories)
                }

                item {
                    Text(
                        text = "Recent Activity Log",
                        style = MaterialTheme.typography.titleLarge,
                        color = SproutelleColors.Ink,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(SproutelleSpacing.Medium))
                    ActivitiesSection(activities = data.recentActivities)
                }
            }
        }
    }
}

@Composable
private fun HeaderGreetingCard(data: HomeData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SproutelleColors.LeafGreen),
        shape = SproutelleShapes.Large
    ) {
        Column(
            modifier = Modifier.padding(SproutelleSpacing.Large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${data.userName}!",
                        style = MaterialTheme.typography.headlineLarge,
                        color = SproutelleColors.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = data.greetingMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = SproutelleColors.Mint
                    )
                }

                Box(
                    modifier = Modifier
                        .background(SproutelleColors.SunYellow, shape = SproutelleShapes.Pill)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${data.streakDays} Days 🔥",
                        style = MaterialTheme.typography.titleMedium,
                        color = SproutelleColors.Ink,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoriesRow(categories: List<CategoryItem>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(SproutelleSpacing.Medium)
    ) {
        items(categories) { category ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SproutelleColors.White),
                shape = SproutelleShapes.Medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌿", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(SproutelleSpacing.Small))
                    Column {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = SproutelleColors.Ink,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${category.itemCount} plants",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SproutelleColors.SecondaryText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlantCardItem(plant: PlantItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SproutelleColors.White),
        shape = SproutelleShapes.Medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(SproutelleSpacing.Large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(SproutelleColors.Mint, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🪴", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(SproutelleSpacing.Large))
                    Column {
                        Text(
                            text = plant.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = SproutelleColors.Ink,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = plant.species,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SproutelleColors.SecondaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SproutelleSpacing.Medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = plant.statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SproutelleColors.LeafGreen,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${(plant.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SproutelleColors.SecondaryText
                )
            }

            Spacer(modifier = Modifier.height(SproutelleSpacing.Small))

            LinearProgressIndicator(
                progress = { plant.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = SproutelleColors.LeafGreen,
                trackColor = SproutelleColors.Mint
            )
        }
    }
}

@Composable
private fun ActivitiesSection(activities: List<ActivityItem>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Small)
    ) {
        activities.forEach { activity ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SproutelleColors.White, shape = SproutelleShapes.Small)
                    .padding(SproutelleSpacing.Medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (activity.isCompleted) "✅" else "⏳", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(SproutelleSpacing.Small))
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = SproutelleColors.Ink
                    )
                }
                Text(
                    text = activity.timestamp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SproutelleColors.SecondaryText
                )
            }
        }
    }
}
