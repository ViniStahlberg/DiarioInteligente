package com.smartdiary.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.smartdiary.databinding.ActivityLoginBinding
import com.smartdiary.helper.FeedbackHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: SharedPreferences

    // Credenciais fixas locais (sem Firebase)
    companion object {
        const val VALID_USER = "admin"
        const val VALID_PASS = "1234"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("smart_diary_prefs", MODE_PRIVATE)

        binding.btnLogin.setOnClickListener {
            val user = binding.etUsername.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            attemptLogin(user, pass)
        }
    }

    private fun attemptLogin(user: String, pass: String) {
        if (user == VALID_USER && pass == VALID_PASS) {
            prefs.edit().putBoolean("is_logged_in", true).apply()
            FeedbackHelper.vibrate(this)
            startActivity(Intent(this, BiometricActivity::class.java))
            finish()
        } else {
            binding.tilUsername.error = null
            binding.tilPassword.error = getString(com.smartdiary.R.string.error_invalid_credentials)
            FeedbackHelper.showSnackbar(
                binding.root,
                getString(com.smartdiary.R.string.error_invalid_credentials)
            )
        }
    }
}