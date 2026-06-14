package com.aml_sakr.fitlife.feature.workout.domain.gemini

object GeminiBenchmarkDecisionMaker {
    private const val MinimumCallCount = 10
    private const val MinimumSuccessRate = 0.95
    private const val MaximumAverageLatencyMillis = 5_000.0

    fun decide(summary: GeminiBenchmarkSummary): GeminiBenchmarkDecision {
        val inconclusiveReasons = inconclusiveReasons(summary)
        if (inconclusiveReasons.isNotEmpty()) {
            return GeminiBenchmarkDecision(
                outcome = GeminiBenchmarkOutcome.Inconclusive,
                satisfiesAcceptanceCriteria = false,
                recommendation = "Run the benchmark with a real Gemini API key and verified quota before deciding the v1.0 AI plan source.",
                reasons = inconclusiveReasons
            )
        }

        val failureReasons = failureReasons(summary)
        if (failureReasons.isNotEmpty()) {
            return GeminiBenchmarkDecision(
                outcome = GeminiBenchmarkOutcome.Fail,
                satisfiesAcceptanceCriteria = false,
                recommendation = "Make static fallback templates the primary source and use Gemini only as an enhancement.",
                reasons = failureReasons
            )
        }

        return GeminiBenchmarkDecision(
            outcome = GeminiBenchmarkOutcome.Pass,
            satisfiesAcceptanceCriteria = true,
            recommendation = "Keep Gemini primary for v1.0 workout-plan generation, with the required 5-second timeout and local fallback path.",
            reasons = listOf("Live Gemini run met the 10-call, 95% success, and under-5-second average latency thresholds.")
        )
    }

    private fun inconclusiveReasons(summary: GeminiBenchmarkSummary): List<String> = buildList {
        if (!summary.usedLiveGeminiApi) {
            add("Benchmark did not use live Gemini API calls.")
        }
        if (!summary.quotaVerified) {
            add("Free-tier or project quota was not verified before the run.")
        }
        if (summary.totalCallCount < MinimumCallCount) {
            add("Benchmark captured fewer than $MinimumCallCount calls.")
        }
    }

    private fun failureReasons(summary: GeminiBenchmarkSummary): List<String> = buildList {
        if (summary.successRate < MinimumSuccessRate) {
            add("Success rate was below 95%.")
        }
        if (summary.averageSuccessfulLatencyMillis >= MaximumAverageLatencyMillis) {
            add("Average successful end-to-end latency was 5 seconds or slower.")
        }
        if (summary.timeoutCount > 0) {
            add("One or more calls exceeded the 5-second timeout.")
        }
        if (summary.parseErrorCount > 0) {
            add("One or more responses could not be parsed into the expected workout-plan shape.")
        }
    }
}
