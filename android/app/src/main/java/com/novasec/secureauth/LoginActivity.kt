package com.novasec.secureauth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.novasec.secureauth.repository.AuthRepository
import com.novasec.secureauth.security.SessionManager
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepository
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var errorMessage: TextView
    private lateinit var loginButton: Button
    private lateinit var biometricButton: Button
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize
        sessionManager = SessionManager(this)
        authRepository = AuthRepository(this)

        // Initialize views
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        errorMessage = findViewById(R.id.errorMessage)
        loginButton = findViewById(R.id.loginButton)
        biometricButton = findViewById(R.id.biometricButton)
        val backButton = findViewById<ImageView>(R.id.backButton)
        val registerLink = findViewById<TextView>(R.id.registerLink)

        backButton.setOnClickListener { finish() }
        registerLink.setOnClickListener {
            startActivity(android.content.Intent(this, RegisterActivity::class.java))
        }

        loginButton.setOnClickListener { performLogin() }

        checkBiometricAvailability()
    }

    private fun performLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email")
            return
        }

        loginButton.isEnabled = false
        loginButton.text = "Signing in..."

        coroutineScope.launch {
            try {
                // Check rate limiting
                if (sessionManager.isLockedOut(email)) {
                    showError("Too many failed attempts. Please try again later.")
                    loginButton.isEnabled = true
                    loginButton.text = "Sign In"
                    return@launch
                }

                // Attempt login with Supabase
                val result = withTimeout(10000) {
                    authRepository.signIn(email, password)
                }

                result.fold(
                    onSuccess = { user ->
                        // Save session
                        sessionManager.saveSession(user)
                        sessionManager.recordAuthAttempt(email, true)

                        Toast.makeText(
                            this@LoginActivity,
                            "Welcome ${user.fullName}!",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // Navigate to main screen

// Replace finish() with:
startActivity(Intent(this, MainActivity::class.java))
finish()
                        // TODO: Start MainActivity
                    },
                    onFailure = { error ->
                        sessionManager.recordAuthAttempt(email, false)
                        val message = when (error.message) {
                            null -> "Invalid credentials"
                            else -> error.message ?: "Login failed"
                        }
                        showError(message)
                    }
                )

            } catch (e: java.util.concurrent.TimeoutException) {
                showError("Connection timeout. Please try again.")
            } catch (e: Exception) {
                sessionManager.recordAuthAttempt(email, false)
                val message = when {
                    e.message?.contains("Unable to resolve host") == true -> 
                        "Network error. Please check your connection."
                    e.message?.contains("timeout") == true ->
                        "Connection timeout. Please try again."
                    else -> "Login failed. Please try again."
                }
                showError(message)
            } finally {
                loginButton.isEnabled = true
                loginButton.text = "Sign In"
            }
        }
    }

    private fun checkBiometricAvailability() {
        try {
            val biometricPrompt = BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        coroutineScope.launch {
                            try {
                                // Try to login with cached credentials
                                val cachedUser = sessionManager.getCachedUser("")
                                if (cachedUser != null) {
                                    emailInput.setText(cachedUser.email)
                                    // Use stored password from secure storage
                                    performLogin()
                                } else {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "No saved credentials found",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Biometric login failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(
                            this@LoginActivity,
                            "Biometric failed: $errString",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )

            if (sessionManager.isBiometricEnabled()) {
                biometricButton.visibility = android.view.View.VISIBLE
                biometricButton.setOnClickListener {
                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Biometric Login")
                        .setSubtitle("Use your fingerprint to login")
                        .setDescription("Securely access your account")
                        .setNegativeButtonText("Cancel")
                        .build()
                    biometricPrompt.authenticate(promptInfo)
                }
            }
        } catch (e: Exception) {
            biometricButton.visibility = android.view.View.GONE
        }
    }

    private fun showError(message: String) {
        errorMessage.text = message
        errorMessage.visibility = android.view.View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
