package com.novasec.auth

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.novasec.auth.data.local.PreferencesManager
import com.novasec.auth.presentation.navigation.NovaSecNavHost
import com.novasec.auth.ui.theme.NovaSecTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { !isReady }

        enableEdgeToEdge()

        lifecycleScope.launch {
            // Simulate loading and check onboarding status
            delay(1500)
            val hasCompletedOnboarding = PreferencesManager.getInstance(applicationContext)
                .hasCompletedOnboarding()
                .first()

            isReady = true

            setContent {
                NovaSecTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NovaSecNavHost(
                            startDestination = if (hasCompletedOnboarding) {
                                com.novasec.auth.presentation.navigation.Screen.Splash.route
                            } else {
                                com.novasec.auth.presentation.navigation.Screen.Splash.route
                            }
                        )
                    }
                }
            }
        }

        // Splash screen exit animation
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val slideUp = ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.TRANSLATION_Y,
                0f,
                -splashScreenView.view.height.toFloat()
            )

            slideUp.apply {
                duration = 500
                interpolator = AnticipateInterpolator()
                doOnEnd { splashScreenView.remove() }
                start()
            }
        }
    }
}
