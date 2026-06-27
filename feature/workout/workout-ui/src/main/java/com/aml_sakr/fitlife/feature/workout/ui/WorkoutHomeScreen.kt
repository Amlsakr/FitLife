package com.aml_sakr.fitlife.feature.workout.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aml_sakr.fitlife.core.ui.theme.FitLifeDimens
import com.aml_sakr.fitlife.core.ui.theme.FitnessAppTheme
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutDay
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutExercise
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutFitnessLevel
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutGoal
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutLocation
import com.aml_sakr.fitlife.feature.workout.domain.model.WorkoutPlan

@Composable
fun WorkoutHomeScreen(
    state: WorkoutHomeState,
    onRequestPlan: () -> Unit,
    onNavigateToDayDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(FitLifeDimens.SpaceLg),
            verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceLg)
        ) {
            WorkoutHomeHeader()

            when (state) {
                WorkoutHomeState.Loading -> LoadingPlanState(WorkoutHomeState.Loading)
                WorkoutHomeState.Empty -> EmptyPlanState(
                    state = WorkoutHomeState.Empty,
                    onRequestPlan = onRequestPlan
                )
                is WorkoutHomeState.Success -> SuccessPlanState(
                    state = state,
                    onRequestPlan = onRequestPlan,
                    onNavigateToDayDetail = onNavigateToDayDetail
                )
                is WorkoutHomeState.Error -> ErrorPlanState(
                    state = state,
                    onRequestPlan = onRequestPlan
                )
            }
        }
    }
}

@Composable
private fun WorkoutHomeHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceSm)
    ) {
        Text(
            text = "Home",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Weekly workout plan",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LoadingPlanState(state: WorkoutHomeState.Loading) {
    StatePanel {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(44.dp)
        )
        Text(
            text = "Preparing your weekly plan",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "This usually takes a moment.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        PlanActionButton(
            label = state.primaryCtaLabel,
            enabled = state.canRequestPlan,
            isLoading = state.isRequestInFlight,
            icon = Icons.Default.Refresh,
            onClick = {}
        )
    }
}

@Composable
private fun EmptyPlanState(
    state: WorkoutHomeState.Empty,
    onRequestPlan: () -> Unit
) {
    StatePanel {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(44.dp)
        )
        Text(
            text = "No plan ready",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Start with a fresh 7-day plan based on your current profile.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        PlanActionButton(
            label = state.primaryCtaLabel,
            enabled = state.canRequestPlan,
            isLoading = state.isRequestInFlight,
            icon = Icons.Default.FitnessCenter,
            onClick = onRequestPlan
        )
    }
}

@Composable
private fun SuccessPlanState(
    state: WorkoutHomeState.Success,
    onRequestPlan: () -> Unit,
    onNavigateToDayDetail: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceMd)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceXs)
            ) {
                Text(
                    text = "${state.plan.days.size} days - ${state.plan.location.displayName()} - ${state.plan.fitnessLevel.displayName()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (state.plan.isFallback) {
                    Text(
                        text = "Fallback plan",
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            PlanActionButton(
                label = state.primaryCtaLabel,
                enabled = state.canRequestPlan,
                isLoading = state.isRequestInFlight,
                icon = Icons.Default.Refresh,
                onClick = onRequestPlan,
                modifier = Modifier.weight(1f)
            )
        }

        WeeklyOverview(
            days = state.plan.days,
            onDayClick = onNavigateToDayDetail
        )

        WorkoutPlanSummaryCard(plan = state.plan)
    }
}

@Composable
private fun ErrorPlanState(
    state: WorkoutHomeState.Error,
    onRequestPlan: () -> Unit
) {
    StatePanel(
        borderColor = MaterialTheme.colorScheme.error,
        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.06f)
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(44.dp)
        )
        Text(
            text = "Plan unavailable",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = state.message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        PlanActionButton(
            label = state.primaryCtaLabel,
            enabled = state.canRequestPlan,
            isLoading = state.isRequestInFlight,
            icon = Icons.Default.Refresh,
            onClick = onRequestPlan
        )
    }
}

@Composable
private fun StatePanel(
    modifier: Modifier = Modifier,
    borderColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.outline,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FitLifeDimens.CornerMd),
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.36f)),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FitLifeDimens.SpaceLg),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceMd),
            content = content
        )
    }
}

@Composable
private fun WorkoutPlanSummaryCard(
    plan: WorkoutPlan,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FitLifeDimens.CornerMd),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FitLifeDimens.SpaceMd),
            verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceSm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceMd),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(FitLifeDimens.CornerSm)
                ) {
                    Text(
                        text = "${plan.days.size} days",
                        modifier = Modifier.padding(
                            horizontal = FitLifeDimens.SpaceSm,
                            vertical = FitLifeDimens.SpaceXs
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceXs)
                ) {
                    Text(
                        text = "${plan.location.displayName()} - ${plan.fitnessLevel.displayName()}",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (plan.isFallback) {
                            "Fallback plan"
                        } else {
                            "Generated plan"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            plan.days.forEach { day ->
                Text(
                    text = "Day ${day.day}: ${day.title} - ${day.durationMinutes} min",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PlanActionButton(
    label: String,
    enabled: Boolean,
    isLoading: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 52.dp),
        shape = RoundedCornerShape(FitLifeDimens.CornerMd),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f),
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.size(FitLifeDimens.SpaceSm))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun WorkoutFitnessLevel.displayName(): String =
    when (this) {
        WorkoutFitnessLevel.Beginner -> "Beginner"
        WorkoutFitnessLevel.Intermediate -> "Intermediate"
    }

private fun WorkoutLocation.displayName(): String =
    when (this) {
        WorkoutLocation.Home -> "Home"
        WorkoutLocation.Gym -> "Gym"
        WorkoutLocation.Outdoor -> "Outdoor"
    }

@Preview(showBackground = true)
@Composable
private fun WorkoutHomeEmptyPreview() {
    FitnessAppTheme {
        WorkoutHomeScreen(
            state = WorkoutHomeState.Empty,
            onRequestPlan = {},
            onNavigateToDayDetail = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutHomeSuccessPreview() {
    FitnessAppTheme {
        WorkoutHomeScreen(
            state = WorkoutHomeState.Success(previewPlan()),
            onRequestPlan = {},
            onNavigateToDayDetail = {}
        )
    }
}

private fun previewPlan(): WorkoutPlan =
    WorkoutPlan(
        userId = "preview-user",
        fitnessLevel = WorkoutFitnessLevel.Beginner,
        goals = setOf(WorkoutGoal.GeneralHealth),
        location = WorkoutLocation.Home,
        availableEquipment = setOf("bodyweight", "chair"),
        days = (1..7).map { day ->
            WorkoutDay(
                day = day,
                title = "Full-body foundation",
                durationMinutes = 30,
                exercises = listOf(
                    WorkoutExercise(
                        name = "Bodyweight squat",
                        sets = 3,
                        reps = "10",
                        estimatedDurationMinutes = 8
                    ),
                    WorkoutExercise(
                        name = "Incline push-up",
                        sets = 3,
                        reps = "8",
                        estimatedDurationMinutes = 7
                    )
                )
            )
        },
        generatedAtEpochMillis = 1_000L,
        expiresAtEpochMillis = 2_000L,
        isFallback = false
    )

fun WorkoutDay.calculateTotalReps(): Int {
    return exercises.sumOf { exercise ->
        val cleanReps = exercise.reps.split('-', ' ', '/').firstOrNull { part ->
            part.any { it.isDigit() }
        }?.filter { it.isDigit() }?.toIntOrNull() ?: 0
        exercise.sets * cleanReps
    }
}

@Composable
private fun WeeklyOverview(
    days: List<WorkoutDay>,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceSm)
    ) {
        Text(
            text = "Weekly overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceMd),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(days.size) { index ->
                val day = days[index]
                WorkoutDayCard(
                    day = day,
                    onClick = { onDayClick(day.day) }
                )
            }
        }
    }
}

@Composable
private fun WorkoutDayCard(
    day: WorkoutDay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalReps = day.calculateTotalReps()
    Card(
        modifier = modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(FitLifeDimens.CornerMd),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(FitLifeDimens.SpaceMd)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FitLifeDimens.SpaceXs)
        ) {
            Text(
                text = "Day ${day.day}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = day.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(FitLifeDimens.SpaceXs))
            Text(
                text = "${day.durationMinutes} min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$totalReps reps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
