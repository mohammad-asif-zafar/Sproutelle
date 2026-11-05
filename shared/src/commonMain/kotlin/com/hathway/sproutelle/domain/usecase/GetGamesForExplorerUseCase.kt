package com.hathway.sproutelle.domain.usecase

import com.hathway.sproutelle.domain.model.SubcategoryWithGames
import com.hathway.sproutelle.domain.repository.LearningRepository
import com.hathway.sproutelle.domain.repository.LearningState
import kotlinx.coroutines.flow.Flow

class GetGamesForExplorerUseCase(private val learningRepository: LearningRepository) {
    operator fun invoke(explorerId: String): Flow<LearningState<List<SubcategoryWithGames>>> {
        return learningRepository.getGamesForExplorerState(explorerId)
    }
}
