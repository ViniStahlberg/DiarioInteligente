package com.smartdiary.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smartdiary.databinding.ActivityContextTimelineBinding
import com.smartdiary.model.DiaryEntry
import com.smartdiary.ui.adapter.DiaryEntryAdapter
import java.text.SimpleDateFormat
import java.util.Locale

class ContextTimelineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContextTimelineBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var diaryAdapter: DiaryEntryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContextTimelineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadTimelineData()

        // Configuração do clique do botão para gerar e injetar o registro contextual randômico
        binding.fabAddEntry.setOnClickListener {
            criarRegistroRapidoNaLinhaDoTempo()
        }
    }

    private fun setupToolbar() {
        binding.toolbarTimeline.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        diaryAdapter = DiaryEntryAdapter(
            onItemClick = { entry ->
                val intent = Intent(this, EntryDetailActivity::class.java).apply {
                    putExtra("ENTRY_ID", entry.id)
                }
                startActivity(intent)
            },
            onItemLongClick = { true }
        )

        binding.rvTimelineContext.apply {
            layoutManager = LinearLayoutManager(this@ContextTimelineActivity)
            adapter = diaryAdapter
        }
    }

    private fun loadTimelineData() {
        val currentFirebaseUser = auth.currentUser
        val userId = currentFirebaseUser?.uid ?: ""

        binding.progressBar.visibility = View.VISIBLE

        db.collection("entries")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                val entriesList = mutableListOf<DiaryEntry>()

                for (doc in documents) {
                    try {
                        val id = doc.id
                        val uId = doc.getString("userId") ?: ""
                        val title = doc.getString("title") ?: ""
                        val description = doc.getString("description") ?: ""
                        val imageUrl = doc.getString("imageUrl") ?: ""
                        val mood = doc.getString("mood") ?: "📝"
                        val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                        // Resgata o campo numérico genérico com segurança para evitar ClassCastException
                        val lightLevelRaw = doc.get("lightLevel")
                            ?: doc.get("lightlevel")
                            ?: doc.get("light_level")
                        val lightLevel = (lightLevelRaw as? Number)?.toFloat() ?: 0.0f

                        val stepsRaw = doc.get("stepsAtTime")
                            ?: doc.get("stepsattime")
                            ?: doc.get("steps_at_time")
                            ?: doc.get("steps")
                        val stepsAtTime = (stepsRaw as? Number)?.toInt() ?: 0

                        val entry = DiaryEntry(
                            id = id,
                            userId = uId,
                            title = title,
                            description = description,
                            imageUrl = imageUrl,
                            lightLevel = lightLevel,
                            stepsAtTime = stepsAtTime,
                            mood = mood,
                            createdAt = createdAt
                        )
                        entriesList.add(entry)
                    } catch (e: Exception) {
                        android.util.Log.e("SMART_DIARY", "Erro mapeamento: ${e.message}", e)
                    }
                }

                if (entriesList.isEmpty()) {
                    binding.tvEmptyTimeline.visibility = View.VISIBLE
                    binding.tvEmptyTimeline.text = "Nenhum contexto registrado ainda."
                    binding.rvTimelineContext.visibility = View.GONE
                } else {
                    binding.tvEmptyTimeline.visibility = View.GONE
                    binding.rvTimelineContext.visibility = View.VISIBLE

                    val sortedList = entriesList.sortedByDescending { it.createdAt }
                    diaryAdapter.submitList(sortedList)
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.tvEmptyTimeline.visibility = View.VISIBLE
                binding.tvEmptyTimeline.text = "Nenhum contexto registrado ainda."
                binding.rvTimelineContext.visibility = View.GONE
            }
    }

    // Método avançado que gera dados contextuais simulados e inteligentes salvando direto no Firestore
    private fun criarRegistroRapidoNaLinhaDoTempo() {
        val currentFirebaseUser = auth.currentUser
        val userIdLogado = currentFirebaseUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE

        // 1. Coleta e gera dados base ambientais de teste
        val horaAtual = SimpleDateFormat("HH:mm", Locale.getDefault()).format(java.util.Date())
        val passosSimulados = (400..9900).random()
        val luxSimulado = (5..800).random().toFloat()

        val humores = listOf("😄", "😊", "📝", "⚡", "😴", "😔")
        val humorAleatorio = humores.random()

        // 2. Define títulos alternativos baseado no humor sorteado
        val tituloDinamico = when (humorAleatorio) {
            "😄", "😊" -> "Momento de Energia ($horaAtual)"
            "⚡" -> "Pico de Estresse ($horaAtual)"
            "😴" -> "Momento de Descanso ($horaAtual)"
            "😔" -> "Momento de Reflexão ($horaAtual)"
            else -> "Nota Contextual ($horaAtual)"
        }

        // 3. Monta mensagens descritivas inteligentes cruzando humor com sensores
        val descricaoDinamica = when {
            humorAleatorio == "😴" && luxSimulado < 50 ->
                "Preparando para dormir em um ambiente devidamente escuro."
            humorAleatorio == "⚡" && passosSimulados < 1500 ->
                "Sentindo agitação acumulada após passar muito tempo sentado."
            humorAleatorio == "😄" && luxSimulado > 450 ->
                "Bom humor impulsionado por um ambiente bem iluminado e ativo."
            passosSimulados > 7500 ->
                "Registro feito logo após uma caminhada intensa ou atividade física."
            luxSimulado < 20 ->
                "Registro feito em um local com pouca luz. Monitorando fadiga visual."
            else ->
                "Análise de rotina: correlacionando sensores de movimento e ambiente com o bem-estar."
        }


        val novoRegistroRapido = hashMapOf(
            "userId" to userIdLogado,
            "title" to tituloDinamico,
            "description" to descricaoDinamica,
            "imageUrl" to "",
            "lightLevel" to luxSimulado,
            "stepsAtTime" to passosSimulados,
            "mood" to humorAleatorio,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("entries")
            .add(novoRegistroRapido)
            .addOnSuccessListener {
                Toast.makeText(this, "Novo contexto inteligente registrado! 🎉", Toast.LENGTH_SHORT).show()
                loadTimelineData() // Recarrega automaticamente a lista da tela
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Erro ao criar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}