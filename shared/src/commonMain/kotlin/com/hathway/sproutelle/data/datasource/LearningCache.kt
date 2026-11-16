package com.hathway.sproutelle.data.datasource

import com.hathway.sproutelle.domain.model.GameConfig
import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.domain.model.GameLevel
import com.hathway.sproutelle.domain.model.GameProgress
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.Subcategory
import com.hathway.sproutelle.domain.model.SubcategoryWithGames

interface LearningCacheStorage {
    suspend fun saveSubcategories(explorerId: String, items: List<Subcategory>)
    suspend fun loadSubcategories(explorerId: String): List<Subcategory>
    fun loadSubcategoriesSync(explorerId: String): List<Subcategory>

    suspend fun saveGames(subcategoryId: String, items: List<GameItem>)
    suspend fun loadGames(subcategoryId: String): List<GameItem>
    fun loadGamesSync(subcategoryId: String): List<GameItem>

    suspend fun saveGamesForExplorer(explorerId: String, items: List<SubcategoryWithGames>)
    suspend fun loadGamesForExplorer(explorerId: String): List<SubcategoryWithGames>
    fun loadGamesForExplorerSync(explorerId: String): List<SubcategoryWithGames>

    suspend fun saveQuestions(subcategoryId: String, gameId: String, items: List<Question>)
    suspend fun loadQuestions(subcategoryId: String, gameId: String): List<Question>
    fun loadQuestionsSync(subcategoryId: String, gameId: String): List<Question>

    suspend fun saveGameConfig(gameId: String, config: GameConfig)
    suspend fun loadGameConfig(gameId: String): GameConfig?

    suspend fun saveGameLevels(gameId: String, levels: List<GameLevel>)
    suspend fun loadGameLevels(gameId: String): List<GameLevel>

    suspend fun saveGameProgress(progress: GameProgress)
    suspend fun loadGameProgress(childId: String, gameId: String, levelId: String?): GameProgress?
}

expect class LearningCache() : LearningCacheStorage {
    override suspend fun saveSubcategories(explorerId: String, items: List<Subcategory>)
    override suspend fun loadSubcategories(explorerId: String): List<Subcategory>
    override fun loadSubcategoriesSync(explorerId: String): List<Subcategory>

    override suspend fun saveGames(subcategoryId: String, items: List<GameItem>)
    override suspend fun loadGames(subcategoryId: String): List<GameItem>
    override fun loadGamesSync(subcategoryId: String): List<GameItem>

    override suspend fun saveGamesForExplorer(explorerId: String, items: List<SubcategoryWithGames>)
    override suspend fun loadGamesForExplorer(explorerId: String): List<SubcategoryWithGames>
    override fun loadGamesForExplorerSync(explorerId: String): List<SubcategoryWithGames>

    override suspend fun saveQuestions(subcategoryId: String, gameId: String, items: List<Question>)
    override suspend fun loadQuestions(subcategoryId: String, gameId: String): List<Question>
    override fun loadQuestionsSync(subcategoryId: String, gameId: String): List<Question>

    override suspend fun saveGameConfig(gameId: String, config: GameConfig)
    override suspend fun loadGameConfig(gameId: String): GameConfig?

    override suspend fun saveGameLevels(gameId: String, levels: List<GameLevel>)
    override suspend fun loadGameLevels(gameId: String): List<GameLevel>

    override suspend fun saveGameProgress(progress: GameProgress)
    override suspend fun loadGameProgress(childId: String, gameId: String, levelId: String?): GameProgress?
}
