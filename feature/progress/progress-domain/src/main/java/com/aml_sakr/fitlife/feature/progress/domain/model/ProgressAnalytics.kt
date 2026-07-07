package com.aml_sakr.fitlife.feature.progress.domain.model

data class ProgressAnalytics(
    val totalSessions: Int,
    val totalCalories: Int,
    val totalFatigueEvents: Int,
    val averageDurationSeconds: Int
)
