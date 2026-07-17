package com.novasec.auth.presentation.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.novasec.auth.R
import com.novasec.auth.ui.theme.NovaAccentAmber
import com.novasec.auth.ui.theme.NovaAccentPink
import com.novasec.auth.ui.theme.NovaPrimary
import com.novasec.auth.ui.theme.NovaSecondary

data class OnboardingPage(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val description: Int,
    val accentColor: androidx.compose.ui.graphics.Color
)

val onboardingPages = listOf(
    OnboardingPage(
        icon = R.drawable.ic_security,
        title = R.string.onboarding_title_1,
        description = R.string.onboarding_desc_1,
        accentColor = NovaPrimary
    ),
    OnboardingPage(
        icon = R.drawable.ic_fingerprint,
        title = R.string.onboarding_title_2,
        description = R.string.onboarding_desc_2,
        accentColor = NovaSecondary
    ),
    OnboardingPage(
        icon = R.drawable.ic_notification,
        title = R.string.onboarding_title_3,
        description = R.string.onboarding_desc_3,
        accentColor = NovaAccentAmber
    ),
    OnboardingPage(
        icon = R.drawable.ic_sync,
        title = R.string.onboarding_title_4,
        description = R.string.onboarding_desc_4,
        accentColor = NovaAccentPink
    )
)
