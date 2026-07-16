package com.novasec.secureauth.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourapp.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    
    var uiState by mutableStateOf(AuthUiState())
        private set
    
    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email, errorMessage = null)
    }
    
    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password, errorMessage = null)
    }
    
    fun onLoginClick(onLoginSuccess: () -> Unit) {
        val email = uiState.email.trim()
        val password = uiState.password
        
        // Validation
        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Email and password are required")
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            uiState = uiState.copy(errorMessage = "Please enter a valid email")
            return
        }
        
        if (password.length < 6) {
            uiState = uiState.copy(errorMessage = "Password must be at least 6 characters")
            return
        }
        
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            
            authRepository.signIn(email, password)
                .onSuccess {
                    uiState = uiState.copy(isLoading = false)
                    onLoginSuccess()
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Login failed. Please try again."
                    )
                }
        }
    }
    
    fun onSignUpClick(onSignUpSuccess: () -> Unit) {
        val email = uiState.email.trim()
        val password = uiState.password
        
        if (email.isBlank() || password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Email and password are required")
            return
        }
        
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            
            authRepository.signUp(email, password)
                .onSuccess {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                    onSignUpSuccess()
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Sign up failed. Please try again."
                    )
                }
        }
    }
    
    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
