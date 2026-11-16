package com.hathway.sproutelle.data.datasource

import com.hathway.sproutelle.domain.model.GameConfig
import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.domain.model.GameLevel
import com.hathway.sproutelle.domain.model.GameProgress
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.Subcategory
import com.hathway.sproutelle.domain.model.SubcategoryWithGames
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

actual class LearningCache : LearningCacheStorage {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    actual override suspend fun saveSubcategories(explorerId: String, items: List<Subcategory>) {
        saveSubcategoriesSync(explorerId, items)
    }

    actual override suspend fun loadSubcategories(explorerId: String): List<Subcategory> {
        return loadSubcategoriesSync(explorerId)
    }

    actual override fun loadSubcategoriesSync(explorerId: String): List<Subcategory> {
        return try {
            val file = getCacheFile("sproutelle_subcategories_${explorerId}.json") ?: return emptyList()
            if (!file.exists()) return emptyList()
            val text = file.readText()
            json.decodeFromString<List<Subcategory>>(text)
        } catch (e: Exception) {
            println("LearningCache: Error loading subcategories cache: ${e.message}")
            emptyList()
        }
    }

    actual override suspend fun saveGames(subcategoryId: String, items: List<GameItem>) {
        saveGamesSync(subcategoryId, items)
    }

    actual override suspend fun loadGames(subcategoryId: String): List<GameItem> {
        return loadGamesSync(subcategoryId)
    }

    actual override fun loadGamesSync(subcategoryId: String): List<GameItem> {
        return try {
            val file = getCacheFile("sproutelle_games_${subcategoryId}.json") ?: return emptyList()
            if (!file.exists()) return emptyList()
            val text = file.readText()
            json.decodeFromString<List<GameItem>>(text)
        } catch (e: Exception) {
            println("LearningCache: Error loading games cache: ${e.message}")
            emptyList()
        }
    }

    actual override suspend fun saveGamesForExplorer(explorerId: String, items: List<SubcategoryWithGames>) {
        saveGamesForExplorerSync(explorerId, items)
    }

    actual override suspend fun loadGamesForExplorer(explorerId: String): List<SubcategoryWithGames> {
        return loadGamesForExplorerSync(explorerId)
    }

    actual override fun loadGamesForExplorerSync(explorerId: String): List<SubcategoryWithGames> {
        return try {
            val file = getCacheFile("sproutelle_explorer_games_${explorerId}.json") ?: return emptyList()
            if (!file.exists()) return emptyList()
            val text = file.readText()
            json.decodeFromString<List<SubcategoryWithGames>>(text)
        } catch (e: Exception) {
            println("LearningCache: Error loading explorer games cache: ${e.message}")
            emptyList()
        }
    }

    actual override suspend fun saveQuestions(subcategoryId: String, gameId: String, items: List<Question>) {
        saveQuestionsSync(subcategoryId, gameId, items)
    }

    actual override suspend fun loadQuestions(subcategoryId: String, gameId: String): List<Question> {
        return loadQuestionsSync(subcategoryId, gameId)
    }

    actual override fun loadQuestionsSync(subcategoryId: String, gameId: String): List<Question> {
        return try {
            val file = getCacheFile("sproutelle_questions_${subcategoryId}_${gameId}.json") ?: return emptyList()
            if (!file.exists()) return emptyList()
            val text = file.readText()
            json.decodeFromString<List<Question>>(text)
        } catch (e: Exception) {
            println("LearningCache: Error loading questions cache: ${e.message}")
            emptyList()
        }
    }

    actual override suspend fun saveGameConfig(gameId: String, config: GameConfig) {
        saveObjectToFile("sproutelle_config_${gameId}.json", config)
    }

    actual override suspend fun loadGameConfig(gameId: String): GameConfig? {
        return loadObjectFromFile("sproutelle_config_${gameId}.json")
    }

    actual override suspend fun saveGameLevels(gameId: String, levels: List<GameLevel>) {
        saveObjectToFile("sproutelle_levels_${gameId}.json", levels)
    }

    actual override suspend fun loadGameLevels(gameId: String): List<GameLevel> {
        return loadObjectFromFile<List<GameLevel>>("sproutelle_levels_${gameId}.json") ?: emptyList()
    }

    actual override suspend fun saveGameProgress(progress: GameProgress) {
        val fileName = "sproutelle_progress_${progress.childId}_${progress.gameId}_${progress.levelId ?: "default"}.json"
        saveObjectToFile(fileName, progress)
    }

    actual override suspend fun loadGameProgress(childId: String, gameId: String, levelId: String?): GameProgress? {
        val fileName = "sproutelle_progress_${childId}_${gameId}_${levelId ?: "default"}.json"
        return loadObjectFromFile(fileName)
    }

    private inline fun <reified T> saveObjectToFile(fileName: String, data: T) {
        try {
            val file = getCacheFile(fileName) ?: return
            file.parentFile?.mkdirs()
            val tempFile = File(file.parentFile, "${file.name}.tmp")
            val serialized = json.encodeToString(data)
            tempFile.writeText(serialized)
            if (file.exists()) file.delete()
            tempFile.renameTo(file)
        } catch (e: Exception) {
            println("LearningCache: Error saving $fileName: ${e.message}")
        }
    }

    private inline fun <reified T> loadObjectFromFile(fileName: String): T? {
        return try {
            val file = getCacheFile(fileName) ?: return null
            if (!file.exists()) return null
            val text = file.readText()
            json.decodeFromString<T>(text)
        } catch (e: Exception) {
            println("LearningCache: Error loading $fileName: ${e.message}")
            null
        }
    }

    private fun saveSubcategoriesSync(explorerId: String, items: List<Subcategory>) {
        saveObjectToFile("sproutelle_subcategories_${explorerId}.json", items)
    }

    private fun saveGamesSync(subcategoryId: String, items: List<GameItem>) {
        saveObjectToFile("sproutelle_games_${subcategoryId}.json", items)
    }

    private fun saveGamesForExplorerSync(explorerId: String, items: List<SubcategoryWithGames>) {
        saveObjectToFile("sproutelle_explorer_games_${explorerId}.json", items)
    }

    private fun saveQuestionsSync(subcategoryId: String, gameId: String, items: List<Question>) {
        saveObjectToFile("sproutelle_questions_${subcategoryId}_${gameId}.json", items)
    }

    private fun getCacheFile(fileName: String): File? {
        val appContext = ExplorerCache.androidAppCtx ?: return null
        return appContext.filesDir?.let { File(it, fileName) }
    }
}
