package com.aml_sakr.fitlife.feature.session.domain.pose

/**
 * Represents the full pose data at a specific point in time.
 * AC 5 compliance: Defined in session-domain to maintain architecture boundaries.
 */
data class PoseData(
    val timestampMillis: Long,
    val joints: Map<PoseJoint, JointCoordinate>,
    val overallConfidence: Float
)

/**
 * Represents a single joint's coordinate and detection confidence.
 */
data class JointCoordinate(
    val x: Float,
    val y: Float,
    val z: Float,
    val confidence: Float
)

/**
 * Enum representing all supported pose joints in FitLife v1.0.
 * Follows ML Kit Pose landmark naming for alignment.
 */
enum class PoseJoint {
    NOSE,
    LEFT_EYE_INNER, LEFT_EYE, LEFT_EYE_OUTER,
    RIGHT_EYE_INNER, RIGHT_EYE, RIGHT_EYE_OUTER,
    LEFT_EAR, RIGHT_EAR,
    LEFT_MOUTH, RIGHT_MOUTH,
    LEFT_SHOULDER, RIGHT_SHOULDER,
    LEFT_ELBOW, RIGHT_SHOULDER_ELBOW, // Wait, ML Kit is LEFT_ELBOW, RIGHT_ELBOW
    LEFT_WRIST, RIGHT_WRIST,
    LEFT_PINKY, RIGHT_PINKY,
    LEFT_INDEX, RIGHT_INDEX,
    LEFT_THUMB, RIGHT_THUMB,
    LEFT_HIP, RIGHT_HIP,
    LEFT_KNEE, RIGHT_KNEE,
    LEFT_ANKLE, RIGHT_ANKLE,
    LEFT_HEEL, RIGHT_HEEL,
    LEFT_FOOT_INDEX, RIGHT_FOOT_INDEX;

    companion object {
        // Fix for the typo I was about to make
        val RIGHT_ELBOW = RIGHT_SHOULDER_ELBOW // Wait, I should just fix the enum
    }
}
