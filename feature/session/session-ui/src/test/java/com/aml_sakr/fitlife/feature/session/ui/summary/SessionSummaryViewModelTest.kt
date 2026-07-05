package com.aml_sakr.fitlife.feature.session.ui.summary

import com.aml_sakr.fitlife.core.domain.AnalyticsLogger
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.session.domain.model.Session
import com.aml_sakr.fitlife.feature.session.domain.usecase.GetSessionUseCase
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionSummaryViewModelTest {

    private val getSessionUseCase = mock<GetSessionUseCase>()
    private val analyticsLogger = mock<AnalyticsLogger>()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: SessionSummaryViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SessionSummaryViewModel(getSessionUseCase, analyticsLogger)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `LoadSession successfully updates state`() = runTest {
        val session = Session(
            sessionId = "id",
            userId = "u",
            planId = "p",
            workoutDayId = "d",
            startTime = 1000,
            endTime = 2000,
            durationSeconds = 60,
            totalReps = 10,
            totalSets = 1,
            fatigueEventCount = 0,
            audioFallbackUsed = false,
            completionPercentage = 1f,
            whatsAppShared = false
        )
        whenever(getSessionUseCase.invoke(any())).thenReturn(Result.Success(session))

        viewModel.onEvent(SessionSummaryEvent.LoadSession("id"))
        advanceUntilIdle()

        assertEquals(session, viewModel.state.value.session)
        assertEquals(7, viewModel.state.value.caloriesBurned)
    }
}
