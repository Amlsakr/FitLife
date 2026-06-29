package com.aml_sakr.fitlife.feature.session.ui.utils

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

class PoseCoordinateMapperTest {

    @Test
    fun `map should scale and center correctly when canvas is wider than source aspect ratio`() {
        // Source: 640x480 (1.33 ratio)
        // Canvas: 1000x500 (2.0 ratio)
        // Scale = max(1000/640, 500/480) = max(1.5625, 1.0416) = 1.5625
        // OffsetX = (1000 - 640 * 1.5625) / 2 = 0
        // OffsetY = (500 - 480 * 1.5625) / 2 = (500 - 750) / 2 = -125
        
        val result = PoseCoordinateMapper.map(
            jointX = 320f, // Center of source width
            jointY = 240f, // Center of source height
            sourceWidth = 640,
            sourceHeight = 480,
            canvasWidth = 1000f,
            canvasHeight = 500f
        )
        
        assertEquals(500f, result.x) // 320 * 1.5625 + 0 = 500
        assertEquals(250f, result.y) // 240 * 1.5625 - 125 = 375 - 125 = 250
    }

    @Test
    fun `map should scale and center correctly when canvas is taller than source aspect ratio`() {
        // Source: 640x480 (1.33 ratio)
        // Canvas: 500x1000 (0.5 ratio)
        // Scale = max(500/640, 1000/480) = max(0.78125, 2.0833) = 2.0833
        // OffsetX = (500 - 640 * 2.0833) / 2 = (500 - 1333.3) / 2 = -416.65
        // OffsetY = (1000 - 480 * 2.0833) / 2 = 0
        
        val result = PoseCoordinateMapper.map(
            jointX = 320f,
            jointY = 240f,
            sourceWidth = 640,
            sourceHeight = 480,
            canvasWidth = 500f,
            canvasHeight = 1000f
        )
        
        // 320 * 2.0833 - 416.65 = 666.66 - 416.65 = 250
        assertEquals(250f, result.x, 0.1f)
        // 240 * 2.0833 + 0 = 500
        assertEquals(500f, result.y, 0.1f)
    }

    @Test
    fun `map should mirror X coordinate correctly`() {
        // Source: 640x480, Scale 1.0 (Canvas 640x480)
        // Joint at 100, 100. Mirrored joint at (640 - 100) = 540
        val result = PoseCoordinateMapper.map(
            jointX = 100f,
            jointY = 100f,
            sourceWidth = 640,
            sourceHeight = 480,
            canvasWidth = 640f,
            canvasHeight = 480f,
            isMirrored = true
        )
        assertEquals(540f, result.x)
        assertEquals(100f, result.y)
    }

    @Test
    fun `map should return zero for invalid source dimensions`() {
        val result = PoseCoordinateMapper.map(10f, 10f, 0, 480, 100f, 100f)
        assertEquals(Offset.Zero, result)
    }
}
