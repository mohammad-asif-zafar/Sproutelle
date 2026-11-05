package com.hathway.sproutelle.core.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data class Games(
        val explorerId: String,
        val explorerTitle: String
    ) : Screen("games/$explorerId")
    data class GameShell(
        val explorerId: String,
        val explorerTitle: String,
        val subcategoryId: String,
        val gameId: String,
        val gameName: String,
        val subcategoryName: String
    ) : Screen("game_shell/$explorerId/$subcategoryId/$gameId")
    data class Question(
        val explorerId: String,
        val explorerTitle: String,
        val subcategoryId: String,
        val gameId: String,
        val gameName: String,
        val subcategoryName: String
    ) : Screen("question/$explorerId/$subcategoryId/$gameId")
}
