package com.aml_sakr.fitlife.feature.session.domain.pose

import kotlin.math.abs
import kotlin.math.atan2

import javax.inject.Inject

/**
 * Use case for detecting user fatigue based on pose stability.
 * AC 1, 2, 3 compliance:
 * - Establishes baseline from first 2 reps.
 * - Detects >15° deviation over 3 consecutive reps.
 */
class DetectFatigueUseCase @Inject constructor() {

    private var baselineAngles: Map<PoseJoint, Double>? = null
    private var firstRepAngles: Map<PoseJoint, Double>? = null
    private var repCount = 0
    private var consecutiveBadReps = 0

    private val monitoredJoints = listOf(
        JointTrio(PoseJoint.LEFT_SHOULDER, PoseJoint.LEFT_ELBOW, PoseJoint.LEFT_WRIST),
        JointTrio(PoseJoint.RIGHT_SHOULDER, PoseJoint.RIGHT_ELBOW, PoseJoint.RIGHT_WRIST),
        JointTrio(PoseJoint.LEFT_HIP, PoseJoint.LEFT_KNEE, PoseJoint.LEFT_ANKLE),
        JointTrio(PoseJoint.RIGHT_HIP, PoseJoint.RIGHT_KNEE, PoseJoint.RIGHT_ANKLE),
        JointTrio(PoseJoint.LEFT_SHOULDER, PoseJoint.LEFT_HIP, PoseJoint.LEFT_KNEE),
        JointTrio(PoseJoint.RIGHT_SHOULDER, PoseJoint.RIGHT_HIP, PoseJoint.RIGHT_KNEE)
    )

    /**
     * Processes the peak pose of a rep and returns the current fatigue status.
     * @param peakPose The [PoseData] captured at the most difficult part of the rep.
     * @return [FatigueStatus.FATIGUED] if fatigue is detected, otherwise [FatigueStatus.HEALTHY].
     */
    fun analyzeRep(peakPose: PoseData): FatigueStatus {
        val currentAngles = calculateCurrentAngles(peakPose)
        repCount++

        when {
            repCount == 1 -> {
                firstRepAngles = currentAngles
                return FatigueStatus.HEALTHY
            }
            repCount == 2 -> {
                baselineAngles = calculateBaseline(firstRepAngles!!, currentAngles)
                return FatigueStatus.HEALTHY
            }
            repCount > 2 -> {
                val isBadRep = checkDeviation(currentAngles)
                if (isBadRep) {
                    consecutiveBadReps++
                } else {
                    consecutiveBadReps = 0
                }

                return if (consecutiveBadReps >= 3) {
                    FatigueStatus.FATIGUED
                } else {
                    FatigueStatus.HEALTHY
                }
            }
            else -> return FatigueStatus.HEALTHY
        }
    }

    /**
     * Resets the fatigue detection state for a new exercise.
     */
    fun reset() {
        baselineAngles = null
        firstRepAngles = null
        repCount = 0
        consecutiveBadReps = 0
    }

    private fun calculateCurrentAngles(pose: PoseData): Map<PoseJoint, Double> {
        val angles = mutableMapOf<PoseJoint, Double>()
        for (trio in monitoredJoints) {
            val p1 = pose.joints[trio.p1]
            val p2 = pose.joints[trio.p2]
            val p3 = pose.joints[trio.p3]

            if (p1 != null && p2 != null && p3 != null) {
                angles[trio.p2] = calculateAngle(p1, p2, p3)
            }
        }
        return angles
    }

    private fun calculateAngle(p1: JointCoordinate, p2: JointCoordinate, p3: JointCoordinate): Double {
        var result = Math.toDegrees(
            (atan2(p3.y - p2.y, p3.x - p2.x) - atan2(p1.y - p2.y, p1.x - p2.x)).toDouble()
        )
        result = abs(result)
        if (result > 180) {
            result = 360.0 - result
        }
        return result
    }

    private fun calculateBaseline(rep1: Map<PoseJoint, Double>, rep2: Map<PoseJoint, Double>): Map<PoseJoint, Double> {
        return rep1.keys.intersect(rep2.keys).associateWith { joint ->
            (rep1[joint]!! + rep2[joint]!!) / 2.0
        }
    }

    private fun checkDeviation(current: Map<PoseJoint, Double>): Boolean {
        val baseline = baselineAngles ?: return false
        for ((joint, angle) in current) {
            val baseAngle = baseline[joint] ?: continue
            if (abs(angle - baseAngle) > FATIGUE_DEVIATION_THRESHOLD) {
                return true
            }
        }
        return false
    }

    private data class JointTrio(val p1: PoseJoint, val p2: PoseJoint, val p3: PoseJoint)

    companion object {
        private const val FATIGUE_DEVIATION_THRESHOLD = 15.0
    }
}
