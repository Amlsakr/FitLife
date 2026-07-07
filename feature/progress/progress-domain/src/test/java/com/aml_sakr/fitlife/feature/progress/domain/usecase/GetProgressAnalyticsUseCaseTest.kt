package com.aml_sakr.fitlife.feature.progress.domain.usecase

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.domain.usecase.CalculateCaloriesUseCase
import com.aml_sakr.fitlife.feature.progress.domain.repository.IProgressRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class GetProgressAnalyticsUseCaseTest {

    private lateinit var repository: IProgressRepository
    private lateinit var calculateCaloriesUseCase: CalculateCaloriesUseCase
    private lateinit var useCase: GetProgressAnalyticsUseCase

    @Before
    fun setUp() {
        repository = mock()
        calculateCaloriesUseCase = mock()
        useCase = GetProgressAnalyticsUseCase(repository, calculateCaloriesUseCase)
    }

    @Test
    fun `invoke returns success with correct analytics when repository calls succeed`() = runTest {
        // Arrange
        val userId = "user123"
        val startTime = 1000L
        val weight = 80.0
        whenever(repository.getSessionCount(userId, startTime)).doReturn(Result.Success(5))
        whenever(repository.getTotalFatigueEvents(userId, startTime)).doReturn(Result.Success(2))
        whenever(repository.getTotalDuration(userId, startTime)).doReturn(Result.Success(300))
        whenever(calculateCaloriesUseCase(eq(300), any(), eq(weight))).doReturn(150)

        // Act
        val result = useCase(userId, startTime, weight)

        // Assert
        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(5, data.totalSessions)
        assertEquals(2, data.totalFatigueEvents)
        assertEquals(60, data.averageDurationSeconds) // 300 / 5
        assertEquals(150, data.totalCalories)
        verify(calculateCaloriesUseCase).invoke(eq(300), any(), eq(weight))
    }

    @Test
    fun `invoke returns failure when any repository call fails`() = runTest {
        // Arrange
        val userId = "user123"
        val startTime = 1000L
        whenever(repository.getSessionCount(userId, startTime)).doReturn(Result.Failure(mock()))
        whenever(repository.getTotalFatigueEvents(userId, startTime)).doReturn(Result.Success(2))
        whenever(repository.getTotalDuration(userId, startTime)).doReturn(Result.Success(300))

        // Act
        val result = useCase(userId, startTime)

        // Assert
        assertTrue(result is Result.Failure)
    }
}
