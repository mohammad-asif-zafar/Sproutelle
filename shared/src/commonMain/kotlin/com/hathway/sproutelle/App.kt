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
import com.hathway.sproutelle.domain.usecase.GetGamesForExplorerUseCase
import com.hathway.sproutelle.domain.usecase.GetHomeDataUseCase
import com.hathway.sproutelle.domain.usecase.GetQuestionsUseCase
import com.hathway.sproutelle.presentation.games.GameShellScreen
import com.hathway.sproutelle.presentation.games.GameShellViewModel
import com.hathway.sproutelle.presentation.games.GamesScreen
import com.hathway.sproutelle.presentation.games.GamesViewModel
import com.hathway.sproutelle.presentation.home.HomeScreen
import com.hathway.sproutelle.presentation.home.HomeViewModel
import com.hathway.sproutelle.presentation.onboarding.OnboardingScreen
import com.hathway.sproutelle.presentation.onboarding.OnboardingViewModel
import com.hathway.sproutelle.presentation.picturequest.PictureQuestScreen
import com.hathway.sproutelle.presentation.picturequest.PictureQuestViewModel
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

    val learningRemoteDataSource = remember { SupabaseLearningRemoteDataSource() }
    val learningRepository = remember { LearningRepositoryImpl(learningRemoteDataSource) }

    val checkOnboardingStatusUseCase = remember { CheckOnboardingStatusUseCase(userPreferencesRepository) }
    val completeOnboardingUseCase = remember { CompleteOnboardingUseCase(userPreferencesRepository) }
    val getHomeDataUseCase = remember { GetHomeDataUseCase(homeRepository) }
    val getExplorerItemsUseCase = remember { GetExplorerItemsUseCase(explorerRepository) }

    val getGamesForExplorerUseCase = remember { GetGamesForExplorerUseCase(learningRepository) }
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
                        learningRepository.preloadGamesForExplorer(explorerItem.id)
                        currentScreen = Screen.Games(
                            explorerId = explorerItem.id,
                            explorerTitle = explorerItem.titleName
                        )
                    }
                )
            }
            is Screen.Games -> {
                SystemBackHandler {
                    currentScreen = Screen.Home
                }
                val gamesViewModel = remember(screen.explorerId) {
                    GamesViewModel(
                        explorerId = screen.explorerId,
                        explorerTitle = screen.explorerTitle,
                        learningRepository = learningRepository
                    )
                }
                GamesScreen(
                    viewModel = gamesViewModel,
                    onBackClick = {
                        currentScreen = Screen.Home
                    },
                    onGameClick = { gameId, subcategoryId, gameName, subcategoryName ->
                        learningRepository.preloadQuestions(subcategoryId, gameId)
                        val isWordGame = gameId.contains("word", ignoreCase = true) || gameName.contains("word", ignoreCase = true)
                        val isMatchGame = gameId.contains("match", ignoreCase = true) || gameName.contains("match", ignoreCase = true)
                        val isNumberGame = gameId.contains("number", ignoreCase = true) || gameName.contains("number", ignoreCase = true)
                        currentScreen = when {
                            isWordGame -> Screen.WordBuilder(
                                explorerId = screen.explorerId,
                                explorerTitle = screen.explorerTitle,
                                subcategoryId = subcategoryId,
                                gameId = gameId,
                                gameName = gameName,
                                subcategoryName = subcategoryName
                            )
                            isMatchGame -> Screen.MatchAndDiscover(
                                explorerId = screen.explorerId,
                                explorerTitle = screen.explorerTitle,
                                subcategoryId = subcategoryId,
                                gameId = gameId,
                                gameName = gameName,
                                subcategoryName = subcategoryName
                            )
                            isNumberGame -> Screen.NumberQuest(
                                explorerId = screen.explorerId,
                                explorerTitle = screen.explorerTitle,
                                subcategoryId = subcategoryId,
                                gameId = gameId,
                                gameName = gameName,
                                subcategoryName = subcategoryName
                            )
                            else -> Screen.Question(
                                explorerId = screen.explorerId,
                                explorerTitle = screen.explorerTitle,
                                subcategoryId = subcategoryId,
                                gameId = gameId,
                                gameName = gameName,
                                subcategoryName = subcategoryName
                            )
                        }
                    }
                )
            }
            is Screen.Question -> {
                SystemBackHandler {
                    currentScreen = Screen.Games(
                        explorerId = screen.explorerId,
                        explorerTitle = screen.explorerTitle
                    )
                }
                val pictureQuestViewModel = remember(screen.subcategoryId, screen.gameId) {
                    PictureQuestViewModel(
                        explorerId = screen.explorerId,
                        subcategoryId = screen.subcategoryId,
                        gameId = screen.gameId,
                        gameName = screen.gameName.ifBlank { "Picture Quest" },
                        learningRepository = learningRepository
                    )
                }
                PictureQuestScreen(
                    viewModel = pictureQuestViewModel,
                    onBackClick = {
                        currentScreen = Screen.Games(
                            explorerId = screen.explorerId,
                            explorerTitle = screen.explorerTitle
                        )
                    }
                )
            }
            is Screen.WordBuilder -> {
                SystemBackHandler {
                    currentScreen = Screen.Games(
                        explorerId = screen.explorerId,
                        explorerTitle = screen.explorerTitle
                    )
                }
                val wordBuilderViewModel = remember(screen.subcategoryId, screen.gameId) {
                    com.hathway.sproutelle.presentation.wordbuilder.WordBuilderViewModel(
                        explorerId = screen.explorerId,
                        subcategoryId = screen.subcategoryId,
                        gameId = screen.gameId,
                        gameName = screen.gameName.ifBlank { "Word Builder" },
                        learningRepository = learningRepository
                    )
                }
                com.hathway.sproutelle.presentation.wordbuilder.WordBuilderScreen(
                    viewModel = wordBuilderViewModel,
                    onBackClick = {
                        currentScreen = Screen.Games(
                            explorerId = screen.explorerId,
                            explorerTitle = screen.explorerTitle
                        )
                    },
                    onHomeClick = {
                        currentScreen = Screen.Home
                    }
                )
            }
            is Screen.MatchAndDiscover -> {
                SystemBackHandler {
                    currentScreen = Screen.Games(
                        explorerId = screen.explorerId,
                        explorerTitle = screen.explorerTitle
                    )
                }
                val matchViewModel = remember(screen.subcategoryId, screen.gameId) {
                    com.hathway.sproutelle.presentation.matchanddiscover.MatchAndDiscoverViewModel(
                        explorerId = screen.explorerId,
                        subcategoryId = screen.subcategoryId,
                        gameId = screen.gameId,
                        gameName = screen.gameName.ifBlank { "Match & Discover" },
                        learningRepository = learningRepository
                    )
                }
                com.hathway.sproutelle.presentation.matchanddiscover.MatchAndDiscoverScreen(
                    viewModel = matchViewModel,
                    onBackClick = {
                        currentScreen = Screen.Games(
                            explorerId = screen.explorerId,
                            explorerTitle = screen.explorerTitle
                        )
                    },
                    onHomeClick = {
                        currentScreen = Screen.Home
                    }
                )
            }
            is Screen.NumberQuest -> {
                SystemBackHandler {
                    currentScreen = Screen.Games(
                        explorerId = screen.explorerId,
                        explorerTitle = screen.explorerTitle
                    )
                }
                val numberViewModel = remember(screen.subcategoryId, screen.gameId) {
                    com.hathway.sproutelle.presentation.numberquest.NumberQuestViewModel(
                        explorerId = screen.explorerId,
                        subcategoryId = screen.subcategoryId,
                        gameId = screen.gameId,
                        gameName = screen.gameName.ifBlank { "Number Quest" },
                        learningRepository = learningRepository
                    )
                }
                com.hathway.sproutelle.presentation.numberquest.NumberQuestScreen(
                    viewModel = numberViewModel,
                    onBackClick = {
                        currentScreen = Screen.Games(
                            explorerId = screen.explorerId,
                            explorerTitle = screen.explorerTitle
                        )
                    },
                    onHomeClick = {
                        currentScreen = Screen.Home
                    }
                )
            }
            is Screen.GameShell -> {
                SystemBackHandler {
                    currentScreen = Screen.Games(
                        explorerId = screen.explorerId,
                        explorerTitle = screen.explorerTitle
                    )
                }
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
                        currentScreen = Screen.Games(
                            explorerId = screen.explorerId,
                            explorerTitle = screen.explorerTitle
                        )
                    }
                )
            }
        }
    }
}
