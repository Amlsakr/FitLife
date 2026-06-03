package com.aml_sakr.fitlife.core.data.preferences

import kotlinx.coroutines.flow.Flow

interface PreferencesDataSource {
    fun booleanFlow(key: String, defaultValue: Boolean): Flow<Boolean>
    suspend fun putBoolean(key: String, value: Boolean)

    fun stringFlow(key: String, defaultValue: String): Flow<String>
    suspend fun putString(key: String, value: String)

    fun longFlow(key: String, defaultValue: Long): Flow<Long>
    suspend fun putLong(key: String, value: Long)
}
