package com.hathway.sproutelle.data.datasource

import com.hathway.sproutelle.domain.model.ExplorerItem
import com.hathway.sproutelle.domain.model.HomeData
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

actual class ExplorerCache : ExplorerCacheStorage {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    actual override suspend fun saveCachedItems(items: List<ExplorerItem>) {
        saveStringToFile("sproutelle_explorer_cache.json", json.encodeToString(items))
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun loadCachedItems(): List<ExplorerItem> {
        return loadCachedItemsSync()
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override fun loadCachedItemsSync(): List<ExplorerItem> {
        val content = loadStringFromFile("sproutelle_explorer_cache.json") ?: return emptyList()
        return try {
            json.decodeFromString<List<ExplorerItem>>(content)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    actual override suspend fun saveCachedHomeData(homeData: HomeData) {
        saveStringToFile("sproutelle_homedata_cache.json", json.encodeToString(homeData))
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun loadCachedHomeData(): HomeData? {
        return loadCachedHomeDataSync()
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override fun loadCachedHomeDataSync(): HomeData? {
        val content = loadStringFromFile("sproutelle_homedata_cache.json") ?: return null
        return try {
            json.decodeFromString<HomeData>(content)
        } catch (e: Exception) {
            null
        }
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
            println("ExplorerCache: iOS Error saving file $fileName: ${e.message}")
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
