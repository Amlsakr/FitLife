package com.aml_sakr.fitlife.feature.progress.domain.usecase

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.progress.domain.model.SessionBasicInfo
import com.aml_sakr.fitlife.feature.progress.domain.repository.IProgressRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetSessionHistoryUseCaseTest {

    private lateinit var repository: IProgressRepository
    private lateinit var useCase: GetSessionHistoryUseCase

    @Before
    fun setUp() {
        repository = mock()
        useCase = GetSessionHistoryUseCase(repository)
    }

    @Test
    fun `invoke returns success with session list when repository succeeds`() = runTest {
        // Arrange
        val userId = "user123"
        val limit = 10
        val sessions = listOf(
            SessionBasicInfo("s1", 1000L, 300),
            SessionBasicInfo("s2", 2000L, 600)
        )
        whenever(repository.getSessionHistory(userId, limit)).doReturn(Result.Success(sessions))

        // Act
        val result = useCase(userId, limit)

        // Assert
        assertTrue(result is Result.Success)
        assertEquals(sessions, (result as Result.Success).data)
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        // Arrange
        val userId = "user123"
        whenever(repository.getSessionHistory(userId, 20)).doReturn(Result.Failure(mock()))

        // Act
        val result = useCase(userId)

        // Assert
        assertTrue(result is Result.Failure)
    }
}
