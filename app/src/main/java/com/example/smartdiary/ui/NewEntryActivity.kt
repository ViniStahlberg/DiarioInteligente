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
import com.smartdiary.model.DiaryEntryEntity
import com.smartdiary.sensor.LightSensorManager
import com.smartdiary.speech.SpeechHelper
import com.smartdiary.viewmodel.DiaryViewModel

class NewEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewEntryBinding
    private val viewModel: DiaryViewModel by viewModels()

    private lateinit var lightSensorManager: LightSensorManager
    private lateinit var speechHelper: SpeechHelper

    private var currentLightLevel: Float = 0f
    private var capturedImagePath: String = ""

    companion object {
        const val EXTRA_IMAGE_PATH = "extra_image_path"
    }

    // Launcher para resultado da CameraActivity
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            capturedImagePath = result.data?.getStringExtra(EXTRA_IMAGE_PATH) ?: ""
            if (capturedImagePath.isNotEmpty()) {
                binding.tvImageStatus.text = "✓ Foto capturada"
                binding.tvImageStatus.visibility = View.VISIBLE
            }
        }
    }

    // Launcher de permissão de áudio
    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startVoiceRecognition()
        else FeedbackHelper.showSnackbar(binding.root, "Permissão de áudio negada")
    }

    // Launcher de permissão de câmera
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera()
        else FeedbackHelper.showSnackbar(binding.root, "Permissão de câmera negada")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Novo Registro"

        setupLightSensor()
        setupSpeechHelper()
        setupClickListeners()
    }

    private fun setupLightSensor() {
        lightSensorManager = LightSensorManager(this) { lux ->
            currentLightLevel = lux
            binding.tvLightLevel.text = getString(
                com.smartdiary.R.string.txt_light_level, lux
            )
        }
        if (!lightSensorManager.isAvailable) {
            binding.tvLightLevel.text = "Sensor de luz não disponível"
        }
    }

    private fun setupSpeechHelper() {
        speechHelper = SpeechHelper(
            context = this,
            onResult = { text ->
                val current = binding.etDescription.text.toString()
                val updated = if (current.isEmpty()) text else "$current $text"
                binding.etDescription.setText(updated)
                binding.btnVoice.text = "🎤 Gravar Voz"
                binding.btnVoice.isEnabled = true
                FeedbackHelper.showToast(this, "Voz reconhecida!")
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

    private fun setupClickListeners() {
        binding.btnVoice.setOnClickListener {
            checkAudioPermissionAndRecord()
        }

        binding.btnCamera.setOnClickListener {
            checkCameraPermissionAndOpen()
        }

        binding.btnSave.setOnClickListener {
            saveEntry()
        }
    }

    private fun checkAudioPermissionAndRecord() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> startVoiceRecognition()

            else -> audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> openCamera()

            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startVoiceRecognition() {
        if (!speechHelper.isAvailable()) {
            FeedbackHelper.showSnackbar(binding.root, "Reconhecimento de voz não disponível")
            return
        }
        speechHelper.startListening()
    }

    private fun openCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        cameraLauncher.launch(intent)
    }

    private fun saveEntry() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            FeedbackHelper.showSnackbar(
                binding.root,
                getString(com.smartdiary.R.string.msg_empty_fields)
            )
            return
        }

        val entry = DiaryEntryEntity(
            title = title,
            description = description,
            imagePath = capturedImagePath,
            lightLevel = currentLightLevel,
            createdAt = System.currentTimeMillis()
        )

        viewModel.insert(entry)
        FeedbackHelper.vibrate(this)
        FeedbackHelper.playSuccessSound(this)
        FeedbackHelper.showSnackbar(
            binding.root,
            getString(com.smartdiary.R.string.msg_save_success)
        )

        // Pequeno delay para o Snackbar ser visível antes de fechar
        binding.root.postDelayed({ finish() }, 1200)
    }

    override fun onResume() {
        super.onResume()
        lightSensorManager.start()
    }

    override fun onPause() {
        super.onPause()
        lightSensorManager.stop()
        speechHelper.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechHelper.destroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}