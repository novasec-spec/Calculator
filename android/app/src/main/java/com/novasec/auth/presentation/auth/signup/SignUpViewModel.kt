package com.novasec.auth.presentation.auth.signup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novasec.auth.data.local.PreferencesManager
import com.novasec.auth.data.repository.AuthRepository
import com.novasec.auth.domain.model.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val signUpSuccess: Boolean = false
)

class SignUpViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val preferencesManager = PreferencesManager.getInstance(application)

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null,
            generalError = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null,
            generalError = null
        )
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null,
            generalError = null
        )
    }

    fun signUp(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        val emailValidation = validateEmail(email)
        val passwordValidation = validatePassword(password)
        val confirmValidation = validateConfirmPassword(password, confirmPassword)

        if (!emailValidation.successful || !passwordValidation.successful || !confirmValidation.successful) {
            _uiState.value = _uiState.value.copy(
                emailError = emailValidation.errorMessage,
                passwordError = passwordValidation.errorMessage,
                confirmPasswordError = confirmValidation.errorMessage
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, generalError = null)

            authRepository.signUp(email, password)
                .onSuccess { userInfo ->
                    preferencesManager.setLoggedIn(true)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        signUpSuccess = true
                    )
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generalError = error.message ?: "Sign up failed. Please try again."
                    )
                }
        }
    }

    private fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(false, "Email is required")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                ValidationResult(false, "Please enter a valid email")
            else -> ValidationResult(true)
        }
    }

    private fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult(false, "Password is required")
            password.length < 8 -> ValidationResult(false, "Password must be at least 8 characters")
            !password.any { it.isUpperCase() } -> ValidationResult(false, "Must contain uppercase letter")
            !password.any { it.isDigit() } -> ValidationResult(false, "Must contain a number")
            else -> ValidationResult(true)
        }
    }

    private fun validateConfirmPassword(password: String, confirm: String): ValidationResult {
        return when {
            confirm.isBlank() -> ValidationResult(false, "Please confirm your password")
            confirm != password -> ValidationResult(false, "Passwords do not match")
            else -> ValidationResult(true)
        }
    }
}
