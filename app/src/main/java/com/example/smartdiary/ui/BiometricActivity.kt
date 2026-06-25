package com.smartdiary.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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

        // Configuração inicial do botão e status
        binding.tvBiometricStatus.text = "Aguardando autenticação..."
        binding.btnAuth.text = "Autenticar com Biometria"

        if (!BiometricHelper.isAvailable(this)) {
            // Caso realmente não haja hardware ou biometria cadastrada
            binding.tvBiometricStatus.text = "Biometria não configurada ou indisponível.\nPor favor, cadastre uma digital nas configurações do aparelho."
            binding.btnAuth.text = "Configurar no Sistema"
            binding.btnAuth.setOnClickListener {
                // Abre a tela de segurança do Android para o usuário cadastrar a biometria
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                startActivity(intent)
            }
            return
        }

        // Inicializa o Helper com os callbacks corrigidos
        biometricHelper = BiometricHelper(
            activity = this,
            onSuccess = {
                binding.tvBiometricStatus.text = "✓ Identidade confirmada!"
                FeedbackHelper.vibrate(this)
                FeedbackHelper.playSuccessSound(this)
                goHome()
            },
            onError = { msg ->
                binding.tvBiometricStatus.text = "Erro: $msg"
                FeedbackHelper.showSnackbar(binding.root, msg)
            },
            onFailed = {
                binding.tvBiometricStatus.text = "Biometria não reconhecida. Tente de novo."
                FeedbackHelper.showToast(this, "Tentativa falhou. Tente novamente.")
            }
        )

        // Listener do botão para re-chamar caso feche o popup nativo
        binding.btnAuth.setOnClickListener {
            biometricHelper.authenticate()
        }

        // Dispara automaticamente ao abrir a tela
        biometricHelper.authenticate()
    }

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}