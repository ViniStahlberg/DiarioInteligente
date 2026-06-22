package com.smartdiary.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.smartdiary.auth.BiometricHelper
import com.smartdiary.databinding.ActivityBiometricBinding
import com.smartdiary.helper.FeedbackHelper

class BiometricActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBiometricBinding
    private lateinit var biometricHelper: BiometricHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBiometricBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBiometric()

        binding.btnAuthBiometric.setOnClickListener {
            biometricHelper.authenticate()
        }

        // Se biometria não disponível, vai direto
        if (!BiometricHelper.isAvailable(this)) {
            binding.tvBiometricStatus.text = "Biometria não disponível neste dispositivo"
            binding.btnAuthBiometric.text = "Continuar"
            binding.btnAuthBiometric.setOnClickListener {
                goToHome()
            }
        }
    }

    private fun setupBiometric() {
        biometricHelper = BiometricHelper(
            activity = this,
            onSuccess = {
                binding.tvBiometricStatus.text = "✓ Autenticado com sucesso!"
                FeedbackHelper.vibrate(this)
                FeedbackHelper.playSuccessSound(this)
                goToHome()
            },
            onError = { error ->
                binding.tvBiometricStatus.text = "Erro: $error"
                FeedbackHelper.showSnackbar(binding.root, "Erro: $error")
            },
            onFailed = {
                binding.tvBiometricStatus.text = "Biometria não reconhecida. Tente novamente."
                FeedbackHelper.showToast(this, "Biometria não reconhecida")
            }
        )

        // Inicia autenticação automaticamente
        if (BiometricHelper.isAvailable(this)) {
            biometricHelper.authenticate()
        }
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}