package com.hathway.sproutelle.data.datasource

import com.hathway.sproutelle.domain.model.ExplorerItem
import com.hathway.sproutelle.domain.model.HomeData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

actual class ExplorerCache : ExplorerCacheStorage {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    actual override suspend fun saveCachedItems(items: List<ExplorerItem>) {
        saveCachedItemsSync(items)
    }

    actual override suspend fun loadCachedItems(): List<ExplorerItem> {
        return loadCachedItemsSync()
    }

    actual override fun loadCachedItemsSync(): List<ExplorerItem> {
        return try {
            val file = getCacheFile("sproutelle_explorer_cache.json") ?: return emptyList()
            if (!file.exists()) return emptyList()
            val text = file.readText()
            json.decodeFromString<List<ExplorerItem>>(text)
        } catch (e: Exception) {
            println("ExplorerCache: Error loading explorer cache: ${e.message}")
            emptyList()
        }
    }

    actual override suspend fun saveCachedHomeData(homeData: HomeData) {
        saveCachedHomeDataSync(homeData)
    }

    actual override suspend fun loadCachedHomeData(): HomeData? {
        return loadCachedHomeDataSync()
    }

    actual override fun loadCachedHomeDataSync(): HomeData? {
        return try {
            val file = getCacheFile("sproutelle_homedata_cache.json") ?: return null
            if (!file.exists()) return null
            val text = file.readText()
            json.decodeFromString<HomeData>(text)
        } catch (e: Exception) {
            println("ExplorerCache: Error loading homedata cache: ${e.message}")
            null
        }
    }

    private fun saveCachedItemsSync(items: List<ExplorerItem>) {
        try {
            val file = getCacheFile("sproutelle_explorer_cache.json") ?: return
            file.parentFile?.mkdirs()
            val tempFile = File(file.parentFile, "${file.name}.tmp")
            val serialized = json.encodeToString(items)
            tempFile.writeText(serialized)
            if (file.exists()) file.delete()
            tempFile.renameTo(file)
        } catch (e: Exception) {
            println("ExplorerCache: Error saving explorer cache: ${e.message}")
        }
    }

    private fun saveCachedHomeDataSync(homeData: HomeData) {
        try {
            val file = getCacheFile("sproutelle_homedata_cache.json") ?: return
            file.parentFile?.mkdirs()
            val tempFile = File(file.parentFile, "${file.name}.tmp")
            val serialized = json.encodeToString(homeData)
            tempFile.writeText(serialized)
            if (file.exists()) file.delete()
            tempFile.renameTo(file)
        } catch (e: Exception) {
            println("ExplorerCache: Error saving homedata cache: ${e.message}")
        }
    }

    private fun getCacheFile(fileName: String): File? {
        val appContext = androidAppCtx ?: return null
        return appContext.filesDir?.let { File(it, fileName) }
    }

    companion object {
        var androidAppCtx: android.content.Context? = null
    }
}
