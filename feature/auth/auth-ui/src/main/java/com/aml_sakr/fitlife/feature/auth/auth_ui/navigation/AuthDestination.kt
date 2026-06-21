package com.aml_sakr.fitlife.feature.auth.auth_ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthDestination : NavKey {
    @Serializable
    data object Splash : AuthDestination

    @Serializable
    data object SignIn : AuthDestination

    @Serializable
    data object SignUp : AuthDestination

    @Serializable
    data object ForgotPassword : AuthDestination
}
