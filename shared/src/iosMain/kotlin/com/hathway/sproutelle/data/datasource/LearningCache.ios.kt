package com.hathway.sproutelle.data.datasource

import com.hathway.sproutelle.domain.model.GameConfig
import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.domain.model.GameLevel
import com.hathway.sproutelle.domain.model.GameProgress
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.Subcategory
import com.hathway.sproutelle.domain.model.SubcategoryWithGames
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

actual class LearningCache : LearningCacheStorage {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    actual override suspend fun saveSubcategories(explorerId: String, items: List<Subcategory>) {
        saveStringToFile("sproutelle_subcategories_${explorerId}.json", json.encodeToString(items))
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun loadSubcategories(explorerId: String): List<Subcategory> {
        return loadSubcategoriesSync(explorerId)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override fun loadSubcategoriesSync(explorerId: String): List<Subcategory> {
        val content = loadStringFromFile("sproutelle_subcategories_${explorerId}.json") ?: return emptyList()
        return try {
            json.decodeFromString<List<Subcategory>>(content)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    actual override suspend fun saveGames(subcategoryId: String, items: List<GameItem>) {
        saveStringToFile("sproutelle_games_${subcategoryId}.json", json.encodeToString(items))
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun loadGames(subcategoryId: String): List<GameItem> {
        return loadGamesSync(subcategoryId)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override fun loadGamesSync(subcategoryId: String): List<GameItem> {
        val content = loadStringFromFile("sproutelle_games_${subcategoryId}.json") ?: return emptyList()
        return try {
            json.decodeFromString<List<GameItem>>(content)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    actual override suspend fun saveGamesForExplorer(explorerId: String, items: List<SubcategoryWithGames>) {
        saveStringToFile("sproutelle_explorer_games_${explorerId}.json", json.encodeToString(items))
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun loadGamesForExplorer(explorerId: String): List<SubcategoryWithGames> {
        return loadGamesForExplorerSync(explorerId)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override fun loadGamesForExplorerSync(explorerId: String): List<SubcategoryWithGames> {
        val content = loadStringFromFile("sproutelle_explorer_games_${explorerId}.json") ?: return emptyList()
        return try {
            json.decodeFromString<List<SubcategoryWithGames>>(content)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    actual override suspend fun saveQuestions(subcategoryId: String, gameId: String, items: List<Question>) {
        saveStringToFile("sproutelle_questions_${subcategoryId}_${gameId}.json", json.encodeToString(items))
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun loadQuestions(subcategoryId: String, gameId: String): List<Question> {
        return loadQuestionsSync(subcategoryId, gameId)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override fun loadQuestionsSync(subcategoryId: String, gameId: String): List<Question> {
        val content = loadStringFromFile("sproutelle_questions_${subcategoryId}_${gameId}.json") ?: return emptyList()
        return try {
            json.decodeFromString<List<Question>>(content)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    actual override suspend fun saveGameConfig(gameId: String, config: GameConfig) {
        saveStringToFile("sproutelle_config_${gameId}.json", json.encodeToString(config))
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun loadGameConfig(gameId: String): GameConfig? {
        val content = loadStringFromFile("sproutelle_config_${gameId}.json") ?: return null
        return try { json.decodeFromString<GameConfig>(content) } catch (_: Exception) { null }
    }

    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    actual override suspend fun saveGameLevels(gameId: String, levels: List<GameLevel>) {
        saveStringToFile("sproutelle_levels_${gameId}.json", json.encodeToString(levels))
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun loadGameLevels(gameId: String): List<GameLevel> {
        val content = loadStringFromFile("sproutelle_levels_${gameId}.json") ?: return emptyList()
        return try { json.decodeFromString<List<GameLevel>>(content) } catch (_: Exception) { emptyList() }
    }

    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    actual override suspend fun saveGameProgress(progress: GameProgress) {
        val fileName = "sproutelle_progress_${progress.childId}_${progress.gameId}_${progress.levelId ?: "default"}.json"
        saveStringToFile(fileName, json.encodeToString(progress))
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun loadGameProgress(childId: String, gameId: String, levelId: String?): GameProgress? {
        val fileName = "sproutelle_progress_${childId}_${gameId}_${levelId ?: "default"}.json"
        val content = loadStringFromFile(fileName) ?: return null
        return try { json.decodeFromString<GameProgress>(content) } catch (_: Exception) { null }
    }

    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    private fun saveStringToFile(fileName: String, content: String) {
        try {
            val path = getCacheFilePath(fileName) ?: return
            val tempPath = "$path.tmp"
            val nsString = NSString.create(string = content)
            nsString.writeToFile(tempPath, atomically = true, encoding = NSUTF8StringEncoding, error = null)
            val fileManager = NSFileManager.defaultManager
            if (fileManager.fileExistsAtPath(path)) {
                fileManager.removeItemAtPath(path, error = null)
            }
            fileManager.moveItemAtPath(tempPath, toPath = path, error = null)
        } catch (e: Exception) {
            println("LearningCache: iOS Error saving file $fileName: ${e.message}")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun loadStringFromFile(fileName: String): String? {
        return try {
            val path = getCacheFilePath(fileName) ?: return null
            val fileManager = NSFileManager.defaultManager
            if (!fileManager.fileExistsAtPath(path)) return null
            NSString.stringWithContentsOfFile(path, encoding = NSUTF8StringEncoding, error = null) as? String
        } catch (e: Exception) {
            null
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getCacheFilePath(fileName: String): String? {
        val urls = NSFileManager.defaultManager.URLsForDirectory(NSDocumentDirectory, inDomains = NSUserDomainMask)
        val cacheDir = urls.firstOrNull() as? NSURL ?: return null
        return cacheDir.URLByAppendingPathComponent(fileName)?.path
    }
}
