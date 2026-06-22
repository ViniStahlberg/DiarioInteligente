package com.smartdiary.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.smartdiary.databinding.ActivityHomeBinding
import com.smartdiary.viewmodel.DiaryViewModel

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: DiaryViewModel by viewModels()
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("smart_diary_prefs", MODE_PRIVATE)

        observeEntryCount()
        setupClickListeners()
    }

    private fun observeEntryCount() {
        viewModel.entryCount.observe(this) { count ->
            binding.tvEntryCount.text = getString(
                com.smartdiary.R.string.home_entries_count, count
            )
        }
    }

    private fun setupClickListeners() {
        binding.btnNewEntry.setOnClickListener {
            startActivity(Intent(this, NewEntryActivity::class.java))
        }

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            prefs.edit().putBoolean("is_logged_in", false).apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }
}