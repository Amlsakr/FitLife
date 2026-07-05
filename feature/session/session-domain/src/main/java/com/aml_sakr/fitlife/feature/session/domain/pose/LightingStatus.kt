package com.aml_sakr.fitlife.feature.session.domain.pose

/**
 * Represents the lighting status and whether the session should be in audio-only mode.
 */
sealed class LightingStatus {
    object Visual : LightingStatus()
    object AudioOnly : LightingStatus()
}
