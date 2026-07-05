package com.aml_sakr.fitlife.feature.session.data.equipment

data class SessionGeminiConfiguration(
    val apiKey: String,
    val modelName: String = "models/gemini-1.5-flash",
    val apiVersion: String = "v1beta"
) {
    init {
        require(apiKey.isNotBlank()) {
            "Gemini API Key is missing. Please add FITLIFE_GEMINI_API_KEY to your local.properties or environment variables."
        }
    }
}
