package com.aml_sakr.fitlife.feature.onboarding.ui.beginner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aml_sakr.fitlife.core.ui.theme.FitLifeDimens
import com.aml_sakr.fitlife.feature.onboarding.domain.model.BeginnerOnboardingStep
import com.aml_sakr.fitlife.feature.onboarding.domain.model.Equipment
import com.aml_sakr.fitlife.feature.onboarding.domain.model.FitnessGoal
import androidx.compose.foundation.layout.size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeginnerOnboardingScreen(
    state: BeginnerOnboardingState,
    onBack: () -> Unit,
    onGoalToggled: (FitnessGoal) -> Unit,
    onEquipmentToggled: (Equipment) -> Unit,
    onFrequencySelected: (Int) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val continueLabel = when (state.currentStep) {
        BeginnerOnboardingStep.Frequency -> "Finish"
        else -> "Continue"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Beginner onboarding",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    Surface(
                        onClick = onBack,
                        enabled = !state.isLoading,
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .semantics {
                                contentDescription = "Back to level selector"
                            }
                            .padding(start = FitLifeDimens.SpaceSm)
                            .width(48.dp)
                            .height(48.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "<",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                        onClick = onContinue,
                        enabled = state.canContinue && !state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (state.isLoading && state.currentStep == BeginnerOnboardingStep.Frequency) {
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
                LinearProgressIndicator(
                    progress = { (state.currentStep.ordinal + 1) / 3f },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = when (state.currentStep) {
                        BeginnerOnboardingStep.Goals -> "What do you want to improve?"
                        BeginnerOnboardingStep.Equipment -> "What equipment do you have?"
                        BeginnerOnboardingStep.Frequency -> "How often do you want to train?"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (state.currentStep) {
                        BeginnerOnboardingStep.Goals -> "Pick every goal that fits you. We can tailor the plan from there."
                        BeginnerOnboardingStep.Equipment -> "Choose the equipment you can actually use right now."
                        BeginnerOnboardingStep.Frequency -> "Pick a weekly rhythm that feels realistic and sustainable."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (state.errorMessage != null) {
                ErrorBanner(message = state.errorMessage)
            }

            when (state.currentStep) {
                BeginnerOnboardingStep.Goals -> GoalSelection(
                    selectedGoals = state.selectedGoals,
                    enabled = !state.isLoading,
                    onGoalToggled = onGoalToggled
                )
                BeginnerOnboardingStep.Equipment -> EquipmentSelection(
                    selectedEquipment = state.selectedEquipment,
                    enabled = !state.isLoading,
                    onEquipmentToggled = onEquipmentToggled
                )
                BeginnerOnboardingStep.Frequency -> FrequencySelection(
                    selectedFrequency = state.weeklyFrequency,
                    enabled = !state.isLoading,
                    onFrequencySelected = onFrequencySelected
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
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
                    contentDescription = "Goal ${goal.displayName}"
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EquipmentSelection(
    selectedEquipment: Set<Equipment>,
    enabled: Boolean,
    onEquipmentToggled: (Equipment) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceSm),
        verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceSm)
    ) {
        Equipment.entries.forEach { equipment ->
            FilterChip(
                selected = selectedEquipment.contains(equipment),
                enabled = enabled,
                onClick = { onEquipmentToggled(equipment) },
                label = {
                    Text(
                        text = equipment.displayName,
                        textAlign = TextAlign.Center
                    )
                },
                modifier = Modifier.semantics {
                    contentDescription = "Equipment ${equipment.displayName}"
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FrequencySelection(
    selectedFrequency: Int?,
    enabled: Boolean,
    onFrequencySelected: (Int) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceSm),
        verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceSm)
    ) {
        (1..7).forEach { frequency ->
            FilterChip(
                selected = selectedFrequency == frequency,
                enabled = enabled,
                onClick = { onFrequencySelected(frequency) },
                label = {
                    Text(
                        text = "$frequency day${if (frequency == 1) "" else "s"} / week",
                        textAlign = TextAlign.Center
                    )
                },
                modifier = Modifier.semantics {
                    contentDescription = "Frequency $frequency days per week"
                }
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

private val FitnessGoal.displayName: String
    get() = when (this) {
        FitnessGoal.WeightLoss -> "Weight loss"
        FitnessGoal.Strength -> "Strength"
        FitnessGoal.GeneralHealth -> "General health"
    }

private val Equipment.displayName: String
    get() = when (this) {
        Equipment.None -> "No equipment"
        Equipment.ResistanceBands -> "Resistance bands"
        Equipment.Dumbbells -> "Dumbbells"
        Equipment.FullGym -> "Full gym"
    }
