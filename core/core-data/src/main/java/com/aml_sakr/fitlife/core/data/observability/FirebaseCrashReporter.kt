package com.aml_sakr.fitlife.core.data.observability

import com.aml_sakr.fitlife.core.domain.CrashReporter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

class FirebaseCrashReporter internal constructor(
    private val sink: FirebaseCrashlyticsSink
) : CrashReporter {
    @Inject
    constructor(crashlytics: FirebaseCrashlytics) : this(
        FirebaseCrashlyticsSdkSink(crashlytics)
    )

    override fun recordException(throwable: Throwable, keys: Map<String, String>) {
        keys.forEach { (key, value) ->
            sink.setCustomKey(key, value)
        }
        sink.recordException(throwable)
    }
}

internal interface FirebaseCrashlyticsSink {
    fun setCustomKey(key: String, value: String)
    fun recordException(throwable: Throwable)
}

private class FirebaseCrashlyticsSdkSink(
    private val crashlytics: FirebaseCrashlytics
) : FirebaseCrashlyticsSink {
    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
}
