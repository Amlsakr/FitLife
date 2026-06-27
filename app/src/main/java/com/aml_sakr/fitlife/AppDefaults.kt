package com.aml_sakr.fitlife

import android.util.Log
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser
import com.aml_sakr.fitlife.feature.auth.domain.repository.AuthRepository
import com.aml_sakr.fitlife.feature.auth.domain.startup.AuthSessionReader
import com.aml_sakr.fitlife.feature.auth.domain.startup.OnboardingCompletionReader
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository

internal object DefaultAuthSessionReader : AuthSessionReader {
    override suspend fun currentSession() = null
}

internal object DefaultAuthRepository : AuthRepository {
    override suspend fun signUp(email: String, password: String): Result<AuthUser, AuthError> =
        Result.Failure(AuthError.Unknown)

    override suspend fun signIn(email: String, password: String): Result<AuthUser, AuthError> =
        Result.Failure(AuthError.Unknown)

    override suspend fun signInWithGoogle(googleIdToken: String): Result<AuthUser, AuthError> =
        Result.Failure(AuthError.GoogleSignInFailed)

    override suspend fun resetPassword(email: String): Result<Unit, AuthError> =
        Result.Failure(AuthError.Unknown)

    override suspend fun deleteAccount(): Result<Unit, AuthError> =
        Result.Failure(AuthError.Unknown)

    override suspend fun signOut(): Result<Unit, AuthError> = Result.Success(Unit)

    override suspend fun currentUser(): Result<AuthUser?, AuthError> = Result.Success(null)

    override suspend fun sendEmailVerification(): Result<Unit, AuthError> =
        Result.Failure(AuthError.NoAuthenticatedUser)

    override suspend fun refreshCurrentUser(): Result<AuthUser?, AuthError> =
        Result.Success(null)
}

internal object DefaultOnboardingRepository : OnboardingRepository {
    override suspend fun getSelectedFitnessLevel(userId: String): Result<FitnessLevel?, OnboardingError> =
        Result.Success(null)

    override suspend fun saveSelectedFitnessLevel(
        userId: String,
        level: FitnessLevel
    ): Result<Unit, OnboardingError> =
        Result.Success(Unit)

    override suspend fun getBeginnerDraft(userId: String): Result<BeginnerOnboardingDraft, OnboardingError> =
        Result.Success(BeginnerOnboardingDraft())

    override suspend fun saveBeginnerDraft(
        userId: String,
        draft: BeginnerOnboardingDraft
    ): Result<Unit, OnboardingError> =
        Result.Success(Unit)

    override suspend fun syncBeginnerProfile(
        userId: String,
        draft: BeginnerOnboardingDraft
    ): Result<Unit, OnboardingError> = Result.Success(Unit)

    override suspend fun isOnboardingComplete(userId: String): Result<Boolean, OnboardingError> =
        Result.Success(false)

    override suspend fun markOnboardingComplete(userId: String): Result<Unit, OnboardingError> =
        Result.Success(Unit)

    override suspend fun getIntermediateDraft(userId: String): Result<IntermediateOnboardingDraft, OnboardingError> =
        Result.Success(IntermediateOnboardingDraft())

    override suspend fun saveIntermediateDraft(
        userId: String,
        draft: IntermediateOnboardingDraft
    ): Result<Unit, OnboardingError> = Result.Success(Unit)

    override suspend fun syncIntermediateProfile(
        userId: String,
        draft: IntermediateOnboardingDraft
    ): Result<Unit, OnboardingError> = Result.Success(Unit)
}

internal class RepositoryOnboardingCompletionReader(
    private val onboardingRepository: OnboardingRepository
) : OnboardingCompletionReader {
    override suspend fun isOnboardingComplete(userId: String): Boolean {
        val result = onboardingRepository.isOnboardingComplete(userId)
        Log.e("RepositoryOnboarding", "isOnboardingComplete: $result")
        return when (result) {
            is Result.Success -> result.value
            is Result.Failure -> false
        }
    }
}
