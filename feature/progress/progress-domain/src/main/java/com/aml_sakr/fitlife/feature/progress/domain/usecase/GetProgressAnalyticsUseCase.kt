package com.aml_sakr.fitlife.feature.progress.domain.usecase

import com.aml_sakr.fitlife.core.domain.DomainError
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.domain.usecase.CalculateCaloriesUseCase
import com.aml_sakr.fitlife.feature.progress.domain.model.ProgressAnalytics
import com.aml_sakr.fitlife.feature.progress.domain.repository.IProgressRepository
import javax.inject.Inject
import kotlin.math.roundToInt

class GetProgressAnalyticsUseCase @Inject constructor(
    private val repository: IProgressRepository,
    private val calculateCaloriesUseCase: CalculateCaloriesUseCase
) {
    suspend operator fun invoke(
        userId: String,
        startTime: Long,
        weightKg: Double = CalculateCaloriesUseCase.DEFAULT_WEIGHT_KG
    ): Result<ProgressAnalytics, DomainError> {
        val sessionCountResult = repository.getSessionCount(userId, startTime)
        val fatigueEventsResult = repository.getTotalFatigueEvents(userId, startTime)
        val totalDurationResult = repository.getTotalDuration(userId, startTime)

        if (sessionCountResult is Result.Success &&
            fatigueEventsResult is Result.Success &&
            totalDurationResult is Result.Success
        ) {
            val totalSessions = sessionCountResult.data
            val totalFatigueEvents = fatigueEventsResult.data
            val totalDuration = totalDurationResult.data
            val averageDuration = if (totalSessions > 0) {
                (totalDuration.toDouble() / totalSessions).roundToInt()
            } else 0
            
            val totalCalories = calculateCaloriesUseCase(
                durationSeconds = totalDuration,
                weightKg = weightKg
            )

            return Result.Success(
                ProgressAnalytics(
                    totalSessions = totalSessions,
                    totalCalories = totalCalories,
                    totalFatigueEvents = totalFatigueEvents,
                    averageDurationSeconds = averageDuration
                )
            )
        }

        return Result.Failure(ProgressDomainError("ANALYTICS_FAILED", "Failed to fetch progress analytics"))
    }

    private data class ProgressDomainError(override val code: String, override val message: String) : DomainError
}
