package com.aml_sakr.fitlife.feature.workout.data.fallback

import java.io.InputStream

interface WorkoutPlanAssetReader {
    fun open(path: String): InputStream
}
