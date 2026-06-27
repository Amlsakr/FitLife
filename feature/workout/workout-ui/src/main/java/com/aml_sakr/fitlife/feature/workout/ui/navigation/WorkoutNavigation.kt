package com.aml_sakr.fitlife.feature.workout.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import com.aml_sakr.fitlife.feature.workout.ui.WorkoutDayDetailScreen

@Composable
fun WorkoutTabHost(
    isVisible: Boolean,
    backStack: NavBackStack<WorkoutDestination>,
    modifier: Modifier = Modifier
) {
    NavDisplay(
        backStack = backStack,
        modifier = modifier.tabHostVisibility(isVisible),
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider<WorkoutDestination> {
            registerWorkoutEntries(backStack)
        }
    )
}

fun EntryProviderScope<WorkoutDestination>.registerWorkoutEntries(
    backStack: NavBackStack<WorkoutDestination>
) {
    entry<WorkoutDestination.Root> {
        WorkoutTabScreen()
    }

    entry<WorkoutDestination.DayDetail> { dayDetail ->
        val dayNumber = dayDetail.day
        // Note: For pure UI navigation registration, we render the screen.
        // The host app can pass the specific day content when integrated, or we can use a placeholder day.
        WorkoutDayDetailScreen(
            workoutDay = null, // Can be integrated/populated by the host VM/repository
            onBack = { if (backStack.size > 1) backStack.removeLastOrNull() }
        )
    }
}

@Serializable
sealed interface WorkoutDestination : NavKey {
    @Serializable
    data object Root : WorkoutDestination

    @Serializable
    data class DayDetail(val day: Int) : WorkoutDestination
}

private fun Modifier.tabHostVisibility(isVisible: Boolean): Modifier =
    if (isVisible) {
        this.zIndex(1f)
    } else {
        this.clearAndSetSemantics { }.zIndex(0f)
    }

@Composable
private fun WorkoutTabScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Workout",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Your workout planning tools will live here.",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
