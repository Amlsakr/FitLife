package com.aml_sakr.fitlife.feature.session.domain.usecase

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.session.domain.model.Session
import com.aml_sakr.fitlife.feature.session.domain.repository.ISessionRepository
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveSessionUseCaseTest {

    private val repository = mock<ISessionRepository>()
    private val useCase = SaveSessionUseCase(repository)

    @Test
    fun `invoke calls saveSession on repository`() = runTest {
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
        whenever(repository.saveSession(session)).thenReturn(Result.Success(Unit))

        val result = useCase(session)

        assertTrue(result is Result.Success)
    }
}
