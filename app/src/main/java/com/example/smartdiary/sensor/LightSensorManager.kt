package com.smartdiary.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class LightSensorManager(
    context: Context,
    private val onLightChanged: (Float) -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    var lastValue: Float = 0f
        private set

    val isAvailable: Boolean get() = lightSensor != null

    fun start() {
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() = sensorManager.unregisterListener(this)

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            lastValue = event.values[0]
            onLightChanged(lastValue)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
}