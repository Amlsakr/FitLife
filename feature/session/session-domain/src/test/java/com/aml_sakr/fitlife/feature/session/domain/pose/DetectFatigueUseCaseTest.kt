package com.aml_sakr.fitlife.feature.session.domain.pose

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DetectFatigueUseCaseTest {

    private lateinit var useCase: DetectFatigueUseCase

    @Before
    fun setUp() {
        useCase = DetectFatigueUseCase()
    }

    @Test
    fun `analyzeRep - first two reps - returns HEALTHY and establishes baseline`() {
        // Rep 1: 90 degrees
        val pose1 = createPoseWithAngle(90.0)
        assertEquals(FatigueStatus.HEALTHY, useCase.analyzeRep(pose1))

        // Rep 2: 100 degrees
        val pose2 = createPoseWithAngle(100.0)
        assertEquals(FatigueStatus.HEALTHY, useCase.analyzeRep(pose2))
        
        // Baseline should be (90 + 100) / 2 = 95 degrees
    }

    @Test
    fun `analyzeRep - healthy reps after baseline - returns HEALTHY`() {
        establishBaseline(90.0) // Baseline 90

        // Rep 3: 100 degrees (deviation 10 < 15)
        assertEquals(FatigueStatus.HEALTHY, useCase.analyzeRep(createPoseWithAngle(100.0)))
        
        // Rep 4: 80 degrees (deviation 10 < 15)
        assertEquals(FatigueStatus.HEALTHY, useCase.analyzeRep(createPoseWithAngle(80.0)))
    }

    @Test
    fun `analyzeRep - three consecutive bad reps - returns FATIGUED`() {
        establishBaseline(90.0) // Baseline 90

        // Rep 3: 110 degrees (deviation 20 > 15) -> 1st bad rep
        assertEquals(FatigueStatus.HEALTHY, useCase.analyzeRep(createPoseWithAngle(110.0)))

        // Rep 4: 120 degrees (deviation 30 > 15) -> 2nd bad rep
        assertEquals(FatigueStatus.HEALTHY, useCase.analyzeRep(createPoseWithAngle(120.0)))

        // Rep 5: 115 degrees (deviation 25 > 15) -> 3rd bad rep
        assertEquals(FatigueStatus.FATIGUED, useCase.analyzeRep(createPoseWithAngle(115.0)))
    }

    @Test
    fun `analyzeRep - bad reps interrupted by good rep - returns HEALTHY`() {
        establishBaseline(90.0) // Baseline 90

        // Rep 3: 110 degrees (Bad)
        useCase.analyzeRep(createPoseWithAngle(110.0))
        // Rep 4: 110 degrees (Bad)
        useCase.analyzeRep(createPoseWithAngle(110.0))
        
        // Rep 5: 95 degrees (Good)
        assertEquals(FatigueStatus.HEALTHY, useCase.analyzeRep(createPoseWithAngle(95.0)))

        // Rep 6: 110 degrees (Bad - count resets)
        assertEquals(FatigueStatus.HEALTHY, useCase.analyzeRep(createPoseWithAngle(110.0)))
    }

    @Test
    fun `reset - clears state for new exercise`() {
        establishBaseline(90.0)
        useCase.analyzeRep(createPoseWithAngle(110.0))
        useCase.analyzeRep(createPoseWithAngle(110.0))
        useCase.analyzeRep(createPoseWithAngle(110.0)) // FATIGUED

        useCase.reset()

        // Should start over with baseline collection
        assertEquals(FatigueStatus.HEALTHY, useCase.analyzeRep(createPoseWithAngle(150.0)))
        assertEquals(FatigueStatus.HEALTHY, useCase.analyzeRep(createPoseWithAngle(150.0)))
        // New baseline 150
        assertEquals(FatigueStatus.HEALTHY, useCase.analyzeRep(createPoseWithAngle(155.0)))
    }

    private fun establishBaseline(angle: Double) {
        useCase.analyzeRep(createPoseWithAngle(angle))
        useCase.analyzeRep(createPoseWithAngle(angle))
    }

    private fun createPoseWithAngle(degrees: Double): PoseData {
        val radians = Math.toRadians(degrees)
        // P2 at origin (0,0)
        // P1 at (1, 0)
        // P3 at (cos(radians), sin(radians))
        
        val joints = mutableMapOf<PoseJoint, JointCoordinate>()
        val trio = JointTrio(PoseJoint.LEFT_SHOULDER, PoseJoint.LEFT_ELBOW, PoseJoint.LEFT_WRIST)
        
        joints[trio.p1] = JointCoordinate(1f, 0f, 0f, 1f)
        joints[trio.p2] = JointCoordinate(0f, 0f, 0f, 1f)
        joints[trio.p3] = JointCoordinate(Math.cos(radians).toFloat(), Math.sin(radians).toFloat(), 0f, 1f)
        
        return PoseData(System.currentTimeMillis(), joints, 1f)
    }

    private data class JointTrio(val p1: PoseJoint, val p2: PoseJoint, val p3: PoseJoint)
}
