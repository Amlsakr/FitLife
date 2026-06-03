package com.aml_sakr.fitlife.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.aml_sakr.fitlife.core.data.connectivity.AndroidConnectivityMonitor
import com.aml_sakr.fitlife.core.data.connectivity.ConnectivityMonitor
import com.aml_sakr.fitlife.core.data.preferences.DataStorePreferencesDataSource
import com.aml_sakr.fitlife.core.data.preferences.PreferencesDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.fitLifePreferencesDataStore by preferencesDataStore(
    name = "fitlife_preferences"
)

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreDataBindingsModule {
    @Binds
    @Singleton
    abstract fun bindConnectivityMonitor(
        monitor: AndroidConnectivityMonitor
    ): ConnectivityMonitor

    @Binds
    @Singleton
    abstract fun bindPreferencesDataSource(
        dataSource: DataStorePreferencesDataSource
    ): PreferencesDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object CoreDataModule {
    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.fitLifePreferencesDataStore
}
