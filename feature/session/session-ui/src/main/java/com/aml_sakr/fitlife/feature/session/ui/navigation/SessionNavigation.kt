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
import com.aml_sakr.fitlife.feature.session.ui.summary.SessionSummaryScreen
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.serialization.Serializable
import androidx.compose.ui.Modifier

fun EntryProviderScope<NavKey>.registerSessionEntries(
    onExitSession: () -> Unit,
    onNavigateToSummary: (String) -> Unit,
    onNavigateHome: () -> Unit
) {
    entry<SessionDestination.Session> { key ->
        SessionEntryDestination(
            userId = key.userId,
            planId = key.planId,
            workoutDayId = key.workoutDayId,
            onExitSession = onExitSession,
            onNavigateToSummary = onNavigateToSummary
        )
    }
    entry<SessionDestination.Summary> { key ->
        SessionSummaryScreen(
            sessionId = key.sessionId,
            onNavigateHome = onNavigateHome
        )
    }
}

interface SessionNavigator {
    fun startSession(userId: String, planId: String, workoutDayId: String)
    fun navigateToSummary(sessionId: String)
}

val LocalSessionNavigator = staticCompositionLocalOf<SessionNavigator?> { null }

@Composable
fun rememberSessionNavigator(backStack: MutableList<NavKey>): SessionNavigator {
    return remember(backStack) {
        object : SessionNavigator {
            override fun startSession(userId: String, planId: String, workoutDayId: String) {
                backStack.add(SessionDestination.Session(userId, planId, workoutDayId))
            }

            override fun navigateToSummary(sessionId: String) {
                if (backStack.isNotEmpty()) {
                    backStack.removeAt(backStack.lastIndex)
                }
                backStack.add(SessionDestination.Summary(sessionId))
            }
        }
    }
}

@Composable
fun SessionStartButton(
    userId: String,
    planId: String,
    workoutDayId: String,
    modifier: Modifier = Modifier
) {
    val navigator = LocalSessionNavigator.current
        ?: error("SessionStartButton requires a SessionNavigator")

    Button(
        onClick = { navigator.startSession(userId, planId, workoutDayId) },
        modifier = modifier
    ) {
        Text("Start session")
    }
}

@Serializable
sealed interface SessionDestination : NavKey {
    @Serializable
    data class Session(
        val userId: String,
        val planId: String,
        val workoutDayId: String
    ) : SessionDestination

    @Serializable
    data class Summary(val sessionId: String) : SessionDestination
}
