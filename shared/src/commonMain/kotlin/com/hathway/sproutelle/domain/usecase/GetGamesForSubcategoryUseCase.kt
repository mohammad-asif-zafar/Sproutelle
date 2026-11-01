package com.hathway.sproutelle.domain.usecase

import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.domain.repository.LearningRepository
import kotlinx.coroutines.flow.Flow

class GetGamesForSubcategoryUseCase(
    private val repository: LearningRepository
) {
    suspend operator fun invoke(subcategoryId: String): Flow<List<GameItem>> {
        return repository.getGamesForSubcategory(subcategoryId)
    }
}
