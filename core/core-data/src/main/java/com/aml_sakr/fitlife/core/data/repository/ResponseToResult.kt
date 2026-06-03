package com.aml_sakr.fitlife.core.data.repository

import com.aml_sakr.fitlife.core.domain.NetworkErrors
import com.aml_sakr.fitlife.core.domain.Result

data class NetworkResponse<T>(
    val statusCode: Int,
    val body: T?
)

fun <T> NetworkResponse<T>.toResult(): Result<T, NetworkErrors> =
    when {
        statusCode in 200..299 && body != null -> Result.Success(body)
        statusCode == 401 || statusCode == 403 -> Result.Failure(NetworkErrors.Unauthorized)
        statusCode in 500..599 -> Result.Failure(NetworkErrors.ServerError)
        else -> Result.Failure(NetworkErrors.UnknownApiError)
    }

fun <T> NetworkResponse<T>.toResult(emptyBody: T): Result<T, NetworkErrors> =
    copy(body = body ?: emptyBody).toResult()
