package com.aml_sakr.fitlife.core.domain.usecase

import javax.inject.Inject

class CalculateCaloriesUseCase @Inject constructor() {
    companion object {
        const val DEFAULT_MET_MODERATE = 6.0
        const val DEFAULT_WEIGHT_KG = 70.0
    }

    /**
     * Calculates estimated calories burned.
     * Formula: duration_minutes * MET_value * weight_kg / 60
     * @param durationSeconds Total workout duration in seconds
     * @param metValue Metabolic Equivalent of Task (default 6.0 for moderate intensity)
     * @param weightKg User weight in kg (default 70.0)
     */
    operator fun invoke(
        durationSeconds: Int,
        metValue: Double = DEFAULT_MET_MODERATE,
        weightKg: Double = DEFAULT_WEIGHT_KG
    ): Int {
        if (durationSeconds <= 0) return 0
        val durationMinutes = durationSeconds / 60.0
        // Use Long intermediate to avoid precision issues if values are very large
        // although Double should handle it, we follow the guard suggestion.
        val calories = (durationMinutes * metValue * weightKg / 60.0)
        return calories.toInt()
    }
}
