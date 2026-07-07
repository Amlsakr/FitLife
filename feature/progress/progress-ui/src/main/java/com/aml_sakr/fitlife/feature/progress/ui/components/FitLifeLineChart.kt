package com.aml_sakr.fitlife.feature.progress.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.aml_sakr.fitlife.feature.progress.domain.model.ChartData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

@Composable
fun FitLifeLineChart(
    chartData: ChartData?,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    onPointClicked: ((index: Int, label: String, value: Float) -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(rememberShimmerBrush()))
        } else if (chartData == null || chartData.dataPoints.isEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = chartData?.emptyMessage ?: "No data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
            val lifecycleOwner = LocalLifecycleOwner.current

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        legend.isEnabled = false
                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(false)
                        setDrawGridBackground(false)
                        setNoDataText("")
                        
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                        }
                        
                        axisLeft.apply {
                            setDrawGridLines(true)
                            granularity = 1f
                            axisMinimum = 0f
                        }
                        axisRight.isEnabled = false
                        
                        setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                            override fun onValueSelected(e: Entry?, h: Highlight?) {
                                e?.let {
                                    val index = it.x.toInt()
                                    if (index >= 0 && index < chartData.dataPoints.size) {
                                        val point = chartData.dataPoints[index]
                                        onPointClicked?.invoke(index, point.label, point.value)
                                    }
                                }
                            }
                            override fun onNothingSelected() {}
                        })
                    }
                },
                update = { chart ->
                    chart.xAxis.textColor = onSurfaceColor
                    chart.axisLeft.textColor = onSurfaceColor

                    val currentData = chart.data
                    val needsUpdate = currentData == null || currentData.entryCount != chartData.dataPoints.size
                    
                    val labels = chartData.dataPoints.map { it.label }
                    chart.xAxis.valueFormatter = object : IndexAxisValueFormatter(labels) {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt()
                            return if (index >= 0 && index < labels.size) labels[index] else ""
                        }
                    }

                    if (needsUpdate) {
                        val entries = chartData.dataPoints.mapIndexed { index, point ->
                            Entry(index.toFloat(), point.value)
                        }
                        val dataSet = LineDataSet(entries, chartData.yAxisLabel).apply {
                            color = primaryColor
                            setCircleColor(primaryColor)
                            lineWidth = 2f
                            circleRadius = 4f
                            setDrawValues(false)
                            mode = LineDataSet.Mode.CUBIC_BEZIER
                            setDrawFilled(true)
                            fillColor = primaryColor
                            fillAlpha = 50
                        }
                        chart.data = LineData(dataSet)
                        chart.notifyDataSetChanged()
                    } else {
                        val dataSet = chart.data?.getDataSetByIndex(0) as? LineDataSet
                        dataSet?.apply {
                            color = primaryColor
                            setCircleColor(primaryColor)
                            fillColor = primaryColor
                        }
                    }
                    chart.invalidate()
                }
            )

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    // Lifecycle hooks for legacy views if needed by MPAndroidChart internals
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }
        }
    }
}
