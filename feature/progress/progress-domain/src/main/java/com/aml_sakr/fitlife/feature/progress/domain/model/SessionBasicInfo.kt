package com.aml_sakr.fitlife.feature.progress.domain.model

data class SessionBasicInfo(
    val sessionId: String,
    val startTime: Long,
    val durationSeconds: Int?
)
