package com.aml_sakr.fitlife.feature.session.domain.equipment

import com.aml_sakr.fitlife.feature.session.domain.model.ExerciseDifficulty

data class ExerciseAlternative(
    val exerciseId: String,
    val name: String,
    val description: String,
    val muscleGroups: List<String>,
    val equipmentRequired: String,
    val difficulty: ExerciseDifficulty,
    val lottieAssetPath: String?,
    val defaultSets: Int,
    val defaultReps: Int
)
