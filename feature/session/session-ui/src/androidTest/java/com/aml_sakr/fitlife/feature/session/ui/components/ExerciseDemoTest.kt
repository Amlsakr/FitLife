package com.aml_sakr.fitlife.feature.session.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import org.junit.Rule
import org.junit.Test

class ExerciseDemoTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun exerciseDemo_showsPlaceholder_whenPathIsNull() {
        composeRule.setContent {
            FitnessAppTheme {
                ExerciseDemo(
                    lottiePath = null,
                    totalReps = 0
                )
            }
        }

        composeRule.onNodeWithContentDescription("Exercise Placeholder").assertIsDisplayed()
    }

    @Test
    fun exerciseDemo_showsPlaceholder_whenPathIsInvalid() {
        composeRule.setContent {
            FitnessAppTheme {
                ExerciseDemo(
                    lottiePath = "invalid_path",
                    totalReps = 0
                )
            }
        }

        composeRule.onNodeWithContentDescription("Exercise Placeholder").assertIsDisplayed()
    }
}
