package com.hathway.sproutelle.data.datasource

import com.hathway.sproutelle.domain.model.ExplorerItem
import com.hathway.sproutelle.domain.model.HomeData

interface ExplorerCacheStorage {
    suspend fun saveCachedItems(items: List<ExplorerItem>)
    suspend fun loadCachedItems(): List<ExplorerItem>
    fun loadCachedItemsSync(): List<ExplorerItem>
    suspend fun saveCachedHomeData(homeData: HomeData)
    suspend fun loadCachedHomeData(): HomeData?
    fun loadCachedHomeDataSync(): HomeData?
}

expect class ExplorerCache() : ExplorerCacheStorage {
    override suspend fun saveCachedItems(items: List<ExplorerItem>)
    override suspend fun loadCachedItems(): List<ExplorerItem>
    override fun loadCachedItemsSync(): List<ExplorerItem>
    override suspend fun saveCachedHomeData(homeData: HomeData)
    override suspend fun loadCachedHomeData(): HomeData?
    override fun loadCachedHomeDataSync(): HomeData?
}
