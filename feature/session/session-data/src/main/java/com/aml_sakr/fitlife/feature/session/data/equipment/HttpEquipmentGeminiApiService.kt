package com.aml_sakr.fitlife.feature.session.data.equipment

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class HttpEquipmentGeminiApiService @Inject constructor(
    private val gson: Gson,
    private val baseUrl: String = "https://generativelanguage.googleapis.com"
) : EquipmentGeminiApiService {
    override suspend fun generateAlternatives(
        request: EquipmentGeminiRequest,
        apiKey: String,
        configuration: EquipmentGeminiConfiguration
    ): EquipmentGeminiApiCallResult = withContext(Dispatchers.IO) {
        val modelPath = configuration.modelName.removePrefix("models/")
        val url = URL("$baseUrl/${configuration.apiVersion}/models/$modelPath:generateContent")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = configuration.timeoutMillis.toInt()
            readTimeout = configuration.timeoutMillis.toInt()
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("x-goog-api-key", apiKey)
        }

        try {
            val body = gson.toJson(request).toByteArray(Charsets.UTF_8)
            connection.outputStream.use { it.write(body) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() } ?: ""
            EquipmentGeminiApiCallResult(
                httpStatusCode = status,
                responseBody = response,
                responseSizeChars = response.length
            )
        } catch (e: Exception) {
            EquipmentGeminiApiCallResult(
                httpStatusCode = -1,
                responseBody = e.message ?: "Unknown Error",
                responseSizeChars = 0
            )
        } finally {
            connection.disconnect()
        }
    }
}
