package com.aml_sakr.fitlife.feature.session.domain.pose

import kotlinx.coroutines.flow.Flow

/**
 * Interface for providing ambient light intensity in lux.
 */
interface ILightSensorProvider {
    /**
     * Emits ambient light intensity in lux.
     */
    fun getAmbientLightLux(): Flow<Float>
}
