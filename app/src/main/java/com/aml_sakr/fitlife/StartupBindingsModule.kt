package com.aml_sakr.fitlife

import android.util.Log
import com.aml_sakr.fitlife.feature.auth.auth_ui.splash.StartupDestinationResolver
import com.aml_sakr.fitlife.feature.auth.auth_ui.splash.StartupRouteErrorLogger
import com.aml_sakr.fitlife.feature.auth.domain.startup.AuthSessionReader
import com.aml_sakr.fitlife.feature.auth.domain.startup.DetermineStartupDestinationUseCase
import com.aml_sakr.fitlife.feature.auth.domain.startup.OnboardingCompletionReader
import com.aml_sakr.fitlife.feature.onboarding.domain.usecase.ReadOnboardingCompletionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StartupBindingsModule {
    @Provides
    @Singleton
    fun provideOnboardingCompletionReader(
        readOnboardingCompletionUseCase: ReadOnboardingCompletionUseCase
    ): OnboardingCompletionReader = object : OnboardingCompletionReader {
        override suspend fun isOnboardingComplete(userId: String): Boolean {
            return when (val result = readOnboardingCompletionUseCase(userId)) {
                is com.aml_sakr.fitlife.core.domain.Result.Success -> result.value
                is com.aml_sakr.fitlife.core.domain.Result.Failure -> false
            }
        }
    }

    @Provides
    @Singleton
    fun provideDetermineStartupDestinationUseCase(
        authSessionReader: AuthSessionReader,
        onboardingCompletionReader: OnboardingCompletionReader
    ): DetermineStartupDestinationUseCase = DetermineStartupDestinationUseCase(
        authSessionReader = authSessionReader,
        onboardingCompletionReader = onboardingCompletionReader
    )

    @Provides
    @Singleton
    fun provideStartupDestinationResolver(
        determineStartupDestinationUseCase: DetermineStartupDestinationUseCase
    ): StartupDestinationResolver = StartupDestinationResolver {
        determineStartupDestinationUseCase()
    }

    @Provides
    @Singleton
    fun provideStartupRouteErrorLogger(): StartupRouteErrorLogger =
        StartupRouteErrorLogger { throwable ->
            Log.e("FitLifeStartup", "Unable to determine startup route", throwable)
        }
}
