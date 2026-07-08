package com.aml_sakr.fitlife.feature.progress.ui.state

sealed interface ProgressDashboardAction {
    data class NavigateToSessionDetail(val sessionId: String) : ProgressDashboardAction
}
