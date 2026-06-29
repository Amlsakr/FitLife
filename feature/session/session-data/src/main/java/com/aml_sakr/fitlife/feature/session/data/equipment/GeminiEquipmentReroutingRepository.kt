package com.aml_sakr.fitlife.feature.session.data.equipment

import com.aml_sakr.fitlife.core.domain.NetworkErrors
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.session.domain.equipment.ExerciseAlternative
import com.aml_sakr.fitlife.feature.session.domain.equipment.IEquipmentReroutingRepository
import com.aml_sakr.fitlife.feature.session.domain.model.ExerciseDifficulty
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject

class GeminiEquipmentReroutingRepository @Inject constructor(
    private val apiService: EquipmentGeminiApiService,
    private val promptBuilder: EquipmentReroutingPromptBuilder,
    private val dao: EquipmentReroutingDao,
    private val config: SessionGeminiConfiguration,
    private val gson: Gson
) : IEquipmentReroutingRepository {

    override suspend fun fetchAlternatives(
        exerciseName: String,
        equipment: Set<String>
    ): Result<List<ExerciseAlternative>, NetworkErrors> {
        // Cache-first check
        val cached = dao.getAlternativesForExercise(exerciseName)
        if (cached != null && cached.expiresAt > System.currentTimeMillis()) {
            val type = object : TypeToken<List<ExerciseAlternative>>() {}.type
            val alternatives: List<ExerciseAlternative> = gson.fromJson(cached.alternativesJson, type)
            return Result.Success(alternatives)
        }

        val request = promptBuilder.buildPrompt(exerciseName, equipment)
        val apiConfig = EquipmentGeminiConfiguration(
            modelName = config.modelName,
            apiVersion = config.apiVersion,
            timeoutMillis = 5000L
        )

        var lastError: NetworkErrors = NetworkErrors.UnknownApiError
        var retryDelay = 1000L

        repeat(3) { attempt ->
            val result = apiService.generateAlternatives(request, config.apiKey, apiConfig)
            if (result.httpStatusCode in 200..299) {
                val alternatives = parseAlternatives(result.responseBody)
                cacheAlternatives(exerciseName, alternatives)
                return Result.Success(alternatives)
            } else {
                lastError = mapHttpError(result.httpStatusCode)
                if (attempt < 2) {
                    delay(retryDelay)
                    retryDelay *= 2
                }
            }
        }

        // Final fallback: try expired cache
        if (cached != null) {
            val type = object : TypeToken<List<ExerciseAlternative>>() {}.type
            val alternatives: List<ExerciseAlternative> = gson.fromJson(cached.alternativesJson, type)
            return Result.Success(alternatives)
        }

        return Result.Failure(lastError)
    }

    private fun parseAlternatives(json: String): List<ExerciseAlternative> {
        return try {
            val response = gson.fromJson(json, GeminiAlternativesResponse::class.java)
            response.alternatives.map { draft ->
                ExerciseAlternative(
                    exerciseId = UUID.randomUUID().toString(),
                    name = draft.name,
                    description = draft.description,
                    muscleGroups = draft.muscle_groups,
                    equipmentRequired = draft.equipment_required,
                    difficulty = mapDifficulty(draft.difficulty),
                    lottieAssetPath = null,
                    defaultSets = 3,
                    defaultReps = 12
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun mapDifficulty(difficulty: String): ExerciseDifficulty {
        return when (difficulty.uppercase()) {
            "BEGINNER" -> ExerciseDifficulty.BEGINNER
            "ADVANCED" -> ExerciseDifficulty.ADVANCED
            else -> ExerciseDifficulty.INTERMEDIATE
        }
    }

    private suspend fun cacheAlternatives(exerciseName: String, alternatives: List<ExerciseAlternative>) {
        val json = gson.toJson(alternatives)
        val now = System.currentTimeMillis()
        val entity = EquipmentReroutingEntity(
            exerciseName = exerciseName,
            alternativesJson = json,
            fetchedAt = now,
            expiresAt = now + 24 * 60 * 60 * 1000L
        )
        dao.insertAlternatives(entity)
    }

    private fun mapHttpError(statusCode: Int): NetworkErrors {
        return when (statusCode) {
            401, 403 -> NetworkErrors.Unauthorized
            500, 502, 503, 504 -> NetworkErrors.ServerError
            -1 -> NetworkErrors.Timeout
            else -> NetworkErrors.UnknownApiError
        }
    }
}
