package com.aml_sakr.fitlife.feature.session.ui.permission

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CameraPermissionViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sessionEntered_withoutRationale_keepsDisclosureReadyAndIdle() = runTest(dispatcher) {
        val viewModel = CameraPermissionViewModel()

        viewModel.onEvent(
            CameraPermissionEvent.SessionEntered(
                shouldShowRationale = false
            )
        )

        assertEquals(
            CameraPermissionState(),
            viewModel.state.value
        )
    }

    @Test
    fun sessionEntered_withRationale_marksRationaleRequired() = runTest(dispatcher) {
        val viewModel = CameraPermissionViewModel()

        viewModel.onEvent(
            CameraPermissionEvent.SessionEntered(
                shouldShowRationale = true
            )
        )

        assertTrue(viewModel.state.value.isRationaleRequired)
        assertFalse(viewModel.state.value.isPermissionRequestPending)
    }

    @Test
    fun continueClicked_requestsSystemPermission_once() = runTest(dispatcher) {
        val viewModel = CameraPermissionViewModel()
        val action = async { viewModel.actions.first() }

        viewModel.onEvent(CameraPermissionEvent.ContinueClicked)
        runCurrent()

        assertTrue(viewModel.state.value.isPermissionRequestPending)
        assertEquals(CameraPermissionAction.RequestSystemPermission, action.await())
    }

    @Test
    fun continueClicked_whilePending_doesNotDuplicatePermissionRequest() = runTest(dispatcher) {
        val viewModel = CameraPermissionViewModel()

        viewModel.onEvent(CameraPermissionEvent.ContinueClicked)
        runCurrent()
        viewModel.onEvent(CameraPermissionEvent.ContinueClicked)
        runCurrent()

        assertTrue(viewModel.state.value.isPermissionRequestPending)
    }

    @Test
    fun permissionResultGranted_clearsPendingStateWithoutFallback() = runTest(dispatcher) {
        val viewModel = CameraPermissionViewModel()

        viewModel.onEvent(
            CameraPermissionEvent.SystemPermissionResult(
                isGranted = true
            )
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isPermissionRequestPending)
        assertFalse(viewModel.state.value.isAudioOnlyFallbackVisible)
    }

    @Test
    fun permissionResultDenied_emitsAudioOnlyActionAndShowsFallback() = runTest(dispatcher) {
        val viewModel = CameraPermissionViewModel()
        val action = async { viewModel.actions.first() }

        viewModel.onEvent(
            CameraPermissionEvent.SystemPermissionResult(
                isGranted = false
            )
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isPermissionRequestPending)
        assertTrue(viewModel.state.value.isAudioOnlyFallbackVisible)
        assertEquals(CameraPermissionAction.EnterAudioOnlySession, action.await())
    }

    @Test
    fun sessionEntered_afterDenial_keepsAudioOnlyFallbackVisible() = runTest(dispatcher) {
        val viewModel = CameraPermissionViewModel()

        viewModel.onEvent(
            CameraPermissionEvent.SystemPermissionResult(
                isGranted = false
            )
        )
        advanceUntilIdle()

        viewModel.onEvent(
            CameraPermissionEvent.SessionEntered(
                shouldShowRationale = true
            )
        )

        assertTrue(viewModel.state.value.isAudioOnlyFallbackVisible)
        assertTrue(viewModel.state.value.isRationaleRequired)
    }

    @Test
    fun rationaleStatusChanged_afterDenial_updatesRationaleWithoutClearingFallback() = runTest(dispatcher) {
        val viewModel = CameraPermissionViewModel()

        viewModel.onEvent(
            CameraPermissionEvent.SystemPermissionResult(
                isGranted = false
            )
        )
        advanceUntilIdle()

        viewModel.onEvent(
            CameraPermissionEvent.RationaleStatusChanged(
                shouldShowRationale = true
            )
        )

        assertTrue(viewModel.state.value.isAudioOnlyFallbackVisible)
        assertTrue(viewModel.state.value.isRationaleRequired)
        assertFalse(viewModel.state.value.isPermissionRequestPending)
    }

    @Test
    fun useAudioOnlyClicked_emitsAudioOnlyActionWithoutPermissionPrompt() = runTest(dispatcher) {
        val viewModel = CameraPermissionViewModel()
        val action = async { viewModel.actions.first() }

        viewModel.onEvent(CameraPermissionEvent.UseAudioOnlyClicked)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isAudioOnlyFallbackVisible)
        assertEquals(CameraPermissionAction.EnterAudioOnlySession, action.await())
    }
}
