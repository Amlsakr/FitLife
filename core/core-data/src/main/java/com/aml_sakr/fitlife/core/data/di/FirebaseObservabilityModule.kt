package com.aml_sakr.fitlife.core.data.di

import android.content.Context
import com.aml_sakr.fitlife.core.domain.AnalyticsLogger
import com.aml_sakr.fitlife.core.domain.CrashReporter
import com.aml_sakr.fitlife.core.data.observability.FirebaseAnalyticsLogger
import com.aml_sakr.fitlife.core.data.observability.FirebaseCrashReporter
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseObservabilityBindingsModule {
    @Binds
    @Singleton
    abstract fun bindAnalyticsLogger(
        logger: FirebaseAnalyticsLogger
    ): AnalyticsLogger

    @Binds
    @Singleton
    abstract fun bindCrashReporter(
        reporter: FirebaseCrashReporter
    ): CrashReporter
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseObservabilityModule {
    @Provides
    @Singleton
    fun provideFirebaseAnalytics(
        @ApplicationContext context: Context
    ): FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics =
        FirebaseCrashlytics.getInstance()
}
