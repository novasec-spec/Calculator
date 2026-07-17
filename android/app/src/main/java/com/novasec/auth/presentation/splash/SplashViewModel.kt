package com.novasec.auth.presentation.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novasec.auth.data.local.PreferencesManager
import com.novasec.auth.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class SplashDestination {
    data object Onboarding : SplashDestination()
    data object Login : SplashDestination()
    data object Home : SplashDestination()
}

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager.getInstance(application)
    private val authRepository = AuthRepository()

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination

    init {
        determineDestination()
    }

    private fun determineDestination() {
        viewModelScope.launch {
            delay(2000) // Minimum splash duration

            val hasCompletedOnboarding = preferencesManager.hasCompletedOnboarding().first()

            if (!hasCompletedOnboarding) {
                _destination.value = SplashDestination.Onboarding
                return@launch
            }

            val isLoggedIn = authRepository.isUserLoggedIn()
            if (isLoggedIn) {
                // Try to refresh session
                val refreshResult = authRepository.refreshSession()
                if (refreshResult.isSuccess) {
                    _destination.value = SplashDestination.Home
                } else {
                    _destination.value = SplashDestination.Login
                }
            } else {
                _destination.value = SplashDestination.Login
            }
        }
    }
}
