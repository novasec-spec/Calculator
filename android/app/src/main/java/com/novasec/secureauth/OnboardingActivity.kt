package com.novasec.secureauth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        WindowCompat.setDecorFitsSystemWindows(window, false)
      
     val pages = listOf(
            OnboardingItem(
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1)
            ),
            OnboardingItem(
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2)
            ),
            OnboardingItem(
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3)
            )
        )

        val pager = findViewById<ViewPager2>(R.id.onboardingPager)
        val tabLayout = findViewById<TabLayout>(R.id.indicatorDots)
        val skipButton = findViewById<TextView>(R.id.skipButton)
        val nextButton = findViewById<Button>(R.id.nextButton)

        pager.adapter = OnboardingPagerAdapter(pages)

        // Connect TabLayout with ViewPager2 for dots indicator
        TabLayoutMediator(tabLayout, pager) { _, _ ->
            // No text needed, just dots
        }.attach()

        skipButton.setOnClickListener { goToWelcome() }

        nextButton.setOnClickListener {
            val nextIndex = pager.currentItem + 1
            if (nextIndex < pages.size) {
                pager.currentItem = nextIndex
            } else {
                goToWelcome()
            }
        }

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                nextButton.text = if (position == pages.size - 1)
                    getString(R.string.get_started)
                else
                    getString(R.string.next)
            }
        })
    }

    private fun goToWelcome() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }
}
