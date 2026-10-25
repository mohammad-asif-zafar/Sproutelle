package com.hathway.sproutelle.data.repository

import com.hathway.sproutelle.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepositoryImpl : UserPreferencesRepository {
    private val onboardingCompletedFlow = MutableStateFlow(false)

    override fun isOnboardingCompleted(): Flow<Boolean> {
        return onboardingCompletedFlow.asStateFlow()
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        onboardingCompletedFlow.value = completed
    }
}
