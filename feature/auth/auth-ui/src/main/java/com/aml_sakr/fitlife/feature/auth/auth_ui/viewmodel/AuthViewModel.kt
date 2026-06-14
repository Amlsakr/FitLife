package com.aml_sakr.fitlife.feature.auth.auth_ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.core.ui.mvi.BaseMviViewModel
import com.aml_sakr.fitlife.feature.auth.auth_ui.R
import com.aml_sakr.fitlife.feature.auth.domain.error.AuthError
import com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser
import com.aml_sakr.fitlife.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.RefreshCurrentUserUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.ResendEmailVerificationUseCase
import com.aml_sakr.fitlife.feature.auth.domain.usecase.SignInUseCase
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
            AuthEvent.ShowSignIn -> showSignIn()
            AuthEvent.ShowSignUp -> showSignUp()
            AuthEvent.ResendVerification -> resendVerification()
            AuthEvent.RefreshVerification -> refreshVerification()
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
            user.isEmailVerified -> {
                setState { copy(isLoading = false) }
                sendAction(AuthAction.NavigateToAuthenticatedUser)
            }
            else -> showVerification(user)
        }
    }

    private fun submit() {
        if (state.value.isLoading) return
        val current = state.value
        if (current.mode == AuthMode.VerifyEmail) return
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
                    if (
                        current.mode == AuthMode.SignUp &&
                        result.error == AuthError.VerificationEmailFailed
                    ) {
                        showVerificationEmail(email)
                    } else {
                        setState { copy(isLoading = false) }
                    }
                    sendError(result.error)
                }
            }
        }
    }

    private fun handleAuthenticationSuccess(
        user: AuthUser,
        wasSignUp: Boolean
    ) {
        if (user.isEmailVerified) {
            setState { copy(isLoading = false, password = "", confirmPassword = "") }
            sendAction(AuthAction.NavigateToAuthenticatedUser)
            return
        }

        showVerification(user)
        sendAction(
            AuthAction.ShowMessage(
                if (wasSignUp) {
                    R.string.auth_message_verification_email_sent_after_sign_up
                } else {
                    R.string.auth_message_verify_email_before_continuing
                }
            )
        )
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
                        user.isEmailVerified -> {
                            setState { copy(isLoading = false) }
                            sendAction(AuthAction.NavigateToAuthenticatedUser)
                        }
                        else -> {
                            showVerification(user)
                            sendAction(
                                AuthAction.ShowMessage(R.string.auth_message_email_not_verified_yet)
                            )
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
                    setState { copy(isLoading = false) }
                    sendError(result.error)
                }
            }
        }
    }

    private fun showSignIn() {
        if (state.value.isLoading) return
        setState {
            copy(
                mode = AuthMode.SignIn,
                password = "",
                confirmPassword = "",
                emailErrorResId = null,
                passwordErrorResId = null,
                confirmPasswordErrorResId = null,
                verificationEmail = null,
                isLoading = false
            )
        }
    }

    private fun showSignUp() {
        if (state.value.isLoading) return
        setState {
            copy(
                mode = AuthMode.SignUp,
                password = "",
                confirmPassword = "",
                emailErrorResId = null,
                passwordErrorResId = null,
                confirmPasswordErrorResId = null,
                verificationEmail = null,
                isLoading = false
            )
        }
    }

    private fun showVerification(user: AuthUser) {
        showVerificationEmail(user.email.orEmpty())
    }

    private fun showVerificationEmail(email: String) {
        setState {
            copy(
                mode = AuthMode.VerifyEmail,
                email = email,
                password = "",
                confirmPassword = "",
                emailErrorResId = null,
                passwordErrorResId = null,
                confirmPasswordErrorResId = null,
                verificationEmail = email.takeIf { it.isNotBlank() },
                isLoading = false
            )
        }
    }

    private fun validate(current: AuthState): Boolean {
        val emailError =
            if (isValidEmail(current.email.trim())) null else R.string.auth_error_invalid_email
        val passwordError =
            if (current.password.length < MINIMUM_PASSWORD_LENGTH) {
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
        val parts = email.split('@')
        if (parts.size != 2) return false

        val localPart = parts[0]
        val domainLabels = parts[1].split('.')
        return localPart.isNotBlank() &&
            !localPart.startsWith('.') &&
            !localPart.endsWith('.') &&
            ".." !in localPart &&
            LOCAL_PART_PATTERN.matches(localPart) &&
            domainLabels.size >= 2 &&
            domainLabels.all { label ->
                label.isNotBlank() &&
                    !label.startsWith('-') &&
                    !label.endsWith('-') &&
                    DOMAIN_LABEL_PATTERN.matches(label)
            }
    }

    private companion object {
        const val MINIMUM_PASSWORD_LENGTH = 6
        val LOCAL_PART_PATTERN = Regex("^[A-Za-z0-9+_.-]+$")
        val DOMAIN_LABEL_PATTERN = Regex("^[A-Za-z0-9-]+$")
    }
}
