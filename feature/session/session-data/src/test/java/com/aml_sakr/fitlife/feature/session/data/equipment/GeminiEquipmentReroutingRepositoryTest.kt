package com.aml_sakr.fitlife.feature.session.data.equipment

import com.aml_sakr.fitlife.core.domain.NetworkErrors
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.session.domain.equipment.ExerciseAlternative
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GeminiEquipmentReroutingRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: GeminiEquipmentReroutingRepository
    private val dao: EquipmentReroutingDao = mock()
    private val gson = Gson()
    private val promptBuilder = EquipmentReroutingPromptBuilder(gson)
    private val config = SessionGeminiConfiguration("test-key")

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        val apiService = HttpEquipmentGeminiApiService(gson, server.url("/").toString().removeSuffix("/"))
        repository = GeminiEquipmentReroutingRepository(
            apiService,
            promptBuilder,
            dao,
            config,
            gson
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `fetchAlternatives should return success when API returns valid response`() = runTest {
        val responseJson = """
            {
              "alternatives": [
                {
                  "name": "Goblet Squat",
                  "description": "Desc",
                  "equipment_required": "Dumbbell",
                  "muscle_groups": ["Quads"],
                  "difficulty": "INTERMEDIATE"
                }
              ]
            }
        """.trimIndent()
        
        server.enqueue(MockResponse().setResponseCode(200).setBody(responseJson))
        whenever(dao.getAlternativesForExercise(any())).doReturn(null)

        val result = repository.fetchAlternatives("Barbell Squat", setOf("Dumbbell"))

        assertTrue(result is Result.Success)
        val alternatives = (result as Result.Success).value
        assertEquals(1, alternatives.size)
        assertEquals("Goblet Squat", alternatives.first().name)
    }

    @Test
    fun `fetchAlternatives should return server error when API fails`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        whenever(dao.getAlternativesForExercise(any())).doReturn(null)

        val result = repository.fetchAlternatives("Barbell Squat", setOf("Dumbbell"))

        assertTrue(result is Result.Failure)
        assertEquals(NetworkErrors.ServerError, (result as Result.Failure).error)
    }
}
