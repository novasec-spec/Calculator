package com.novasec.auth.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novasec.auth.R
import com.novasec.auth.ui.theme.NovaAccentCyan
import com.novasec.auth.ui.theme.NovaGradientEnd
import com.novasec.auth.ui.theme.NovaGradientStart
import com.novasec.auth.ui.theme.NovaOnBackground

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    LaunchedEffect(destination) {
        when (destination) {
            is SplashDestination.Onboarding -> onNavigateToOnboarding()
            is SplashDestination.Login -> onNavigateToLogin()
            is SplashDestination.Home -> onNavigateToHome()
            null -> { /* Still loading */ }
        }
    }

    SplashContent()
}

@Composable
private fun SplashContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_animation")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background,
                        NovaGradientStart.copy(alpha = 0.1f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo Container
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(pulseAlpha)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    NovaGradientStart.copy(alpha = 0.3f),
                                    NovaGradientStart.copy(alpha = 0f)
                                )
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )

                // Icon
                Icon(
                    painter = painterResource(id = R.drawable.ic_splash),
                    contentDescription = "NovaSec Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .alpha(alpha),
                    tint = NovaGradientStart
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Name with gradient text effect
            Text(
                text = "NovaSec",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(NovaGradientStart, NovaGradientEnd, NovaAccentCyan)
                    )
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tagline
            Text(
                text = "Secure. Simple. Smart.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = NovaOnBackground.copy(alpha = 0.6f),
                    letterSpacing = 4.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading indicator
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = NovaGradientStart,
                strokeWidth = 2.dp
            )
        }
    }
}
