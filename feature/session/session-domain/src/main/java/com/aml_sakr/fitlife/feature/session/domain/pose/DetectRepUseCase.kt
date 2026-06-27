package com.aml_sakr.fitlife.feature.session.domain.pose

import javax.inject.Inject

/**
 * A temporary mock implementation of rep detection.
 * In a real scenario, this would analyze the [PoseData] stream for peak movements.
 * For now, it considers a rep completed every [framesPerRep] frames.
 */
class DetectRepUseCase @Inject constructor() {
    private var frameCount = 0
    private val framesPerRep = 45 // Approximately 3 seconds at 15fps

    /**
     * Processes a new pose and returns the peak pose if a rep was completed.
     */
    fun processPose(poseData: PoseData): PoseData? {
        frameCount++
        return if (frameCount >= framesPerRep) {
            frameCount = 0
            poseData // Return current pose as the 'peak' for the mock
        } else {
            null
        }
    }

    /**
     * Resets the internal counter for a new exercise.
     */
    fun reset() {
        frameCount = 0
    }
}
