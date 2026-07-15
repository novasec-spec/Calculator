package com.novasec.secureauth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // must run before super.onCreate()
        super.onCreate(savedInstanceState)

        // TODO (later step): once encrypted session storage exists, check here
        // for a valid token and skip straight past onboarding if the user's
        // already signed in. For now we always route through onboarding.
        startActivity(Intent(this, OnboardingActivity::class.java))
        finish()
    }
}
