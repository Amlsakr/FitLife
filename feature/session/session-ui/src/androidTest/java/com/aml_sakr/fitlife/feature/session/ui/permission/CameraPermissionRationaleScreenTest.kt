package com.aml_sakr.fitlife.feature.session.ui.permission

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CameraPermissionRationaleScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun screen_displaysDisclosureAndInvokesCallbacks() {
        var continueClicked = false
        var audioOnlyClicked = false

        composeRule.setContent {
            FitnessAppTheme {
                CameraPermissionRationaleScreen(
                    state = CameraPermissionState(isRationaleRequired = true),
                    onContinue = { continueClicked = true },
                    onUseAudioOnly = { audioOnlyClicked = true }
                )
            }
        }

        composeRule.onNodeWithText("Keep your form analysis private").assertIsDisplayed()
        composeRule.onNodeWithText("Nothing is uploaded or stored.").assertIsDisplayed()
        composeRule.onNodeWithText("Continue to camera").performClick()
        composeRule.onNodeWithText("Use audio only").performClick()

        assertTrue(continueClicked)
        assertTrue(audioOnlyClicked)
    }

    @Test
    fun screen_showsAudioOnlyFallbackNotice_whenFallbackVisible() {
        composeRule.setContent {
            FitnessAppTheme {
                CameraPermissionRationaleScreen(
                    state = CameraPermissionState(isAudioOnlyFallbackVisible = true),
                    onContinue = {},
                    onUseAudioOnly = {}
                )
            }
        }

        composeRule.onNodeWithText("Audio-only guidance is available, so you can keep training without camera access.")
            .assertIsDisplayed()
    }
}
