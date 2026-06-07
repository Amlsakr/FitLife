package com.aml_sakr.fitlife.feature.session.data.pose

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseBenchmarkSample
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetector
import java.util.concurrent.atomic.AtomicBoolean

internal class MlKitPoseBenchmarkAnalyzer(
    private val poseDetector: PoseDetector,
    private val sampleSink: (PoseBenchmarkSample) -> Unit,
    private val timeSourceMillis: () -> Long = System::currentTimeMillis
) : ImageAnalysis.Analyzer {

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            sampleSink(
                PoseBenchmarkSample(
                    timestampMillis = timeSourceMillis(),
                    processingDurationMillis = 0L,
                    poseDetected = false,
                    visibleLandmarkCount = 0,
                    errorMessage = "ImageProxy contained no media image."
                )
            )
            imageProxy.close()
            return
        }

        val startedAt = timeSourceMillis()
        val closed = AtomicBoolean(false)
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        poseDetector.process(inputImage)
            .addOnSuccessListener { pose ->
                sampleSink(
                    PoseBenchmarkSample(
                        timestampMillis = timeSourceMillis(),
                        processingDurationMillis = timeSourceMillis() - startedAt,
                        poseDetected = pose.allPoseLandmarks.isNotEmpty(),
                        visibleLandmarkCount = pose.allPoseLandmarks.size,
                        errorMessage = null
                    )
                )
            }
            .addOnFailureListener { throwable ->
                sampleSink(
                    PoseBenchmarkSample(
                        timestampMillis = timeSourceMillis(),
                        processingDurationMillis = timeSourceMillis() - startedAt,
                        poseDetected = false,
                        visibleLandmarkCount = 0,
                        errorMessage = throwable.message ?: throwable::class.java.simpleName
                    )
                )
            }
            .addOnCompleteListener {
                if (closed.compareAndSet(false, true)) {
                    imageProxy.close()
                }
            }
    }
}
