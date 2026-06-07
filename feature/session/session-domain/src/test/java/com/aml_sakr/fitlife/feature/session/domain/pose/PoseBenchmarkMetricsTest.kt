package com.aml_sakr.fitlife.feature.session.domain.pose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PoseBenchmarkMetricsTest {

    @Test
    fun `summary calculates fps and latency percentiles`() {
        val run = PoseBenchmarkRun(
            environment = PoseBenchmarkEnvironment(
                deviceModel = "Moto G",
                androidApiLevel = 35,
                chipset = "Snapdragon 695",
                ramMb = 6144,
                batteryPercent = 80,
                isCharging = false,
                thermalState = "normal",
                lightingCondition = "good lighting, full body visible",
                subjectVisible = true,
                isPhysicalDevice = true,
                isRepresentativeMidRangeDevice = true
            ),
            configuration = PoseBenchmarkConfiguration(
                cameraLens = "back",
                analysisResolution = "640x480",
                outputFormat = "YUV_420_888",
                mlKitSdk = "pose-detection:18.0.0-beta5",
                cameraXVersion = "1.6.1"
            ),
            samples = (1..900).map { index ->
                PoseBenchmarkSample(
                    timestampMillis = index * 1_000L / 30L,
                    processingDurationMillis = if (index <= 854) 40L else 90L,
                    poseDetected = true,
                    visibleLandmarkCount = 33,
                    errorMessage = null
                )
            },
            detectorInitializationMillis = 120L,
            previewRemainedResponsive = true,
            appCrashed = false,
            analyzerBacklogObserved = false,
            unclosedFramesObserved = false,
            thermalThrottlingObserved = false
        )

        val summary = PoseBenchmarkSummarizer.summarize(run)

        assertEquals(30_000L, summary.elapsedMillis)
        assertEquals(900, summary.processedFrameCount)
        assertEquals(30.0, summary.averageFps, 0.01)
        assertEquals(40L, summary.p50ProcessingLatencyMillis)
        assertEquals(90L, summary.p95ProcessingLatencyMillis)
        assertEquals(0, summary.errorCount)
    }

    @Test
    fun `decision passes only when five minute representative run sustains target fps`() {
        val summary = PoseBenchmarkSummary(
            elapsedMillis = 300_000L,
            processedFrameCount = 4_800,
            averageFps = 16.0,
            p50ProcessingLatencyMillis = 42L,
            p95ProcessingLatencyMillis = 70L,
            errorCount = 0,
            rollingWindowFps = listOf(15.6, 16.1, 15.9),
            decisionInputs = PoseBenchmarkDecisionInputs(
                isPhysicalDevice = true,
                isRepresentativeMidRangeDevice = true,
                subjectVisible = true,
                previewRemainedResponsive = true,
                appCrashed = false,
                analyzerBacklogObserved = false,
                unclosedFramesObserved = false,
                thermalThrottlingObserved = false
            )
        )

        val decision = PoseBenchmarkDecisionMaker.decide(summary)

        assertEquals(PoseBenchmarkOutcome.Pass, decision.outcome)
        assertTrue(decision.recommendation.contains("keep pose feedback", ignoreCase = true))
    }

    @Test
    fun `decision fails when fps target is missed on representative run`() {
        val summary = passingSummary().copy(averageFps = 14.9)

        val decision = PoseBenchmarkDecisionMaker.decide(summary)

        assertEquals(PoseBenchmarkOutcome.Fail, decision.outcome)
        assertTrue(decision.recommendation.contains("defer pose detection to v1.1", ignoreCase = true))
    }

    @Test
    fun `decision is inconclusive for emulator or non representative devices`() {
        val summary = passingSummary().copy(
            decisionInputs = passingSummary().decisionInputs.copy(
                isPhysicalDevice = false,
                isRepresentativeMidRangeDevice = false
            )
        )

        val decision = PoseBenchmarkDecisionMaker.decide(summary)

        assertEquals(PoseBenchmarkOutcome.Inconclusive, decision.outcome)
        assertFalse(decision.satisfiesAcceptanceCriteria)
    }

    private fun passingSummary() = PoseBenchmarkSummary(
        elapsedMillis = 300_000L,
        processedFrameCount = 4_650,
        averageFps = 15.5,
        p50ProcessingLatencyMillis = 45L,
        p95ProcessingLatencyMillis = 72L,
        errorCount = 0,
        rollingWindowFps = listOf(15.1, 15.6, 15.7),
        decisionInputs = PoseBenchmarkDecisionInputs(
            isPhysicalDevice = true,
            isRepresentativeMidRangeDevice = true,
            subjectVisible = true,
            previewRemainedResponsive = true,
            appCrashed = false,
            analyzerBacklogObserved = false,
            unclosedFramesObserved = false,
            thermalThrottlingObserved = false
        )
    )
}
