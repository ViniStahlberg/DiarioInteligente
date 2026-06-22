package com.smartdiary.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.smartdiary.databinding.ActivitySettingsBinding
import com.smartdiary.helper.FeedbackHelper
import com.smartdiary.viewmodel.DiaryViewModel

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: DiaryViewModel by viewModels()
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Configurações"

        prefs = getSharedPreferences("smart_diary_prefs", MODE_PRIVATE)

        binding.tvAppVersion.text = "SmartDiary v1.0"
        binding.tvDevInfo.text = "Desenvolvido com Kotlin + Room + CameraX"

        binding.btnClearData.setOnClickListener {
            showClearDataDialog()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun showClearDataDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Limpar todos os dados")
            .setMessage("Todos os registros serão excluídos permanentemente. Confirma?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Confirmar") { _, _ ->
                viewModel.deleteAll()
                FeedbackHelper.vibrate(this)
                FeedbackHelper.showToast(this, "Todos os registros foram removidos")
            }
            .show()
    }

    private fun logout() {
        prefs.edit().putBoolean("is_logged_in", false).apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}