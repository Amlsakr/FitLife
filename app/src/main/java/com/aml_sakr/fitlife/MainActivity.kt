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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import com.aml_sakr.fitlife.feature.auth.auth_ui.google.DefaultGoogleSignInLauncher
import com.aml_sakr.fitlife.feature.auth.auth_ui.google.GoogleSignInLauncher
import com.aml_sakr.fitlife.feature.auth.auth_ui.navigation.AuthDestination
import com.aml_sakr.fitlife.feature.auth.auth_ui.navigation.registerAuthEntries
import com.aml_sakr.fitlife.feature.auth.auth_ui.splash.StartupRouteErrorLogger
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser
import com.aml_sakr.fitlife.feature.auth.domain.repository.AuthRepository
import com.aml_sakr.fitlife.feature.auth.domain.startup.AuthSession
import com.aml_sakr.fitlife.feature.auth.domain.startup.AuthSessionReader
import com.aml_sakr.fitlife.feature.auth.domain.startup.OnboardingCompletionReader
import com.aml_sakr.fitlife.feature.auth.domain.startup.StartupDestination
import com.aml_sakr.fitlife.feature.auth.domain.usecase.DeleteAccountUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.RefreshCurrentUserUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.ResetPasswordUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.ResendEmailVerificationUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignInUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignInWithGoogleUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignOutUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignUpUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.Equipment
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateTrainingSplit
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessGoal
import com.aml_sakr.fitlife.feature.onboarding.domain.model.OneRepMaxLift
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.ReadBeginnerDraftUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.ReadIntermediateDraftUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.ReadSelectedFitnessLevelUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.MarkOnboardingCompleteUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.SaveBeginnerProfileUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.SaveIntermediateProfileUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.SaveSelectedFitnessLevelUseCase
import com.aml_sakr.fitlife.feature.onboarding.ui.WelcomeLevelRoute
import com.aml_sakr.fitlife.feature.onboarding.ui.WelcomeLevelViewModel
import com.aml_sakr.fitlife.feature.onboarding.ui.beginner.BeginnerOnboardingRoute
import com.aml_sakr.fitlife.feature.onboarding.ui.beginner.BeginnerOnboardingViewModel
import com.aml_sakr.fitlife.feature.onboarding.ui.intermediate.IntermediateOnboardingRoute
import com.aml_sakr.fitlife.feature.onboarding.ui.intermediate.IntermediateOnboardingViewModel
import com.aml_sakr.fitlife.feature.session.ui.navigation.LocalSessionNavigator
import com.aml_sakr.fitlife.feature.session.ui.navigation.SessionStartButton
import com.aml_sakr.fitlife.feature.session.ui.navigation.rememberSessionNavigator
import com.aml_sakr.fitlife.feature.session.ui.navigation.registerSessionEntries
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

    @Inject
    lateinit var onboardingCompletionReader: OnboardingCompletionReader

    @Inject
    lateinit var onboardingRepository: OnboardingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessAppTheme {
                FitLifeApp(
                    authRepository = authRepository,
                    authSessionReader = authSessionReader,
                    onboardingCompletionReader = onboardingCompletionReader,
                    onboardingRepository = onboardingRepository,
                    googleClientId = resolveGoogleClientId()
                )
            }
        }
    }
}

private fun ComponentActivity.resolveGoogleClientId(): String {
    val resourceId = resources.getIdentifier(
        "default_web_client_id",
        "string",
        packageName
    )
    return if (resourceId == 0) "" else getString(resourceId)
}

@Composable
fun FitLifeApp(
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey> = rememberNavBackStack(AuthDestination.Splash),
    authRepository: AuthRepository = DefaultAuthRepository,
    authSessionReader: AuthSessionReader = DefaultAuthSessionReader,
    onboardingRepository: OnboardingRepository = DefaultOnboardingRepository,
    onboardingCompletionReader: OnboardingCompletionReader =
        RepositoryOnboardingCompletionReader(onboardingRepository),
    googleClientId: String = "",
    googleSignInLauncher: GoogleSignInLauncher = DefaultGoogleSignInLauncher,
    determineStartupDestination: (suspend () -> StartupDestination)? = null,
    startupRouteErrorLogger: StartupRouteErrorLogger = AndroidStartupRouteErrorLogger,
    splashDisplayDurationMillis: Long =
        if (determineStartupDestination == null) {
            5_000L
        } else {
            0L
        }
) {
    val sessionNavigator = rememberSessionNavigator(backStack)

    CompositionLocalProvider(LocalSessionNavigator provides sessionNavigator) {
        NavDisplay(
            backStack = backStack,
            modifier = modifier.fillMaxSize(),
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider<NavKey> {
            registerAuthEntries(
                authRepository = authRepository,
                authSessionReader = authSessionReader,
                onboardingCompletionReader = onboardingCompletionReader,
                googleClientId = googleClientId,
                googleSignInLauncher = googleSignInLauncher,
                determineStartupDestination = determineStartupDestination,
                startupRouteErrorLogger = startupRouteErrorLogger,
                splashDisplayDurationMillis = splashDisplayDurationMillis,
                onResetToSignIn = {
                    backStack.replaceRoot(AuthDestination.Splash, AuthDestination.SignIn)
                },
                onNavigateToHome = {
                    backStack.replaceRoot(AuthDestination.Splash, AppRoute.Home)
                },
                onNavigateToOnboarding = {
                    backStack.replaceRoot(AuthDestination.Splash, AppRoute.Onboarding)
                },
                onNavigateToSignUp = {
                    backStack.add(AuthDestination.SignUp)
                },
                onNavigateToSignIn = {
                    backStack.replaceRoot(
                        backStack.lastOrNull() ?: AuthDestination.SignIn,
                        AuthDestination.SignIn
                    )
                },
                onNavigateToForgotPassword = {
                    backStack.add(AuthDestination.ForgotPassword)
                },
                onAuthenticated = { user ->
                    try {
                        when (
                            resolvePostLoginDestination(
                                user = user,
                                onboardingCompletionReader = onboardingCompletionReader
                            )
                        ) {
                            StartupDestination.Home ->
                                backStack.replaceRoot(
                                    backStack.lastOrNull() ?: AuthDestination.SignIn,
                                    AppRoute.Home
                                )
                            StartupDestination.Onboarding ->
                                backStack.replaceRoot(
                                    backStack.lastOrNull() ?: AuthDestination.SignIn,
                                    AppRoute.Onboarding
                                )
                            StartupDestination.Auth ->
                                backStack.replaceRoot(
                                    backStack.lastOrNull() ?: AuthDestination.SignIn,
                                    AuthDestination.SignIn
                                )
                        }
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (throwable: Throwable) {
                        startupRouteErrorLogger.logStartupRouteFailure(throwable)
                        throw throwable
                    }
                }
            )

            registerSessionEntries(
                onExitSession = {
                    backStack.removeLastOrNull()
                }
            )

            entry<AppRoute.Onboarding> {
                val onboardingViewModel = viewModel<WelcomeLevelViewModel>(
                    factory = viewModelFactory {
                        initializer {
                            WelcomeLevelViewModel(
                                readSelectedFitnessLevel = ReadSelectedFitnessLevelUseCase(
                                    onboardingRepository
                                ),
                                saveSelectedFitnessLevel = SaveSelectedFitnessLevelUseCase(
                                    onboardingRepository
                                )
                            )
                        }
                    }
                )
                WelcomeLevelRoute(
                    viewModel = onboardingViewModel,
                    onNavigateToBeginner = {
                        backStack.replaceRoot(
                            AppRoute.Onboarding,
                            AppRoute.BeginnerOnboarding
                        )
                    },
                    onNavigateToIntermediate = {
                        backStack.replaceRoot(
                            AppRoute.Onboarding,
                            AppRoute.IntermediateOnboarding
                        )
                    },
                    onBack = {
                        backStack.replaceRoot(AppRoute.Onboarding, AuthDestination.SignIn)
                    }
                )
            }

            entry<AppRoute.BeginnerOnboarding> {
                val branchSession by produceState<OnboardingBranchSession>(
                    initialValue = OnboardingBranchSession.Loading,
                    authSessionReader
                ) {
                    value = readOnboardingBranchSession(authSessionReader)
                }

                when (val session = branchSession) {
                    OnboardingBranchSession.Invalid -> {
                        LaunchedEffect(Unit) {
                            backStack.replaceRoot(
                                AppRoute.BeginnerOnboarding,
                                AuthDestination.SignIn
                            )
                        }
                        LoadingBranchDestination(
                            title = "Returning to sign in...",
                            description = "We could not verify your signed-in session."
                        )
                    }

                    OnboardingBranchSession.Loading -> {
                        LoadingBranchDestination(
                            title = "Preparing beginner onboarding...",
                            description = "Loading your signed-in session so we can save your progress."
                        )
                    }

                    is OnboardingBranchSession.Valid -> {
                        val beginnerViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                            BeginnerOnboardingViewModel(
                                userId = session.userId,
                                readBeginnerDraftUseCase = ReadBeginnerDraftUseCase(
                                    onboardingRepository
                                ),
                                saveBeginnerProfileUseCase = SaveBeginnerProfileUseCase(
                                    onboardingRepository
                                ),
                                markOnboardingCompleteUseCase = MarkOnboardingCompleteUseCase(
                                    onboardingRepository
                                )
                            )
                        }
                        BeginnerOnboardingRoute(
                            viewModel = beginnerViewModel,
                            onBackToLevelSelector = {
                                backStack.replaceRoot(
                                    AppRoute.BeginnerOnboarding,
                                    AppRoute.Onboarding
                                )
                            },
                            onFinish = {
                                backStack.replaceRoot(
                                    AppRoute.BeginnerOnboarding,
                                    AppRoute.Home
                                )
                            }
                        )
                    }
                }
            }

            entry<AppRoute.IntermediateOnboarding> {
                val branchSession by produceState<OnboardingBranchSession>(
                    initialValue = OnboardingBranchSession.Loading,
                    authSessionReader
                ) {
                    value = readOnboardingBranchSession(authSessionReader)
                }

                when (val session = branchSession) {
                    OnboardingBranchSession.Invalid -> {
                        LaunchedEffect(Unit) {
                            backStack.replaceRoot(
                                AppRoute.IntermediateOnboarding,
                                AuthDestination.SignIn
                            )
                        }
                        LoadingBranchDestination(
                            title = "Returning to sign in...",
                            description = "We could not verify your signed-in session."
                        )
                    }

                    OnboardingBranchSession.Loading -> {
                        LoadingBranchDestination(
                            title = "Preparing intermediate onboarding...",
                            description = "Loading your signed-in session so we can save your progress."
                        )
                    }

                    is OnboardingBranchSession.Valid -> {
                        val intermediateViewModel = viewModel<IntermediateOnboardingViewModel>(
                            factory = viewModelFactory {
                                initializer {
                                    IntermediateOnboardingViewModel(
                                        userId = session.userId,
                                        readIntermediateDraftUseCase = ReadIntermediateDraftUseCase(
                                            onboardingRepository
                                        ),
                                        saveIntermediateProfileUseCase = SaveIntermediateProfileUseCase(
                                            onboardingRepository
                                        ),
                                        markOnboardingCompleteUseCase = MarkOnboardingCompleteUseCase(
                                            onboardingRepository
                                        ),
                                        savedStateHandle = createSavedStateHandle()
                                    )
                                }
                            }
                        )
                        IntermediateOnboardingRoute(
                            viewModel = intermediateViewModel,
                            onBackToLevelSelector = {
                                backStack.replaceRoot(
                                    AppRoute.IntermediateOnboarding,
                                    AppRoute.Onboarding
                                )
                            },
                            onFinish = {
                                backStack.replaceRoot(
                                    AppRoute.IntermediateOnboarding,
                                    AppRoute.Home
                                )
                            }
                        )
                    }
                }
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
                    },
                    onDeleteAccount = {
                        deleteAccountAndNavigateToAuth(
                            authRepository = authRepository,
                            backStack = backStack,
                            currentRoute = AppRoute.Home
                        )
                    },
                    sessionStartContent = { SessionStartButton() }
                )
            }

        }
    )
}
}



private fun MutableList<NavKey>.replaceRoot(
    expectedCurrentRoute: NavKey,
    newRoot: NavKey
) {
    if (lastOrNull() != expectedCurrentRoute) return

    clear()
    add(newRoot)
}

private suspend fun signOutAndNavigateToAuth(
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

private suspend fun deleteAccountAndNavigateToAuth(
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

@Composable
private fun ProtectedDestination(
    title: String,
    onSignOut: suspend () -> Unit,
    onDeleteAccount: suspend () -> Result<Unit, AuthError>,
    sessionStartContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSigningOut by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
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
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showDeleteAccountDialog = true },
                    enabled = !isSigningOut && !isDeletingAccount
                ) {
                    if (isDeletingAccount) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    } else {
                        Text("Delete account")
                    }
                }
                if (sessionStartContent != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    sessionStartContent()
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) showDeleteAccountDialog = false },
            title = { Text("Delete your account?") },
            text = { Text("This will remove your FitLife account and the data tied to it from the app.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isDeletingAccount) return@TextButton
                        showDeleteAccountDialog = false
                        isDeletingAccount = true
                        coroutineScope.launch {
                            try {
                                when (val result = onDeleteAccount()) {
                                    is Result.Success -> {
                                        showDeleteAccountDialog = false
                                    }

                                    is Result.Failure -> {
                                        snackbarHostState.showSnackbar(
                                            when (result.error) {
                                                AuthError.ReauthenticationRequired ->
                                                    "Please sign in again before deleting your account."
                                                else ->
                                                    "Unable to delete account. Please try again."
                                            }
                                        )
                                    }
                                }
                            } catch (cancellation: CancellationException) {
                                throw cancellation
                            } catch (_: Throwable) {
                                snackbarHostState.showSnackbar(
                                    "Unable to delete account. Please try again."
                                )
                            } finally {
                                isDeletingAccount = false
                            }
                        }
                    },
                    enabled = !isDeletingAccount
                ) {
                    Text("Delete permanently")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    enabled = !isDeletingAccount
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Serializable
internal sealed interface AppRoute : NavKey {
    @Serializable
    data object Onboarding : AppRoute

    @Serializable
    data object BeginnerOnboarding : AppRoute

    @Serializable
    data object IntermediateOnboarding : AppRoute

    @Serializable
    data object Home : AppRoute
}

private object DefaultAuthSessionReader : AuthSessionReader {
    override suspend fun currentSession() = null
}

private sealed interface OnboardingBranchSession {
    data object Loading : OnboardingBranchSession

    data object Invalid : OnboardingBranchSession

    data class Valid(val userId: String) : OnboardingBranchSession
}

private fun AuthSession?.toOnboardingBranchSession(): OnboardingBranchSession =
    when {
        this == null -> OnboardingBranchSession.Invalid
        userId.isBlank() -> OnboardingBranchSession.Invalid
        !isEmailVerified -> OnboardingBranchSession.Invalid
        else -> OnboardingBranchSession.Valid(userId)
    }

private suspend fun readOnboardingBranchSession(
    authSessionReader: AuthSessionReader
): OnboardingBranchSession =
    try {
        authSessionReader.currentSession().toOnboardingBranchSession()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        OnboardingBranchSession.Invalid
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

    override suspend fun signInWithGoogle(
        googleIdToken: String
    ): Result<AuthUser, AuthError> = Result.Failure(AuthError.GoogleSignInFailed)

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

private object DefaultOnboardingRepository : OnboardingRepository {
    override suspend fun getSelectedFitnessLevel(): Result<FitnessLevel?, OnboardingError> =
        Result.Success(null)

    override suspend fun saveSelectedFitnessLevel(level: FitnessLevel): Result<Unit, OnboardingError> =
        Result.Success(Unit)

    override suspend fun getBeginnerDraft(): Result<BeginnerOnboardingDraft, OnboardingError> =
        Result.Success(BeginnerOnboardingDraft())

    override suspend fun saveBeginnerDraft(draft: BeginnerOnboardingDraft): Result<Unit, OnboardingError> =
        Result.Success(Unit)

    override suspend fun syncBeginnerProfile(
        userId: String,
        draft: BeginnerOnboardingDraft
    ): Result<Unit, OnboardingError> = Result.Success(Unit)

    override suspend fun isOnboardingComplete(userId: String): Result<Boolean, OnboardingError> =
        Result.Success(false)

    override suspend fun markOnboardingComplete(userId: String): Result<Unit, OnboardingError> =
        Result.Success(Unit)

    override suspend fun getIntermediateDraft(): Result<IntermediateOnboardingDraft, OnboardingError> =
        Result.Success(IntermediateOnboardingDraft())

    override suspend fun saveIntermediateDraft(
        draft: IntermediateOnboardingDraft
    ): Result<Unit, OnboardingError> = Result.Success(Unit)

    override suspend fun syncIntermediateProfile(
        userId: String,
        draft: IntermediateOnboardingDraft
    ): Result<Unit, OnboardingError> = Result.Success(Unit)
}

private class RepositoryOnboardingCompletionReader(
    private val onboardingRepository: OnboardingRepository
) : OnboardingCompletionReader {
    override suspend fun isOnboardingComplete(userId: String): Boolean {
        return when (val result = onboardingRepository.isOnboardingComplete(userId)) {
            is Result.Success -> result.value
            is Result.Failure -> false
        }
    }
}

private suspend fun resolvePostLoginDestination(
    user: AuthUser,
    onboardingCompletionReader: OnboardingCompletionReader
): StartupDestination {
    if (user.id.isBlank()) return StartupDestination.Auth
    if (!user.isEmailVerified) return StartupDestination.Auth

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

private object AndroidStartupRouteErrorLogger : StartupRouteErrorLogger {
    override fun logStartupRouteFailure(throwable: Throwable) {
        Log.e("FitLifeStartup", "Unable to determine startup route", throwable)
    }
}

@Composable
private fun BranchDestination(
    title: String,
    description: String,
    onChangeLevel: () -> Unit,
    onContinueToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onChangeLevel) {
                Text("Change level")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onContinueToHome) {
                Text("Continue to home")
            }
        }
    }
}

@Composable
private fun LoadingBranchDestination(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun BeginnerCompletionHandoffDestination(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Completing your setup...",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "We’re preparing the last onboarding step. You’ll only see this briefly.",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun IntermediateCompletionHandoffDestination(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Completing your setup...",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "We are preparing the last onboarding step. You will only see this briefly.",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FitLifeAppPreview() {
    FitnessAppTheme {
        ProtectedDestination(
            title = "Splash preview handled in auth-ui",
            onSignOut = {},
            onDeleteAccount = { Result.Success(Unit) }
        )
    }
}
