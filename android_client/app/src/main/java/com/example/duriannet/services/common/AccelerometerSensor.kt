package com.example.duriannet.services.common

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

class AccelerometerSensor(
    context: Context,
) : SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private var movementThreshold = 0.3f // Adjust threshold based on sensitivity needs

    private var minShakeInterval = 1000L // Minimum time interval between shakes in milliseconds

    private var lastShakeTime = 0L

    private var currMagnitude = 0f
    private var lastMagnitude = 0f
    private var magnitude = 0f

    fun start() {
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun setThreshold(threshold: Float) {
        movementThreshold = threshold
    }

    fun setMinShakeInterval(interval: Long) {
        minShakeInterval = interval
    }

    var onShakeDetected: (() -> Unit)? = null

    override fun onSensorChanged(event: SensorEvent) {

        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastShakeTime < minShakeInterval) return

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            lastMagnitude = currMagnitude
            currMagnitude = calculateMagnitude(x, y, z)

            val delta = currMagnitude - lastMagnitude

            magnitude = magnitude * 0.9f + delta

            if (magnitude > movementThreshold) {
                Log.e("Accelerometer", "magnitude : $magnitude")

                lastShakeTime = currentTime
                onShakeDetected?.invoke()
            }
        }
    }

    private fun calculateMagnitude(x: Float, y: Float, z: Float): Float {
        return sqrt(x * x + y * y + z * z)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
}