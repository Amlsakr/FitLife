package com.aml_sakr.fitlife.feature.auth.data.repository

internal interface GoogleCredentialStateDataSource {
    suspend fun clearCredentialState()
}
