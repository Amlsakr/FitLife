package com.aml_sakr.fitlife.feature.workout.data.repository

import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutGenerationRequest
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutPlan

internal fun WorkoutGenerationRequest.toRequestKey(): String =
    buildRequestKey(
        userId = userId,
        fitnessLevel = fitnessLevel.name,
        location = location.name,
        requestedDays = requestedDays,
        goals = goals.map { it.name },
        equipment = availableEquipment
    )

internal fun WorkoutPlan.toRequestKey(): String =
    buildRequestKey(
        userId = userId,
        fitnessLevel = fitnessLevel.name,
        location = location.name,
        requestedDays = days.size,
        goals = goals.map { it.name },
        equipment = availableEquipment
    )

private fun buildRequestKey(
    userId: String,
    fitnessLevel: String,
    location: String,
    requestedDays: Int,
    goals: Collection<String>,
    equipment: Collection<String>
): String =
    listOf(
        userId.trim(),
        fitnessLevel.trim(),
        location.trim(),
        requestedDays.toString(),
        goals.map { it.trim() }.sorted().joinToString(separator = ","),
        equipment.map { it.trim().lowercase() }.sorted().joinToString(separator = ",")
    ).joinToString(separator = ":")

internal fun WorkoutPlan.planStorageId(): String = "${toRequestKey()}:$generatedAtEpochMillis"
