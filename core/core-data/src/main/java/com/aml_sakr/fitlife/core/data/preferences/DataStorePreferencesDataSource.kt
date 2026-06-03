package com.aml_sakr.fitlife.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class DataStorePreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferencesDataSource {
    override fun booleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> =
        safePreferences().map { preferences ->
            preferences[booleanPreferencesKey(key)] ?: defaultValue
        }

    override suspend fun putBoolean(key: String, value: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(key)] = value
        }
    }

    override fun stringFlow(key: String, defaultValue: String): Flow<String> =
        safePreferences().map { preferences ->
            preferences[stringPreferencesKey(key)] ?: defaultValue
        }

    override suspend fun putString(key: String, value: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
    }

    override fun longFlow(key: String, defaultValue: Long): Flow<Long> =
        safePreferences().map { preferences ->
            preferences[longPreferencesKey(key)] ?: defaultValue
        }

    override suspend fun putLong(key: String, value: Long) {
        dataStore.edit { preferences ->
            preferences[longPreferencesKey(key)] = value
        }
    }

    private fun safePreferences(): Flow<Preferences> =
        dataStore.data.catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
}
