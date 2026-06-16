package com.aml_sakr.fitlife

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser
import com.aml_sakr.fitlife.feature.auth.domain.repository.AuthRepository
import com.aml_sakr.fitlife.feature.auth.auth_ui.google.GoogleSignInLauncher
import com.aml_sakr.fitlife.feature.auth.auth_ui.google.GoogleSignInResult
import com.aml_sakr.fitlife.feature.auth.domain.startup.StartupDestination
import com.aml_sakr.fitlife.feature.auth.auth_ui.splash.StartupRouteErrorLogger
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FitLifeAppNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun authAction_navigatesToAuthAndRemovesSplash() {
        assertStartupNavigation(
            destination = StartupDestination.Auth,
            expectedRoute = AppRoute.Auth,
            expectedTitle = "Login"
        )
    }

    @Test
    fun onboardingAction_navigatesToOnboardingAndRemovesSplash() {
        assertStartupNavigation(
            destination = StartupDestination.Onboarding,
            expectedRoute = AppRoute.Onboarding,
            expectedTitle = "Onboarding"
        )
    }

    @Test
    fun homeAction_navigatesToHomeAndRemovesSplash() {
        assertStartupNavigation(
            destination = StartupDestination.Home,
            expectedRoute = AppRoute.Home,
            expectedTitle = "Home"
        )
    }

    @Test
    fun verifiedAuthUser_navigatesToOnboardingAndRemovesAuth() {
        assertPostAuthNavigation(
            destination = StartupDestination.Onboarding,
            expectedRoute = AppRoute.Onboarding,
            expectedTitle = "Onboarding"
        )
    }

    @Test
    fun verifiedAuthUser_navigatesToHomeAndRemovesAuth() {
        assertPostAuthNavigation(
            destination = StartupDestination.Home,
            expectedRoute = AppRoute.Home,
            expectedTitle = "Home"
        )
    }

    @Test
    fun protectedDestination_signOutReplacesRootWithAuth() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AppRoute.Home)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = VerifiedAuthRepository,
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        composeRule.onNodeWithText("Sign out").performClick()
        composeRule.onNodeWithText("Login").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AppRoute.Auth), backStack.toList())
        }
    }

    @Test
    fun googleSignInAction_navigatesToHomeAndRemovesAuth() {
        lateinit var backStack: NavBackStack<NavKey>
        var destinationCheckCount = 0

        composeRule.setContent {
            backStack = rememberNavBackStack(AppRoute.Splash)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = GoogleAuthRepository,
                    googleClientId = "test-web-client-id",
                    googleSignInLauncher = FakeGoogleSignInLauncher,
                    determineStartupDestination = {
                        if (destinationCheckCount++ == 0) {
                            StartupDestination.Auth
                        } else {
                            StartupDestination.Home
                        }
                    },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        composeRule.onNodeWithText("Sign in with Google").performClick()
        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AppRoute.Home), backStack.toList())
        }
    }

    @Test
    fun googleSignInLaunch_disablesOtherAuthActionsUntilResultReturns() {
        val launchStarted = CompletableDeferred<Unit>()
        val releaseLaunch = CompletableDeferred<Unit>()
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AppRoute.Auth)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = PendingGoogleAuthRepository,
                    googleClientId = "test-web-client-id",
                    googleSignInLauncher = BlockingGoogleSignInLauncher(
                        launchStarted = launchStarted,
                        releaseLaunch = releaseLaunch
                    ),
                    determineStartupDestination = { StartupDestination.Home },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        composeRule.onNodeWithText("Sign in with Google").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { launchStarted.isCompleted }
        composeRule.onNodeWithText("Login").assertIsNotEnabled()
        composeRule.onNodeWithText("Register").assertIsNotEnabled()

        releaseLaunch.complete(Unit)
        composeRule.waitForIdle()
    }

    @Test
    fun signInScreen_rendersRedesignedLayout() {
        composeRule.setContent {
            FitnessAppTheme {
                FitLifeApp(
                    authRepository = PendingGoogleAuthRepository,
                    googleClientId = "test-web-client-id",
                    googleSignInLauncher = FakeGoogleSignInLauncher,
                    determineStartupDestination = { StartupDestination.Auth },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        composeRule.onNodeWithText("FitLife AI").assertIsDisplayed()
        composeRule.onNodeWithText("Welcome back").assertIsDisplayed()
        composeRule.onNodeWithText("Please enter your details to login.").assertIsDisplayed()
        composeRule.onNodeWithText("Sign in with Google").assertIsDisplayed()
        composeRule.onNodeWithText("Login").assertIsDisplayed()
        composeRule.onNodeWithText("Training floor").assertIsDisplayed()
        composeRule.onNodeWithText("Smart metrics").assertIsDisplayed()
    }

    @Test
    fun signUpScreen_rendersRedesignedLayout() {
        composeRule.setContent {
            FitnessAppTheme {
                FitLifeApp(
                    authRepository = PendingGoogleAuthRepository,
                    googleClientId = "test-web-client-id",
                    googleSignInLauncher = FakeGoogleSignInLauncher,
                    determineStartupDestination = { StartupDestination.Auth },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        composeRule.onNodeWithText("Register").performClick()
        composeRule.onNodeWithText("Create Account").assertIsDisplayed()
        composeRule.onNodeWithText("Join FitLife to start your AI-powered fitness journey.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("OR REGISTER WITH").assertIsDisplayed()
        composeRule.onNodeWithText("Already have an account?").assertIsDisplayed()
        composeRule.onNodeWithText("Login").assertIsDisplayed()
        composeRule.onNodeWithText("By registering, you agree to our Terms of Service and Privacy Policy.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Secure Data").assertIsDisplayed()
        composeRule.onNodeWithText("AI Powered").assertIsDisplayed()
    }

    private fun assertPostAuthNavigation(
        destination: StartupDestination,
        expectedRoute: AppRoute,
        expectedTitle: String
    ) {
        lateinit var backStack: NavBackStack<NavKey>
        var destinationCheckCount = 0

        composeRule.setContent {
            backStack = rememberNavBackStack(AppRoute.Splash)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = VerifiedAuthRepository,
                    determineStartupDestination = {
                        if (destinationCheckCount++ == 0) {
                            StartupDestination.Auth
                        } else {
                            destination
                        }
                    },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        composeRule.onNodeWithText(expectedTitle).assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(expectedRoute), backStack.toList())
        }
    }

    private fun assertStartupNavigation(
        destination: StartupDestination,
        expectedRoute: AppRoute,
        expectedTitle: String
    ) {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AppRoute.Splash)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    determineStartupDestination = { destination },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        composeRule.onNodeWithText(expectedTitle).assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(expectedRoute), backStack.toList())
        }
    }

    private object NoOpStartupRouteErrorLogger : StartupRouteErrorLogger {
        override fun logStartupRouteFailure(throwable: Throwable) = Unit
    }

    private object VerifiedAuthRepository : AuthRepository {
        private val user = AuthUser(
            id = "verified-user",
            email = "verified@example.com",
            isEmailVerified = true
        )

        override suspend fun signUp(
            email: String,
            password: String
        ): Result<AuthUser, AuthError> = Result.Success(user)

        override suspend fun signIn(
            email: String,
            password: String
        ): Result<AuthUser, AuthError> = Result.Success(user)

        override suspend fun signInWithGoogle(
            googleIdToken: String
        ): Result<AuthUser, AuthError> = Result.Success(user)

        override suspend fun signOut(): Result<Unit, AuthError> = Result.Success(Unit)

        override suspend fun currentUser(): Result<AuthUser?, AuthError> = Result.Success(user)

        override suspend fun sendEmailVerification(): Result<Unit, AuthError> =
            Result.Success(Unit)

        override suspend fun refreshCurrentUser(): Result<AuthUser?, AuthError> =
            Result.Success(user)
    }

    private object FakeGoogleSignInLauncher : GoogleSignInLauncher {
        override suspend fun launch(
            context: android.content.Context,
            googleClientId: String
        ): GoogleSignInResult = GoogleSignInResult.Token("google-id-token")
    }

    private object GoogleAuthRepository : AuthRepository {
        private val user = AuthUser(
            id = "verified-user",
            email = "google@example.com",
            isEmailVerified = true
        )

        override suspend fun signUp(
            email: String,
            password: String
        ): Result<AuthUser, AuthError> = Result.Success(user)

        override suspend fun signIn(
            email: String,
            password: String
        ): Result<AuthUser, AuthError> = Result.Success(user)

        override suspend fun signInWithGoogle(
            googleIdToken: String
        ): Result<AuthUser, AuthError> = Result.Success(user)

        override suspend fun signOut(): Result<Unit, AuthError> = Result.Success(Unit)

        override suspend fun currentUser(): Result<AuthUser?, AuthError> = Result.Success(user)

        override suspend fun sendEmailVerification(): Result<Unit, AuthError> =
            Result.Success(Unit)

        override suspend fun refreshCurrentUser(): Result<AuthUser?, AuthError> =
            Result.Success(user)
    }

    private object PendingGoogleAuthRepository : AuthRepository {
        private val user = AuthUser(
            id = "pending-user",
            email = "pending@example.com",
            isEmailVerified = true
        )

        override suspend fun signUp(
            email: String,
            password: String
        ): Result<AuthUser, AuthError> = Result.Success(user)

        override suspend fun signIn(
            email: String,
            password: String
        ): Result<AuthUser, AuthError> = Result.Success(user)

        override suspend fun signInWithGoogle(
            googleIdToken: String
        ): Result<AuthUser, AuthError> = Result.Success(user)

        override suspend fun signOut(): Result<Unit, AuthError> = Result.Success(Unit)

        override suspend fun currentUser(): Result<AuthUser?, AuthError> = Result.Success(null)

        override suspend fun sendEmailVerification(): Result<Unit, AuthError> =
            Result.Success(Unit)

        override suspend fun refreshCurrentUser(): Result<AuthUser?, AuthError> =
            Result.Success(null)
    }

    private class BlockingGoogleSignInLauncher(
        private val launchStarted: CompletableDeferred<Unit>,
        private val releaseLaunch: CompletableDeferred<Unit>
    ) : GoogleSignInLauncher {
        override suspend fun launch(
            context: android.content.Context,
            googleClientId: String
        ): GoogleSignInResult {
            launchStarted.complete(Unit)
            releaseLaunch.await()
            return GoogleSignInResult.Token("google-id-token")
        }
    }
}
