package com.aml_sakr.fitlife.feature.session.data.pose

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MlKitPoseDetectorTest {

    @Test
    fun detector_canBeInitialized() {
        val detector = MlKitPoseDetector()
        assertNotNull(detector)
        detector.close()
    }
}
