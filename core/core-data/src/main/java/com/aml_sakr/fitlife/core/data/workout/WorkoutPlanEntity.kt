package com.aml_sakr.fitlife.core.data.workout

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_plans")
data class WorkoutPlanEntity(
    @PrimaryKey val planId: String,
    val userId: String,
    val requestKey: String,
    val generatedAtEpochMillis: Long,
    val expiresAtEpochMillis: Long,
    val fitnessLevel: String,
    val location: String,
    val requestedDays: Int,
    val goalNames: List<String>,
    val equipmentNames: List<String>,
    val weekNumber: Int,
    val isFallback: Boolean,
    val planJson: String
)
