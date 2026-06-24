package com.aml_sakr.fitlife

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import com.aml_sakr.fitlife.feature.auth.auth_ui.navigation.AuthDestination
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser
import com.aml_sakr.fitlife.feature.auth.domain.repository.AuthRepository
import com.aml_sakr.fitlife.feature.auth.auth_ui.google.GoogleSignInLauncher
import com.aml_sakr.fitlife.feature.auth.auth_ui.google.GoogleSignInResult
import com.aml_sakr.fitlife.feature.auth.domain.startup.AuthSession
import com.aml_sakr.fitlife.feature.auth.domain.startup.AuthSessionReader
import com.aml_sakr.fitlife.feature.auth.domain.startup.OnboardingCompletionReader
import com.aml_sakr.fitlife.feature.auth.domain.startup.StartupDestination
import com.aml_sakr.fitlife.feature.auth.auth_ui.splash.StartupRouteErrorLogger
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.Equipment
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateTrainingSplit
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessGoal
import com.aml_sakr.fitlife.feature.onboarding.domain.model.OneRepMaxLift
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
            expectedRoute = AuthDestination.SignIn,
            expectedTitle = "Login"
        )
        assertShellTabsHidden()
    }

    @Test
    fun onboardingAction_navigatesToOnboardingAndRemovesSplash() {
        assertStartupNavigation(
            destination = StartupDestination.Onboarding,
            expectedRoute = AppRoute.Onboarding,
            expectedTitle = "Welcome to FitLife"
        )
        assertShellTabsHidden()
    }

    @Test
    fun homeAction_navigatesToShellAndRemovesSplash() {
        assertStartupNavigation(
            destination = StartupDestination.Home,
            expectedRoute = AppRoute.Shell,
            expectedTitle = "Home"
        )
        assertShellTabsVisible()
    }

    @Test
    fun appLaunch_toHome_doesNotShowSessionDisclosure() {
        composeRule.setContent {
            FitnessAppTheme {
                FitLifeApp(
                    backStack = rememberNavBackStack(AuthDestination.Splash),
                    determineStartupDestination = { StartupDestination.Home },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForText("Home")
        assertShellTabsVisible()
        assertTrue(
            composeRule.onAllNodesWithText("Keep your form analysis private")
                .fetchSemanticsNodes()
                .isEmpty()
        )
    }

    @Test
    fun verifiedAuthUser_navigatesToShellAndRemovesAuth() {
        assertPostAuthNavigation(
            onboardingComplete = true,
            expectedRoute = AppRoute.Shell,
            expectedTitle = "Home"
        )
        assertShellTabsVisible()
    }

    @Test
    fun uncompletedOnboardingAuthUser_navigatesToOnboardingAndRemovesAuth() {
        assertPostAuthNavigation(
            onboardingComplete = false,
            expectedRoute = AppRoute.Onboarding,
            expectedTitle = "Welcome to FitLife"
        )
    }

    @Test
    fun unverifiedAuthUser_staysInAuthAfterGoogleSignIn() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AuthDestination.Splash)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = UnverifiedAuthRepository,
                    googleClientId = "test-web-client-id",
                    googleSignInLauncher = FakeGoogleSignInLauncher,
                    onboardingCompletionReader = FakeOnboardingCompletionReader(true),
                    determineStartupDestination = { StartupDestination.Auth },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForText("Sign in with Google")
        composeRule.onNodeWithText("Sign in with Google").performClick()
        waitForText("Login")
        composeRule.onNodeWithText("Login").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AuthDestination.SignIn), backStack.toList())
        }
    }

    @Test
    fun onboardingCompletionReadFailure_navigatesToOnboardingAfterGoogleSignIn() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AuthDestination.Splash)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = GoogleAuthRepository,
                    googleClientId = "test-web-client-id",
                    googleSignInLauncher = FakeGoogleSignInLauncher,
                    onboardingCompletionReader = FakeOnboardingCompletionReader(
                        isComplete = false,
                        failure = IllegalStateException("preferences unavailable")
                    ),
                    determineStartupDestination = { StartupDestination.Auth },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForText("Sign in with Google")
        composeRule.onNodeWithText("Sign in with Google").performClick()
        waitForText("Onboarding")
        composeRule.onNodeWithText("Onboarding").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AppRoute.Onboarding), backStack.toList())
        }
    }

    @Test
    fun signUp_navigatesToOnboardingAndRemovesAuth() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AuthDestination.Splash)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = VerifiedAuthRepository,
                    onboardingCompletionReader = FakeOnboardingCompletionReader(false),
                    determineStartupDestination = { StartupDestination.Auth },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForText("Register")
        composeRule.onNodeWithText("Register").performClick()
        waitForText("Email")
        composeRule.onNodeWithText("Email").performTextInput("new-user@example.com")
        composeRule.onNodeWithText("Password").performTextInput("secret1")
        composeRule.onNodeWithText("Confirm password").performTextInput("secret1")
        composeRule.onNodeWithText("Create account").performClick()
        waitForText("Welcome to FitLife")
        composeRule.onNodeWithText("Welcome to FitLife").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AppRoute.Onboarding), backStack.toList())
        }
    }

    @Test
    fun signUp_backButtonReturnsToSignIn() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AuthDestination.Splash)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = PendingGoogleAuthRepository,
                    googleClientId = "test-web-client-id",
                    googleSignInLauncher = FakeGoogleSignInLauncher,
                    onboardingCompletionReader = FakeOnboardingCompletionReader(false),
                    determineStartupDestination = { StartupDestination.Auth },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        composeRule.onNodeWithText("Register").performClick()
        composeRule.onNodeWithText("Login").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AuthDestination.SignIn, AuthDestination.SignUp), backStack.toList())
        }

        composeRule.onNodeWithText("Login").performClick()
        composeRule.onNodeWithText("Login").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AuthDestination.SignIn), backStack.toList())
        }
    }

    @Test
    fun signUp_typedKeyRestoresAfterStateSave() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var backStack: NavBackStack<NavKey>

        restorationTester.setContent {
            backStack = rememberNavBackStack(AuthDestination.Splash)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = PendingGoogleAuthRepository,
                    googleClientId = "test-web-client-id",
                    googleSignInLauncher = FakeGoogleSignInLauncher,
                    onboardingCompletionReader = FakeOnboardingCompletionReader(false),
                    determineStartupDestination = { StartupDestination.Auth },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForText("Register")
        composeRule.onNodeWithText("Register").performClick()
        waitForText("Create Account")
        composeRule.onNodeWithText("Create Account").assertIsDisplayed()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText("Create Account").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AuthDestination.SignIn, AuthDestination.SignUp), backStack.toList())
        }
    }

    @Test
    fun forgotPasswordDestination_rendersResetScreen() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AuthDestination.ForgotPassword)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = PendingGoogleAuthRepository,
                    googleClientId = "test-web-client-id",
                    googleSignInLauncher = FakeGoogleSignInLauncher,
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForText("Forgot password?")
        composeRule.onNodeWithText("Forgot password?").assertIsDisplayed()
        composeRule.onNodeWithText("Send reset email").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AuthDestination.ForgotPassword), backStack.toList())
        }
    }

    @Test
    fun signInForgotPasswordLink_navigatesToResetScreen() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AuthDestination.SignIn)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = PendingGoogleAuthRepository,
                    googleClientId = "test-web-client-id",
                    googleSignInLauncher = FakeGoogleSignInLauncher,
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        composeRule.onNodeWithText("Forgot password?").performClick()
        composeRule.onNodeWithText("Send Reset Link").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(
                listOf(AuthDestination.SignIn, AuthDestination.ForgotPassword),
                backStack.toList()
            )
        }
    }

    @Test
    fun forgotPasswordBackButtonRestoresSignInWhenItIsTheRoot() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AuthDestination.ForgotPassword)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = PendingGoogleAuthRepository,
                    googleClientId = "test-web-client-id",
                    googleSignInLauncher = FakeGoogleSignInLauncher,
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForText("Back to login")
        composeRule.onNodeWithText("Back to login").performClick()
        waitForText("Login")
        composeRule.onNodeWithText("Login").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AuthDestination.SignIn), backStack.toList())
        }
    }

    @Test
    fun signUpBackButtonRestoresSignInWhenItIsTheRoot() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AuthDestination.SignUp)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = PendingGoogleAuthRepository,
                    googleClientId = "test-web-client-id",
                    googleSignInLauncher = FakeGoogleSignInLauncher,
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        composeRule.onNodeWithText("Login").performClick()
        composeRule.onNodeWithText("Login").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AuthDestination.SignIn), backStack.toList())
        }
    }

    @Test
    fun onboardingLevelSelection_navigatesToBeginnerBranchAndRemovesOnboarding() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AppRoute.Onboarding)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = VerifiedAuthRepository,
                    authSessionReader = FakeAuthSessionReader(
                        AuthSession(userId = "beginner-user", isEmailVerified = true)
                    ),
                    onboardingRepository = InMemoryOnboardingRepository(),
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForContentDescription("Select Beginner")
        composeRule.onNodeWithContentDescription("Select Beginner").performClick()
        composeRule.onNodeWithText("Beginner onboarding").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AppRoute.BeginnerOnboarding), backStack.toList())
        }
    }

    @Test
    fun onboardingLevelSelection_navigatesToIntermediateBranchAndRemovesOnboarding() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AppRoute.Onboarding)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = VerifiedAuthRepository,
                    authSessionReader = FakeAuthSessionReader(
                        AuthSession(userId = "intermediate-user", isEmailVerified = true)
                    ),
                    onboardingRepository = InMemoryOnboardingRepository(),
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForContentDescription("Select Intermediate")
        composeRule.onNodeWithContentDescription("Select Intermediate").performClick()
        composeRule.onNodeWithText("Intermediate onboarding").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AppRoute.IntermediateOnboarding), backStack.toList())
        }
    }

    @Test
    fun onboardingBranchContinue_navigatesToHomeAfterCompletion() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AppRoute.BeginnerOnboarding)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = VerifiedAuthRepository,
                    authSessionReader = FakeAuthSessionReader(
                        AuthSession(userId = "beginner-user", isEmailVerified = true)
                    ),
                    onboardingRepository = InMemoryOnboardingRepository(),
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        composeRule.onNodeWithText("What do you want to improve?").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Goal Weight loss").performClick()
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("What equipment do you have?").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Equipment Dumbbells").performClick()
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("How often do you want to train?").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Frequency 3 days per week").performClick()
        composeRule.onNodeWithText("Finish").performClick()
        waitForText("Home")
        assertShellTabsVisible()
        composeRule.runOnIdle {
            assertEquals(listOf(AppRoute.Shell), backStack.toList())
        }
    }

    @Test
    fun intermediateBranchContinue_navigatesToHomeAfterCompletion() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AppRoute.IntermediateOnboarding)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = VerifiedAuthRepository,
                    authSessionReader = FakeAuthSessionReader(
                        AuthSession(userId = "intermediate-user", isEmailVerified = true)
                    ),
                    onboardingRepository = InMemoryOnboardingRepository(),
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        composeRule.onNodeWithText("Choose your split").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Split Full Body").performClick()
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Pick your goals").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Goal Strength").performClick()
        composeRule.onNodeWithText("Continue").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Estimate your 1RM").assertIsDisplayed()
        composeRule.onNodeWithText("Finish").assertIsDisplayed()
        composeRule.onNodeWithText("Finish").performClick()
        waitForText("Home")
        assertShellTabsVisible()
        composeRule.runOnIdle {
            assertEquals(listOf(AppRoute.Shell), backStack.toList())
        }
    }

    @Test
    fun beginnerBranch_unverifiedSessionReturnsToAuth() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AppRoute.BeginnerOnboarding)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = VerifiedAuthRepository,
                    authSessionReader = FakeAuthSessionReader(
                        AuthSession(userId = "unverified-user", isEmailVerified = false)
                    ),
                    onboardingRepository = InMemoryOnboardingRepository(),
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForText("Login")
        composeRule.onNodeWithText("Login").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AuthDestination.SignIn), backStack.toList())
        }
    }

    @Test
    fun intermediateBranch_missingSessionReturnsToAuth() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AppRoute.IntermediateOnboarding)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = VerifiedAuthRepository,
                    authSessionReader = FakeAuthSessionReader(null),
                    onboardingRepository = InMemoryOnboardingRepository(),
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForText("Login")
        composeRule.onNodeWithText("Login").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AuthDestination.SignIn), backStack.toList())
        }
    }

    @Test
    fun profileTab_signOutReplacesRootWithAuth() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AppRoute.Shell)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = VerifiedAuthRepository,
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForText("Home")
        composeRule.onNodeWithContentDescription("Profile tab", useUnmergedTree = true).performClick()
        waitForText("Profile")
        composeRule.onNodeWithText("Sign out").performClick()
        waitForText("Login")
        composeRule.onNodeWithText("Login").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AuthDestination.SignIn), backStack.toList())
        }
    }

    @Test
    fun profileTab_deleteAccountReplacesRootWithAuth() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AppRoute.Shell)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = VerifiedAuthRepository,
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForText("Home")
        composeRule.onNodeWithContentDescription("Profile tab", useUnmergedTree = true).performClick()
        waitForText("Profile")
        composeRule.onNodeWithText("Delete account").performClick()
        composeRule.onNodeWithText("Delete permanently").performClick()
        waitForText("Login")
        composeRule.onNodeWithText("Login").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(listOf(AuthDestination.SignIn), backStack.toList())
        }
    }

    @Test
    fun googleSignInAction_navigatesToHomeAndRemovesAuth() {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AuthDestination.Splash)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = GoogleAuthRepository,
                    googleClientId = "test-web-client-id",
                    googleSignInLauncher = FakeGoogleSignInLauncher,
                    onboardingCompletionReader = FakeOnboardingCompletionReader(true),
                    determineStartupDestination = { StartupDestination.Auth },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForText("Sign in with Google")
        composeRule.onNodeWithText("Sign in with Google").performClick()
        waitForText("Home")
        assertShellTabsVisible()
        composeRule.runOnIdle {
            assertEquals(listOf(AppRoute.Shell), backStack.toList())
        }
    }

    @Test
    fun shellTabSwitching_preservesSelectedTabAndHomeBackStackAcrossStateRestore() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var backStack: NavBackStack<NavKey>

        restorationTester.setContent {
            backStack = rememberNavBackStack(AuthDestination.Splash)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    determineStartupDestination = { StartupDestination.Home },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForText("Home")
        composeRule.onNodeWithText("Open home detail").performClick()
        waitForText("Home detail")
        composeRule.onNodeWithContentDescription("Workout tab", useUnmergedTree = true).performClick()
        waitForText("Your workout planning tools will live here.")

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithContentDescription("Workout tab", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("Your workout planning tools will live here.")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Home tab", useUnmergedTree = true).performClick()
        waitForText("Home detail")
        composeRule.onNodeWithText("Home detail").assertIsDisplayed()

        composeRule.runOnIdle {
            assertEquals(listOf(AppRoute.Shell), backStack.toList())
        }
    }

    @Test
    fun googleSignInLaunch_disablesOtherAuthActionsUntilResultReturns() {
        val launchStarted = CompletableDeferred<Unit>()
        val releaseLaunch = CompletableDeferred<Unit>()
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AuthDestination.SignIn)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = PendingGoogleAuthRepository,
                    googleClientId = "test-web-client-id",
                    googleSignInLauncher = BlockingGoogleSignInLauncher(
                        launchStarted = launchStarted,
                        releaseLaunch = releaseLaunch
                    ),
                    onboardingCompletionReader = FakeOnboardingCompletionReader(false),
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
                    onboardingCompletionReader = FakeOnboardingCompletionReader(false),
                    determineStartupDestination = { StartupDestination.Auth },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForText("FitLife AI")
        composeRule.onNodeWithText("FitLife AI").assertIsDisplayed()
        composeRule.onNodeWithText("Welcome back").assertIsDisplayed()
        composeRule.onNodeWithText("Please enter your details to login.").assertIsDisplayed()
        composeRule.onNodeWithText("Sign in with Google").assertIsDisplayed()
        composeRule.onNodeWithText("Login").assertIsDisplayed()
        composeRule.onNodeWithText("Training floor").assertExists()
        composeRule.onNodeWithText("Smart metrics").assertExists()
    }

    @Test
    fun signUpScreen_rendersRedesignedLayout() {
        composeRule.setContent {
            FitnessAppTheme {
                FitLifeApp(
                    authRepository = PendingGoogleAuthRepository,
                    googleClientId = "test-web-client-id",
                    googleSignInLauncher = FakeGoogleSignInLauncher,
                    onboardingCompletionReader = FakeOnboardingCompletionReader(false),
                    determineStartupDestination = { StartupDestination.Auth },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForText("Register")
        composeRule.onNodeWithText("Register").performClick()
        waitForText("Create Account")
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
        onboardingComplete: Boolean,
        expectedRoute: AppRoute,
        expectedTitle: String
    ) {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AuthDestination.Splash)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    authRepository = VerifiedAuthRepository,
                    onboardingCompletionReader = FakeOnboardingCompletionReader(onboardingComplete),
                    determineStartupDestination = { StartupDestination.Auth },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForText(expectedTitle)
        assertTrue(composeRule.onAllNodesWithText(expectedTitle).fetchSemanticsNodes().isNotEmpty())
        composeRule.runOnIdle {
            assertEquals(listOf(expectedRoute), backStack.toList())
        }
    }

    private fun assertStartupNavigation(
        destination: StartupDestination,
        expectedRoute: NavKey,
        expectedTitle: String
    ) {
        lateinit var backStack: NavBackStack<NavKey>

        composeRule.setContent {
            backStack = rememberNavBackStack(AuthDestination.Splash)

            FitnessAppTheme {
                FitLifeApp(
                    backStack = backStack,
                    determineStartupDestination = { destination },
                    startupRouteErrorLogger = NoOpStartupRouteErrorLogger
                )
            }
        }

        waitForText(expectedTitle)
        assertTrue(composeRule.onAllNodesWithText(expectedTitle).fetchSemanticsNodes().isNotEmpty())
        composeRule.runOnIdle {
            assertEquals(listOf(expectedRoute), backStack.toList())
        }
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForContentDescription(contentDescription: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithContentDescription(contentDescription)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun assertShellTabsVisible() {
        assertTrue(composeRule.onAllNodesWithContentDescription("Home tab", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
        assertTrue(composeRule.onAllNodesWithContentDescription("Workout tab", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
        assertTrue(composeRule.onAllNodesWithContentDescription("Progress tab", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
        assertTrue(composeRule.onAllNodesWithContentDescription("Profile tab", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty())
    }

    private fun assertShellTabsHidden() {
        assertTrue(composeRule.onAllNodesWithContentDescription("Home tab", useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithContentDescription("Workout tab", useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithContentDescription("Progress tab", useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithContentDescription("Profile tab", useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
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

        override suspend fun resetPassword(email: String): Result<Unit, AuthError> =
            Result.Success(Unit)

        override suspend fun deleteAccount(): Result<Unit, AuthError> =
            Result.Success(Unit)

        override suspend fun signOut(): Result<Unit, AuthError> = Result.Success(Unit)

        override suspend fun currentUser(): Result<AuthUser?, AuthError> = Result.Success(user)

        override suspend fun sendEmailVerification(): Result<Unit, AuthError> =
            Result.Success(Unit)

        override suspend fun refreshCurrentUser(): Result<AuthUser?, AuthError> =
            Result.Success(user)
    }

    private class InMemoryOnboardingRepository : OnboardingRepository {
        private var selectedLevel: FitnessLevel? = null
        private var beginnerDraft: BeginnerOnboardingDraft = BeginnerOnboardingDraft()
        private var intermediateDraft: IntermediateOnboardingDraft = IntermediateOnboardingDraft()
        private var onboardingComplete: Boolean = false

        override suspend fun getSelectedFitnessLevel(): Result<FitnessLevel?, OnboardingError> =
            Result.Success(selectedLevel)

        override suspend fun saveSelectedFitnessLevel(level: FitnessLevel): Result<Unit, OnboardingError> {
            selectedLevel = level
            return Result.Success(Unit)
        }

        override suspend fun getBeginnerDraft(): Result<BeginnerOnboardingDraft, OnboardingError> =
            Result.Success(beginnerDraft)

        override suspend fun saveBeginnerDraft(draft: BeginnerOnboardingDraft): Result<Unit, OnboardingError> {
            beginnerDraft = draft
            return Result.Success(Unit)
        }

        override suspend fun syncBeginnerProfile(
            userId: String,
            draft: BeginnerOnboardingDraft
        ): Result<Unit, OnboardingError> {
            beginnerDraft = draft
            return Result.Success(Unit)
        }

        override suspend fun isOnboardingComplete(
            userId: String
        ): Result<Boolean, OnboardingError> = Result.Success(onboardingComplete)

        override suspend fun markOnboardingComplete(
            userId: String
        ): Result<Unit, OnboardingError> {
            onboardingComplete = true
            return Result.Success(Unit)
        }

        override suspend fun getIntermediateDraft(): Result<IntermediateOnboardingDraft, OnboardingError> =
            Result.Success(intermediateDraft)

        override suspend fun saveIntermediateDraft(
            draft: IntermediateOnboardingDraft
        ): Result<Unit, OnboardingError> {
            intermediateDraft = draft
            return Result.Success(Unit)
        }

        override suspend fun syncIntermediateProfile(
            userId: String,
            draft: IntermediateOnboardingDraft
        ): Result<Unit, OnboardingError> {
            intermediateDraft = draft
            return Result.Success(Unit)
        }
    }

    private class FakeAuthSessionReader(
        private val session: AuthSession?
    ) : AuthSessionReader {
        override suspend fun currentSession(): AuthSession? = session
    }

    private class FakeOnboardingCompletionReader(
        private val isComplete: Boolean,
        private val failure: Throwable? = null
    ) : OnboardingCompletionReader {
        override suspend fun isOnboardingComplete(userId: String): Boolean {
            failure?.let { throw it }
            return isComplete
        }
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

        override suspend fun resetPassword(email: String): Result<Unit, AuthError> =
            Result.Success(Unit)

        override suspend fun deleteAccount(): Result<Unit, AuthError> =
            Result.Success(Unit)

        override suspend fun signOut(): Result<Unit, AuthError> = Result.Success(Unit)

        override suspend fun currentUser(): Result<AuthUser?, AuthError> = Result.Success(user)

        override suspend fun sendEmailVerification(): Result<Unit, AuthError> =
            Result.Success(Unit)

        override suspend fun refreshCurrentUser(): Result<AuthUser?, AuthError> =
            Result.Success(user)
    }

    private object UnverifiedAuthRepository : AuthRepository {
        private val user = AuthUser(
            id = "unverified-user",
            email = "unverified@example.com",
            isEmailVerified = false
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

        override suspend fun resetPassword(email: String): Result<Unit, AuthError> =
            Result.Success(Unit)

        override suspend fun deleteAccount(): Result<Unit, AuthError> =
            Result.Success(Unit)

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

        override suspend fun resetPassword(email: String): Result<Unit, AuthError> =
            Result.Success(Unit)

        override suspend fun deleteAccount(): Result<Unit, AuthError> =
            Result.Success(Unit)

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
