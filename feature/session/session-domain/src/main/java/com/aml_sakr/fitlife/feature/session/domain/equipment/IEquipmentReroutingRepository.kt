package com.aml_sakr.fitlife.feature.session.domain.equipment

import com.aml_sakr.fitlife.core.domain.NetworkErrors
import com.aml_sakr.fitlife.core.domain.Result

interface IEquipmentReroutingRepository {
    suspend fun fetchAlternatives(
        exerciseName: String,
        equipment: Set<String>
    ): Result<List<ExerciseAlternative>, NetworkErrors>
}
