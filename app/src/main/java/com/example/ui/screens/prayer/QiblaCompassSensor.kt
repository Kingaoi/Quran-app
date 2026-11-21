package com.example.ui.screens.prayer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Immutable
internal data class QiblaSensorState(
    val azimuthDeg: Float = 0f,
    val hasSensor: Boolean = true,
    val needsCalibration: Boolean = false,
    val pitchDeg: Float = 0f,
    val rollDeg: Float = 0f
)

@Composable
internal fun rememberQiblaSensorState(): QiblaSensorState {
    val context = LocalContext.current
    var state by remember { mutableStateOf(QiblaSensorState()) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (sensorManager == null || (rotationSensor == null && (accelSensor == null || magSensor == null))) {
            state = QiblaSensorState(hasSensor = false)
            onDispose { }
        } else {
            val rotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            val gravity = FloatArray(3)
            val geomagnetic = FloatArray(3)
            var hasGravity = false
            var hasMag = false

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val computed = when (event.sensor.type) {
                        Sensor.TYPE_ROTATION_VECTOR -> {
                            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                            true
                        }
                        Sensor.TYPE_ACCELEROMETER -> {
                            System.arraycopy(event.values, 0, gravity, 0, 3)
                            hasGravity = true
                            hasMag && SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                            hasMag = true
                            hasGravity && SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
                        }
                        else -> false
                    }
                    if (!computed) return

                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val rawAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    val normalizedAzimuth = ((rawAzimuth % 360f) + 360f) % 360f
                    val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                    val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()

                    state = state.copy(
                        hasSensor = true,
                        azimuthDeg = smoothCompassAngle(state.azimuthDeg, normalizedAzimuth),
                        pitchDeg = state.pitchDeg * 0.7f + pitch * 0.3f,
                        rollDeg = state.rollDeg * 0.7f + roll * 0.3f
                    )
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    val low = accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE ||
                        accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW
                    state = state.copy(needsCalibration = low)
                }
            }

            if (rotationSensor != null) {
                sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            } else {
                sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
                sensorManager.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
            }
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }
    return state
}

private fun smoothCompassAngle(current: Float, target: Float): Float {
    val delta = ((target - current + 540f) % 360f) - 180f
    return ((current + delta * 0.25f) + 360f) % 360f
}
