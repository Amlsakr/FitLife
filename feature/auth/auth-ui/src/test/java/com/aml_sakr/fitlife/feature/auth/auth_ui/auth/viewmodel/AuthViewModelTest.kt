package com.aml_sakr.fitlife.feature.auth.auth_ui.auth.viewmodel

import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.auth.auth_ui.R
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser
import com.aml_sakr.fitlife.feature.auth.domain.repository.AuthRepository
import com.aml_sakr.fitlife.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.RefreshCurrentUserUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.ResendEmailVerificationUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignInUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignInWithGoogleUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignOutUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignUpUseCase
import com.aml_sakr.fitlife.feature.auth.auth_ui.action.AuthAction
import com.aml_sakr.fitlife.feature.auth.auth_ui.event.AuthEvent
import com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthMode
import com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthState
import com.aml_sakr.fitlife.feature.auth.auth_ui.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialLoad_showsVerification_whenCurrentUserIsUnverified() = runTest(dispatcher) {
        val user = AuthUser("user-1", "amal@example.com", false)
        val viewModel = createViewModel(
            FakeAuthRepository(currentUserResult = Result.Success(user))
        )

        advanceUntilIdle()

        assertEquals(AuthMode.VerifyEmail, viewModel.state.value.mode)
        assertEquals("amal@example.com", viewModel.state.value.verificationEmail)
    }

    @Test
    fun submitSignUp_setsFieldErrors_withoutCallingRepository() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(AuthEvent.ShowSignUp)
        viewModel.onEvent(AuthEvent.EmailChanged("not-an-email"))
        viewModel.onEvent(AuthEvent.PasswordChanged("123"))
        viewModel.onEvent(AuthEvent.ConfirmPasswordChanged("different"))
        viewModel.onEvent(AuthEvent.Submit)

        val state = viewModel.state.value
        assertEquals(R.string.auth_error_invalid_email, state.emailErrorResId)
        assertEquals(R.string.auth_error_min_password_length, state.passwordErrorResId)
        assertEquals(R.string.auth_error_passwords_do_not_match, state.confirmPasswordErrorResId)
        assertEquals(0, repository.signUpCount)
    }

    @Test
    fun successfulSignUp_entersVerificationState_andClearsPasswords() = runTest(dispatcher) {
        val user = AuthUser("user-1", "amal@example.com", false)
        val repository = FakeAuthRepository(signUpResult = Result.Success(user))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        enterCredentials(viewModel, signUp = true)
        viewModel.onEvent(AuthEvent.Submit)
        advanceUntilIdle()

        assertEquals(AuthMode.VerifyEmail, viewModel.state.value.mode)
        assertEquals("amal@example.com", viewModel.state.value.verificationEmail)
        assertEquals("", viewModel.state.value.password)
        assertEquals("", viewModel.state.value.confirmPassword)
        assertEquals(
            AuthAction.ShowMessage(R.string.auth_message_verification_email_sent_after_sign_up),
            viewModel.actions.first()
        )
    }

    @Test
    fun signUpVerificationDispatchFailure_stillEntersVerificationState() = runTest(dispatcher) {
        val viewModel = createViewModel(
            FakeAuthRepository(
                signUpResult = Result.Failure(AuthError.VerificationEmailFailed)
            )
        )
        advanceUntilIdle()

        enterCredentials(viewModel, signUp = true)
        viewModel.onEvent(AuthEvent.Submit)
        advanceUntilIdle()

        assertEquals(AuthMode.VerifyEmail, viewModel.state.value.mode)
        assertEquals("amal@example.com", viewModel.state.value.verificationEmail)
        assertEquals(
            AuthAction.ShowMessage(
                R.string.auth_error_verification_email_failed
            ),
            viewModel.actions.first()
        )
    }

    @Test
    fun successfulVerifiedSignIn_emitsAuthenticatedNavigation() = runTest(dispatcher) {
        val user = AuthUser("user-1", "amal@example.com", true)
        val viewModel = createViewModel(
            FakeAuthRepository(signInResult = Result.Success(user))
        )
        advanceUntilIdle()

        enterCredentials(viewModel)
        viewModel.onEvent(AuthEvent.Submit)
        advanceUntilIdle()

        assertEquals(AuthAction.NavigateToAuthenticatedUser, viewModel.actions.first())
    }

    @Test
    fun successfulUnverifiedSignIn_staysInAuthVerificationState() = runTest(dispatcher) {
        val user = AuthUser("user-1", "amal@example.com", false)
        val viewModel = createViewModel(
            FakeAuthRepository(signInResult = Result.Success(user))
        )
        advanceUntilIdle()

        enterCredentials(viewModel)
        viewModel.onEvent(AuthEvent.Submit)
        advanceUntilIdle()

        assertEquals(AuthMode.VerifyEmail, viewModel.state.value.mode)
        assertEquals(
            AuthAction.ShowMessage(R.string.auth_message_verify_email_before_continuing),
            viewModel.actions.first()
        )
    }

    @Test
    fun authFailure_emitsSafeMessage_withoutRawFirebaseText() = runTest(dispatcher) {
        val viewModel = createViewModel(
            FakeAuthRepository(signInResult = Result.Failure(AuthError.InvalidCredentials))
        )
        advanceUntilIdle()

        enterCredentials(viewModel)
        viewModel.onEvent(AuthEvent.Submit)
        advanceUntilIdle()

        assertEquals(
            AuthAction.ShowMessage(R.string.auth_error_invalid_credentials),
            viewModel.actions.first()
        )
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun googleSignInRequested_emitsLaunchAction() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeAuthRepository())
        advanceUntilIdle()

        viewModel.onEvent(AuthEvent.GoogleSignInRequested)

        assertEquals(AuthAction.LaunchGoogleSignIn, viewModel.actions.first())
        assertTrue(viewModel.state.value.isGoogleSignInInProgress)
    }

    @Test
    fun googleSignInDismissed_clearsLaunchGuardWithoutError() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeAuthRepository())
        advanceUntilIdle()

        viewModel.onEvent(AuthEvent.GoogleSignInRequested)
        viewModel.onEvent(AuthEvent.GoogleSignInDismissed)

        assertFalse(viewModel.state.value.isGoogleSignInInProgress)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun successfulGoogleSignIn_emitsAuthenticatedNavigation() = runTest(dispatcher) {
        val user = AuthUser("user-1", "amal@example.com", true)
        val viewModel = createViewModel(
            FakeAuthRepository(
                googleSignInResult = Result.Success(user)
            )
        )
        advanceUntilIdle()

        viewModel.onEvent(AuthEvent.GoogleSignInTokenReceived("google-id-token"))
        advanceUntilIdle()

        assertEquals(AuthAction.NavigateToAuthenticatedUser, viewModel.actions.first())
    }

    @Test
    fun googleSignInAccountSetupFailure_emitsSafeMessage() = runTest(dispatcher) {
        val viewModel = createViewModel(
            FakeAuthRepository(
                googleSignInResult = Result.Failure(AuthError.GoogleAccountSetupFailed)
            )
        )
        advanceUntilIdle()

        viewModel.onEvent(AuthEvent.GoogleSignInTokenReceived("google-id-token"))
        advanceUntilIdle()

        assertEquals(
            AuthAction.ShowMessage(R.string.auth_error_google_account_setup_failed),
            viewModel.actions.first()
        )
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun signOutCredentialCleanupFailure_stillNavigatesToSignIn_withoutReportingError() = runTest(
        dispatcher
    ) {
        val viewModel = createViewModel(
            FakeAuthRepository(
                currentUserResult = Result.Success(null),
                signOutResult = Result.Success(Unit)
            )
        )
        advanceUntilIdle()

        viewModel.onEvent(AuthEvent.SignOut)
        advanceUntilIdle()

        assertEquals(AuthState(), viewModel.state.value)
        assertEquals(AuthAction.NavigateToSignIn, viewModel.actions.first())
    }

    @Test
    fun duplicateSubmit_isIgnoredWhileRequestIsActive() = runTest(dispatcher) {
        val repository = FakeAuthRepository(suspendSignIn = true)
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        enterCredentials(viewModel)
        viewModel.onEvent(AuthEvent.Submit)
        runCurrent()
        viewModel.onEvent(AuthEvent.Submit)
        runCurrent()

        assertEquals(1, repository.signInCount)
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun refreshVerification_navigatesOnlyAfterReloadedUserIsVerified() = runTest(dispatcher) {
        val unverified = AuthUser("user-1", "amal@example.com", false)
        val verified = unverified.copy(isEmailVerified = true)
        val viewModel = createViewModel(
            FakeAuthRepository(
                currentUserResult = Result.Success(unverified),
                refreshResult = Result.Success(verified)
            )
        )
        advanceUntilIdle()

        viewModel.onEvent(AuthEvent.RefreshVerification)
        advanceUntilIdle()

        assertEquals(AuthAction.NavigateToAuthenticatedUser, viewModel.actions.first())
    }

    @Test
    fun refreshVerification_returnsToEnabledSignIn_whenSessionHasEnded() = runTest(dispatcher) {
        val unverified = AuthUser("user-1", "amal@example.com", false)
        val viewModel = createViewModel(
            FakeAuthRepository(
                currentUserResult = Result.Success(unverified),
                refreshResult = Result.Success(null)
            )
        )
        advanceUntilIdle()

        viewModel.onEvent(AuthEvent.RefreshVerification)
        advanceUntilIdle()

        assertEquals(AuthMode.SignIn, viewModel.state.value.mode)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.verificationEmail)
        assertEquals(AuthAction.NavigateToSignIn, viewModel.actions.first())
    }

    @Test
    fun submit_rejectsMalformedEmail_withoutCallingRepository() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(AuthEvent.EmailChanged("amal@fitlife..com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("secret1"))
        viewModel.onEvent(AuthEvent.Submit)

        assertEquals(R.string.auth_error_invalid_email, viewModel.state.value.emailErrorResId)
        assertEquals(0, repository.signInCount)
    }

    @Test
    fun resendVerification_emitsConfirmation() = runTest(dispatcher) {
        val user = AuthUser("user-1", "amal@example.com", false)
        val viewModel = createViewModel(
            FakeAuthRepository(
                currentUserResult = Result.Success(user),
                verificationResult = Result.Success(Unit)
            )
        )
        advanceUntilIdle()

        viewModel.onEvent(AuthEvent.ResendVerification)
        advanceUntilIdle()

        assertEquals(
            AuthAction.ShowMessage(R.string.auth_message_verification_email_resent),
            viewModel.actions.first()
        )
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun signOut_clearsVerificationState_andEmitsSignInNavigation() = runTest(dispatcher) {
        val user = AuthUser("user-1", "amal@example.com", false)
        val viewModel = createViewModel(
            FakeAuthRepository(
                currentUserResult = Result.Success(user),
                signOutResult = Result.Success(Unit)
            )
        )
        advanceUntilIdle()

        viewModel.onEvent(AuthEvent.SignOut)
        advanceUntilIdle()

        assertEquals(AuthMode.SignIn, viewModel.state.value.mode)
        assertNull(viewModel.state.value.verificationEmail)
        assertEquals(AuthAction.NavigateToSignIn, viewModel.actions.first())
    }

    private fun enterCredentials(
        viewModel: AuthViewModel,
        signUp: Boolean = false
    ) {
        if (signUp) viewModel.onEvent(AuthEvent.ShowSignUp)
        viewModel.onEvent(AuthEvent.EmailChanged("amal@example.com"))
        viewModel.onEvent(AuthEvent.PasswordChanged("secret1"))
        if (signUp) {
            viewModel.onEvent(AuthEvent.ConfirmPasswordChanged("secret1"))
        }
    }

    private fun createViewModel(repository: AuthRepository): AuthViewModel =
        AuthViewModel(
            signUp = SignUpUseCase(repository),
            signIn = SignInUseCase(repository),
            signInWithGoogle = SignInWithGoogleUseCase(repository),
            signOut = SignOutUseCase(repository),
            getCurrentUser = GetCurrentUserUseCase(repository),
            resendEmailVerification = ResendEmailVerificationUseCase(repository),
            refreshCurrentUser = RefreshCurrentUserUseCase(repository)
        )

    private class FakeAuthRepository(
        private val signUpResult: Result<AuthUser, AuthError> = Result.Failure(AuthError.Unknown),
        private val signInResult: Result<AuthUser, AuthError> = Result.Failure(AuthError.Unknown),
        private val googleSignInResult: Result<AuthUser, AuthError> = Result.Failure(AuthError.Unknown),
        private val signOutResult: Result<Unit, AuthError> = Result.Failure(AuthError.Unknown),
        private val currentUserResult: Result<AuthUser?, AuthError> = Result.Success(null),
        private val verificationResult: Result<Unit, AuthError> = Result.Success(Unit),
        private val refreshResult: Result<AuthUser?, AuthError> = Result.Success(null),
        private val suspendSignIn: Boolean = false
    ) : AuthRepository {
        var signUpCount = 0
        var signInCount = 0
        var googleSignInCount = 0

        override suspend fun signUp(
            email: String,
            password: String
        ): Result<AuthUser, AuthError> {
            signUpCount += 1
            return signUpResult
        }

        override suspend fun signIn(
            email: String,
            password: String
        ): Result<AuthUser, AuthError> {
            signInCount += 1
            if (suspendSignIn) awaitCancellation()
            return signInResult
        }

        override suspend fun signInWithGoogle(
            googleIdToken: String
        ): Result<AuthUser, AuthError> {
            googleSignInCount += 1
            return googleSignInResult
        }

        override suspend fun signOut(): Result<Unit, AuthError> = signOutResult

        override suspend fun currentUser(): Result<AuthUser?, AuthError> = currentUserResult

        override suspend fun sendEmailVerification(): Result<Unit, AuthError> =
            verificationResult

        override suspend fun refreshCurrentUser(): Result<AuthUser?, AuthError> = refreshResult
    }
}
