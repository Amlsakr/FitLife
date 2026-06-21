package com.aml_sakr.fitlife.feature.workout.data.gemini

import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkConfiguration

data class WorkoutGeminiGatewayConfiguration(
    val apiKey: String = configuredValue("FITLIFE_GEMINI_API_KEY")
        ?: configuredValue("GEMINI_API_KEY")
        ?: "",
    val modelName: String = configuredValue("FITLIFE_GEMINI_MODEL")
        ?: configuredValue("GEMINI_MODEL_NAME")
        ?: "",
    val apiVersion: String = "v1beta",
    val responseMimeType: String = "application/json",
    val temperature: Double = 0.2,
    val maxOutputTokens: Int = 4_096,
    val timeoutMillis: Long = 5_000L,
    val maxRetries: Int = 2,
    val backoffMillis: Long = 250L
) {
    fun toBenchmarkConfiguration(): GeminiBenchmarkConfiguration {
        val configuredModelName = validatedModelName()
        return GeminiBenchmarkConfiguration(
            modelName = configuredModelName,
            apiVersion = apiVersion,
            endpoint = "$apiVersion/models/${configuredModelName.removePrefix("models/")}:generateContent",
            responseMimeType = responseMimeType,
            temperature = temperature,
            maxOutputTokens = maxOutputTokens,
            timeoutMillis = timeoutMillis,
            maxRetries = maxRetries
        )
    }

    private fun validatedModelName(): String = requireNotBlank(
        modelName,
        "Workout Gemini model name is not configured."
    )
}

private fun configuredValue(name: String): String? =
    System.getProperty(name)?.takeIf { it.isNotBlank() }
        ?: System.getenv(name)?.takeIf { it.isNotBlank() }

private fun requireNotBlank(value: String, message: String): String =
    value.takeIf { it.isNotBlank() } ?: error(message)
