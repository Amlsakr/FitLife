package com.aml_sakr.fitlife.feature.workout.data.fallback

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutGenerationRequest

class WorkoutPlanFallbackSelector(
    private val logger: WorkoutPlanFallbackLogger = NoOpWorkoutPlanFallbackLogger
) {
    fun select(
        request: WorkoutGenerationRequest,
        templates: List<WorkoutPlanFallbackTemplate>
    ): Result<WorkoutPlanFallbackTemplate, WorkoutGenerationError> {
        val normalizedEquipment = request.availableEquipment.map { it.normalizedEquipmentName() }.toSet()
        val candidates = templates.mapNotNull { template ->
            if (template.fitnessLevel != request.fitnessLevel) {
                null
            } else {
                template.matchScore(request, normalizedEquipment)?.let { score ->
                    ScoredTemplate(template, score)
                }
            }
        }

        val bestMatch = candidates.maxWithOrNull(compareBy<ScoredTemplate> { it.score }
            .thenByDescending { it.template.requiredEquipment.size }
            .thenByDescending { if (it.template.location == request.location) 1 else 0 })
            ?.template

        return if (bestMatch != null) {
            logger.onFallbackSelected(request, bestMatch.id)
            Result.Success(bestMatch)
        } else {
            Result.Failure(WorkoutGenerationError.NoMatchingFallbackTemplate)
        }
    }

    private fun WorkoutPlanFallbackTemplate.matchScore(
        request: WorkoutGenerationRequest,
        normalizedEquipment: Set<String>
    ): Int? {
        if (requiredEquipment.any { equipment ->
                !normalizedEquipment.contains(equipment.normalizedEquipmentName())
            }) {
            return null
        }

        val matchedEquipmentCount = requiredEquipment.count { equipment ->
            normalizedEquipment.contains(equipment.normalizedEquipmentName())
        }
        val locationScore = if (location == request.location) 200 else 0
        val equipmentScore = matchedEquipmentCount * 25
        return locationScore + equipmentScore
    }

    private fun String.normalizedEquipmentName(): String =
        lowercase().replace(Regex("[^a-z0-9]+"), "")
}

private data class ScoredTemplate(
    val template: WorkoutPlanFallbackTemplate,
    val score: Int
)
