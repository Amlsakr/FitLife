package com.aml_sakr.fitlife.feature.workout.domain.error

sealed class WorkoutGenerationError(
    override val code: String,
    override val message: String
) : com.aml_sakr.fitlife.core.domain.error.WorkoutGenerationError {
    data object RemoteTimeout : WorkoutGenerationError(
        code = "workout_remote_timeout",
        message = "The workout plan request timed out."
    )

    data object RemoteRateLimited : WorkoutGenerationError(
        code = "workout_remote_rate_limited",
        message = "The workout plan request was rate limited."
    )

    data object RemoteHttpError : WorkoutGenerationError(
        code = "workout_remote_http_error",
        message = "The workout plan service returned an HTTP error."
    )

    data object RemoteNetworkError : WorkoutGenerationError(
        code = "workout_remote_network_error",
        message = "The workout plan request could not reach the service."
    )

    data object RemoteParseError : WorkoutGenerationError(
        code = "workout_remote_parse_error",
        message = "The workout plan response could not be parsed."
    )

    data object NoMatchingFallbackTemplate : WorkoutGenerationError(
        code = "workout_no_matching_fallback_template",
        message = "No fallback workout template matched the current profile."
    )

    data object FallbackAssetUnavailable : WorkoutGenerationError(
        code = "workout_fallback_asset_unavailable",
        message = "The fallback workout asset could not be loaded."
    )

    data object CacheUnavailable : WorkoutGenerationError(
        code = "workout_cache_unavailable",
        message = "The cached workout plan could not be read."
    )

    data object PersistenceFailed : WorkoutGenerationError(
        code = "workout_persistence_failed",
        message = "The workout plan could not be saved."
    )
}
