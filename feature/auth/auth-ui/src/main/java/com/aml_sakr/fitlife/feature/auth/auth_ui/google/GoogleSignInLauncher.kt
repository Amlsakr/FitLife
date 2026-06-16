package com.aml_sakr.fitlife.feature.auth.auth_ui.google

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

sealed interface GoogleSignInResult {
    data class Token(val idToken: String) : GoogleSignInResult
    data object Cancelled : GoogleSignInResult
    data class Failed(val error: AuthError) : GoogleSignInResult
}

interface GoogleSignInLauncher {
    suspend fun launch(context: Context, googleClientId: String): GoogleSignInResult
}

object DefaultGoogleSignInLauncher : GoogleSignInLauncher {
    override suspend fun launch(
        context: Context,
        googleClientId: String
    ): GoogleSignInResult {
        if (googleClientId.isBlank()) {
            return GoogleSignInResult.Failed(AuthError.GoogleSignInFailed)
        }

        val credentialManager = CredentialManager.create(context)
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetSignInWithGoogleOption.Builder(serverClientId = googleClientId).build()
            )
            .build()

        return try {
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential
            when (credential) {
                is CustomCredential ->
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            GoogleSignInResult.Token(googleCredential.idToken)
                        } catch (_: GoogleIdTokenParsingException) {
                            GoogleSignInResult.Failed(AuthError.GoogleSignInFailed)
                        }
                    } else {
                        GoogleSignInResult.Failed(AuthError.GoogleSignInFailed)
                    }
                else -> GoogleSignInResult.Failed(AuthError.GoogleSignInFailed)
            }
        } catch (_: GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (_: NoCredentialException) {
            GoogleSignInResult.Failed(AuthError.GoogleSignInFailed)
        } catch (_: GetCredentialException) {
            GoogleSignInResult.Failed(AuthError.GoogleSignInFailed)
        }
    }
}
