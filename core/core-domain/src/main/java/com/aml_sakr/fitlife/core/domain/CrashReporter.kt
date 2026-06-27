package com.aml_sakr.fitlife.core.domain

interface CrashReporter {
    fun recordException(
        throwable: Throwable,
        keys: Map<String, String> = emptyMap()
    )
}
