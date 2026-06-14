package com.aml_sakr.fitlife.feature.workout.domain.gemini

import kotlin.math.ceil
import kotlin.math.max

object GeminiBenchmarkSummarizer {
    fun summarize(run: GeminiBenchmarkRun): GeminiBenchmarkSummary {
        val successfulSamples = run.samples
            .filter { it.status == GeminiCallStatus.Success && it.schemaValid && it.mappedToWorkoutPlan }
        val successfulLatencies = successfulSamples.map { it.totalLatencyMillis }.sorted()
        val totalCallCount = run.samples.size
        val successfulCallCount = successfulSamples.size

        return GeminiBenchmarkSummary(
            totalCallCount = totalCallCount,
            successfulCallCount = successfulCallCount,
            successRate = if (totalCallCount == 0) 0.0 else successfulCallCount.toDouble() / totalCallCount,
            averageSuccessfulLatencyMillis = successfulLatencies.averageOrZero(),
            p50SuccessfulLatencyMillis = percentile(successfulLatencies, 0.50),
            p95SuccessfulLatencyMillis = percentile(successfulLatencies, 0.95),
            minSuccessfulLatencyMillis = successfulLatencies.firstOrNull() ?: 0L,
            maxSuccessfulLatencyMillis = successfulLatencies.lastOrNull() ?: 0L,
            timeoutCount = run.samples.count { it.status == GeminiCallStatus.Timeout },
            rateLimitCount = run.samples.count { it.status == GeminiCallStatus.RateLimited },
            httpErrorCount = run.samples.count { it.status == GeminiCallStatus.HttpError },
            networkErrorCount = run.samples.count { it.status == GeminiCallStatus.NetworkError },
            parseErrorCount = run.samples.count { it.status == GeminiCallStatus.ParseError },
            fallbackCount = run.samples.count { it.fallbackUsed },
            usedLiveGeminiApi = run.environment.usedLiveGeminiApi,
            quotaVerified = run.environment.quotaVerified,
            selectedModel = run.configuration.modelName,
            endpoint = run.configuration.endpoint
        )
    }

    private fun List<Long>.averageOrZero(): Double =
        if (isEmpty()) 0.0 else average()

    private fun percentile(sortedValues: List<Long>, percentile: Double): Long {
        if (sortedValues.isEmpty()) return 0L
        val index = max(0, ceil(sortedValues.size * percentile).toInt() - 1)
        return sortedValues[index.coerceAtMost(sortedValues.lastIndex)]
    }
}
