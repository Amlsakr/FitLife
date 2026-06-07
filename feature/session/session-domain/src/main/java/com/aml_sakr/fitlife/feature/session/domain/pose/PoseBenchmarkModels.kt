package com.aml_sakr.fitlife.feature.session.domain.pose

data class PoseBenchmarkEnvironment(
    val deviceModel: String,
    val androidApiLevel: Int,
    val chipset: String?,
    val ramMb: Int?,
    val batteryPercent: Int?,
    val isCharging: Boolean,
    val thermalState: String,
    val lightingCondition: String,
    val subjectVisible: Boolean,
    val isPhysicalDevice: Boolean,
    val isRepresentativeMidRangeDevice: Boolean
)

data class PoseBenchmarkConfiguration(
    val cameraLens: String,
    val analysisResolution: String,
    val outputFormat: String,
    val mlKitSdk: String,
    val cameraXVersion: String
)

data class PoseBenchmarkSample(
    val timestampMillis: Long,
    val processingDurationMillis: Long,
    val poseDetected: Boolean,
    val visibleLandmarkCount: Int,
    val errorMessage: String?
)

data class PoseBenchmarkRun(
    val environment: PoseBenchmarkEnvironment,
    val configuration: PoseBenchmarkConfiguration,
    val samples: List<PoseBenchmarkSample>,
    val detectorInitializationMillis: Long,
    val previewRemainedResponsive: Boolean,
    val appCrashed: Boolean,
    val analyzerBacklogObserved: Boolean,
    val unclosedFramesObserved: Boolean,
    val thermalThrottlingObserved: Boolean
)

data class PoseBenchmarkDecisionInputs(
    val isPhysicalDevice: Boolean,
    val isRepresentativeMidRangeDevice: Boolean,
    val subjectVisible: Boolean,
    val previewRemainedResponsive: Boolean,
    val appCrashed: Boolean,
    val analyzerBacklogObserved: Boolean,
    val unclosedFramesObserved: Boolean,
    val thermalThrottlingObserved: Boolean
)

data class PoseBenchmarkSummary(
    val elapsedMillis: Long,
    val processedFrameCount: Int,
    val averageFps: Double,
    val p50ProcessingLatencyMillis: Long,
    val p95ProcessingLatencyMillis: Long,
    val errorCount: Int,
    val rollingWindowFps: List<Double>,
    val decisionInputs: PoseBenchmarkDecisionInputs
)

enum class PoseBenchmarkOutcome {
    Pass,
    Fail,
    Inconclusive
}

data class PoseBenchmarkDecision(
    val outcome: PoseBenchmarkOutcome,
    val satisfiesAcceptanceCriteria: Boolean,
    val recommendation: String,
    val reasons: List<String>
)
