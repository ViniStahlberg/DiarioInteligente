package com.smartdiary.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smartdiary.databinding.ActivityContextTimelineBinding
import com.smartdiary.model.DiaryEntry
import com.smartdiary.ui.adapter.DiaryEntryAdapter

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

                        // SUPORTE DUPLO DE NOMENCLATURA:
                        // Tenta ler 'lightLevel' (camelCase). Se vier nulo, tenta 'lightlevel' ou 'light_level'
                        val lightLevel = (doc.getDouble("lightLevel")
                            ?: doc.getDouble("lightlevel")
                            ?: doc.getDouble("light_level")
                            ?: 0.0).toFloat()

                        // Tenta ler 'stepsAtTime'. Se vier nulo, tenta 'steps' ou as outras variações comuns
                        val stepsAtTime = (doc.getLong("stepsAtTime")
                            ?: doc.getLong("stepsattime")
                            ?: doc.getLong("steps_at_time")
                            ?: doc.getLong("steps")
                            ?: 0L).toInt()

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
                        android.util.Log.e("SMART_DIARY", "Erro mapeamento: ${e.message}")
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
}