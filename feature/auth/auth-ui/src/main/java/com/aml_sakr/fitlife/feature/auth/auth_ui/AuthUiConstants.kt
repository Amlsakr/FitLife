package com.aml_sakr.fitlife.feature.auth.auth_ui

internal object AuthUiConstants {
    const val EMPTY_TEXT = ""
    const val MINIMUM_PASSWORD_LENGTH = 6
    const val SPLASH_DISPLAY_DURATION_MILLIS = 5_000L
    const val CONTINUE_UNAVAILABLE_MESSAGE = "Unable to continue. Please try again."
    const val RETRY_ACTION_LABEL = "Retry"
    const val LOCAL_PART_REGEX = "^[A-Za-z0-9+_.-]+$"
    const val DOMAIN_LABEL_REGEX = "^[A-Za-z0-9-]+$"
    const val EMAIL_ADDRESS_SEPARATOR = '@'
    const val EMAIL_DOMAIN_SEPARATOR = '.'
    const val EMAIL_LOCAL_PART_CONTAINS_SEQUENCE = ".."
    const val EMAIL_DOMAIN_LABEL_DISALLOWED_EDGE = '-'
}
