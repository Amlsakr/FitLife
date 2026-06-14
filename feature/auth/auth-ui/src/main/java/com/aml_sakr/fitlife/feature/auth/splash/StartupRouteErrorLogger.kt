package com.aml_sakr.fitlife.feature.auth.splash

fun interface StartupRouteErrorLogger {
    fun logStartupRouteFailure(throwable: Throwable)
}
