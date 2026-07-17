package com.novasec.auth.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novasec.auth.data.local.PreferencesManager
import com.novasec.auth.data.repository.AuthRepository
import com.novasec.auth.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val preferencesManager = PreferencesManager.getInstance(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadUser()
    }

    private fun loadUser() {
        val userInfo = authRepository.getCurrentUser()
        _uiState.value = _uiState.value.copy(
            user = userInfo?.let { User.fromSupabaseUser(it) }
        )
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            authRepository.signOut()
                .onSuccess {
                    preferencesManager.setLoggedIn(false)
                    preferencesManager.clearAll()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onLogoutComplete()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }
}
