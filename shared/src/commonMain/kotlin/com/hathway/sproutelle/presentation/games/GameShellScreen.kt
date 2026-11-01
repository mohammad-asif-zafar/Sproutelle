package com.hathway.sproutelle.presentation.games

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hathway.sproutelle.core.responsive.ProvideWindowSizeClass
import com.hathway.sproutelle.presentation.components.PrimaryButton
import com.hathway.sproutelle.presentation.components.RoundedIconButton
import theme.SproutelleColors
import theme.SproutelleDimens
import theme.SproutelleShapes
import theme.SproutelleSpacing

@Composable
fun GameShellScreen(
    viewModel: GameShellViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProvideWindowSizeClass { windowSize ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SproutelleColors.Cream
        ) {
            Scaffold(
                topBar = {
                    GameShellHeader(
                        title = uiState.gameName,
                        onBackClick = onBackClick
                    )
                },
                bottomBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SproutelleDimens.MobileContentPadding)
                    ) {
                        PrimaryButton(
                            text = "Return to Topics",
                            onClick = onBackClick
                        )
                    }
                },
                containerColor = SproutelleColors.Cream
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = SproutelleDimens.MobileContentPadding),
                    verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (windowSize.isTablet) 200.dp else 160.dp),
                        colors = CardDefaults.cardColors(containerColor = SproutelleColors.White),
                        shape = SproutelleShapes.Large,
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(SproutelleSpacing.Large),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("🎮", fontSize = if (windowSize.isTablet) 44.sp else 36.sp)
                            Spacer(modifier = Modifier.height(SproutelleSpacing.Small))
                            Text(
                                text = uiState.gameName,
                                style = MaterialTheme.typography.titleLarge,
                                color = SproutelleColors.Ink,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(SproutelleSpacing.XSmall))
                            Text(
                                text = "Game Engine Foundation Frame",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SproutelleColors.SecondaryText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    val templateCards = listOf("Question Frame Template", "Option Renderer Template", "Audio Slot Template", "Feedback State Renderer")
                    val gridColumns = if (windowSize.isTablet) 4 else 2

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        horizontalArrangement = Arrangement.spacedBy(SproutelleSpacing.Medium),
                        verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Medium),
                        contentPadding = PaddingValues(vertical = SproutelleSpacing.Small)
                    ) {
                        items(templateCards.size) { index ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                colors = CardDefaults.cardColors(containerColor = SproutelleColors.White),
                                shape = SproutelleShapes.Large,
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = templateCards[index],
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SproutelleColors.SecondaryText,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameShellHeader(
    title: String,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SproutelleColors.Cream)
            .padding(
                horizontal = SproutelleDimens.MobileContentPadding,
                vertical = SproutelleSpacing.Medium
            ),
        verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundedIconButton(
                icon = "←",
                onClick = onBackClick
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = SproutelleColors.LeafGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Engine Visual Frame",
                    style = MaterialTheme.typography.bodySmall,
                    color = SproutelleColors.SecondaryText
                )
            }

            Spacer(modifier = Modifier.size(SproutelleDimens.MinimumTouchTarget))
        }
    }
}
