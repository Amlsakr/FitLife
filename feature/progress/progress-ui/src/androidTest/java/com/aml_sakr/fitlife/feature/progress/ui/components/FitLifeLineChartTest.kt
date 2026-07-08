package com.aml_sakr.fitlife.feature.progress.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aml_sakr.fitlife.feature.progress.domain.model.ChartData
import com.aml_sakr.fitlife.feature.progress.domain.model.ChartDataPoint
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FitLifeLineChartTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun lineChart_emptyData_displaysEmptyMessage() {
        val emptyData = ChartData(
            dataPoints = emptyList(),
            emptyMessage = "No data yet!"
        )

        composeTestRule.setContent {
            FitLifeLineChart(chartData = emptyData)
        }
        
        composeTestRule.onNodeWithText("No data yet!").assertIsDisplayed()
    }
}
