package com.hathway.sproutelle.domain.usecase

import com.hathway.sproutelle.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class CheckOnboardingStatusUseCase(
    private val repository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return repository.isOnboardingCompleted()
    }
}
