package com.aml_sakr.fitlife.feature.onboarding.ui.intermediate

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aml_sakr.fitlife.core.ui.theme.FitLifeDimens
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessGoal
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.IntermediateTrainingSplit
import com.aml_sakr.fitlife.feature.onboarding.domain.model.OneRepMaxLift

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntermediateOnboardingScreen(
    state: IntermediateOnboardingState,
    onBack: () -> Unit,
    onSplitSelected: (IntermediateTrainingSplit) -> Unit,
    onGoalToggled: (FitnessGoal) -> Unit,
    onOneRepMaxValueChanged: (OneRepMaxLift, String) -> Unit,
    onOneRepMaxUnitChanged: (OneRepMaxLift, OneRepMaxUnit) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val continueLabel = when (state.currentStep) {
        IntermediateOnboardingStep.OneRepMax -> "Finish"
        else -> "Continue"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Intermediate onboarding",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = !state.isLoading,
                        modifier = Modifier.semantics {
                            contentDescription = "Back to level selector"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = FitLifeDimens.SpaceLg,
                            top = FitLifeDimens.SpaceMd,
                            end = FitLifeDimens.SpaceLg,
                            bottom = FitLifeDimens.SpaceLg
                        ),
                    verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceSm)
                ) {
                    Button(
                        onClick = {
                            onContinue.invoke()
                            Log.e("onboarding", "continue")
                        },
                        enabled = !state.isLoading && state.canContinue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (state.isLoading && state.currentStep == IntermediateOnboardingStep.OneRepMax) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(text = continueLabel, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = FitLifeDimens.SpaceLg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceLg)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceSm)) {
                Text(
                    text = "Step ${state.currentStep.ordinal + 1} of 3",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { (state.currentStep.ordinal + 1) / 3f },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = when (state.currentStep) {
                        IntermediateOnboardingStep.Split -> "Choose your split"
                        IntermediateOnboardingStep.Goals -> "Pick your goals"
                        IntermediateOnboardingStep.OneRepMax -> "Estimate your 1RM"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (state.currentStep) {
                        IntermediateOnboardingStep.Split ->
                            "Pick the routine that best matches how you already train."

                        IntermediateOnboardingStep.Goals ->
                            "Choose every goal you want the plan to support."

                        IntermediateOnboardingStep.OneRepMax ->
                            "Optional. Leave any lift blank if you do not know it yet."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            state.errorMessage?.let { message ->
                ErrorBanner(message = message)
            }

            if (state.currentStep == IntermediateOnboardingStep.OneRepMax) {
                state.oneRepMaxValidationError()?.let { message ->
                    ErrorBanner(message = message)
                }
            }

            when (state.currentStep) {
                IntermediateOnboardingStep.Split -> SplitSelection(
                    selectedSplit = state.selectedSplit,
                    enabled = !state.isLoading,
                    onSplitSelected = onSplitSelected
                )

                IntermediateOnboardingStep.Goals -> GoalSelection(
                    selectedGoals = state.selectedGoals,
                    enabled = !state.isLoading,
                    onGoalToggled = onGoalToggled
                )

                IntermediateOnboardingStep.OneRepMax -> OneRepMaxSelection(
                    entries = state.oneRepMaxInputs,
                    enabled = !state.isLoading,
                    onValueChanged = onOneRepMaxValueChanged,
                    onUnitChanged = onOneRepMaxUnitChanged
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SplitSelection(
    selectedSplit: IntermediateTrainingSplit?,
    enabled: Boolean,
    onSplitSelected: (IntermediateTrainingSplit) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceSm)) {
        IntermediateTrainingSplit.entries.forEach { split ->
            val selected = selectedSplit == split
            Card(
                onClick = { onSplitSelected(split) },
                enabled = enabled,
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        selected -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceContainerLow
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        role = Role.Button
                        this.selected = selected
                        contentDescription = "Split ${split.displayName}"
                    }
            ) {
                Column(
                    modifier = Modifier.padding(FitLifeDimens.SpaceMd),
                    verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceXs)
                ) {
                    Text(
                        text = split.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = split.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoalSelection(
    selectedGoals: Set<FitnessGoal>,
    enabled: Boolean,
    onGoalToggled: (FitnessGoal) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceSm),
        verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceSm)
    ) {
        FitnessGoal.entries.forEach { goal ->
            FilterChip(
                selected = selectedGoals.contains(goal),
                enabled = enabled,
                onClick = { onGoalToggled(goal) },
                label = {
                    Text(
                        text = goal.displayName,
                        textAlign = TextAlign.Center
                    )
                },
                modifier = Modifier.semantics {
                    role = Role.Button
                    selected = selectedGoals.contains(goal)
                    contentDescription = "Goal ${goal.displayName}"
                }
            )
        }
    }
}

@Composable
private fun OneRepMaxSelection(
    entries: Map<OneRepMaxLift, OneRepMaxInput>,
    enabled: Boolean,
    onValueChanged: (OneRepMaxLift, String) -> Unit,
    onUnitChanged: (OneRepMaxLift, OneRepMaxUnit) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceMd)) {
        OneRepMaxLift.entries.forEach { lift ->
            val input = entries.getValue(lift)
            Column(verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceSm)) {
                Text(
                    text = lift.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceSm)
                ) {
                    OutlinedTextField(
                        value = input.valueText,
                        onValueChange = { onValueChanged(lift, it) },
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        ),
                        placeholder = { Text("0") }
                    )
                    UnitPicker(
                        selectedUnit = input.unit,
                        enabled = enabled,
                        onUnitChanged = { onUnitChanged(lift, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitPicker(
    selectedUnit: OneRepMaxUnit,
    enabled: Boolean,
    onUnitChanged: (OneRepMaxUnit) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceXs)) {
        OneRepMaxUnit.entries.forEach { unit ->
            FilterChip(
                selected = selectedUnit == unit,
                enabled = enabled,
                onClick = { onUnitChanged(unit) },
                label = { Text(unit.displayName) }
            )
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(FitLifeDimens.SpaceMd),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

private val IntermediateTrainingSplit.displayName: String
    get() = when (this) {
        IntermediateTrainingSplit.FullBody -> "Full Body"
        IntermediateTrainingSplit.UpperLower -> "Upper/Lower"
        IntermediateTrainingSplit.PushPullLegs -> "Push/Pull/Legs"
    }

private val IntermediateTrainingSplit.description: String
    get() = when (this) {
        IntermediateTrainingSplit.FullBody -> "Train the full body in each session."
        IntermediateTrainingSplit.UpperLower -> "Split sessions between upper and lower body movements."
        IntermediateTrainingSplit.PushPullLegs -> "Separate pushing, pulling, and leg days."
    }

private val FitnessGoal.displayName: String
    get() = when (this) {
        FitnessGoal.WeightLoss -> "Weight loss"
        FitnessGoal.Strength -> "Strength"
        FitnessGoal.GeneralHealth -> "General health"
    }

private val OneRepMaxLift.displayName: String
    get() = when (this) {
        OneRepMaxLift.BenchPress -> "Bench Press"
        OneRepMaxLift.Squat -> "Squat"
        OneRepMaxLift.Deadlift -> "Deadlift"
    }

private val OneRepMaxUnit.displayName: String
    get() = when (this) {
        OneRepMaxUnit.Kilograms -> "KG"
        OneRepMaxUnit.Pounds -> "LBS"
    }
