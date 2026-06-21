package com.aml_sakr.fitlife.feature.workout.data.repository

import com.aml_sakr.fitlife.core.data.workout.WorkoutPlanEntity
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutFitnessLevel
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutGenerationRequest
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutLocation
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutPlan
import com.google.gson.Gson

class WorkoutPlanRoomMapper(
    private val gson: Gson
) {
    fun toEntity(plan: WorkoutPlan): WorkoutPlanEntity = WorkoutPlanEntity(
        planId = plan.planStorageId(),
        userId = plan.userId,
        requestKey = plan.toRequestKey(),
        generatedAtEpochMillis = plan.generatedAtEpochMillis,
        expiresAtEpochMillis = plan.expiresAtEpochMillis,
        fitnessLevel = plan.fitnessLevel.name,
        location = plan.location.name,
        requestedDays = plan.days.size,
        goalNames = plan.goals.map { it.name }.sorted(),
        equipmentNames = plan.availableEquipment.map { it.trim().lowercase() }.sorted(),
        weekNumber = 1,
        isFallback = plan.isFallback,
        planJson = gson.toJson(plan)
    )

    fun toDomain(entity: WorkoutPlanEntity): WorkoutPlan? =
        runCatching { gson.fromJson(entity.planJson, WorkoutPlan::class.java) }
            .getOrNull()
            ?.takeIf { plan ->
                plan.userId == entity.userId &&
                    plan.fitnessLevel.name == entity.fitnessLevel &&
                    plan.location.name == entity.location &&
                    plan.days.size == entity.requestedDays &&
                    plan.goals.map { it.name }.sorted() == entity.goalNames.sorted() &&
                    plan.availableEquipment.map { it.trim().lowercase() }.sorted() == entity.equipmentNames.sorted()
            }

    fun matchesRequest(entity: WorkoutPlanEntity, request: WorkoutGenerationRequest): Boolean =
        entity.requestKey == request.toRequestKey()

    fun isFresh(entity: WorkoutPlanEntity, nowEpochMillis: Long): Boolean =
        entity.expiresAtEpochMillis > nowEpochMillis

    fun toFailureSafeDomain(entity: WorkoutPlanEntity): WorkoutPlan? = toDomain(entity)

    private fun WorkoutFitnessLevel.asStorageValue(): String = name

    private fun WorkoutLocation.asStorageValue(): String = name
}
