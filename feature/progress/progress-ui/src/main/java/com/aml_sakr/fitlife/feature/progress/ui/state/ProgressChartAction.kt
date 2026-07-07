package com.aml_sakr.fitlife.feature.progress.ui.state

sealed interface ProgressChartAction {
    data class ShowToast(val message: String) : ProgressChartAction
}
