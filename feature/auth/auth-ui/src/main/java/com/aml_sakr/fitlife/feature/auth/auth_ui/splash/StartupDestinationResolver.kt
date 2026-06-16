package com.aml_sakr.fitlife.feature.auth.auth_ui.splash

import com.aml_sakr.fitlife.feature.auth.domain.startup.StartupDestination

fun interface StartupDestinationResolver {
    suspend fun resolve(): StartupDestination
}
