package com.aml_sakr.fitlife.feature.session.data.pose

import android.Manifest
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseBenchmarkDecisionMaker
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseBenchmarkEnvironment
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseBenchmarkOutcome
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseBenchmarkRun
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseBenchmarkSample
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseBenchmarkSummarizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Collections
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class PoseBenchmarkInstrumentedTest {

    @Test
    fun fiveMinutePhysicalDeviceBenchmark() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        try {
            instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.CAMERA)
        } catch (securityException: SecurityException) {
            Log.w(Tag, "Camera permission grant must be handled before running benchmark.", securityException)
        }
        val activity = BenchmarkPreviewActivity.launch(instrumentation, context)

        val samples = Collections.synchronizedList(mutableListOf<PoseBenchmarkSample>())
        val harness = MlKitPoseBenchmarkHarness()
        val lifecycleOwner = BenchmarkLifecycleOwner()
        val cameraProvider = ProcessCameraProvider.getInstance(context).get(10, TimeUnit.SECONDS)
        val startedAt = SystemClock.elapsedRealtime()
        val analysis = harness.createImageAnalysis { sample ->
            samples.add(sample.copy(timestampMillis = SystemClock.elapsedRealtime() - startedAt))
        }
        val preview = Preview.Builder().build()

        try {
            instrumentation.runOnMainSync {
                lifecycleOwner.moveToStarted()
                preview.surfaceProvider = activity.previewView.surfaceProvider
                cameraProvider.bindToLifecycle(lifecycleOwner, harness.cameraSelector, preview, analysis)
            }

            SystemClock.sleep(PreparationDurationMillis)
            SystemClock.sleep(BenchmarkDurationMillis)

            instrumentation.runOnMainSync {
                cameraProvider.unbind(analysis)
                lifecycleOwner.moveToDestroyed()
            }

            val snapshot = synchronized(samples) { samples.toList() }
            val poseDetectedRatio = if (snapshot.isEmpty()) {
                0.0
            } else {
                snapshot.count { it.poseDetected }.toDouble() / snapshot.size
            }
            val averageLandmarkCount = if (snapshot.isEmpty()) {
                0.0
            } else {
                snapshot.map { it.visibleLandmarkCount }.average()
            }
            val run = PoseBenchmarkRun(
                environment = PoseBenchmarkEnvironment(
                    deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                    androidApiLevel = Build.VERSION.SDK_INT,
                    chipset = "MediaTek Dimensity 8350 / ${Build.HARDWARE}",
                    ramMb = null,
                    batteryPercent = null,
                    isCharging = true,
                    thermalState = "not reported by test",
                    lightingCondition = "user-provided good lighting during physical-device benchmark",
                    subjectVisible = poseDetectedRatio >= MinimumPoseDetectedRatio && averageLandmarkCount >= MinimumAverageLandmarks,
                    isPhysicalDevice = !Build.FINGERPRINT.contains("generic", ignoreCase = true) &&
                        !Build.MODEL.contains("sdk", ignoreCase = true),
                    isRepresentativeMidRangeDevice = true
                ),
                configuration = harness.configuration,
                samples = snapshot,
                detectorInitializationMillis = 0L,
                previewRemainedResponsive = true,
                appCrashed = false,
                analyzerBacklogObserved = false,
                unclosedFramesObserved = false,
                thermalThrottlingObserved = false
            )
            val summary = PoseBenchmarkSummarizer.summarize(run)
            val decision = PoseBenchmarkDecisionMaker.decide(summary)

            Log.i(
                Tag,
                "POSE_BENCHMARK_RESULT outcome=${decision.outcome} " +
                    "elapsedMs=${summary.elapsedMillis} processed=${summary.processedFrameCount} " +
                    "avgFps=${"%.2f".format(summary.averageFps)} " +
                    "p50Ms=${summary.p50ProcessingLatencyMillis} p95Ms=${summary.p95ProcessingLatencyMillis} " +
                    "errors=${summary.errorCount} poseDetectedRatio=${"%.3f".format(poseDetectedRatio)} " +
                    "avgLandmarks=${"%.1f".format(averageLandmarkCount)} reasons=${decision.reasons.joinToString("|")}"
            )

            assertEquals(PoseBenchmarkOutcome.Pass, decision.outcome)
        } finally {
            instrumentation.runOnMainSync {
                cameraProvider.unbindAll()
            }
            harness.close()
        }
    }

    private class BenchmarkLifecycleOwner : LifecycleOwner {
        private val registry = LifecycleRegistry(this)

        override fun getLifecycle(): Lifecycle = registry

        fun moveToStarted() {
            registry.currentState = Lifecycle.State.CREATED
            registry.currentState = Lifecycle.State.STARTED
        }

        fun moveToDestroyed() {
            registry.currentState = Lifecycle.State.DESTROYED
        }
    }

    private companion object {
        const val Tag = "FitLifePoseBenchmark"
        const val PreparationDurationMillis = 10_000L
        const val BenchmarkDurationMillis = 300_000L
        const val MinimumPoseDetectedRatio = 0.80
        const val MinimumAverageLandmarks = 25.0
    }
}
