package com.aml_sakr.fitlife.feature.progress.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aml_sakr.fitlife.feature.progress.ui.components.FitLifeBarChart
import com.aml_sakr.fitlife.feature.progress.ui.components.FitLifeLineChart
import com.aml_sakr.fitlife.feature.progress.ui.components.MetricCard
import com.aml_sakr.fitlife.feature.progress.ui.components.SessionHistoryItem
import com.aml_sakr.fitlife.feature.progress.ui.state.ProgressDashboardAction
import com.aml_sakr.fitlife.feature.progress.ui.state.ProgressDashboardEvent
import com.aml_sakr.fitlife.feature.progress.ui.state.ProgressDashboardState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressDashboard(
    modifier: Modifier = Modifier,
    viewModel: ProgressDashboardViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.action.collect { action ->
            when (action) {
                is ProgressDashboardAction.NavigateToSessionDetail -> onNavigateToDetail(action.sessionId)
            }
        }
    }

    ProgressDashboardContent(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressDashboardContent(
    state: ProgressDashboardState,
    onEvent: (ProgressDashboardEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Progress Dashboard") },
                actions = {
                    IconButton(onClick = { onEvent(ProgressDashboardEvent.RefreshRequested) }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (state.isLoading && state.analytics == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null && state.analytics == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.error)
                        Button(onClick = { onEvent(ProgressDashboardEvent.RefreshRequested) }) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        MetricCardsSection(state.analytics)
                    }

                    item {
                        Text(
                            text = "Weekly Sessions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FitLifeLineChart(
                            chartData = state.weeklyTrend,
                            isLoading = false
                        )
                    }

                    item {
                        Text(
                            text = "Daily Calories",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FitLifeBarChart(
                            chartData = state.dailyCalories,
                            isLoading = false
                        )
                    }

                    item {
                        Text(
                            text = "Recent History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (state.sessionHistory.isEmpty()) {
                        item {
                            Text(
                                text = "No sessions recorded yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(state.sessionHistory) { session ->
                            SessionHistoryItem(
                                session = session,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onEvent(ProgressDashboardEvent.SessionClicked(session.sessionId))
                                    }
                            )
                        }
                    }
                }

                if (state.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCardsSection(analytics: com.aml_sakr.fitlife.feature.progress.domain.model.ProgressAnalytics?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Total Sessions",
                value = analytics?.totalSessions?.toString() ?: "0",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Total Calories",
                value = analytics?.totalCalories?.toString() ?: "0",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Fatigue Events",
                value = analytics?.totalFatigueEvents?.toString() ?: "0",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Avg. Duration",
                value = analytics?.let { formatDuration(it.averageDurationSeconds) } ?: "0 mins",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    return "$minutes mins"
}
