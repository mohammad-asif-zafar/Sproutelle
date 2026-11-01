package com.hathway.sproutelle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.hathway.sproutelle.core.navigation.Screen
import com.hathway.sproutelle.data.datasource.SupabaseExplorerRemoteDataSource
import com.hathway.sproutelle.data.datasource.SupabaseLearningRemoteDataSource
import com.hathway.sproutelle.data.repository.ExplorerRepositoryImpl
import com.hathway.sproutelle.data.repository.HomeRepositoryImpl
import com.hathway.sproutelle.data.repository.LearningRepositoryImpl
import com.hathway.sproutelle.data.repository.UserPreferencesRepositoryImpl
import com.hathway.sproutelle.domain.usecase.CheckOnboardingStatusUseCase
import com.hathway.sproutelle.domain.usecase.CompleteOnboardingUseCase
import com.hathway.sproutelle.domain.usecase.GetExplorerItemsUseCase
import com.hathway.sproutelle.domain.usecase.GetGamesForSubcategoryUseCase
import com.hathway.sproutelle.domain.usecase.GetHomeDataUseCase
import com.hathway.sproutelle.domain.usecase.GetQuestionsUseCase
import com.hathway.sproutelle.domain.usecase.GetSubcategoriesUseCase
import com.hathway.sproutelle.presentation.games.GameSelectionScreen
import com.hathway.sproutelle.presentation.games.GameSelectionUiState
import com.hathway.sproutelle.presentation.games.GameSelectionViewModel
import com.hathway.sproutelle.presentation.games.GameShellScreen
import com.hathway.sproutelle.presentation.games.GameShellViewModel
import com.hathway.sproutelle.presentation.home.HomeScreen
import com.hathway.sproutelle.presentation.home.HomeViewModel
import com.hathway.sproutelle.presentation.onboarding.OnboardingScreen
import com.hathway.sproutelle.presentation.onboarding.OnboardingViewModel
import com.hathway.sproutelle.presentation.question.QuestionScreen
import com.hathway.sproutelle.presentation.question.QuestionViewModel
import com.hathway.sproutelle.presentation.splash.SplashScreen
import com.hathway.sproutelle.presentation.splash.SplashViewModel
import com.hathway.sproutelle.presentation.subcategory.SubcategoryScreen
import com.hathway.sproutelle.presentation.subcategory.SubcategoryViewModel
import theme.SproutelleTheme

@Composable
@Preview
fun App() {
    // Dependencies Setup (Clean Architecture & SOLID Dependency Injection)
    val userPreferencesRepository = remember { UserPreferencesRepositoryImpl() }
    val explorerRemoteDataSource = remember { SupabaseExplorerRemoteDataSource() }
    val explorerRepository = remember { ExplorerRepositoryImpl(explorerRemoteDataSource) }
    val homeRepository = remember { HomeRepositoryImpl(explorerRepository) }

    val learningRemoteDataSource = remember { SupabaseLearningRemoteDataSource() }
    val learningRepository = remember { LearningRepositoryImpl(learningRemoteDataSource) }

    val checkOnboardingStatusUseCase = remember { CheckOnboardingStatusUseCase(userPreferencesRepository) }
    val completeOnboardingUseCase = remember { CompleteOnboardingUseCase(userPreferencesRepository) }
    val getHomeDataUseCase = remember { GetHomeDataUseCase(homeRepository) }
    val getExplorerItemsUseCase = remember { GetExplorerItemsUseCase(explorerRepository) }

    val getSubcategoriesUseCase = remember { GetSubcategoriesUseCase(learningRepository) }
    val getGamesForSubcategoryUseCase = remember { GetGamesForSubcategoryUseCase(learningRepository) }
    val getQuestionsUseCase = remember { GetQuestionsUseCase(learningRepository) }

    val splashViewModel = remember { SplashViewModel(checkOnboardingStatusUseCase) }
    val onboardingViewModel = remember { OnboardingViewModel(completeOnboardingUseCase) }
    val homeViewModel = remember { HomeViewModel(getHomeDataUseCase, getExplorerItemsUseCase) }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

    SproutelleTheme {
        when (val screen = currentScreen) {
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
                HomeScreen(
                    viewModel = homeViewModel,
                    onCategoryClick = { explorerItem ->
                        learningRepository.preloadSubcategories(explorerItem.id)
                        currentScreen = Screen.Subcategories(
                            explorerId = explorerItem.id,
                            explorerTitle = explorerItem.titleName
                        )
                    }
                )
            }
            is Screen.Subcategories -> {
                val subViewModel = remember(screen.explorerId) {
                    SubcategoryViewModel(
                        explorerId = screen.explorerId,
                        explorerTitle = screen.explorerTitle,
                        learningRepository = learningRepository
                    )
                }
                SubcategoryScreen(
                    viewModel = subViewModel,
                    onBackClick = {
                        currentScreen = Screen.Home
                    },
                    onSubcategoryClick = { subcategory ->
                        learningRepository.preloadGames(subcategory.id)
                        currentScreen = Screen.GameSelection(
                            explorerId = screen.explorerId,
                            explorerTitle = screen.explorerTitle,
                            subcategoryId = subcategory.id,
                            subcategoryName = subcategory.name
                        )
                    }
                )
            }
            is Screen.GameSelection -> {
                val gameViewModel = remember(screen.subcategoryId) {
                    GameSelectionViewModel(
                        explorerId = screen.explorerId,
                        subcategoryId = screen.subcategoryId,
                        subcategoryName = screen.subcategoryName,
                        learningRepository = learningRepository
                    )
                }
                GameSelectionScreen(
                    viewModel = gameViewModel,
                    onBackClick = {
                        currentScreen = Screen.Subcategories(
                            explorerId = screen.explorerId,
                            explorerTitle = screen.explorerTitle
                        )
                    },
                    onGameClick = { gameId, subcategoryId ->
                        val selectedGameName = gameViewModel.uiState.value.let {
                            if (it is GameSelectionUiState.Success) {
                                it.games.find { g -> g.gameId == gameId }?.name ?: "Game"
                            } else "Game"
                        }
                        learningRepository.preloadQuestions(subcategoryId, gameId)
                        currentScreen = Screen.Question(
                            explorerId = screen.explorerId,
                            explorerTitle = screen.explorerTitle,
                            subcategoryId = subcategoryId,
                            gameId = gameId,
                            gameName = selectedGameName,
                            subcategoryName = screen.subcategoryName
                        )
                    }
                )
            }
            is Screen.Question -> {
                val questionViewModel = remember(screen.subcategoryId, screen.gameId) {
                    QuestionViewModel(
                        explorerId = screen.explorerId,
                        subcategoryId = screen.subcategoryId,
                        gameId = screen.gameId,
                        gameName = screen.gameName,
                        learningRepository = learningRepository
                    )
                }
                QuestionScreen(
                    viewModel = questionViewModel,
                    onBackClick = {
                        currentScreen = Screen.GameSelection(
                            explorerId = screen.explorerId,
                            explorerTitle = screen.explorerTitle,
                            subcategoryId = screen.subcategoryId,
                            subcategoryName = screen.subcategoryName
                        )
                    }
                )
            }
            is Screen.GameShell -> {
                val gameShellViewModel = remember(screen.subcategoryId, screen.gameId) {
                    GameShellViewModel(
                        subcategoryId = screen.subcategoryId,
                        gameId = screen.gameId,
                        initialGameName = screen.gameName
                    )
                }
                GameShellScreen(
                    viewModel = gameShellViewModel,
                    onBackClick = {
                        currentScreen = Screen.GameSelection(
                            explorerId = screen.explorerId,
                            explorerTitle = screen.explorerTitle,
                            subcategoryId = screen.subcategoryId,
                            subcategoryName = screen.subcategoryName
                        )
                    }
                )
            }
        }
    }
}
