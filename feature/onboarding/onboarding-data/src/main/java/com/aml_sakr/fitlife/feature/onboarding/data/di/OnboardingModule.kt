package com.aml_sakr.fitlife.feature.onboarding.data.di

import com.aml_sakr.fitlife.feature.onboarding.data.repository.BeginnerOnboardingRemoteDataSource
import com.aml_sakr.fitlife.feature.onboarding.data.repository.FirebaseBeginnerOnboardingRemoteDataSource
import com.aml_sakr.fitlife.feature.onboarding.data.repository.FirebaseIntermediateOnboardingRemoteDataSource
import com.aml_sakr.fitlife.feature.onboarding.data.repository.IntermediateOnboardingRemoteDataSource
import com.aml_sakr.fitlife.feature.onboarding.data.repository.PreferencesOnboardingRepository
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OnboardingModule {
    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(
        repository: PreferencesOnboardingRepository
    ): OnboardingRepository

    @Binds
    @Singleton
    abstract fun bindBeginnerOnboardingRemoteDataSource(
        dataSource: FirebaseBeginnerOnboardingRemoteDataSource
    ): BeginnerOnboardingRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindIntermediateOnboardingRemoteDataSource(
        dataSource: FirebaseIntermediateOnboardingRemoteDataSource
    ): IntermediateOnboardingRemoteDataSource
}
