package com.aml_sakr.fitlife.feature.session.data.pose

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseBenchmarkConfiguration
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseBenchmarkSample
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MlKitPoseBenchmarkHarness(
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
) : AutoCloseable {

    val configuration = PoseBenchmarkConfiguration(
        cameraLens = "back",
        analysisResolution = "640x480",
        outputFormat = "YUV_420_888",
        mlKitSdk = "pose-detection:18.0.0-beta5",
        cameraXVersion = "1.6.1"
    )

    val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private val poseDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
    )

    @Suppress("DEPRECATION")
    fun createImageAnalysis(sampleSink: (PoseBenchmarkSample) -> Unit): ImageAnalysis {
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(640, 480))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(
            executor,
            MlKitPoseBenchmarkAnalyzer(
                poseDetector = poseDetector,
                sampleSink = sampleSink
            )
        )
        return imageAnalysis
    }

    override fun close() {
        poseDetector.close()
        executor.shutdown()
    }
}
