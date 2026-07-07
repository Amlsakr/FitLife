package com.aml_sakr.fitlife.feature.session.ui.summary

import androidx.lifecycle.viewModelScope
import com.aml_sakr.fitlife.core.domain.AnalyticsLogger
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.domain.usecase.CalculateCaloriesUseCase
import com.aml_sakr.fitlife.core.ui.mvi.BaseMviViewModel
import com.aml_sakr.fitlife.feature.session.domain.usecase.GetSessionUseCase
import com.aml_sakr.fitlife.feature.session.domain.model.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionSummaryViewModel @Inject constructor(
    private val getSessionUseCase: GetSessionUseCase,
    private val calculateCaloriesUseCase: CalculateCaloriesUseCase,
    private val analyticsLogger: AnalyticsLogger
) : BaseMviViewModel<SessionSummaryState, SessionSummaryEvent, SessionSummaryAction>(
    SessionSummaryState()
) {

    override fun handleEvent(event: SessionSummaryEvent) {
        when (event) {
            is SessionSummaryEvent.LoadSession -> loadSession(event.sessionId)
            SessionSummaryEvent.ShareToWhatsApp -> handleShare()
            SessionSummaryEvent.NavigateHome -> sendAction(SessionSummaryAction.NavigateHome)
        }
    }

    private fun loadSession(sessionId: String) {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getSessionUseCase(sessionId)) {
                is Result.Success -> {
                    val session = result.value
                    if (session != null) {
                        val calories = calculateCaloriesUseCase(session.durationSeconds ?: 0)
                        setState { copy(session = session, caloriesBurned = calories, isLoading = false) }
                    } else {
                        setState { copy(error = "Session not found", isLoading = false) }
                    }
                }
                is Result.Failure -> {
                    setState { copy(error = result.error.message, isLoading = false) }
                }
            }
        }
    }

    private fun handleShare() {
        analyticsLogger.logEvent("whatsapp_share_tapped", mapOf("session_id" to state.value.session?.sessionId))
        // Actual image generation and sharing logic will be added in Task 5
        sendAction(SessionSummaryAction.OpenShareSheet("temp_uri", "I just finished a workout on FitLife!"))
    }
}
