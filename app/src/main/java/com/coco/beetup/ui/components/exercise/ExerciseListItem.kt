package com.coco.beetup.ui.components.exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.coco.beetup.core.data.BeetExercise
import com.coco.beetup.ui.viewmodel.BeetViewModel

@Composable
fun ExerciseListItem(
    isSelected: Boolean,
    exercise: BeetExercise,
    usageCount: Int,
    animatedPadding: Dp,
    viewModel: BeetViewModel,
    onClick: (Int?) -> Unit,
    onRemoveResistanceReference: (Int) -> Unit,
    onAddResistance: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

  ListItem(
      modifier =
          Modifier.clickable { onClick(if (isSelected) null else exercise.id) }
              .padding(vertical = animatedPadding)
              .border(
                  width = 1.dp,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                  shape = MaterialTheme.shapes.medium),
      overlineContent = { Text("$usageCount entries") },
      headlineContent = {
        Text(exercise.exerciseName, style = MaterialTheme.typography.titleMedium)
      },
      supportingContent = {
        Column {
          Text(exercise.exerciseDescription)
          if (isSelected) {
            val resistances by
                viewModel.validResistancesFor(exercise.id).collectAsState(initial = null)

            AnimatedVisibility(visible = resistances != null) {
              resistances?.let { resistances ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  for (res in resistances) {
                    ListItem(
                        headlineContent = {
                          Text(res.name, style = MaterialTheme.typography.labelMedium)
                        },
                        supportingContent = {
                          Text(res.description, style = MaterialTheme.typography.labelSmall)
                        },
                        trailingContent = {
                          IconButton({ onRemoveResistanceReference(res.id) }) {
                            Icon(
                                Icons.Default.RemoveCircleOutline,
                                "",
                                tint = MaterialTheme.colorScheme.error)
                          }
                        })
                  }

                  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Button({ onAddResistance() }) {
                      Icon(Icons.Default.Add, "")
                      Text("Add Resistance Type")
                    }
                  }

                  Row(
                      Modifier.fillMaxWidth(),
                      horizontalArrangement =
                          Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                        Button(onClick = onEdit) {
                          Icon(Icons.Default.Edit, contentDescription = null)
                          Text("Edit")
                        }
                        Button(
                            onClick = onDelete,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error)) {
                              Icon(Icons.Default.Delete, contentDescription = null)
                              Text("Delete")
                            }
                      }
                }
              }
            }
          }
        }
      })
}
