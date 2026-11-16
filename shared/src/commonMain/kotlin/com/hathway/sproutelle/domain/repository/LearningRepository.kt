package com.hathway.sproutelle.domain.repository

import com.hathway.sproutelle.domain.model.GameConfig
import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.domain.model.GameLevel
import com.hathway.sproutelle.domain.model.GameProgress
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.Subcategory
import com.hathway.sproutelle.domain.model.SubcategoryWithGames
import kotlinx.coroutines.flow.Flow

sealed interface LearningState<out T> {
    data object Loading : LearningState<Nothing>
    data class Cached<T>(val data: T) : LearningState<T>
    data class Fresh<T>(val data: T) : LearningState<T>
    data class Offline<T>(val data: T, val message: String? = null) : LearningState<T>
    data class Error<T>(val message: String, val cachedData: T? = null) : LearningState<T>
}

interface LearningRepository {
    fun getSubcategories(explorerId: String): Flow<List<Subcategory>>
    fun getSubcategoriesState(explorerId: String): Flow<LearningState<List<Subcategory>>>
    suspend fun refreshSubcategories(explorerId: String): Result<Unit>

    fun getGamesForSubcategory(subcategoryId: String): Flow<List<GameItem>>
    fun getGamesForSubcategoryState(subcategoryId: String): Flow<LearningState<List<GameItem>>>
    suspend fun refreshGamesForSubcategory(subcategoryId: String): Result<Unit>

    fun getGamesForExplorer(explorerId: String): Flow<List<SubcategoryWithGames>>
    fun getGamesForExplorerState(explorerId: String): Flow<LearningState<List<SubcategoryWithGames>>>
    suspend fun refreshGamesForExplorer(explorerId: String): Result<Unit>

    fun getQuestions(subcategoryId: String, gameId: String): Flow<List<Question>>
    fun getQuestionsState(subcategoryId: String, gameId: String): Flow<LearningState<List<Question>>>
    suspend fun refreshQuestions(subcategoryId: String, gameId: String): Result<Unit>

    fun getGameConfigState(gameId: String): Flow<LearningState<GameConfig>>
    suspend fun refreshGameConfig(gameId: String): Result<Unit>

    fun getGameLevelsState(gameId: String): Flow<LearningState<List<GameLevel>>>
    suspend fun refreshGameLevels(gameId: String): Result<Unit>

    suspend fun saveGameProgress(progress: GameProgress)
    suspend fun getGameProgress(childId: String, gameId: String, levelId: String?): GameProgress?

    fun preloadSubcategories(explorerId: String)
    fun preloadGames(subcategoryId: String)
    fun preloadGamesForExplorer(explorerId: String)
    fun preloadQuestions(subcategoryId: String, gameId: String)
}
