package com.aml_sakr.fitlife.feature.progress.ui

import app.cash.turbine.test
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.progress.domain.model.ChartData
import com.aml_sakr.fitlife.feature.progress.domain.model.ProgressAnalytics
import com.aml_sakr.fitlife.feature.progress.domain.model.SessionBasicInfo
import com.aml_sakr.fitlife.feature.progress.domain.usecase.GetProgressAnalyticsUseCase
import com.aml_sakr.fitlife.feature.progress.domain.usecase.GetProgressChartDataUseCase
import com.aml_sakr.fitlife.feature.progress.domain.usecase.GetSessionHistoryUseCase
import com.aml_sakr.fitlife.feature.progress.ui.state.ProgressDashboardAction
import com.aml_sakr.fitlife.feature.progress.ui.state.ProgressDashboardEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressDashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getProgressAnalyticsUseCase: GetProgressAnalyticsUseCase
    private lateinit var getProgressChartDataUseCase: GetProgressChartDataUseCase
    private lateinit var getSessionHistoryUseCase: GetSessionHistoryUseCase
    
    private lateinit var viewModel: ProgressDashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getProgressAnalyticsUseCase = mock()
        getProgressChartDataUseCase = mock()
        getSessionHistoryUseCase = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads data successfully`() = runTest {
        // Arrange
        val analytics = ProgressAnalytics(5, 100, 2, 300)
        val weeklyTrend = ChartData(emptyList(), "y", "empty")
        val dailyCalories = ChartData(emptyList(), "y", "empty")
        val history = listOf(SessionBasicInfo("s1", 1000L, 300))

        whenever(getProgressAnalyticsUseCase(eq("USER_ID"), any(), any())).doReturn(Result.Success(analytics))
        whenever(getProgressChartDataUseCase.getWeeklySessionsTrend(eq("USER_ID"), any())).doReturn(Result.Success(weeklyTrend))
        whenever(getProgressChartDataUseCase.getDailyCaloriesTrend(eq("USER_ID"), any(), any())).doReturn(Result.Success(dailyCalories))
        whenever(getSessionHistoryUseCase(eq("USER_ID"), any())).doReturn(Result.Success(history))

        // Act
        viewModel = ProgressDashboardViewModel(
            getProgressAnalyticsUseCase,
            getProgressChartDataUseCase,
            getSessionHistoryUseCase
        )
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(analytics, state.analytics)
        assertEquals(weeklyTrend, state.weeklyTrend)
        assertEquals(dailyCalories, state.dailyCalories)
        assertEquals(history, state.sessionHistory)
        assertNull(state.error)
    }

    @Test
    fun `init sets error state when use cases fail`() = runTest {
        // Arrange
        whenever(getProgressAnalyticsUseCase(any(), any(), any())).doReturn(Result.Failure(mock()))
        whenever(getProgressChartDataUseCase.getWeeklySessionsTrend(any(), any())).doReturn(Result.Success(mock()))
        whenever(getProgressChartDataUseCase.getDailyCaloriesTrend(any(), any(), any())).doReturn(Result.Success(mock()))
        whenever(getSessionHistoryUseCase(any(), any())).doReturn(Result.Success(emptyList()))

        // Act
        viewModel = ProgressDashboardViewModel(
            getProgressAnalyticsUseCase,
            getProgressChartDataUseCase,
            getSessionHistoryUseCase
        )
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `init loads partial data when some use cases fail`() = runTest {
        // Arrange
        val analytics = ProgressAnalytics(5, 100, 2, 300)
        whenever(getProgressAnalyticsUseCase(any(), any(), any())).doReturn(Result.Success(analytics))
        whenever(getProgressChartDataUseCase.getWeeklySessionsTrend(any(), any())).doReturn(Result.Failure(mock()))
        whenever(getProgressChartDataUseCase.getDailyCaloriesTrend(any(), any(), any())).doReturn(Result.Success(mock()))
        whenever(getSessionHistoryUseCase(any(), any())).doReturn(Result.Success(emptyList()))

        // Act
        viewModel = ProgressDashboardViewModel(
            getProgressAnalyticsUseCase,
            getProgressChartDataUseCase,
            getSessionHistoryUseCase
        )
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(analytics, state.analytics)
        assertNotNull(state.error)
        assertEquals("Some data could not be loaded", state.error)
    }

    @Test
    fun `RefreshRequested event reloads data`() = runTest {
        // Arrange
        whenever(getProgressAnalyticsUseCase(any(), any(), any())).doReturn(Result.Success(mock()))
        whenever(getProgressChartDataUseCase.getWeeklySessionsTrend(any(), any())).doReturn(Result.Success(mock()))
        whenever(getProgressChartDataUseCase.getDailyCaloriesTrend(any(), any(), any())).doReturn(Result.Success(mock()))
        whenever(getSessionHistoryUseCase(any(), any())).doReturn(Result.Success(emptyList()))

        viewModel = ProgressDashboardViewModel(
            getProgressAnalyticsUseCase,
            getProgressChartDataUseCase,
            getSessionHistoryUseCase
        )
        advanceUntilIdle()

        // Act
        viewModel.onEvent(ProgressDashboardEvent.RefreshRequested)
        advanceUntilIdle()

        // Assert
        // Verify it was called twice (init + refresh)
        org.mockito.kotlin.verify(getProgressAnalyticsUseCase, org.mockito.kotlin.times(2)).invoke(any(), any(), any())
    }

    @Test
    fun `SessionClicked event emits NavigateToSessionDetail action`() = runTest {
        // Arrange
        whenever(getProgressAnalyticsUseCase(any(), any(), any())).doReturn(Result.Success(mock()))
        whenever(getProgressChartDataUseCase.getWeeklySessionsTrend(any(), any())).doReturn(Result.Success(mock()))
        whenever(getProgressChartDataUseCase.getDailyCaloriesTrend(any(), any(), any())).doReturn(Result.Success(mock()))
        whenever(getSessionHistoryUseCase(any(), any())).doReturn(Result.Success(emptyList()))

        viewModel = ProgressDashboardViewModel(
            getProgressAnalyticsUseCase,
            getProgressChartDataUseCase,
            getSessionHistoryUseCase
        )
        advanceUntilIdle()

        viewModel.action.test {
            // Act
            viewModel.onEvent(ProgressDashboardEvent.SessionClicked("s1"))
            
            // Assert
            val action = awaitItem()
            assertTrue(action is ProgressDashboardAction.NavigateToSessionDetail)
            assertEquals("s1", (action as ProgressDashboardAction.NavigateToSessionDetail).sessionId)
        }
    }
}
