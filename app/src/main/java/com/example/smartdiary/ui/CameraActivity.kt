package com.smartdiary.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.smartdiary.camera.CameraHelper
import com.smartdiary.databinding.ActivityCameraBinding
import com.smartdiary.helper.FeedbackHelper

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var cameraHelper: CameraHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        setupCamera()

        binding.btnCapture.setOnClickListener {
            binding.btnCapture.isEnabled = false
            cameraHelper.takePhoto()
        }

        binding.btnCancelCamera.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun setupCamera() {
        cameraHelper = CameraHelper(
            context = this,
            lifecycleOwner = this,
            previewView = binding.previewView,
            onPhotoSaved = { uri ->
                onPhotoSaved(uri)
            },
            onError = { error ->
                binding.btnCapture.isEnabled = true
                FeedbackHelper.showToast(this, error)
            }
        )
        cameraHelper.startCamera()
    }

    private fun onPhotoSaved(uri: Uri) {
        FeedbackHelper.vibrate(this)
        FeedbackHelper.playSuccessSound(this)
        FeedbackHelper.showToast(this, "Foto salva!")

        val resultIntent = Intent().apply {
            putExtra(NewEntryActivity.EXTRA_IMAGE_PATH, uri.path)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}