package com.hathway.sproutelle.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hathway.sproutelle.domain.model.OnboardingPage
import com.hathway.sproutelle.domain.usecase.CompleteOnboardingUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    val pages = listOf(
        OnboardingPage(
            id = 1,
            title = "Track Plant Growth",
            description = "Monitor watering schedules, sunlight needs, and plant health with interactive reminders.",
            iconName = "🌱"
        ),
        OnboardingPage(
            id = 2,
            title = "Smart Care Alerts",
            description = "Receive personalized notifications and expert guidance based on your plant species.",
            iconName = "🪴"
        ),
        OnboardingPage(
            id = 3,
            title = "Thriving Community",
            description = "Connect with plant enthusiasts, share tips, and watch your botanical sanctuary flourish.",
            iconName = "🌿"
        )
    )

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _onboardingCompleted = MutableSharedFlow<Unit>()
    val onboardingCompleted: SharedFlow<Unit> = _onboardingCompleted.asSharedFlow()

    fun onNextClicked() {
        viewModelScope.launch {
            if (_currentPage.value < pages.size - 1) {
                _currentPage.value += 1
            } else {
                completeOnboarding()
            }
        }
    }

    fun onPreviousClicked() {
        if (_currentPage.value > 0) {
            _currentPage.value -= 1
        }
    }

    fun onSkipClicked() {
        viewModelScope.launch {
            completeOnboarding()
        }
    }

    fun onPageSelected(index: Int) {
        if (index in pages.indices) {
            _currentPage.value = index
        }
    }

    private suspend fun completeOnboarding() {
        completeOnboardingUseCase()
        _onboardingCompleted.emit(Unit)
    }
}
