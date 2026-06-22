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
        supportActionBar?.title = "Histórico"

        setupRecyclerView()
        observeEntries()
    }

    private fun setupRecyclerView() {
        adapter = DiaryEntryAdapter(
            onItemClick = { entry ->
                val intent = Intent(this, EntryDetailActivity::class.java).apply {
                    putExtra(EntryDetailActivity.EXTRA_ENTRY_ID, entry.id)
                }
                startActivity(intent)
            },
            onItemLongClick = { entry ->
                showDeleteDialog(entry.id, entry.title)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun observeEntries() {
        viewModel.allEntries.observe(this) { entries ->
            adapter.submitList(entries)
            if (entries.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.GONE
            }
        }
    }

    private fun showDeleteDialog(id: Long, title: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Excluir registro")
            .setMessage("Deseja excluir \"$title\"?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.getEntryById(id) { entry ->
                    entry?.let {
                        viewModel.delete(it)
                        FeedbackHelper.showToast(this, "Registro excluído")
                    }
                }
            }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}