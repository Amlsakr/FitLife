package com.aml_sakr.fitlife.feature.workout.data.fallback

import com.google.gson.Gson
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.workout.data.gemini.WorkoutPlanMapper
import com.aml_sakr.fitlife.feature.workout.domain.WorkoutPlanDefaults
import com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutGenerationRequest
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutPlan

class WorkoutPlanFallbackSource(
    private val assetReader: WorkoutPlanAssetReader,
    private val selector: WorkoutPlanFallbackSelector = WorkoutPlanFallbackSelector(),
    private val mapper: WorkoutPlanMapper = WorkoutPlanMapper(),
    private val gson: Gson = Gson(),
    private val assetPath: String = DEFAULT_ASSET_PATH
) {
    fun load(
        request: WorkoutGenerationRequest,
        generatedAtEpochMillis: Long
    ): Result<WorkoutPlan, WorkoutGenerationError> {
        val catalog = runCatching {
            assetReader.open(assetPath).use { input ->
                input.bufferedReader().use { reader ->
                    gson.fromJson(reader, WorkoutPlanFallbackCatalog::class.java)
                }
            }
        }.getOrElse {
            return Result.Failure(WorkoutGenerationError.FallbackAssetUnavailable)
        } ?: return Result.Failure(WorkoutGenerationError.FallbackAssetUnavailable)

        val templates = catalog.templates
            ?: return Result.Failure(WorkoutGenerationError.FallbackAssetUnavailable)
        val validTemplates = templates.filter { it.isStructurallyValid() }

        if (templates.isNotEmpty() && validTemplates.isEmpty()) {
            return Result.Failure(WorkoutGenerationError.FallbackAssetUnavailable)
        }

        val selected = when (val selection = selector.select(request, validTemplates)) {
            is Result.Success -> selection.data
            is Result.Failure -> return selection
        }

        return runCatching {
            Result.Success(
                mapper.fromFallbackTemplate(
                    request = request,
                    template = selected,
                    generatedAtEpochMillis = generatedAtEpochMillis
                )
            )
        }.getOrElse {
            Result.Failure(WorkoutGenerationError.FallbackAssetUnavailable)
        }
    }

    companion object {
        const val DEFAULT_ASSET_PATH = "fallback_workout_plans.json"
    }
}

private fun WorkoutPlanFallbackTemplate.isStructurallyValid(): Boolean =
    runCatching {
        id.isNotBlank() &&
            requiredEquipment.all { it.isNotBlank() } &&
            days.size == WorkoutPlanDefaults.DefaultRequestedDays &&
            days.map { it.day }.sorted() == (1..WorkoutPlanDefaults.DefaultRequestedDays).toList() &&
            days.all { it.isStructurallyValid() }
    }.getOrDefault(false)

private fun WorkoutPlanFallbackDay.isStructurallyValid(): Boolean =
    runCatching {
        title.isNotBlank() &&
            durationMinutes > 0 &&
            exercises.isNotEmpty() &&
            exercises.all { it.isStructurallyValid() }
    }.getOrDefault(false)

private fun WorkoutPlanFallbackExercise.isStructurallyValid(): Boolean =
    runCatching {
        name.isNotBlank() &&
            sets > 0 &&
            reps.isNotBlank() &&
            estimatedDurationMinutes > 0
    }.getOrDefault(false)
