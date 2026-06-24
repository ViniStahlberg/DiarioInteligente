package com.smartdiary.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.smartdiary.databinding.ActivityRegisterBinding
import com.smartdiary.helper.FeedbackHelper

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Criar Conta"

        binding.btnRegister.setOnClickListener { attemptRegister() }
    }

    private fun attemptRegister() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirm = binding.etConfirmPassword.text.toString().trim()

        var valid = true
        if (name.isEmpty()) { binding.tilName.error = "Informe seu nome"; valid = false }
        else binding.tilName.error = null

        if (email.isEmpty()) { binding.tilEmail.error = "Informe o e-mail"; valid = false }
        else binding.tilEmail.error = null

        if (password.length < 6) { binding.tilPassword.error = "Mínimo 6 caracteres"; valid = false }
        else binding.tilPassword.error = null

        if (password != confirm) { binding.tilConfirmPassword.error = "Senhas não coincidem"; valid = false }
        else binding.tilConfirmPassword.error = null

        if (!valid) return

        binding.btnRegister.isEnabled = false
        binding.progressRegister.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.updateProfile(
                    com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                )
                FeedbackHelper.vibrate(this)
                FeedbackHelper.playSuccessSound(this)
                startActivity(Intent(this, BiometricActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
            .addOnFailureListener { e ->
                binding.btnRegister.isEnabled = true
                binding.progressRegister.visibility = View.GONE
                FeedbackHelper.showSnackbar(binding.root, "Erro: ${e.message}")
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}