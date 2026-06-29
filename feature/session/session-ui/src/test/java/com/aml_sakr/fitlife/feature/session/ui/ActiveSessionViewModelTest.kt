package com.aml_sakr.fitlife.feature.session.ui

import com.aml_sakr.fitlife.core.domain.AnalyticsLogger
import com.aml_sakr.fitlife.feature.session.domain.pose.AnalyzePoseUseCase
import com.aml_sakr.fitlife.feature.session.domain.pose.DetectFatigueUseCase
import com.aml_sakr.fitlife.feature.session.domain.pose.DetectRepUseCase
import com.aml_sakr.fitlife.feature.session.domain.pose.FatigueStatus
import com.aml_sakr.fitlife.feature.session.domain.pose.LightingStatus
import com.aml_sakr.fitlife.feature.session.domain.pose.LightingUseCase
import com.aml_sakr.fitlife.feature.session.domain.pose.PoseData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveSessionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var viewModel: ActiveSessionViewModel
    private val analyzePoseUseCase: AnalyzePoseUseCase = mock()
    private val detectFatigueUseCase: DetectFatigueUseCase = mock()
    private val detectRepUseCase: DetectRepUseCase = mock()
    private val lightingUseCase: LightingUseCase = mock()
    private val analyticsLogger: AnalyticsLogger = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(lightingUseCase.invoke(any())).thenReturn(emptyFlow())
        viewModel = ActiveSessionViewModel(
            analyzePoseUseCase,
            detectFatigueUseCase,
            detectRepUseCase,
            lightingUseCase,
            analyticsLogger
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `PoseDetected event - updates state and checks for rep completion`() {
        val poseData = createPoseData()
        whenever(detectRepUseCase.processPose(poseData)).thenReturn(null)

        viewModel.onEvent(ActiveSessionEvent.PoseDetected(poseData))

        assertEquals(poseData, viewModel.state.value.latestPoseData)
        verify(detectRepUseCase).processPose(poseData)
    }

    @Test
    fun `RepCompleted event - detects fatigue and logs analytics`() {
        val peakPose = createPoseData()
        whenever(detectFatigueUseCase.analyzeRep(peakPose)).thenReturn(FatigueStatus.FATIGUED)

        viewModel.onEvent(ActiveSessionEvent.RepCompleted(peakPose))

        assertTrue(viewModel.state.value.isFatigued)
        verify(analyticsLogger).logEvent("fatigue_detected", mapOf("rep_number" to 1))
    }

    @Test
    fun `DismissFatigue event - clears fatigue state and logs analytics`() {
        // First trigger fatigue
        val peakPose = createPoseData()
        whenever(detectFatigueUseCase.analyzeRep(peakPose)).thenReturn(FatigueStatus.FATIGUED)
        viewModel.onEvent(ActiveSessionEvent.RepCompleted(peakPose))
        assertTrue(viewModel.state.value.isFatigued)

        // Then dismiss
        viewModel.onEvent(ActiveSessionEvent.DismissFatigue)

        assertFalse(viewModel.state.value.isFatigued)
        verify(analyticsLogger).logEvent("fatigue_dismissed")
    }

    @Test
    fun `cooldown logic - prevents fatigue re-trigger for 5 reps after dismissal`() {
        val peakPose = createPoseData()
        // Improved: Use specific mock behavior instead of broad any()
        whenever(detectFatigueUseCase.analyzeRep(peakPose)).thenReturn(FatigueStatus.FATIGUED)

        // 1. Fatigue detected
        viewModel.onEvent(ActiveSessionEvent.RepCompleted(peakPose))
        assertTrue(viewModel.state.value.isFatigued)

        // 2. Dismiss
        viewModel.onEvent(ActiveSessionEvent.DismissFatigue)
        assertFalse(viewModel.state.value.isFatigued)

        // 3. Next 5 reps - should NOT trigger fatigue even if detected
        repeat(5) { rep ->
            viewModel.onEvent(ActiveSessionEvent.RepCompleted(peakPose))
            assertFalse("Fatigue should be suppressed at rep ${rep + 1} after dismissal", viewModel.state.value.isFatigued)
        }

        // 4. 6th rep after dismissal - should trigger fatigue again
        viewModel.onEvent(ActiveSessionEvent.RepCompleted(peakPose))
        assertTrue("Fatigue should be re-triggered at 6th rep", viewModel.state.value.isFatigued)
    }

    private fun createPoseData() = PoseData(
        timestampMillis = System.currentTimeMillis(),
        joints = emptyMap(),
        overallConfidence = 1f,
        sourceWidth = 640,
        sourceHeight = 480
    )
}
