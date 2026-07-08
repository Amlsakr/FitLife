package com.aml_sakr.fitlife.feature.progress.ui.state

import com.aml_sakr.fitlife.feature.progress.domain.model.ChartData
import com.aml_sakr.fitlife.feature.progress.domain.model.ProgressAnalytics
import com.aml_sakr.fitlife.feature.progress.domain.model.SessionBasicInfo

data class ProgressDashboardState(
    val isLoading: Boolean = true,
    val analytics: ProgressAnalytics? = null,
    val weeklyTrend: ChartData? = null,
    val dailyCalories: ChartData? = null,
    val sessionHistory: List<SessionBasicInfo> = emptyList(),
    val error: String? = null
)
