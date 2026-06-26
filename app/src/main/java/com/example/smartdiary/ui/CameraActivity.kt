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

        cameraHelper = CameraHelper(
            context = this,
            lifecycleOwner = this,
            previewView = binding.previewView,
            onPhotoSaved = { uri -> returnPhoto(uri) },
            onError = { msg ->
                binding.btnCapture.isEnabled = true
                FeedbackHelper.showToast(this, msg)
            }
        )
        cameraHelper.startCamera()

        binding.btnCapture.setOnClickListener {
            binding.btnCapture.isEnabled = false
            cameraHelper.takePhoto()
        }

        binding.btnCancelCamera.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun returnPhoto(uri: Uri) {
        FeedbackHelper.vibrate(this)
        val result = Intent().apply {
            // passa a URI diretamente (não como String)
            data = uri
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        setResult(RESULT_OK, result)
        finish()
    }
}