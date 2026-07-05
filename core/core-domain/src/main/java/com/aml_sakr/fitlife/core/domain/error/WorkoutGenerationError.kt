package com.aml_sakr.fitlife.core.domain.error

import com.aml_sakr.fitlife.core.domain.DomainError

interface WorkoutGenerationError : DomainError {
    override val code: String get() = this::class.simpleName ?: "UNKNOWN_ERROR"
    override val message: String get() = code

    data object RemoteTimeout : WorkoutGenerationError
    data object RemoteNetworkError : WorkoutGenerationError
    data object RemoteHttpError : WorkoutGenerationError
    data object RemoteRateLimited : WorkoutGenerationError
    data object RemoteParseError : WorkoutGenerationError
    data object CacheUnavailable : WorkoutGenerationError
    data object PersistenceFailed : WorkoutGenerationError
    data object NoMatchingFallbackTemplate : WorkoutGenerationError
    data object FallbackAssetUnavailable : WorkoutGenerationError
}
