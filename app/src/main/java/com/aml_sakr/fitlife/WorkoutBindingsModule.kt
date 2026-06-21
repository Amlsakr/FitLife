package com.aml_sakr.fitlife

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import com.aml_sakr.fitlife.BuildConfig

@Module
@InstallIn(SingletonComponent::class)
object WorkoutBindingsModule {
    @Provides
    @Singleton
    @Named(WorkoutBindingNames.GEMINI_API_KEY)
    fun provideWorkoutGeminiApiKey(): String = BuildConfig.WORKOUT_GEMINI_API_KEY

    @Provides
    @Singleton
    @Named(WorkoutBindingNames.GEMINI_MODEL_NAME)
    fun provideWorkoutGeminiModelName(): String = BuildConfig.WORKOUT_GEMINI_MODEL_NAME
}

object WorkoutBindingNames {
    const val GEMINI_API_KEY = "workoutGeminiApiKey"
    const val GEMINI_MODEL_NAME = "workoutGeminiModelName"
}
