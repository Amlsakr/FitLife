package com.aml_sakr.fitlife.feature.workout.domain.repository

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.domain.model.WorkoutGenerationRequest
import com.aml_sakr.fitlife.core.domain.model.WorkoutPlan
import com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError

interface WorkoutPlanRepository {
    suspend fun getCachedPlan(request: WorkoutGenerationRequest): Result<WorkoutPlan?, WorkoutGenerationError>

    suspend fun getPlanById(planId: String): Result<WorkoutPlan?, WorkoutGenerationError>

    suspend fun generateRemotePlan(request: WorkoutGenerationRequest): Result<WorkoutPlan, WorkoutGenerationError>

    suspend fun loadFallbackPlan(request: WorkoutGenerationRequest): Result<WorkoutPlan, WorkoutGenerationError>

    suspend fun savePlan(plan: WorkoutPlan): Result<Unit, WorkoutGenerationError>
}
