package com.aml_sakr.fitlife.feature.session.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aml_sakr.fitlife.feature.session.domain.equipment.ExerciseAlternative

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentUnavailableBottomSheet(
    alternatives: List<ExerciseAlternative>,
    isLoading: Boolean,
    onAlternativeSelected: (ExerciseAlternative) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Equipment Unavailable?",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Choose an alternative exercise that targets the same muscles with your available equipment.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (alternatives.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No alternatives found for your equipment.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 450.dp), // Prevent full-screen expansion
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alternatives.take(3)) { alternative -> // AC 2: Exactly 3
                        AlternativeCard(
                            alternative = alternative,
                            onSelect = { onAlternativeSelected(alternative) }
                        )
                    }
                }
            }
        }
    }
}
