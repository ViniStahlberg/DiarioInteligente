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
import com.smartdiary.model.DiaryEntry
import com.smartdiary.sensor.LightSensorManager
import com.smartdiary.speech.SpeechHelper
import com.smartdiary.viewmodel.DiaryViewModel

class NewEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewEntryBinding
    private val viewModel: DiaryViewModel by viewModels()
    private lateinit var lightSensor: LightSensorManager
    private lateinit var speechHelper: SpeechHelper

    private var currentLux: Float = 0f
    private var capturedPhotoUri: Uri? = null
    private var selectedMood: String = "" // Começa vazio, livre do emoji neutro fixo
    private var simulatedSteps: Int = 0   // Substitui a geolocalização antiga para o pitch

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Novo Momento"

        setupSensor()
        setupSpeech()
        setupMoodSelector()
        setupListeners()
        observeViewModel()

        // Simulação charmosa para fins de teste rápido e apresentação do Pitch
        simulatedSteps = (1000..8000).random()
        if (binding.root.findViewById<View>(com.smartdiary.R.id.tvLocationStatus) != null) {
            binding.tvLocationStatus.text = "👣 Contexto de Movimento Ativo ($simulatedSteps passos)"
        }
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
            binding.btnMoodTired
        )
        val moods = listOf("😄", "😔", "😤", "😴")

        // Deixa a seleção limpa inicialmente, respeitando o seu desejo de tirar o emoji fixo
        moodButtons.forEach { btn -> btn.alpha = 0.5f }

        moodButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectedMood = moods[index]
                moodButtons.forEach { btn ->
                    btn.alpha = if (btn == button) 1.0f else 0.4f
                    btn.strokeWidth = if (btn == button) 4 else 0
                }
            }
        }

        // Esconde o botão antigo do emoji neutro caso ele ainda esteja inflado no XML
        if (binding.root.findViewById<View>(com.smartdiary.R.id.btnMoodNeutral) != null) {
            binding.btnMoodNeutral.visibility = View.GONE
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

        // CORREÇÃO: Pega o ID real do usuário logado no Firebase para vincular à nota
        val currentFirebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val userIdLogado = currentFirebaseUser?.uid ?: ""

        // Criando o objeto com o userId correto para aparecer na Linha do Tempo
        val entry = DiaryEntry(
            id = "",
            userId = userIdLogado, // Vinculado ao seu usuário
            title = title,
            description = description,
            imageUrl = "",
            lightLevel = currentLux,
            stepsAtTime = simulatedSteps,
            mood = selectedMood,
            createdAt = System.currentTimeMillis()
        )
        viewModel.saveEntryWithPhoto(entry, capturedPhotoUri)
    }

    override fun onResume() { super.onResume(); lightSensor.start() }
    override fun onPause() { super.onPause(); lightSensor.stop(); speechHelper.stopListening() }
    override fun onDestroy() { super.onDestroy(); speechHelper.destroy() }
    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}