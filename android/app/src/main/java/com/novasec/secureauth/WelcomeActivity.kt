package com.novasec.secureauth

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            // TODO: launch LoginActivity once secure auth layer exists
            Toast.makeText(this, "Login — building this next", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.createAccountButton).setOnClickListener {
            // TODO: launch RegisterActivity once secure auth layer exists
            Toast.makeText(this, "Create account — building this next", Toast.LENGTH_SHORT).show()
        }
    }
}
