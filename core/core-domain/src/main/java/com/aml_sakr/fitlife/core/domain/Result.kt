package com.aml_sakr.fitlife.core.domain

sealed interface Result<out T, out E : DomainError> {
    data class Success<T>(val value: T) : Result<T, Nothing> {
        val data: T = value
    }

    data class Failure<E : DomainError>(val error: E) : Result<Nothing, E>
}
