package com.aml_sakr.fitlife.feature.progress.ui.state

sealed interface ProgressChartEvent {
    object LoadWeeklyTrend : ProgressChartEvent
    object LoadDailyCalories : ProgressChartEvent
    data class OnChartPointClicked(val index: Int, val label: String, val value: Float) : ProgressChartEvent
}
