package com.aml_sakr.fitlife.feature.session.domain.pose

import kotlinx.coroutines.flow.Flow

/**
 * Interface for the pose detector.
 * AC 5 compliance: Defined in session-domain to maintain architecture boundaries.
 */
interface PoseDetector {
    /**
     * Starts the pose detection and returns a stream of [PoseData].
     */
    fun detectPose(imageProxy: Any): Flow<PoseData>

    /**
     * Closes the detector and releases resources.
     */
    fun close()
}
