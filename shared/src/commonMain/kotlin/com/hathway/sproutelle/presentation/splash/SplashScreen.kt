package com.hathway.sproutelle.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hathway.sproutelle.core.navigation.Screen
import com.hathway.sproutelle.core.responsive.ProvideWindowSizeClass
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import sproutelle.shared.generated.resources.Res
import sproutelle.shared.generated.resources.sproutelle_splash
import sproutelle.shared.generated.resources.sproutelle_splash_tablet
import theme.SproutelleDimens
import theme.SproutelleSpacing
import theme.SproutelleTheme

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigate: (Screen) -> Unit
) {
    val scale = remember { Animatable(0.9f) }
    val cardAlpha = remember { Animatable(0f) }
    val destination by viewModel.destination.collectAsState()

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600)
            )
        }
        launch {
            cardAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, delayMillis = 200)
            )
        }
    }

    LaunchedEffect(destination) {
        destination?.let {
            onNavigate(it)
        }
    }

    SproutelleTheme {
        ProvideWindowSizeClass { windowSize ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                val splashImage = if (windowSize.isTablet) {
                    Res.drawable.sproutelle_splash_tablet
                } else {
                    Res.drawable.sproutelle_splash
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(splashImage),
                        contentDescription = "Sproutelle Splash Artwork",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Loading Card container matching design reference
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(
                                bottom = if (windowSize.isTablet) SproutelleSpacing.Huge else SproutelleSpacing.XXLarge
                            )
                            .padding(horizontal = SproutelleSpacing.XLarge)
                            .widthIn(max = 260.dp)
                            .graphicsLayer { alpha = cardAlpha.value },
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = SproutelleDimens.MobileContentPadding,
                                        vertical = SproutelleSpacing.Medium
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(SproutelleSpacing.Small)
                            ) {
                                Text(
                                    text = "Loading...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold
                                )
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(SproutelleDimens.ProgressBarHeight / 2)
                                        .clip(RoundedCornerShape(2.5.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* --- Multi-Device Previews --- */

@Preview(name = "Phone Format", device = "spec:width=411dp,height=891dp", showBackground = true)
@Composable
fun SplashScreenPhonePreview() {
    SproutelleTheme {}
}

@Preview(name = "Tablet Format", device = "spec:width=1280dp,height=800dp", showBackground = true)
@Composable
fun SplashScreenTabletPreview() {
    SproutelleTheme {}
}
