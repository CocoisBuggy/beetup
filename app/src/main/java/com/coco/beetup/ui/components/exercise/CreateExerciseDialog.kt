package com.coco.beetup.ui.components.exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring.DampingRatioLowBouncy
import androidx.compose.animation.core.Spring.StiffnessMedium
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.coco.beetup.core.data.BeetExercise
import com.coco.beetup.core.data.BeetMagnitude
import com.coco.beetup.ui.components.grain.DynamicBlobShape
import com.coco.beetup.ui.viewmodel.BeetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExerciseDialog(
    viewModel: BeetViewModel,
    showExerciseDialog: Boolean,
    onDismiss: () -> Unit
) {
  var name by remember { mutableStateOf("") }
  var desc by remember { mutableStateOf("") }
  var magnitude by remember { mutableStateOf<BeetMagnitude?>(null) }

  val magnitudes by viewModel.allMagnitudes().collectAsState(initial = null)

  LaunchedEffect(showExerciseDialog) {
    if (showExerciseDialog) {
      name = ""
      desc = ""
      magnitude = null
    }
  }

  if (showExerciseDialog) {
    val isNamed = name.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Exercise") },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        text = {
          Box(modifier = Modifier.widthIn(min = 280.dp, max = 360.dp).padding(24.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                  TextField(
                      value = name,
                      onValueChange = { name = it },
                      label = { Text("Exercise Name") },
                      singleLine = true)

                  AnimatedVisibility(
                      visible = isNamed,
                      enter = slideInVertically { h -> h } + fadeIn(),
                      exit = slideOutVertically { h -> -h } + fadeOut()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)) {
                              TextField(
                                  value = desc,
                                  onValueChange = { desc = it },
                                  label = { Text("Description") })

                              if (desc.isNotBlank()) {
                                if (magnitudes != null) {
                                  MagnitudeSelector(
                                      magnitudes = magnitudes!!,
                                      selectedMagnitude = magnitude,
                                      onMagnitudeSelected = { magnitude = it })
                                } else {
                                  CircularProgressIndicator()
                                }
                              }
                            }
                      }
                }
          }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        confirmButton = {
          Button(
              onClick = {
                viewModel.insertExercise(
                    BeetExercise(
                        exerciseName = name,
                        exerciseDescription = desc,
                        magnitudeKind = magnitude?.id ?: 0))
                onDismiss()
              },
              enabled = name.isNotBlank() && desc.isNotBlank() && magnitude != null) {
                Text("Add")
                Icon(Icons.Default.Add, "")
              }
        })
  }
}
