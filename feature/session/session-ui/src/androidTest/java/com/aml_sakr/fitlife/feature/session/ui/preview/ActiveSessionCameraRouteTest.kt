package com.aml_sakr.fitlife.feature.session.ui.preview

import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.LifecycleOwner
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import org.junit.Rule
import org.junit.Test

class ActiveSessionCameraRouteTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun session_showsLoading_initially() {
        val fakeProvider = FakeCameraPreviewProvider()
        
        composeRule.setContent {
            FitnessAppTheme {
                ActiveSessionCameraRoute(
                    onExitSession = {},
                    onSwitchToAudioOnly = {},
                    cameraPreviewProvider = fakeProvider
                )
            }
        }

        composeRule.runOnIdle {
            fakeProvider.respond(Result.success(Unit))
        }
        
        composeRule.onNodeWithText("Live camera preview").assertIsDisplayed()
    }

    @Test
    fun session_showsError_whenBindingFails() {
        val fakeProvider = FakeCameraPreviewProvider()
        
        composeRule.setContent {
            FitnessAppTheme {
                ActiveSessionCameraRoute(
                    onExitSession = {},
                    onSwitchToAudioOnly = {},
                    cameraPreviewProvider = fakeProvider
                )
            }
        }

        composeRule.runOnIdle {
            fakeProvider.respond(Result.failure(Exception("Camera hardware unavailable")))
        }
        
        composeRule.onNodeWithText("Camera Error").assertIsDisplayed()
        composeRule.onNodeWithText("Retry Camera").assertIsDisplayed()
    }

    @Test
    fun session_handlesExitAction() {
        var exited = false
        composeRule.setContent {
            FitnessAppTheme {
                ActiveSessionCameraRoute(
                    onExitSession = { exited = true },
                    onSwitchToAudioOnly = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("Exit session").performClick()
        assert(exited)
    }

    private class FakeCameraPreviewProvider : CameraPreviewProvider {
        private var callback: ((Result<Unit>) -> Unit)? = null

        override fun bindPreview(
            lifecycleOwner: LifecycleOwner,
            cameraSelector: CameraSelector,
            preview: Preview,
            onResult: (Result<Unit>) -> Unit
        ) {
            callback = onResult
        }

        override fun unbindPreview(preview: Preview) {}

        fun respond(result: Result<Unit>) {
            callback?.invoke(result)
        }
    }
}
