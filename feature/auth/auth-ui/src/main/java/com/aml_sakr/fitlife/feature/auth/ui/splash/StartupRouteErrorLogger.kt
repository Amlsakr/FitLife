package com.aml_sakr.fitlife.feature.auth.ui.splash

fun interface StartupRouteErrorLogger {
    fun logStartupRouteFailure(throwable: Throwable)
}
