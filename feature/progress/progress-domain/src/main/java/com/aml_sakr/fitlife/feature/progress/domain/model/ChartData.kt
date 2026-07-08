package com.aml_sakr.fitlife.feature.progress.domain.model

data class ChartDataPoint(
    val label: String,
    val value: Float
)

data class ChartData(
    val dataPoints: List<ChartDataPoint>,
    val yAxisLabel: String = "",
    val emptyMessage: String = "No data available"
)
