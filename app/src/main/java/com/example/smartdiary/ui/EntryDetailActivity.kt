package com.smartdiary.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.smartdiary.R
import com.smartdiary.databinding.ActivityEntryDetailBinding
import com.smartdiary.viewmodel.DiaryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EntryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntryDetailBinding
    private val viewModel: DiaryViewModel by viewModels()

    companion object {

        const val EXTRA_ENTRY_ID = "ENTRY_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEntryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalhes"

        val id = intent.getStringExtra(EXTRA_ENTRY_ID) ?: run { finish(); return }
        loadEntry(id)
    }

    private fun loadEntry(id: String) {
        binding.progressDetail.visibility = View.VISIBLE
        viewModel.getEntryById(id) { entry ->
            binding.progressDetail.visibility = View.GONE
            if (entry == null) { finish(); return@getEntryById }

            binding.tvDetailTitle.text = entry.title
            binding.tvDetailDescription.text = entry.description
            binding.tvDetailDate.text = formatDate(entry.createdAt)
            binding.tvDetailLight.text = "☀ %.1f lx".format(entry.lightLevel)
            binding.tvDetailMood.text = entry.moodLabel()

            if (entry.imageUrl.isNotEmpty()) {
                binding.ivDetailPhoto.visibility = View.VISIBLE
                binding.tvNoImage.visibility = View.GONE
                Glide.with(this)
                    .load(entry.imageUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .centerCrop()
                    .into(binding.ivDetailPhoto)
            } else {
                binding.ivDetailPhoto.visibility = View.GONE
                binding.tvNoImage.visibility = View.VISIBLE
            }


            binding.tvNoLocation.visibility = View.VISIBLE
            binding.tvNoLocation.text = entry.getAmbientContextDescription()
        }
    }

    private fun formatDate(ts: Long): String =
        SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault()).format(Date(ts))

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}