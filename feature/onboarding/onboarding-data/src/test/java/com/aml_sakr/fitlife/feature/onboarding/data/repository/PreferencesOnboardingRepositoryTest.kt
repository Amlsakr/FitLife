package com.aml_sakr.fitlife.feature.onboarding.data.repository

import com.aml_sakr.fitlife.core.data.preferences.InMemoryPreferencesDataSource
import com.aml_sakr.fitlife.core.data.preferences.PreferencesDataSource
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.Equipment
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessGoal
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOneRepMaxInput
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateTrainingSplit
import com.aml_sakr.fitlife.feature.onboarding.domain.model.OneRepMaxLift
import com.aml_sakr.fitlife.feature.onboarding.domain.model.OneRepMaxUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PreferencesOnboardingRepositoryTest {
    @Test
    fun getSelectedFitnessLevel_returnsNullWhenNoSelectionExists() = runTest {
        val repository = buildRepository(InMemoryPreferencesDataSource())

        assertEquals(Result.Success(null), repository.getSelectedFitnessLevel())
    }

    @Test
    fun saveAndReadSelectedFitnessLevel_roundTripsPersistedValue() = runTest {
        val dataSource = InMemoryPreferencesDataSource()
        val repository = buildRepository(dataSource)

        assertEquals(Result.Success(Unit), repository.saveSelectedFitnessLevel(FitnessLevel.Beginner))
        assertEquals(
            Result.Success(FitnessLevel.Beginner),
            repository.getSelectedFitnessLevel()
        )
    }

    @Test
    fun getSelectedFitnessLevel_returnsFailureForUnknownStoredValue() = runTest {
        val dataSource = InMemoryPreferencesDataSource()
        dataSource.putString("onboarding_selected_fitness_level", "MYSTERY")
        val repository = buildRepository(dataSource)

        assertEquals(
            Result.Failure(OnboardingError.InvalidStoredFitnessLevel),
            repository.getSelectedFitnessLevel()
        )
    }

    @Test
    fun getSelectedFitnessLevel_returnsReadFailureWhenSourceThrows() = runTest {
        val repository = buildRepository(ThrowingPreferencesDataSource())

        assertEquals(
            Result.Failure(OnboardingError.StorageReadFailure),
            repository.getSelectedFitnessLevel()
        )
    }

    @Test
    fun saveSelectedFitnessLevel_returnsWriteFailureWhenSourceThrows() = runTest {
        val repository = buildRepository(WriteThrowingPreferencesDataSource())

        assertEquals(
            Result.Failure(OnboardingError.StorageWriteFailure),
            repository.saveSelectedFitnessLevel(FitnessLevel.Intermediate)
        )
    }

    @Test
    fun onboardingCompletion_roundTripsPerUser() = runTest {
        val dataSource = InMemoryPreferencesDataSource()
        val repository = buildRepository(dataSource)

        assertEquals(Result.Success(false), repository.isOnboardingComplete("user-123"))
        assertEquals(Result.Success(Unit), repository.markOnboardingComplete("user-123"))
        assertEquals(Result.Success(true), repository.isOnboardingComplete("user-123"))
        assertEquals(Result.Success(false), repository.isOnboardingComplete("user-456"))
    }

    @Test
    fun onboardingCompletion_rejectsMissingUserId() = runTest {
        val repository = buildRepository(InMemoryPreferencesDataSource())

        assertEquals(
            Result.Failure(OnboardingError.MissingUserId),
            repository.isOnboardingComplete("")
        )
        assertEquals(
            Result.Failure(OnboardingError.MissingUserId),
            repository.markOnboardingComplete("")
        )
    }

    @Test
    fun onboardingCompletion_returnsReadFailureWhenSourceThrows() = runTest {
        val repository = buildRepository(ThrowingPreferencesDataSource())

        assertEquals(
            Result.Failure(OnboardingError.StorageReadFailure),
            repository.isOnboardingComplete("user-123")
        )
    }

    @Test
    fun onboardingCompletion_returnsWriteFailureWhenSourceThrows() = runTest {
        val repository = buildRepository(WriteThrowingPreferencesDataSource())

        assertEquals(
            Result.Failure(OnboardingError.StorageWriteFailure),
            repository.markOnboardingComplete("user-123")
        )
    }

    @Test
    fun onboardingCompletionRead_rethrowsCancellation() = runTest {
        val repository = buildRepository(CancellationThrowingPreferencesDataSource())

        try {
            repository.isOnboardingComplete("user-123")
            error("Expected cancellation to be rethrown")
        } catch (expected: CancellationException) {
            assertEquals("cancelled", expected.message)
        }
    }

    @Test
    fun onboardingCompletionWrite_rethrowsCancellation() = runTest {
        val repository = buildRepository(CancellationThrowingPreferencesDataSource())

        try {
            repository.markOnboardingComplete("user-123")
            error("Expected cancellation to be rethrown")
        } catch (expected: CancellationException) {
            assertEquals("cancelled", expected.message)
        }
    }

    @Test
    fun saveAndReadBeginnerDraft_roundTripsPersistedValues() = runTest {
        val dataSource = InMemoryPreferencesDataSource()
        val repository = buildRepository(dataSource)
        val draft = BeginnerOnboardingDraft(
            currentStep = BeginnerOnboardingStep.Frequency,
            goals = setOf(FitnessGoal.Strength, FitnessGoal.GeneralHealth),
            equipment = setOf(Equipment.Dumbbells, Equipment.ResistanceBands),
            weeklyFrequency = 4
        )

        assertEquals(Result.Success(Unit), repository.saveBeginnerDraft(draft))
        assertEquals(Result.Success(draft), repository.getBeginnerDraft())
    }

    @Test
    fun saveAndReadIntermediateDraft_roundTripsPersistedValues() = runTest {
        val dataSource = InMemoryPreferencesDataSource()
        val repository = buildRepository(dataSource)
        val draft = IntermediateOnboardingDraft(
            currentStep = IntermediateOnboardingStep.OneRepMax,
            currentSplit = IntermediateTrainingSplit.PushPullLegs,
            goals = setOf(FitnessGoal.Strength, FitnessGoal.GeneralHealth),
            oneRepMaxInputs = mapOf(
                OneRepMaxLift.BenchPress to IntermediateOneRepMaxInput("80"),
                OneRepMaxLift.Squat to IntermediateOneRepMaxInput("120.5", OneRepMaxUnit.Kilograms),
                OneRepMaxLift.Deadlift to IntermediateOneRepMaxInput()
            )
        )

        assertEquals(Result.Success(Unit), repository.saveIntermediateDraft(draft))
        assertEquals(Result.Success(draft), repository.getIntermediateDraft())
    }

    @Test
    fun saveAndReadIntermediateDraft_preservesInvalidOneRepMaxText() = runTest {
        val dataSource = InMemoryPreferencesDataSource()
        val repository = buildRepository(dataSource)
        val draft = IntermediateOnboardingDraft(
            currentStep = IntermediateOnboardingStep.OneRepMax,
            currentSplit = IntermediateTrainingSplit.FullBody,
            goals = setOf(FitnessGoal.Strength),
            oneRepMaxInputs = mapOf(
                OneRepMaxLift.BenchPress to IntermediateOneRepMaxInput("-1", OneRepMaxUnit.Pounds),
                OneRepMaxLift.Squat to IntermediateOneRepMaxInput(),
                OneRepMaxLift.Deadlift to IntermediateOneRepMaxInput()
            )
        )

        assertEquals(Result.Success(Unit), repository.saveIntermediateDraft(draft))
        assertEquals(Result.Success(draft), repository.getIntermediateDraft())
    }

    @Test
    fun saveAndReadIntermediateDraft_preservesDelimiterCharactersInOneRepMaxText() = runTest {
        val dataSource = InMemoryPreferencesDataSource()
        val repository = buildRepository(dataSource)
        val draft = IntermediateOnboardingDraft(
            currentStep = IntermediateOnboardingStep.OneRepMax,
            currentSplit = IntermediateTrainingSplit.FullBody,
            goals = setOf(FitnessGoal.Strength),
            oneRepMaxInputs = mapOf(
                OneRepMaxLift.BenchPress to IntermediateOneRepMaxInput("100;old|school", OneRepMaxUnit.Pounds),
                OneRepMaxLift.Squat to IntermediateOneRepMaxInput(),
                OneRepMaxLift.Deadlift to IntermediateOneRepMaxInput()
            )
        )

        assertEquals(Result.Success(Unit), repository.saveIntermediateDraft(draft))
        assertEquals(Result.Success(draft), repository.getIntermediateDraft())
    }

    @Test
    fun getIntermediateDraft_returnsFailureForUnknownStoredValue() = runTest {
        val dataSource = InMemoryPreferencesDataSource()
        dataSource.putString("onboarding_intermediate_goals", "MYSTERY")
        val repository = buildRepository(dataSource)

        assertEquals(
            Result.Failure(OnboardingError.InvalidStoredIntermediateDraft),
            repository.getIntermediateDraft()
        )
    }

    @Test
    fun syncBeginnerProfile_forwardsToRemoteDataSource() = runTest {
        val remote = RecordingBeginnerRemoteDataSource()
        val repository = buildRepository(InMemoryPreferencesDataSource(), remote = remote)
        val draft = BeginnerOnboardingDraft(weeklyFrequency = 3)

        assertEquals(Result.Success(Unit), repository.syncBeginnerProfile("user-123", draft))
        assertEquals("user-123", remote.lastUserId)
        assertEquals(draft, remote.lastDraft)
    }

    @Test
    fun syncIntermediateProfile_forwardsToRemoteDataSource() = runTest {
        val remote = RecordingIntermediateRemoteDataSource()
        val repository = buildRepository(
            InMemoryPreferencesDataSource(),
            intermediateRemote = remote
        )
        val draft = IntermediateOnboardingDraft(
            currentSplit = IntermediateTrainingSplit.UpperLower,
            goals = setOf(FitnessGoal.WeightLoss)
        )

        assertEquals(Result.Success(Unit), repository.syncIntermediateProfile("user-123", draft))
        assertEquals("user-123", remote.lastUserId)
        assertEquals(draft, remote.lastDraft)
    }

    @Test
    fun syncBeginnerProfile_rejectsMissingUserId() = runTest {
        val repository = buildRepository(InMemoryPreferencesDataSource())

        assertEquals(
            Result.Failure(OnboardingError.MissingUserId),
            repository.syncBeginnerProfile("", BeginnerOnboardingDraft())
        )
    }

    @Test
    fun syncIntermediateProfile_rejectsMissingUserId() = runTest {
        val repository = buildRepository(InMemoryPreferencesDataSource())

        assertEquals(
            Result.Failure(OnboardingError.MissingUserId),
            repository.syncIntermediateProfile("", IntermediateOnboardingDraft())
        )
    }

    private fun buildRepository(
        dataSource: PreferencesDataSource,
        remote: BeginnerOnboardingRemoteDataSource = NoOpBeginnerRemoteDataSource,
        intermediateRemote: IntermediateOnboardingRemoteDataSource = NoOpIntermediateRemoteDataSource
    ): PreferencesOnboardingRepository = PreferencesOnboardingRepository(
        dataSource,
        remote,
        intermediateRemote
    )

    private class ThrowingPreferencesDataSource : PreferencesDataSource {
        override fun booleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> = flow {
            throw IllegalStateException("boom")
        }

        override suspend fun putBoolean(key: String, value: Boolean) = Unit

        override fun stringFlow(key: String, defaultValue: String): Flow<String> = flow {
            throw IllegalStateException("boom")
        }

        override suspend fun putString(key: String, value: String) = Unit

        override fun longFlow(key: String, defaultValue: Long): Flow<Long> = flow {
            throw IllegalStateException("boom")
        }

        override suspend fun putLong(key: String, value: Long) = Unit
    }

    private class WriteThrowingPreferencesDataSource : PreferencesDataSource {
        override fun booleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> =
            flowOf(defaultValue)

        override suspend fun putBoolean(key: String, value: Boolean) {
            throw IllegalStateException("boom")
        }

        override fun stringFlow(key: String, defaultValue: String): Flow<String> =
            flowOf(defaultValue)

        override suspend fun putString(key: String, value: String) {
            throw IllegalStateException("boom")
        }

        override fun longFlow(key: String, defaultValue: Long): Flow<Long> =
            flowOf(defaultValue)

        override suspend fun putLong(key: String, value: Long) {
            throw IllegalStateException("boom")
        }
    }

    private class CancellationThrowingPreferencesDataSource : PreferencesDataSource {
        override fun booleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> = flow {
            throw CancellationException("cancelled")
        }

        override suspend fun putBoolean(key: String, value: Boolean) {
            throw CancellationException("cancelled")
        }

        override fun stringFlow(key: String, defaultValue: String): Flow<String> =
            flowOf(defaultValue)

        override suspend fun putString(key: String, value: String) = Unit

        override fun longFlow(key: String, defaultValue: Long): Flow<Long> =
            flowOf(defaultValue)

        override suspend fun putLong(key: String, value: Long) = Unit
    }

    private object NoOpBeginnerRemoteDataSource : BeginnerOnboardingRemoteDataSource {
        override suspend fun upsertBeginnerProfile(
            userId: String,
            draft: BeginnerOnboardingDraft
        ) = Unit
    }

    private object NoOpIntermediateRemoteDataSource : IntermediateOnboardingRemoteDataSource {
        override suspend fun upsertIntermediateProfile(
            userId: String,
            draft: IntermediateOnboardingDraft
        ) = Unit
    }

    private class RecordingBeginnerRemoteDataSource : BeginnerOnboardingRemoteDataSource {
        var lastUserId: String? = null
        var lastDraft: BeginnerOnboardingDraft? = null

        override suspend fun upsertBeginnerProfile(
            userId: String,
            draft: BeginnerOnboardingDraft
        ) {
            lastUserId = userId
            lastDraft = draft
        }
    }

    private class RecordingIntermediateRemoteDataSource : IntermediateOnboardingRemoteDataSource {
        var lastUserId: String? = null
        var lastDraft: IntermediateOnboardingDraft? = null

        override suspend fun upsertIntermediateProfile(
            userId: String,
            draft: IntermediateOnboardingDraft
        ) {
            lastUserId = userId
            lastDraft = draft
        }
    }
}
