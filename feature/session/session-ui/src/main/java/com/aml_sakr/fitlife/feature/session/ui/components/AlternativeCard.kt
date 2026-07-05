package com.aml_sakr.fitlife.feature.session.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aml_sakr.fitlife.feature.session.domain.equipment.ExerciseAlternative

@Composable
fun AlternativeCard(
    alternative: ExerciseAlternative,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // AC 6: Exercise Image Placeholder
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = alternative.name.firstOrNull()?.toString() ?: "",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = alternative.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Equipment: ${alternative.equipmentRequired}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = alternative.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select")
            }
        }
    }
}
