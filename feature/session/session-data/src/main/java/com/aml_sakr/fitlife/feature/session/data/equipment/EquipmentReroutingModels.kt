package com.aml_sakr.fitlife.feature.session.data.equipment

data class EquipmentGeminiRequest(
    val contents: List<EquipmentGeminiContent>,
    val generationConfig: EquipmentGeminiGenerationConfig
)

data class EquipmentGeminiContent(
    val parts: List<EquipmentGeminiPart>
)

data class EquipmentGeminiPart(
    val text: String
)

data class EquipmentGeminiGenerationConfig(
    val responseMimeType: String,
    val responseSchema: EquipmentGeminiResponseSchema,
    val temperature: Double,
    val maxOutputTokens: Int
)

data class EquipmentGeminiResponseSchema(
    val type: String,
    val properties: Map<String, EquipmentGeminiResponseSchema> = emptyMap(),
    val items: EquipmentGeminiResponseSchema? = null,
    val required: List<String> = emptyList()
)

data class EquipmentGeminiApiCallResult(
    val httpStatusCode: Int,
    val responseBody: String,
    val responseSizeChars: Int
)

data class EquipmentGeminiConfiguration(
    val modelName: String,
    val apiVersion: String,
    val timeoutMillis: Long,
    val maxRetries: Int = 3,
    val backoffMillis: Long = 1000L
)

data class GeminiAlternativesResponse(
    val alternatives: List<GeminiAlternativeDraft>
)

data class GeminiAlternativeDraft(
    val name: String,
    val description: String,
    val equipment_required: String,
    val muscle_groups: List<String>,
    val difficulty: String
)
