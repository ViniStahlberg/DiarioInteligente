package com.smartdiary.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricHelper(
    private val activity: FragmentActivity,
    private val onSuccess: () -> Unit,
    private val onError: (String) -> Unit,
    private val onFailed: () -> Unit
) {
    private val executor = ContextCompat.getMainExecutor(activity)

    private val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            onSuccess()
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            onError(errString.toString())
        }
        override fun onAuthenticationFailed() {
            onFailed()
        }
    }

    private val prompt = BiometricPrompt(activity, executor, callback)

    private val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Autenticação Biométrica")
        .setSubtitle("Use sua digital ou reconhecimento facial")
        .setDescription("Confirme sua identidade para acessar o SmartDiary")
        .setNegativeButtonText("Cancelar")
        .build()

    fun authenticate() = prompt.authenticate(promptInfo)

    companion object {
        fun isAvailable(activity: FragmentActivity): Boolean {
            val mgr = BiometricManager.from(activity)
            return mgr.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
            ) == BiometricManager.BIOMETRIC_SUCCESS
        }
    }
}