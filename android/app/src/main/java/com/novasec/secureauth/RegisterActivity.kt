package com.novasec.secureauth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.novasec.secureauth.repository.AuthRepository
import com.novasec.secureauth.security.SessionManager
import kotlinx.coroutines.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepository
    private lateinit var fullNameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var passwordStrength: TextView
    private lateinit var errorMessage: TextView
    private lateinit var registerButton: Button
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sessionManager = SessionManager(this)
        authRepository = AuthRepository(this)

        // Initialize views
        val backButton = findViewById<ImageView>(R.id.backButton)
        fullNameInput = findViewById(R.id.fullNameInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        passwordStrength = findViewById(R.id.passwordStrength)
        errorMessage = findViewById(R.id.errorMessage)
        registerButton = findViewById(R.id.registerButton)
        val loginLink = findViewById<TextView>(R.id.loginLink)

        backButton.setOnClickListener { finish() }
        loginLink.setOnClickListener { finish() }

        // Password strength checker
        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkPasswordStrength()
            }
        })

        registerButton.setOnClickListener { performRegistration() }
    }

    private fun checkPasswordStrength() {
        val password = passwordInput.text.toString()
        val strength = when {
            password.length < 6 -> "Weak"
            password.length < 10 -> "Medium"
            else -> "Strong"
        }
        passwordStrength.text = "Password strength: $strength"
        val color = when (strength) {
            "Weak" -> getColor(android.R.color.holo_red_dark)
            "Medium" -> getColor(android.R.color.holo_orange_dark)
            else -> getColor(android.R.color.holo_green_dark)
        }
        passwordStrength.setTextColor(color)
    }

    private fun performRegistration() {
        val fullName = fullNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()

        // Validation
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields")
            return
        }

        if (fullName.length < 2) {
            showError("Please enter your full name")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email")
            return
        }

        if (password.length < 8) {
            showError("Password must be at least 8 characters")
            return
        }

        if (password != confirmPassword) {
            showError("Passwords do not match")
            return
        }

        registerButton.isEnabled = false
        registerButton.text = "Creating account..."

        coroutineScope.launch {
            try {
                // Attempt registration with Supabase
                val result = withTimeout(10000) {
                    authRepository.signUp(email, password, fullName)
                }

                result.fold(
                    onSuccess = { user ->
                        // Save session
                        sessionManager.saveSession(user)
                        
                        Toast.makeText(
                            this@RegisterActivity,
                            "Account created successfully!",
                            Toast.LENGTH_LONG
                        ).show()
                        
// Replace finish() with:
startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
finish()
                    },
                    onFailure = { error ->
                        val message = when {
                            error.message?.contains("User already registered") == true ->
                                "Email already registered. Please login."
                            else -> error.message ?: "Registration failed"
                        }
                        showError(message)
                    }
                )

            } catch (e: java.util.concurrent.TimeoutException) {
                showError("Connection timeout. Please try again.")
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("Unable to resolve host") == true -> 
                        "Network error. Please check your connection."
                    e.message?.contains("timeout") == true ->
                        "Connection timeout. Please try again."
                    else -> "Registration failed. Please try again."
                }
                showError(message)
            } finally {
                registerButton.isEnabled = true
                registerButton.text = "Create Account"
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
