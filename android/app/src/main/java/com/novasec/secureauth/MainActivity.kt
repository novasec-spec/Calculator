package com.novasec.secureauth

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.novasec.secureauth.security.SessionManager
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)

        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        coroutineScope.launch {
            val user = sessionManager.getSession()
            if (user != null) {
                welcomeText.text = "Welcome, ${user.fullName}! 👋"
            } else {
                welcomeText.text = "Welcome! 👋"
            }
        }

        logoutButton.setOnClickListener {
            coroutineScope.launch {
                sessionManager.clearSession()
                Toast.makeText(
                    this@MainActivity,
                    "Logged out successfully",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
