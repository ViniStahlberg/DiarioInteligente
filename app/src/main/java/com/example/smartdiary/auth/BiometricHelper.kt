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
            super.onAuthenticationSucceeded(result)
            onSuccess()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onError(errString.toString())
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onFailed()
        }
    }

    private val biometricPrompt = BiometricPrompt(activity, executor, callback)

    private val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Autenticação Biométrica")
        .setSubtitle("Toque no sensor para acessar seus registros")
        .setDescription("Use sua impressão digital ou face para entrar no SmartDiary")
        .setNegativeButtonText("Cancelar")
        .build()

    fun authenticate() {
        biometricPrompt.authenticate(promptInfo)
    }

    companion object {
        fun isAvailable(activity: FragmentActivity): Boolean {
            val manager = BiometricManager.from(activity)
            return manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
            ) == BiometricManager.BIOMETRIC_SUCCESS
        }
    }
}