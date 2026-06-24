package com.aml_sakr.fitlife.feature.home.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.aml_sakr.fitlife.feature.session.ui.navigation.SessionStartButton
import kotlinx.serialization.Serializable

@Composable
fun HomeTabHost(
    isVisible: Boolean,
    backStack: NavBackStack<HomeDestination>,
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
        entryProvider = entryProvider<HomeDestination> {
            registerHomeEntries(backStack)
        }
    )
}

fun EntryProviderScope<HomeDestination>.registerHomeEntries(backStack: NavBackStack<HomeDestination>) {
    entry<HomeDestination.Root> {
        HomeTabScreen(
            onOpenDetail = {
                backStack.add(HomeDestination.Detail)
            }
        )
    }
    entry<HomeDestination.Detail> {
        HomeDetailScreen(
            onBack = { if (backStack.size > 1) backStack.removeLastOrNull() }
        )
    }
}

@Serializable
sealed interface HomeDestination : NavKey {
    @Serializable
    data object Root : HomeDestination

    @Serializable
    data object Detail : HomeDestination
}

private fun Modifier.tabHostVisibility(isVisible: Boolean): Modifier =
    if (isVisible) {
        this.zIndex(1f)
    } else {
        this.clearAndSetSemantics { }.zIndex(0f)
    }

@Composable
private fun HomeTabScreen(
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Home",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Your dashboard, session entry point, and plan summary live here.",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onOpenDetail) {
                Text("Open home detail")
            }
            Spacer(modifier = Modifier.height(16.dp))
            SessionStartButton()
        }
    }
}

@Composable
private fun HomeDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Home detail",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "This nested screen proves the Home tab keeps its own back stack.",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("Back to home")
        }
    }
}
