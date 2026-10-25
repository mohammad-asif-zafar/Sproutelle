package com.hathway.sproutelle.domain

import com.hathway.sproutelle.data.repository.HomeRepositoryImpl
import com.hathway.sproutelle.data.repository.UserPreferencesRepositoryImpl
import com.hathway.sproutelle.domain.usecase.CheckOnboardingStatusUseCase
import com.hathway.sproutelle.domain.usecase.CompleteOnboardingUseCase
import com.hathway.sproutelle.domain.usecase.GetHomeDataUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UseCaseTest {

    @Test
    fun testOnboardingStatusFlow() = runBlocking {
        val repository = UserPreferencesRepositoryImpl()
        val checkUseCase = CheckOnboardingStatusUseCase(repository)
        val completeUseCase = CompleteOnboardingUseCase(repository)

        assertFalse(checkUseCase().first(), "Onboarding should initially be incomplete")

        completeUseCase()

        assertTrue(checkUseCase().first(), "Onboarding should be complete after invocation")
    }

    @Test
    fun testGetHomeDataUseCase() = runBlocking {
        val repository = HomeRepositoryImpl()
        val getHomeDataUseCase = GetHomeDataUseCase(repository)

        val homeData = getHomeDataUseCase().first()

        assertEquals("Sarah", homeData.userName)
        assertEquals(3, homeData.activePlants.size)
        assertEquals(14, homeData.streakDays)
    }
}
