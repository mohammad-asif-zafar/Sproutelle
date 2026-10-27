package com.hathway.sproutelle.domain.usecase

import com.hathway.sproutelle.domain.model.ExplorerItem
import com.hathway.sproutelle.domain.repository.ExplorerRepository
import kotlinx.coroutines.flow.Flow

class GetExplorerItemsUseCase(
    private val repository: ExplorerRepository
) {
    suspend operator fun invoke(): Flow<List<ExplorerItem>> {
        return repository.getExplorerItems()
    }
}
