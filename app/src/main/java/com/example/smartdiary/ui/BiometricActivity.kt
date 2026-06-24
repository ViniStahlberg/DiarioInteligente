package com.smartdiary.ui

import android.content.Intent
import android.os.Bundle
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

        if (!BiometricHelper.isAvailable(this)) {
            binding.tvBiometricStatus.text = "Biometria não disponível.\nAcesso liberado."
            binding.btnAuth.text = "Continuar"
            binding.btnAuth.setOnClickListener { goHome() }
            return
        }

        biometricHelper = BiometricHelper(
            activity = this,
            onSuccess = {
                binding.tvBiometricStatus.text = "✓ Identidade confirmada!"
                FeedbackHelper.vibrate(this)
                FeedbackHelper.playSuccessSound(this)
                goHome()
            },
            onError = { msg ->
                binding.tvBiometricStatus.text = msg
                FeedbackHelper.showSnackbar(binding.root, msg)
            },
            onFailed = {
                binding.tvBiometricStatus.text = "Biometria não reconhecida. Tente de novo."
                FeedbackHelper.showToast(this, "Tente novamente")
            }
        )

        binding.btnAuth.setOnClickListener { biometricHelper.authenticate() }
        biometricHelper.authenticate()
    }

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}