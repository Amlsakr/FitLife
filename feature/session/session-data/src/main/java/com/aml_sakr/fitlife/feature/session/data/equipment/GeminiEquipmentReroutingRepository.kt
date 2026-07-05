package com.aml_sakr.fitlife.feature.session.data.equipment

import android.content.Context
import com.aml_sakr.fitlife.core.domain.NetworkErrors
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.session.domain.equipment.ExerciseAlternative
import com.aml_sakr.fitlife.feature.session.domain.equipment.IEquipmentReroutingRepository
import com.aml_sakr.fitlife.feature.session.domain.model.ExerciseDifficulty
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject

class GeminiEquipmentReroutingRepository @Inject constructor(
    @ApplicationContext private val context: Context,
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
        try {
            val cached = dao.getAlternativesForExercise(exerciseName)
            if (cached != null && cached.expiresAt > System.currentTimeMillis()) {
                val type = object : TypeToken<List<ExerciseAlternative>>() {}.type
                val alternatives: List<ExerciseAlternative> = gson.fromJson(cached.alternativesJson, type)
                if (alternatives.isNotEmpty()) return Result.Success(alternatives)
            }
        } catch (e: Exception) {
            // Log error and continue to API
        }

        val request = promptBuilder.buildPrompt(exerciseName, equipment)
        val apiConfig = EquipmentGeminiConfiguration(
            modelName = config.modelName,
            apiVersion = config.apiVersion,
            timeoutMillis = 4000L // Keep internal timeout below use case 5s limit
        )

        var lastError: NetworkErrors = NetworkErrors.UnknownApiError
        var retryDelay = 500L // Reduced delay to fit within 5s budget

        repeat(2) { attempt -> // Reduced to 2 attempts (initial + 1 retry) to fit time budget
            val result = apiService.generateAlternatives(request, config.apiKey, apiConfig)
            if (result.httpStatusCode in 200..299) {
                if (result.responseBody.isBlank()) {
                    lastError = NetworkErrors.SerializationError
                } else {
                    val alternatives = parseAlternatives(result.responseBody)
                    if (alternatives.isNotEmpty()) {
                        cacheAlternatives(exerciseName, alternatives)
                        return Result.Success(alternatives)
                    } else {
                        lastError = NetworkErrors.SerializationError
                    }
                }
            } else {
                lastError = mapHttpError(result.httpStatusCode)
            }

            if (attempt < 1 && (lastError == NetworkErrors.ServerError || lastError == NetworkErrors.Timeout)) {
                delay(retryDelay)
                retryDelay *= 2
            } else if (lastError != NetworkErrors.ServerError && lastError != NetworkErrors.Timeout) {
                return@repeat // Don't retry client/auth errors
            }
        }

        // Final fallback: try expired cache, then local assets
        try {
            val cached = dao.getAlternativesForExercise(exerciseName)
            if (cached != null) {
                val type = object : TypeToken<List<ExerciseAlternative>>() {}.type
                val alternatives: List<ExerciseAlternative> = gson.fromJson(cached.alternativesJson, type)
                if (alternatives.isNotEmpty()) return Result.Success(alternatives)
            }
        } catch (e: Exception) { }

        val localFallback = getLocalFallback(exerciseName)
        if (localFallback.isNotEmpty()) {
            return Result.Success(localFallback)
        }

        return Result.Failure(lastError)
    }

    private fun getLocalFallback(exerciseName: String): List<ExerciseAlternative> {
        return try {
            val json = context.assets.open("fallback_equipment_alternatives.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<FallbackExerciseGroup>>() {}.type
            val groups: List<FallbackExerciseGroup> = gson.fromJson(json, type)
            val group = groups.find { it.exerciseName.equals(exerciseName, ignoreCase = true) }
            group?.alternatives?.map { draft ->
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
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseAlternatives(json: String): List<ExerciseAlternative> {
        return try {
            val response = gson.fromJson(json, GeminiAlternativesResponse::class.java)
            response.alternatives?.map { draft ->
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
            } ?: emptyList()
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
