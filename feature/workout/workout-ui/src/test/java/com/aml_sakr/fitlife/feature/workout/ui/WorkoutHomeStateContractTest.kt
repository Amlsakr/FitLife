package com.aml_sakr.fitlife.feature.workout.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutHomeStateContractTest {
    @Test
    fun emptyState_exposesGeneratePlanCopyAndBranch() {
        assertEquals(WorkoutHomeRenderBranch.Empty, WorkoutHomeState.Empty.renderBranch)
        assertEquals("Generate a plan", WorkoutHomeState.Empty.primaryCtaLabel)
        assertTrue(WorkoutHomeState.Empty.canRequestPlan)
    }

    @Test
    fun loadingState_exposesDisabledLoadingBranch() {
        assertEquals(WorkoutHomeRenderBranch.Loading, WorkoutHomeState.Loading.renderBranch)
        assertEquals("Generating", WorkoutHomeState.Loading.primaryCtaLabel)
        assertFalse(WorkoutHomeState.Loading.canRequestPlan)
    }

    @Test
    fun successState_exposesRefreshContractWithoutDuplicatingPlan() {
        val plan = WorkoutHomeTestFixtures.samplePlan()
        val state = WorkoutHomeState.Success(plan = plan)

        assertEquals(WorkoutHomeRenderBranch.Success, state.renderBranch)
        assertEquals("Refresh plan", state.primaryCtaLabel)
        assertTrue(state.canRequestPlan)
        assertEquals(plan, state.plan)
    }

    @Test
    fun errorState_exposesRetryContractWithSafeMessage() {
        val state = WorkoutHomeState.Error(message = WorkoutHomeCopy.GenericErrorMessage)

        assertEquals(WorkoutHomeRenderBranch.Error, state.renderBranch)
        assertEquals("Try again", state.primaryCtaLabel)
        assertTrue(state.canRequestPlan)
        assertEquals(WorkoutHomeCopy.GenericErrorMessage, state.message)
    }
}
