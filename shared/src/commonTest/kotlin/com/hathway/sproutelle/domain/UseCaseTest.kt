package com.hathway.sproutelle.domain

import com.hathway.sproutelle.data.datasource.ExplorerCacheStorage
import com.hathway.sproutelle.data.datasource.ExplorerRemoteDataSource
import com.hathway.sproutelle.data.repository.ExplorerRepositoryImpl
import com.hathway.sproutelle.data.repository.HomeRepositoryImpl
import com.hathway.sproutelle.data.repository.UserPreferencesRepositoryImpl
import com.hathway.sproutelle.domain.model.ExplorerItem
import com.hathway.sproutelle.domain.model.HomeData
import com.hathway.sproutelle.domain.usecase.CheckOnboardingStatusUseCase
import com.hathway.sproutelle.domain.usecase.CompleteOnboardingUseCase
import com.hathway.sproutelle.domain.usecase.GetHomeDataUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UseCaseTest {

    private class FakeExplorerRemoteDataSource(
        var itemsToReturn: Result<List<ExplorerItem>>
    ) : ExplorerRemoteDataSource {
        override suspend fun fetchExplorerItemsFromSupabase(): Result<List<ExplorerItem>> {
            return itemsToReturn
        }
    }

    private class FakeExplorerCacheStorage(
        initialCachedItems: List<ExplorerItem> = emptyList()
    ) : ExplorerCacheStorage {
        var storedItems = initialCachedItems.toMutableList()

        override suspend fun saveCachedItems(items: List<ExplorerItem>) {
            storedItems = items.toMutableList()
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
    fun testOnboardingStatusFlow() = runBlocking {
        val repository = UserPreferencesRepositoryImpl()
        val checkUseCase = CheckOnboardingStatusUseCase(repository)
        val completeUseCase = CompleteOnboardingUseCase(repository)

        assertFalse(checkUseCase().first(), "Onboarding should initially be incomplete")

        completeUseCase()

        assertTrue(checkUseCase().first(), "Onboarding should be complete after invocation")
    }

    @Test
    fun testGetHomeDataUseCase() = runBlocking {
        val remoteDataSource = FakeExplorerRemoteDataSource(
            Result.success(listOf(ExplorerItem("1", "Letters", null, "#fff", "c1", 1, true)))
        )
        val cacheStorage = FakeExplorerCacheStorage()
        val explorerRepository = ExplorerRepositoryImpl(remoteDataSource, cacheStorage)
        val repository = HomeRepositoryImpl(explorerRepository)
        explorerRepository.refreshExplorerItems()
        val getHomeDataUseCase = GetHomeDataUseCase(repository)

        val homeData = getHomeDataUseCase().first()

        assertEquals("Welcome to Sproutelle", homeData.greetingMessage)
        assertEquals(1, homeData.categories.size)
        assertEquals("Letters", homeData.categories[0].title)
    }
}
