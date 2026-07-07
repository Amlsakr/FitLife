package com.aml_sakr.fitlife.feature.progress.ui.state

import com.aml_sakr.fitlife.feature.progress.domain.model.ChartData

data class ChartViewState(
    val isLoading: Boolean = true,
    val data: ChartData? = null,
    val error: String? = null
)

data class ProgressChartState(
    val weeklyTrend: ChartViewState = ChartViewState(),
    val dailyCalories: ChartViewState = ChartViewState()
)
