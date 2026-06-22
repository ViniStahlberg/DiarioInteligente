package com.smartdiary.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.smartdiary.databinding.ActivityEntryDetailBinding
import com.smartdiary.viewmodel.DiaryViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EntryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntryDetailBinding
    private val viewModel: DiaryViewModel by viewModels()

    companion object {
        const val EXTRA_ENTRY_ID = "extra_entry_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalhes"

        val entryId = intent.getLongExtra(EXTRA_ENTRY_ID, -1L)
        if (entryId == -1L) {
            finish()
            return
        }

        loadEntry(entryId)
    }

    private fun loadEntry(id: Long) {
        viewModel.getEntryById(id) { entry ->
            entry?.let {
                binding.tvDetailTitle.text = it.title
                binding.tvDetailDescription.text = it.description
                binding.tvDetailDate.text = formatDate(it.createdAt)
                binding.tvDetailLight.text = "☀ Luminosidade: %.2f lx".format(it.lightLevel)

                if (it.imagePath.isNotEmpty()) {
                    val file = File(it.imagePath)
                    if (file.exists()) {
                        binding.ivDetailPhoto.visibility = View.VISIBLE
                        Glide.with(this)
                            .load(file)
                            .centerCrop()
                            .into(binding.ivDetailPhoto)
                    } else {
                        binding.ivDetailPhoto.visibility = View.GONE
                        binding.tvNoImage.visibility = View.VISIBLE
                    }
                } else {
                    binding.ivDetailPhoto.visibility = View.GONE
                    binding.tvNoImage.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}