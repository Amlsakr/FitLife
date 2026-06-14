package com.aml_sakr.fitlife.feature.auth.domain.model

data class AuthUser(
    val id: String,
    val email: String?,
    val isEmailVerified: Boolean
)
