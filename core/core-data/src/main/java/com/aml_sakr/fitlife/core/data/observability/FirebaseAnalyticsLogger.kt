package com.aml_sakr.fitlife.core.data.observability

import android.os.Bundle
import com.aml_sakr.fitlife.core.domain.AnalyticsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

class FirebaseAnalyticsLogger internal constructor(
    private val sink: FirebaseAnalyticsEventSink
) : AnalyticsLogger {
    @Inject
    constructor(firebaseAnalytics: FirebaseAnalytics) : this(
        FirebaseAnalyticsSdkSink(firebaseAnalytics)
    )

    override fun logEvent(name: String, params: Map<String, Any?>) {
        sink.logEvent(name, FirebaseAnalyticsParameters.from(params))
    }
}

internal interface FirebaseAnalyticsEventSink {
    fun logEvent(name: String, parameters: FirebaseAnalyticsParameters)
}

internal class FirebaseAnalyticsParameters private constructor(
    val values: Map<String, FirebaseAnalyticsParameter>
) {
    fun toBundle(): Bundle =
        Bundle().also { bundle ->
            values.forEach { (key, value) ->
                when (value) {
                    is FirebaseAnalyticsParameter.DoubleValue -> bundle.putDouble(key, value.value)
                    is FirebaseAnalyticsParameter.LongValue -> bundle.putLong(key, value.value)
                    is FirebaseAnalyticsParameter.StringValue -> bundle.putString(key, value.value)
                }
            }
        }

    companion object {
        fun from(params: Map<String, Any?>): FirebaseAnalyticsParameters =
            FirebaseAnalyticsParameters(
                params.mapNotNull { (key, value) ->
                    val parameter = when (value) {
                        null -> null
                        is String -> FirebaseAnalyticsParameter.StringValue(value)
                        is Int -> FirebaseAnalyticsParameter.LongValue(value.toLong())
                        is Long -> FirebaseAnalyticsParameter.LongValue(value)
                        is Float -> FirebaseAnalyticsParameter.DoubleValue(value.toDouble())
                        is Double -> FirebaseAnalyticsParameter.DoubleValue(value)
                        is Boolean -> FirebaseAnalyticsParameter.StringValue(value.toString())
                        else -> FirebaseAnalyticsParameter.StringValue(value.toString())
                    }
                    parameter?.let { key to it }
                }.toMap()
            )
    }
}

internal sealed interface FirebaseAnalyticsParameter {
    data class StringValue(val value: String) : FirebaseAnalyticsParameter
    data class LongValue(val value: Long) : FirebaseAnalyticsParameter
    data class DoubleValue(val value: Double) : FirebaseAnalyticsParameter
}

private class FirebaseAnalyticsSdkSink(
    private val firebaseAnalytics: FirebaseAnalytics
) : FirebaseAnalyticsEventSink {
    override fun logEvent(name: String, parameters: FirebaseAnalyticsParameters) {
        firebaseAnalytics.logEvent(name, parameters.toBundle())
    }
}
