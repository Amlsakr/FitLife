package com.aml_sakr.fitlife.feature.auth.data.di

import com.aml_sakr.fitlife.feature.auth.data.repository.FirebaseAuthDataSource
import com.aml_sakr.fitlife.feature.auth.data.repository.FirebaseAuthRemoteDataSource
import com.aml_sakr.fitlife.feature.auth.data.repository.FirebaseAuthRepository
import com.aml_sakr.fitlife.feature.auth.data.startup.FirebaseAuthSessionReader
import com.aml_sakr.fitlife.feature.auth.domain.repository.AuthRepository
import com.aml_sakr.fitlife.feature.auth.domain.startup.AuthSessionReader
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AuthBindingsModule {
    @Binds
    @Singleton
    abstract fun bindFirebaseAuthDataSource(
        dataSource: FirebaseAuthRemoteDataSource
    ): FirebaseAuthDataSource

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        repository: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAuthSessionReader(
        reader: FirebaseAuthSessionReader
    ): AuthSessionReader
}

@Module
@InstallIn(SingletonComponent::class)
object AuthFirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}
