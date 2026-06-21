package com.aml_sakr.fitlife.feature.session.ui.permission

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CameraPermissionGateRouteTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun gate_waitsForUserAction_beforeRequestingPermission() {
        val permissionRequester = FakeCameraPermissionRequester()
        var cameraSessionReady = false
        var audioOnlySession = false

        composeRule.setContent {
            FitnessAppTheme {
                CameraPermissionGateRoute(
                    onCameraSessionReady = { cameraSessionReady = true },
                    onAudioOnlySession = { audioOnlySession = true },
                    permissionStatus = CameraPermissionStatus(
                        isGranted = false,
                        shouldShowRationale = false
                    ),
                    permissionRequester = permissionRequester
                )
            }
        }

        composeRule.onNodeWithText("Keep your form analysis private").assertIsDisplayed()
        assertEquals(0, permissionRequester.requestCount)

        composeRule.onNodeWithText("Continue to camera").performClick()
        assertEquals(1, permissionRequester.requestCount)
        assertFalse(cameraSessionReady)
        assertFalse(audioOnlySession)

        composeRule.runOnIdle {
            permissionRequester.respond(true)
        }

        composeRule.waitForIdle()
        assertTrue(cameraSessionReady)
        assertFalse(audioOnlySession)
    }

    @Test
    fun gate_routesDeniedPermission_toAudioOnlyFallback() {
        val permissionRequester = FakeCameraPermissionRequester()
        var cameraSessionReady = false
        var audioOnlySession = false

        composeRule.setContent {
            FitnessAppTheme {
                CameraPermissionGateRoute(
                    onCameraSessionReady = { cameraSessionReady = true },
                    onAudioOnlySession = { audioOnlySession = true },
                    permissionStatus = CameraPermissionStatus(
                        isGranted = false,
                        shouldShowRationale = true
                    ),
                    permissionRequester = permissionRequester
                )
            }
        }

        composeRule.onNodeWithText("Continue to camera").performClick()
        assertEquals(1, permissionRequester.requestCount)

        composeRule.runOnIdle {
            permissionRequester.respond(false)
        }

        composeRule.waitForIdle()
        assertFalse(cameraSessionReady)
        assertTrue(audioOnlySession)
    }

    private class FakeCameraPermissionRequester : CameraPermissionRequester {
        var requestCount: Int = 0
            private set

        private var callback: ((Boolean) -> Unit)? = null

        override fun request(onPermissionResult: (Boolean) -> Unit) {
            requestCount += 1
            callback = onPermissionResult
        }

        fun respond(isGranted: Boolean) {
            callback?.invoke(isGranted)
        }
    }
}
