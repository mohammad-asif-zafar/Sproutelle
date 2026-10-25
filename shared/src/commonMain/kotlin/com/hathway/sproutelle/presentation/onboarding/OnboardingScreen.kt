package com.hathway.sproutelle.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hathway.sproutelle.core.responsive.ProvideWindowSizeClass
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import sproutelle.shared.generated.resources.Res
import sproutelle.shared.generated.resources.sproutelle_onboarding_learn_landscape
import sproutelle.shared.generated.resources.sproutelle_onboarding_learn_portrait
import sproutelle.shared.generated.resources.sproutelle_onboarding_safety_landscape
import sproutelle.shared.generated.resources.sproutelle_onboarding_safety_portrait
import sproutelle.shared.generated.resources.sproutelle_onboarding_welcome_landscape
import sproutelle.shared.generated.resources.sproutelle_onboarding_welcome_portrait
import theme.SproutelleColors
import theme.SproutelleDimens
import theme.SproutelleShapes
import theme.SproutelleSpacing

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onNavigateHome: () -> Unit
) {
    val pages = viewModel.pages
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageSelected(pagerState.currentPage)
    }

    LaunchedEffect(viewModel) {
        viewModel.onboardingCompleted.collectLatest {
            onNavigateHome()
        }
    }

    ProvideWindowSizeClass { windowSize ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(SproutelleColors.Cream)
        ) {
            val useLandscapeArtwork = windowSize.isTablet && maxWidth > maxHeight

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                val imageRes = onboardingArtwork(
                    pageIndex = pageIndex,
                    landscape = useLandscapeArtwork
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = pages[pageIndex].title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )
                }
            }

            // Native Compose controls overlay (Skip, Page Indicator Dots, and Get Started on last page)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(
                        horizontal = if (useLandscapeArtwork) 28.dp else 20.dp,
                        vertical = if (useLandscapeArtwork) 16.dp else 12.dp
                    ),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.titleMedium,
                        color = SproutelleColors.Ink,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.onSkipClicked() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (useLandscapeArtwork) {
                                Modifier.padding(horizontal = 12.dp)
                            } else {
                                Modifier
                            }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PageIndicatorDots(
                        pageCount = pages.size,
                        currentPageIndex = pagerState.currentPage,
                        onPageSelect = { targetPage ->
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(targetPage)
                            }
                        }
                    )

                    if (pagerState.currentPage == pages.lastIndex) {
                        Spacer(modifier = Modifier.height(SproutelleSpacing.Large))

                        Button(
                            onClick = { viewModel.onNextClicked() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(SproutelleDimens.ButtonHeight),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SproutelleColors.LeafGreen,
                                contentColor = SproutelleColors.OnPrimary
                            ),
                            shape = SproutelleShapes.Pill
                        ) {
                            Text(
                                text = "Get Started",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun onboardingArtwork(
    pageIndex: Int,
    landscape: Boolean
): DrawableResource = when (pageIndex) {
    0 -> if (landscape) {
        Res.drawable.sproutelle_onboarding_welcome_landscape
    } else {
        Res.drawable.sproutelle_onboarding_welcome_portrait
    }
    1 -> if (landscape) {
        Res.drawable.sproutelle_onboarding_learn_landscape
    } else {
        Res.drawable.sproutelle_onboarding_learn_portrait
    }
    else -> if (landscape) {
        Res.drawable.sproutelle_onboarding_safety_landscape
    } else {
        Res.drawable.sproutelle_onboarding_safety_portrait
    }
}

@Composable
private fun PageIndicatorDots(
    pageCount: Int,
    currentPageIndex: Int,
    onPageSelect: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until pageCount) {
            val isSelected = i == currentPageIndex
            Box(
                modifier = Modifier
                    .size(
                        width = if (isSelected) 24.dp else 8.dp,
                        height = 8.dp
                    )
                    .clip(CircleShape)
                    .background(
                        if (isSelected) SproutelleColors.LeafGreen
                        else SproutelleColors.Disabled
                    )
                    .clickable { onPageSelect(i) }
            )
        }
    }
}


/* --- Previews for All Onboarding Images --- */

@Preview(widthDp = 390, heightDp = 844, name = "Welcome - Mobile Portrait")
@Composable
private fun OnboardingWelcomeMobilePreview() {
    MaterialTheme {
        OnboardingStaticPreview(
            imageRes = Res.drawable.sproutelle_onboarding_welcome_portrait,
            isLandscape = false
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, name = "Learn - Mobile Portrait")
@Composable
private fun OnboardingLearnMobilePreview() {
    MaterialTheme {
        OnboardingStaticPreview(
            imageRes = Res.drawable.sproutelle_onboarding_learn_portrait,
            isLandscape = false
        )
    }
}

@Preview(widthDp = 390, heightDp = 844, name = "Safety - Mobile Portrait")
@Composable
private fun OnboardingSafetyMobilePreview() {
    MaterialTheme {
        OnboardingStaticPreview(
            imageRes = Res.drawable.sproutelle_onboarding_safety_portrait,
            isLandscape = false
        )
    }
}

@Preview(widthDp = 1024, heightDp = 768, name = "Welcome - Tablet Landscape")
@Composable
private fun OnboardingWelcomeTabletPreview() {
    MaterialTheme {
        OnboardingStaticPreview(
            imageRes = Res.drawable.sproutelle_onboarding_welcome_landscape,
            isLandscape = true
        )
    }
}

@Preview(widthDp = 1024, heightDp = 768, name = "Learn - Tablet Landscape")
@Composable
private fun OnboardingLearnTabletPreview() {
    MaterialTheme {
        OnboardingStaticPreview(
            imageRes = Res.drawable.sproutelle_onboarding_learn_landscape,
            isLandscape = true
        )
    }
}

@Preview(widthDp = 1024, heightDp = 768, name = "Safety - Tablet Landscape")
@Composable
private fun OnboardingSafetyTabletPreview() {
    MaterialTheme {
        OnboardingStaticPreview(
            imageRes = Res.drawable.sproutelle_onboarding_safety_landscape,
            isLandscape = true
        )
    }
}

@Composable
private fun OnboardingStaticPreview(
    imageRes: DrawableResource,
    isLandscape: Boolean
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SproutelleColors.Cream)
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = "Sproutelle onboarding preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = if (isLandscape) 28.dp else 20.dp,
                    vertical = if (isLandscape) 16.dp else 12.dp
                ),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.titleMedium,
                    color = SproutelleColors.Ink,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(
                                    width = if (index == 0) 24.dp else 8.dp,
                                    height = 8.dp
                                )
                                .clip(CircleShape)
                                .background(
                                    if (index == 0) SproutelleColors.LeafGreen
                                    else SproutelleColors.Disabled
                                )
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(SproutelleSpacing.Large)
                )

                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth(if (isLandscape) 0.72f else 1f)
                        .height(SproutelleDimens.ButtonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SproutelleColors.LeafGreen,
                        contentColor = SproutelleColors.OnPrimary
                    ),
                    shape = SproutelleShapes.Pill
                ) {
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
