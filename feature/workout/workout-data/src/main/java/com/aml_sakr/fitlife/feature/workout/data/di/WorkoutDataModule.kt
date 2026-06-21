package com.aml_sakr.fitlife.feature.workout.data.di

import android.util.Log
import com.aml_sakr.fitlife.core.data.workout.WorkoutPlanDao
import com.aml_sakr.fitlife.feature.workout.data.fallback.AndroidWorkoutPlanAssetReader
import com.aml_sakr.fitlife.feature.workout.data.fallback.WorkoutPlanAssetReader
import com.aml_sakr.fitlife.feature.workout.data.fallback.WorkoutPlanFallbackLogger
import com.aml_sakr.fitlife.feature.workout.data.fallback.WorkoutPlanFallbackSelector
import com.aml_sakr.fitlife.feature.workout.data.fallback.WorkoutPlanFallbackSource
import com.aml_sakr.fitlife.feature.workout.data.gemini.GeminiApiService
import com.aml_sakr.fitlife.feature.workout.data.gemini.GeminiPlanResponseParser
import com.aml_sakr.fitlife.feature.workout.data.gemini.GeminiWorkoutPromptBuilder
import com.aml_sakr.fitlife.feature.workout.data.gemini.HttpGeminiApiService
import com.aml_sakr.fitlife.feature.workout.data.gemini.WorkoutGeminiGatewayConfiguration
import com.aml_sakr.fitlife.feature.workout.data.gemini.WorkoutPlanFailureClassifier
import com.aml_sakr.fitlife.feature.workout.data.gemini.WorkoutPlanMapper
import com.aml_sakr.fitlife.feature.workout.data.repository.WorkoutPlanRepositoryImpl
import com.aml_sakr.fitlife.feature.workout.data.repository.WorkoutPlanRoomMapper
import com.aml_sakr.fitlife.feature.workout.domain.repository.WorkoutPlanRepository
import com.aml_sakr.fitlife.feature.workout.domain.usecase.SystemWorkoutPlanClock
import com.aml_sakr.fitlife.feature.workout.domain.usecase.WorkoutPlanClock
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import android.content.Context

@Module
@InstallIn(SingletonComponent::class)
object WorkoutDataModule {
    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideWorkoutPlanClock(): WorkoutPlanClock = SystemWorkoutPlanClock

    @Provides
    @Singleton
    fun provideWorkoutGeminiGatewayConfiguration(
        @Named("workoutGeminiApiKey") apiKey: String,
        @Named("workoutGeminiModelName") modelName: String
    ): WorkoutGeminiGatewayConfiguration = WorkoutGeminiGatewayConfiguration(
        apiKey = apiKey,
        modelName = modelName
    )

    @Provides
    @Singleton
    fun provideWorkoutPlanAssetReader(
        @ApplicationContext context: Context
    ): WorkoutPlanAssetReader = AndroidWorkoutPlanAssetReader(context.assets)

    @Provides
    @Singleton
    fun provideWorkoutPlanFallbackLogger(): WorkoutPlanFallbackLogger =
        WorkoutPlanFallbackLogger { _, templateId ->
            Log.i(
                "FitLifeWorkout",
                "Using fallback workout template $templateId"
            )
        }

    @Provides
    @Singleton
    fun provideGeminiApiService(): GeminiApiService = HttpGeminiApiService()

    @Provides
    @Singleton
    fun provideWorkoutPlanFallbackSource(
        assetReader: WorkoutPlanAssetReader,
        logger: WorkoutPlanFallbackLogger,
        gson: Gson
    ): WorkoutPlanFallbackSource = WorkoutPlanFallbackSource(
        assetReader = assetReader,
        selector = WorkoutPlanFallbackSelector(logger = logger),
        mapper = WorkoutPlanMapper(),
        gson = gson
    )

    @Provides
    @Singleton
    fun provideWorkoutPlanRepository(
        workoutPlanDao: WorkoutPlanDao,
        geminiConfiguration: WorkoutGeminiGatewayConfiguration,
        apiService: GeminiApiService,
        promptBuilder: GeminiWorkoutPromptBuilder,
        responseParser: GeminiPlanResponseParser,
        fallbackSource: WorkoutPlanFallbackSource,
        mapper: WorkoutPlanMapper,
        failureClassifier: WorkoutPlanFailureClassifier,
        gson: Gson,
        roomMapper: WorkoutPlanRoomMapper,
        clock: WorkoutPlanClock
    ): WorkoutPlanRepository = WorkoutPlanRepositoryImpl(
        workoutPlanDao = workoutPlanDao,
        geminiConfiguration = geminiConfiguration,
        apiService = apiService,
        promptBuilder = promptBuilder,
        responseParser = responseParser,
        fallbackSource = fallbackSource,
        mapper = mapper,
        failureClassifier = failureClassifier,
        gson = gson,
        roomMapper = roomMapper,
        clock = clock
    )

    @Provides
    @Singleton
    fun provideWorkoutPlanRoomMapper(gson: Gson): WorkoutPlanRoomMapper = WorkoutPlanRoomMapper(gson)

    @Provides
    @Singleton
    fun provideWorkoutPlanMapper(): WorkoutPlanMapper = WorkoutPlanMapper()

    @Provides
    @Singleton
    fun provideWorkoutPlanFailureClassifier(): WorkoutPlanFailureClassifier =
        WorkoutPlanFailureClassifier()

    @Provides
    @Singleton
    fun provideWorkoutPlanResponseParser(): GeminiPlanResponseParser =
        GeminiPlanResponseParser()

    @Provides
    @Singleton
    fun provideWorkoutPromptBuilder(): GeminiWorkoutPromptBuilder =
        GeminiWorkoutPromptBuilder()
}
