package com.hathway.sproutelle.domain

import com.hathway.sproutelle.data.datasource.ExplorerCacheStorage
import com.hathway.sproutelle.data.datasource.ExplorerRemoteDataSource
import com.hathway.sproutelle.data.repository.ExplorerRepositoryImpl
import com.hathway.sproutelle.data.repository.HomeRepositoryImpl
import com.hathway.sproutelle.domain.model.ExplorerItem
import com.hathway.sproutelle.domain.model.HomeData
import com.hathway.sproutelle.domain.repository.ExplorerState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExplorerRepositoryTest {

    private class FakeExplorerRemoteDataSource(
        var itemsToReturn: Result<List<ExplorerItem>>
    ) : ExplorerRemoteDataSource {
        var fetchCount = 0
        override suspend fun fetchExplorerItemsFromSupabase(): Result<List<ExplorerItem>> {
            fetchCount++
            return itemsToReturn
        }
    }

    private class FakeExplorerCacheStorage(
        initialCachedItems: List<ExplorerItem> = emptyList(),
        var throwOnSave: Boolean = false
    ) : ExplorerCacheStorage {
        var storedItems = initialCachedItems.toMutableList()
        var saveCallCount = 0

        override suspend fun saveCachedItems(items: List<ExplorerItem>) {
            if (throwOnSave) throw Exception("Disk full / serialization error")
            storedItems = items.toMutableList()
            saveCallCount++
        }

        override suspend fun loadCachedItems(): List<ExplorerItem> {
            return storedItems
        }

        override fun loadCachedItemsSync(): List<ExplorerItem> {
            return storedItems
        }

        override suspend fun saveCachedHomeData(homeData: HomeData) {}
        override suspend fun loadCachedHomeData(): HomeData? = null
        override fun loadCachedHomeDataSync(): HomeData? = null
    }

    @Test
    fun testCacheFirstBehaviorControlled(): Unit = runBlocking {
        val cached = listOf(ExplorerItem("1", "Category A", null, "#fff", "c1", 1, true, imageUrl = "urlA", signedAtTimestamp = 1000L))
        val cache = FakeExplorerCacheStorage(cached)

        val remoteDeferred = CompletableDeferred<Result<List<ExplorerItem>>>()
        val remote = object : ExplorerRemoteDataSource {
            var fetchCount = 0
            override suspend fun fetchExplorerItemsFromSupabase(): Result<List<ExplorerItem>> {
                fetchCount++
                return remoteDeferred.await()
            }
        }

        val repo = ExplorerRepositoryImpl(remote, cache, clock = { 1000L })
        
        // Deterministically await Cached emission while remote is blocked (zero arbitrary delay)
        val cachedState = repo.getExplorerState().first { it is ExplorerState.Cached }
        assertEquals(cached, (cachedState as ExplorerState.Cached).data)
        assertEquals(1, remote.fetchCount, "Exactly one remote request should have started")

        // Release remote response containing category B
        val fresh = listOf(ExplorerItem("2", "Category B", null, "#fff", "c2", 1, true, imageUrl = "urlB", signedAtTimestamp = 1000L))
        remoteDeferred.complete(Result.success(fresh))

        // Deterministically await Fresh emission
        val freshState = repo.getExplorerState().first { it is ExplorerState.Fresh }
        assertEquals(fresh, (freshState as ExplorerState.Fresh).data)
        assertEquals(fresh, cache.storedItems, "Category B should be persisted")
    }

    @Test
    fun test1_CacheLoadedAsynchronously(): Unit = runBlocking {
        val cached = listOf(ExplorerItem("1", "Cached", null, "#fff", "c1", 1, true, imageUrl = "url1"))
        val cache = FakeExplorerCacheStorage(cached)
        val remote = FakeExplorerRemoteDataSource(Result.success(emptyList()))
        val repo = ExplorerRepositoryImpl(remote, cache)
        val state = repo.getExplorerState().first { it is ExplorerState.Cached || it is ExplorerState.Fresh }
        assertNotNull(state)
    }

    @Test
    fun test2_CachedContentBeforeApi(): Unit = runBlocking {
        val cached = listOf(ExplorerItem("1", "Cached", null, "#fff", "c1", 1, true, imageUrl = "url1"))
        val cache = FakeExplorerCacheStorage(cached)
        val remote = FakeExplorerRemoteDataSource(Result.success(listOf(ExplorerItem("2", "Remote", null, "#fff", "c2", 1, true))))
        val repo = ExplorerRepositoryImpl(remote, cache)
        val state = repo.getExplorerState().first { it is ExplorerState.Cached }
        assertEquals(cached, (state as ExplorerState.Cached).data)
    }

    @Test
    fun test3_FirstLaunchWithoutCacheSuccess(): Unit = runBlocking {
        val remoteItems = listOf(ExplorerItem("1", "Remote", null, "#fff", "c1", 1, true, imageUrl = "url1", signedAtTimestamp = 1000L))
        val cache = FakeExplorerCacheStorage()
        val remote = FakeExplorerRemoteDataSource(Result.success(remoteItems))
        val repo = ExplorerRepositoryImpl(remote, cache, clock = { 1000L })
        val state = repo.getExplorerState().first { it is ExplorerState.Fresh }
        assertEquals(remoteItems, (state as ExplorerState.Fresh).data)
    }

    @Test
    fun test4_FirstLaunchWithoutCacheFailure(): Unit = runBlocking {
        val cache = FakeExplorerCacheStorage()
        val remote = FakeExplorerRemoteDataSource(Result.failure(Exception("Network error")))
        val repo = ExplorerRepositoryImpl(remote, cache)
        val result = repo.refreshExplorerItems()
        assertTrue(result.isFailure)
    }

    @Test
    fun test5_OfflineStartupPreservesCache(): Unit = runBlocking {
        val cached = listOf(ExplorerItem("1", "Cached", null, "#fff", "c1", 1, true, imageUrl = "url1"))
        val cache = FakeExplorerCacheStorage(cached)
        val remote = FakeExplorerRemoteDataSource(Result.failure(Exception("Offline")))
        val repo = ExplorerRepositoryImpl(remote, cache)
        val state = repo.getExplorerState().first { it is ExplorerState.Offline }
        assertEquals(cached, (state as ExplorerState.Offline).data)
    }

    @Test
    fun test6_ApiFailureNeverClearsCache(): Unit = runBlocking {
        val cached = listOf(ExplorerItem("1", "Cached", null, "#fff", "c1", 1, true, imageUrl = "url1"))
        val cache = FakeExplorerCacheStorage(cached)
        val remote = FakeExplorerRemoteDataSource(Result.failure(Exception("Error")))
        val repo = ExplorerRepositoryImpl(remote, cache)
        repo.refreshExplorerItems()
        val items = repo.getExplorerItems().first()
        assertEquals(cached, items)
    }

    @Test
    fun test7_AuthoritativeEmptyResponseClearsRecords(): Unit = runBlocking {
        val cached = listOf(ExplorerItem("1", "Cached", null, "#fff", "c1", 1, true, imageUrl = "url1"))
        val cache = FakeExplorerCacheStorage(cached)
        val remote = FakeExplorerRemoteDataSource(Result.success(emptyList()))
        val repo = ExplorerRepositoryImpl(remote, cache)
        val state = repo.getExplorerState().first { it is ExplorerState.Fresh && it.data.isEmpty() }
        assertTrue((state as ExplorerState.Fresh).data.isEmpty())
    }

    @Test
    fun test8_InvalidResponsePreservesCache(): Unit = runBlocking {
        val cached = listOf(ExplorerItem("1", "Cached", null, "#fff", "c1", 1, true, imageUrl = "url1"))
        val cache = FakeExplorerCacheStorage(cached)
        val remote = FakeExplorerRemoteDataSource(Result.failure(Exception("Invalid data")))
        val repo = ExplorerRepositoryImpl(remote, cache)
        repo.refreshExplorerItems()
        val items = repo.getExplorerItems().first()
        assertEquals(cached, items)
    }

    @Test
    fun test9_MultipleSubscribersSingleInit(): Unit = runBlocking {
        val remote = FakeExplorerRemoteDataSource(Result.success(listOf(ExplorerItem("1", "Item", null, "#fff", "c1", 1, true))))
        val cache = FakeExplorerCacheStorage()
        val repo = ExplorerRepositoryImpl(remote, cache)
        coroutineScope {
            repeat(5) {
                launch {
                    repo.getExplorerItems().first()
                }
            }
        }
        repo.getExplorerState().first { it is ExplorerState.Fresh }
        assertEquals(1, remote.fetchCount)
    }

    @Test
    fun test10_HomeAndExploreShareOneInitialization(): Unit = runBlocking {
        val remote = FakeExplorerRemoteDataSource(Result.success(listOf(ExplorerItem("1", "Item", null, "#fff", "c1", 1, true))))
        val cache = FakeExplorerCacheStorage()
        val explorerRepo = ExplorerRepositoryImpl(remote, cache)
        val homeRepo = HomeRepositoryImpl(explorerRepo)
        coroutineScope {
            launch { explorerRepo.getExplorerItems().first() }
            launch { homeRepo.getHomeData().first() }
        }
        explorerRepo.getExplorerState().first { it is ExplorerState.Fresh }
        assertEquals(1, remote.fetchCount)
    }

    @Test
    fun test11_ConcurrentRefreshSharedInFlight(): Unit = runBlocking {
        val remote = FakeExplorerRemoteDataSource(Result.success(listOf(ExplorerItem("1", "Item", null, "#fff", "c1", 1, true))))
        val cache = FakeExplorerCacheStorage(listOf(ExplorerItem("1", "Cached", null, "#fff", "c1", 1, true)))
        val repo = ExplorerRepositoryImpl(remote, cache)
        repo.getExplorerState().first { it is ExplorerState.Fresh }
        remote.fetchCount = 0
        coroutineScope {
            repeat(5) {
                launch {
                    repo.refreshExplorerItems()
                }
            }
        }
        assertEquals(1, remote.fetchCount)
    }

    @Test
    fun test12_WarmStartRefreshHappensAutomatically(): Unit = runBlocking {
        val cached = listOf(ExplorerItem("1", "Cached", null, "#fff", "c1", 1, true, imageUrl = "url1"))
        val cache = FakeExplorerCacheStorage(cached)
        val remote = FakeExplorerRemoteDataSource(Result.success(listOf(ExplorerItem("1", "Fresh", null, "#fff", "c1", 1, true, imageUrl = "url2"))))
        val repo = ExplorerRepositoryImpl(remote, cache)
        val state = repo.getExplorerState().first { it is ExplorerState.Fresh && it.data[0].titleName == "Fresh" }
        assertEquals("Fresh", (state as ExplorerState.Fresh).data[0].titleName)
    }

    @Test
    fun test13_SignedUrlExpiryTriggersRenewal(): Unit = runBlocking {
        val currentTime = 1000L
        val expired = listOf(ExplorerItem("1", "Item", "asset1", "#fff", "c1", 1, true, imageUrl = "oldUrl", signedAtTimestamp = 100L))
        val renewed = listOf(ExplorerItem("1", "Item", "asset1", "#fff", "c1", 1, true, imageUrl = "newUrl", signedAtTimestamp = 1000L))
        val cache = FakeExplorerCacheStorage(expired)
        val remote = FakeExplorerRemoteDataSource(Result.success(renewed))
        val repo = ExplorerRepositoryImpl(remote, cache, clock = { currentTime })
        repo.refreshExplorerItems()
        val items = repo.getExplorerItems().first()
        assertEquals("newUrl", items[0].imageUrl)
    }

    @Test
    fun test14_SigningFailureRetainsAssetReferences(): Unit = runBlocking {
        val cached = listOf(ExplorerItem("1", "Item", "asset1", "#fff", "c1", 1, true, imageUrl = "validUrl", signedAtTimestamp = 1000L))
        val cache = FakeExplorerCacheStorage(cached)
        val remote = FakeExplorerRemoteDataSource(Result.failure(Exception("Signing failed")))
        val repo = ExplorerRepositoryImpl(remote, cache, clock = { 1000L })
        repo.refreshExplorerItems()
        val items = repo.getExplorerItems().first()
        assertEquals("validUrl", items[0].imageUrl)
    }

    @Test
    fun test15_HomeAndExploreShareSameData(): Unit = runBlocking {
        val remoteItems = listOf(ExplorerItem("1", "Letters", null, "#fff", "c1", 1, true, imageUrl = "url1"))
        val cache = FakeExplorerCacheStorage()
        val remote = FakeExplorerRemoteDataSource(Result.success(remoteItems))
        val explorerRepo = ExplorerRepositoryImpl(remote, cache)
        val homeRepo = HomeRepositoryImpl(explorerRepo)
        val state = explorerRepo.getExplorerState().first { it is ExplorerState.Fresh }
        assertNotNull(state)
        val homeData = homeRepo.getHomeData().first()
        assertEquals(1, homeData.categories.size)
        assertEquals("Letters", homeData.categories[0].title)
        assertEquals("url1", homeData.categories[0].imageUrl)
    }
}
