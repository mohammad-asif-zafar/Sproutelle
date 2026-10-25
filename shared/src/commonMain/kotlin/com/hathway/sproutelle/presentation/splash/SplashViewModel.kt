package com.hathway.sproutelle.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hathway.sproutelle.core.navigation.Screen
import com.hathway.sproutelle.domain.usecase.CheckOnboardingStatusUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(
    private val checkOnboardingStatusUseCase: CheckOnboardingStatusUseCase
) : ViewModel() {

    private val _destination = MutableStateFlow<Screen?>(null)
    val destination: StateFlow<Screen?> = _destination.asStateFlow()

    init {
        checkStatus()
    }

    private fun checkStatus() {
        viewModelScope.launch {
            delay(1200) // Allow splash animation to play smoothly
            val isOnboardingCompleted = checkOnboardingStatusUseCase().first()
            if (isOnboardingCompleted) {
                _destination.value = Screen.Home
            } else {
                _destination.value = Screen.Onboarding
            }
        }
    }
}
