package com.aml_sakr.fitlife.feature.auth.auth_ui.splash

fun interface StartupRouteErrorLogger {
    fun logStartupRouteFailure(throwable: Throwable)
}
