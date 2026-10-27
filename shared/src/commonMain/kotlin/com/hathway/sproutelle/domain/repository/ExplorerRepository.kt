package com.hathway.sproutelle.domain.repository

import com.hathway.sproutelle.domain.model.ExplorerItem
import kotlinx.coroutines.flow.Flow

sealed interface ExplorerState<out T> {
    data object Loading : ExplorerState<Nothing>
    data class Cached<T>(val data: T) : ExplorerState<T>
    data class Fresh<T>(val data: T) : ExplorerState<T>
    data class Offline<T>(val data: T, val message: String? = null) : ExplorerState<T>
    data class Error<T>(val message: String, val cachedData: T? = null) : ExplorerState<T>
}

interface ExplorerRepository {
    fun getExplorerItems(): Flow<List<ExplorerItem>>
    fun getExplorerState(): Flow<ExplorerState<List<ExplorerItem>>>
    suspend fun refreshExplorerItems(): Result<Unit>
}
