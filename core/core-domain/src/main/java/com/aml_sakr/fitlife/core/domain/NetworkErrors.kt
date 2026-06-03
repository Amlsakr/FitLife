package com.aml_sakr.fitlife.core.domain

sealed class NetworkErrors(
    override val code: String,
    override val message: String
) : DomainError {
    data object NoConnection : NetworkErrors(
        code = "network_no_connection",
        message = "No network connection."
    )

    data object Timeout : NetworkErrors(
        code = "network_timeout",
        message = "Request timed out."
    )

    data object Unauthorized : NetworkErrors(
        code = "network_unauthorized",
        message = "Authentication is required."
    )

    data object ServerError : NetworkErrors(
        code = "network_server_error",
        message = "Server error."
    )

    data object SerializationError : NetworkErrors(
        code = "network_serialization_error",
        message = "Response could not be parsed."
    )

    data object UnknownApiError : NetworkErrors(
        code = "network_unknown_api_error",
        message = "Unknown API error."
    )
}
