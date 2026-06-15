package com.aml_sakr.fitlife

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser
import com.aml_sakr.fitlife.feature.auth.domain.repository.AuthRepository
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import com.aml_sakr.fitlife.feature.auth.domain.startup.StartupDestination
import com.aml_sakr.fitlife.feature.auth.auth_ui.splash.StartupRouteErrorLogger
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
            expectedTitle = "Sign in"
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
        composeRule.onNodeWithText("Sign in").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AppRoute.Auth), backStack.toList())
        }
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

        override suspend fun signOut(): Result<Unit, AuthError> = Result.Success(Unit)

        override suspend fun currentUser(): Result<AuthUser?, AuthError> = Result.Success(user)

        override suspend fun sendEmailVerification(): Result<Unit, AuthError> =
            Result.Success(Unit)

        override suspend fun refreshCurrentUser(): Result<AuthUser?, AuthError> =
            Result.Success(user)
    }
}
