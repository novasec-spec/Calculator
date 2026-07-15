package com.novasec.secureauth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.novasec.secureauth.data.models.LoginRequest
import com.novasec.secureauth.data.models.AuthSession
import com.novasec.secureauth.network.ApiClient
import com.novasec.secureauth.security.SessionManager
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var errorMessage: TextView
    private lateinit var loginButton: Button
    private lateinit var biometricButton: Button
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        // Initialize views
        val backButton = findViewById<ImageView>(R.id.backButton)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        errorMessage = findViewById(R.id.errorMessage)
        loginButton = findViewById(R.id.loginButton)
        biometricButton = findViewById(R.id.biometricButton)
        val registerLink = findViewById<TextView>(R.id.registerLink)

        backButton.setOnClickListener { finish() }
        registerLink.setOnClickListener { 
            startActivity(android.content.Intent(this, RegisterActivity::class.java))
        }

        loginButton.setOnClickListener { performLogin() }

        // Check for biometric availability
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

        // Disable button to prevent double clicks
        loginButton.isEnabled = false
        loginButton.text = "Signing in..."

        coroutineScope.launch {
            try {
                // Check rate limiting first
                if (sessionManager.isLockedOut(email)) {
                    showError("Too many failed attempts. Please try again later.")
                    loginButton.isEnabled = true
                    loginButton.text = "Sign In"
                    return@launch
                }

                // Attempt login
                val response = ApiClient.apiService.login(LoginRequest(email, password))
                
                // Save session
                val session = AuthSession(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    expiresAt = System.currentTimeMillis() + (response.expiresIn * 1000),
                    user = response.user
                )
                sessionManager.saveSession(session)
                sessionManager.recordAuthAttempt(email, true)

                // Success - go to main screen
                Toast.makeText(this@LoginActivity, "Welcome ${response.user.fullName}!", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to MainActivity

            } catch (e: Exception) {
                sessionManager.recordAuthAttempt(email, false)
                val error = if (e.message != null) "Invalid credentials" else "Network error. Please try again."
                showError(error)
            } finally {
                loginButton.isEnabled = true
                loginButton.text = "Sign In"
            }
        }
    }

    private fun checkBiometricAvailability() {
        val biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Biometric success - get cached credentials and login
                    coroutineScope.launch {
                        val email = sessionManager.getCachedUser("")?.email
                        if (email != null) {
                            // Auto-fill and login
                            emailInput.setText(email)
                            performLogin()
                        }
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@LoginActivity, "Biometric failed: $errString", Toast.LENGTH_SHORT).show()
                }
            }
        )

        // Check if biometric is enabled and available
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
