package com.hathway.sproutelle.domain

import com.hathway.sproutelle.data.datasource.ExplorerRemoteDataSource
import com.hathway.sproutelle.data.repository.ExplorerRepositoryImpl
import com.hathway.sproutelle.domain.model.ExplorerItem
import com.hathway.sproutelle.domain.usecase.GetExplorerItemsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExplorerRepositoryTest {

    private class FakeExplorerRemoteDataSource(
        var itemsToReturn: Result<List<ExplorerItem>>
    ) : ExplorerRemoteDataSource {
        override suspend fun fetchExplorerItemsFromSupabase(): Result<List<ExplorerItem>> {
            return itemsToReturn
        }
    }

    @Test
    fun testExplorerFailurePreservesCache() = runBlocking {
        val initialItems = listOf(ExplorerItem("1", "Letters", null, "#FFE1D6", "cat_1", 1, true))
        val remoteDataSource = FakeExplorerRemoteDataSource(Result.success(initialItems))
        val repository = ExplorerRepositoryImpl(remoteDataSource)
        val useCase = GetExplorerItemsUseCase(repository)

        val firstEmission = useCase().first()
        assertEquals(initialItems, firstEmission)

        // Simulate network failure on refresh
        remoteDataSource.itemsToReturn = Result.failure(Exception("Network error"))
        repository.refreshExplorerItems()

        val secondEmission = useCase().first()
        assertEquals(initialItems, secondEmission, "Cache should be preserved on failure")
    }

    @Test
    fun testSuccessfulEmptyResponseClearsCache() = runBlocking {
        val initialItems = listOf(ExplorerItem("1", "Letters", null, "#FFE1D6", "cat_1", 1, true))
        val remoteDataSource = FakeExplorerRemoteDataSource(Result.success(initialItems))
        val repository = ExplorerRepositoryImpl(remoteDataSource)
        val useCase = GetExplorerItemsUseCase(repository)

        val firstEmission = useCase().first()
        assertEquals(initialItems, firstEmission)

        // Simulate successful empty response
        remoteDataSource.itemsToReturn = Result.success(emptyList())
        repository.refreshExplorerItems()

        val secondEmission = useCase().first()
        assertTrue(secondEmission.isEmpty(), "Cache should be cleared when successful response is empty")
    }

    @Test
    fun testInactiveItemsFilteredOut() = runBlocking {
        val mixedItems = listOf(
            ExplorerItem("1", "Letters", null, "#FFE1D6", "cat_1", 1, true),
            ExplorerItem("2", "Inactive", null, "#FFE1D6", "cat_2", 2, false)
        )
        val remoteDataSource = FakeExplorerRemoteDataSource(Result.success(mixedItems))
        val repository = ExplorerRepositoryImpl(remoteDataSource)
        val useCase = GetExplorerItemsUseCase(repository)

        val emission = useCase().first()
        assertEquals(1, emission.size)
        assertEquals("Letters", emission[0].titleName)
    }

    @Test
    fun testInitialFailureThrowsException() = runBlocking {
        val remoteDataSource = FakeExplorerRemoteDataSource(Result.failure(Exception("Initial network error")))
        val repository = ExplorerRepositoryImpl(remoteDataSource)
        val useCase = GetExplorerItemsUseCase(repository)

        var exceptionThrown = false
        try {
            useCase().first()
        } catch (e: Exception) {
            exceptionThrown = true
            assertEquals("Initial network error", e.message)
        }
        assertTrue(exceptionThrown, "Initial fetch failure with empty cache should throw exception")
    }
}
