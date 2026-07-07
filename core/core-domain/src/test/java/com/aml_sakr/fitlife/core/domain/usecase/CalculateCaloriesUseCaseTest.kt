package com.aml_sakr.fitlife.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateCaloriesUseCaseTest {

    private val useCase = CalculateCaloriesUseCase()

    @Test
    fun `invoke returns correct calories for given duration`() {
        // Formula: duration_minutes * MET_value * weight_kg / 60
        // For 3600s (60 min), MET 6.0, weight 70kg:
        // 60 * 6.0 * 70 / 60 = 420
        val result = useCase(3600, 6.0, 70.0)
        assertEquals(420, result)
    }

    @Test
    fun `invoke returns 0 for 0 duration`() {
        val result = useCase(0)
        assertEquals(0, result)
    }

    @Test
    fun `invoke handles default values correctly`() {
        // Default: MET 6.0, weight 70.0
        // For 600s (10 min): 10 * 6.0 * 70 / 60 = 70
        val result = useCase(600)
        assertEquals(70, result)
    }
}
