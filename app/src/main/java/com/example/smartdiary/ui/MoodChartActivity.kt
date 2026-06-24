package com.smartdiary.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.smartdiary.databinding.ActivityMoodChartBinding
import com.smartdiary.viewmodel.DiaryViewModel

class MoodChartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodChartBinding
    private val viewModel: DiaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Meu Humor da Semana"

        viewModel.entries.observe(this) {
            setupChart()
        }
    }

    private fun setupChart() {
        val moodCounts = viewModel.getMoodCountsForChart()

        if (moodCounts.isEmpty()) {
            binding.chartMood.visibility = View.GONE
            binding.tvChartEmpty.visibility = View.VISIBLE
            return
        }

        binding.chartMood.visibility = View.VISIBLE
        binding.tvChartEmpty.visibility = View.GONE

        val allMoods = listOf("😄", "😔", "😤", "😴", "😐")
        val moodColors = listOf(
            Color.parseColor("#FFD700"), // 😄 amarelo
            Color.parseColor("#6495ED"), // 😔 azul
            Color.parseColor("#FF6347"), // 😤 vermelho
            Color.parseColor("#9B59B6"), // 😴 roxo
            Color.parseColor("#95A5A6")  // 😐 cinza
        )
        val moodLabels = listOf("Feliz", "Triste", "Estres.", "Cansado", "Neutro")

        val entries = allMoods.mapIndexed { index, mood ->
            BarEntry(index.toFloat(), (moodCounts[mood] ?: 0).toFloat())
        }

        val dataSet = BarDataSet(entries, "Registros por Humor").apply {
            colors = moodColors
            valueTextSize = 13f
            valueTextColor = Color.BLACK
        }

        binding.chartMood.apply {
            data = BarData(dataSet).apply { barWidth = 0.6f }
            xAxis.valueFormatter = IndexAxisValueFormatter(moodLabels)
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            axisLeft.granularity = 1f
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            setFitBars(true)
            animateY(800)
            invalidate()
        }

        // Resumo textual
        val dominant = moodCounts.maxByOrNull { it.value }
        val total = moodCounts.values.sum()
        binding.tvMoodSummaryChart.text =
            "📊 $total registros esta semana\n" +
                    "Humor mais frequente: ${dominant?.key} (${dominant?.value}x)"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed(); return true
    }
}