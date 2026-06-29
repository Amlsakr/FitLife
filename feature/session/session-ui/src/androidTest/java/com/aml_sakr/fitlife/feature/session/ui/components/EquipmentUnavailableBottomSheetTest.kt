package com.aml_sakr.fitlife.feature.session.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.aml_sakr.fitlife.feature.session.domain.equipment.ExerciseAlternative
import com.aml_sakr.fitlife.feature.session.domain.model.ExerciseDifficulty
import org.junit.Rule
import org.junit.Test

class EquipmentUnavailableBottomSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomSheet_showsAlternatives() {
        val alternatives = listOf(
            ExerciseAlternative(
                exerciseId = "1",
                name = "Goblet Squat",
                description = "Desc",
                muscleGroups = listOf("Quads"),
                equipmentRequired = "Dumbbell",
                difficulty = ExerciseDifficulty.INTERMEDIATE,
                lottieAssetPath = null,
                defaultSets = 3,
                defaultReps = 12
            )
        )

        composeTestRule.setContent {
            EquipmentUnavailableBottomSheet(
                alternatives = alternatives,
                isLoading = false,
                onAlternativeSelected = {},
                onDismiss = {}
            )
        }

        composeTestRule.onNodeWithText("Equipment Unavailable?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Goblet Squat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select").assertIsDisplayed()
    }

    @Test
    fun bottomSheet_showsLoading() {
        composeTestRule.setContent {
            EquipmentUnavailableBottomSheet(
                alternatives = emptyList(),
                isLoading = true,
                onAlternativeSelected = {},
                onDismiss = {}
            )
        }

        composeTestRule.onNodeWithText("Equipment Unavailable?").assertIsDisplayed()
        // CircularProgressIndicator doesn't have text, but we could test for its existence via tag
    }
}
