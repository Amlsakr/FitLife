package com.aml_sakr.fitlife.feature.progress.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.progress.domain.usecase.GetProgressAnalyticsUseCase
import com.aml_sakr.fitlife.feature.progress.domain.usecase.GetProgressChartDataUseCase
import com.aml_sakr.fitlife.feature.progress.domain.usecase.GetSessionHistoryUseCase
import com.aml_sakr.fitlife.feature.progress.ui.state.ProgressDashboardAction
import com.aml_sakr.fitlife.feature.progress.ui.state.ProgressDashboardEvent
import com.aml_sakr.fitlife.feature.progress.ui.state.ProgressDashboardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressDashboardViewModel @Inject constructor(
    private val getProgressAnalyticsUseCase: GetProgressAnalyticsUseCase,
    private val getProgressChartDataUseCase: GetProgressChartDataUseCase,
    private val getSessionHistoryUseCase: GetSessionHistoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressDashboardState())
    val state = _state.asStateFlow()

    private val _action = MutableSharedFlow<ProgressDashboardAction>()
    val action = _action.asSharedFlow()

    init {
        loadDashboardData()
    }

    fun onEvent(event: ProgressDashboardEvent) {
        when (event) {
            ProgressDashboardEvent.RefreshRequested -> loadDashboardData()
            is ProgressDashboardEvent.SessionClicked -> {
                viewModelScope.launch {
                    _action.emit(ProgressDashboardAction.NavigateToSessionDetail(event.sessionId))
                }
            }
        }
    }

    private fun loadDashboardData() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val userId = "USER_ID" // Placeholder until Auth is fully wired
            
            val analyticsDeferred = async { getProgressAnalyticsUseCase(userId, 0L) }
            val weeklyTrendDeferred = async { getProgressChartDataUseCase.getWeeklySessionsTrend(userId) }
            val dailyCaloriesDeferred = async { getProgressChartDataUseCase.getDailyCaloriesTrend(userId) }
            val historyDeferred = async { getSessionHistoryUseCase(userId) }

            val analyticsResult = analyticsDeferred.await()
            val weeklyTrendResult = weeklyTrendDeferred.await()
            val dailyCaloriesResult = dailyCaloriesDeferred.await()
            val historyResult = historyDeferred.await()

            val hasFailure = analyticsResult is Result.Failure ||
                    weeklyTrendResult is Result.Failure ||
                    dailyCaloriesResult is Result.Failure ||
                    historyResult is Result.Failure

            _state.update {
                it.copy(
                    isLoading = false,
                    analytics = (analyticsResult as? Result.Success)?.data ?: it.analytics,
                    weeklyTrend = (weeklyTrendResult as? Result.Success)?.data ?: it.weeklyTrend,
                    dailyCalories = (dailyCaloriesResult as? Result.Success)?.data ?: it.dailyCalories,
                    sessionHistory = (historyResult as? Result.Success)?.data ?: it.sessionHistory,
                    error = if (hasFailure) "Some data could not be loaded" else null
                )
            }
        }
    }
}
