package com.aml_sakr.fitlife.feature.progress.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.aml_sakr.fitlife.feature.progress.ui.ProgressDashboard
import kotlinx.serialization.Serializable

@Composable
fun ProgressTabHost(
    isVisible: Boolean,
    backStack: NavBackStack<ProgressDestination>,
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
        entryProvider = entryProvider<ProgressDestination> {
            registerProgressEntries()
        }
    )
}

fun EntryProviderScope<ProgressDestination>.registerProgressEntries() {
    entry<ProgressDestination.Root> {
        ProgressDashboard()
    }
}

@Serializable
sealed interface ProgressDestination : NavKey {
    @Serializable
    data object Root : ProgressDestination
}

private fun Modifier.tabHostVisibility(isVisible: Boolean): Modifier =
    if (isVisible) {
        this.zIndex(1f)
    } else {
        this.clearAndSetSemantics { }.zIndex(0f)
    }
