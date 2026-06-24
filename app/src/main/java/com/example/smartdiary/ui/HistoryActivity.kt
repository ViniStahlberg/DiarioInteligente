package com.smartdiary.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.smartdiary.databinding.ActivityHistoryBinding
import com.smartdiary.helper.FeedbackHelper
import com.smartdiary.ui.adapter.DiaryEntryAdapter
import com.smartdiary.viewmodel.DiaryViewModel

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val viewModel: DiaryViewModel by viewModels()
    private lateinit var adapter: DiaryEntryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Meu Diário"

        adapter = DiaryEntryAdapter(
            onItemClick = { entry ->
                startActivity(
                    Intent(this, EntryDetailActivity::class.java).apply {
                        putExtra(EntryDetailActivity.EXTRA_ENTRY_ID, entry.id)
                    }
                )
            },
            onItemLongClick = { entry ->
                MaterialAlertDialogBuilder(this)
                    .setTitle("Excluir registro")
                    .setMessage("Deseja excluir \"${entry.title}\"?\nEsta ação não pode ser desfeita.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Excluir") { _, _ ->
                        viewModel.deleteEntry(entry.id)
                        FeedbackHelper.showToast(this, "Registro excluído")
                    }
                    .show()
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.entries.observe(this) { list ->
            adapter.submitList(list)
            binding.recyclerView.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            binding.layoutEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed(); return true
    }
}