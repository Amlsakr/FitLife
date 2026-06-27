package com.aml_sakr.fitlife.feature.session.data.pose

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.aml_sakr.fitlife.feature.session.domain.pose.JointCoordinate
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseData
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseDetector
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseJoint
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onCompletion

/**
 * ML Kit implementation of [PoseDetector].
 * AC 1, 4, 5 compliance:
 * - Uses ML Kit Pose Detection SDK.
 * - Respects architecture boundaries.
 * - Lifecycle-safe resource management via [close].
 */
class MlKitPoseDetector : PoseDetector {

    private val detector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun detectPose(imageProxy: Any): Flow<PoseData> = callbackFlow {
        if (imageProxy !is ImageProxy) {
            close()
            return@callbackFlow
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            detector.process(inputImage)
                .addOnSuccessListener { pose ->
                    trySend(mapToPoseData(pose, imageProxy.imageInfo.timestamp))
                }
                .addOnFailureListener {
                    // Log error or handle failure
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }

        awaitClose {
            // No-op here, detector is closed via explicit close() call from owner
        }
    }.onCompletion {
        // Ensure imageProxy is closed if flow is cancelled
        if (imageProxy is ImageProxy) {
            imageProxy.close()
        }
    }

    override fun close() {
        detector.close()
    }

    private fun mapToPoseData(pose: Pose, timestamp: Long): PoseData {
        val joints = mutableMapOf<PoseJoint, JointCoordinate>()
        
        PoseJoint.entries.forEach { joint ->
            val landmarkType = mapToLandmarkType(joint)
            val landmark = pose.getPoseLandmark(landmarkType)
            if (landmark != null) {
                joints[joint] = JointCoordinate(
                    x = landmark.position.x,
                    y = landmark.position.y,
                    z = landmark.position3D.z,
                    confidence = landmark.inFrameLikelihood
                )
            }
        }

        // Simplistic overall confidence: average of detected landmarks
        val overallConfidence = if (joints.isNotEmpty()) {
            joints.values.map { it.confidence }.average().toFloat()
        } else {
            0f
        }

        return PoseData(
            timestampMillis = timestamp,
            joints = joints,
            overallConfidence = overallConfidence
        )
    }

    private fun mapToLandmarkType(joint: PoseJoint): Int = when (joint) {
        PoseJoint.NOSE -> PoseLandmark.NOSE
        PoseJoint.LEFT_EYE_INNER -> PoseLandmark.LEFT_EYE_INNER
        PoseJoint.LEFT_EYE -> PoseLandmark.LEFT_EYE
        PoseJoint.LEFT_EYE_OUTER -> PoseLandmark.LEFT_EYE_OUTER
        PoseJoint.RIGHT_EYE_INNER -> PoseLandmark.RIGHT_EYE_INNER
        PoseJoint.RIGHT_EYE -> PoseLandmark.RIGHT_EYE
        PoseJoint.RIGHT_EYE_OUTER -> PoseLandmark.RIGHT_EYE_OUTER
        PoseJoint.LEFT_EAR -> PoseLandmark.LEFT_EAR
        PoseJoint.RIGHT_EAR -> PoseLandmark.RIGHT_EAR
        PoseJoint.LEFT_MOUTH -> PoseLandmark.LEFT_MOUTH
        PoseJoint.RIGHT_MOUTH -> PoseLandmark.RIGHT_MOUTH
        PoseJoint.LEFT_SHOULDER -> PoseLandmark.LEFT_SHOULDER
        PoseJoint.RIGHT_SHOULDER -> PoseLandmark.RIGHT_SHOULDER
        PoseJoint.LEFT_ELBOW -> PoseLandmark.LEFT_ELBOW
        PoseJoint.RIGHT_ELBOW -> PoseLandmark.RIGHT_ELBOW
        PoseJoint.LEFT_WRIST -> PoseLandmark.LEFT_WRIST
        PoseJoint.RIGHT_WRIST -> PoseLandmark.RIGHT_WRIST
        PoseJoint.LEFT_PINKY -> PoseLandmark.LEFT_PINKY
        PoseJoint.RIGHT_PINKY -> PoseLandmark.RIGHT_PINKY
        PoseJoint.LEFT_INDEX -> PoseLandmark.LEFT_INDEX
        PoseJoint.RIGHT_INDEX -> PoseLandmark.RIGHT_INDEX
        PoseJoint.LEFT_THUMB -> PoseLandmark.LEFT_THUMB
        PoseJoint.RIGHT_THUMB -> PoseLandmark.RIGHT_THUMB
        PoseJoint.LEFT_HIP -> PoseLandmark.LEFT_HIP
        PoseJoint.RIGHT_HIP -> PoseLandmark.RIGHT_HIP
        PoseJoint.LEFT_KNEE -> PoseLandmark.LEFT_KNEE
        PoseJoint.RIGHT_KNEE -> PoseLandmark.RIGHT_KNEE
        PoseJoint.LEFT_ANKLE -> PoseLandmark.LEFT_ANKLE
        PoseJoint.RIGHT_ANKLE -> PoseLandmark.RIGHT_ANKLE
        PoseJoint.LEFT_HEEL -> PoseLandmark.LEFT_HEEL
        PoseJoint.RIGHT_HEEL -> PoseLandmark.RIGHT_HEEL
        PoseJoint.LEFT_FOOT_INDEX -> PoseLandmark.LEFT_FOOT_INDEX
        PoseJoint.RIGHT_FOOT_INDEX -> PoseLandmark.RIGHT_FOOT_INDEX
    }
}
