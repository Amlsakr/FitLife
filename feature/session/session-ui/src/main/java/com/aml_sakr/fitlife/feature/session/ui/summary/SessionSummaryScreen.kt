package com.aml_sakr.fitlife.feature.session.ui.summary

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.ui.platform.LocalContext
import com.aml_sakr.fitlife.feature.session.ui.utils.SessionShareUtils

@Composable
fun SessionSummaryScreen(
    sessionId: String,
    onNavigateHome: () -> Unit,
    viewModel: SessionSummaryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.onEvent(SessionSummaryEvent.LoadSession(sessionId))
    }

    LaunchedEffect(Unit) {
        viewModel.actions.collect { action ->
            when (action) {
                SessionSummaryAction.NavigateHome -> onNavigateHome()
                is SessionSummaryAction.OpenShareSheet -> {
                    state.session?.let { session ->
                        // The collective effect runs in a coroutine scope
                        SessionShareUtils.shareToWhatsApp(context, session, state.caloriesBurned)
                    }
                }
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Workout Complete!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.error != null) {
                Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
            } else {
                state.session?.let { session ->
                    MetricCard(label = "Duration", value = "${(session.durationSeconds ?: 0) / 60} min")
                    MetricCard(label = "Total Sets", value = session.totalSets.toString())
                    MetricCard(label = "Total Reps", value = session.totalReps.toString())
                    MetricCard(label = "Fatigue Detected", value = session.fatigueEventCount.toString())
                    MetricCard(label = "Estimated Calories", value = "${state.caloriesBurned} kcal")

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = { viewModel.onEvent(SessionSummaryEvent.ShareToWhatsApp) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Share to WhatsApp")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { viewModel.onEvent(SessionSummaryEvent.NavigateHome) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to Home")
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
