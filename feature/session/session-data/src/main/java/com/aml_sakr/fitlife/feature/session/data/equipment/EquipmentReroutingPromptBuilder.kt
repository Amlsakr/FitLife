package com.aml_sakr.fitlife.feature.session.data.equipment

import com.google.gson.Gson
import javax.inject.Inject

class EquipmentReroutingPromptBuilder @Inject constructor(
    private val gson: Gson
) {
    fun buildPrompt(
        exerciseName: String,
        availableEquipment: Set<String>
    ): EquipmentGeminiRequest {
        val promptText = """
            Suggest 3 alternative exercises for '$exerciseName' that target similar muscle groups 
            using only the following available equipment: ${availableEquipment.joinToString(", ")}.
            Provide the response in JSON format.
        """.trimIndent()

        return EquipmentGeminiRequest(
            contents = listOf(
                EquipmentGeminiContent(
                    parts = listOf(EquipmentGeminiPart(text = promptText))
                )
            ),
            generationConfig = EquipmentGeminiGenerationConfig(
                responseMimeType = "application/json",
                responseSchema = buildResponseSchema(),
                temperature = 0.2,
                maxOutputTokens = 1024
            )
        )
    }

    private fun buildResponseSchema(): EquipmentGeminiResponseSchema {
        return EquipmentGeminiResponseSchema(
            type = "OBJECT",
            properties = mapOf(
                "alternatives" to EquipmentGeminiResponseSchema(
                    type = "ARRAY",
                    items = EquipmentGeminiResponseSchema(
                        type = "OBJECT",
                        properties = mapOf(
                            "name" to EquipmentGeminiResponseSchema(type = "STRING"),
                            "description" to EquipmentGeminiResponseSchema(type = "STRING"),
                            "equipment_required" to EquipmentGeminiResponseSchema(type = "STRING"),
                            "muscle_groups" to EquipmentGeminiResponseSchema(
                                type = "ARRAY",
                                items = EquipmentGeminiResponseSchema(type = "STRING")
                            ),
                            "difficulty" to EquipmentGeminiResponseSchema(type = "STRING")
                        ),
                        required = listOf("name", "description", "equipment_required", "muscle_groups", "difficulty")
                    )
                )
            ),
            required = listOf("alternatives")
        )
    }
}
