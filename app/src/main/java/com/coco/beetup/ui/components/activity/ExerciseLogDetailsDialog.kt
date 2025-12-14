package com.coco.beetup.ui.components.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.BeetExercise

@Composable
fun ExerciseLogDetailsDialog(
    exercise: BeetExercise,
    magnitudeName: String,
    magnitudeUnit: String,
    onConfirm: (magnitude: Int) -> Unit,
    onDismiss: () -> Unit,
) {
  var magnitude by remember { mutableIntStateOf(1) }

  AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(text = exercise.exerciseName) },
      text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
              text = "$magnitudeName ($magnitudeUnit)",
              style = MaterialTheme.typography.titleMedium,
              modifier = Modifier.padding(bottom = 8.dp))
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
              modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { if (magnitude > 0) magnitude-- }) {
                  Icon(Icons.Default.Remove, contentDescription = "Decrement")
                }
                Text(
                    text = magnitude.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 16.dp))
                IconButton(onClick = { magnitude++ }) {
                  Icon(Icons.Default.Add, contentDescription = "Increment")
                }
              }
        }
      },
      confirmButton = { TextButton(onClick = { onConfirm(magnitude) }) { Text("Confirm") } },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
