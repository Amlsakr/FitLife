package com.aml_sakr.fitlife.feature.session.ui.summary

import com.aml_sakr.fitlife.core.ui.mvi.UIEvent
import com.aml_sakr.fitlife.core.ui.mvi.UIState
import com.aml_sakr.fitlife.core.ui.mvi.OneTimeAction
import com.aml_sakr.fitlife.feature.session.domain.model.Session

data class SessionSummaryState(
    val session: Session? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val caloriesBurned: Int = 0
) : UIState

sealed interface SessionSummaryEvent : UIEvent {
    data class LoadSession(val sessionId: String) : SessionSummaryEvent
    data object ShareToWhatsApp : SessionSummaryEvent
    data object NavigateHome : SessionSummaryEvent
}

sealed interface SessionSummaryAction : OneTimeAction {
    data object NavigateHome : SessionSummaryAction
    data class OpenShareSheet(val imageUri: String, val text: String) : SessionSummaryAction
}
