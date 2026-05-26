package com.example.moodlog

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var quantidadeAberturas = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Recupera valor salvo ao girar a tela
        quantidadeAberturas = savedInstanceState?.getInt("aberturas") ?: 0

        quantidadeAberturas++

        Toast.makeText(
            this,
            "MoodLog iniciado $quantidadeAberturas vez(es)",
            Toast.LENGTH_SHORT
        ).show()

        supportActionBar?.title = "MoodLog - Diário de Humor"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Salva quantidade de aberturas
        outState.putInt("aberturas", quantidadeAberturas)
    }
}