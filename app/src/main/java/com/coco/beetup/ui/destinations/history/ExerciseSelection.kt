package com.coco.beetup.ui.destinations.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.BeetExercise

@Composable
fun ExerciseSelection(exercises: List<BeetExercise>?, onSelection: (BeetExercise) -> Unit) {
  var searchQuery by remember { mutableStateOf("") }

  Text(
      "Select an Exercise",
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold)

  OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("Search exercises...") },
      singleLine = true)

  exercises?.let { exerciseList ->
    val filteredExercises =
        exerciseList.filter { it.exerciseName.contains(searchQuery, ignoreCase = true) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      filteredExercises.forEach { exercise ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onSelection(exercise) },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
              Row(
                  modifier = Modifier.fillMaxWidth().padding(16.dp),
                  verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 12.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                          text = exercise.exerciseName,
                          style = MaterialTheme.typography.bodyLarge,
                          fontWeight = FontWeight.Medium)
                      Text(
                          text = exercise.exerciseDescription,
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }
            }
      }
    }
  }
}
