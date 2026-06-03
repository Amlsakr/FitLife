package com.aml_sakr.fitlife.core.data.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryPreferencesDataSource : PreferencesDataSource {
    private val values = MutableStateFlow<Map<String, Any>>(emptyMap())

    override fun booleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> =
        values.map { it[key] as? Boolean ?: defaultValue }

    override suspend fun putBoolean(key: String, value: Boolean) {
        putValue(key, value)
    }

    override fun stringFlow(key: String, defaultValue: String): Flow<String> =
        values.map { it[key] as? String ?: defaultValue }

    override suspend fun putString(key: String, value: String) {
        putValue(key, value)
    }

    override fun longFlow(key: String, defaultValue: Long): Flow<Long> =
        values.map { it[key] as? Long ?: defaultValue }

    override suspend fun putLong(key: String, value: Long) {
        putValue(key, value)
    }

    private fun putValue(key: String, value: Any) {
        values.value = values.value + (key to value)
    }
}
