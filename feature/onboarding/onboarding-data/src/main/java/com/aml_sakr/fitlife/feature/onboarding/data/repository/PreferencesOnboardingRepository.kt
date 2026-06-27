package com.aml_sakr.fitlife.feature.onboarding.data.repository

import com.aml_sakr.fitlife.core.data.preferences.PreferencesDataSource
import com.aml_sakr.fitlife.core.domain.Result
import com.aml_sakr.fitlife.feature.onboarding.domain.error.OnboardingError
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.Equipment
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingDraft
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOneRepMaxInput
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateTrainingSplit
import com.aml_sakr.fitlife.feature.onboarding.domain.model.OneRepMaxLift
import com.aml_sakr.fitlife.feature.onboarding.domain.model.OneRepMaxUnit
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessLevel
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessGoal
import com.aml_sakr.fitlife.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PreferencesOnboardingRepository @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource,
    private val beginnerOnboardingRemoteDataSource: BeginnerOnboardingRemoteDataSource,
    private val intermediateOnboardingRemoteDataSource: IntermediateOnboardingRemoteDataSource
) : OnboardingRepository {
    override suspend fun getSelectedFitnessLevel(): Result<FitnessLevel?, OnboardingError> {
        return try {
            val storedValue = preferencesDataSource
                .stringFlow(SELECTED_LEVEL_KEY, "")
                .first()

            if (storedValue.isBlank()) {
                Result.Success(null)
            } else {
                storedValue.toFitnessLevelOrNull()?.let { Result.Success(it) }
                    ?: Result.Failure(OnboardingError.InvalidStoredFitnessLevel)
            }
        } catch (_: Throwable) {
            Result.Failure(OnboardingError.StorageReadFailure)
        }
    }

    override suspend fun saveSelectedFitnessLevel(level: FitnessLevel): Result<Unit, OnboardingError> {
        return try {
            preferencesDataSource.putString(SELECTED_LEVEL_KEY, level.name)
            Result.Success(Unit)
        } catch (_: Throwable) {
            Result.Failure(OnboardingError.StorageWriteFailure)
        }
    }

    override suspend fun getBeginnerDraft(): Result<BeginnerOnboardingDraft, OnboardingError> {
        return try {
            val currentStep = preferencesDataSource
                .stringFlow(BEGINNER_STEP_KEY, BeginnerOnboardingStep.Goals.name)
                .first()
                .toBeginnerStepOrNull()
                ?: return Result.Failure(OnboardingError.InvalidStoredBeginnerDraft)

            val goals = preferencesDataSource
                .stringFlow(BEGINNER_GOALS_KEY, "")
                .first()
                .toFitnessGoalSetOrNull()
                ?: return Result.Failure(OnboardingError.InvalidStoredBeginnerDraft)

            val equipment = preferencesDataSource
                .stringFlow(BEGINNER_EQUIPMENT_KEY, "")
                .first()
                .toEquipmentSetOrNull()
                ?: return Result.Failure(OnboardingError.InvalidStoredBeginnerDraft)

            val weeklyFrequency = preferencesDataSource
                .longFlow(BEGINNER_FREQUENCY_KEY, UNSET_FREQUENCY)
                .first()
                .takeIf { it != UNSET_FREQUENCY }
                ?.toInt()

            Result.Success(
                BeginnerOnboardingDraft(
                    currentStep = currentStep,
                    goals = goals,
                    equipment = equipment,
                    weeklyFrequency = weeklyFrequency
                )
            )
        } catch (_: Throwable) {
            Result.Failure(OnboardingError.StorageReadFailure)
        }
    }

    override suspend fun saveBeginnerDraft(draft: BeginnerOnboardingDraft): Result<Unit, OnboardingError> {
        return try {
            preferencesDataSource.putString(BEGINNER_STEP_KEY, draft.currentStep.name)
            preferencesDataSource.putString(BEGINNER_GOALS_KEY, draft.goals.encodeEnumValues())
            preferencesDataSource.putString(BEGINNER_EQUIPMENT_KEY, draft.equipment.encodeEnumValues())
            preferencesDataSource.putLong(
                BEGINNER_FREQUENCY_KEY,
                draft.weeklyFrequency?.toLong() ?: UNSET_FREQUENCY
            )
            Result.Success(Unit)
        } catch (_: Throwable) {
            Result.Failure(OnboardingError.StorageWriteFailure)
        }
    }

    override suspend fun syncBeginnerProfile(
        userId: String,
        draft: BeginnerOnboardingDraft
    ): Result<Unit, OnboardingError> {
        if (userId.isBlank()) {
            return Result.Failure(OnboardingError.MissingUserId)
        }

        return try {
            beginnerOnboardingRemoteDataSource.upsertBeginnerProfile(userId, draft)
            Result.Success(Unit)
        } catch (e: Exception) {

            Result.Failure(OnboardingError.RemoteSyncFailure)
        }
    }

    override suspend fun isOnboardingComplete(
        userId: String
    ): Result<Boolean, OnboardingError> {
        if (userId.isBlank()) {
            return Result.Failure(OnboardingError.MissingUserId)
        }

        return try {
            val completion = preferencesDataSource
                .booleanFlow(onboardingCompletionKey(userId), false)
                .first()
            Result.Success(completion)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            Result.Failure(OnboardingError.StorageReadFailure)
        }
    }

    override suspend fun markOnboardingComplete(
        userId: String
    ): Result<Unit, OnboardingError> {
        if (userId.isBlank()) {
            return Result.Failure(OnboardingError.MissingUserId)
        }

        return try {
            preferencesDataSource.putBoolean(onboardingCompletionKey(userId), true)
            Result.Success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            Result.Failure(OnboardingError.StorageWriteFailure)
        }
    }

    override suspend fun getIntermediateDraft(): Result<IntermediateOnboardingDraft, OnboardingError> {
        return try {
            val currentStep = preferencesDataSource
                .stringFlow(INTERMEDIATE_STEP_KEY, IntermediateOnboardingStep.Split.name)
                .first()
                .toIntermediateStepOrNull()
                ?: return Result.Failure(OnboardingError.InvalidStoredIntermediateDraft)

            val currentSplit = preferencesDataSource
                .stringFlow(INTERMEDIATE_SPLIT_KEY, "")
                .first()
                .let { rawSplit ->
                    if (rawSplit.isBlank()) {
                        null
                    } else {
                        rawSplit.toIntermediateSplitOrNull()
                            ?: return Result.Failure(OnboardingError.InvalidStoredIntermediateDraft)
                    }
                }

            val goals = preferencesDataSource
                .stringFlow(INTERMEDIATE_GOALS_KEY, "")
                .first()
                .toFitnessGoalSetOrNull()
                ?: return Result.Failure(OnboardingError.InvalidStoredIntermediateDraft)

            val oneRepMax = preferencesDataSource
                .stringFlow(INTERMEDIATE_ONE_REP_MAX_STATE_KEY, "")
                .first()
                .toOneRepMaxInputsOrNull()
                ?: return Result.Failure(OnboardingError.InvalidStoredIntermediateDraft)

            Result.Success(
                IntermediateOnboardingDraft(
                    currentStep = currentStep,
                    currentSplit = currentSplit,
                    goals = goals,
                    oneRepMaxInputs = oneRepMax
                )
            )
        } catch (_: Throwable) {
            Result.Failure(OnboardingError.StorageReadFailure)
        }
    }

    override suspend fun saveIntermediateDraft(
        draft: IntermediateOnboardingDraft
    ): Result<Unit, OnboardingError> {
        return try {
            preferencesDataSource.putString(INTERMEDIATE_STEP_KEY, draft.currentStep.name)
            preferencesDataSource.putString(
                INTERMEDIATE_SPLIT_KEY,
                draft.currentSplit?.name.orEmpty()
            )
            preferencesDataSource.putString(
                INTERMEDIATE_GOALS_KEY,
                draft.goals.encodeEnumValues()
            )
            preferencesDataSource.putString(
                INTERMEDIATE_ONE_REP_MAX_STATE_KEY,
                draft.oneRepMaxInputs.encodeOneRepMaxInputs()
            )
            Result.Success(Unit)
        } catch (_: Throwable) {
            Result.Failure(OnboardingError.StorageWriteFailure)
        }
    }

    override suspend fun syncIntermediateProfile(
        userId: String,
        draft: IntermediateOnboardingDraft
    ): Result<Unit, OnboardingError> {
        if (userId.isBlank()) {
            return Result.Failure(OnboardingError.MissingUserId)
        }

        return try {
            intermediateOnboardingRemoteDataSource.upsertIntermediateProfile(userId, draft)
            Result.Success(Unit)
        } catch (_: Throwable) {
            Result.Failure(OnboardingError.RemoteSyncFailure)
        }
    }

    private fun String.toFitnessLevelOrNull(): FitnessLevel? =
        runCatching { FitnessLevel.valueOf(this) }.getOrNull()

    private fun String.toBeginnerStepOrNull(): BeginnerOnboardingStep? =
        runCatching { BeginnerOnboardingStep.valueOf(this) }.getOrNull()

    private fun String.toFitnessGoalSetOrNull(): Set<FitnessGoal>? {
        if (isBlank()) return emptySet()
        return split(ENUM_SEPARATOR)
            .filter { it.isNotBlank() }
            .map { runCatching { FitnessGoal.valueOf(it) }.getOrNull() ?: return null }
            .toSet()
    }

    private fun String.toEquipmentSetOrNull(): Set<Equipment>? {
        if (isBlank()) return emptySet()
        return split(ENUM_SEPARATOR)
            .filter { it.isNotBlank() }
            .map { runCatching { Equipment.valueOf(it) }.getOrNull() ?: return null }
            .toSet()
    }

    private fun String.toIntermediateStepOrNull(): IntermediateOnboardingStep? =
        runCatching { IntermediateOnboardingStep.valueOf(this) }.getOrNull()

    private fun String.toIntermediateSplitOrNull(): IntermediateTrainingSplit? {
        if (isBlank()) return null
        return runCatching { IntermediateTrainingSplit.valueOf(this) }.getOrNull()
    }

    private fun String.toOneRepMaxInputsOrNull(): Map<OneRepMaxLift, IntermediateOneRepMaxInput>? {
        if (isBlank()) return defaultOneRepMaxInputs()

        val trimmed = trimStart()
        return if (trimmed.startsWith(RAW_ONE_REP_MAX_PREFIX)) {
            trimmed.removePrefix(RAW_ONE_REP_MAX_PREFIX).parseOneRepMaxInputsOrNull()
        } else {
            toLegacyOneRepMaxMapOrNull()?.toOneRepMaxInputsOrNull()
        }
    }

    private fun String.parseOneRepMaxInputsOrNull():
        Map<OneRepMaxLift, IntermediateOneRepMaxInput>? {
        if (isBlank()) return null

        val parsed = defaultOneRepMaxInputs().toMutableMap()

        return try {
            splitEntries()
                .filter { it.isNotBlank() }
                .forEach { entry ->
                    val parts = entry.splitEscaped(FIELD_SEPARATOR, limit = 3)
                    if (parts.size != 3) return@forEach

                    val lift = runCatching { OneRepMaxLift.valueOf(parts[0].decodeEscapedValue()) }.getOrNull()
                        ?: return@forEach
                    val unit = runCatching { OneRepMaxUnit.valueOf(parts[2].decodeEscapedValue()) }.getOrNull()
                        ?: return@forEach
                    parsed[lift] = IntermediateOneRepMaxInput(
                        valueText = parts[1].decodeEscapedValue(),
                        unit = unit
                    )
                }
            parsed
        } catch (_: Throwable) {
            null
        }
    }

    private fun String.toLegacyOneRepMaxMapOrNull(): Map<OneRepMaxLift, Float>? {
        if (isBlank()) return emptyMap()

        return try {
            split(ENUM_SEPARATOR)
                .filter { it.isNotBlank() }
                .associate { entry ->
                    val parts = entry.split(KEY_VALUE_SEPARATOR)
                    if (parts.size != 2) return null

                    val lift = runCatching { OneRepMaxLift.valueOf(parts[0]) }.getOrNull()
                        ?: return null
                    val value = parts[1].toFloatOrNull() ?: return null
                    lift to value
                }
        } catch (_: Throwable) {
            null
        }
    }

    private fun Map<OneRepMaxLift, Float>.toOneRepMaxInputsOrNull():
        Map<OneRepMaxLift, IntermediateOneRepMaxInput> = defaultOneRepMaxInputs().mapValues { (lift, input) ->
        val savedValue = get(lift) ?: return@mapValues input
        input.copy(
            valueText = savedValue.formatDecimal(),
            unit = OneRepMaxUnit.Kilograms
        )
    }

    private fun <T : Enum<T>> Set<T>.encodeEnumValues(): String =
        joinToString(separator = ENUM_SEPARATOR) { it.name }

    private fun Map<OneRepMaxLift, IntermediateOneRepMaxInput>.encodeOneRepMaxInputs(): String =
        RAW_ONE_REP_MAX_PREFIX + entries.sortedBy { it.key.name }.joinToString(
            separator = ENTRY_SEPARATOR.toString()
        ) { (lift, value) ->
            listOf(
                lift.name,
                value.valueText.escapeDraftValue(),
                value.unit.name
            ).joinToString(separator = FIELD_SEPARATOR.toString())
        }

    private fun Float.formatDecimal(): String =
        if (this % 1f == 0f) {
            toInt().toString()
        } else {
            toString()
        }

    private fun String.escapeDraftValue(): String = buildString(length) {
        for (char in this@escapeDraftValue) {
            when (char) {
                ESCAPE_CHAR, ENTRY_SEPARATOR, FIELD_SEPARATOR -> {
                    append(ESCAPE_CHAR)
                    append(char)
                }
                else -> append(char)
            }
        }
    }

    private fun String.decodeEscapedValue(): String {
        val builder = StringBuilder(length)
        var escaping = false

        for (char in this) {
            if (escaping) {
                builder.append(char)
                escaping = false
            } else if (char == ESCAPE_CHAR) {
                escaping = true
            } else {
                builder.append(char)
            }
        }

        if (escaping) {
            throw IllegalArgumentException("Dangling escape sequence")
        }

        return builder.toString()
    }

    private fun String.splitEntries(): List<String> = splitEscaped(ENTRY_SEPARATOR)

    private fun String.splitEscaped(delimiter: Char, limit: Int = Int.MAX_VALUE): List<String> {
        val values = mutableListOf<String>()
        val current = StringBuilder(length)
        var escaping = false

        for (char in this) {
            current.append(char)

            when {
                escaping -> escaping = false
                char == ESCAPE_CHAR -> escaping = true
                char == delimiter && values.size < limit - 1 -> {
                    current.deleteCharAt(current.length - 1)
                    values += current.toString()
                    current.setLength(0)
                }
            }
        }

        if (escaping) {
            throw IllegalArgumentException("Dangling escape sequence")
        }

        values += current.toString()
        return values
    }

    private fun defaultOneRepMaxInputs(): Map<OneRepMaxLift, IntermediateOneRepMaxInput> =
        OneRepMaxLift.entries.associateWith { IntermediateOneRepMaxInput() }

    private companion object {
        const val SELECTED_LEVEL_KEY = "onboarding_selected_fitness_level"
        const val BEGINNER_STEP_KEY = "onboarding_beginner_step"
        const val BEGINNER_GOALS_KEY = "onboarding_beginner_goals"
        const val BEGINNER_EQUIPMENT_KEY = "onboarding_beginner_equipment"
        const val BEGINNER_FREQUENCY_KEY = "onboarding_beginner_weekly_frequency"
        const val ONBOARDING_COMPLETION_KEY_PREFIX = "onboarding_complete_"
        const val INTERMEDIATE_STEP_KEY = "onboarding_intermediate_step"
        const val INTERMEDIATE_SPLIT_KEY = "onboarding_intermediate_split"
        const val INTERMEDIATE_GOALS_KEY = "onboarding_intermediate_goals"
        const val INTERMEDIATE_ONE_REP_MAX_STATE_KEY = "onboarding_intermediate_one_rep_max_state"
        const val RAW_ONE_REP_MAX_PREFIX = "v3;"
        const val ENUM_SEPARATOR = "|"
        const val KEY_VALUE_SEPARATOR = "="
        const val ENTRY_SEPARATOR = ';'
        const val FIELD_SEPARATOR = '|'
        const val ESCAPE_CHAR = '\\'
        const val UNSET_FREQUENCY = -1L
    }

    private fun onboardingCompletionKey(userId: String): String =
        ONBOARDING_COMPLETION_KEY_PREFIX + userId
}
