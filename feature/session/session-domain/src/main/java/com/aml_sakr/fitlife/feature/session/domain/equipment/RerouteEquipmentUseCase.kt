package com.aml_sakr.fitlife.feature.session.domain.equipment

import com.aml_sakr.fitlife.core.domain.NetworkErrors
import com.aml_sakr.fitlife.core.domain.Result
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class RerouteEquipmentUseCase @Inject constructor(
    private val repository: IEquipmentReroutingRepository
) {
    suspend operator fun invoke(
        exerciseName: String,
        availableEquipment: Set<String>
    ): Result<List<ExerciseAlternative>, NetworkErrors> {
        return try {
            withTimeout(5000) {
                repository.fetchAlternatives(exerciseName, availableEquipment)
            }
        } catch (e: TimeoutCancellationException) {
            Result.Failure(NetworkErrors.Timeout)
        } catch (e: Exception) {
            Result.Failure(NetworkErrors.UnknownApiError)
        }
    }
}
