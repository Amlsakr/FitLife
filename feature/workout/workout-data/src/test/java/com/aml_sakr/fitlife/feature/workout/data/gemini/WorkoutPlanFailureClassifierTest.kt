package com.aml_sakr.fitlife.feature.workout.data.gemini

import com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.SocketTimeoutException

class WorkoutPlanFailureClassifierTest {
    private val classifier = WorkoutPlanFailureClassifier()

    @Test
    fun `classifies timeout failures`() {
        assertEquals(
            WorkoutGenerationError.RemoteTimeout,
            classifier.classifyThrowable(SocketTimeoutException("timed out"))
        )
    }

    @Test
    fun `classifies rate limited responses`() {
        assertEquals(
            WorkoutGenerationError.RemoteRateLimited,
            classifier.classifyHttpStatus(429)
        )
    }

    @Test
    fun `classifies non success http responses as http errors`() {
        assertEquals(
            WorkoutGenerationError.RemoteHttpError,
            classifier.classifyHttpStatus(500)
        )
    }

    @Test
    fun `classifies malformed parse results`() {
        assertEquals(
            WorkoutGenerationError.RemoteParseError,
            classifier.classifyParseValidity(false)
        )
    }
}
