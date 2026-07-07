package com.aml_sakr.fitlife.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.aml_sakr.fitlife.core.data.connectivity.AndroidConnectivityMonitor
import com.aml_sakr.fitlife.core.data.connectivity.ConnectivityMonitor
import com.aml_sakr.fitlife.core.data.database.SessionDao
import com.aml_sakr.fitlife.core.data.preferences.DataStorePreferencesDataSource
import com.aml_sakr.fitlife.core.data.preferences.PreferencesDataSource
import com.aml_sakr.fitlife.core.data.workout.WorkoutPlanDao
import com.aml_sakr.fitlife.core.data.workout.WorkoutPlanDatabase
import com.google.gson.Gson
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
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.fitLifePreferencesDataStore

    @Provides
    @Singleton
    fun provideWorkoutPlanDatabase(
        @ApplicationContext context: Context
    ): WorkoutPlanDatabase = Room.databaseBuilder(
        context,
        WorkoutPlanDatabase::class.java,
        "workout_plan.db"
    ).build()

    @Provides
    fun provideWorkoutPlanDao(database: WorkoutPlanDatabase): WorkoutPlanDao =
        database.workoutPlanDao()

    @Provides
    fun provideSessionDao(database: WorkoutPlanDatabase): SessionDao =
        database.sessionDao()
}
