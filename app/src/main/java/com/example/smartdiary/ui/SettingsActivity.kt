package com.smartdiary.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.smartdiary.databinding.ActivitySettingsBinding
import com.smartdiary.helper.FeedbackHelper
import com.smartdiary.viewmodel.DiaryViewModel

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: DiaryViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Configurações"

        val user = auth.currentUser
        binding.tvUserEmail.text = user?.email ?: "—"
        binding.tvUserName.text = user?.displayName ?: "Usuário"
        binding.tvAppVersion.text = "SmartDiary v1.0"

        viewModel.isLoading.observe(this) { loading ->
            binding.progressSettings.visibility = if (loading) View.VISIBLE else View.GONE
        }

        binding.btnClearData.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("⚠ Limpar todos os dados")
                .setMessage("Todos os seus registros serão excluídos do Firestore permanentemente. Deseja continuar?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar") { _, _ ->
                    viewModel.deleteAllEntries()
                    FeedbackHelper.vibrate(this)
                    FeedbackHelper.showToast(this, "Todos os registros foram removidos")
                }
                .show()
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            FeedbackHelper.showToast(this, "Até logo!")
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed(); return true
    }
}