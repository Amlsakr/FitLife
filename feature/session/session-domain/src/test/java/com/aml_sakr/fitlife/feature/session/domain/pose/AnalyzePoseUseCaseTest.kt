package com.aml_sakr.fitlife.feature.session.domain.pose

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyzePoseUseCaseTest {

    @Test
    fun `invoke should return pose data from detector`() = runBlocking {
        // Given
        val expectedPoseData = PoseData(
            timestampMillis = 1000L,
            joints = emptyMap(),
            overallConfidence = 0.8f
        )
        val fakeDetector = object : PoseDetector {
            override fun detectPose(imageProxy: Any): Flow<PoseData> = flowOf(expectedPoseData)
            override fun close() {}
        }
        val useCase = AnalyzePoseUseCase(fakeDetector)

        // When
        val result = useCase("fake-image").first()

        // Then
        assertEquals(expectedPoseData, result)
    }
}
