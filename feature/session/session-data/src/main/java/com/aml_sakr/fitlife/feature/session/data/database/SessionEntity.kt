package com.aml_sakr.fitlife.feature.session.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val sessionId: String,
    val userId: String,
    val planId: String,
    val workoutDayId: String,
    val startTime: Long,
    val endTime: Long?,
    val durationSeconds: Int?,
    val totalReps: Int,
    val totalSets: Int,
    val fatigueEventCount: Int,
    val audioFallbackUsed: Boolean,
    val completionPercentage: Float,
    val whatsAppShared: Boolean
)
