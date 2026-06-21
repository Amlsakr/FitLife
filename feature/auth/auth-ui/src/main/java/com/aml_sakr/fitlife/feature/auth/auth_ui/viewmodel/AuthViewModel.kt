package com.aml_sakr.fitlife.feature.auth.auth_ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.ui.mvi.BaseMviViewModel
import com.aml_sakr.fitlife.feature.auth.auth_ui.R
import com.aml_sakr.fitlife.feature.auth.auth_ui.AuthUiConstants
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser
import com.aml_sakr.fitlife.feature.auth.domain.usecase.DeleteAccountUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.RefreshCurrentUserUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.ResetPasswordUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.ResendEmailVerificationUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignInUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignInWithGoogleUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignOutUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignUpUseCase
import com.aml_sakr.fitlife.feature.auth.auth_ui.action.AuthAction
import com.aml_sakr.fitlife.feature.auth.auth_ui.component.AuthErrorMessageMapper
import com.aml_sakr.fitlife.feature.auth.auth_ui.event.AuthEvent
import com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthMode
import com.aml_sakr.fitlife.feature.auth.auth_ui.state.AuthState
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signUp: SignUpUseCase,
    private val signIn: SignInUseCase,
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val signOut: SignOutUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val resendEmailVerification: ResendEmailVerificationUseCase,
    private val refreshCurrentUser: RefreshCurrentUserUseCase
) : BaseMviViewModel<AuthState, AuthEvent, AuthAction>(AuthState()) {
    init {
        loadCurrentUser()
    }

    override fun handleEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged -> setState {
                copy(email = event.value, emailErrorResId = null)
            }
            is AuthEvent.PasswordChanged -> setState {
                copy(password = event.value, passwordErrorResId = null)
            }
            is AuthEvent.ConfirmPasswordChanged -> setState {
                copy(confirmPassword = event.value, confirmPasswordErrorResId = null)
            }
            AuthEvent.Submit -> submit()
            AuthEvent.ResetPasswordRequested -> resetPassword()
            AuthEvent.GoogleSignInRequested -> requestGoogleSignIn()
            is AuthEvent.GoogleSignInTokenReceived -> handleGoogleSignIn(event.token)
            is AuthEvent.GoogleSignInFailed -> {
                setState { copy(isLoading = false, isGoogleSignInInProgress = false) }
                sendError(event.error)
            }
            AuthEvent.GoogleSignInDismissed -> setState {
                copy(isGoogleSignInInProgress = false)
            }
            AuthEvent.ShowSignIn -> showSignIn()
            AuthEvent.ShowSignUp -> showSignUp()
            AuthEvent.ResendVerification -> resendVerification()
            AuthEvent.RefreshVerification -> refreshVerification()
            AuthEvent.DeleteAccountRequested -> requestDeleteAccount()
            AuthEvent.DeleteAccountConfirmed -> confirmDeleteAccount()
            AuthEvent.DeleteAccountDismissed -> setState {
                copy(isDeleteAccountConfirmationVisible = false)
            }
            AuthEvent.SignOut -> performSignOut()
        }
    }

    private fun loadCurrentUser() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getCurrentUser()) {
                is Result.Success -> handleLoadedUser(result.value)
                is Result.Failure -> {
                    setState { copy(isLoading = false) }
                    sendError(result.error)
                }
            }
        }
    }

    private fun handleLoadedUser(user: AuthUser?) {
        when {
            user == null -> setState { copy(isLoading = false) }
            else -> {
                setState {
                    copy(
                        isLoading = false,
                        password = AuthUiConstants.EMPTY_TEXT,
                        confirmPassword = AuthUiConstants.EMPTY_TEXT
                    )
                }
                sendAction(AuthAction.NavigateToAuthenticatedUser(user))
            }
        }
    }

    private fun submit() {
        if (state.value.isLoading) return
        val current = state.value
        if (!validate(current)) return

        setState { copy(isLoading = true) }
        viewModelScope.launch {
            val email = current.email.trim()
            val result = if (current.mode == AuthMode.SignUp) {
                signUp(email, current.password)
            } else {
                signIn(email, current.password)
            }

            when (result) {
                is Result.Success -> handleAuthenticationSuccess(
                    user = result.value,
                    wasSignUp = current.mode == AuthMode.SignUp
                )
                is Result.Failure -> {
                    setState { copy(isLoading = false) }
                    sendError(result.error)
                }
            }
        }
    }

    private fun resetPassword() {
        if (state.value.isLoading) return
        val email = state.value.email.trim()
        if (!isValidEmail(email)) {
            setState { copy(emailErrorResId = R.string.auth_error_invalid_email) }
            return
        }

        setState { copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = resetPasswordUseCase(email)) {
                is Result.Success -> {
                    setState { copy(isLoading = false, password = AuthUiConstants.EMPTY_TEXT) }
                    sendAction(AuthAction.ShowMessage(R.string.auth_message_password_reset_sent))
                }
                is Result.Failure -> {
                    setState { copy(isLoading = false) }
                    sendError(result.error)
                }
            }
        }
    }

    private fun handleGoogleSignIn(googleIdToken: String) {
        if (state.value.isLoading) return
        setState { copy(isLoading = true, isGoogleSignInInProgress = false) }
        viewModelScope.launch {
            when (val result = signInWithGoogle(googleIdToken)) {
                is Result.Success -> handleAuthenticationSuccess(
                    user = result.value,
                    wasSignUp = false
                )
                is Result.Failure -> {
                    setState { copy(isLoading = false) }
                    sendError(result.error)
                }
            }
        }
    }

    private fun requestGoogleSignIn() {
        if (state.value.isLoading || state.value.isGoogleSignInInProgress) return
        setState { copy(isGoogleSignInInProgress = true) }
        sendAction(AuthAction.LaunchGoogleSignIn)
    }

    private fun handleAuthenticationSuccess(
        user: AuthUser,
        wasSignUp: Boolean
    ) {
        setState {
            copy(
                isLoading = false,
                password = AuthUiConstants.EMPTY_TEXT,
                confirmPassword = AuthUiConstants.EMPTY_TEXT
            )
        }
        if (wasSignUp && user.isEmailVerified) {
            sendAction(AuthAction.NavigateToOnboarding)
        } else {
            sendAction(AuthAction.NavigateToAuthenticatedUser(user))
        }
    }

    private fun resendVerification() {
        if (state.value.isLoading) return
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = resendEmailVerification()) {
                is Result.Success -> {
                    setState { copy(isLoading = false) }
                    sendAction(AuthAction.ShowMessage(R.string.auth_message_verification_email_resent))
                }
                is Result.Failure -> {
                    setState { copy(isLoading = false) }
                    sendError(result.error)
                }
            }
        }
    }

    private fun refreshVerification() {
        if (state.value.isLoading) return
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = refreshCurrentUser()) {
                is Result.Success -> {
                    val user = result.value
                    when {
                        user == null -> {
                            setState { AuthState() }
                            sendAction(AuthAction.NavigateToSignIn)
                        }
                        else -> {
                            setState { copy(isLoading = false) }
                            sendAction(AuthAction.NavigateToAuthenticatedUser(user))
                        }
                    }
                }
                is Result.Failure -> {
                    setState { copy(isLoading = false) }
                    sendError(result.error)
                }
            }
        }
    }

    private fun requestDeleteAccount() {
        if (state.value.isLoading) return
        setState { copy(isDeleteAccountConfirmationVisible = true) }
    }

    private fun confirmDeleteAccount() {
        if (state.value.isLoading) return
        setState {
            copy(
                isLoading = true,
                isDeleteAccountConfirmationVisible = false
            )
        }
        viewModelScope.launch {
            when (val result = deleteAccountUseCase()) {
                is Result.Success -> {
                    setState { AuthState() }
                    sendAction(AuthAction.NavigateToSignIn)
                }
                is Result.Failure -> {
                    setState { copy(isLoading = false) }
                    sendError(result.error)
                }
            }
        }
    }

    private fun performSignOut() {
        if (state.value.isLoading) return
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = signOut()) {
                is Result.Success -> {
                    setState { AuthState() }
                    sendAction(AuthAction.NavigateToSignIn)
                }
                is Result.Failure -> {
                    when (result.error) {
                        AuthError.GoogleCredentialStateClearFailed -> {
                            setState { AuthState() }
                            sendAction(AuthAction.NavigateToSignIn)
                            sendError(result.error)
                        }
                        else -> {
                            setState { copy(isLoading = false) }
                            sendError(result.error)
                        }
                    }
                }
            }
        }
    }

    private fun showSignIn() {
        if (state.value.isLoading) return
        setState {
            copy(
                mode = AuthMode.SignIn,
                password = AuthUiConstants.EMPTY_TEXT,
                confirmPassword = AuthUiConstants.EMPTY_TEXT,
                emailErrorResId = null,
                passwordErrorResId = null,
                confirmPasswordErrorResId = null,
                isDeleteAccountConfirmationVisible = false,
                isGoogleSignInInProgress = false,
                isLoading = false
            )
        }
    }

    private fun showSignUp() {
        if (state.value.isLoading) return
        setState {
            copy(
                mode = AuthMode.SignUp,
                password = AuthUiConstants.EMPTY_TEXT,
                confirmPassword = AuthUiConstants.EMPTY_TEXT,
                emailErrorResId = null,
                passwordErrorResId = null,
                confirmPasswordErrorResId = null,
                isDeleteAccountConfirmationVisible = false,
                isGoogleSignInInProgress = false,
                isLoading = false
            )
        }
    }

    private fun validate(current: AuthState): Boolean {
        val emailError =
            if (isValidEmail(current.email.trim())) null else R.string.auth_error_invalid_email
        val passwordError =
            if (current.password.length < AuthUiConstants.MINIMUM_PASSWORD_LENGTH) {
                R.string.auth_error_min_password_length
            } else {
                null
            }
        val confirmError =
            if (
                current.mode == AuthMode.SignUp &&
                current.confirmPassword != current.password
            ) {
                R.string.auth_error_passwords_do_not_match
            } else {
                null
            }

        setState {
            copy(
                emailErrorResId = emailError,
                passwordErrorResId = passwordError,
                confirmPasswordErrorResId = confirmError
            )
        }
        return emailError == null && passwordError == null && confirmError == null
    }

    private fun sendError(error: AuthError) {
        sendAction(AuthAction.ShowMessage(AuthErrorMessageMapper.messageResId(error)))
    }

    private fun isValidEmail(email: String): Boolean {
        val parts = email.split(AuthUiConstants.EMAIL_ADDRESS_SEPARATOR)
        if (parts.size != 2) return false

        val localPart = parts[0]
        val domainLabels = parts[1].split(AuthUiConstants.EMAIL_DOMAIN_SEPARATOR)
        return localPart.isNotBlank() &&
            !localPart.startsWith(AuthUiConstants.EMAIL_DOMAIN_SEPARATOR) &&
            !localPart.endsWith(AuthUiConstants.EMAIL_DOMAIN_SEPARATOR) &&
            AuthUiConstants.EMAIL_LOCAL_PART_CONTAINS_SEQUENCE !in localPart &&
            LOCAL_PART_PATTERN.matches(localPart) &&
            domainLabels.size >= 2 &&
            domainLabels.all { label ->
                label.isNotBlank() &&
                    !label.startsWith(AuthUiConstants.EMAIL_DOMAIN_LABEL_DISALLOWED_EDGE) &&
                    !label.endsWith(AuthUiConstants.EMAIL_DOMAIN_LABEL_DISALLOWED_EDGE) &&
                    DOMAIN_LABEL_PATTERN.matches(label)
            }
    }

    private companion object {
        val LOCAL_PART_PATTERN = Regex(AuthUiConstants.LOCAL_PART_REGEX)
        val DOMAIN_LABEL_PATTERN = Regex(AuthUiConstants.DOMAIN_LABEL_REGEX)
    }
}
