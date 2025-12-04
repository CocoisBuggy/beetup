package com.coco.beetup.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.ActivityGroup
import com.coco.beetup.core.data.BeetExerciseLog
import com.coco.beetup.ui.viewmodel.BeetViewModel
import java.util.Date

@Composable
fun ActivityEntry(
    viewModel: BeetViewModel,
    day: Int,
    activity: ActivityGroup,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
  val logs by
      viewModel.exerciseLogs(day, activity.exercise.id).collectAsState(initial = emptyList())

  ListItem(
      headlineContent = { Text("${activity.exercise.exerciseName}") },
      supportingContent = {
        Column {
            var text = "${logs.size} sets"

            if (logs.isNotEmpty()) {
                text += ", ${logs.first().log.magnitude} ${activity.magnitude.name}"
            }

            Text(text)

          AnimatedVisibility(isSelected) {
            Column {
              for (logEntry in logs) {
                val resistances =
                    logEntry.resistances.joinToString(separator = ", ") {
                      "${it.entry.resistanceValue} ${it.extra.name}"
                    }
                ListItem(
                    headlineContent = {
                      Text(
                          text = "${activity.magnitude.name}: ${logEntry.log.magnitude}",
                          style = MaterialTheme.typography.bodyLarge)
                    },
                    supportingContent =
                        if (resistances.isNotEmpty()) {
                          { Text(text = resistances, style = MaterialTheme.typography.bodySmall) }
                        } else null,
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.padding(start = 16.dp))
              }
            }
          }
        }
      },
      leadingContent = { Icon(Icons.Default.FitnessCenter, contentDescription = "Exercise Icon") },
      trailingContent = {
        AnimatedVisibility(isSelected) {
          FilledTonalButton(
              onClick = {
                if (logs.isNotEmpty()) {
                  viewModel.insertActivity(logs.last().log.copy(id = 0, logDate = Date()))
                } else {
                  viewModel.insertActivity(
                      BeetExerciseLog(exerciseId = activity.exercise.id, logDate = Date()))
                }
              }) {
                Text("+1")
              }
        }
      },
      colors =
          ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
      modifier =
          modifier
              .padding(vertical = 4.dp)
              .clip(MaterialTheme.shapes.medium)
              .border(
                  width = 1.dp,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                  shape = MaterialTheme.shapes.medium))
}
