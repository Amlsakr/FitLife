package com.aml_sakr.fitlife.feature.auth.auth_ui.state

import androidx.annotation.StringRes
import com.aml_sakr.fitlife.core.ui.mvi.UIState
import com.aml_sakr.fitlife.feature.auth.auth_ui.AuthUiConstants

enum class AuthMode {
    SignIn,
    SignUp
}

data class AuthState(
    val mode: AuthMode = AuthMode.SignIn,
    val email: String = AuthUiConstants.EMPTY_TEXT,
    val password: String = AuthUiConstants.EMPTY_TEXT,
    val confirmPassword: String = AuthUiConstants.EMPTY_TEXT,
    @param:StringRes val emailErrorResId: Int? = null,
    @param:StringRes val passwordErrorResId: Int? = null,
    @param:StringRes val confirmPasswordErrorResId: Int? = null,
    val isDeleteAccountConfirmationVisible: Boolean = false,
    val isGoogleSignInInProgress: Boolean = false,
    val isLoading: Boolean = false
) : UIState
