package com.aml_sakr.fitlife.core.data.observability

import com.aml_sakr.fitlife.core.domain.AnalyticsLogger

class InMemoryAnalyticsLogger : AnalyticsLogger {
    private val mutableEvents = mutableListOf<AnalyticsEvent>()
    val events: List<AnalyticsEvent> = mutableEvents

    override fun logEvent(name: String, params: Map<String, Any?>) {
        mutableEvents += AnalyticsEvent(
            name = name,
            params = params.filterValues { it != null }
        )
    }
}

data class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any?>
)
