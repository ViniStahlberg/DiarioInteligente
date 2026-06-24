package com.smartdiary.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.smartdiary.R
import com.smartdiary.databinding.ActivityEntryDetailBinding
import com.smartdiary.viewmodel.DiaryViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.preference.PreferenceManager

class EntryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntryDetailBinding
    private val viewModel: DiaryViewModel by viewModels()

    companion object {
        const val EXTRA_ENTRY_ID = "extra_entry_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        Configuration.getInstance().userAgentValue = packageName

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
            binding.tvDetailMood.text = "${entry.mood}  ${entry.moodLabel()}"

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

            if (entry.hasLocation()) {
                binding.miniMapView.visibility = View.VISIBLE
                binding.tvNoLocation.visibility = View.GONE
                setupMiniMap(entry.latitude, entry.longitude, entry.title)
            } else {
                binding.miniMapView.visibility = View.GONE
                binding.tvNoLocation.visibility = View.VISIBLE
            }
        }
    }

    private fun setupMiniMap(lat: Double, lng: Double, title: String) {
        binding.miniMapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(false)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(lat, lng))
        }

        val marker = Marker(binding.miniMapView).apply {
            position = GeoPoint(lat, lng)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            this.title = title
        }
        binding.miniMapView.overlays.add(marker)
        binding.miniMapView.invalidate()
    }

    private fun formatDate(ts: Long): String =
        SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault()).format(Date(ts))

    override fun onResume() { super.onResume(); binding.miniMapView.onResume() }
    override fun onPause() { super.onPause(); binding.miniMapView.onPause() }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed(); return true
    }
}