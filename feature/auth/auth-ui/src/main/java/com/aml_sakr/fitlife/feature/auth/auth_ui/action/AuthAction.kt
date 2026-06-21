package com.aml_sakr.fitlife.feature.auth.auth_ui.action

import androidx.annotation.StringRes
import com.aml_sakr.fitlife.core.ui.mvi.OneTimeAction
import com.aml_sakr.fitlife.feature.auth.domain.model.AuthUser

sealed interface AuthAction : OneTimeAction {
    data class NavigateToAuthenticatedUser(val user: AuthUser) : AuthAction
    data object NavigateToOnboarding : AuthAction
    data object NavigateToSignIn : AuthAction
    data object LaunchGoogleSignIn : AuthAction
    data class ShowMessage(@param:StringRes val messageResId: Int) : AuthAction
}
