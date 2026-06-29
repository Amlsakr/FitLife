package com.aml_sakr.fitlife.feature.session.data.pose

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.aml_sakr.fitlife.feature.session.domain.pose.ILightSensorProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android implementation of [ILightSensorProvider] using [Sensor.TYPE_LIGHT].
 * Falls back to a safe high-light value if the sensor is missing.
 */
@Singleton
class AndroidLightSensorProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : ILightSensorProvider {

    private val sensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val lightSensor by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    override fun getAmbientLightLux(): Flow<Float> = callbackFlow {
        if (lightSensor == null) {
            // Default to high lux to avoid accidental audio-only mode on sensorless devices
            trySend(GOOD_LIGHT_THRESHOLD)
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                    val lux = event.values.getOrNull(0) ?: GOOD_LIGHT_THRESHOLD
                    trySend(lux)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    companion object {
        private const val GOOD_LIGHT_THRESHOLD = 50f
    }
}
