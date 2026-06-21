package com.aml_sakr.fitlife.feature.workout.data.gemini

import com.aml_sakr.fitlife.feature.workout.data.fallback.WorkoutPlanFallbackDay
import com.aml_sakr.fitlife.feature.workout.data.fallback.WorkoutPlanFallbackExercise
import com.aml_sakr.fitlife.feature.workout.data.fallback.WorkoutPlanFallbackTemplate
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutDay
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutExercise
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutGenerationRequest
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutPlan

class WorkoutPlanMapper {
    fun fromGeminiDraft(
        request: WorkoutGenerationRequest,
        draft: GeminiWorkoutPlanDraft,
        generatedAtEpochMillis: Long,
        isFallback: Boolean = false
    ): WorkoutPlan = WorkoutPlan(
        userId = request.userId,
        fitnessLevel = request.fitnessLevel,
        goals = request.goals,
        location = request.location,
        availableEquipment = request.availableEquipment,
        days = draft.days.map { it.toDomainDay() },
        generatedAtEpochMillis = generatedAtEpochMillis,
        expiresAtEpochMillis = WorkoutPlan.expiresAt(generatedAtEpochMillis),
        isFallback = isFallback
    )

    fun fromFallbackTemplate(
        request: WorkoutGenerationRequest,
        template: WorkoutPlanFallbackTemplate,
        generatedAtEpochMillis: Long
    ): WorkoutPlan = WorkoutPlan(
        userId = request.userId,
        fitnessLevel = template.fitnessLevel,
        goals = request.goals,
        location = template.location,
        availableEquipment = request.availableEquipment,
        days = template.days.map { it.toDomainDay() },
        generatedAtEpochMillis = generatedAtEpochMillis,
        expiresAtEpochMillis = WorkoutPlan.expiresAt(generatedAtEpochMillis),
        isFallback = true
    )

    private fun GeminiWorkoutDayDraft.toDomainDay(): WorkoutDay = WorkoutDay(
        day = day,
        title = title,
        durationMinutes = durationMinutes,
        exercises = exercises.map { it.toDomainExercise() }
    )

    private fun WorkoutPlanFallbackDay.toDomainDay(): WorkoutDay = WorkoutDay(
        day = day,
        title = title,
        durationMinutes = durationMinutes,
        exercises = exercises.map { it.toDomainExercise() }
    )

    private fun GeminiWorkoutExerciseDraft.toDomainExercise(): WorkoutExercise = WorkoutExercise(
        name = name,
        sets = sets,
        reps = reps,
        estimatedDurationMinutes = estimatedDurationMinutes
    )

    private fun WorkoutPlanFallbackExercise.toDomainExercise(): WorkoutExercise = WorkoutExercise(
        name = name,
        sets = sets,
        reps = reps,
        estimatedDurationMinutes = estimatedDurationMinutes
    )
}
