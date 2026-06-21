package com.aml_sakr.fitlife.feature.workout.domain.usecase

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutGenerationRequest
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutPlan
import com.aml_sakr.fitlife.feature.workout.domain.repository.WorkoutPlanRepository
import javax.inject.Inject

class GenerateWorkoutPlanUseCase @Inject constructor(
    private val repository: WorkoutPlanRepository,
    private val clock: WorkoutPlanClock = SystemWorkoutPlanClock
) {
    suspend operator fun invoke(
        request: WorkoutGenerationRequest
    ): Result<WorkoutPlan, WorkoutGenerationError> {
        when (val cached = repository.getCachedPlan(request)) {
            is Result.Success -> {
                val cachedPlan = cached.data
                if (cachedPlan != null && cachedPlan.isFresh(clock.nowEpochMillis())) {
                    return Result.Success(cachedPlan)
                }
            }
            is Result.Failure -> {
                if (cached.error == WorkoutGenerationError.CacheUnavailable) {
                    // Cache failures should not block fresh generation.
                }
            }
        }

        return when (val remote = repository.generateRemotePlan(request)) {
            is Result.Success -> persistAndReturn(remote.data)
            is Result.Failure -> when (val fallback = repository.loadFallbackPlan(request)) {
                is Result.Success -> persistAndReturn(fallback.data.copy(isFallback = true))
                is Result.Failure -> Result.Failure(fallback.error)
            }
        }
    }

    private suspend fun persistAndReturn(
        plan: WorkoutPlan
    ): Result<WorkoutPlan, WorkoutGenerationError> {
        return when (val save = repository.savePlan(plan)) {
            is Result.Success -> Result.Success(plan)
            is Result.Failure -> Result.Failure(save.error)
        }
    }
}

interface WorkoutPlanClock {
    fun nowEpochMillis(): Long
}

object SystemWorkoutPlanClock : WorkoutPlanClock {
    override fun nowEpochMillis(): Long = System.currentTimeMillis()
}
