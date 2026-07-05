package com.aml_sakr.fitlife.feature.session.domain.model

data class Session(
    val sessionId: String,
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
