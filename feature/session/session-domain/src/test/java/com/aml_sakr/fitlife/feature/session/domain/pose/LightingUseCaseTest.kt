package com.aml_sakr.fitlife.feature.session.domain.pose

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.delay

class LightingUseCaseTest {

    private val lightSensorFlow = MutableSharedFlow<Float>()
    private val poseDataFlow = MutableSharedFlow<PoseData>()

    private val lightSensorProvider = object : ILightSensorProvider {
        override fun getAmbientLightLux(): Flow<Float> = lightSensorFlow
    }

    private val useCase = LightingUseCase(lightSensorProvider)

    @Test
    fun `should switch to AudioOnly after 2 seconds of low lux`() = runTest {
        useCase(poseDataFlow).test {
            // Initial state is Visual (Wait, should we have an initial state?)
            // If the flows haven't emitted yet, it might wait.

            // High lux and high confidence
            lightSensorFlow.emit(50f)
            poseDataFlow.emit(createPoseData(0.9f))
            assertEquals(LightingStatus.Visual, awaitItem())

            // Low lux starts
            lightSensorFlow.emit(5f)
            
            // Advance time by 1.9s - should still be Visual
            delay(1900)
            // No new emission yet

            // Advance time to 2s
            delay(100)
            assertEquals(LightingStatus.AudioOnly, awaitItem())
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should switch to AudioOnly after 2 seconds of low confidence`() = runTest {
        useCase(poseDataFlow).test {
            lightSensorFlow.emit(50f)
            poseDataFlow.emit(createPoseData(0.9f))
            assertEquals(LightingStatus.Visual, awaitItem())

            // Low confidence starts
            poseDataFlow.emit(createPoseData(0.5f))
            
            delay(1900)
            // Still Visual

            delay(100)
            assertEquals(LightingStatus.AudioOnly, awaitItem())
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should revert to Visual after 3 seconds of stable good conditions`() = runTest {
        useCase(poseDataFlow).test {
            // Start in AudioOnly mode
            lightSensorFlow.emit(5f)
            poseDataFlow.emit(createPoseData(0.5f))
            delay(2000)
            assertEquals(LightingStatus.AudioOnly, awaitItem())

            // Conditions become good
            lightSensorFlow.emit(50f)
            poseDataFlow.emit(createPoseData(0.9f))
            
            delay(2900)
            // Still AudioOnly

            delay(100)
            assertEquals(LightingStatus.Visual, awaitItem())
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createPoseData(confidence: Float) = PoseData(
        timestampMillis = System.currentTimeMillis(),
        joints = emptyMap(),
        overallConfidence = confidence
    )
}
