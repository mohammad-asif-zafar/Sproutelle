package com.hathway.sproutelle.domain.usecase

import com.hathway.sproutelle.domain.repository.UserPreferencesRepository

class CompleteOnboardingUseCase(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke() {
        repository.setOnboardingCompleted(true)
    }
}
