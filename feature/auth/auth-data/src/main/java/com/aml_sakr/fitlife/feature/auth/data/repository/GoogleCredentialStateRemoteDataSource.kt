package com.aml_sakr.fitlife.feature.auth.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class GoogleCredentialStateRemoteDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) : GoogleCredentialStateDataSource {
    private val credentialManager by lazy { CredentialManager.create(context) }

    override suspend fun clearCredentialState() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}
