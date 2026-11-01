package com.hathway.sproutelle.data.datasource

import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.Subcategory

interface LearningCacheStorage {
    suspend fun saveSubcategories(explorerId: String, items: List<Subcategory>)
    suspend fun loadSubcategories(explorerId: String): List<Subcategory>
    fun loadSubcategoriesSync(explorerId: String): List<Subcategory>
    suspend fun saveGames(subcategoryId: String, items: List<GameItem>)
    suspend fun loadGames(subcategoryId: String): List<GameItem>
    fun loadGamesSync(subcategoryId: String): List<GameItem>
    suspend fun saveQuestions(subcategoryId: String, gameId: String, items: List<Question>)
    suspend fun loadQuestions(subcategoryId: String, gameId: String): List<Question>
    fun loadQuestionsSync(subcategoryId: String, gameId: String): List<Question>
}

expect class LearningCache() : LearningCacheStorage {
    override suspend fun saveSubcategories(explorerId: String, items: List<Subcategory>)
    override suspend fun loadSubcategories(explorerId: String): List<Subcategory>
    override fun loadSubcategoriesSync(explorerId: String): List<Subcategory>
    override suspend fun saveGames(subcategoryId: String, items: List<GameItem>)
    override suspend fun loadGames(subcategoryId: String): List<GameItem>
    override fun loadGamesSync(subcategoryId: String): List<GameItem>
    override suspend fun saveQuestions(subcategoryId: String, gameId: String, items: List<Question>)
    override suspend fun loadQuestions(subcategoryId: String, gameId: String): List<Question>
    override fun loadQuestionsSync(subcategoryId: String, gameId: String): List<Question>
}
