package com.novasec.nova.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.novasec.nova.R
import com.novasec.nova.databinding.FragmentHomeBinding
import com.novasec.nova.data.local.AppDatabase
import com.novasec.nova.data.local.entities.MoodEntry
import com.novasec.nova.ui.home.adapters.QuickAccessAdapter
import com.novasec.nova.utils.AppPreferences
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var quickAccessAdapter: QuickAccessAdapter
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getInstance(requireContext())

        setupQuickAccess()
        loadData()
        setupListeners()
    }

    private fun setupQuickAccess() {
        val quickAccessItems = listOf(
            QuickAccessItem(R.drawable.ic_new_note, "New Note", "#FF6B9D"),
            QuickAccessItem(R.drawable.ic_memory_jar, "Memory Jar", "#A855F7"),
            QuickAccessItem(R.drawable.ic_from_him, "From Him", "#EC4899"),
            QuickAccessItem(R.drawable.ic_playlist, "Our Playlist", "#22C55E"),
            QuickAccessItem(R.drawable.ic_vault, "Vault", "#3B82F6"),
            QuickAccessItem(R.drawable.ic_alerts, "Alerts", "#F59E0B")
        )

        quickAccessAdapter = QuickAccessAdapter(quickAccessItems)
        binding.recyclerQuickAccess.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerQuickAccess.adapter = quickAccessAdapter
    }

    private fun loadData() {
        lifecycleScope.launch {
            // Load today's mood
            loadTodayMood()

            // Load streak
            loadStreak()

            // Load quote
            loadDailyQuote()

            // Load unread count
            loadUnreadCount()
        }
    }

    private suspend fun loadTodayMood() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val mood = database.moodDao().getMoodForDate(today)
        
        if (mood != null) {
            binding.tvMoodLabel.text = mood.mood
            binding.tvMoodEmoji.text = getMoodEmoji(mood.mood)
            binding.tvMoodTime.text = "Logged • ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())}"
            binding.layoutTodayMood.visibility = View.VISIBLE
            binding.btnLogMood.text = "Update Mood"
        } else {
            binding.layoutTodayMood.visibility = View.GONE
            binding.btnLogMood.text = "Log Mood"
        }
    }

    private suspend fun loadStreak() {
        val streak = AppPreferences.getStreak()
        binding.tvStreak.text = "$streak days in a row!"
    }

    private suspend fun loadDailyQuote() {
        val quote = getDailyQuote()
        binding.tvQuote.text = "\"${quote.first}\""
        binding.tvQuoteAuthor.text = "— ${quote.second}"
    }

    private suspend fun loadUnreadCount() {
        val count = AppPreferences.getUnreadCount()
        if (count > 0) {
            binding.tvUnreadCount.text = count.toString()
            binding.badgeUnread.visibility = View.VISIBLE
        } else {
            binding.badgeUnread.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        binding.btnLogMood.setOnClickListener {
            showMoodPicker()
        }

        binding.btnRefreshQuote.setOnClickListener {
            loadDailyQuote()
        }

        binding.btnCopyQuote.setOnClickListener {
            copyQuoteToClipboard()
        }
    }

    private fun showMoodPicker() {
        // Will implement mood picker dialog
    }

    private fun copyQuoteToClipboard() {
        // Will implement clipboard copy
    }

    private fun getMoodEmoji(mood: String): String {
        return when (mood.lowercase()) {
            "happy" -> "😊"
            "loved" -> "🥰"
            "relaxed" -> "😌"
            "thoughtful" -> "🤔"
            "sad" -> "😢"
            "frustrated" -> "😤"
            else -> "😊"
        }
    }

    private fun getDailyQuote(): Pair<String, String> {
        // Will fetch from local quotes database
        return Pair("Every day with you is my favourite day", "Your Person 💕")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class QuickAccessItem(
    val icon: Int,
    val label: String,
    val color: String
)
