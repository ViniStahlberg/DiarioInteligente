package com.example.moodlog

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var quantidadeAberturas = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        quantidadeAberturas = savedInstanceState?.getInt("aberturas") ?: 0
        quantidadeAberturas++

        val horarioAtual = SimpleDateFormat(
            "HH:mm:ss",
            Locale.getDefault()
        ).format(Date())

        Toast.makeText(
            this,
            "MoodLog aberto às $horarioAtual",
            Toast.LENGTH_LONG
        ).show()

        supportActionBar?.title =
            "MoodLog • Sessão $quantidadeAberturas"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt("aberturas", quantidadeAberturas)
    }
}