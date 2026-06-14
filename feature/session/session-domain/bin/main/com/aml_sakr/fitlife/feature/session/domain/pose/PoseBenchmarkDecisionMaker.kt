package com.aml_sakr.fitlife.feature.session.domain.pose

object PoseBenchmarkDecisionMaker {
    private const val MinimumDurationMillis = 300_000L
    private const val MinimumAverageFps = 15.0

    fun decide(summary: PoseBenchmarkSummary): PoseBenchmarkDecision {
        val inconclusiveReasons = inconclusiveReasons(summary)
        if (inconclusiveReasons.isNotEmpty()) {
            return PoseBenchmarkDecision(
                outcome = PoseBenchmarkOutcome.Inconclusive,
                satisfiesAcceptanceCriteria = false,
                recommendation = "Run the benchmark on a physical Snapdragon 6xx-class or equivalent device before making the v1.0 pose-detection decision.",
                reasons = inconclusiveReasons
            )
        }

        val failureReasons = failureReasons(summary)
        if (failureReasons.isNotEmpty()) {
            return PoseBenchmarkDecision(
                outcome = PoseBenchmarkOutcome.Fail,
                satisfiesAcceptanceCriteria = false,
                recommendation = "Defer pose detection to v1.1 and launch v1.0 with audio-only guidance.",
                reasons = failureReasons
            )
        }

        return PoseBenchmarkDecision(
            outcome = PoseBenchmarkOutcome.Pass,
            satisfiesAcceptanceCriteria = true,
            recommendation = "Keep pose feedback in v1.0 and promote the spike harness carefully through the production session stories.",
            reasons = listOf("Representative 5-minute run sustained at least 15 FPS without invalidating runtime conditions.")
        )
    }

    private fun inconclusiveReasons(summary: PoseBenchmarkSummary): List<String> = buildList {
        if (!summary.decisionInputs.isPhysicalDevice) {
            add("Benchmark did not run on a physical device.")
        }
        if (!summary.decisionInputs.isRepresentativeMidRangeDevice) {
            add("Benchmark device was not confirmed as Snapdragon 6xx-class or equivalent.")
        }
        if (!summary.decisionInputs.subjectVisible) {
            add("Full-body subject visibility was not confirmed.")
        }
    }

    private fun failureReasons(summary: PoseBenchmarkSummary): List<String> = buildList {
        if (summary.elapsedMillis < MinimumDurationMillis) {
            add("Benchmark duration was shorter than 5 continuous minutes.")
        }
        if (summary.averageFps < MinimumAverageFps) {
            add("Average processed pose FPS was below 15.")
        }
        if (!summary.decisionInputs.previewRemainedResponsive) {
            add("Preview responsiveness was not preserved.")
        }
        if (summary.decisionInputs.appCrashed) {
            add("App crashed during benchmark.")
        }
        if (summary.decisionInputs.analyzerBacklogObserved) {
            add("Analyzer backlog was observed.")
        }
        if (summary.decisionInputs.unclosedFramesObserved) {
            add("Unclosed frame handling was observed or suspected.")
        }
        if (summary.decisionInputs.thermalThrottlingObserved) {
            add("Thermal throttling invalidated the result.")
        }
    }
}
