package com.hathway.sproutelle.domain.usecase

import com.hathway.sproutelle.domain.model.Subcategory
import com.hathway.sproutelle.domain.repository.LearningRepository
import kotlinx.coroutines.flow.Flow

class GetSubcategoriesUseCase(
    private val repository: LearningRepository
) {
    suspend operator fun invoke(explorerId: String): Flow<List<Subcategory>> {
        return repository.getSubcategories(explorerId)
    }
}
