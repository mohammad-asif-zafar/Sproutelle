package com.hathway.sproutelle.domain.usecase

import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.repository.LearningRepository
import kotlinx.coroutines.flow.Flow

class GetQuestionsUseCase(
    private val repository: LearningRepository
) {
    operator fun invoke(subcategoryId: String, gameId: String): Flow<List<Question>> {
        return repository.getQuestions(subcategoryId, gameId)
    }
}
