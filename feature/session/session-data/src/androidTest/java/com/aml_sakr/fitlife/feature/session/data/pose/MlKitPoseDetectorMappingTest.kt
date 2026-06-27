package com.aml_sakr.fitlife.feature.session.data.pose

import android.graphics.PointF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseJoint
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.common.PointF3D
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class MlKitPoseDetectorMappingTest {

    @Test
    fun mapToPoseData_correctlyMapsLandmarks() {
        val detector = MlKitPoseDetector()
        val mockPose = mock(Pose::class.java)
        val mockLandmark = mock(PoseLandmark::class.java)
        
        `when`(mockLandmark.position).thenReturn(PointF(100f, 200f))
        `when`(mockLandmark.inFrameLikelihood).thenReturn(0.95f)
        
        val mockPosition3D = PointF3D.from(100f, 200f, 50f)
        `when`(mockLandmark.position3D).thenReturn(mockPosition3D)

        // Mock NOSE landmark
        `when`(mockPose.getPoseLandmark(PoseLandmark.NOSE)).thenReturn(mockLandmark)
        
        // Use reflection to test the private mapToPoseData method
        val method = detector.javaClass.getDeclaredMethod("mapToPoseData", Pose::class.java, Long::class.javaPrimitiveType)
        method.isAccessible = true
        
        val result = method.invoke(detector, mockPose, 1000L) as com.aml_sakr.fitlife.feature.session.domain.pose.PoseData
        
        assertEquals(1000L, result.timestampMillis)
        val noseJoint = result.joints[PoseJoint.NOSE]
        assertNotNull("Nose joint should be mapped", noseJoint)
        assertEquals(100f, noseJoint!!.x)
        assertEquals(200f, noseJoint.y)
        assertEquals(50f, noseJoint.z)
        assertEquals(0.95f, noseJoint.confidence)
    }
}
