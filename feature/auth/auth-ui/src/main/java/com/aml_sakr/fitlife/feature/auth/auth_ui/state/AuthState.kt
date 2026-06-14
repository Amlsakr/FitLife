package com.aml_sakr.fitlife.feature.auth.auth_ui.state

import androidx.annotation.StringRes
import com.aml_sakr.fitlife.core.ui.mvi.UIState

enum class AuthMode {
    SignIn,
    SignUp,
    VerifyEmail
}

data class AuthState(
    val mode: AuthMode = AuthMode.SignIn,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    @param:StringRes val emailErrorResId: Int? = null,
    @param:StringRes val passwordErrorResId: Int? = null,
    @param:StringRes val confirmPasswordErrorResId: Int? = null,
    val verificationEmail: String? = null,
    val isLoading: Boolean = false
) : UIState
