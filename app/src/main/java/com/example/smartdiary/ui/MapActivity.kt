package com.smartdiary.ui

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.smartdiary.databinding.ActivityMapBinding
import com.smartdiary.helper.FeedbackHelper
import com.smartdiary.model.DiaryEntry
import com.smartdiary.viewmodel.DiaryViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding
    private val viewModel: DiaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuração obrigatória do OSMDroid
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        Configuration.getInstance().userAgentValue = packageName

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mapa de Memórias"

        setupMap()
        observeEntries()
    }

    private fun setupMap() {
        binding.mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(12.0)
            // Centro inicial no Brasil
            controller.setCenter(GeoPoint(-15.7942, -47.8822))
        }
    }

    private fun observeEntries() {
        viewModel.entries.observe(this) { entries ->
            val withLocation = entries.filter { it.hasLocation() }

            if (withLocation.isEmpty()) {
                binding.tvMapEmpty.visibility = View.VISIBLE
                return@observe
            }

            binding.tvMapEmpty.visibility = View.GONE
            addMarkersToMap(withLocation)

            // Centraliza no primeiro marcador
            val first = withLocation.first()
            binding.mapView.controller.animateTo(
                GeoPoint(first.latitude, first.longitude)
            )
            binding.mapView.controller.setZoom(14.0)
        }
    }

    private fun addMarkersToMap(entries: List<DiaryEntry>) {
        binding.mapView.overlays.clear()

        entries.forEach { entry ->
            val marker = Marker(binding.mapView).apply {
                position = GeoPoint(entry.latitude, entry.longitude)
                title = "${entry.mood} ${entry.title}"
                snippet = entry.description.take(60) + if (entry.description.length > 60) "..." else ""
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }

            marker.setOnMarkerClickListener { m, _ ->
                InfoWindow.closeAllInfoWindowsOn(binding.mapView)
                m.showInfoWindow()
                true
            }

            binding.mapView.overlays.add(marker)
        }

        binding.mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed(); return true
    }
}