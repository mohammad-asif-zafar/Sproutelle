package com.hathway.sproutelle.data.datasource

import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.Subcategory
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

    private fun saveSubcategoriesSync(explorerId: String, items: List<Subcategory>) {
        try {
            val file = getCacheFile("sproutelle_subcategories_${explorerId}.json") ?: return
            file.parentFile?.mkdirs()
            val tempFile = File(file.parentFile, "${file.name}.tmp")
            val serialized = json.encodeToString(items)
            tempFile.writeText(serialized)
            if (file.exists()) file.delete()
            tempFile.renameTo(file)
        } catch (e: Exception) {
            println("LearningCache: Error saving subcategories cache: ${e.message}")
        }
    }

    private fun saveGamesSync(subcategoryId: String, items: List<GameItem>) {
        try {
            val file = getCacheFile("sproutelle_games_${subcategoryId}.json") ?: return
            file.parentFile?.mkdirs()
            val tempFile = File(file.parentFile, "${file.name}.tmp")
            val serialized = json.encodeToString(items)
            tempFile.writeText(serialized)
            if (file.exists()) file.delete()
            tempFile.renameTo(file)
        } catch (e: Exception) {
            println("LearningCache: Error saving games cache: ${e.message}")
        }
    }

    private fun saveQuestionsSync(subcategoryId: String, gameId: String, items: List<Question>) {
        try {
            val file = getCacheFile("sproutelle_questions_${subcategoryId}_${gameId}.json") ?: return
            file.parentFile?.mkdirs()
            val tempFile = File(file.parentFile, "${file.name}.tmp")
            val serialized = json.encodeToString(items)
            tempFile.writeText(serialized)
            if (file.exists()) file.delete()
            tempFile.renameTo(file)
        } catch (e: Exception) {
            println("LearningCache: Error saving questions cache: ${e.message}")
        }
    }

    private fun getCacheFile(fileName: String): File? {
        val appContext = ExplorerCache.androidAppCtx ?: return null
        return appContext.filesDir?.let { File(it, fileName) }
    }
}
