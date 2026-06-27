package com.aml_sakr.fitlife.feature.workout.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutDay
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutExercise
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutFitnessLevel
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutGoal
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutLocation
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutPlan
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WorkoutScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_successState_displaysDaysAndClickTriggersNavigation() {
        var navigatedDayNumber: Int? = null
        val samplePlan = WorkoutPlan(
            userId = "user-1",
            fitnessLevel = WorkoutFitnessLevel.Beginner,
            goals = setOf(WorkoutGoal.GeneralHealth),
            location = WorkoutLocation.Home,
            availableEquipment = setOf("bodyweight"),
            days = listOf(
                WorkoutDay(
                    day = 1,
                    title = "Test Day 1",
                    durationMinutes = 30,
                    exercises = listOf(
                        WorkoutExercise("Exercise A", sets = 3, reps = "10", estimatedDurationMinutes = 10)
                    )
                ),
                WorkoutDay(
                    day = 2,
                    title = "Test Day 2",
                    durationMinutes = 45,
                    exercises = listOf(
                        WorkoutExercise("Exercise B", sets = 4, reps = "12", estimatedDurationMinutes = 15)
                    )
                )
            ),
            generatedAtEpochMillis = 1000L,
            expiresAtEpochMillis = 2000L,
            isFallback = false
        )
        val state = WorkoutHomeState.Success(plan = samplePlan)

        composeTestRule.setContent {
            FitnessAppTheme {
                WorkoutHomeScreen(
                    state = state,
                    onRequestPlan = {},
                    onNavigateToDayDetail = { day -> navigatedDayNumber = day }
                )
            }
        }

        // Verify day cards are displayed
        composeTestRule.onNodeWithText("Day 1").assertExists()
        composeTestRule.onNodeWithText("Test Day 1").assertExists()
        composeTestRule.onNodeWithText("Day 2").assertExists()
        composeTestRule.onNodeWithText("Test Day 2").assertExists()

        // Perform click on Day 1 card
        composeTestRule.onNodeWithText("Test Day 1").performClick()

        // Assert navigation callback triggered with correct day number
        assertEquals(1, navigatedDayNumber)
    }

    @Test
    fun dayDetailScreen_displaysExerciseDetailsCorrectly() {
        var backClicked = false
        val sampleDay = WorkoutDay(
            day = 3,
            title = "Leg Day",
            durationMinutes = 40,
            exercises = listOf(
                WorkoutExercise("Squats", sets = 4, reps = "10", estimatedDurationMinutes = 15),
                WorkoutExercise("Lunges", sets = 3, reps = "12", estimatedDurationMinutes = 12)
            )
        )

        composeTestRule.setContent {
            FitnessAppTheme {
                WorkoutDayDetailScreen(
                    workoutDay = sampleDay,
                    onBack = { backClicked = true }
                )
            }
        }

        // Verify title
        composeTestRule.onNodeWithText("Day 3: Leg Day").assertExists()

        // Verify exercises list
        composeTestRule.onNodeWithText("Squats").assertExists()
        composeTestRule.onNodeWithText("4 sets x 10 reps").assertExists()
        composeTestRule.onNodeWithText("15 min").assertExists()

        composeTestRule.onNodeWithText("Lunges").assertExists()
        composeTestRule.onNodeWithText("3 sets x 12 reps").assertExists()
        composeTestRule.onNodeWithText("12 min").assertExists()
    }
}
