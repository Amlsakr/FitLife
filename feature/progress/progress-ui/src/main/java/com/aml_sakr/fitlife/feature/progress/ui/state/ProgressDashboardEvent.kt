package com.aml_sakr.fitlife.feature.progress.ui.state

sealed interface ProgressDashboardEvent {
    object RefreshRequested : ProgressDashboardEvent
    data class SessionClicked(val sessionId: String) : ProgressDashboardEvent
}
