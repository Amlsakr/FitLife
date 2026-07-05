package com.aml_sakr.fitlife.core.domain

interface AnalyticsLogger {
    fun logEvent(
        name: String,
        params: Map<String, Any?> = emptyMap()
    )
}
