package com.aml_sakr.fitlife.feature.session.domain.pose

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for analyzing a camera frame and emitting pose data.
 * AC 3, 5 compliance:
 * - Emits a [PoseData] stream.
 * - Does not leak implementation details (e.g., ML Kit) into the domain.
 */
class AnalyzePoseUseCase @Inject constructor(
    private val poseDetector: PoseDetector
) : AutoCloseable {
    /**
     * Analyzes the given image frame and returns a stream of detected pose data.
     * The [imageFrame] is expected to be an [ImageProxy] or equivalent implementation-specific object.
     */
    operator fun invoke(imageFrame: Any): Flow<PoseData> {
        return poseDetector.detectPose(imageFrame)
    }

    override fun close() {
        poseDetector.close()
    }
}
