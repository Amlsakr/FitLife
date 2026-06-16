package com.aml_sakr.fitlife.feature.workout.domain.gemini

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiBenchmarkMetricsTest {

    @Test
    fun `summary calculates latency percentiles and success rate`() {
        val run = GeminiBenchmarkRun(
            environment = benchmarkEnvironment(),
            configuration = benchmarkConfiguration(),
            samples = listOf(
                successfulSample(callIndex = 1, latencyMillis = 1_200L, parsingMillis = 40L),
                successfulSample(callIndex = 2, latencyMillis = 1_600L, parsingMillis = 45L),
                successfulSample(callIndex = 3, latencyMillis = 2_000L, parsingMillis = 50L),
                timedOutSample(callIndex = 4)
            )
        )

        val summary = GeminiBenchmarkSummarizer.summarize(run)

        assertEquals(4, summary.totalCallCount)
        assertEquals(3, summary.successfulCallCount)
        assertEquals(0.75, summary.successRate, 0.001)
        assertEquals(1_600.0, summary.averageSuccessfulLatencyMillis, 0.001)
        assertEquals(1_600L, summary.p50SuccessfulLatencyMillis)
        assertEquals(2_000L, summary.p95SuccessfulLatencyMillis)
        assertEquals(1, summary.timeoutCount)
        assertEquals(0, summary.parseErrorCount)
        assertEquals(1, summary.fallbackCount)
    }

    @Test
    fun `decision passes when minimum live calls are all successful under five seconds`() {
        val summary = passingSummary()

        val decision = GeminiBenchmarkDecisionMaker.decide(summary)

        assertEquals(GeminiBenchmarkOutcome.Pass, decision.outcome)
        assertTrue(decision.satisfiesAcceptanceCriteria)
        assertTrue(decision.recommendation.contains("Gemini primary", ignoreCase = true))
    }

    @Test
    fun `decision fails when successful calls average five seconds or more`() {
        val decision = GeminiBenchmarkDecisionMaker.decide(
            passingSummary().copy(averageSuccessfulLatencyMillis = 5_000.0)
        )

        assertEquals(GeminiBenchmarkOutcome.Fail, decision.outcome)
        assertFalse(decision.satisfiesAcceptanceCriteria)
        assertTrue(decision.recommendation.contains("static fallback templates", ignoreCase = true))
    }

    @Test
    fun `decision fails when minimum ten call run has any failed call`() {
        val summary = passingSummary().copy(
            successfulCallCount = 9,
            successRate = 0.9,
            timeoutCount = 1,
            fallbackCount = 1
        )

        val decision = GeminiBenchmarkDecisionMaker.decide(summary)

        assertEquals(GeminiBenchmarkOutcome.Fail, decision.outcome)
        assertTrue(decision.reasons.any { it.contains("success rate", ignoreCase = true) })
    }

    @Test
    fun `decision is inconclusive when live Gemini calls are not available`() {
        val decision = GeminiBenchmarkDecisionMaker.decide(
            passingSummary().copy(usedLiveGeminiApi = false)
        )

        assertEquals(GeminiBenchmarkOutcome.Inconclusive, decision.outcome)
        assertFalse(decision.satisfiesAcceptanceCriteria)
        assertTrue(decision.reasons.any { it.contains("live Gemini", ignoreCase = true) })
    }

    @Test
    fun `representative profiles cover beginner and intermediate plan inputs`() {
        val profiles = GeminiBenchmarkProfiles.representativeProfiles()

        assertTrue(profiles.any { it.fitnessLevel == FitnessLevel.Beginner })
        assertTrue(profiles.any { it.fitnessLevel == FitnessLevel.Intermediate })
        assertTrue(profiles.all { it.days == 7 })
        assertTrue(profiles.all { it.availableEquipment.isNotEmpty() })
    }

    @Test
    fun `minimum benchmark profiles provide ten call run inputs`() {
        val profiles = GeminiBenchmarkProfiles.minimumTenCallProfiles()

        assertEquals(10, profiles.size)
        assertTrue(profiles.any { it.fitnessLevel == FitnessLevel.Beginner })
        assertTrue(profiles.any { it.fitnessLevel == FitnessLevel.Intermediate })
        assertTrue(profiles.all { it.id.contains("-call-") })
    }

    private fun passingSummary() = GeminiBenchmarkSummary(
        totalCallCount = 10,
        successfulCallCount = 10,
        successRate = 1.0,
        averageSuccessfulLatencyMillis = 3_250.0,
        p50SuccessfulLatencyMillis = 3_100L,
        p95SuccessfulLatencyMillis = 4_700L,
        minSuccessfulLatencyMillis = 2_200L,
        maxSuccessfulLatencyMillis = 4_700L,
        timeoutCount = 0,
        rateLimitCount = 0,
        httpErrorCount = 0,
        networkErrorCount = 0,
        parseErrorCount = 0,
        fallbackCount = 0,
        usedLiveGeminiApi = true,
        quotaVerified = true,
        selectedModel = "models/gemini-2.5-flash-lite",
        endpoint = "v1beta/models/gemini-2.5-flash-lite:generateContent"
    )

    private fun benchmarkEnvironment() = GeminiBenchmarkEnvironment(
        deviceModel = "OPPO CPH2737",
        androidApiLevel = 36,
        networkType = "Wi-Fi",
        approximateRegion = "Egypt",
        runTimestamp = "2026-06-07T00:00:00+03:00",
        executionTarget = GeminiBenchmarkExecutionTarget.AndroidDebug,
        quotaVerified = true,
        usedLiveGeminiApi = true
    )

    private fun benchmarkConfiguration() = GeminiBenchmarkConfiguration(
        modelName = "models/gemini-2.5-flash-lite",
        apiVersion = "v1beta",
        endpoint = "v1beta/models/gemini-2.5-flash-lite:generateContent",
        responseMimeType = "application/json",
        temperature = 0.2,
        maxOutputTokens = 4096,
        timeoutMillis = 5_000L,
        maxRetries = 0
    )

    private fun successfulSample(
        callIndex: Int,
        latencyMillis: Long,
        parsingMillis: Long
    ) = GeminiBenchmarkSample(
        callIndex = callIndex,
        status = GeminiCallStatus.Success,
        firstAttemptLatencyMillis = latencyMillis,
        totalLatencyMillis = latencyMillis,
        requestLatencyMillis = latencyMillis - parsingMillis,
        parsingLatencyMillis = parsingMillis,
        httpStatusCode = 200,
        retryCount = 0,
        responseSizeChars = 1_024,
        promptSizeChars = 700,
        outputTokenEstimate = 250,
        schemaValid = true,
        mappedToWorkoutPlan = true,
        fallbackUsed = false,
        fallbackPlanPath = null,
        errorCategory = null
    )

    private fun timedOutSample(callIndex: Int) = GeminiBenchmarkSample(
        callIndex = callIndex,
        status = GeminiCallStatus.Timeout,
        firstAttemptLatencyMillis = 5_000L,
        totalLatencyMillis = 5_000L,
        requestLatencyMillis = 5_000L,
        parsingLatencyMillis = 0L,
        httpStatusCode = null,
        retryCount = 0,
        responseSizeChars = 0,
        promptSizeChars = 700,
        outputTokenEstimate = null,
        schemaValid = false,
        mappedToWorkoutPlan = false,
        fallbackUsed = true,
        fallbackPlanPath = "assets/fallback_workout_plans.json",
        errorCategory = "timeout"
    )
}
