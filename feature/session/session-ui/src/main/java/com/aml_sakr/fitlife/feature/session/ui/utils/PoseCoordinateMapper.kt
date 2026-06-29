package com.aml_sakr.fitlife.feature.session.ui.utils

import androidx.compose.ui.geometry.Offset
import kotlin.math.max

/**
 * Utility for mapping ML Kit pose coordinates to Compose Canvas coordinates.
 * AC 5 compliance: Handles FILL_CENTER scaling and offset calculations.
 */
object PoseCoordinateMapper {
    /**
     * Maps a coordinate from source image space to canvas space using FILL_CENTER logic.
     * @param isMirrored Whether to flip the X coordinate (typical for front-facing camera).
     */
    fun map(
        jointX: Float,
        jointY: Float,
        sourceWidth: Int,
        sourceHeight: Int,
        canvasWidth: Float,
        canvasHeight: Float,
        isMirrored: Boolean = false
    ): Offset {
        // Guard against division by zero and invalid dimensions
        if (sourceWidth < 1 || sourceHeight < 1 || canvasWidth <= 0 || canvasHeight <= 0) {
            return Offset.Zero
        }

        // Calculate the scale factor (FILL_CENTER uses max to ensure entire canvas is covered)
        val scale = max(
            canvasWidth / sourceWidth.toFloat(),
            canvasHeight / sourceHeight.toFloat()
        )

        // Calculate centering offsets
        val offsetX = (canvasWidth - sourceWidth * scale) / 2f
        val offsetY = (canvasHeight - sourceHeight * scale) / 2f

        // Apply mirroring if required (flip relative to source width before scaling)
        val finalX = if (isMirrored) sourceWidth - jointX else jointX

        return Offset(
            x = finalX * scale + offsetX,
            y = jointY * scale + offsetY
        )
    }
}
