package com.hathway.sproutelle.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.SproutelleColors
import theme.SproutelleDimens
import theme.SproutelleSpacing

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = SproutelleColors.White,
        contentColor = SproutelleColors.Ink
    ) {
        val navItems = listOf("Home" to "🏠", "Games" to "🎮", "Progress" to "📊", "Profile" to "👤")
        navItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Text(item.second, fontSize = 20.sp) },
                label = { Text(item.first) }
            )
        }
    }
}

@Composable
fun TabletNavigationRail(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier,
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
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Text(item.second, fontSize = 22.sp) },
                label = { Text(item.first) }
            )
        }
    }
}

@Composable
fun ScreenHeader(
    title: String,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SproutelleColors.Cream)
            .padding(
                horizontal = SproutelleDimens.MobileContentPadding,
                vertical = SproutelleSpacing.Medium
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundedIconButton(
            icon = "←",
            onClick = onBackClick
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = SproutelleColors.LeafGreen,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        Text(
            text = "Refresh",
            style = MaterialTheme.typography.titleMedium,
            color = SproutelleColors.LeafGreen,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onRefreshClick() }
        )
    }
}
