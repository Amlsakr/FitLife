package com.aml_sakr.fitlife.feature.session.data.di

import android.content.Context
import androidx.room.Room
import com.aml_sakr.fitlife.feature.session.data.database.SessionDatabase
import com.aml_sakr.fitlife.feature.session.data.equipment.*
import com.aml_sakr.fitlife.feature.session.data.repository.SessionRepositoryImpl
import com.aml_sakr.fitlife.feature.session.domain.equipment.IEquipmentReroutingRepository
import com.aml_sakr.fitlife.feature.session.domain.repository.ISessionRepository
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EquipmentReroutingModule {

    @Binds
    @Singleton
    abstract fun bindRepository(
        impl: GeminiEquipmentReroutingRepository
    ): IEquipmentReroutingRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        impl: SessionRepositoryImpl
    ): ISessionRepository

    companion object {
        @Provides
        @Singleton
        fun provideApiService(gson: Gson): EquipmentGeminiApiService {
            return HttpEquipmentGeminiApiService(gson)
        }

        @Provides
        @Singleton
        fun provideSessionDatabase(@ApplicationContext context: Context): SessionDatabase {
            return Room.databaseBuilder(
                context,
                SessionDatabase::class.java,
                "fitlife_session_db"
            ).build()
        }

        @Provides
        fun provideEquipmentReroutingDao(database: SessionDatabase): EquipmentReroutingDao {
            return database.equipmentReroutingDao()
        }

        @Provides
        @Singleton
        fun provideSessionGeminiConfiguration(): SessionGeminiConfiguration {
            return SessionGeminiConfiguration(
                apiKey = com.aml_sakr.fitlife.feature.session.data.BuildConfig.GEMINI_API_KEY
            )
        }
    }
}
