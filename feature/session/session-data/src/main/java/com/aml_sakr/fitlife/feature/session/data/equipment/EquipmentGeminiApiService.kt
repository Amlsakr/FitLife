package com.aml_sakr.fitlife.feature.session.data.equipment

interface EquipmentGeminiApiService {
    suspend fun generateAlternatives(
        request: EquipmentGeminiRequest,
        apiKey: String,
        configuration: EquipmentGeminiConfiguration
    ): EquipmentGeminiApiCallResult
}
