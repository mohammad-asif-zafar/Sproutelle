package com.hathway.sproutelle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.hathway.sproutelle.core.navigation.Screen
import com.hathway.sproutelle.data.datasource.SupabaseExplorerRemoteDataSource
import com.hathway.sproutelle.data.repository.ExplorerRepositoryImpl
import com.hathway.sproutelle.data.repository.HomeRepositoryImpl
import com.hathway.sproutelle.data.repository.UserPreferencesRepositoryImpl
import com.hathway.sproutelle.domain.usecase.CheckOnboardingStatusUseCase
import com.hathway.sproutelle.domain.usecase.CompleteOnboardingUseCase
import com.hathway.sproutelle.domain.usecase.GetExplorerItemsUseCase
import com.hathway.sproutelle.domain.usecase.GetHomeDataUseCase
import com.hathway.sproutelle.presentation.home.HomeScreen
import com.hathway.sproutelle.presentation.home.HomeViewModel
import com.hathway.sproutelle.presentation.onboarding.OnboardingScreen
import com.hathway.sproutelle.presentation.onboarding.OnboardingViewModel
import com.hathway.sproutelle.presentation.splash.SplashScreen
import com.hathway.sproutelle.presentation.splash.SplashViewModel
import theme.SproutelleTheme

@Composable
@Preview
fun App() {
    // Dependencies Setup (Clean Architecture & SOLID Dependency Injection)
    val userPreferencesRepository = remember { UserPreferencesRepositoryImpl() }
    val explorerRemoteDataSource = remember { SupabaseExplorerRemoteDataSource() }
    val explorerRepository = remember { ExplorerRepositoryImpl(explorerRemoteDataSource) }
    val homeRepository = remember { HomeRepositoryImpl(explorerRepository) }

    val checkOnboardingStatusUseCase = remember { CheckOnboardingStatusUseCase(userPreferencesRepository) }
    val completeOnboardingUseCase = remember { CompleteOnboardingUseCase(userPreferencesRepository) }
    val getHomeDataUseCase = remember { GetHomeDataUseCase(homeRepository) }
    val getExplorerItemsUseCase = remember { GetExplorerItemsUseCase(explorerRepository) }

    val splashViewModel = remember { SplashViewModel(checkOnboardingStatusUseCase) }
    val onboardingViewModel = remember { OnboardingViewModel(completeOnboardingUseCase) }
    val homeViewModel = remember { HomeViewModel(getHomeDataUseCase, getExplorerItemsUseCase) }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

    SproutelleTheme {
        when (currentScreen) {
            is Screen.Splash -> {
                SplashScreen(
                    viewModel = splashViewModel,
                    onNavigate = { destination ->
                        currentScreen = destination
                    }
                )
            }
            is Screen.Onboarding -> {
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    onNavigateHome = {
                        currentScreen = Screen.Home
                    }
                )
            }
            is Screen.Home -> {
                HomeScreen(viewModel = homeViewModel)
            }
        }
    }
}
