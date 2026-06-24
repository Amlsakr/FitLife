package com.aml_sakr.fitlife

import android.util.Log
import androidx.navigation3.runtime.NavKey
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser
import com.aml_sakr.fitlife.feature.auth.domain.repository.AuthRepository
import com.aml_sakr.fitlife.feature.auth.domain.startup.AuthSession
import com.aml_sakr.fitlife.feature.auth.domain.startup.AuthSessionReader
import com.aml_sakr.fitlife.feature.auth.domain.startup.OnboardingCompletionReader
import com.aml_sakr.fitlife.feature.auth.domain.startup.StartupDestination
import com.aml_sakr.fitlife.feature.auth.auth_ui.navigation.AuthDestination
import com.aml_sakr.fitlife.feature.auth.domain.usecase.DeleteAccountUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignOutUseCase
import com.aml_sakr.fitlife.feature.auth.auth_ui.splash.StartupRouteErrorLogger
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface AppRoute : NavKey {
    @Serializable
    data object Onboarding : AppRoute

    @Serializable
    data object BeginnerOnboarding : AppRoute

    @Serializable
    data object IntermediateOnboarding : AppRoute

    @Serializable
    data object Shell : AppRoute
}

internal sealed interface OnboardingBranchSession {
    data object Loading : OnboardingBranchSession
    data object Invalid : OnboardingBranchSession
    data class Valid(val userId: String) : OnboardingBranchSession
}

internal fun AuthSession?.toOnboardingBranchSession(): OnboardingBranchSession =
    when {
        this == null -> OnboardingBranchSession.Invalid
        userId.isBlank() -> OnboardingBranchSession.Invalid
        else -> OnboardingBranchSession.Valid(userId)
    }

internal suspend fun readOnboardingBranchSession(
    authSessionReader: AuthSessionReader
): OnboardingBranchSession =
    try {
        authSessionReader.currentSession().toOnboardingBranchSession()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        OnboardingBranchSession.Invalid
    }

internal fun MutableList<NavKey>.replaceRoot(
    expectedCurrentRoute: NavKey,
    newRoot: NavKey
) {
    if (lastOrNull() != expectedCurrentRoute) return

    clear()
    add(newRoot)
}

internal suspend fun signOutAndNavigateToAuth(
    authRepository: AuthRepository,
    backStack: MutableList<NavKey>,
    currentRoute: NavKey
) {
    when (val result = SignOutUseCase(authRepository)()) {
        is Result.Success -> {
            backStack.clear()
            backStack.add(AuthDestination.SignIn)
        }
        is Result.Failure -> error("Sign out failed: ${result.error.code}")
    }
}

internal suspend fun deleteAccountAndNavigateToAuth(
    authRepository: AuthRepository,
    backStack: MutableList<NavKey>,
    currentRoute: NavKey
): Result<Unit, AuthError> {
    return when (val result = DeleteAccountUseCase(authRepository)()) {
        is Result.Success -> {
            backStack.clear()
            backStack.add(AuthDestination.SignIn)
            result
        }

        is Result.Failure -> result
    }
}

internal suspend fun resolvePostLoginDestination(
    user: AuthUser,
    onboardingCompletionReader: OnboardingCompletionReader
): StartupDestination {
    Log.d("user", user.toString())

    return try {
        if (onboardingCompletionReader.isOnboardingComplete(user.id)) {
            StartupDestination.Home
        } else {
            StartupDestination.Onboarding
        }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        StartupDestination.Onboarding
    }
}

internal object AndroidStartupRouteErrorLogger : StartupRouteErrorLogger {
    override fun logStartupRouteFailure(throwable: Throwable) {
        Log.e("FitLifeStartup", "Unable to determine startup route", throwable)
    }
}
