package com.aml_sakr.fitlife.feature.session.domain.equipment

import com.aml_sakr.fitlife.core.domain.NetworkErrors
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.session.domain.model.ExerciseDifficulty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RerouteEquipmentUseCaseTest {

    private val repository: IEquipmentReroutingRepository = mock()
    private lateinit var useCase: RerouteEquipmentUseCase

    @Before
    fun setup() {
        useCase = RerouteEquipmentUseCase(repository)
    }

    @Test
    fun `invoke should return success when repository returns alternatives`() = runTest {
        val alternatives = listOf(
            ExerciseAlternative("1", "Goblet Squat", "Desc", listOf("Quads"), "Dumbbell", ExerciseDifficulty.INTERMEDIATE, null, 3, 12)
        )
        whenever(repository.fetchAlternatives(any(), any())).doReturn(Result.Success(alternatives))

        val result = useCase("Barbell Squat", setOf("Dumbbell"))

        assertTrue(result is Result.Success)
        assertEquals(alternatives, (result as Result.Success<List<ExerciseAlternative>>).value)
    }

    @Test
    fun `invoke should return timeout error when repository takes longer than 5 seconds`() = runTest {
        whenever(repository.fetchAlternatives(any(), any())).doSuspendAnswer {
            delay(6000)
            Result.Success(emptyList<ExerciseAlternative>())
        }

        val result = useCase("Barbell Squat", setOf("Dumbbell"))

        assertTrue(result is Result.Failure)
        assertEquals(NetworkErrors.Timeout, (result as Result.Failure).error)
    }

    @Test
    fun `invoke should return unknown error when repository fails`() = runTest {
        whenever(repository.fetchAlternatives(any(), any())).doAnswer {
            throw RuntimeException("Boom")
        }

        val result = useCase("Barbell Squat", setOf("Dumbbell"))

        assertTrue(result is Result.Failure)
        assertEquals(NetworkErrors.UnknownApiError, (result as Result.Failure).error)
    }
}
