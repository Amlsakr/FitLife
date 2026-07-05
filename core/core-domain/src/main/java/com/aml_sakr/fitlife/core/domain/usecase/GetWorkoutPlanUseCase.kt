package com.aml_sakr.fitlife.core.domain.usecase

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.domain.error.WorkoutGenerationError
import com.aml_sakr.fitlife.core.domain.model.WorkoutPlan
import com.aml_sakr.fitlife.core.domain.repository.WorkoutPlanRepository
import javax.inject.Inject

class GetWorkoutPlanUseCase @Inject constructor(
    private val repository: WorkoutPlanRepository
) {
    suspend operator fun invoke(planId: String): Result<WorkoutPlan?, WorkoutGenerationError> {
        return repository.getPlanById(planId)
    }
}
