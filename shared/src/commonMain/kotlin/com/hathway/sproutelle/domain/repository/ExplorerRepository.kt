package com.hathway.sproutelle.domain.repository

import com.hathway.sproutelle.domain.model.ExplorerItem
import kotlinx.coroutines.flow.Flow

interface ExplorerRepository {
    suspend fun getExplorerItems(): Flow<List<ExplorerItem>>
    suspend fun refreshExplorerItems(): Result<Unit>
}
