package com.aml_sakr.fitlife

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser
import com.aml_sakr.fitlife.feature.auth.domain.repository.AuthRepository
import com.aml_sakr.fitlife.feature.auth.domain.startup.AuthSessionReader
import com.aml_sakr.fitlife.feature.auth.domain.startup.DetermineStartupDestinationUseCase
import com.aml_sakr.fitlife.feature.auth.domain.startup.OnboardingCompletionReader
import com.aml_sakr.fitlife.feature.auth.domain.startup.StartupDestination
import com.aml_sakr.fitlife.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.RefreshCurrentUserUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.ResendEmailVerificationUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignInUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignOutUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignUpUseCase
import com.aml_sakr.fitlife.feature.auth.auth_ui.navigation.SplashRoute
import com.aml_sakr.fitlife.feature.auth.auth_ui.navigation.SplashViewModel
import com.aml_sakr.fitlife.feature.auth.auth_ui.navigation.StartupRouteErrorLogger
import com.aml_sakr.fitlife.feature.auth.auth_ui.screen.AuthRoute
import com.aml_sakr.fitlife.feature.auth.auth_ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var authSessionReader: AuthSessionReader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessAppTheme {
                FitLifeApp(
                    authRepository = authRepository,
                    authSessionReader = authSessionReader
                )
            }
        }
    }
}

@Composable
fun FitLifeApp(
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey> = rememberNavBackStack(AppRoute.Splash),
    authRepository: AuthRepository = DefaultAuthRepository,
    authSessionReader: AuthSessionReader = DefaultAuthSessionReader,
    determineStartupDestination: (suspend () -> StartupDestination)? = null,
    startupRouteErrorLogger: StartupRouteErrorLogger = AndroidStartupRouteErrorLogger
) {
    val startupDestinationUseCase = remember(authSessionReader) {
        DetermineStartupDestinationUseCase(
            authSessionReader = authSessionReader,
            onboardingCompletionReader = DefaultOnboardingCompletionReader
        )
    }
    val resolveStartupDestination = remember(
        determineStartupDestination,
        startupDestinationUseCase
    ) {
        determineStartupDestination ?: startupDestinationUseCase::invoke
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier.fillMaxSize(),
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<AppRoute.Splash> {
                val splashViewModel = viewModel {
                    SplashViewModel(
                        determineStartupDestination = resolveStartupDestination,
                        startupRouteErrorLogger = startupRouteErrorLogger
                    )
                }
                SplashRoute(
                    viewModel = splashViewModel,
                    onNavigateToAuth = {
                        backStack.replaceRoot(AppRoute.Splash, AppRoute.Auth)
                    },
                    onNavigateToOnboarding = {
                        backStack.replaceRoot(AppRoute.Splash, AppRoute.Onboarding)
                    },
                    onNavigateToHome = {
                        backStack.replaceRoot(AppRoute.Splash, AppRoute.Home)
                    }
                )
            }
            entry<AppRoute.Auth> {
                val authViewModel = viewModel {
                    AuthViewModel(
                        signUp = SignUpUseCase(authRepository),
                        signIn = SignInUseCase(authRepository),
                        signOut = SignOutUseCase(authRepository),
                        getCurrentUser = GetCurrentUserUseCase(authRepository),
                        resendEmailVerification = ResendEmailVerificationUseCase(authRepository),
                        refreshCurrentUser = RefreshCurrentUserUseCase(authRepository)
                    )
                }
                AuthRoute(
                    viewModel = authViewModel,
                    onAuthenticated = {
                        try {
                            backStack.navigateAfterAuth(resolveStartupDestination())
                        } catch (cancellation: CancellationException) {
                            throw cancellation
                        } catch (throwable: Throwable) {
                            startupRouteErrorLogger.logStartupRouteFailure(throwable)
                            throw throwable
                        }
                    }
                )
            }
            entry<AppRoute.Onboarding> {
                ProtectedDestination(
                    title = "Onboarding",
                    onSignOut = {
                        signOutAndNavigateToAuth(
                            authRepository = authRepository,
                            backStack = backStack,
                            currentRoute = AppRoute.Onboarding
                        )
                    }
                )
            }
            entry<AppRoute.Home> {
                ProtectedDestination(
                    title = "Home",
                    onSignOut = {
                        signOutAndNavigateToAuth(
                            authRepository = authRepository,
                            backStack = backStack,
                            currentRoute = AppRoute.Home
                        )
                    }
                )
            }
        }
    )
}

private fun MutableList<NavKey>.navigateAfterAuth(destination: StartupDestination) {
    val route = when (destination) {
        StartupDestination.Auth ->
            error("Verified authentication resolved back to the auth destination")
        StartupDestination.Onboarding -> AppRoute.Onboarding
        StartupDestination.Home -> AppRoute.Home
    }

    replaceRoot(AppRoute.Auth, route)
}

private fun MutableList<NavKey>.replaceRoot(
    expectedCurrentRoute: AppRoute,
    newRoot: AppRoute
) {
    if (lastOrNull() != expectedCurrentRoute) return

    clear()
    add(newRoot)
}

private suspend fun signOutAndNavigateToAuth(
    authRepository: AuthRepository,
    backStack: MutableList<NavKey>,
    currentRoute: AppRoute
) {
    when (val result = SignOutUseCase(authRepository)()) {
        is Result.Success -> backStack.replaceRoot(currentRoute, AppRoute.Auth)
        is Result.Failure -> error("Sign out failed: ${result.error.code}")
    }
}

@Composable
private fun ProtectedDestination(
    title: String,
    onSignOut: suspend () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSigningOut by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (isSigningOut) return@Button
                    isSigningOut = true
                    coroutineScope.launch {
                        try {
                            onSignOut()
                        } catch (cancellation: CancellationException) {
                            throw cancellation
                        } catch (_: Throwable) {
                            snackbarHostState.showSnackbar(
                                "Unable to sign out. Please try again."
                            )
                        } finally {
                            isSigningOut = false
                        }
                    }
                },
                enabled = !isSigningOut
            ) {
                if (isSigningOut) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text("Sign out")
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Serializable
internal sealed interface AppRoute : NavKey {
    @Serializable
    data object Splash : AppRoute

    @Serializable
    data object Auth : AppRoute

    @Serializable
    data object Onboarding : AppRoute

    @Serializable
    data object Home : AppRoute
}

private object DefaultAuthSessionReader : AuthSessionReader {
    override suspend fun currentSession() = null
}

private object DefaultAuthRepository : AuthRepository {
    override suspend fun signUp(
        email: String,
        password: String
    ): Result<AuthUser, AuthError> = Result.Failure(AuthError.Unknown)

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<AuthUser, AuthError> = Result.Failure(AuthError.Unknown)

    override suspend fun signOut(): Result<Unit, AuthError> = Result.Success(Unit)

    override suspend fun currentUser(): Result<AuthUser?, AuthError> = Result.Success(null)

    override suspend fun sendEmailVerification(): Result<Unit, AuthError> =
        Result.Failure(AuthError.NoAuthenticatedUser)

    override suspend fun refreshCurrentUser(): Result<AuthUser?, AuthError> =
        Result.Success(null)
}

private object DefaultOnboardingCompletionReader : OnboardingCompletionReader {
    override suspend fun isOnboardingComplete(userId: String): Boolean = false
}

private object AndroidStartupRouteErrorLogger : StartupRouteErrorLogger {
    override fun logStartupRouteFailure(throwable: Throwable) {
        Log.e("FitLifeStartup", "Unable to determine startup route", throwable)
    }
}

@Preview(showBackground = true)
@Composable
fun FitLifeAppPreview() {
    FitnessAppTheme {
        ProtectedDestination(
            title = "Splash preview handled in auth-ui",
            onSignOut = {}
        )
    }
}
