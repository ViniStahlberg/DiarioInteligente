package com.smartdiary.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.smartdiary.databinding.ActivityNewEntryBinding
import com.smartdiary.helper.FeedbackHelper
import com.smartdiary.helper.LocationHelper
import com.smartdiary.model.DiaryEntry
import com.smartdiary.sensor.LightSensorManager
import com.smartdiary.speech.SpeechHelper
import com.smartdiary.viewmodel.DiaryViewModel

class NewEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewEntryBinding
    private val viewModel: DiaryViewModel by viewModels()
    private lateinit var lightSensor: LightSensorManager
    private lateinit var speechHelper: SpeechHelper
    private lateinit var locationHelper: LocationHelper

    private var currentLux: Float = 0f
    private var capturedPhotoUri: Uri? = null
    private var selectedMood: String = "😐"
    private var capturedLatitude: Double = 0.0
    private var capturedLongitude: Double = 0.0

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uriStr = result.data?.getStringExtra(EXTRA_IMAGE_URI)
            if (!uriStr.isNullOrEmpty()) {
                capturedPhotoUri = Uri.parse(uriStr)
                binding.tvPhotoStatus.text = "✓ Foto capturada"
                binding.tvPhotoStatus.visibility = View.VISIBLE
            }
        }
    }

    private val audioPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { if (it) startSpeech() else FeedbackHelper.showSnackbar(binding.root, "Permissão de microfone negada") }

    private val cameraPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { if (it) openCamera() else FeedbackHelper.showSnackbar(binding.root, "Permissão de câmera negada") }

    private val locationPerm = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) captureLocation()
        else FeedbackHelper.showSnackbar(binding.root, "Localização não capturada")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Novo Momento"

        locationHelper = LocationHelper(this)

        setupSensor()
        setupSpeech()
        setupMoodSelector()
        setupListeners()
        observeViewModel()
        requestLocationAuto()
    }

    private fun setupSensor() {
        lightSensor = LightSensorManager(this) { lux ->
            currentLux = lux
            binding.tvLux.text = "☀ %.1f lx  ${getLuxDesc(lux)}".format(lux)
        }
    }

    private fun getLuxDesc(lux: Float) = when {
        lux < 10 -> "🌑 Muito escuro"
        lux < 100 -> "🌒 Pouca luz"
        lux < 500 -> "🌤 Iluminação interna"
        lux < 1000 -> "🌥 Nublado"
        else -> "☀ Luz solar"
    }

    private fun setupSpeech() {
        speechHelper = SpeechHelper(
            context = this,
            onResult = { text ->
                val current = binding.etDescription.text.toString()
                binding.etDescription.setText(if (current.isEmpty()) text else "$current $text")
                binding.btnVoice.text = "🎤 Gravar Voz"
                binding.btnVoice.isEnabled = true
                FeedbackHelper.vibrate(this)
            },
            onError = { error ->
                binding.btnVoice.text = "🎤 Gravar Voz"
                binding.btnVoice.isEnabled = true
                FeedbackHelper.showSnackbar(binding.root, error)
            },
            onReady = {
                binding.btnVoice.text = "🔴 Ouvindo..."
                binding.btnVoice.isEnabled = false
            },
            onEnd = {
                binding.btnVoice.text = "🎤 Gravar Voz"
                binding.btnVoice.isEnabled = true
            }
        )
    }

    private fun setupMoodSelector() {
        val moodButtons = listOf(
            binding.btnMoodHappy,
            binding.btnMoodSad,
            binding.btnMoodStressed,
            binding.btnMoodTired,
            binding.btnMoodNeutral
        )
        val moods = listOf("😄", "😔", "😤", "😴", "😐")

        // Estado inicial — neutro selecionado
        updateMoodSelection(binding.btnMoodNeutral, moodButtons)

        moodButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectedMood = moods[index]
                updateMoodSelection(button, moodButtons)
            }
        }
    }

    private fun updateMoodSelection(
        selected: com.google.android.material.button.MaterialButton,
        all: List<com.google.android.material.button.MaterialButton>
    ) {
        all.forEach { btn ->
            btn.alpha = if (btn == selected) 1.0f else 0.4f
            btn.strokeWidth = if (btn == selected) 4 else 0
        }
    }

    private fun setupListeners() {
        binding.btnVoice.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
            ) startSpeech() else audioPerm.launch(Manifest.permission.RECORD_AUDIO)
        }

        binding.btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) openCamera() else cameraPerm.launch(Manifest.permission.CAMERA)
        }

        binding.btnSave.setOnClickListener { saveEntry() }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.progressSave.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !loading
        }
        viewModel.saveResult.observe(this) { success ->
            if (success == true) {
                FeedbackHelper.vibrate(this)
                FeedbackHelper.playSuccessSound(this)
                FeedbackHelper.showSnackbar(binding.root, "Momento salvo! ✓")
                viewModel.clearSaveResult()
                binding.root.postDelayed({ finish() }, 1200)
            } else if (success == false) {
                FeedbackHelper.showSnackbar(binding.root, "Erro ao salvar. Tente novamente.")
                viewModel.clearSaveResult()
            }
        }
        viewModel.error.observe(this) { msg ->
            msg?.let { FeedbackHelper.showSnackbar(binding.root, it); viewModel.clearError() }
        }
    }

    private fun requestLocationAuto() {
        val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) captureLocation()
        else locationPerm.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    private fun captureLocation() {
        binding.tvLocationStatus.text = "📍 Obtendo localização..."
        locationHelper.getCurrentLocation(
            onSuccess = { lat, lng ->
                capturedLatitude = lat
                capturedLongitude = lng
                binding.tvLocationStatus.text = "📍 Localização capturada ✓"
            },
            onError = {
                binding.tvLocationStatus.text = "📍 Localização indisponível"
            }
        )
    }

    private fun startSpeech() {
        if (!speechHelper.isAvailable()) {
            FeedbackHelper.showSnackbar(binding.root, "Reconhecimento de voz indisponível")
            return
        }
        speechHelper.startListening()
    }

    private fun openCamera() {
        cameraLauncher.launch(Intent(this, CameraActivity::class.java))
    }

    private fun saveEntry() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty()) { binding.tilTitle.error = "Informe o título"; return }
        else binding.tilTitle.error = null
        if (description.isEmpty()) { binding.tilDescription.error = "Adicione uma descrição"; return }
        else binding.tilDescription.error = null

        val entry = DiaryEntry(
            title = title,
            description = description,
            lightLevel = currentLux,
            mood = selectedMood,
            latitude = capturedLatitude,
            longitude = capturedLongitude,
            createdAt = System.currentTimeMillis()
        )
        viewModel.saveEntryWithPhoto(entry, capturedPhotoUri)
    }

    override fun onResume() { super.onResume(); lightSensor.start() }
    override fun onPause() { super.onPause(); lightSensor.stop(); speechHelper.stopListening() }
    override fun onDestroy() { super.onDestroy(); speechHelper.destroy() }
    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}