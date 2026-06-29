package com.aml_sakr.fitlife.feature.session.data.equipment

data class SessionGeminiConfiguration(
    val apiKey: String,
    val modelName: String = "models/gemini-1.5-flash",
    val apiVersion: String = "v1beta"
)
