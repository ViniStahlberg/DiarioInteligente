package com.smartdiary.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.smartdiary.databinding.ActivityLoginBinding
import com.smartdiary.helper.FeedbackHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { attemptLogin() }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilEmail.error = "Informe o e-mail"
            return
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Informe a senha"
            return
        }

        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.btnLogin.isEnabled = false
        binding.progressLogin.visibility = android.view.View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                FeedbackHelper.vibrate(this)
                startActivity(Intent(this, BiometricActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnLogin.isEnabled = true
                binding.progressLogin.visibility = android.view.View.GONE
                FeedbackHelper.showSnackbar(binding.root, "Erro: ${e.message}")
            }
    }
}