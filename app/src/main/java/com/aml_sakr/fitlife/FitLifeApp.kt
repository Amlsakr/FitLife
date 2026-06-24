package com.aml_sakr.fitlife

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
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
import com.aml_sakr.fitlife.feature.auth.auth_ui.google.DefaultGoogleSignInLauncher
import com.aml_sakr.fitlife.feature.auth.auth_ui.google.GoogleSignInLauncher
import com.aml_sakr.fitlife.feature.auth.auth_ui.navigation.AuthDestination
import com.aml_sakr.fitlife.feature.auth.auth_ui.navigation.registerAuthEntries
import com.aml_sakr.fitlife.feature.auth.auth_ui.splash.StartupRouteErrorLogger
import com.aml_sakr.fitlife.feature.auth.domain.repository.AuthRepository
import com.aml_sakr.fitlife.feature.auth.domain.startup.AuthSessionReader
import com.aml_sakr.fitlife.feature.auth.domain.startup.OnboardingCompletionReader
import com.aml_sakr.fitlife.feature.auth.domain.startup.StartupDestination
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.MarkOnboardingCompleteUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.ReadBeginnerDraftUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.ReadIntermediateDraftUseCase
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.ReadSelectedFitnessLevelUseCase
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
import com.aml_sakr.fitlife.feature.session.ui.navigation.registerSessionEntries
import com.aml_sakr.fitlife.feature.session.ui.navigation.rememberSessionNavigator
import com.aml_sakr.fitlife.feature.shell.ui.AppShell
import kotlinx.coroutines.CancellationException

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
            modifier = modifier,
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
                        backStack.replaceRoot(AuthDestination.Splash, AppRoute.Shell)
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
                                        AppRoute.Shell
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
                    onExitSession = { backStack.removeLastOrNull() }
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
                                    Log.e("onboarding", "navigate to home")
                                    backStack.replaceRoot(
                                        AppRoute.BeginnerOnboarding,
                                        AppRoute.Shell
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
                                    Log.e("onboarding", "navigate to home")
                                    backStack.replaceRoot(
                                        AppRoute.IntermediateOnboarding,
                                        AppRoute.Shell
                                    )
                                }
                            )
                        }
                    }
                }

                entry<AppRoute.Shell> {
                    AppShell(
                        onSignOut = {
                            signOutAndNavigateToAuth(
                                authRepository = authRepository,
                                backStack = backStack,
                                currentRoute = AppRoute.Shell
                            )
                        },
                        onDeleteAccount = {
                            deleteAccountAndNavigateToAuth(
                                authRepository = authRepository,
                                backStack = backStack,
                                currentRoute = AppRoute.Shell
                            )
                        }
                    )
                }
            }
        )
    }
}
