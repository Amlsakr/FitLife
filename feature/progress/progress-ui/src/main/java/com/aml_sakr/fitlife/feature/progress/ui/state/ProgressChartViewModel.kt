package com.aml_sakr.fitlife.feature.progress.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.progress.domain.usecase.GetProgressChartDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressChartViewModel @Inject constructor(
    private val getProgressChartDataUseCase: GetProgressChartDataUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressChartState())
    val state = _state.asStateFlow()

    private val _action = MutableSharedFlow<ProgressChartAction>()
    val action = _action.asSharedFlow()

    fun onEvent(event: ProgressChartEvent) {
        when (event) {
            is ProgressChartEvent.LoadWeeklyTrend -> loadWeeklyTrend()
            is ProgressChartEvent.LoadDailyCalories -> loadDailyCalories()
            is ProgressChartEvent.OnChartPointClicked -> {
                viewModelScope.launch {
                    _action.emit(ProgressChartAction.ShowToast("Clicked ${event.label}: ${event.value}"))
                }
            }
        }
    }

    private fun loadWeeklyTrend() {
        _state.update { it.copy(weeklyTrend = it.weeklyTrend.copy(isLoading = true, error = null)) }
        viewModelScope.launch {
            // Using placeholder USER_ID until auth is integrated
            when (val result = getProgressChartDataUseCase.getWeeklySessionsTrend("USER_ID")) {
                is Result.Success -> {
                    _state.update { it.copy(weeklyTrend = it.weeklyTrend.copy(isLoading = false, data = result.data)) }
                }
                is Result.Failure -> {
                    _state.update { it.copy(weeklyTrend = it.weeklyTrend.copy(isLoading = false, error = result.error.message)) }
                }
            }
        }
    }

    private fun loadDailyCalories() {
        _state.update { it.copy(dailyCalories = it.dailyCalories.copy(isLoading = true, error = null)) }
        viewModelScope.launch {
            // Using placeholder USER_ID until auth is integrated
            when (val result = getProgressChartDataUseCase.getDailyCaloriesTrend("USER_ID")) {
                is Result.Success -> {
                    _state.update { it.copy(dailyCalories = it.dailyCalories.copy(isLoading = false, data = result.data)) }
                }
                is Result.Failure -> {
                    _state.update { it.copy(dailyCalories = it.dailyCalories.copy(isLoading = false, error = result.error.message)) }
                }
            }
        }
    }
}
