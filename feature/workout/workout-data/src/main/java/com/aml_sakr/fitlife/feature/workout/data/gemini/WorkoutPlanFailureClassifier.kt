package com.aml_sakr.fitlife.feature.workout.data.gemini

import com.aml_sakr.fitlife.feature.workout.domain.error.WorkoutGenerationError
import java.net.SocketTimeoutException

enum class WorkoutRemoteFailureCategory {
    Timeout,
    RateLimited,
    HttpError,
    NetworkError,
    ParseError
}

class WorkoutPlanFailureClassifier {
    fun classifyHttpStatus(statusCode: Int): WorkoutGenerationError {
        return when (statusCode) {
            429 -> WorkoutGenerationError.RemoteRateLimited
            in 500..599 -> WorkoutGenerationError.RemoteHttpError
            in 400..499 -> WorkoutGenerationError.RemoteHttpError
            else -> WorkoutGenerationError.RemoteHttpError
        }
    }

    fun classifyThrowable(throwable: Throwable): WorkoutGenerationError {
        return when (throwable) {
            is SocketTimeoutException -> WorkoutGenerationError.RemoteTimeout
            else -> WorkoutGenerationError.RemoteNetworkError
        }
    }

    fun classifyParseValidity(isValidPlan: Boolean): WorkoutGenerationError? =
        if (isValidPlan) null else WorkoutGenerationError.RemoteParseError
}
