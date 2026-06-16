package com.aml_sakr.fitlife.feature.auth.data.repository

internal interface FirebaseUserDocumentDataSource {
    suspend fun upsertAuthenticatedUser(user: FirebaseAuthUserData)
}
