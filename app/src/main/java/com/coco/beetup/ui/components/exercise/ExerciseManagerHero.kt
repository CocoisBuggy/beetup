package com.coco.beetup.ui.components.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ExerciseManagerHero(onAddClick: () -> Unit) {
  Column {
    Text(
        "Exercise Manager",
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.primary)

    Text(
        "It's your job. I'm not loading all possible activities in the known universe.",
        style = MaterialTheme.typography.headlineSmall)
  }

  Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
    OutlinedButton(onAddClick) {
      Text("Add Exercise")
      Icon(Icons.Default.Add, "Add Icon")
    }
  }
}
