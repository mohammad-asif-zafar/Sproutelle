package com.hathway.sproutelle.data.repository

import com.hathway.sproutelle.data.datasource.ExplorerRemoteDataSource
import com.hathway.sproutelle.domain.model.ExplorerItem
import com.hathway.sproutelle.domain.repository.ExplorerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ExplorerRepositoryImpl(
    private val remoteDataSource: ExplorerRemoteDataSource
) : ExplorerRepository {

    private val _cachedItems = MutableStateFlow<List<ExplorerItem>>(emptyList())
    private val refreshMutex = Mutex()

    override suspend fun getExplorerItems(): Flow<List<ExplorerItem>> {
        val refreshResult = refreshExplorerItems()
        // If initial fetch failed and cache is empty, throw exception so ViewModel surfaces the error state
        if (refreshResult.isFailure && _cachedItems.value.isEmpty()) {
            throw refreshResult.exceptionOrNull() ?: Exception("Failed to load explore categories")
        }
        return _cachedItems.asStateFlow().map { items ->
            items.filter { it.isActive }.sortedBy { it.sortOrder }
        }
    }

    override suspend fun refreshExplorerItems(): Result<Unit> {
        return refreshMutex.withLock {
            val result = remoteDataSource.fetchExplorerItemsFromSupabase()
            result.onSuccess { fetchedItems ->
                // Successful response replaces cache (including empty list)
                val activeItems = fetchedItems.filter { it.isActive }
                _cachedItems.value = activeItems
            }.onFailure {
                // Preserve last known good cache on network failure.
            }
            result.map { }
        }
    }
}
