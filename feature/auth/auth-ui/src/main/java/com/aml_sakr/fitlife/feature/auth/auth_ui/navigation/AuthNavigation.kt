package com.aml_sakr.fitlife.feature.auth.auth_ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.aml_sakr.fitlife.feature.auth.auth_ui.google.DefaultGoogleSignInLauncher
import com.aml_sakr.fitlife.feature.auth.auth_ui.google.GoogleSignInLauncher
import com.aml_sakr.fitlife.feature.auth.auth_ui.AuthUiConstants
import com.aml_sakr.fitlife.feature.auth.auth_ui.splash.SplashRoute
import com.aml_sakr.fitlife.feature.auth.auth_ui.splash.SplashViewModel
import com.aml_sakr.fitlife.feature.auth.auth_ui.splash.StartupRouteErrorLogger
import com.aml_sakr.fitlife.feature.auth.auth_ui.screen.AuthRoute
import com.aml_sakr.fitlife.feature.auth.auth_ui.viewmodel.AuthViewModel
import com.aml_sakr.fitlife.feature.auth.auth_ui.event.AuthEvent
import com.aml_sakr.fitlife.feature.auth.auth_ui.forgotpassword.ForgotPasswordRoute
import com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser
import com.aml_sakr.fitlife.feature.auth.domain.repository.AuthRepository
import com.aml_sakr.fitlife.feature.auth.domain.startup.AuthSessionReader
import com.aml_sakr.fitlife.feature.auth.domain.startup.DetermineStartupDestinationUseCase
import com.aml_sakr.fitlife.feature.auth.domain.startup.OnboardingCompletionReader
import com.aml_sakr.fitlife.feature.auth.domain.startup.StartupDestination
import com.aml_sakr.fitlife.feature.auth.domain.usecase.DeleteAccountUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.RefreshCurrentUserUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.ResetPasswordUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.ResendEmailVerificationUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignInUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignInWithGoogleUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignOutUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignUpUseCase

fun EntryProviderScope<NavKey>.registerAuthEntries(
    authRepository: AuthRepository,
    authSessionReader: AuthSessionReader,
    onboardingCompletionReader: OnboardingCompletionReader,
    googleClientId: String,
    googleSignInLauncher: GoogleSignInLauncher = DefaultGoogleSignInLauncher,
    determineStartupDestination: (suspend () -> StartupDestination)? = null,
    startupRouteErrorLogger: StartupRouteErrorLogger = StartupRouteErrorLogger { },
    splashDisplayDurationMillis: Long = AuthUiConstants.SPLASH_DISPLAY_DURATION_MILLIS,
    onResetToSignIn: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onAuthenticated: suspend (AuthUser) -> Unit
) {
    val resolveStartupDestination = determineStartupDestination
        ?: DetermineStartupDestinationUseCase(
            authSessionReader = authSessionReader,
            onboardingCompletionReader = onboardingCompletionReader
        )::invoke

    entry<AuthDestination.Splash> {
        val splashViewModel = viewModel {
            SplashViewModel(
                determineStartupDestination = { resolveStartupDestination() },
                startupRouteErrorLogger = startupRouteErrorLogger,
                splashDisplayDurationMillis = splashDisplayDurationMillis
            )
        }
        SplashRoute(
            viewModel = splashViewModel,
            onNavigateToAuth = onResetToSignIn,
            onNavigateToOnboarding = onNavigateToOnboarding,
            onNavigateToHome = onNavigateToHome
        )
    }

    entry<AuthDestination.SignIn> {
        val authViewModel = rememberAuthViewModel(authRepository)
        AuthRoute(
            viewModel = authViewModel,
            onAuthenticated = onAuthenticated,
            onNavigateToOnboarding = onNavigateToOnboarding,
            onNavigateToSignUp = onNavigateToSignUp,
            onNavigateToForgotPassword = onNavigateToForgotPassword,
            googleClientId = googleClientId,
            googleSignInLauncher = googleSignInLauncher
        )
    }

    entry<AuthDestination.SignUp> {
        val authViewModel = rememberAuthViewModel(authRepository)
        LaunchedEffect(authViewModel) {
            authViewModel.onEvent(AuthEvent.ShowSignUp)
        }
        AuthRoute(
            viewModel = authViewModel,
            onAuthenticated = onAuthenticated,
            onNavigateToOnboarding = onNavigateToOnboarding,
            onNavigateToSignIn = onNavigateToSignIn,
            googleClientId = googleClientId,
            googleSignInLauncher = googleSignInLauncher
        )
    }

    entry<AuthDestination.ForgotPassword> {
        val authViewModel = rememberAuthViewModel(authRepository)
        ForgotPasswordRoute(
            viewModel = authViewModel,
            onNavigateToSignIn = onNavigateToSignIn
        )
    }
}

@Composable
private fun rememberAuthViewModel(authRepository: AuthRepository): AuthViewModel {
    return viewModel {
        AuthViewModel(
            signUp = SignUpUseCase(authRepository),
            signIn = SignInUseCase(authRepository),
            signInWithGoogle = SignInWithGoogleUseCase(authRepository),
            resetPasswordUseCase = ResetPasswordUseCase(authRepository),
            deleteAccountUseCase = DeleteAccountUseCase(authRepository),
            signOut = SignOutUseCase(authRepository),
            getCurrentUser = GetCurrentUserUseCase(authRepository),
            resendEmailVerification = ResendEmailVerificationUseCase(authRepository),
            refreshCurrentUser = RefreshCurrentUserUseCase(authRepository)
        )
    }
}
