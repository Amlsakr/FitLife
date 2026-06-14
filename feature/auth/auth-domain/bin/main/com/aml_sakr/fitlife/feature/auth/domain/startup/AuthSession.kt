package com.aml_sakr.fitlife.feature.auth.domain.startup

data class AuthSession(
    val userId: String,
    val isEmailVerified: Boolean
)
