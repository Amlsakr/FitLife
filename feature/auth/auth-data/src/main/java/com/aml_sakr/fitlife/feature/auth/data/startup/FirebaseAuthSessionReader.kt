package com.aml_sakr.fitlife.feature.auth.data.startup

import com.aml_sakr.fitlife.feature.auth.data.repository.FirebaseAuthDataSource
import com.aml_sakr.fitlife.feature.auth.domain.startup.AuthSession
import com.aml_sakr.fitlife.feature.auth.domain.startup.AuthSessionReader
import javax.inject.Inject

class FirebaseAuthSessionReader @Inject internal constructor(
    private val dataSource: FirebaseAuthDataSource
) : AuthSessionReader {
    override suspend fun currentSession(): AuthSession? =
        dataSource.currentUser()?.let { user ->
            AuthSession(
                userId = user.id,
                isEmailVerified = user.isEmailVerified
            )
        }
}
