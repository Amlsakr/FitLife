package com.aml_sakr.fitlife.feature.workout.data.fallback

import android.content.res.AssetManager
import java.io.InputStream

class AndroidWorkoutPlanAssetReader(
    private val assetManager: AssetManager
) : WorkoutPlanAssetReader {
    override fun open(path: String): InputStream = assetManager.open(path)
}
