package com.aml_sakr.fitlife.feature.session.domain.pose

import kotlin.math.ceil
import kotlin.math.max

object PoseBenchmarkSummarizer {
    private const val RollingWindowMillis = 10_000L

    fun summarize(run: PoseBenchmarkRun): PoseBenchmarkSummary {
        val sortedSamples = run.samples.sortedBy { it.timestampMillis }
        val elapsedMillis = sortedSamples.lastOrNull()?.timestampMillis
            ?: 0L
        val processedFrameCount = sortedSamples.size
        val averageFps = if (elapsedMillis > 0L) {
            processedFrameCount * 1_000.0 / elapsedMillis
        } else {
            0.0
        }
        val latencies = sortedSamples.map { it.processingDurationMillis }.sorted()

        return PoseBenchmarkSummary(
            elapsedMillis = elapsedMillis,
            processedFrameCount = processedFrameCount,
            averageFps = averageFps,
            p50ProcessingLatencyMillis = percentile(latencies, 0.50),
            p95ProcessingLatencyMillis = percentile(latencies, 0.95),
            errorCount = sortedSamples.count { it.errorMessage != null },
            rollingWindowFps = rollingWindowFps(sortedSamples, elapsedMillis),
            decisionInputs = PoseBenchmarkDecisionInputs(
                isPhysicalDevice = run.environment.isPhysicalDevice,
                isRepresentativeMidRangeDevice = run.environment.isRepresentativeMidRangeDevice,
                subjectVisible = run.environment.subjectVisible,
                previewRemainedResponsive = run.previewRemainedResponsive,
                appCrashed = run.appCrashed,
                analyzerBacklogObserved = run.analyzerBacklogObserved,
                unclosedFramesObserved = run.unclosedFramesObserved,
                thermalThrottlingObserved = run.thermalThrottlingObserved
            )
        )
    }

    private fun percentile(sortedValues: List<Long>, percentile: Double): Long {
        if (sortedValues.isEmpty()) return 0L
        val index = max(0, ceil(sortedValues.size * percentile).toInt() - 1)
        return sortedValues[index.coerceAtMost(sortedValues.lastIndex)]
    }

    private fun rollingWindowFps(samples: List<PoseBenchmarkSample>, elapsedMillis: Long): List<Double> {
        if (elapsedMillis <= 0L) return emptyList()
        return generateSequence(0L) { start -> start + RollingWindowMillis }
            .takeWhile { it < elapsedMillis }
            .map { start ->
                val endExclusive = start + RollingWindowMillis
                val count = samples.count { it.timestampMillis > start && it.timestampMillis <= endExclusive }
                count * 1_000.0 / RollingWindowMillis
            }
            .toList()
    }
}
