package com.aml_sakr.fitlife.feature.auth.auth_ui.navigation

fun interface StartupRouteErrorLogger {
    fun logStartupRouteFailure(throwable: Throwable)
}
