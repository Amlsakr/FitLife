package com.aml_sakr.fitlife.feature.session.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.aml_sakr.fitlife.feature.session.ui.SessionEntryDestination
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.serialization.Serializable
import androidx.compose.ui.Modifier

fun EntryProviderScope<NavKey>.registerSessionEntries(
    onExitSession: () -> Unit
) {
    entry<SessionDestination.Session> {
        SessionEntryDestination(onExitSession = onExitSession)
    }
}

interface SessionNavigator {
    fun startSession()
}

val LocalSessionNavigator = staticCompositionLocalOf<SessionNavigator?> { null }

@Composable
fun rememberSessionNavigator(backStack: MutableList<NavKey>): SessionNavigator {
    return remember(backStack) {
        object : SessionNavigator {
            override fun startSession() {
                backStack.add(SessionDestination.Session)
            }
        }
    }
}

@Composable
fun SessionStartButton(modifier: Modifier = Modifier) {
    val navigator = LocalSessionNavigator.current
        ?: error("SessionStartButton requires a SessionNavigator")

    Button(
        onClick = { navigator.startSession() },
        modifier = modifier
    ) {
        Text("Start session")
    }
}

@Serializable
sealed interface SessionDestination : NavKey {
    @Serializable
    data object Session : SessionDestination
}
