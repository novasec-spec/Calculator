package com.novasec.secureauth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<Button>(R.id.createAccountButton).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
