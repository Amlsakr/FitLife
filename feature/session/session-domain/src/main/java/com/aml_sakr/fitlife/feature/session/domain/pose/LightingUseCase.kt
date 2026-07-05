package com.aml_sakr.fitlife.feature.session.domain.pose

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * Use case for monitoring lighting conditions and pose confidence to trigger audio-only fallback.
 * AC 1, 2, 3 compliance:
 * - Trigger: Sustained low lux (< 10) or confidence (< 0.6) for 2s.
 * - Revert: Stable good conditions (lux > 10 AND confidence > 0.6) for 3s.
 */
class LightingUseCase @Inject constructor(
    private val lightSensorProvider: ILightSensorProvider
) {
    operator fun invoke(poseDataFlow: Flow<PoseData>): Flow<LightingStatus> {
        return combine(
            lightSensorProvider.getAmbientLightLux(),
            poseDataFlow.onStart { emit(PoseData.EMPTY) }
        ) { lux, pose ->
            // AC 2 & 3: Trigger if lux < 10. Revert if lux > 10. 
            // lux == 10 is treated as 'unreliable' to ensure the 'greater than' revert condition.
            lux <= LOW_LUX_THRESHOLD || pose.overallConfidence < LOW_CONFIDENCE_THRESHOLD
        }
            .distinctUntilChanged()
            .transformLatest { isUnreliable ->
                if (isUnreliable) {
                    delay(TRIGGER_DELAY_MS)
                    emit(LightingStatus.AudioOnly)
                } else {
                    delay(REVERT_DELAY_MS)
                    emit(LightingStatus.Visual)
                }
            }
            .onStart { 
                // We could emit Visual here if we want an immediate state, 
                // but transformLatest will wait for the first combined emission.
            }
            .distinctUntilChanged()
    }

    companion object {
        private const val LOW_LUX_THRESHOLD = 10f
        private const val LOW_CONFIDENCE_THRESHOLD = 0.6f
        private const val TRIGGER_DELAY_MS = 2000L
        private const val REVERT_DELAY_MS = 3000L
    }
}
