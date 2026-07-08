package com.aml_sakr.fitlife.feature.progress.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aml_sakr.fitlife.feature.progress.domain.model.SessionBasicInfo
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SessionHistoryItem(
    session: SessionBasicInfo,
    modifier: Modifier = Modifier
) {
    val dateTime = Instant.ofEpochMilli(session.startTime)
        .atZone(ZoneId.systemDefault())
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm")
    val formattedDate = dateTime.format(dateFormatter)
    
    val durationText = session.durationSeconds?.let {
        val minutes = it / 60
        val seconds = it % 60
        "${minutes}m ${seconds}s"
    } ?: "--"

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Duration: $durationText",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
