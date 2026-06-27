package com.aml_sakr.fitlife.core.data.observability

import com.aml_sakr.fitlife.core.domain.CrashReporter

class InMemoryCrashReporter : CrashReporter {
    private val mutableExceptions = mutableListOf<RecordedException>()
    val exceptions: List<RecordedException> = mutableExceptions

    override fun recordException(throwable: Throwable, keys: Map<String, String>) {
        mutableExceptions += RecordedException(
            throwable = throwable,
            keys = keys
        )
    }
}

data class RecordedException(
    val throwable: Throwable,
    val keys: Map<String, String>
)
