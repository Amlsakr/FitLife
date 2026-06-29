package com.aml_sakr.fitlife.feature.session.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseData
import com.aml_sakr.fitlife.feature.session.ui.utils.PoseCoordinateMapper

/**
 * A canvas-based overlay that draws joint markers based on [PoseData].
 * AC 1, 2, 3, 5, 7 compliance:
 * - Uses [Canvas] for efficient real-time drawing.
 * - Draws circles for each joint.
 * - Maps confidence to Cyan, Orange, or Red.
 * - Handles coordinate mapping for FILL_CENTER scaling and optional mirroring.
 */
@Composable
fun SkeletonOverlay(
    poseData: PoseData,
    modifier: Modifier = Modifier,
    isMirrored: Boolean = false
) {
    // Hoist density-dependent calculations out of the draw loop for performance (AC 7)
    val density = LocalDensity.current
    val radiusPx = remember(density) { with(density) { 6.dp.toPx() } }

    // Use theme-aligned colors where possible
    val highConfidenceColor = Color.Cyan
    val medConfidenceColor = Color(0xFFFFA500) // Orange
    val lowConfidenceColor = MaterialTheme.colorScheme.error

    Canvas(modifier = modifier.fillMaxSize()) {
        // Early returns for empty or invalid data
        if (poseData.sourceWidth <= 0 || poseData.sourceHeight <= 0 || poseData.joints.isEmpty()) {
            return@Canvas
        }

        val canvasWidth = size.width
        val canvasHeight = size.height

        poseData.joints.values.forEach { joint ->
            // AC 5: Coordinate Mapping via Utility
            val offset = PoseCoordinateMapper.map(
                jointX = joint.x,
                jointY = joint.y,
                sourceWidth = poseData.sourceWidth,
                sourceHeight = poseData.sourceHeight,
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                isMirrored = isMirrored
            )

            // AC 3: Color Mapping
            val color = when {
                joint.confidence > 0.8f -> highConfidenceColor
                joint.confidence > 0.5f -> medConfidenceColor
                else -> lowConfidenceColor
            }

            // AC 2 & 7: Draw the joint marker
            drawCircle(
                color = color,
                radius = radiusPx,
                center = offset
            )
        }
    }
}
