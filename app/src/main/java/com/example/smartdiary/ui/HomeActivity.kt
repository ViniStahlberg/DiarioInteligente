package com.smartdiary.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.smartdiary.databinding.ActivityHomeBinding
import com.smartdiary.viewmodel.DiaryViewModel

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: DiaryViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = auth.currentUser?.displayName?.ifEmpty { "Usuário" } ?: "Usuário"
        binding.tvGreeting.text = "Olá, $name 👋"

        viewModel.entryCount.observe(this) { count ->
            binding.tvEntryCount.text = when (count) {
                0 -> "Nenhum registro ainda.\nCrie seu primeiro momento!"
                1 -> "Você tem 1 registro no seu diário."
                else -> "Você tem $count registros no seu diário."
            }
        }

        // Humor predominante da semana
        viewModel.entries.observe(this) { entries ->
            if (entries.isNullOrEmpty()) {
                binding.tvMoodSummary.text = "Sem registros esta semana"
                return@observe
            }
            val moodCounts = viewModel.getMoodCountsForChart()
            val dominant = moodCounts.maxByOrNull { it.value }
            if (dominant != null) {
                binding.tvMoodSummary.text = "Humor predominante esta semana: ${dominant.key}"
            } else {
                binding.tvMoodSummary.text = "Registre seu humor hoje!"
            }
        }

        binding.btnNewEntry.setOnClickListener {
            startActivity(Intent(this, NewEntryActivity::class.java))
        }
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.btnMoodChart.setOnClickListener {
            startActivity(Intent(this, MoodChartActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}