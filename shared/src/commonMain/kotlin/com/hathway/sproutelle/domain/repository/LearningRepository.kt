package com.hathway.sproutelle.domain.repository

import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.Subcategory
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

    fun getQuestions(subcategoryId: String, gameId: String): Flow<List<Question>>
    fun getQuestionsState(subcategoryId: String, gameId: String): Flow<LearningState<List<Question>>>
    suspend fun refreshQuestions(subcategoryId: String, gameId: String): Result<Unit>

    fun preloadSubcategories(explorerId: String)
    fun preloadGames(subcategoryId: String)
    fun preloadQuestions(subcategoryId: String, gameId: String)
}
