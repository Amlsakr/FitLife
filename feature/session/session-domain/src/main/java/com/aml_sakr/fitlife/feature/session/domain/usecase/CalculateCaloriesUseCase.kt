package com.aml_sakr.fitlife.feature.session.domain.usecase

import javax.inject.Inject

class CalculateCaloriesUseCase @Inject constructor() {
    /**
     * Calculates estimated calories burned.
     * Formula: duration_minutes * MET_value * weight_kg / 60
     * @param durationSeconds Total workout duration in seconds
     * @param metValue Metabolic Equivalent of Task (default 6.0 for moderate intensity)
     * @param weightKg User weight in kg (default 70.0)
     */
    operator fun invoke(
        durationSeconds: Int,
        metValue: Double = 6.0,
        weightKg: Double = 70.0
    ): Int {
        val durationMinutes = durationSeconds / 60.0
        return (durationMinutes * metValue * weightKg / 60.0).toInt()
    }
}
