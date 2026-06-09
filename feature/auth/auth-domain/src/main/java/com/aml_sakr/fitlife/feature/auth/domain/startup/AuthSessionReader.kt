package com.aml_sakr.fitlife.feature.auth.domain.startup

interface AuthSessionReader {
    suspend fun currentSession(): AuthSession?
}
