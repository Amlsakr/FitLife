package com.aml_sakr.fitlife.feature.workout.domain.gemini

enum class FitnessLevel {
    Beginner,
    Intermediate
}

data class GeminiWorkoutProfile(
    val id: String,
    val fitnessLevel: FitnessLevel,
    val goal: String,
    val location: String,
    val availableEquipment: List<String>,
    val days: Int
)

enum class GeminiBenchmarkExecutionTarget {
    LocalJvm,
    AndroidDebug,
    AndroidInstrumentation
}

data class GeminiBenchmarkEnvironment(
    val deviceModel: String,
    val androidApiLevel: Int?,
    val networkType: String,
    val approximateRegion: String,
    val runTimestamp: String,
    val executionTarget: GeminiBenchmarkExecutionTarget,
    val quotaVerified: Boolean,
    val usedLiveGeminiApi: Boolean
)

data class GeminiBenchmarkConfiguration(
    val modelName: String,
    val apiVersion: String,
    val endpoint: String,
    val responseMimeType: String,
    val temperature: Double,
    val maxOutputTokens: Int,
    val timeoutMillis: Long,
    val maxRetries: Int
)

enum class GeminiCallStatus {
    Success,
    Timeout,
    RateLimited,
    HttpError,
    NetworkError,
    ParseError
}

data class GeminiBenchmarkSample(
    val callIndex: Int,
    val status: GeminiCallStatus,
    val firstAttemptLatencyMillis: Long,
    val totalLatencyMillis: Long,
    val requestLatencyMillis: Long?,
    val parsingLatencyMillis: Long,
    val httpStatusCode: Int?,
    val retryCount: Int,
    val responseSizeChars: Int,
    val promptSizeChars: Int,
    val outputTokenEstimate: Int?,
    val schemaValid: Boolean,
    val mappedToWorkoutPlan: Boolean,
    val fallbackUsed: Boolean,
    val fallbackPlanPath: String?,
    val errorCategory: String?
)

data class GeminiBenchmarkRun(
    val environment: GeminiBenchmarkEnvironment,
    val configuration: GeminiBenchmarkConfiguration,
    val samples: List<GeminiBenchmarkSample>
)

data class GeminiBenchmarkSummary(
    val totalCallCount: Int,
    val successfulCallCount: Int,
    val successRate: Double,
    val averageSuccessfulLatencyMillis: Double,
    val p50SuccessfulLatencyMillis: Long,
    val p95SuccessfulLatencyMillis: Long,
    val minSuccessfulLatencyMillis: Long,
    val maxSuccessfulLatencyMillis: Long,
    val timeoutCount: Int,
    val rateLimitCount: Int,
    val httpErrorCount: Int,
    val networkErrorCount: Int,
    val parseErrorCount: Int,
    val fallbackCount: Int,
    val usedLiveGeminiApi: Boolean,
    val quotaVerified: Boolean,
    val selectedModel: String,
    val endpoint: String
)

enum class GeminiBenchmarkOutcome {
    Pass,
    Fail,
    Inconclusive
}

data class GeminiBenchmarkDecision(
    val outcome: GeminiBenchmarkOutcome,
    val satisfiesAcceptanceCriteria: Boolean,
    val recommendation: String,
    val reasons: List<String>
)

object GeminiBenchmarkProfiles {
    fun representativeProfiles(): List<GeminiWorkoutProfile> = listOf(
        GeminiWorkoutProfile(
            id = "beginner-home-egypt",
            fitnessLevel = FitnessLevel.Beginner,
            goal = "Build consistency and general strength",
            location = "Home",
            availableEquipment = listOf("bodyweight", "resistance band", "chair"),
            days = 7
        ),
        GeminiWorkoutProfile(
            id = "intermediate-gym-egypt",
            fitnessLevel = FitnessLevel.Intermediate,
            goal = "Improve muscle tone and endurance",
            location = "Gym",
            availableEquipment = listOf("dumbbells", "bench", "cable machine", "treadmill"),
            days = 7
        )
    )

    fun minimumTenCallProfiles(): List<GeminiWorkoutProfile> {
        val baseProfiles = representativeProfiles()
        return (0 until 10).map { index ->
            val base = baseProfiles[index % baseProfiles.size]
            base.copy(id = "${base.id}-call-${index + 1}")
        }
    }
}
