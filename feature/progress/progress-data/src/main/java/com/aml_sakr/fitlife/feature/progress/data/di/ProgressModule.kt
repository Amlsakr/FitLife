package com.aml_sakr.fitlife.feature.progress.data.di

import com.aml_sakr.fitlife.feature.progress.data.repository.ProgressRepositoryImpl
import com.aml_sakr.fitlife.feature.progress.domain.repository.IProgressRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ProgressModule {

    @Binds
    @Singleton
    fun bindProgressRepository(
        impl: ProgressRepositoryImpl
    ): IProgressRepository
}
