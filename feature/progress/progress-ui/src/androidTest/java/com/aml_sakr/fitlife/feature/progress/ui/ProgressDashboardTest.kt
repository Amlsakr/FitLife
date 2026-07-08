package com.aml_sakr.fitlife.feature.progress.ui

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aml_sakr.fitlife.feature.progress.domain.model.ProgressAnalytics
import com.aml_sakr.fitlife.feature.progress.domain.model.SessionBasicInfo
import com.aml_sakr.fitlife.feature.progress.ui.state.ProgressDashboardState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressDashboardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun progressDashboard_loading_showsIndicator() {
        val state = ProgressDashboardState(isLoading = true)
        
        composeTestRule.setContent {
            ProgressDashboardContent(state = state, onEvent = {})
        }
        
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun progressDashboard_withData_displaysMetricsAndHistory() {
        val analytics = ProgressAnalytics(10, 500, 3, 600)
        val history = listOf(
            SessionBasicInfo("s1", 1000L, 300)
        )
        val state = ProgressDashboardState(
            isLoading = false,
            analytics = analytics,
            sessionHistory = history
        )

        composeTestRule.setContent {
            ProgressDashboardContent(state = state, onEvent = {})
        }

        composeTestRule.onNodeWithText("Total Sessions").assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total Calories").assertIsDisplayed()
        composeTestRule.onNodeWithText("500").assertIsDisplayed()
        
        // Check history item (formatted date is tricky to match exactly without knowing local time, 
        // but we can check for duration text)
        composeTestRule.onNodeWithText("Duration: 5m 0s").assertIsDisplayed()
    }

    @Test
    fun progressDashboard_emptyHistory_showsEmptyMessage() {
        val state = ProgressDashboardState(
            isLoading = false,
            sessionHistory = emptyList()
        )

        composeTestRule.setContent {
            ProgressDashboardContent(state = state, onEvent = {})
        }

        composeTestRule.onNodeWithText("No sessions recorded yet.").assertIsDisplayed()
    }
}
