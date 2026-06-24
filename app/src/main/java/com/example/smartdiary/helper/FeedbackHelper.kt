package com.smartdiary.helper

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

object FeedbackHelper {

    fun showSnackbar(view: View, message: String) =
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()

    fun showToast(context: Context, message: String) =
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    fun vibrate(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.vibrate(
                VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(200)
            }
        }
    }

    fun playSuccessSound(context: Context) {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(context, uri).play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}